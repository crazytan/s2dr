/*
 * middleware for encrypting outgoing messages
 */

var express = require('express');
var crypto = require('../lib/cryptoLib');
var router = express.Router();

router.post('/', function(req, res) {
    var plainText = JSON.stringify(req.s2dr.response);
    var cipherText = crypto.encryptAES(plainText, req.s2dr.channel.key);
    res.json({
        result: 0,
        identifier: req.s2dr.channel.myID,
        message: cipherText
    });
});