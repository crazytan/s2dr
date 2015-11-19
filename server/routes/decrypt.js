/*
 *  middleware for decrypting incoming messages
 */
var express = require('express');
var router = express.Router();

router.post('/', function(req, res, next) {
    if (!req.body.identifier) {
        res.json({
            result: 1,
            identifier: null,
            message: 'invalid identifier!'
        });
    }
    // console.log("decrypt middleware");
});

module.exports = router;
