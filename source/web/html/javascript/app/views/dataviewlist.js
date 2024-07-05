/**
 * Our Dataview/worksheet list view
 */

define(["backbone", "jquery", "mustache"],
function(Backbone, $, Mustache) {
    return Backbone.View.extend({
	tagName: "ul",
	className: "dataviews",
	template: $("#dataview-list-template").text(),

	events: {
	    "click .dataview-name": "openDataview",
	    "click .worksheet-name": "openWorksheet"
	},

	initialize: function() {
	    this.render();
	    this.collection.on("reset change", this.render, this);
	},

	render: function() {
	    var viewData = { dataviews: this.collection.toJSON() };
	    this.$el.html(Mustache.render(this.template, viewData));
	    return this;
	},

	openDataview: function(e) {
	    console.log(e);
	    var id = $(e.target).parent().attr("data-id");
	    console.log("selected id = " + id);
	    var dataview = this.collection.get(id);
	    this.selectedDataview = dataview;
	    this.trigger("dataview:selected", dataview);
	    if (!dataview.get("meta")) {
		// "open" the dataview to fetch metadata (columns, worksheets, etc.)
		dataview.open();
	    }
	},

	openWorksheet: function(e) {
	    var worksheetId = $(e.target).parent().attr("data-id");
	    var dataviewId = $(e.target).parent().parent().parent().attr("data-id");
	    var dataview = this.collection.get(dataviewId);
	    this.trigger("worksheet:selected", dataview, worksheetId);
	}
    });
});
