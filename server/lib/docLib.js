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

// check if ACE is expired
isExpired = function (ace) {
    if (ace.lifetime < 0) return false;
    var elapsed = (Date.now() - ace.timestamp) / 1000.0;
    return (elapsed >= ace.lifetime);
};

filterDuplicates = function (acl) {
    if (acl.length <= 0) return null;
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
    var _acl = [];
    for (var i = 0;i < acl.length;i++) {
        if (((acl[i].name === client) || (acl[i].name === 'all')) && contains(acl[i].permission, operation)) {
            _acl.push(acl[i]);
        }
    }
    var ace = filterDuplicates(_acl);
    if (ace && !isExpired(ace)) return ace;
    return null;
};

exports.checkDelegate = function (name, message, acl, callback) {
    var ace = this.checkPermit(acl, name, message.permission);
    if (ace && ace.propagation) {
        callback(null, ace);
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
                if (signature !== decryptedSignature) callback(new Error('file signature does not match!'), null);
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
                if (signature !== decryptedSignature) callback(new Error('file signature does not match!'), null);
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
