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

upsertMeta = function (object, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            db.collection('meta').updateOne(
                {UID:object.uid},
                object,
                {upsert:true, w:1},
                function (err, result) {
                    if (err) callback(err);
                    else callback(null);
                }
            );
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

insertACE = function (uid, newAcl, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            db.collection('meta').updateOne(
                {UID:uid},
                {$set: {acl:newAcl}},
                {w:1},
                function (err, result) {
                    if (err) callback(err);
                    else callback(null);
                }
            );
        }
    });
};

exports.getChannel = function (identifier, callback) {
    getDocument('channels', {clientID: identifier}, callback);
};

exports.getMeta = function (uid, callback) {
    getDocument('meta', {UID: uid}, callback);
};

exports.upsertMeta = function (meta, callback) {
    upsertMeta(meta, callback);
};

exports.deleteMeta = function (uid, callback) {
    deleteDocument('meta', {UID: uid}, callback);
};

exports.deleteChannel = function (identifier, callback) {
    deleteDocument('channels', {clientID: identifier}, callback);
};

exports.delegate = function (message, acl, ace, callback) {
    var now = new Date();
    var _ace = {
        name: message.client,
        timestamp: now,
        lifetime: ace.lifetime <= 0 ? message.time : Math.min(message.time, (now - ace.timestamp) / 1000),
        signature: '',
        permission: message.permission,
        propagation: message.flag != 0
    };
    acl.push(_ace);
    insertACE(message.uid, acl, callback);
};
