/*
 * a MongoDB module for managing keys and meta-data
 */
var port = 8889;
var client = require('mongodb').MongoClient;

getDocument = function (collectionName, property, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err, null);
        else {
            db.collection(collectionName).find(property).toArray(function (err, items) {
                if (err) callback(err, null);
                else {
                    if (items.length != 1) callback(new ErrorObject(), null);
                    else callback(null, items[0]);
                }
            });
        }
    });
};

exports.getChannel = function (identifier, callback) {
    getDocument('channels', {clientID: identifier}, callback);
};

exports.getMeta = function (uid, callback) {
    getDocument('meta', {UID: uid}, callback);
};

exports.deleteMeta = function (uid, callback) {

};