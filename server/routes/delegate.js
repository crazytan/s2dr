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
            doc.checkDelegate(req.s2dr.message, meta.acl, function (err, entry) {
                if (err) {
                    response.result = 1;
                    response.message = 'unable to delegate: permission denied!';
                    next();
                }
                else {
                    db.delegate(req.s2dr.message, meta.acl, ace, function (err) {
                        if (err) {
                            response.result = 1;
                            response.message = 'unable to delegate: db operation failed!';
                            next();
                        }
                        else {
                            response.result = 0;
                            response.message = '';
                            next();
                        }
                    });
                }
            });
        }
    });
});

module.exports = router;
