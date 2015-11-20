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
                    if (items.length != 1) callback(new Error(), null);
                    else callback(null, items[0]);
                }
            });
        }
        db.close();
    });
};

upsertMeta = function (collectionName, object, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            db.collection(collectionName).updateOne(
                {UID:object.uid},
                object,
                {upsert:true, w:1},
                function (err, result) {
                    if (err) callback(err);
                    else callback(null);
            });
        }
        db.close();
    });
};

deleteDocument = function (collectionName, property, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            db.collection(collectionName).deleteMany(property, {w:1}, function (err, result) {
                if (err) callback(err);
                else callback(null);
            });
        }
        db.close();
    });
};

exports.getChannel = function (identifier, callback) {
    getDocument('channels', {clientID: identifier}, callback);
};

exports.getMeta = function (uid, callback) {
    getDocument('meta', {UID: uid}, callback);
};

exports.upsertMeta = function (meta, callback) {
    upsertMeta('meta', meta, callback);
};

exports.deleteMeta = function (uid, callback) {
    deleteDocument('meta', {UID: uid}, callback);
};

exports.deleteChannel = function (identifier, callback) {
    deleteDocument('channels', {clientID: identifier}, callback);
};

exports.delegate = function (message, entry, callback) {
    // TODO: updateOne
};