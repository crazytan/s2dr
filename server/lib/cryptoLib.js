/*
 * a cryptography module
 */
var crypto = require('crypto'),
    NodeRSA = require('node-rsa'),
    fs = require('fs'),
    ifInit = false, // if CA is set up
    CAPublic = new NodeRSA(), // CA's public key
    CAPrivate = new NodeRSA(), // CA's private key
    masterKey = ''; // master key

const aesKeyLen = 256,  // # of bits of AES key
      aesAlgorithm = 'aes-256-ctr',
      CAPublicPath = '../CA.pub', // path of CA's public key
      CAPrivatePath = '../CA'; // path of CA's private key

// set up CA
exports.init = function () {
    if (ifInit) return;
    // set up public key for CA
    var publicBuf = fs.readFileSync(CAPublicPath);
    CAPublic.importKey(publicBuf, 'pkcs8-public');

    // set up private key for CA
    var privateBuf = fs.readFileSync(CAPrivatePath);
    CAPrivate.importKey(privateBuf, 'pkcs1-private');

    masterKey = this.generateAESKey();
    ifInit = true;
};

// return decrypted string in HEX
exports.decryptAES = function (m, key) {
    var cipher = crypto.createDecipher(aesAlgorithm, key);
    var decrypted = cipher.update(m.toString('hex'), 'hex', 'utf8');
    decrypted += cipher.final('utf8');
    return decrypted;
};

// return encrypted string in HEX
exports.encryptAES = function (m, key) {
    var cipher = crypto.createCipher(aesAlgorithm, key);
    var encrypted = cipher.update(m, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};

exports.encryptByMaster = function (m) {
    return this.encryptAES(m, masterKey);
};

exports.decryptKey = function (encryptedKey) {
    return CAPrivate.decrypt(encryptedKey, 'hex');
};

exports.encryptKey = function (key) {
    return CAPublic.encrypt(key, 'base64', 'hex');
};

exports.hash = function (m) {
    var shasum = crypto.createHash('sha256');
    shasum.update(m.toString('binary'), 'binary');
    return shasum.digest('hex');
};

exports.generateAESKey = function () {
    const key = crypto.randomBytes(aesKeyLen / 8);
    return key.toString('hex');
};
