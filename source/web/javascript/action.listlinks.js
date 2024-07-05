function ListLinks(vizuuid) {
	this.vizuuid = vizuuid;
}
ListLinks.prototype.doTask = function() {
	var listlinks = this;
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
		return function(data) {
			var oTable = $('#links-table' + vizuuid).dataTable();
			if (oTable.length == 0){
				return;
			}
			$('#links-table' + vizuuid).dataTable().data('linkslist', listlinks.getFormattedData(data.resultData));
			oTable.fnClearTable();
			oTable.fnAddData(data.resultData);
			var selectLinkById = new SelectLinkById(vizuuid);
			var lastChecked = false;
			$(oTable.fnGetNodes()).click( function( e ) {
					var linkId = $('#links-table' + vizuuid).dataTable().fnGetData(this)['itemId'];
					
					if (!lastChecked) {
						lastChecked = this;
					}
					//Action while pressing shift key
					if (e.shiftKey) {
							var start = $('#links-table'+ vizuuid+' tbody tr').index(this);
							var end = $('#links-table'+ vizuuid+' tbody tr').index(lastChecked);
							var idList = new Array();
							$('#links-table'+ vizuuid+' tbody tr').removeClass('selected');
							for (i = Math.min(start, end); i <= Math
									.max(start, end); i++) {
								if (!$('#links-table'+ vizuuid+' tbody tr').eq(i).hasClass('selected')){
					                $('#links-table'+ vizuuid+' tbody tr').eq(i).addClass('selected');
					            }
								 var id = $('#links-table' + vizuuid).dataTable().fnGetData(i)['itemId'];
								 idList.push(id);
								 }
								   var resetNodes = false;
							       var resetLinks = true;
							       var multiselectModel = new MultiSelectModel(vizuuid,"links",idList);
							       multiselectModel.doTask(resetNodes,resetLinks);
							}
					 //Action while pressing control key
						  else if ((e.metaKey || e.ctrlKey)) { 
							  $(this).toggleClass('selected');
							  selectLinkById.doTask(linkId,false, true);
						} else {
							$('#links-table'+ vizuuid+' tbody tr').removeClass('selected');
							$(this).toggleClass('selected');
							selectLinkById.doTask(linkId,true, true);
						}
	
						lastChecked = this;
						// end
					    e.stopPropagation();
			 });
			$(oTable.fnGetNodes()).find('input:checkbox').click( function(e){ 
					 var event = e;
					 var linkId = $('#links-table' + vizuuid).dataTable().fnGetData(this.parentNode.parentNode)['itemId'];
					 var linkHideUnhideTask = new LinkHideUnhideTask(vizuuid,linkId);
					 linkHideUnhideTask.doTask(event);
			 });
		};
	};

	csi.relgraph.listLinks(vizuuid, {
		onsuccess : doSuccess()
	});
}

ListLinks.prototype.getFormattedData = function(tableData) {
	var formattedData = [];
	$.each(tableData, function() {
		this.sourceValue = this.source.displayLabel;
		this.targetValue = this.target.displayLabel;
		formattedData.push(this);
	});
	return formattedData;
}
