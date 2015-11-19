var express = require('express');
var path = require('path');

var decrypt = require('./routes/decrypt');
var init_session = require('./routes/init');
var check_out = require('./routes/checkout');
var check_in = require('./routes/checkin');
var delegate = require('./routes/delegate');
var safe_delete = require('./routes/delete');
var terminate_session = require('./routes/terminate');

var app = express();

app.use('/init', init_session);
app.use('/checkout', decrypt, check_out);
app.use('/checkin', decrypt, check_in);
app.use('/delegate', decrypt, delegate);
app.use('/delete', decrypt, safe_delete);
app.use('/terminate', decrypt, terminate_session);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

var server = app.listen(8888, function() {
    var host = server.address().address;
    var port = server.address().port;

    console.log('Server starts listening on http://%s:%s', host, port);
});