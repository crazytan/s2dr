/*
 * a cryptography module
 */
var crypto = require('crypto'),
    NodeRSA = require('node-rsa'),
    fs = require('fs'),
    openssl = require('openssl-wrapper'),
    ifInit = false, // if CA is set up
    CAPublic = new NodeRSA(), // CA's public key
    CAPrivate = new NodeRSA(), // CA's private key
    myPrivate = new NodeRSA(), // server's private key
    masterKey = '', // master key
    myCert = '', // server's certificate
    CACert = ''; // CA's certificate

var aesKeyLen = 256,  // # of bits of AES key
    aesAlgorithm = 'aes-256-ctr',
    hashAlgorithm = 'sha256',
    CAPublicPath = '../CA.pub', // path of CA's public key
    CAPrivatePath = '../CA.key', // path of CA's private key
    CACertPath = '../CA.crt', // path of CA's certificate
    myPrivatePath = 'server.key', // path of server's private key
    myCertPath = 'server.crt'; // path of server's certificate

// set up CA
exports.init = function () {
    if (ifInit) return;
    // set up public key for CA
    var buf = fs.readFileSync(CAPublicPath);
    CAPublic.importKey(buf, 'pkcs8-public');

    // set up private key for CA
    buf = fs.readFileSync(CAPrivatePath);
    CAPrivate.importKey(buf, 'pkcs8-private');

    buf = fs.readFileSync(myPrivatePath);
    myPrivate.importKey(buf, 'pkcs8-private');

    buf = fs.readFileSync(myCertPath);
    myCert = buf.toString();

    buf = fs.readFileSync(CACertPath);
    CACert = buf.toString();

    // generate master key
    masterKey = this.generateAESKey();

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
    var key = crypto.randomBytes(aesKeyLen / 8);
    return key.toString('hex');
};

exports.checkCertificate = function (certificate, callback) {
    return openssl.exec('verify', new Buffer(certificate), {trusted: CACertPath}, function (err, buffer) {
        callback(err);
    });
};

exports.getCertificate = function () {
    return myCert;
};

exports.decryptSecureMessage = function (m) {
    var buf = new Buffer(m, 'hex');
    return myPrivate.decrypt(buf);
};

exports.sign = function (m) {
    var hash = this.hash(m);
    return myPrivate.encryptPrivate(hash, 'hex', 'hex');
};

exports.extractPublicKey = function (certificate, callback) {
    return openssl.exec('x509', new Buffer(certificate), {pubkey: null, noout: null}, function (err, buffer) {
        callback(buffer.toString());
    });
};

exports.extractSubject = function (certificate) {
    openssl.exec('x509', new Buffer(certificate), {subject:null, noout: null}, function (err, buffer) {
        var subject = buffer.toString();
        return subject.substring(subject.lastIndexOf('=') + 1);
    });
};

exports.checkSignature = function (m, signature, certificate, callback) {
    var hash = this.hash(m);
    this.extractPublicKey(certificate, function (keyStr) {
        var key = new NodeRSA(keyStr, 'pkcs8-public');
        var base64Sign = new Buffer(signature, 'hex').toString('base64');
        var decryptedHash = key.decryptPublic(base64Sign, 'hex');
        if (hash === decryptedHash)
            callback();
        else
            callback(new Error());
    });
};

exports.encryptHex = function (m) {
    var cipher = crypto.createCipher(aesAlgorithm, masterKey);
    var encrypted = cipher.update(m, 'hex', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};