/**
 * A collection of dataviews.
 */

define(['backbone', 'app/models/dataview'], function(Backbone, Dataview) {
    return Backbone.Collection.extend({
	model: Dataview,

	url: "/Centrifuge/actions/dataview/listRecentlyOpenedDataViews?type=json",

	parse: function(response) {
	    // results are nested in the resultData key, so pass that
	    // on to be parsed as model objects
	    return response.resultData;
	}
    });
});
