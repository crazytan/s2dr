var express = require('express');
var crypto = require('../lib/cryptoLib');
var db = require('../lib/dbLib');
var NodeRSA = require('node-rsa');
var router = express.Router();

checkCertificate = function (certificate, res) {
    var isValid = crypto.checkCertificate(certificate);
    if (!isValid) {
        res.json({
            result:1,
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
            result:1,
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
        db.insertChannel(certificate.subject, certificate.publickey, function (err) {
            if (err) {
                res.json({
                    result:1,
                    message:'unable to insert into db',
                    signature:'',
                    certificate:{}
                });
            }
            else {
                res.json({
                    result:0,
                    message:'',
                    signature:'',
                    certificate:JSON.stringify(crypto.getCertificate())
                });
            }
        });
    }
    else if (req.body.phase === 2) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
        checkSignature(req.body, res);
        db.getChannelByClient(certificate.publickey, function (err, channel) {
            if (err) {
                res.json({
                    result:1,
                    message:'unable to get channel information',
                    signature:'',
                    certificate:{}
                });
            }
            else {
                var J = crypto.decryptSecureMessage(req.body.message);
                var _J = new Buffer(crypto.generateAESKey(), 'hex');
                if (J.length !==  _J.length) {
                    res.json({
                        result:1,
                        message:'key size does not match',
                        signature:'',
                        certificate:{}
                    });
                }
                var keyBuf = new Buffer(J.length);
                for (var i = 0;i < keyBuf.length;i++) keyBuf[i] = J[i] ^ _J[i];
                channel.key = crypto.hash(keyBuf.toString('hex'));
                db.updateChannel(channel, function (err) {
                    if (err) {
                        res.json({
                            result:1,
                            message:'unable to insert key into db',
                            signature:'',
                            certificate:{}
                        });
                    }
                    else {
                        var clientKey = new NodeRSA(channel.clientPublicKey, 'pkcs8-public');
                        var encryptedKey = clientKey.encrypt(keyBuf,'hex');
                        res.json({
                            result:0,
                            message:encryptedKey,
                            signature:crypto.sign(encryptedKey),
                            certificate:JSON.stringify(crypto.getCertificate)
                        });
                    }
                });
            }
        });
    }
    else if (req.body.phase === 3) {
        // TODO
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
    }
    else {
        res.json({
            result:1,
            message:'phase number error',
            signature:'',
            certificate:{}
        });
    }
});

module.exports = router;
