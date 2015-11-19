/*
 *  nodejs server for secure shared document repository
 */
var express = require('express');
var bodyParser = require('body-parser');
var decrypt = require('./routes/decrypt');

var app = express();

// set up body parser
app.use(bodyParser.json());

// set up routers for client calls
app.use('/init', require('./routes/init'));
app.use('/checkout', decrypt, require('./routes/checkout'));
app.use('/checkin', decrypt, require('./routes/checkin'));
app.use('/delegate', decrypt, require('./routes/delegate'));
app.use('/delete', decrypt, require('./routes/delete'));
app.use('/terminate', decrypt, require('./routes/terminate'));

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
