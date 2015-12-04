/*
 * document library for getting, deleting and updating documents and metadata
 */
var fs = require('fs'),
    db = require('../lib/dbLib'),
    crypto = require('../lib/cryptoLib');

var prefix = './docs/';

exports.opEnum = {
    checkIn: 0,
    checkOut: 1,
    both: 2,
    owner: 3
};

exports.secFlag = {
    none: 0,
    confidentiality: 1,
    integrity: 2,
    both: 3
};

// filter expired ace
filterExpired = function (acl) {
    var now = Date.now();
    var _acl = [];
    for (var i = 0;i < acl.length;i++) {
        if (acl[i].lifetime < 0) {
            _acl.push(acl[i]);
        }
        else {
            var elapsed = (now - acl[i].timestamp) / 1000.0;
            if (elapsed < acl[i].lifetime) {
                _acl.push(acl[i]);
            }
        }
    }
    return _acl;
};

filterDuplicate = function (acl) {
    var timestamp = acl[0].timestamp;
    var index = 0;
    for (var i = 0;i < acl.length;i++) {
        if (acl[i].lifetime < 0) {
            return acl[i];
        }
        else {
            if (acl[i].timestamp - timestamp > 0) {
                index = i;
                timestamp = acl[i].timestamp;
            }
        }
    }
    return acl[index];
};

// check if permission grants operation
contains = function (permission, operation) {
    if (permission == 3) return true;
    if (operation == 3) return false;
    if (permission == 2) return true;
    return (permission == operation);
};

exports.ifNew = function (uid) {
    try {
        fs.statSync(prefix + uid);
        return false;
    }
    catch (e) {
        return true;
    }
};

exports.checkPermit = function (acl, client, operation) {
    var _acl = filterExpired(acl);
    for (var i = 0;i < _acl.length;i++) {
        if (((_acl[i].name === client) || (_acl[i].name === 'all')) && contains(_acl[i].permission, operation)) {
            return true;
        }
    }
    return false;
};

exports.checkDelegate = function (name, message, acl, callback) {
    var _acl = filterExpired(acl);
    var found = false;
    var _acls = [];
    for (var i = 0;i < _acl.length;i++) {
        if (((_acl[i].name === name) || (_acl[i].name === 'all')) && contains(_acl[i].permission, message.permission) && _acl[i].propagation) {
            found = true;
            _acls.push(_acl[i]);
        }
    }
    if (found) {
        callback(null, filterDuplicate(_acls));
    }
    else {
        callback(new Error(), null);
    }
};

exports.getDoc = function (meta, callback) {
    var path = prefix + meta.UID;
    var key = crypto.RSADecrypt(new Buffer(meta.key, 'base64'), 'hex');
    if (meta.flag == this.secFlag.none) {
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else callback(null, data);
        });
    }
    else if (meta.flag == this.secFlag.confidentiality) {
        fs.readFile(path, {encoding:'hex'}, function (err, data) {
            if (err) callback(err, null);
            else {
                callback(null, crypto.AESDecrypt(data, key, 'utf8'));
            }
        });
    }
    else if (meta.flag == this.secFlag.integrity) {
        var decryptedSignature = crypto.AESDecrypt(meta.signature, key, 'hex');
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else {
                var signature = crypto.hash(data);
                if (signature !== decryptedSignature) callback(new Error(), null);
                else callback(null, data);
            }
        });
    }
    else {
        decryptedSignature = crypto.AESDecrypt(meta.signature, key, 'hex');
        fs.readFile(path, {encoding:'hex'}, function (err, data) {
            if (err) callback(err, null);
            else {
                var plainText = crypto.AESDecrypt(data, key, 'utf8');
                var signature = crypto.hash(plainText);
                if (signature !== decryptedSignature) callback(new Error(), null);
                else callback(null, plainText);
            }
        });
    }
};

exports.addDoc = function (channel, message, callback) {
    var path = prefix + message.uid;
    var meta = {
        UID: message.uid,
        flag: message.flag,
        signature: '',
        key: '',
        acl: [{
            name: channel.clientName,
            timestamp: new Date(),
            lifetime: -1,
            signature: '',
            permission: 3,
            propagation: true
        }]
    };
    if (message.flag == this.secFlag.none) {
        db.insertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, message.document, function (err) {
                    callback(err);
                });
            }
        });
    }
    else if (message.flag == this.secFlag.integrity) {
        var key = crypto.generateAESKey();
        var signature = crypto.hash(message.document);
        var encryptedSignature = crypto.AESEncrypt(signature, key, 'hex');
        var encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        meta.signature = encryptedSignature;
        meta.key = encryptedKey;
        db.insertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, message.document, function (err) {
                    callback(err);
                });
            }
        });
    }
    else if (message.flag == this.secFlag.confidentiality) {
        key = crypto.generateAESKey();
        encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        cipherText = crypto.AESEncrypt(message.document, key);
        meta.key = encryptedKey;
        db.insertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, {encoding:'hex'}, function (err) {
                    callback(err);
                });
            }
        });
    }
    else {
        key = crypto.generateAESKey();
        signature = crypto.hash(message.document);
        encryptedSignature = crypto.AESEncrypt(signature, key, 'hex');
        encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        var cipherText = crypto.AESEncrypt(message.document, key);
        meta.key = encryptedKey;
        meta.signature = encryptedSignature;
        db.insertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, {encoding:'hex'}, function (err) {
                    callback(err);
                });
            }
        });
    }
};

exports.updateDoc = function (channel, message, meta, callback) {
    var path = prefix + message.uid;
    meta.uid = message.uid;
    meta.flag = message.flag;
    meta.signature =  '';
    meta.key = '';
    if (meta.UID) delete meta.UID;
    if (message.flag == this.secFlag.none) {
        db.updateMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, message.document, function (err) {
                    callback(err);
                });
            }
        });
    }
    else if (message.flag == this.secFlag.integrity) {
        var key = crypto.generateAESKey();
        var signature = crypto.hash(message.document);
        var encryptedSignature = crypto.AESEncrypt(signature, key, 'hex');
        var encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        meta.signature = encryptedSignature;
        meta.key = encryptedKey;
        db.updateMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, message.document, function (err) {
                    callback(err);
                });
            }
        });
    }
    else if (message.flag == this.secFlag.confidentiality) {
        key = crypto.generateAESKey();
        encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        cipherText = crypto.AESEncrypt(message.document, key);
        meta.key = encryptedKey;
        db.updateMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, {encoding:'hex'}, function (err) {
                    callback(err);
                });
            }
        });
    }
    else {
        key = crypto.generateAESKey();
        signature = crypto.hash(message.document);
        encryptedSignature = crypto.AESEncrypt(signature, key, 'hex');
        encryptedKey = crypto.RSAEncrypt(key, 'base64', 'hex');
        var cipherText = crypto.AESEncrypt(message.document, key);
        meta.key = encryptedKey;
        meta.signature = encryptedSignature;
        db.updateMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, {encoding:'hex'}, function (err) {
                    callback(err);
                });
            }
        });
    }
};

exports.deleteDoc = function (uid, callback) {
    fs.unlink(prefix + uid, function (err) {
        callback();
    });
};
