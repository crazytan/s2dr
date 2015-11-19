var express = require('express');
var db = require('../lib/dbLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    db.getMeta(req.body.uid, function (err, meta) {
        var acl = meta.acl;
        // TODO
    });
});

module.exports = router;
