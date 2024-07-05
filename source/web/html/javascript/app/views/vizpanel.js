
// right now this view expects the visualization def as a plain JSON object

// it expects it in the "viz" key in the options to the constructor

// a generic visualization panel/container

define(["backbone", "mustache", "api/csi.relgraph"],
function(Backbone, Mu, RelGraphAPI) {
    return Backbone.View.extend({
	className: "visualization",

	template: Mu.compile($("#viz-panel-template").text()),

	initialize: function(options) {
	    this.dataview = options.dataview;
	    this.viz = options.viz;
	},

	render: function() {
	    this.$el.empty();
	    this.$el.html(this.template(this.viz));

	    var content = this.$("> .content");

	    if (this.viz.type === "RELGRAPH_V2") {
		var img = $("<img>");
		content.append(img);
		var imgUrl = RelGraphAPI.graphImageUrl(this.dataview.id, this.viz.uuid, 640, 480);
		console.log(imgUrl);
		img.attr("src", imgUrl);

		this.img = img;
	    } else {
		content.append("Unknown Visualization type...");
	    }

	    return this;
	}
    });
});
