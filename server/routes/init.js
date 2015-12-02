var express = require('express');
var crypto = require('../lib/cryptoLib');
var db = require('../lib/dbLib');
var NodeRSA = require('node-rsa');
var router = express.Router();

checkCertificate = function (certificate, res) {
    var isValid = crypto.checkCertificate(certificate);
    if (!isValid) {
        res.json({
            result:"1",
            message: 'certificate not valid',
            signature:'',
            certificate:{}
        });
    }
};

checkSignature = function (body, res) {
    var isValid = crypto.checkSignature(body,message, body.signature, body.certificate);
    if (!isValid) {
        res.json({
            result:"1",
            message: 'signature not valid',
            signature:'',
            certificate:{}
        });
    }
};

router.post('/', function(req, res) {
    if (req.body.phase === 1) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
        if (!res.finish()) {
            db.insertChannel(crypto.extractSubject(certificate), crypto.extractPublicKey(certificate), function (err) {
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
    }
    else if (req.body.phase === 2) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
        if (!res.finish()) checkSignature(req.body, res);
        if (!res.finish()) {
            db.getChannelByClient(crypto.extractPublicKey(certificate), function (err, channel) {
                if (err) {
                    res.json({
                        result:"1",
                        message: 'unable to get channel information',
                        signature: '',
                        certificate: {}
                    });
                }
                else {
                    var J = crypto.decryptSecureMessage(req.body.message);
                    var _J = new Buffer(crypto.generateAESKey(), 'hex');
                    if (J.length !== _J.length) { //TODO: verify length of decrypted key
                        res.json({
                            result:"1",
                            message: 'key size does not match',
                            signature: '',
                            certificate: {}
                        });
                    }
                    var keyBuf = new Buffer(J.length);
                    for (var i = 0; i < keyBuf.length; i++) keyBuf[i] = J[i] ^ _J[i]; // calculate J xor _J
                    channel.key = crypto.hash(keyBuf.toString('hex'));
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
                            var encryptedKey = clientKey.encrypt(keyBuf, 'hex');
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
        }
    }
    else if (req.body.phase === 3) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
        if (!res.finish()) checkSignature(req.body, res);
        if (!res.finish()) {
            db.getChannelByClient(crypto.extractPublicKey(certificate), function (err, channel) {
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
        }
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
