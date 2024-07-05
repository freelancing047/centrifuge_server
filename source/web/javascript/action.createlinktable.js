function CreateLinkTable(vizuuid) {
	this.vizuuid = vizuuid;
}
CreateLinkTable.prototype.doTask = function() {
	this.createTable();
}

CreateLinkTable.prototype.createTable = function(){
	
	$('#links-table' + this.vizuuid).dataTable({
			"aoColumns" : [
				{
					"sTitle" : "Source",
					"fnRender" : function(obj) {
						var sReturn = obj['aData']['source']['displayLabel'];
						if (obj['aData']['source']['hidden']) {
							obj['aData']['hideMe'] = true;
						}
						return sReturn;
					},
					"mData" : "source"
				},
				{
					"sTitle" : "Target",
					"fnRender" : function(obj) {
						var sReturn = obj['aData']['target']['displayLabel'];
						if (obj['aData']['target']['hidden']) {
							obj['aData']['hideMe'] = true;
						}
						return sReturn;
					},
					"mData" : "target"
				},
				{
					"sTitle" : "Type",
					"mData" : "displayType"
				},
				{
					"sTitle" : "Hidden",
					"fnRender" : function(obj) {
						var sReturn = '';
						if (obj['aData']['hidden']) {
							sReturn = '<input name="" type="checkbox" value="" checked= "checked">'
						} else {
							sReturn = '<input name="" type="checkbox" value="">'
						}
						if (obj['aData']['hideMe']) {
							sReturn = '<input name="" type="checkbox" value="" checked= "checked" disabled= "disabled">'
						}
						return sReturn;
					},
					"mData" : "hidden"
				} 
			],
			"sScrollY": "150px",
			"bPaginate": true,
			"bJqueryUI": true,
			"bDestroy" : true,
			"sPaginationType": "full_numbers"
		});

	}