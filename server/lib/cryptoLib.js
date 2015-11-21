/*
 * a cryptography module
 */
var crypto = require('crypto'),
    aesKeyLen = 256, // # of bits of AES key
    aesAlgorithm = 'aes-256-ctr';

// return decrypted string in HEX
exports.decryptAES = function (m, key) {
    var cipher = crypto.createCipher(aesAlgorithm, key.toString('binary'));
    var decrypted = cipher.update(m.toString('hex'), 'hex', 'hex');
    decrypted += cipher.final('hex');
    return decrypted;
};

// return encrypted string in HEX
exports.encryptAES = function (m, key) {
    var cipher = crypto.createDecipher(aesAlgorithm, key.toString('binary'));
    var encrypted = cipher.update(m.toString('hex'), 'hex', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};

exports.decryptKey = function (encryptedKey) {
// TODO
};

exports.encryptKey = function (key) {
// TODO
};

exports.hash = function (m) {
    var shasum = crypto.createHash('sha256');
    shasum.update(m.toString('binary'), 'binary');
    return shasum.digest('hex');
};

exports.generateAESKey = function () {
    const key = crypto.randomBytes(aesKeyLen / 8);
    key.toString('hex');
};
