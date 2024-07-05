/**
 * I represent the top-level view that users interact with. I contain
 * all of the views that the users sees and uses for a single
 * Centrifuge session.
 */

define(["jquery",
	"backbone",
	"underscore",
	"app/views/login",
	"app/views/dataviewlist",
	"app/views/vizpanel",
	"app/collections/dataviews"],
function($, Backbone, _, LoginView, DataviewListView, VizPanel, DataviewCollection) {
    return Backbone.View.extend({
	events: {
	},

	initialize: function(options) {
	    this.loginView = new LoginView({el: $("#login"), app: this});
	    this.loginView.render();
	    this.loginView.on("user:authenticated", this.authenticated, this);
	},

	authenticated: function() {
	    // it's go time!
	    this.loginView.remove();
	    // begin loading the actual important stuff
	    var dataviews = new DataviewCollection();
	    dataviews.fetch();
	    this.chooser = new DataviewListView({collection: dataviews});
	    // display the dataview list
	    this.$(".dataview-list-container").append(this.chooser.el);

	    // listen for selected data view changes
	    this.chooser.on("worksheet:selected", this.openWorksheet, this);
	},

	openWorksheet: function(dataview, worksheetId) {
	    // maybe this should use the global channel, or a maybe a
	    // router: /dataviews/:dataviewId/worksheets/:worksheetId

	    // for now just get it the "dumb" way, no nested
	    // collections or models
	    var worksheets = dataview.get("meta").modelDef.worksheets;
	    var worksheet = _.find(worksheets, function(x) {
		return x.uuid == worksheetId;
	    });

	    console.log(worksheet);
	    var workspace = this.$("> .workspace");
	    workspace.empty();
	    _.each(worksheet.visualizations, function(vizDef) {
		console.log("visualization config:", vizDef);
		var vizView = new VizPanel({viz: vizDef, dataview: dataview});
		workspace.append(vizView.render().el);
	    });
	}
    });
});
