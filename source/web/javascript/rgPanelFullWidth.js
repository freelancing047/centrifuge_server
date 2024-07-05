RGPanelFullWidth.prototype = new RGPanel();
RGPanelFullWidth.prototype.constructor = RGPanelFullWidth;

function RGPanelFullWidth(rgView, index, rgPanelObj) {
	RGPanel.call(this, rgView, index, rgPanelObj);
	this.panelOffset = 0;
	this.leftStartOffset = 0;
	this.topStartOffset = 0;
}
RGPanelFullWidth.prototype.generateRelPanel = function(relImage, index) {
	var margin = index * this.panelOffset;
	var relDiv = $('<div>')
			.addClass('box grad_colour_dark_blue relation-panel full-width');
	relDiv.css('left', margin + this.leftStartOffset).css('top',
			margin + this.topStartOffset).css('margin-bottom', '0px');
	var titleH2 = this.generateTitleDiv(relImage.name);
	var titleImagesSection = this.getTitlePanelImages(relImage.viz);
	var toggleContainer = this.generateToggleContainer(relImage, index);
	titleH2.append(titleImagesSection);
	relDiv.append(titleH2).append(toggleContainer);
	relDiv.append('<input type="hidden" class="vizIndex" value="' + index + '">');
	relDiv.append('<input type="hidden" class="vizUuid" value="' + relImage.viz + '">');
	return relDiv;
}