/*
 * a MongoDB module for managing keys and meta-data
 */
var port = 8889;
var client = require('mongodb').MongoClient;

_getDocument = function (db, collectionName, property, callback) {
    db.collection(collectionName).find(property).toArray(function (err, items) {
        callback(err, items);
    });
};

getDocument = function (collectionName, property, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err, null);
        else {
            _getDocument(db, collectionName, property, function (err, items) {
                db.close();
                if (err) callback(err, null);
                else {
                    if (items.length != 1) callback(new Error(), null);
                    else callback(null, items[0]);
                }
            });
        }
    });
};

_upsertMeta = function (db, meta, callback) {
    db.collection('meta').updateOne(
        {"UID":meta.uid},
        meta,
        {upsert:true, w:1},
        function (err, result) {
            callback(err);
        }
    );
};

upsertMeta = function (meta, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            _upsertMeta(db, meta, function (err) {
                db.close();
                callback(err);
            });
        }
    });
};

_deleteDocument = function (db, collectionName, property, callback) {
    db.collection(collectionName).deleteMany(property, {w:1}, function (err, result) {
        callback(err);
    });
};

deleteDocument = function (collectionName, property, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            _deleteDocument(db, collectionName, property, function (err) {
                db.close();
                callback(err);
            });
        }
    });
};

_insertACE = function (db, uid, newAcl, callback) {
    db.collecttion('meta').updateOne(
        {"UID":uid},
        {$set: {"acl":newAcl}},
        {w:1},
        function (err, result) {
            callback(err);
        }
    )
};

insertACE = function (uid, newAcl, callback) {
    client.connect('mongodb://localhost:' + port + '/s2dr', function (err, db) {
        if (err) callback(err);
        else {
            db.collection('meta').updateOne(
                {"UID":uid},
                {$set: {"acl":newAcl}},
                {w:1},
                function (err) {
                    db.close();
                    callback(err);
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
