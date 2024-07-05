function GraphSearch(vizuuid, searchType, visibleGraphSearch, nodeDefsMap, linkDefsMap){
	this.vizuuid = vizuuid;
	this.searchType = searchType;
	this.visibleGraphSearch = visibleGraphSearch;
	this.nodeDefsMap = nodeDefsMap;
	this.linkDefsMap = linkDefsMap;
}

GraphSearch.prototype.doTask = function(){
	var requestJSON = JSON.stringify(this.getSearchRequestJson());
	var url = "/Centrifuge/services/graphs/search/query?_f=json&vizuuid="+this.vizuuid+"&dvuuid="+window.dataview.myData.resultData.uuid;
	var vizuuid = this.vizuuid;
	var graphSearch = this;
	$.ajax({
		type : "POST",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: requestJSON,
		complete : function(data){					
			var searchResultDataTable = $('#graph-search-result-find-data-table' + vizuuid).dataTable();
			searchResultDataTable.fnClearTable();			
			searchResultDataTable.fnAddData(graphSearch.getSearchResponseData(data,graphSearch));
			if(graphSearch.visibleGraphSearch){
				$('#searchGraphSelectAll'+vizuuid).removeClass('disabled-link');
			}else{
				$('#searchGraphAddAll'+vizuuid).removeClass('disabled-link');
			}
			
			$(searchResultDataTable.fnGetNodes()).click( function( e ) {
		        var id = $('#graph-search-result-find-data-table' + vizuuid).dataTable().fnGetData(this)['id'];		        
		        $('#searchGraphSelectChoosen'+vizuuid).removeClass('disabled-link');		        
		        $('#searchGraphAddChoosen'+vizuuid).removeClass('disabled-link');
			});			
			
			graphSearch.toggleSearchActions(searchResultDataTable);
		}
		
	});
	$('#searchOptions' + vizuuid).click( function() {
		var wsIndex = (utils.getWorksheet4Visualization(vizuuid)).index;
		var pos = (utils.getVisualisation(vizuuid)).visualization.position;
		var element = $('div#layout'+wsIndex+'_panel'+pos).find("#relation-graph");
		(element.parent()).find('#graph-max-limit-info').remove();
		element.show();
	});
}

GraphSearch.prototype.getSearchRequestJson = function(){
	var requestJson = {};	
	requestJson.class = "csi.server.common.dto.graph.search.GraphSearch";
	requestJson.searchType = this.searchType;
	requestJson.visibleGraphSearch = this.visibleGraphSearch;
	requestJson.nodeCriteria = this.searchType == "NODES" ? this.getNodeCriteria(this.searchType) : [];
	requestJson.linkCriteria = this.searchType == "LINKS" ? this.getLinkCriteria(this.searchType) : [];
	return requestJson;
	
}

GraphSearch.prototype.getNodeCriteria = function(searchType){
	var rootNode = $('#graph-search-tab' + this.vizuuid).find('.search-graph-tree').dynatree("getRoot");
	var selectedNodes = rootNode.tree.getSelectedNodes();
	var nodeCriteria = [];
	var graphSearch = this;
	$.each(selectedNodes, function(index, node){
		if(node.hasChildren() && node.data.key != "selectAll"){
			var nodecriterion = graphSearch.getNodeCriterion(node.data.title, node);			
			nodeCriteria.push(nodecriterion);
		}
	});
	return nodeCriteria;
	
}

GraphSearch.prototype.getNodeCriterion = function(name, node){
	var nodeCriterionJson = {};
	nodeCriterionJson.class = "csi.server.common.dto.graph.search.NodeSearchCriterion";
	nodeCriterionJson.attributeCriteria = [];
	nodeCriterionJson.nodeDef = this.nodeDefsMap[name];
	$.each(node.getChildren(), function(index, attribute) {
		if (attribute.data.criteria != undefined) {
			$.merge(nodeCriterionJson.attributeCriteria, attribute.data.criteria);
		}
	});	
	return nodeCriterionJson;
	
}

GraphSearch.prototype.getLinkCriteria = function(){
	var rootNode = $('#graph-search-tab' + this.vizuuid).find('.search-graph-tree').dynatree("getRoot");
	var selectedNodes = rootNode.tree.getSelectedNodes();
	var linkCriteria = [];
	var graphSearch = this;
	$.each(selectedNodes, function(index, node){
		if(node.hasChildren() && node.data.key != "selectAll" && node.data.isLink == true){			
			linkCriteria.push(graphSearch.getLinkCriterion(node));
		}
	});
	return linkCriteria;
}

GraphSearch.prototype.getLinkCriterion = function(node){
	var searchgraph = this;
	var linkCriterionJson = new Object();
	linkCriterionJson.attributeCriteria = [];
	linkCriterionJson.class = "csi.server.common.dto.graph.search.LinkSearchCriterion";
	linkCriterionJson.linkDef = this.linkDefsMap[node.data.key];	
	$.each(node.getChildren(), function (index, childNode) {
		if (childNode.hasChildren() && childNode.data.title == node.data.title) {
			linkCriterionJson.node1 = searchgraph.getNodeCriterion(childNode.data.title, childNode);
		} else if (childNode.hasChildren() && childNode.data.title == node.data.toLink) {
			linkCriterionJson.node2 = searchgraph.getNodeCriterion(childNode.data.title, childNode);
		} else if (!childNode.hasChildren() && childNode.data.criteria != undefined) {
			$.merge(linkCriterionJson.attributeCriteria, childNode.data.criteria);
		}
		
	});	
	return linkCriterionJson;	
}
GraphSearch.prototype.getSearchResponseData = function(data, graphSearch){
	var response = JSON.parse(data.responseText);
	var isNodeOrLink = true;
	if(graphSearch.searchType == "NODES"){
		aaData = response.resultData.nodes;
	}else{
		aaData = response.resultData.links;
		isNodeOrLink = false;
	}
	return this.getDataForDataTable(aaData, isNodeOrLink);
}

GraphSearch.prototype.getDataForDataTable = function(aaData,isNodeOrLink){
	var returnData = new Array();	
	$.each(aaData, function(index,rowData){
			var row = new Object();
			if(isNodeOrLink){
				row['column1_data'] = rowData['label'];
				row['column2_data'] = rowData['type'];
			}else{
				row['column1_data'] = rowData['sourceLabel'];
				row['column2_data'] = rowData['targetLabel'];
			}
			row['id'] = rowData['id'];
			returnData.push(row);
	});	
	return returnData;
}
GraphSearch.prototype.toggleSearchActions = function(searchResultDataTable){
	if(this.visibleGraphSearch){
		if(searchResultDataTable.fnGetData().length > 0){
			$('#searchGraphSelectAll' + this.vizuuid).removeClass('disabled-link');
		}else{
			$('#searchGraphSelectAll' + this.vizuuid).addClass('disabled-link');
			$('#searchGraphSelectChoosen' + this.vizuuid).addClass('disabled-link');
		}
	}else{
		if(searchResultDataTable.fnGetData().length > 0){
			$('#searchGraphAddAll' + this.vizuuid).removeClass('disabled-link');
		}else{
			$('#searchGraphAddAll' + this.vizuuid).addClass('disabled-link');
			$('#searchGraphAddChoosen' + this.vizuuid).addClass('disabled-link');
		}
	}
}