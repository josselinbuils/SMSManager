var exec = require('cordova/exec');

exports.deleteConversation = function(infos, success, error) {
	exec(success, error, "SMSManager", "deleteConversation", [ infos ]);
};

exports.getConversations = function(success, error) {
	exec(success, error, "SMSManager", "getConversations", []);
};

exports.getConvMessages = function(infos, success, error) {
	exec(success, error, "SMSManager", "getConvMessages", [ infos ]);
};

exports.getContactPhoto = function(infos, success, error) {
	exec(success, error, "SMSManager", "getContactPhoto", [ infos ]);
};

exports.getContactThumbnail = function(infos, success, error) {
	exec(success, error, "SMSManager", "getContactThumbnail", [ infos ]);
};

exports.listenEvents = function(success, error) {
	exec(success, error, "SMSManager", "listenEvents", []);
};

exports.listenLogs = function(success, error) {
	exec(success, error, "SMSManager", "listenLogs", []);
};

exports.sendSMS = function(infos, success, error) {
	exec(success, error, "SMSManager", "sendSMS", [ infos ]);
};