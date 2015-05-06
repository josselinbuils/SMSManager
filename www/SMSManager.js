var exec = require('cordova/exec');

exports.getConversations = function(options, success, error) {
	exec(success, error, "SMSManager", "getConversations", [ options ]);
};

exports.receiveMessages = function(success, error) {
	exec(success, error, "SMSManager", "receiveMessages", []);
};

exports.sendSMS = function(infos, success, error) {
	exec(success, error, "SMSManager", "sendSMS", [ infos ]);
};