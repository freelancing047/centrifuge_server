function ContextMenuLinkFunction(vizuuid) {
	this.vizuuid = vizuuid;
	this.linkId = 0;
	
	//unbind all clicks because each option are on different links
	$('#contextMenuTemplateLink' + this.vizuuid + " .linkSelect").unbind('click');
	$('#contextMenuTemplateLink' + this.vizuuid + " .linkDeselect").unbind('click');
	$('#contextMenuTemplateLink' + this.vizuuid + " .linkHideSelect").unbind('click');
}


ContextMenuLinkFunction.prototype.doTask = function() {
	var contextMenu = this;
	
	$('#contextMenuTemplateLink' + contextMenu.vizuuid + " .linkSelect").click(function() {
		new SelectLinkById(contextMenu.vizuuid).doTask(contextMenu.linkId, false, 0);
	});
	
	$('#contextMenuTemplateLink' + contextMenu.vizuuid + " .linkDeselect").click(function() {
		new SelectLinkById(contextMenu.vizuuid).doTask(contextMenu.linkId, false, 0);
	});
	
	$('#contextMenuTemplateLink' + contextMenu.vizuuid + " .linkHideSelect").click(function() {
		contextMenu.linkHide(contextMenu.linkId);
	});
}

//function to hide the current link only
ContextMenuLinkFunction.prototype.linkHide = function(elementId) {
	var contextMenu = this;
	var type = "link";
	var flag = true;
	var dvUuid = window.dataview.myData.resultData.uuid;
	console.log("dvuuid:"+dvUuid);
	var vizUuid = contextMenu.vizuuid;
	
	var url = "/Centrifuge/services/graphs2/actions/dohideUnhideTask?_f=json&vduuid="+vizUuid+"&dvuuid="+dvUuid+"&id="+elementId+"&type="+type+"&flag="+flag;
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			new RefreshImage(vizUuid).doTask(); 
			var listLinks = new ListLinks(vizUuid);
			listLinks.doTask();
			var listNodes = $.data($('#nodes-table' + vizUuid)[0], "controller");
			listNodes.doFetch();
		},
		error: function(data) {
		// alert ("Error");
		}
	});
	
	new RefreshImage(contextMenu.vizuuid).doTask(); 
}

//get the link on which the right click is pressed.
ContextMenuLinkFunction.prototype.getLink = function(x,y) {
	var contextMenu = this;	
	var findItemSuccess = function(){
		return function (data) {
			if (data.resultData) {	
				//save itemId		
				contextMenu.linkId = data.resultData.itemId;
				//check if the link is selected with its itemId
				contextMenu.checkSelected(contextMenu.linkId);								
			}
		}
	};
	csi.relgraph.findItemAt(contextMenu.vizuuid, x, y, {
		onsuccess: findItemSuccess()			
	});	
}

//check if the link right clicked is selected or not.
ContextMenuLinkFunction.prototype.checkSelected = function(linkId) {
	var contextMenu = this;
	var selectedflag = 0;
	
	//function that has 'data' about the selected items in a graph
	var doSuccess = function() {
		return function(data) {
			var linksLength = data.resultData.links==undefined ? 0 : data.resultData.links.length;
			var i;
			//console.log("nodeLength:"+nodeLength);
			if(linksLength > 0){
				LinksJson = data.resultData.links;
				for (i in LinksJson)	{
					//Check if (right-clicked) link is present in the 'selected links' list.
					if (LinksJson[i].itemId == linkId){
						selectedflag = 1;
						$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkSelect').attr('disabled','disabled');
						$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkDeselect').removeAttr('disabled');
						$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkHideSelect').removeAttr('disabled');												
					}
				}
				//If the (right-clicked) link is NOT present in the 'selected links' list.
				if (selectedflag == 0){
					$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkSelect').removeAttr('disabled');
					$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkDeselect').attr('disabled','disabled');
					$('#contextMenuTemplateLink' + contextMenu.vizuuid + ' .linkHideSelect').attr('disabled','disabled');
				
				}
			}
		}
	}
	csi.relgraph.selectionInfo(contextMenu.vizuuid, {
		onsuccess: 	doSuccess()
	});	
}