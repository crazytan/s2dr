/*
 * document library for getting, deleting and updating documents and metadata
 */
var fs = require('fs');
var db = require('../lib/dbLib');
var crypto = require('../lib/cryptoLib');

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

// check if permission grants operation
contains = function (permission, operation) {
    if (permission == this.opEnum.owner) return true;
    if (operation == this.opEnum.owner) return false;
    if (permission == this.opEnum.both) return true;
    return (permission == operation);
};

exports.checkPermit = function (acl, client, operation) {
    var _acl = filterExpired(acl);
    for (var i = 0;i < _acl.length;i++) {
        if ((_acl[i].name === client) && contains(_acl[i].permission, operation)) {
            return true;
        }
    }
    return false;
};

exports.checkDelegate = function (message, acl, callback) {
    var _acl = filterExpired(acl);
    for (var i = 0;i < _acl.length;i++) {
        if ((_acl[i].name === message.client) && contains(_acl[i].permission, message.permission) && _acl[i].propagation) {
            callback(null, _acl[i]);
        }
    }
    callback(new Error(), null);
};

exports.getDoc = function (meta, callback) {
    var path = '../docs/' + meta.UID;
    var key = crypto.decryptKey(meta.key);
    if (meta.flag == this.secFlag.none) {
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else callback(null, data);
        });
    }
    else if (meta.flag == this.secFlag.confidentiality) {
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else {
                callback(null, crypto.decryptAES(data, key));
            }
        });
    }
    else if (meta.flag == this.secFlag.integrity) {
        var decryptedSignature = crypto.decryptAES(meta.signature, key);
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
        var decryptedSignature = crypto.decryptAES(meta.signature, key);
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else {
                var plainText = crypto.decryptAES(data, key);
                var signature = crypto.hash(plainText);
                if (signature !== decryptedSignature) callback(new Error(), null);
                else callback(null, plainText);
            }
        });
    }
};

exports.addDoc = function (channel, message, callback) {
    var meta = {
        UID: message.uid,
        /*flag: message.flag,
        signature: '',
        key: '',*/
        acl: [{
            name: channel.client,
            timestamp: new Date(),
            lifetime: -1,
            signature: '',
            permission: 3,
            propagation: true
        }]
    };
    this.updateDoc(channel, message, meta, callback);
};

exports.updateDoc = function (channel, message, meta, callback) {
    var path = '../docs/' + message.uid;
    meta.flag = message.flag;
    meta.signature =  '';
    meta.key = '';
    if (message.flag == this.secFlag.none) {
        db.upsertMeta(meta, function (err) {
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
        var encryptedSignature = crypto.encryptAES(signature, key);
        var encryptedKey = crypto.encryptKey(key);
        meta.signature = encryptedSignature;
        meta.key = encryptedKey;
        db.upsertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, message.document, function (err) {
                    callback(err);
                });
            }
        });
    }
    else if (message.flag == this.secFlag.confidentiality) {
        var key = crypto.generateAESKey();
        var encryptedKey = crypto.encryptKey(key);
        var cipherText = crypto.encryptAES(message.document, key);
        meta.key = encryptedKey;
        db.upsertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, function (err) {
                    callback(err);
                });
            }
        });
    }
    else {
        var key = crypto.generateAESKey();
        var signature = crypto.hash(message.document);
        var encryptedSignature = crypto.encryptAES(signature, key);
        var encryptedKey = crypto.encryptKey(key);
        var cipherText = crypto.encryptAES(message.document, key);
        meta.key = encryptedKey;
        meta.signature = encryptedSignature;
        db.upsertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, function (err) {
                    callback(err);
                });
            }
        });
    }
};

exports.deleteDoc = function (uid, callback) {
    fs.unlink('../docs/' + uid, function (err) {
        callback();
    });
};
