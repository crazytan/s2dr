var express = require('express');
var db = require('../lib/dbLib');
var doc = require('../lib/docLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    req.s2dr.response = {};
    var response = req.s2dr.response;
    db.getMeta(req.s2dr.message.uid, function (err, meta) {
        if (err) {
            response.result = 1;
            response.message = 'unable to get metadata!';
            next();
        }
        else {
            var ifPermit = doc.checkPermit(meta.acl, req.s2dr.channel.clientName, doc.opEnum.checkOut);
            if (!ifPermit) {
                response.result = 1;
                response.message = 'permission denied!';
                next();
            }
            else {
                doc.getDoc(meta, function (err, document) {
                    if (err) {
                        response.result = 1;
                        response.message = 'unable to retrieve document!';
                        next();
                    }
                    response.result = 0;
                    response.message = document.toString();
                    next();
                });
            }
        }
    });
});

module.exports = router;
