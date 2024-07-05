function DataTableFilter(dataTable) {
	this.dataTable = dataTable;	
}

DataTableFilter.prototype.filterTable = function () {
	var datatableFilter = this;
	var tableData = this.dataTable.data('filterCriteria');
	this.dataTable.dataTableExt.afnFiltering = [];
	$.each(tableData, function() {
		var filter = this;
		datatableFilter.dataTable.dataTableExt.afnFiltering.push(
			function( oSettings, aData, iDataIndex ) {
				return datatableFilter.filterDelegate(aData[filter.index], filter.pattern, filter.operator, filter.caseFlag);
			}
		);
	});
	this.dataTable.fnDraw();
}

DataTableFilter.prototype.filterDelegate = function(valueToMatch, patternValue, operator, caseFlag) {
	var result;
	if (operator == "STARTS_WITH") {
		result = Filter.startsWith(valueToMatch, patternValue, caseFlag);
	} else if (operator == "ENDS_WITH") {
		result = Filter.endsWith(valueToMatch, patternValue, caseFlag);
	} else if (operator == "CONTAINS") {
		result = Filter.contains(valueToMatch, patternValue, caseFlag);
	} else if (operator == "EQUALS") {
		result = Filter.equals(valueToMatch, patternValue, caseFlag);
	} else if (operator == "GT") {
		result = Filter.isGreaterThan(valueToMatch, patternValue, caseFlag);
	} else if (operator == "GEQ") {
		result = Filter.isGreaterThanOrEqualTo(valueToMatch, patternValue, caseFlag);
	} else if (operator == "LT") {
		result = Filter.isLessThan(valueToMatch, patternValue, caseFlag);
	} else if (operator == "LEQ") {
		result = Filter.isLessThanOrEqualTo(valueToMatch, patternValue, caseFlag);
	} else if (operator == "PATTERN_MATCH") {
		result = Filter.patternMatch(valueToMatch, patternValue, caseFlag);
	}
	return result;
}

DataTableFilter.prototype.removeFilter = function() {
	this.dataTable.removeData('filterCriteria');
	this.dataTable.dataTableExt.afnFiltering = [];
	this.dataTable.fnDraw();
}