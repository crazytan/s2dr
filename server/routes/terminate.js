var express = require('express');
var db = require('../lib/dbLib');
var router = express.Router();

router.post('/', function(req, res, next) {
    req.s2dr.response = {};
    var response = req.s2dr.response;
    db.deleteChannel(req.s2dr.channel.clientID, function (err) {
        if (err) {
            response.result = 1;
            response.message = 'unable to terminate channel!';
            next();
        }
        else {
            response.result = 0;
            response.message = '';
            next();
        }
    });
});

module.exports = router;
