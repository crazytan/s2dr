/*
 *  middleware for decrypting incoming messages
 */
var express = require('express');
var db = require('../lib/dbLib');
var crypto = require('../lib/cryptoLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    db.getKey(req.body.identifier, function (err, key) {
        if (err) {
            res.json({
                result: 1,
                identifier: null,
                message: 'invalid identifier!'
            });
        }
        var plainText = crypto.decryptAES(req.body.message, key.key);
        var message = JSON.parse(plainText);
        message.name = key.client;
        next(message);
    });
});

module.exports = router;
