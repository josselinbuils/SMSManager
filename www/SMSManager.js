var exec = require('cordova/exec');

exports.getConversations = function(options, success, error) {
	exec(success, error, "SMSManager", "getConversations", [options]);
};