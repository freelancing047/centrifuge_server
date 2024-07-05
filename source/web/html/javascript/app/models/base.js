/**
 * Common base model for all Centrifuge model objects
 */

define(['backbone'], function(Backbone) {
    return Backbone.Model.extend({
	// Centrifuge model objects use a "uuid" attribute as their
	// identifier, so map that to the Backbone id in order to get
	// proper syncing
	idAttribute: "uuid"
    });
});
