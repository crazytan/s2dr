/*
 *  node.js server for secure shared document repository
 */
var express = require('express');
var bodyParser = require('body-parser');
var db = require('./lib/dbLib');
var crypto = require('./lib/cryptoLib');
crypto.init();

// middleware for decrypting incoming messages
var decrypt = express.Router().post('/', function(req, res) {
    db.getChannel(req.body.identifier, function (err, channel) {
        if (err) {
            res.json({
                result: 1,
                identifier: null,
                message: 'invalid identifier!'
            });
        }
        else {
            var plainText = crypto.decryptAES(req.body.message, channel.key);
            req.s2dr.message = JSON.parse(plainText);
            req.s2dr.channel = channel;
            next();
        }
    });
});

// middleware for encrypting outgoing messages
var encrypt = express.Router().post('/', function(req, res) {
    var plainText = JSON.stringify(req.s2dr.response);
    var cipherText = crypto.encryptAES(plainText, req.s2dr.channel.key);
    res.json({
        result: 0,
        identifier: req.s2dr.channel.myID,
        message: cipherText
    });
});

var app = express();

// set up body parser
app.use(bodyParser.json());

// set up routers for client calls
app.use('/init', require('./routes/init'));
app.use('/checkout', decrypt, require('./routes/checkout'), encrypt);
app.use('/checkin', decrypt, require('./routes/checkin'), encrypt);
app.use('/delegate', decrypt, require('./routes/delegate'), encrypt);
app.use('/delete', decrypt, require('./routes/delete'), encrypt);
app.use('/terminate', decrypt, require('./routes/terminate'), encrypt);

// catch 404 and return error message
app.use(function(req, res, next) {
    res.json({
        result: 1,
        identifier: null,
        message: 'url or method error!'
    });
});

var server = app.listen(8888, function() {
    var host = server.address().address;
    var port = server.address().port;

    console.log('Server starts listening on http://%s:%s', host, port);
});
