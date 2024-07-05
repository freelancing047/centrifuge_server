/**
 * I am a view representing a named configuration of panels.
 */

define(["jquery", "backbone", "mustache"], function($, Backbone, Mustache) {
    return Backbone.View.extend({
	tagName: "div",
	className: "workspace",
	template: Mustache.compile($("#workspace-template").text()),
	
	events: {
	    "click .close": "close"
	},
	
	render: function() {
	    this.$el.html(this.template({"title":"New Workspace"}));
	    return this;
	},
	
	close: function() {
	    console.log("close me");
	}
    });
});
