function CreateSearchResultsTable(vizuuid){
	this.vizuuid = vizuuid;
}

CreateSearchResultsTable.prototype.doTask = function(){	
	var vizuuid = this.vizuuid;
	$('#graph-search-result-find-data-table' + vizuuid).dataTable({
		"aoColumns": [	{"sTitle": "Label", "mData": "column1_data","sClass" : "header1"},
		              	{"sTitle": "Type", "mData": "column2_data","sClass" : "header2"},
		              	{"bVisible" : false, "mData" : "id"}],
        "sScrollY": "98px",
        "aaData": [],
        "sPaginationType": "full_numbers",
        "bfilter": false,
        "bDestroy": true,
        "bAutoWidth":true,
        "sDom": 'T<"clear">lfrtip',
        "oTableTools": {
			"sRowSelect": "multi",
			"aButtons": []
		}		
		
	});
	
	
	
}

