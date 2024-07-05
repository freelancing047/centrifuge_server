function CreateNodeTable(vizuuid) {
	this.vizuuid = vizuuid;
	this.currentDisplay = "-1";
	this.oTable = null;
	this.lastChecked = null;
	this.selectNodeById = new SelectNodeById(vizuuid);
	this.myData = null;
}

CreateNodeTable.prototype.doTask = function() {
	this.createTable();
}

CreateNodeTable.prototype.doFetch = function() {
	var cnTable = this;
	var doSuccess = function(cnTable) {
		return function(data) {
			if (cnTable.oTable){
				cnTable.myData = data.resultData;
				cnTable.oTable.fnClearTable();
				cnTable.oTable.fnAddData(cnTable.myData);
				$("#toolbar-node-" + cnTable.vizuuid).html('');
				cnTable.currentDisplay = "-1";
			}
		};
	};
	csi.relgraph.listNodes(cnTable.vizuuid, {
		onsuccess : doSuccess(cnTable)
	});

}

CreateNodeTable.prototype.createTable = function() {
	var cntable = this;
	
	this.oTable = $('#nodes-table' + this.vizuuid).dataTable(
					{
						"bAutoWidth": false,
						"aoColumns" : [
								{
									"sTitle" : "Label",
									"sWidth": "30%",
									"fnRender" : function(obj) {
										obj.aData.label = obj.aData.displayLabel;
										var sReturn = '';
										if (obj.aData.bundle) {
											sReturn = '<img class ="control center" src="../images/details_open.png"></img>&nbsp&nbsp&nbsp'
													+ obj.aData.displayLabel;
										} else {
											sReturn = obj.aData.displayLabel;
										}
										return sReturn;
									},
									"mData" : "displayLabel"
								},
								{
									"sWidth": "25%",
									"sTitle" : "Type",
									"mData" : "displayType"
								},
								{
									"sWidth": "25%",
									"sTitle" : "Visible Neighbors",
									"mData" : "visibleNeighbors"
								},
								{
									"sTitle" : "Hidden",
									"sWidth": "10%",
									"fnRender" : function(obj) {
										var sReturn = '';
										if (obj.aData.hidden) {
											sReturn = '<input name="" type="checkbox" value="" checked="checked"/>'
										} else {
											sReturn = '<input name="" type="checkbox" value=""/>'
										}
										return sReturn;
									},
									"mData" : "hidden"
								}, {
									"sTitle" : "Bundled",
									"sWidth": "10%",
									"fnRender" : function(obj) {
										var sReturn = '';
										if (obj.aData.bundledNode) {
											sReturn = '<input name="" type="checkbox" value="" checked="checked"/>'
										} else {
											sReturn = ''
										}
										return sReturn;
									},
									"mData" : "bundle"
								} ],
								"bPaginate": true,
								"bJqueryUI": true,
								"sPaginationType": "full_numbers",
								"sScrollY" : "150px",
								 "bDestroy" : true,
								"fnCreatedRow": function( nRow, aData, iDataIndex ) {
									
									$(nRow).find('img').click( function( e ){
										var data = cntable.oTable.fnGetData(this.parentNode.parentNode);
										if (!data){
											return;
										}//bundledNode flag is to set children node's check box 
										var oData = data.children;
										$.each(oData, function(){
											if(this.bundle == false){
											this.bundledNode = true;
											}
										});
										var label = data.label;
										if (oData){
											cntable.oTable.fnClearTable();
											cntable.oTable.fnAddData(oData);
											cntable.processBundleNavigation(oData, data);
										}
									});
									$(nRow).find('td:eq(3)').find('input:checkbox').click( function( e ){ 
									 	var nodeId = cntable.oTable.fnGetData(this.parentNode.parentNode).itemId;
									 	var nodeHideUnhideTask = new NodeHideUnhideTask(cntable.vizuuid, nodeId);
									 	nodeHideUnhideTask.doTask(event);
									});
									$(nRow).find('td:eq(4)').find('input:checkbox').click( function( e ){ 
									 	var nodeId = cntable.oTable.fnGetData(this.parentNode.parentNode).itemId;
									 	var unBundleSingleNode = new UnBundleSingleNode(cntable.vizuuid, nodeId);
									 	unBundleSingleNode.doTask();
								    });			
									$(nRow).click( function( e ) {
										var data = cntable.oTable.fnGetData(this);
										if (!data){
											return;
										}
										var nodeId = data.itemId;
										if (!cntable.lastChecked) {
											cntable.lastChecked = this;
										}
										
										//Action while pressing shift key
										if (e.shiftKey) {
												var start = $('#nodes-table'+ cntable.vizuuid +' tbody tr').index(this);
												var end = $('#nodes-table'+ cntable.vizuuid+' tbody tr').index(cntable.lastChecked);
												var idList = [];
												$('#nodes-table'+ cntable.vizuuid+' tbody tr').removeClass('selected');
												for (i = Math.min(start, end); i <= Math.max(start, end); i++) {
													if (!$('#nodes-table'+ cntable.vizuuid+' tbody tr').eq(i).hasClass('selected')){
										                $('#nodes-table'+ cntable.vizuuid+' tbody tr').eq(i).addClass('selected');
										            }
													var id = $('#nodes-table' + cntable.vizuuid).dataTable().fnGetData(i).displayLabel;
													idList.push(parseInt(id));
												}
												var resetNodes = true;
												var resetLinks = false;
												var multiselectModel = new MultiSelectModel(cntable.vizuuid, "nodes", idList);
												multiselectModel.doTask(resetNodes,resetLinks);
											}
										 //Action while pressing control key
											else if ((e.ctrlKey || e.metaKey)) {
												$(this).toggleClass('selected');
												cntable.selectNodeById.doTask(nodeId,false, true);
											} else {
												$('#nodes-table'+ cntable.vizuuid+' tbody tr').removeClass('selected');
												 $(this).toggleClass('selected');
												 cntable.selectNodeById.doTask(nodeId,true, true);
											}
										    cntable.lastChecked = this;
								            e.stopPropagation();
									});
								}
					});
	this.oTable.data("controller", this);
}
CreateNodeTable.prototype.findData = function(id, myData){
	var data;
	var cnTable = this;
	if (id == -1){
		data = myData;
	} else {
		$.each(myData, function(){
			if (this.itemId == id){
				data = this;
				return;
			} else if (this.bundle) {
				data = cnTable.findData(id, this.children);
				if (data){
					return data;
				}
			}
		});
	}
	if (data.bundle){
		data = data.children;
	}
	return data;
}
CreateNodeTable.prototype.processBundleNavigation = function(oData, bundleData){
	//Dont do anything if they are same
	if (bundleData.itemId == this.currentDisplay) {
		return;
	}
	if (this.currentDisplay == "-1") {
		var nodes = this.oTable.fnGetData();
		var mNode = $('<li></li>').append($("<a></a>").text("Nodes").attr("id", "-1"));
		$.data(mNode[0], "mydata", nodes);//setting the data 
		var arrow = $("<span>&nbsp</span>").attr("class", "divider");
		var bNode = $('<li></li>').append($("<a></a>").text(bundleData.label).attr("id", bundleData.itemId));
		$("#toolbar-node-" + this.vizuuid).html('').append(mNode).append(arrow).append(bNode);
		this.addListener($(bNode).find('a'));
		this.addListener($(mNode).find('a'));
	} else {
		var arrow = $("<span>&nbsp</span>").attr("class", "divider");
		var bNode = $('<li></li>').append($("<a></a>").text(bundleData.label).attr("id", bundleData.itemId));
		$("#toolbar-node-" + this.vizuuid).append(arrow).append(bNode);
		this.addListener($(bNode).find('a'));
	}
	this.currentDisplay = bundleData.itemId;
}
CreateNodeTable.prototype.reArrangeBreadCrumb = function(element){
	var remove = false;
	if ($(element).attr("id") == "-1"){
		$("#toolbar-node-" + this.vizuuid).html('');
		return;
	}
	$.each($(element).parent().parent().children(), function(){
		if (remove){
			$(this).remove();
		}
		if(!remove && $(this).find('a').attr('id') == $(element).attr("id")){
			remove = true;
		}
	});
}
CreateNodeTable.prototype.addListener = function(element){
	var cntable = this;
	$(element).click(function() {
		  var id = $(this).attr("id");
		  if (id == cntable.currentDisplay){
			  return;
		  }
		  var data = cntable.findData(id, cntable.myData);
  		  cntable.oTable.fnClearTable();
		  cntable.oTable.fnAddData(data);
		  cntable.reArrangeBreadCrumb(this);
		  cntable.currentDisplay = id;
	});
}
