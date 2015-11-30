var express = require('express');
var crypto = require('../lib/cryptoLib');
var db = require('../lib/dbLib');
var router = express.Router();

checkCertificate = function (certificate, res) {
    var ifValid = crypto.checkCertificate(certificate);
    if (!ifValid) {
        res.json({
            result:1,
            message: 'certificate not valid',
            signature:'',
            certificate:{}
        });
    }
};

router.post('/', function(req, res) {
    if (req.body.phase == 1) {
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
    else if (req.body.phase == 2) {
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);

    }
    else if (req.body.phase == 3) {
        // TODO
        var certificate = req.body.certificate;
        checkCertificate(certificate, res);
    }
    else {
        res.json({
            result:1,
            message:'phase error',
            signature:'',
            certificate:{}
        });
    }
});

module.exports = router;
