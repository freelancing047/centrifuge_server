function CreateLinkEditDialog(source,target,linkName){
	this.source = source.attr('title');
	this.target = target.attr('title');
	this.linkName = linkName;
	this.sourceNodeDetail = source;
	this.targetNodeDetail = target;
	this.vizdef = null;
	this.toolTipTab = null;
	this.computedFieldsTab = null;
	this.directionTab = null;
	this.basicstab = null;	
	this.linksupTab = null;
}

CreateLinkEditDialog.prototype.doTask = function(){
	this.fetchVisualisation();
	var idx = this.getlinkDefIdx(this.vizdef.linkDefs, this.source, this.target);
	var linkId = this.vizdef.linkDefs[idx].uuid;
	var linkEditView = {
		source: this.source,
		target: this.target,
		linkName: utils.getAlphaNumericString(this.linkName),
		linkid: linkId 
	}
	var linkModalHtml = Mustache.render($('#link-edit-popup-template').html(), linkEditView);	
	var linkModalDiv = $(linkModalHtml).filter('.link-edit-modal');
	var existingLinkModelDiv = $('#link-edit-modal' + utils.getAlphaNumericString(this.linkName));
	existingLinkModelDiv.remove();
	this.populateGeneralInfo(linkModalDiv);
	this.initailizeLinkEditContainer(linkModalDiv);
	this.createEditLinkTabs();
	this.registerUpdateBttnAction(linkModalDiv);
	linkModalDiv.modal();
}

CreateLinkEditDialog.prototype.initailizeLinkEditContainer = function(linkModalDiv){	
	this.toolTipTab =  new LinkEditorToolTipTab(linkModalDiv, this.vizdef);
	this.computedFieldsTab =  new LinkEditorComputedFieldsTab(linkModalDiv, this.vizdef);
	this.directionTab = new LinkEditorDirection(linkModalDiv, this.linkName,this.vizdef);
	this.basicstab =  new LinkEditorBasicsTab(linkModalDiv, this.vizdef);
	this.linksupTab = new LinkEditorLinksTab(linkModalDiv, this.linkName, this.vizdef);
	this.isValidate(linkModalDiv);
}

CreateLinkEditDialog.prototype.createEditLinkTabs = function(){
	this.computedFieldsTab.populateTabInfo(this.source,this.target);
	this.toolTipTab.populateTabInfo(this.source,this.target);
	this.directionTab.populateTabInfo(this.sourceNodeDetail,this.targetNodeDetail);
	this.basicstab.populateTabInfo(this.source,this.target);
	this.linksupTab.populateTabInfo(this.source,this.target);
	
}

CreateLinkEditDialog.prototype.fetchVisualisation = function(){
	this.vizdef = $('#treeFinish-rg').data("vizDef");	
}

CreateLinkEditDialog.prototype.registerUpdateBttnAction = function(linkModalDiv){
	var editlink = this;
	var vizDef = this.vizdef;
	var computedFieldsTab = this.computedFieldsTab;
	var toolTipTab = this.toolTipTab;
	var directionTab = this.directionTab;
	var basicsTab =  this.basicstab;
	var linksupTab = this.linksupTab;
	linkModalDiv.find('#treeFinish').click(function(event){	
//		To display link name on link
		if($(this).attr('disabled') == 'disabled'){
			return;
		}
		var connections = jsPlumb.getConnections();
		var link;
		$.each(connections, function() {
			var startPoint = $('#'+ this.endpoints[0].elementId);
			var endPoint = $('#'+ this.endpoints[1].elementId);
			var startPointColumnName = startPoint.find('.node-label').text();
			var endPointColumnName = endPoint.find('.node-label').text();
			if(startPointColumnName == editlink.source && endPointColumnName == editlink.target){
				link = this;
			}
		});
		if(link){
			var labelOverlay = link.getOverlay("label");
			if(labelOverlay) {
				labelOverlay.setLabel(linkModalDiv.find("#link-name").val());
			} else {
				link.addOverlay(["Label", {
					label:linkModalDiv.find("#link-name").val(),
					location:0.5,
					id:"label",
					cssClass:"link-label"
				}]);
			}
			
		}
		var newVizDef = toolTipTab.doSave(vizDef);
		newVizDef = computedFieldsTab.doSave(newVizDef);	
		newVizDef = directionTab.doSave(newVizDef);
		newVizDef = editlink.doSave(newVizDef, linkModalDiv);
		newVizDef = basicsTab.doSave(newVizDef);
		newVizDef = linksupTab.doSave(newVizDef);
		$.data($('#treeFinish-rg')[0], 'vizDef', newVizDef);
		linkModalDiv.modal('hide');
		linkModalDiv.remove();
	});	
	linkModalDiv.find('#cancel-ne').click(function(event){		
		linkModalDiv.modal('hide');
		linkModalDiv.remove();
	});	

}
CreateLinkEditDialog.prototype.doSave = function(newVizDef, linkModalDiv){
	var linkModalDiv = linkModalDiv;
	var editLink = this;
	var linkDefIdx = this.getlinkDefIdx(this.vizdef.linkDefs, this.source, this.target);
	var hideEmptyValue = linkModalDiv.find('#link-hideEmptyValue').is(":checked"); 
	var linkName;
	if(!_.isEmpty(linkModalDiv.find("#link-name").val())){
		linkName = linkModalDiv.find("#link-name").val();
		newVizDef.linkDefs[linkDefIdx].name = linkName;
	}
	else{
		linkName = newVizDef.linkDefs[linkDefIdx].uuid;
	}
	var staticStatus = linkModalDiv.find('#link-static').is(':checked');	
	var scaleValue;
	if(staticStatus){
		scaleValue =  linkModalDiv.find('#link-static-scale').val();
		if (scaleValue == "" || parseFloat(scaleValue) <= 0) {
			scaleValue = 1;
		}
	}else{
		scaleValue =  linkModalDiv.find('#link-non-static-scale').val();
	}	
	var isSizeAttrExists = false;
	$.each(newVizDef.linkDefs[linkDefIdx].attributeDefs, function(index, attributeDef){
		if (attributeDef.kind == "COMPUTED") {
			linkModalDiv.find('#link-non-static-scale').append( new Option(attributeDef.name,attributeDef.name) );
        }
		if(attributeDef.name == "csi.internal.Size"){
            newVizDef.linkDefs[linkDefIdx].attributeDefs[index] = utils.getAttributeDef(staticStatus, scaleValue + "", attributeDef.uuid, "csi.internal.Size");
            isSizeAttrExists = true;
	    }
		newVizDef.linkDefs[linkDefIdx].attributeDefs[index].hideEmptyInTooltip = hideEmptyValue;
	});
	if(!isSizeAttrExists){
		var sizeAttributeDef = utils.getAttributeDef(staticStatus, scaleValue + "", utils.guidGenerator(), "csi.internal.Size");
		sizeAttributeDef.hideEmptyInTooltip = hideEmptyValue;
		newVizDef.linkDefs[linkDefIdx].attributeDefs.push(sizeAttributeDef);
	}
	this.updateLink(newVizDef.linkDefs[linkDefIdx].uuid, linkName);
	return newVizDef;
}

CreateLinkEditDialog.prototype.updateLink =  function(uuid, linkName){
	//to do
}

CreateLinkEditDialog.prototype.populateGeneralInfo = function(linkModalDiv){
	var editLink = this;
	var linkDefIdx = this.getlinkDefIdx(this.vizdef.linkDefs, this.source, this.target);
	var linkDef = this.vizdef.linkDefs[linkDefIdx];	
	var hideEmptyValue = linkDef.clientProperties.hideAllEmptyLabels;
	linkModalDiv.find('#link-hideEmptyValue').attr('checked', linkDef.attributeDefs[0].hideEmptyInTooltip);
	if(linkDef.name != linkDef.uuid){
		linkModalDiv.find("#link-name").val(linkDef.name);
	}
	var isStatic = false;
	var isSize = false;
	var isTransparency = false;
	var staticText = null;
	var referenceName = null;
	$.each(linkDef.attributeDefs, function(){
		if (this.kind == "COMPUTED") {
			linkModalDiv.find('#link-non-static-scale').append( new Option(this.name,this.name) );
        }
		if(this.name == "csi.internal.Size"){
			isStatic = this.byStatic;
			isSize = this.bySize;
			isTransparency = this.byTransparency;
			referenceName = this.referenceName;
			staticText = (this.fieldDef) ? this.fieldDef.staticText : "";
		}
	});
	linkModalDiv.find('#link-static').attr('checked',isStatic);
	if(isStatic){
		linkModalDiv.find('#link-non-static-scale').attr('style','display:none;');
		linkModalDiv.find('#link-static-scale').val(staticText).attr('class',undefined);
		linkModalDiv.find('#link-static-scale').removeAttr('style');
	}
	else if(isSize || isTransparency){
		linkModalDiv.find('#link-non-static-scale').removeAttr('style');
		linkModalDiv.find('#link-non-static-scale').find('option[value='+referenceName+']').attr('selected', 'selected');
		linkModalDiv.find('#link-static-scale').attr('style','display:none;');
	}
	linkModalDiv.find('#link-static').change(function(){
		if($(this).is(':checked')){
			linkModalDiv.find('#link-non-static-scale').attr('style','display:none;');
			linkModalDiv.find('#link-static-scale').removeAttr('style');
			linkModalDiv.find('#link-static-scale').val(1+"").attr('class',undefined);
			
		}else{
			linkModalDiv.find('#link-static-scale').attr('style','display:none;');
			linkModalDiv.find('#link-non-static-scale').removeAttr('style');
		}
	});
}
CreateLinkEditDialog.prototype.getlinkDefIdx = function(linkDefs, source, target) {
	var linkIdx = null;
	var source = source;
	var target = target;
	$.each(linkDefs, function(index, value) {
		if (value.nodeDef1.name == source && value.nodeDef2.name == target) {
			linkIdx = index;
			return;
		}
	});
	return linkIdx;
}

CreateLinkEditDialog.prototype.isValidate =  function(linkModalDiv){
	var linkEditDialog = this;
	linkModalDiv.find('#treeFinish').bind("change", function () {
		if(linkEditDialog.toolTipTab.findIsValid() && linkEditDialog.computedFieldsTab.findIsValid() && linkEditDialog.directionTab.findIsValid()){
			$(this).removeAttr('disabled');
		}
		else{
			$(this).attr('disabled','disabled');
		}
	});
}
