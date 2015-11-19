/*
 * check if an ACL permits a specific operation from a client
 */
var fs = require('fs');
var db = require('../lib/dbLib');

exports.opEnum = {
    checkIn: 0,
    checkOut: 1,
    both: 2,
    owner: 3
};

exports.secFlag = {
    none: 0,
    confidentiality: 1,
    integrity: 2,
    both: 3
};

exports.checkPermit = function (acl, client, operation) {
    // TODO: implement acl algorithm
};

exports.checkDelegate = function (message, meta, callback) {

};

exports.getDoc = function (meta, callback) {
    // TODO: check flag
    fs.readFile('../docs/' + meta.UID, function (err, data) {
        if (err) callback(err, null);
        else callback(null, data);
    });
};

exports.addDoc = function (channel, message, callback) {
    // TODO: maintain metadata
    fs.writeFile('../docs/' + message.uid, message.document, function (err) {
        if (err) callback(err);
        else callback(null);
    });
};

exports.updateDoc = function (channel, message, callback) {

};

exports.deleteDoc = function (uid, callback) {

};
