/*
 * check if an ACL permits a specific operation from a client
 */
var fs = require('fs');

exports.opEnum = {
    checkIn: 0,
    checkOut: 1,
    both: 2,
    owner: 3
};

exports.checkPermit = function (acl, client, operation) {
    // TODO: implement acl algorithm
};

exports.getDoc = function (meta, callback) {
    // TODO: file operation
};

exports.addDoc = function (channel, message, callback) {

};

exports.updateDoc = function (channel, message, callback) {

};

exports.deleteDoc = function (uid, callback) {

};