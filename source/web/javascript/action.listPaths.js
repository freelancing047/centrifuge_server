function ListPaths(vizuuid) {
	this.vizuuid = vizuuid;
}
ListPaths.prototype.doTask = function(data) {
	var vizuuid = this.vizuuid;
	var resultsTable = $('#result-find-data-table' + vizuuid).dataTable();
	resultsTable.fnClearTable();
	resultsTable.fnAddData(data["resultData"]);
	resultsTable.fnDraw();
	var highlightPath = new HighlightPath(vizuuid);
	$(resultsTable .fnGetNodes()).click(
			function(e) {
				$('#selectpath' + vizuuid).removeAttr("disabled");
				$('#addpathtoselection' + vizuuid).removeAttr("disabled");
				var resultsTable = $('#result-find-data-table' + vizuuid)
						.dataTable();
				var index = resultsTable.fnGetPosition(this);
				highlightPath.doTask(index);
				var selectPath = new SelectPath(vizuuid);
				$('#selectpath' + vizuuid).click(
						function(e) {
							var resultsTable = $(
									'#result-find-data-table' + vizuuid)
									.dataTable();
							selectPath.doTask(index);
						});
				var addPathToSelection = new AddPathsToSelection(vizuuid);
				$('#addpathtoselection' + vizuuid).click(
						function(e) {
							var resultsTable = $(
									'#result-find-data-table' + vizuuid)
									.dataTable();
							addPathToSelection.doTask(index);
						});
			});

}
