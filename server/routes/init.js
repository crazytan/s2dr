var express = require('express');
var crypto = require('../lib/cryptoLib');
var db = require('../lib/dbLib');
var NodeRSA = require('node-rsa');
var router = express.Router();

checkCertificate = function (certificate, res, callback) {
    crypto.checkCertificate(certificate, function (err) {
        if (err) {
            res.json({
                result:"1",
                message: 'certificate not valid',
                signature:'',
                certificate:{}
            });
        }
        else
            callback();
    });
};

checkSignature = function (body, res, callback) {
    crypto.checkSignature(body.message, body.signature, body.certificate, function (err) {
        if (err) {
            res.json({
                result:"1",
                message: 'signature not valid',
                signature:'',
                certificate:{}
            });
        }
        else
            callback();
    });
};

router.post('/', function(req, res) {
    if (req.body.phase === 1) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res, function () {
            crypto.extractPublicKey(certificate, function (key) {
                crypto.extractSubject(certificate, function (subject) {
                    db.getChannelByPublicKey(key, function (err, channel) {
                        if (err) {
                            db.insertChannel(subject, key, function (err) {
                                if (err) {
                                    res.json({
                                        result:"1",
                                        message: 'unable to insert into db',
                                        signature: '',
                                        certificate: {}
                                    });
                                }
                                else {
                                    res.json({
                                        result:"0",
                                        message: '',
                                        signature: '',
                                        certificate: crypto.getCertificate()
                                    });
                                }
                            });
                        }
                        else {
                            res.json({
                                result:"1",
                                message:'channel already exists',
                                signature:'',
                                certificate:{}
                            });
                        }
                    });
                });
            });
        });
    }
    else if (req.body.phase === 2) {
        certificate = req.body.certificate;
        checkCertificate(certificate, res, function () {
            crypto.extractPublicKey(certificate, function (key) {
                checkSignature(req.body, res, function () {
                    db.getChannelByPublicKey(key, function (err, channel) {
                        if (err) {
                            res.json({
                                result:"1",
                                message: 'unable to get channel information',
                                signature: '',
                                certificate: {}
                            });
                        }
                        else {
                            var J = crypto.RSADecrypt(new Buffer(req.body.message, 'hex'));
                            var _J = new Buffer(crypto.generateAESKey(), 'hex');
                            if (J.length !== _J.length) {
                                res.json({
                                    result:"1",
                                    message: 'key size does not match',
                                    signature: '',
                                    certificate: {}
                                });
                            }
                            var keyBuf = new Buffer(J.length);
                            for (var i = 0; i < keyBuf.length; i++) keyBuf[i] = J[i] ^ _J[i]; // calculate J xor _J
                            channel.key = crypto.hash(keyBuf).substring(0, 32);
                            db.updateChannel(channel, function (err) {
                                if (err) {
                                    res.json({
                                        result:"1",
                                        message: 'unable to insert key into db',
                                        signature: '',
                                        certificate: {}
                                    });
                                }
                                else {
                                    var clientKey = new NodeRSA(channel.clientPublicKey, 'pkcs8-public');
                                    var encryptedKey = clientKey.encrypt(_J, 'hex');
                                    res.json({
                                        result:"0",
                                        message: encryptedKey,
                                        signature: crypto.sign(encryptedKey),
                                        certificate: crypto.getCertificate()
                                    });
                                }
                            });
                        }
                    });
                });
            });
        });
    }
    else if (req.body.phase === 3) {
        certificate = req.body.certificate;
        checkCertificate(certificate, res, function () {
            crypto.extractPublicKey(certificate, function (key) {
                checkSignature(req.body, res, function () {
                    db.getChannelByPublicKey(key, function (err, channel) {
                        if (err) {
                            res.json({
                                result:"1",
                                message: 'unable to get channel information',
                                signature: '',
                                certificate: {}
                            });
                        }
                        else {
                            channel.clientID = req.body.message;
                            channel.myID = crypto.encryptHex(channel.key);
                            db.updateChannel(channel, function (err) {
                                if (err) {
                                    res.json({
                                        result:"1",
                                        message: 'unable to insert identifier into db',
                                        signature: '',
                                        certificate: {}
                                    });
                                }
                                else {
                                    res.json({
                                        result:"0",
                                        message: channel.myID,
                                        signature: crypto.sign(channel.myID),
                                        certificate: crypto.getCertificate()
                                    });
                                }
                            });
                        }
                    });
                });
            });
        });
    }
    else {
        res.json({
            result:"1",
            message:'phase number error',
            signature:'',
            certificate:{}
        });
    }
});

module.exports = router;
