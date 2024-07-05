requirejs.config({
    baseUrl: "javascript/lib",
    paths: {
	"app": "../app",
	"api": "../api"
    },
    shim: {
	backbone: {
	    deps: ["underscore", "jquery"],
	    exports: "Backbone"
	},
	underscore: {
	    exports: "_"
	}
    }
});

require(["jquery",
	 "underscore",
	 "backbone",
	 "app/views/app",
	 "app/collections/dataviews"],
function($, _, Backbone, AppView) {
    console.log("All ready to go now");
    // set up the global event channel
    window.channel = _.extend(Backbone.Events);
    window.app = new AppView({el: $("#app")});
    window.$ = $;
});
