function CollapseExpandLegend(vizuuid) {
	this.vizuuid=vizuuid;
}
CollapseExpandLegend.prototype.doTask = function(element) {
	var legend = $(element).parents('.graphLegend');
	var legendBody = legend.find('.legendBody');
	var vizuuid = this.vizuuid;
	legendBody.hide();
	if($(element).text() == '-') {
		legendBody.hide();
		$(element).text('+');
		legend.height("35px");
		legend.resizable( "disable" );
	} else {
		legend.resizable( "enable" );
		legendBody.show();
		$(element).text('-');
		new_legend_height = legendBody.height() + 50;
		legend.height(new_legend_height);
	}
}