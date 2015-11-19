var express = require('express');
var db = require('../lib/dbLib');
var doc = require('../lib/docLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    var response = req.s2dr.response;
    db.getMeta(req.body.uid, function (err, meta) {
        if (err) {
            response.result = 1;
            response.message = 'error getting metadata!';
            next();
        }
        else {
            var ifPermit = doc.checkPermit(meta.acl, req.s2dr.channel.client, doc.opEnum.checkOut);
            if (!ifPermit) {
                response.result = 1;
                response.message = 'permission denied!';
                next();
            }
            else {
                doc.getDoc(meta, function (err, document) {
                    if (err) {
                        response.result = 1;
                        response.message = 'document retrieval failed!';
                        next();
                    }
                    response.result = 0;
                    response.message = document;
                    next();
                });
            }
        }
    });
});

module.exports = router;
