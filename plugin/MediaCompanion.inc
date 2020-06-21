app.CreateMediaCompanion = function() {
	"use strict";
	//create private vars.
	var res = {}, //the object
	plugin = _CreatePlugin( "com.laurepro.plugins.user.MediaCompanion"), //the interface with java code.
	version = parseFloat(plugin.Send("GetVersion")); //get the version once.

	//public functions.

	//get plugin version.
	res.GetVersion = function() {
		return version;
	}; //GetVersion

	//state: true = connected.
	res.SendAvrcpMeta = function() {
		return plugin.Send("SendAvrcpMeta") === "Connected";
	}; //SendAvrcpMeta

	//set callback for onSignalStatusChange event.
	res.SetOnSignalStatusChange = function(cb) {
		plugin.Send("SetOnSignalStatusChange", cb.name);
	}; //SetOnSignalStatusChange

	//ok, done adding funcs. Return the new object.
	return res;
}; //MediaCompanion
