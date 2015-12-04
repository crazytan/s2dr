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

var AESKeyLen = 128,  // # of bits of AES key
    AESAlgorithm = 'aes-128-ecb',
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

exports.AESEncrypt = function (m, key, encoding) {
    var cipher = crypto.createCipheriv(AESAlgorithm, new Buffer(key, 'hex'), '');
    if (encoding) {
        var encrypted = cipher.update(new Buffer(m, encoding), '', 'hex');
    }
    else {
        encrypted = cipher.update(new Buffer(m), '', 'hex');
    }
    encrypted += cipher.final('hex');
    return encrypted;
};

exports.AESDecrypt = function (m, key, encoding) {
    var cipher = crypto.createDecipheriv(AESAlgorithm, new Buffer(key, 'hex'), '');
    var decrypted = cipher.update(new Buffer(m, 'hex'), '', encoding);
    decrypted += cipher.final(encoding);
    return decrypted;
};

exports.RSADecrypt = function (buf, encoding) {
    if (encoding) {
        return myPrivate.decrypt(buf, encoding);
    }
    return myPrivate.decrypt(buf);
};

exports.RSAEncrypt = function (m, encoding, sourceEncoding) {
    return myPrivate.encrypt(m, encoding, sourceEncoding);
};

exports.hash = function (m) {
    return crypto.createHash(hashAlgorithm).update(new Buffer(m)).digest('hex');
};

exports.generateAESKey = function () {
    var key = crypto.randomBytes(AESKeyLen / 8);
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

exports.sign = function (m) {
    return myPrivate.encryptPrivate(this.hash(m), 'hex', 'hex');
};

exports.extractPublicKey = function (certificate, callback) {
    return openssl.exec('x509', new Buffer(certificate), {pubkey: null, noout: null}, function (err, buffer) {
        callback(buffer.toString());
    });
};

exports.extractSubject = function (certificate, callback) {
    return openssl.exec('x509', new Buffer(certificate), {subject:null, noout: null}, function (err, buffer) {
        var subject = buffer.toString().replace('\n', '');
        callback(subject.substring(subject.lastIndexOf('=') + 1));
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
    var cipher = crypto.createCipher(AESAlgorithm, masterKey);
    var encrypted = cipher.update(m, 'hex', 'hex');
    encrypted += cipher.final('hex');
    return encrypted;
};