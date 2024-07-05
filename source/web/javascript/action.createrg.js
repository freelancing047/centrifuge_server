function CreateRG(){
}

CreateRG.prototype.doTask =function() {
	this.addVisualization();
}

CreateRG.prototype.saveRelationShipGraph = function(vizUuid, requestJson){
   requestJson.uuid = vizUuid;
   var rgJson = new RelationGraphJson(window.dataview.myData);
   requestJson = rgJson.getLinkAndNodeDefJson(requestJson);
   var hex = utils.rgb2int($('.node-diagram').css('backgroundColor'));
   var props = requestJson.settings.properties;
   $.each(props, function(){
       if (this.class == "csi.server.common.model.Property") {
           this.value = hex + "";
           return;
       }
   });
   requestJson.clientProperties["vizBox.loadOnStartup"] = $('#alertModal').find('#loadOnStartUp').is(":checked");
   if($('#rgName')[0].value!=""){
       requestJson['name'] = $('#rgName')[0].value;
   }
   requestJson.clientProperties['render.threshold'] = $('#rndrThreshold')[0].value;
   requestJson.optionSetName = $('#theme').val();
   this.saveSettings(vizUuid, requestJson, true);
}
CreateRG.prototype.saveSettings = function(vizUuid, vizDef, bLoad){
    var id = window.dataview.myData.resultData.uuid;
    var viz = utils.getVisualisation(vizUuid);
    var ws = utils.getWorksheet4Visualization(vizUuid);
    if(viz && ws) {
    	var wsIndex = utils.getWorksheetIndex(ws.worksheet.uuid);
    	utils.showLoadingIndicator(wsIndex, viz.visualization.position);
    }
    var action = this;
    var doSuccess = function() {
    	action.refreshVisualisation(id, vizUuid, bLoad);
	};
    console.log('save-position' + vizDef.position);
	csi.viz.saveSettings(id, true, vizDef, {
		onsuccess : doSuccess
	});
   
}

CreateRG.prototype.refreshVisualisation = function(dvuuid, vizuuid, bLoad){
	$('.alertModal').modal('hide');
	$('#treeFinish-rg').removeAttr("disabled");
    var doSuccess = function() {
		var checked = $('#alertModal').find('#dontLoadAftrSave').is(":checked");
		if (checked){
			$('#'+vizuuid).parent().find(".progress_loader").hide();
			return;
		}
		var viz = utils.getVisualisation(vizuuid);
		var ws = utils.getWorksheet4Visualization(vizuuid);
		var relImage = {};
		relImage.name = viz.visualization.name;
		relImage.position = (viz.visualization.position == undefined ? 0 : viz.visualization.position);
		relImage.imageSrc = '';
		relImage.viz = viz.visualization.uuid;
		relImage.vizIndex = viz.index;
		relImage.ws = ws.worksheet;
		relImage.wsIndex = ws.index;
		relImage.nodes = utils.getNodesList(viz.visualization.nodeDefs);
		relImage.links = utils.getLinksList(viz.visualization.linkDefs);
		relImage.nodeDefsMap = utils.getNodeDefsMap(viz.visualization.nodeDefs);
		relImage.linkDefsMap = utils.getLinkDefsMap(viz.visualization.linkDefs);
		utils.drawLayout(relImage, false, bLoad);
	};
	if (bLoad) {
		csi.dataview.getDataView(window.dataview.myData.resultData.uuid, {
			onsuccess : function(data){
				window.dataview.myData.resultData=data.resultData;
				doSuccess();
			}
		});
	} else {
		doSuccess();
	}
}
CreateRG.prototype.addVisualization = function(){
	$('#treeFinish-rg').attr("disabled", "disabled");
    var dv = window.dataview;
    var id =  window.dataview.myData.resultData.uuid;
    var worksheetUuid = $('#new-visualization-dialogue').data('worksheetUuid');
    //clearing the worksheetid
    $('#new-visualization-dialogue').removeData('worksheetUuid');
    var requestJson = $('#treeFinish-rg').data('vizDef');
    if (requestJson.isNew != undefined ) {
    	delete requestJson.isNew;
    }
    var action = this;
    var requestJsonClone = JSON.parse(JSON.stringify(requestJson));
    requestJsonClone.bundleDefs = null;

    requestJsonClone.optionSetName = null;
    requestJsonClone.linkDefs = [];
    requestJsonClone.nodeDefs = [];
    requestJsonClone.settings.properties=[];
    requestJsonClone.playerSettings.speed = 0;
    console.log('add-wsuuid' + worksheetUuid);
    console.log('add-position' + requestJsonClone.position);
    csi.viz.addVisualization(window.dataview.myData.resultData.uuid, worksheetUuid, requestJsonClone, {
		onsuccess : function(data){
            action.saveRelationShipGraph(requestJson.uuid, requestJson);
		}
	});
}
