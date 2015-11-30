/*
 * a cryptography module
 */
var crypto = require('crypto'),
    NodeRSA = require('node-rsa'),
    fs = require('fs'),
    ifInit = false, // if CA is set up
    CAPublic = new NodeRSA(), // CA's public key
    CAPrivate = new NodeRSA(), // CA's private key
    myPrivate = new NodeRSA({b: 2048}), // server's private key
    masterKey = '', // master key
    certificate = {}; // server's certificate

const aesKeyLen = 256,  // # of bits of AES key
      aesAlgorithm = 'aes-256-ctr',
      hashAlgorithm = 'sha256',
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
    CAPrivate.importKey(privateBuf, 'pkcs8-private');

    // generate master key
    masterKey = this.generateAESKey();

    // make the certificate valid for one year
    var date = new Date();
    date.setFullYear(date.getFullYear() + 1);

    var myPublicStr = myPrivate.exportKey('pkcs8-public');
    // generate certificate
    certificate = {
        subject: 's2dr-server',
        validto: date.toDateString(),
        publickey: myPublicStr,
        signature: CAPrivate.sign(myPublicStr, 'hex', 'utf8')
    };
    ifInit = true;
};

// return decrypted string in HEX
exports.decryptMessage = function (m, key) {
    var cipher = crypto.createDecipher(aesAlgorithm, key);
    var decrypted = cipher.update(m.toString('hex'), 'hex', 'utf8');
    decrypted += cipher.final('utf8');
    return decrypted;
};

// return encrypted string in HEX
exports.encryptMessage = function (m, key) {
    var cipher = crypto.createCipher(aesAlgorithm, key);
    var encrypted = cipher.update(m, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};

exports.decryptSignature = function (m, key) {
    var cipher = crypto.createDecipher(aesAlgorithm, key);
    var decrypted = cipher.update(m.toString('hex'), 'hex', 'hex');
    decrypted += cipher.final('hex');
    return decrypted;
};

exports.encryptSignature = function (m, key) {
    var cipher = crypto.createCipher(aesAlgorithm, key);
    var encrypted = cipher.update(m, 'hex', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};

exports.decryptKey = function (encryptedKey) {
    return CAPrivate.decrypt(encryptedKey, 'hex');
};

exports.encryptKey = function (key) {
    return CAPublic.encrypt(key, 'base64', 'hex');
};

exports.hash = function (m) {
    var shasum = crypto.createHash(hashAlgorithm);
    shasum.update(m.toString('binary'), 'binary');
    return shasum.digest('hex');
};

exports.generateAESKey = function () {
    const key = crypto.randomBytes(aesKeyLen / 8);
    return key.toString('hex');
};

exports.checkCertificate = function (certificate) {
    var hash = this.hash(certificate.publickey);
    var base64Sign = new Buffer(certificate.signature, 'hex').toString('base64');
    var decryptedHash = CAPublic.decryptPublic(base64Sign, 'hex');
    if (hash !== decryptedHash) return false;
    var date = new Date(certificate.validto);
    var now = new Date();
    return now < date;
};

exports.getCertificate = function () {
    return certificate;
};