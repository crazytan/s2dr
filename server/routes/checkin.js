var express = require('express');
var doc = require('../lib/docLib');
var db = require('../lib/dbLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    req.s2dr.response = {};
    var response = req.s2dr.response;
    var ifNew = doc.ifNew(req.s2dr.message.uid);
    if (ifNew) {
        doc.addDoc(req.s2dr.channel, req.s2dr.message, function (err) {
            if (err) {
                response.result = 1;
                response.message = 'unable to add new document';
                next();
            }
            else {
                response.result = 0;
                response.message = '';
                next();
            }
        });
    }
    else {
        db.getMeta(req.s2dr.message.uid, function (err, meta) {
            if (err) {
                response.result = 1;
                response.message = 'unable to get metadata!';
                next();
            }
            else {
                var ifPermit = doc.checkPermit(meta.acl, req.s2dr.channel.clientName, doc.opEnum.checkIn);
                if (!ifPermit) {
                    response.result = 1;
                    response.message = 'unable to check in: permission denied!';
                    next();
                }
                else {
                    doc.updateDoc(req.s2dr.channel, req.s2dr.message, meta, function (err) {
                        if (err) {
                            response.result = 1;
                            response.message = 'unable to update the document!';
                        }
                        else {
                            response.result = 0;
                            response.message = '';
                        }
                        next();
                    });
                }
            }
        });
    }
});

module.exports = router;
