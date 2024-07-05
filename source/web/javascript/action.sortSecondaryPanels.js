function SortSecondaryPanels(wkshtIndex, wkshtUuid) {
	this.wkshtIndex = wkshtIndex;
	this.wkshtUuid = wkshtUuid;

}
SortSecondaryPanels.prototype.doTask = function() {
	var sSort = this;
	var originalPositions = [];
	$('#layout' + sSort.wkshtIndex + ' #left_content > .span12 .column').each(function(index, col) {
		originalPositions.push($(col).find('.vizPanels').attr('data-position'));
	});
	$('#layout' + sSort.wkshtIndex + ' #left_content > .span12').sortable({
		connectWith: '.column',
		handle: 'h2',
		cursor: 'move',
		placeholder: 'placeholder',
		forcePlaceholderSize: true,
		opacity: 0.4,
		containment: "#layout" + sSort.wkshtIndex + " #left_content",
		stop: function() {
			var changedPositions = [];
			$('#layout' + sSort.wkshtIndex + ' #left_content > .span12 .column').each(function(index, col) {
				changedPositions.push($(col).find('.vizPanels').attr('data-position'));
			});
			
			var changedItems = utils.getChangedItemsWithPosition(originalPositions, changedPositions);
			
			// If the positions are changed only, then only perform the server calls
			// sometimes the stop is triggered even when the
			// positions have not changed
			if(changedItems.length > 0) {
				var rightSidePanels = $('#layout' + sSort.wkshtIndex + ' #left_content > .span12 .column');
				var panelsWithVisualization = []; 
				rightSidePanels.each(function(index, col) {
					var newPosition = index + 1;
					
					// update the ids of the panel outer divs 
					// with the new position, so that after the server
					// call it updates the correct panel
					$(col).attr('id', 'col' + newPosition);
					$(col).find('.vizPanels').attr('id', 'layout' + sSort.wkshtIndex + '_panel' + newPosition);
					$(col).find('.vizPanels').attr('data-position', newPosition);
					
					var relGraphImg = $(col).find('.vizPanels .toggle_container > img.relGraphImage');
					// If panel contains relgraph image (visualization),
					// save the visualization and refresh the visualization
					// else update the panel with empty panel layout with new position
					if(relGraphImg.length > 0) {
						panelsWithVisualization.push({
							vizUuid: relGraphImg.attr('id'),
							position: newPosition
						});
						
						// code for saving the visualization
						// with the new position comes here.
						var vizUuid = relGraphImg.attr('id');
						var viz = utils.getVisualisation(vizUuid);
						
						viz.visualization.position = "" + newPosition;
						var createrg = new CreateRG();
						createrg.saveSettings(vizUuid, viz.visualization, false);
					}
					else {
						var emptyPanelView = {
							index: sSort.wkshtIndex,
							worksheetUuid: sSort.wkshtUuid
						}
						var emptyPanelHtml = Mustache.render($('#small-panel-empty-layout').html(), emptyPanelView);
						$(col).find('.vizPanels').html(emptyPanelHtml);
					}
					
				});
			}
		}
	});
}