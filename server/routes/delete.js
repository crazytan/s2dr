var express = require('express');
var db = require('../lib/dbLib');
var doc = require('../lib/docLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    req.s2dr.response = {};
    var response = req.s2dr.response;
    var uid = req.s2dr.message.uid;
    db.getMeta(uid, function (err, meta) {
        if (err) {
            response.result = 1;
            response.message = 'unable to get metadata!';
            next();
        }
        else {
            var ifPermit = doc.checkPermit(meta.acl, req.s2dr.channel.clientName, doc.opEnum.owner);
            if (!ifPermit) {
                response.result = 1;
                response.message = 'unable to delete: permission denied!';
                next();
            }
            else {
                db.deleteMeta(uid, function (err) {
                    if (err) {
                        response.result = 1;
                        response.message  = 'unable to delete metadata!';
                        next();
                    }
                    else {
                        doc.deleteDoc(uid, function () {
                            response.result = 0;
                            response.message = '';
                            next();
                        });
                    }
                });
            }
        }
    });
});

module.exports = router;
