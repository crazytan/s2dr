var express = require('express');
var db = require('../lib/dbLib');
var doc = require('../lib/docLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    db.getMeta(req.body.uid, function (err, meta) {
        var ifPermit = doc.checkPermit(meta.acl, req.s2dr.channel.client, doc.opEnum.checkOut);
        var response = req.s2dr.response;
        if (!ifPermit) {
            response.result = 1;
            response.message = 'permission denied!';
            next();
        }
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
    });
});

module.exports = router;
