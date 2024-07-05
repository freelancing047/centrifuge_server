function CreatePathsTable(vizuuid) {
	this.vizuuid = vizuuid;
}
CreatePathsTable.prototype.doTask = function() {
	$('#result-find-data-table' + this.vizuuid).dataTable({
		"aoColumns": [
              {"sTitle": "Name", "mData": "name"},
              {"sTitle": "Length", "mData": "length"}
         ],
        "sScrollY": "150px",
        "aaData": [],       
        "bfilter": false,
        "bDestroy": true,
        "bAutoWidth":true,
        "sDom": '<flp><"clear">',
        "bPaginate": true,
		"bJqueryUI": true,
		"sPaginationType": "full_numbers"
	});
}
