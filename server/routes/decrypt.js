/*
 *  middleware for decrypting incoming messages
 */
var express = require('express');
var db = require('../lib/dbLib');
var crypto = require('../lib/cryptoLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    db.getKey(req.body.identifier, function (err, channel) {
        if (err) {
            res.json({
                result: 1,
                identifier: null,
                message: 'invalid identifier!'
            });
        }
        var plainText = crypto.decryptAES(req.body.message, channel.key);
        req.s2dr.message = JSON.parse(plainText);
        req.s2dr.channel = channel;
        next();
    });
});

module.exports = router;
