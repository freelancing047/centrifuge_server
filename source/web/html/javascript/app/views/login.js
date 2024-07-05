define(["backbone", "jquery", "api/csi", "mustache"], function(Backbone, $, csi, Mustache) {
    return Backbone.View.extend({
	events: {
	    "submit": "doLogin"
	},

	initialize: function(options) {
	},

	doLogin: function(e) {
	    // do not submit the form
	    e.preventDefault();
	    console.log("logging in ...");
	    var username = this.$el.find("input.username").val();
	    var password = this.$el.find("input.password").val();
	    var $el = this.$el;
	    var self = this;
	    csi.login(username, password, {
		onsuccess: function(data, status, xhr) {
		    console.log("login success!");
		    self.trigger("user:authenticated", status);
		},
		onerror: function(xhr, status, error) {
		    console.log("login error: " + error);
		    $el.find(".error-message").text("Incorrect username or password.");
		}
	    });
	},

	render: function() {
	    this.$el.show();
	}
    });
});
