/*
 * a MongoDB module for managing keys and meta-data
 */
var port = 8889;
var client = require('mongodb').MongoClient;

exports.getKey = function (identifier, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err, null);
        db.collection('keys').find({clientID: identifier}).toArray(function (err, items) {
            if (err) callback(err, null);
            if (items.length != 1) callback(err, null);
            callback(null, items[0]);
        });
    });
};

exports.getMeta = function (uid, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err, null);
        db.collection('meta').find({UID: uid}).toArray(function (err, items) {
            if (err) callback(err, null);
            if (items.length != 1) callback(err, null);
            callback(null, items[0]);
        });
    });
};