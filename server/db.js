/*
 * a MongoDB module for managing keys and meta-data
 */
var client = require('mongodb').MongoClient;
var port = 8889;

exports.getKey = function (identifier, callback) {
    client.connect('mongodb://localhost:' + port, function (err, db) {
        if (err) callback(false, null);
    });
};
