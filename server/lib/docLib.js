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

exports.checkPermit = function (acl, client, operation) {
    // TODO: implement acl algorithm
};

exports.checkDelegate = function (message, meta, callback) {

};

exports.getDoc = function (meta, callback) {
    var path = '../docs/' + meta.UID;
    if (meta.flag == this.secFlag.none) {
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else callback(null, data);
        });
    }
    else if (meta.flag == this.secFlag.confidentiality) {
        var key = crypto.decryptKey(meta.key);
        fs.readFile(path, function (err, data) {
            if (err) callback(err, null);
            else {
                callback(null, crypto.decryptAES(data, key));
            }
        });
    }
    else if (meta.flag == this.secFlag.integrity) {
        var key = crypto.decryptKey(meta.key);
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
        var key = crypto.decryptKey(meta.key);
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
    var path = '../docs/' + message.uid;
    var meta = {
        UID: message.uid,
        flag: message.flag,
        signature: '',
        key: '',
        acl: [{
            name: channel.client,
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
        var encryptedSignature = crypto.encryptAES(signature, key);
        var encryptedKey = crypto.encryptKey(key);
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
        var key = crypto.generateAESKey();
        var encryptedKey = crypto.encryptKey(key);
        var cipherText = crypto.encryptAES(message.document, key);
        meta.key = encryptedKey;
        db.insertMeta(meta, function (err) {
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
        db.insertMeta(meta, function (err) {
            if (err) callback(err);
            else {
                fs.writeFile(path, cipherText, function (err) {
                    callback(err);
                });
            }
        });
    }
};

exports.updateDoc = function (channel, message, callback) {

};

exports.deleteDoc = function (uid, callback) {

};
