function RGPanel(rgView, index, rgPanelObj) {
	this.panelOffset = 50;
	this.leftStartOffset = 10;
	this.topStartOffset = 40;
	if(!rgView){ return; }
	this.rgView = rgView;
	this.index = rgView.vizuuid;
	this.rgPanelObj = rgPanelObj;
	this.searchInNodeOrLink = "nodes";
	this.isSearchInGraph = false;
	this.isColorpickerExpanded = false;
	this.default_color = '#fefefe';
	this.topWindowOffset = 96;
}

RGPanel.fixedNodeLinkAttributeServerParamMap = {
		"Type" : "type",
		"Label" : "label",
		"Count" : "count",
		"Number of Neighbors" : "degree",
		"Betweenness" : "Betweenness",
		"Closeness" : "Closeness",
		"Eigenvector" : "Eigenvector"
}

RGPanel.prototype.changeVisibility = function(panel) {
	$('.relation-panel').css('z-index', 0);
	$(panel).css('z-index', 10);
}
RGPanel.prototype.registerDraggable = function() {
	var rgPanel = this;
	var changeVisible = function(rgPanel) {
		return function(event, ui) {
			rgPanel.changeVisibility(this);
		};
	};
	$('.relation-panel').draggable({
		containment : 'div.tab-content',
		drag : changeVisible(rgPanel),
		handle : 'h2.box_head'
	});
	$('.relation-panel').click(changeVisible(rgPanel));
}

RGPanel.prototype.generateTitleDiv = function(title) {
	var h2 = $('<h2>').addClass('box_head round_top');
	var leftIcon = '<span src style="width: 0px; height: 32px; margin-top: -3px; margin-left: 23px; float: left;" class="left_icons"></span>';
	var h2Text = $('<strong>').text('Relationship Graph' + ' - ' + title);
	return h2.append(leftIcon).append(h2Text);
}

RGPanel.prototype.getTitlePanelImages = function(vizUuid) {
	var id = {};
	id.vizUuid = vizUuid;
	id.index = this.index;
	return Mustache.render($('#large-panel-toolbar-layout').html(), id);
}

RGPanel.prototype.getLegendHTML = function(index) {
	 collapseExpandLegend=new CollapseExpandLegend(index);
	var lid = 'legendPanel' + index;
	var lbid = 'legendBody' + index;
	var legendPanelDiv = $('<div>').addClass('graphLegend').attr('id', lid);
	var graphTitleBarDiv = $('<div>').addClass('graphTitleBar');
	var rgPanel = this;
	var table = "<table summary=\"\"><tr><td nowrap=\"nowrap\" width=\"100%\">Legend</td><td align=\"right\" nowrap=\"nowrap\"><span class=\"togglelegend\">-</span>&nbsp;&nbsp;&nbsp;<span class=\"closelegend\">x</span></td></tr></table>";
	var legendDiv = $('<div>').addClass('legendBody').attr('id', lbid);	
	var view = {
		index: index
	};
	var legendSummaryDiv = Mustache.render($('#legendPanelSummaryTemplate').html(), view); 
	legendDiv.append(legendSummaryDiv);
	graphTitleBarDiv.append(table);
	graphTitleBarDiv.find('.closelegend').click(function() {
		rgPanel.hideLegendPanel();
	});
	
	graphTitleBarDiv.find('.togglelegend').click(function(event) {
		new CollapseExpandLegend(index).doTask(this);
	});
	
	
	legendPanelDiv.append(graphTitleBarDiv);
	legendPanelDiv.append(legendDiv);
	legendPanelDiv.draggable({
		handle: '.graphTitleBar',
		containment: 'parent'
	});
	
	legendPanelDiv.css('position', 'absolute');
	return legendPanelDiv;
}
RGPanel.prototype.getTooltipHTML = function(index) {
	var ttid = 'tooltipPanel' + index;
	var htmlDiv = $('<div>').addClass('graphTooltip').attr('id', ttid);
	return htmlDiv;
}

RGPanel.prototype.generateToggleContainer = function(relImage, index) {
	var rgPanel = this;
	var id = 'myContainer' + index;
	var did = relImage.viz;
	var normalizedWindowHeight = $(window).height() - this.topWindowOffset;
	var toggleDiv = $('<div>').addClass('toggle_container relation_panel_div').css('height', (normalizedWindowHeight - 33)+'px');
	var absoluteDiv = $('<div>').addClass('absolute_div').css('position', 'absolute').css({'width': '100%', 'height': (normalizedWindowHeight - 29)+'px'});
	var innerDiv = $('<div>').addClass(
			'block box_content round_bottom std_padding');
	var btnGrpDiv = $('<div>').addClass('btn-group');
	var relGrpDiv = $('<div>').attr('id', 'relation-graph').css({'height': (normalizedWindowHeight - 66)+'px', 'overflow': 'hidden'});
	var zoomEleDiv = $('<div>').attr('id', id).css({'height': (normalizedWindowHeight - 68)+'px'});
	var relImg = $('<canvas>').attr('id', did);
	var toolBarEle = this.generateToolBar();
	var toolPanelEle = this.generateToolPanel();
	zoomEleDiv.append(relImg);
	relGrpDiv.append(this.getToolPanel());
	relGrpDiv.append(zoomEleDiv);
	relGrpDiv.append(this.getTooltipHTML(relImage.viz));
	relGrpDiv.append(this.getLegendHTML(relImage.viz));
	btnGrpDiv.append(toolBarEle).append(relGrpDiv);
	absoluteDiv.append(btnGrpDiv);
	absoluteDiv.append(this.generateLists());
	innerDiv.append(absoluteDiv);
	toggleDiv.append(innerDiv);
	return toggleDiv;
}
RGPanel.prototype.generateToolPanel = function() {
	var result = $('#zoom-tool-panel').html();
	result = result.replace(/\{{index\}}/g, this.rgView.vizuuid);
	return result;
}
RGPanel.prototype.generateToolBar = function() {
	return Mustache.render($('#rg-toolbar-src').html(), {'index' : this.index });
}
RGPanel.prototype.bindNodeEditorPanelIconClick = function(){
	var modalDiv = $($('#edit-node-modal-div').html()).filter('.edit-node-modal');	
	var modalBody=$($(modalDiv).html()).filter('.modal-body');
	$($(modalDiv).html()).filter('.modal-body').find('#add-computed-field-button').click(function() {
		var resultsTable = $('#computed-fields-table').dataTable();
		oTable.fnClearTable();
		resultsTable.fnDraw();
	});
}
RGPanel.prototype.bindToolPanelIconClick = function() {
	var rgView = this.rgView;
	var rgPanel = this;
	var vizuuid = rgView.vizuuid;
	$('#select-button' + rgView.vizuuid).click(function() {		
		rgView.draggraph.setDragMode("select");
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
	});
	$('#pan-button' + rgView.vizuuid).click(function() {		
		rgView.draggraph.setDragMode("pan");
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
	});
	$('#zoom-button' + rgView.vizuuid).click(function() {		
		rgView.draggraph.setDragMode("zoom");
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
	});
	$('#cursor-button' + rgView.vizuuid).click(function() {		
		rgView.draggraph.setDragMode("cursor");
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
		rgPanel.bindCanvasMode();
	});
	$('#arrow-in-button' + rgView.vizuuid).click(function() {		
		var fittosize = new FitToSize(vizuuid);
		fittosize.doTask();
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
		rgPanel.bindCanvasMode();
	});
	$('#arrow-out-button' + rgView.vizuuid).click(function() {		
		var fittoselection = new FitToSelection(vizuuid);
		fittoselection.doTask();
		$('#' + rgPanel.rgView.vizuuid).unbind('mousemove', rgPanel.findCanvasDragMode);
		rgPanel.bindCanvasMode();
	});
	this.bindCanvasMode();
}
RGPanel.prototype.bindCanvasMode = function() {
	var rgPanel = this;
	canvas = $('#' + rgPanel.rgView.vizuuid)[0];
	if(_.isEmpty(rgPanel.rgView.draggraph)){
		rgPanel.rgView.doRegister();
		rgPanel.rgView.draggraph = new DragGraph(rgPanel.rgView.vizuuid);
		rgPanel.rgView.draggraph.doRegister();
		rgPanel.rgView.draggraph.setImageAreaSelection(canvas);
	}
	this.findCanvasDragMode = function(event) {
		var canvasOffset = $(canvas).offset();
		var canvasX = Math.floor(event.pageX - canvasOffset.left);
		var canvasY = Math.floor(event.pageY - canvasOffset.top);
		var vizbgColorInt = utils.getVizBgColorInt(rgPanel.rgView.vizuuid);
		var context = this.getContext("2d");
		var pixel = context.getImageData(canvasX, canvasY, 1, 1);
		var rgb = pixel.data;
		var bGColorInt = utils.rgbComp2Int(rgb[0], rgb[1], rgb[2]);
		if(bGColorInt == vizbgColorInt) {
			rgPanel.rgView.draggraph.setDragMode("pan");
		} else {
			rgPanel.rgView.draggraph.setDragMode("cursor");
		}
	}
	$(canvas).bind('mousemove', this.findCanvasDragMode);
}
RGPanel.prototype.bindToolbarIconClick = function() {
	$('#config'+this.index).click(
			function() {
				$(this).parent().parent().parent().find(
						'.toggle_container .btn-group .rg-toolbar')
						.slideToggle();
				return false;
			});
	var rgView = this.rgView;
	var rgPanel = this;
	$('#savegraph' + rgView.vizuuid).click(function() {
		var savegraph = new SaveGraph(rgView.vizuuid);
		savegraph.doTask();
	});
	$('#selectneighbor' + rgView.vizuuid).click(function() {
		$('#select-neighbor-dialogue'+rgView.vizuuid).modal();
	});
	$('#appearanceeditor' + rgView.vizuuid).click(function() {
		$('#appearance-editor-dialogue'+rgView.vizuuid).modal();
	});
	$('#revealneighbor' + rgView.vizuuid).click(function() {
		$('#reveal-neighbor-dialogue'+rgView.vizuuid).modal();
	});
	$('#deselectall' + rgView.vizuuid).click(function() {
		var deselectall = new DeSelectAll(rgView.vizuuid);
		deselectall.doTask();
	});
	$('#selectall' + rgView.vizuuid).click(function() {
		var selectall = new SelectAll(rgView.vizuuid);
		selectall.doTask();
	});
	$('#hideselection' + rgView.vizuuid).click(function() {
		var hideselection = new HideSelection(rgView.vizuuid);
		hideselection.doTask();
	});
	$('#unhideselection' + rgView.vizuuid).click(function() {
		var unhideselection = new UnHideSelection(rgView.vizuuid);
		unhideselection.doTask();
	});
	$('#centrifugeLayout' + rgView.vizuuid).click(function(){
		var centrifugeLayout = new Layout(rgView.vizuuid);
		centrifugeLayout.doTask('centrifuge');
	});
	$('#circularLayout' + rgView.vizuuid).click(function(){
		var centrifugeLayout = new Layout(rgView.vizuuid);
		centrifugeLayout.doTask('circular');
	});
	$('#forceDirectedLayout' + rgView.vizuuid).click(function(){
		var forceDirectedLayout = new Layout(rgView.vizuuid);
		forceDirectedLayout.doTask('forceDirected');
	});
	$('#hierarchicalLayout' + rgView.vizuuid).click(function(){
		var hierarchicalLayout = new Layout(rgView.vizuuid);
		hierarchicalLayout.doTask('hierarchical');
	});
	$('#radialLayout' + rgView.vizuuid).click(function(){
		var radialLayout = new Layout(rgView.vizuuid);
		radialLayout.doTask('radial');
	});
	$('#scrambleLayout' + rgView.vizuuid).click(function(){
		var scrambleLayout = new Layout(rgView.vizuuid);
		scrambleLayout.doTask('scramble');
	});
	$('#computeSNA' + rgView.vizuuid).click(function(){
		var computeSNA = new ComputeSNA(rgView.vizuuid);
		computeSNA.doTask();
	});
	$('#hidelegend' + rgView.vizuuid).click(function() {
		rgPanel.toggleLegendPanel();
	});
	$('#resetlegend' + rgView.vizuuid).click(function() {
		rgPanel.resetLegendPanel();
	});	
	$('#bundleselection' + rgView.vizuuid).click(function() {
		$('#bundle-dialogue' + rgView.vizuuid).modal();
	});	
	$('#unbundleselection' + rgView.vizuuid).click(function() {
		$('#unbundle-dialogue' + rgView.vizuuid).modal();
	});	
	$('#unbundleentiregraph' + rgView.vizuuid).click(function() {
		var unBundleEntireGraph = new UnBundleEntireGraph(rgView.vizuuid);
		unBundleEntireGraph.doTask();
	});		
	$('#settings' + rgView.vizuuid).click(function(){
		var createtrgSettings = new CreateRgSettings(rgView.vizuuid);
		createtrgSettings.generateSettingsDialog();
	});
	$('#timeplayer' + rgView.vizuuid).click(function(){
		var tp = $('#timeplayer' + rgView.vizuuid).data('controller');
		if(!tp){
			tp = new CreateTimePlayer(rgView.vizuuid);
                        $('#graph-time-player-control-tab' + rgView.vizuuid).show();
			$('#timeplayer' + rgView.vizuuid).data('controller', tp);
                        tp.doOpen();
		}
                else{
                      $('#timeplayer' + rgView.vizuuid).data('controller', '');
                      $('#graph-time-player-control-tab' + rgView.vizuuid).hide();
                      tp.doClose();
                }
        });
$("#time-player-header" + rgView.vizuuid + " .graph-time-tab"). click(function(){
    if($("#graph-time-player-control-tab" + rgView.vizuuid).css("display") == 'none') {    
        $('#timeplayer' + rgView.vizuuid).click();
    }
});

$("#nodes-list-tab-header" + rgView.vizuuid + " .nodes-list-tab"). click(function(){
    if($("#graph-time-player-control-tab" + rgView.vizuuid).css("display") == 'block') {    
        $('#timeplayer' + rgView.vizuuid).click();
    }
});

$("#links-list-tab-header" + rgView.vizuuid + " .links-list-tab"). click(function(){
    if($("#graph-time-player-control-tab" + rgView.vizuuid).css("display") == 'block') {    
        $('#timeplayer' + rgView.vizuuid).click();
    }
});

$("#find-list-tab-header" + rgView.vizuuid + " .find-list-tab"). click(function(){
    if($("#graph-time-player-control-tab" + rgView.vizuuid).css("display") == 'block') {    
        $('#timeplayer' + rgView.vizuuid).click();
    }
});

$("#graph-search-tab-header" + rgView.vizuuid + " .graph-search-tab"). click(function(){
    if($("#graph-time-player-control-tab" + rgView.vizuuid).css("display") == 'block') {    
        $('#timeplayer' + rgView.vizuuid).click();
    }
});
	
}
RGPanel.prototype.generateLists = function() {
	var linksTabId = 'links-tab' + this.index;
	var nodesTabId = 'nodes-tab' + this.index;
	var findPathTabId = 'find-path-tab' + this.index;
	var graphSearchTabId = 'graph-search-tab' + this.index;
        var graphTimePlayerTabId = 'graph-time-player-tab' + this.index;
	
	var view = {
		index: this.index
	}
	var tabHeaders = $(Mustache.render($('#nodes-list-tab-template').html(), view));
	var tabsDivWrapper = $('<div>').addClass('collapse').attr('id', 'rel-tab-content'+this.index)
	var tabsDiv = $('<div>').addClass('tab-content rel-tab-content').css('min-height', '298px');
	var linksTabDiv = this.generateLinksTab(linksTabId);
	var nodesTabDiv = this.generateNodesTab(nodesTabId);
	tabsDiv.append(linksTabDiv).append(nodesTabDiv);
	tabsDiv.append(this.generateFindPathTab(findPathTabId));
	tabsDiv.append(this.generateGraphSearchTab(graphSearchTabId));
        tabsDiv.append(this.generateGraphTimePlayerTab(graphTimePlayerTabId));
	tabsDivWrapper.append(tabsDiv);
	tabHeaders.append(tabsDivWrapper);
	return tabHeaders;
}
RGPanel.prototype.generateLinksTab = function(tabid) {
	linksDiv = $('<div>').addClass('tab-pane row-fluid rg-tabs').attr('id', tabid);
	linksDiv.append(this.getTableFind("link"));
	$(linksDiv).find('[value^=Unbundle]').remove();
	var linksTableView = {
		index: this.index
	};
	var linksTableHtml = Mustache.render($('#links-table-template').html(), linksTableView);
	linksDiv.append(linksTableHtml);
	linksDiv = this.registerAdvancedButtonClick(linksDiv, false);
	// var tableid = 'links-table' + this.index;
	// linksDiv.append($('<table>').attr('cellpadding', '0').attr('border', '0')
			// .addClass('table table-striped list-tables').attr('id', tableid));
	return linksDiv;
}
RGPanel.prototype.generateNodesTab = function(tabid) {
	nodesDiv = $('<div>').addClass('tab-pane active row-fluid rg-tabs').attr('id', tabid);
	nodesDiv.append(this.getTableFind("node"));
	
	// var tableid = 'nodes-table' + this.index;
	var nodeTableView = {
		index: this.index
	};
	var nodesTableHtml = Mustache.render($('#nodes-table-template').html(), nodeTableView);
	nodesDiv.append(nodesTableHtml);
	nodesDiv = this.registerAdvancedButtonClick(nodesDiv, true);
	// nodesDiv.append($('<table>').attr('cellpadding', '0').attr('border', '0')
			// .addClass('table table-striped list-tables').attr('id', tableid));
	return nodesDiv;
}
RGPanel.prototype.registerAdvancedButtonClick = function(nodesOrLinksDiv, isNode) {	
	var rgpanel = this;
	nodesOrLinksDiv.find('.advanced-search-button').click(function() {
		if(isNode) {
			var createnodetable = $('#nodes-table' + rgpanel.rgView.vizuuid).dataTable().data('controller');
			var dataTableFilter = new DataTableFilter(createnodetable.oTable);
			dataTableFilter.removeFilter();
			nodesOrLinksDiv.find('#typeFilterVal').empty();
			$.each(rgpanel.rgView.nodes, function() {
				nodesOrLinksDiv.find('#typeFilterVal').append($('<option></option>').val(this).text(this));
			});			
			nodesOrLinksDiv.find('.advanced-menu-block #label').change( function() {
				if ($(this).is(":checked")) {
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterName').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterCase').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterVal').removeAttr('disabled');
				} else {
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterName').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterCase').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #labelFilterVal').attr('disabled', 'disabled');
				}
			});
			nodesOrLinksDiv.find('.advanced-menu-block #type').change( function() {
				if ($(this).is(":checked")) {
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterName').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterCase').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterVal').removeAttr('disabled');
				} else {
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterName').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterCase').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #typeFilterVal').attr('disabled', 'disabled');
				}
			});
			nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighbors').change( function() {
				if ($(this).is(":checked")) {
					nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborFilterVal').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborsFilterName').removeAttr('disabled');				
				} else {
					nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborFilterVal').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborsFilterName').attr('disabled', 'disabled');
				}
			});
			
			nodesOrLinksDiv.find('.advanced-menu-block #find').click(function() {				
				var filterCriteria = [];
				if (!(nodesOrLinksDiv.find('#labelFilterName').attr('disabled'))) {					
					var pattern = nodesOrLinksDiv.find('#labelFilterVal').val().trim();
					var operator = nodesOrLinksDiv.find('#labelFilterName').val();
					var caseFlag = nodesOrLinksDiv.find('#labelFilterCase').is(':checked');
					var filterCriterion = new FilterCriterion(0, pattern, operator, caseFlag);
					filterCriteria.push(filterCriterion);
				}
				if (!(nodesOrLinksDiv.find('#typeFilterName').attr('disabled'))) {
					var pattern = nodesOrLinksDiv.find('#typeFilterVal').val().trim();
					var operator = nodesOrLinksDiv.find('#typeFilterName').val();
					var caseFlag = nodesOrLinksDiv.find('#typeFilterCase').is(':checked');
					var filterCriterion = new FilterCriterion(1, pattern, operator, caseFlag);
					filterCriteria.push(filterCriterion);					
				}
				if (!(nodesOrLinksDiv.find('#visibleNeighborsFilterName').attr('disabled'))) {
					var pattern =  parseInt(nodesOrLinksDiv.find('#visibleNeighborFilterVal').val());
					var operator = nodesOrLinksDiv.find('#visibleNeighborsFilterName').val();
					var filterCriterion = new FilterCriterion(2, pattern, operator, false);
					filterCriteria.push(filterCriterion);					
				}				
				if (filterCriteria.length > 0) {
					createnodetable.oTable.data('filterCriteria', filterCriteria);					
					dataTableFilter.filterTable();
				} else {					
					dataTableFilter.removeFilter();
				}
				
			});
			
			nodesOrLinksDiv.find('.advanced-menu-block #hide-menu').click(function() {
				dataTableFilter.removeFilter();				
			});
			nodesOrLinksDiv.find('.advanced-menu-block #clear-selection').click(function() {
				dataTableFilter.removeFilter();
				
				nodesOrLinksDiv.find('.advanced-menu-block #label').attr('checked', false);			
				nodesOrLinksDiv.find('.advanced-menu-block #labelFilterName').attr('disabled', 'disabled').find('option:first').attr('selected', 'selected');	
				nodesOrLinksDiv.find('.advanced-menu-block #labelFilterCase').attr('disabled', 'disabled').attr('checked', false);	
				nodesOrLinksDiv.find('.advanced-menu-block #labelFilterVal').attr('disabled', 'disabled').val("");			
				
				nodesOrLinksDiv.find('.advanced-menu-block #type').attr('checked', false);
				nodesOrLinksDiv.find('.advanced-menu-block #typeFilterName').attr('disabled', 'disabled').find('option:first').attr('selected', 'selected');	
				nodesOrLinksDiv.find('.advanced-menu-block #typeFilterCase').attr('disabled', 'disabled').attr('checked', false);	
				nodesOrLinksDiv.find('.advanced-menu-block #typeFilterVal').attr('disabled', 'disabled');
				
				nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighbors').attr('checked', false);
				nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborFilterVal').attr('disabled', 'disabled').val(1);
				nodesOrLinksDiv.find('.advanced-menu-block #visibleNeighborsFilterName').attr('disabled', 'disabled').find('option:first').attr('selected', 'selected');
				
			});	
		} else {
			var linksTable = $('#links-table' + rgpanel.rgView.vizuuid).dataTable();
			var dataTableFilter = new DataTableFilter(linksTable);
			dataTableFilter.removeFilter();
			nodesOrLinksDiv.find('.advanced-menu-block #source').change( function() {
				if ($(this).is(":checked")) {
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterName').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterCase').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterVal').removeAttr('disabled');
				} else {
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterName').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterCase').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterVal').attr('disabled', 'disabled');
				}
			});
			nodesOrLinksDiv.find('.advanced-menu-block #target').change( function() {
				if ($(this).is(":checked")) {
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterName').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterCase').removeAttr('disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterVal').removeAttr('disabled');
				} else {
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterName').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterCase').attr('disabled', 'disabled');
					nodesOrLinksDiv.find('.advanced-menu-block #targetFilterVal').attr('disabled', 'disabled');
				}
			});
			nodesOrLinksDiv.find('.advanced-menu-block #find-link').click(function() {
				var filterCriteria = [];
				if (!(nodesOrLinksDiv.find('#sourceFilterName').attr('disabled'))) {
					var pattern =  nodesOrLinksDiv.find('#sourceFilterVal').val().trim();
					var operator = nodesOrLinksDiv.find('#sourceFilterName').val();
					var caseFlag = nodesOrLinksDiv.find('#sourceFilterCase').is(':checked');
					var filterCriterion = new FilterCriterion(0, pattern, operator, caseFlag);
					filterCriteria.push(filterCriterion);					
				}
				if (!(nodesOrLinksDiv.find('#targetFilterName').attr('disabled'))) {					
					var pattern =  nodesOrLinksDiv.find('#targetFilterVal').val().trim();
					var operator = nodesOrLinksDiv.find('#targetFilterName').val();
					var caseFlag = nodesOrLinksDiv.find('#targetFilterCase').is(':checked');
					var filterCriterion = new FilterCriterion(1, pattern, operator, caseFlag);
					filterCriteria.push(filterCriterion);					
				}				
				if (filterCriteria.length > 0) {					
					linksTable.data('filterCriteria', filterCriteria);					
					dataTableFilter.filterTable();
				} else {
					dataTableFilter.removeFilter();
				}			
				
			});
			
			nodesOrLinksDiv.find('.advanced-menu-block #hide-link-advance-menu').click(function() {				
				dataTableFilter.removeFilter();			
			});
			nodesOrLinksDiv.find('.advanced-menu-block #clear-link-filter').click(function() {
				dataTableFilter.removeFilter();
				
				nodesOrLinksDiv.find('.advanced-menu-block #source').attr('checked', false);			
				nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterName').attr('disabled', 'disabled').find('option:first').attr('selected', 'selected');	
				nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterCase').attr('disabled', 'disabled').attr('checked', false);	
				nodesOrLinksDiv.find('.advanced-menu-block #sourceFilterVal').attr('disabled', 'disabled').val("");			
				
				nodesOrLinksDiv.find('.advanced-menu-block #target').attr('checked', false);
				nodesOrLinksDiv.find('.advanced-menu-block #targetFilterName').attr('disabled', 'disabled').find('option:first').attr('selected', 'selected');	
				nodesOrLinksDiv.find('.advanced-menu-block #targetFilterCase').attr('disabled', 'disabled').attr('checked', false);	
				nodesOrLinksDiv.find('.advanced-menu-block #targetFilterVal').attr('disabled', 'disabled');				
				
			});
			
		}
		nodesOrLinksDiv.find('.advanced-menu-block').show();
		nodesOrLinksDiv.find('.search-div').hide();
		nodesOrLinksDiv.find('.nodes-links-table-wrapper').removeClass('span12').addClass('span8');
		RGPanel.redrawVisibleTables();
	});
	nodesOrLinksDiv.find('.advanced-hide-button').click(function() {
		nodesOrLinksDiv.find('.advanced-menu-block').hide();
		nodesOrLinksDiv.find('.search-div').show();
		nodesOrLinksDiv.find('.nodes-links-table-wrapper').removeClass('span8').addClass('span12');
		RGPanel.redrawVisibleTables();
	});
	return nodesOrLinksDiv;
}

RGPanel.prototype.resetLinksTable = function() {
	var rgpanel = this;
	var createLinkTable = new CreateLinkTable(rgpanel.rgView.vizuuid);
	createLinkTable.doTask();
	var listLinks = new ListLinks(rgpanel.rgView.vizuuid);
	listLinks.doTask();
}
RGPanel.prototype.resetNodesTable = function() {
	var rgpanel = this;
	var createNodeTable = new CreateNodeTable(rgpanel.rgView.vizuuid);
	createNodeTable.doTask();
	var listNodes = $.data($('#nodes-table' + rgpanel.rgView.vizuuid)[0], "controller");
	listNodes.doFetch();
}


RGPanel.prototype.redrawDataTables = function() {
	RGPanel.redrawVisibleTables();
	$('#bottom-tab-headers' + this.index + ' > li > a').on('shown', function (e) {
		RGPanel.redrawVisibleTables();
	});
	$('#find-path-tab-header' + this.index + ' > li > a').on('shown', function (e) {
		RGPanel.redrawVisibleTables();
	});
}
RGPanel.prototype.registerSearchForDatatables = function() {
	$('#rel-tab-content' + this.index +' .search-icon-span').click(function() {
		$(this).parent().parent().parent().parent().find('div.dataTables_scrollBody>table').dataTable().fnFilter($(this).prev().val(), 0);
	});
	$('#rel-tab-content' + this.index +' .search-bar').keyup(function(e) {
		if(e.keyCode == 13) {
			// Enter key pressed
			$($.fn.dataTable.fnTables(true)).dataTable().fnFilter($(this).val(), 0);
		}
	});
	$('#rel-tab-content' + this.index +' .search-clear').click(function() {
		$(this).parent().find('.search-bar').val('');
		$($.fn.dataTable.fnTables(true)).dataTable().fnFilter('', 0);
	});
}
RGPanel.prototype.registerSelectedClickForDatatables = function() {
	$('#rel-tab-content' + this.index +' .list-tables:not([id^=graph-search-result-find-data-table])').on('click', 'tbody tr', function( e ) {
        if ( $(this).hasClass('selected') ) {
            $(this).removeClass('selected');
        }
        else {
        	var parentTable = $(this).parent().parent();
            parentTable.find('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
    });
}
RGPanel.redrawVisibleTables = function() {
	var oTable = $.fn.dataTable.fnTables(true);
	$(oTable).css("width", "100%");
	if ( oTable.length > 0 ) {
        $(oTable).dataTable().fnDraw();
    }
}
RGPanel.prototype.getToolPanel = function() {
	var view = {
		index : this.index
	}
	return Mustache.render($('#zoom-tool-panel').html(), view);
}
RGPanel.prototype.getTableFind = function(lbl) {
	var view = {
		label : lbl,
		index : this.index,
		viz : this.rgView.vizuuid
	}
	return Mustache.render($('#rg-table-find').html(), view);
}
RGPanel.prototype.generateFindPathTab = function(findPathTabId) {
	var findPathDiv = $('<div>').addClass('tab-pane row-fluid').attr('id', findPathTabId);
	var indexView = {
		index: this.index
	}
	var html = Mustache.render($('#find-path-tab-template').html(), indexView);
	findPathDiv.append(html);
	this.registerFindTabItems(findPathDiv);	
	this.registerFindPathButtonClick(findPathDiv);
	this.registerStartNodeChooseClick(findPathDiv);
	this.registerSwitchNodesButtonClick(findPathDiv);
	return findPathDiv;
}
RGPanel.prototype.registerFindPathButtonClick = function(findPathDiv) {
	var index = this.index;
	var rgView = this.rgView;
	var vizuuid = this.rgView.vizuuid;
	findPathDiv.find('#find-path-button'+index).click(function() {
		$('#find-path-tab-header' + index + ' a:last').tab('show');	
		var findPathResults = new FindPathResults(vizuuid, $('#startnodeid' + rgView.vizuuid)[0].textContent, $('#endnodeid' + rgView.vizuuid)[0].textContent,$('#no_of_paths'+rgView.vizuuid)[0].value,$('#min_length'+rgView.vizuuid)[0].value,$('#max_length'+rgView.vizuuid)[0].value);
		findPathResults.doTask();
	});
}
RGPanel.prototype.registerSwitchNodesButtonClick = function(findPathDiv) {
	var index = this.index;
	var rgView = this.rgView;		
	findPathDiv.find('#switch-node'+index).click(function() {	
		if(findPathDiv.find('#startnodeid'+index)[0].textContent=="" && findPathDiv.find('#endnodeid'+index)[0].textContent==""){
			return false;
		}
		else{
		var startnodeid=findPathDiv.find('#startnodeid'+index)[0].textContent;
		var startnode=findPathDiv.find('#startnode'+index)[0].textContent;
		var endnodeid=findPathDiv.find('#endnodeid'+index)[0].textContent;
		var endnode=findPathDiv.find('#endnode'+index)[0].textContent;		
		findPathDiv.find('#startnodeid'+index)[0].textContent=endnodeid;
		findPathDiv.find('#startnode'+index)[0].textContent=endnode;
		findPathDiv.find('#endnodeid'+index)[0].textContent=startnodeid;
		findPathDiv.find('#endnode'+index)[0].textContent=startnode;
		}
	});
}

RGPanel.prototype.registerFindTabItems = function(findPathDiv) {
	var rgView = this.rgView;
	var showLink = findPathDiv.find('.show-find-path-advanced');
	var advancedOptionDiv = findPathDiv.find('.advanced_options_wrapper');	
	var min_length = advancedOptionDiv.find('#min_length'+rgView.vizuuid);
	min_length.bind('keyup', function(e){
		if (e.target.value!= null) {
			$("#find-path-button"+rgView.vizuuid).removeAttr("disabled");
			$('#lengthbelow').remove();
		if(parseInt(e.target.value)>50 || parseInt(e.target.value)<1){
			$('#find-path-advanced'+rgView.vizuuid).append('<p id="lengthbelow" class="alertBox">MinLength should be between 1 and 50</p>');
			$("#find-path-button"+rgView.vizuuid).attr("disabled", "disabled");
		}
		}
	});
	var max_length=advancedOptionDiv.find('#max_length'+rgView.vizuuid);
	max_length.bind('keyup', function(e){
		if (e.target.value!= null) {
			$("#find-path-button"+rgView.vizuuid).removeAttr("disabled");
			$('#lengthexceed').remove();
		if(parseInt(e.target.value)>50 || parseInt(e.target.value)<1){
			$('#find-path-advanced'+rgView.vizuuid).append('<p id="lengthexceed" class="alertBox">MaxLength should be between 1 and 50</p>');
			$("#find-path-button"+rgView.vizuuid).attr("disabled", "disabled");
		}
		}
	});
	showLink.click(function() {
		var advancedOptionDiv = findPathDiv.find('.advanced_options_wrapper');
		var showLink = this;
		advancedOptionDiv.slideToggle(function() {
			if(advancedOptionDiv.is(':visible')) {
				$(showLink).text('Hide Advanced Options');
				if(rgView.links[0]!=null){
				if(rgView.linkDefsMap[rgView.links[0].uuid].directionDef!=null){
			    	 $('#include-direction'+rgView.vizuuid).removeAttr("hidden");
			    	 $('#direction-label'+rgView.vizuuid).removeAttr("hidden");
			     }
				}
			}
			else {
				$(showLink).text('Show Advanced Options');			     
			}
		});
		return false;
	});
	
	findPathDiv.find('.find_path_lengths_checkbox').click(function() {
		var numberInput = $(this).parent().find('.number_input');
		if(this.checked) {
			numberInput.removeAttr('disabled');
		} else {
			numberInput.attr('disabled', '');
		}
	});
}
RGPanel.prototype.registerStartNodeChooseClick = function(findPathDiv) {
	var index = this.index;
	var rgPanel = this;
	var rgView	= rgPanel.rgView;

	findPathDiv.find('#start-node-choose-link' +rgView.vizuuid).click(function() {
		$("#"+rgView.vizuuid).css('cursor', 'url(../images/gun_cursor.png) 18 18,auto');
		$('#'+rgView.vizuuid).bind("click", function(event) {
			 var location = $('#'+rgView.vizuuid).offset();
		  	 var x = event.pageX - location.left;
		  	 var y = event.pageY - location.top;
		  	 var clickNodeRG = new ClickNodeRG(rgView.vizuuid,'startnode' +rgView.vizuuid,'startnodeid' +rgView.vizuuid);
		 	clickNodeRG.doTask(x,y);
		 	$(event.target).unbind("click");
		 	$(event.target).css('cursor', 'default');
		 });
		return false;
	});

	findPathDiv.find('#end-node-choose-link' +rgView.vizuuid).click(function() {
		$('#'+rgView.vizuuid).css('cursor', 'url(../images/gun_cursor.png) 18 18,auto');
		$('#'+rgView.vizuuid).bind("click", function(event) {
			 var location = $('#'+rgView.vizuuid).offset();
		  	 var x = event.pageX - location.left;
		  	 var y = event.pageY - location.top;
		  	 var clickNodeRG = new ClickNodeRG(rgView.vizuuid, 'endnode' +rgView.vizuuid,'endnodeid' +rgView.vizuuid);
		  	 clickNodeRG.doTask(x,y);
		  	$(event.target).unbind("click");
		 	$(event.target).css('cursor', 'default');
		 });
		return false;
	});
}
RGPanel.prototype.generateGraphSearchTab = function(tabid) {
	var graphSearchDiv = $('<div>').addClass('tab-pane row-fluid').attr('id', tabid);
	var indexView = {
		index: this.index
	}
	var html = Mustache.render($('#graph-search-tab-template').html(), indexView);
	graphSearchDiv.append(html);
	this.createGraphTreeView(graphSearchDiv,tabid);
	this.registerNodeLinkRadioBttnChangeListner(graphSearchDiv);
	this.registerSearchAction(graphSearchDiv);
	this.registerResetAction(graphSearchDiv);
	this.registerDropDownActions(graphSearchDiv);
	return graphSearchDiv;
}

RGPanel.prototype.registerNodeLinkRadioBttnChangeListner = function(graphSearchDiv){
	var searchDiv = graphSearchDiv;
	var rgPanel = this;
	graphSearchDiv.find('.firstOption'+ rgPanel.index).click(function(){
		var selectedFirstOption = $(this).val();
		rgPanel.searchInNodeOrLink = selectedFirstOption;
		rgPanel.createNodes(searchDiv, selectedFirstOption == "nodes" ? true : false);				
		if(selectedFirstOption == "nodes"){	
			$('#graph-search-result-find-data-table'+ rgPanel.rgView.vizuuid).parents().find('div.dataTables_scrollHeadInner thead tr:first').find('td.header1').html("Label");
			$('#graph-search-result-find-data-table'+ rgPanel.rgView.vizuuid).parents().find('div.dataTables_scrollHeadInner thead tr:first').find('td.header2').html("Type");
		}else{
			$('#graph-search-result-find-data-table'+ rgPanel.rgView.vizuuid).parents().find('div.dataTables_scrollHeadInner thead tr:first').find('td.header1').html("Source");
			$('#graph-search-result-find-data-table'+ rgPanel.rgView.vizuuid).parents().find('div.dataTables_scrollHeadInner thead tr:first').find('td.header2').html("Target");
		}			
		var dataTable = $('#graph-search-result-find-data-table' + rgPanel.rgView.vizuuid).dataTable();
		dataTable.fnClearTable();		
		rgPanel.toggleGraphSearchResultDropdownActions(rgPanel);
	});
	graphSearchDiv.find('.secondOption'+ rgPanel.index).click(function(){
		var secondOption = $(this).val();
		
		rgPanel.isSearchInGraph =  $(this).val() == "true" ? true : false;
		rgPanel.toggleGraphSearchResultDropdownActions(rgPanel);	
		
	});
}

RGPanel.prototype.generateGraphTimePlayerTab = function(tabid) {
	var graphTimePlayerDiv = $('<div>').addClass('tab-pane row-fluid').attr('id', tabid);
	var indexView = {
		vizuuid: this.index
	}
	var html = Mustache.render($('#graph-time-tab-template').html(), indexView);
	graphTimePlayerDiv.append(html);
	return graphTimePlayerDiv;
}

RGPanel.prototype.createGraphTreeView = function(graphSearchDiv, tabid) {
	this.createNodes(graphSearchDiv, true);	
}
RGPanel.prototype.editableDynaTreeRow = function(node) {
	var view = {
		index: this.index,
		columnTitle: node.data.title,
		uuid : node.data.key
	};
	return Mustache.render($('#dynatree-row-template').html(), view);
}
RGPanel.prototype.linkDynaTreeRow = function(node) {
	var view = {
		index: this.index,
		toLink: node.data.toLink,
		fromLink: node.data.title
	};
	return Mustache.render($('#dynatree-link-row-template').html(), view);
}

RGPanel.prototype.createBundleDialogue = function() {
	var view = {
		index: this.index,
		viz : this.rgView.vizuuid
	}
	var bundleDialogue = Mustache.render($('#bundle-dialogue-template').html(), view);
	
	$('body').append(bundleDialogue);
}

RGPanel.prototype.createUnbundleDialogue = function() {
	var view = {
		index: this.index,
		viz : this.rgView.vizuuid
	}
	var unBundleDialogue = Mustache.render($('#unbundle-dialogue-template').html(), view);
	
	$('body').append(unBundleDialogue);
}

RGPanel.prototype.createNodes = function(graphSearchDiv, isSearchInNodes){		
	
	if(isSearchInNodes){		
		this.initializeSearchTree(graphSearchDiv, this.getChildrenForNodes(), true);
	}else{
		this.initializeSearchTree(graphSearchDiv, this.getChildrenForLinks(), false);
	}
	
}

RGPanel.prototype.getChildrenForNodes = function(){	
	var childNodes = new Array();
	var rgPanel = this;
	var nodeNames;
	if(typeof(this.rgView.nodes[0]) == "object"){
		nodeNames = [];
		$.each(this.rgView.nodes,function(key,value){
			nodeNames.push(value['name']);
		});		
	}
	else{
		var nodeNames = this.rgView.nodes;
	}
	$.each(nodeNames,function(index,node){		
		childNodes.push(rgPanel.getChildrenForNode(node));			
	});
	
	return childNodes;
	
}

RGPanel.prototype.getChildrenForNode = function(nodeName){
	var rgpanel = this;
	var children = [
                    {title: "Type", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Label", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Count", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Number of Neighbors", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Betweenness", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Closeness", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                    {title: "Eigenvector", hideCheckbox: true, editable: true, key : utils.guidGenerator()},
                   ];
	
	var computedFields =  _.filter(rgpanel.rgView.nodeDefsMap[nodeName].attributeDefs , function(attributeDef){ return  attributeDef.kind == "COMPUTED"});
	$.each(computedFields, function (index, computedField) {
		children.push({title : computedField.name, hideCheckbox: true, editable: true, key : utils.guidGenerator(), attrDef : computedField});
	});
	var tooltipFields = _.filter(rgpanel.rgView.nodeDefsMap[nodeName].attributeDefs , function(attributeDef){ return  attributeDef.kind == null && (attributeDef.name.match("^csi.internal")) == null});
	$.each(tooltipFields, function (index, tooltipField) {
		children.push({title : tooltipField.name, hideCheckbox: true, editable: true, key : utils.guidGenerator(), attrDef : tooltipField});
	});
	var node = new Object();
	node['title'] = nodeName;
	node['children'] = children;
	return node;
	
}

RGPanel.prototype.getChildrenForLinks = function(){		
	var childNodes = new Array();
	var rgPanel = this;
	$.each(this.rgView.links,function(index,link){
		var child = new Object();
		child['title'] = link['node1'];
		child['toLink'] = link['node2'];
		child['isLink'] = true;
		child['key'] = link['uuid'];
		child['children'] = rgPanel.getChildrenForLink(link['node1'], link['node2']);
		childNodes.push(child);			
	});			
	
	return childNodes;
	
}

RGPanel.prototype.getChildrenForLink = function(nodeName1, nodeName2){
	var linkAttributes = [
							{title: "Type", hideCheckbox: true, editable: true},
							{title: "Label", hideCheckbox: true, editable: true},
							{title: "Count", hideCheckbox: true, editable: true}
		                ];
	var currentLink = _.filter(this.rgView.links, function (link) { return link.node1 == nodeName1 && link.node2 == nodeName2});
	var computedFields =  _.filter(this.rgView.linkDefsMap[currentLink[0].uuid].attributeDefs , function(attributeDef){ return  attributeDef.kind == "COMPUTED"});
	$.each(computedFields, function (index, computedField) {
		linkAttributes.push( {title : computedField.name, hideCheckbox: true, editable: true, attrDef : computedField});
	});
	var tooltipFields = _.filter(this.rgView.linkDefsMap[currentLink[0].uuid].attributeDefs , function(attributeDef){ return  attributeDef.kind == null && (attributeDef.name.match("^csi.internal")) == null});
	$.each(tooltipFields, function (index, tooltipField) {
		linkAttributes.push({title : tooltipField.name, hideCheckbox: true, editable: true, key : utils.guidGenerator(), attrDef : tooltipField});
	});
	var nodesList = [];
	$.merge(nodesList,linkAttributes);		
	nodesList.push(this.getChildrenForNode(nodeName1));
	nodesList.push(this.getChildrenForNode(nodeName2));
	return nodesList;
}

RGPanel.prototype.initializeSearchTree = function(graphSearchDiv,childNodes, isNode){
	var treeData = [{title : "Select All",key : "selectAll",expand: true,children: childNodes}];
	 var rgpanel = this;
	 if(isNode){
		 graphSearchDiv.find(".search-graph-tree").dynatree({
	         checkbox: true,
	         selectMode: 3,
	         children: treeData,
	         onCustomRender: function(node) {
	             if(node.data.editable) {
	             	 
	                 return rgpanel.editableDynaTreeRow(node);
	             }
	         },
	         onCreate: function(node, nodeSpan) {
	         	if(node.data.editable) {
	         		rgpanel.registerGraphNodeEdit(node,nodeSpan);
			    }
			 }
	     });
	 }else{
		 graphSearchDiv.find(".search-graph-tree").dynatree({
	         checkbox: true,
	         selectMode: 3,
	         children: treeData,
	         onCustomRender: function(node) {
	             if(node.hasChildren() && node.data.key != "selectAll" && node.data.toLink != undefined) {
	                 return rgpanel.linkDynaTreeRow(node);
	             }else if(!node.hasChildren() && node.data.key != "selectAll"){
	            	 return rgpanel.editableDynaTreeRow(node);
	             }
	         },
	     });
	 }
	 graphSearchDiv.find(".search-graph-tree").dynatree("getTree").reload();
     
}
RGPanel.prototype.registerSearchAction = function(graphSearchDiv){
	var rgView = this.rgView;
	var rgPanel = this;	
	graphSearchDiv.find('#search' + rgView.vizuuid).click(function(){		
		var searchGraph = new GraphSearch(rgView.vizuuid, rgPanel.searchInNodeOrLink.toUpperCase(), rgPanel.isSearchInGraph, rgView.nodeDefsMap, rgView.linkDefsMap);
		searchGraph.doTask();
		rgPanel.toggleGraphSearchResultDropdownActions(rgPanel);
	});
}
RGPanel.prototype.registerResetAction = function(graphSearchDiv){
	var rgView = this.rgView;
	var rgPanel = this;
	graphSearchDiv.find('#reset' + rgView.vizuuid).click(function(){
		var selectedNodes = $('#graph-search-tab'+ rgPanel.index ).find('.search-graph-tree').dynatree("getSelectedNodes");		
		$.each(selectedNodes, function(index, node){
			node['hideCheckbox'] = true;
		});
		$('#graph-search-tab'+ rgPanel.index ).find('.search-graph-tree').dynatree("getTree").reload();
		var searchResultDataTable = $('#graph-search-result-find-data-table' + rgView.vizuuid).dataTable();
		searchResultDataTable.fnClearTable();
		
		if(rgPanel.isSearchInGraph){
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
			$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
			$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
		}else{
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
			$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
			$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
		}
	});
}

RGPanel.prototype.createAppearanceEditorDialogue = function() {
	var selectItems = [];
	selectItems.push({'text': '', 'value': ''})
	for(i=1;i<11;i++) {
		selectItems.push({'text': i+ "x", 'value': i+"x"})
	};
	var view = {
		'index': this.index,
		'selectItems': selectItems,
		'viz' : this.rgView.vizuuid
	}
	var appEditorDialogue = Mustache.render($('#app-editor-dialogue-template').html(), view);
	$('body').append(appEditorDialogue);
	this.registerColorPicker(appEditorDialogue);
}
RGPanel.prototype.createRevealNeighborsDialogue = function() {
	var selectItems = [];
	for(i=1;i<6;i++) {
		selectItems.push({'text': i, 'value': i})
	};
	var view = {
		'index': this.index,
		'selectItems': selectItems,
		'viz' : this.rgView.vizuuid
	}
	var revealNeighborDialogue = Mustache.render($('#reveal-neighbor-dialogue-template').html(), view);
	$('body').append(revealNeighborDialogue);
}
RGPanel.prototype.createSelectNeighborsDialogue = function() {
	var selectItems = [];
	for(i=1;i<6;i++) {
		selectItems.push({'text': i, 'value': i})
	};
	var view = {
		'index': this.index,
		'selectItems': selectItems,
		'viz' : this.rgView.vizuuid
	}
	var revealNeighborDialogue = Mustache.render($('#select-neighbor-dialogue-template').html(), view);
	$('body').append(revealNeighborDialogue);
}
RGPanel.prototype.registerColorPicker = function(appEditorDialogue) {
	var rgPanel = this;
	$('#colorpickerHolder'+rgPanel.index).ColorPicker({
		flat: true,
		color: rgPanel.default_color,
		onSubmit: function(hsb, hex, rgb) {
			$('#appearance-color-int'+rgPanel.index).val(parseInt(hex,16));
			$('#appearance-editor-dialogue'+rgPanel.index+' .modal-body').stop().animate({height: rgPanel.isColorpickerExpanded ? 143 : 275}, 500);
			$('#colorSelector'+rgPanel.index+' div').css('backgroundColor', '#' + hex);
			$('#colorpickerHolder'+rgPanel.index).stop().animate({height: rgPanel.isColorpickerExpanded ? 0 : 173}, 500);
			rgPanel.isColorpickerExpanded = !(rgPanel.isColorpickerExpanded);
		}
	});
	$('#colorpickerHolder'+rgPanel.index+'>div').css('position', 'relative');
	
	$('#colorSelector'+rgPanel.index).bind('click', function() {
		$('#appearance-editor-dialogue'+rgPanel.index+' .modal-body').stop().animate({height: rgPanel.isColorpickerExpanded ? 143 : 275}, 500);
		$('#colorpickerHolder'+rgPanel.index).stop().animate({height: rgPanel.isColorpickerExpanded ? 0 : 173}, 500);
		rgPanel.isColorpickerExpanded = !(rgPanel.isColorpickerExpanded);
	});
}	
RGPanel.prototype.registerDropDownActions = function(graphSearchDiv){	
	var rgView = this.rgView;
	var rgPanel = this;
	graphSearchDiv.find("#searchGraphSelectAll"+ rgView.vizuuid).click(function(){
		var allLinks;
		var allNodes;
		if(rgPanel.searchInNodeOrLink == "nodes"){
			allLinks = false;
			allNodes = true;
		}else{
			allLinks = true;
			allNodes = false;
		}
		var select = new Select(rgView.vizuuid,allLinks,allNodes,null,null);
		select.doTask();
	});
	graphSearchDiv.find("#searchGraphSelectChoosen"+ rgView.vizuuid).click(function(){		
		var oTT = TableTools.fnGetInstance('graph-search-result-find-data-table'+ rgView.vizuuid );
		var aData = oTT.fnGetSelectedData();
		var idList = new Array();
		$.each(aData, function(index, rowData){
			idList.push(rowData['id']);
		});
		var select = new Select(rgView.vizuuid,false,false,idList,rgPanel.searchInNodeOrLink);
		select.doTask();
	});
	graphSearchDiv.find("#searchGraphAddAll"+ rgView.vizuuid).click(function(){	
		var graphAddAll = new GraphSearchAddAll(rgView.vizuuid,rgPanel.searchInNodeOrLink);
		graphAddAll.doTask();
	});
	graphSearchDiv.find("#searchGraphAddChoosen"+ rgView.vizuuid).click(function(){
		var oTT = TableTools.fnGetInstance('graph-search-result-find-data-table'+ rgView.vizuuid );
		var aData = oTT.fnGetSelectedData();
		var idList = new Array();
		$.each(aData, function(index, rowData){
			idList.push(rowData['id']);
		});
		var graphAddChoosen = new GraphSearchAddChosen(rgView.vizuuid,rgPanel.searchInNodeOrLink,idList);
		graphAddChoosen.doTask();
	});	

}

RGPanel.prototype.toggleGraphSearchResultDropdownActions = function(rgPanel){	
	
	if(rgPanel.isSearchInGraph){
		$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
		$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
		$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
		$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');			
	}else{
		$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
		$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).parent().attr('style','display:none;');
		$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).parent().removeAttr('style');
		$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).parent().removeAttr('style');			
	}
	var dataTable = $('#graph-search-result-find-data-table' + rgPanel.rgView.vizuuid).dataTable();
	if(rgPanel.isSearchInGraph){
		if(dataTable.fnGetData().length > 0){
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).removeClass('disabled-link');
		}else{
			$('#searchGraphSelectAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphSelectChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
		}
	}else{
		if(dataTable.fnGetData().length > 0){
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).removeClass('disabled-link');
		}else{
			$('#searchGraphAddAll'+rgPanel.rgView.vizuuid).addClass('disabled-link');
			$('#searchGraphAddChoosen'+rgPanel.rgView.vizuuid).addClass('disabled-link');
		}
	}
}
RGPanel.prototype.toggleLegendPanel = function(){
	if($("#legendPanel" + this.index).css('visibility') == 'hidden') {
		this.showLegendPanel();
	}
	else {
		this.hideLegendPanel();
	}
}
RGPanel.prototype.showLegendPanel = function(){
	$("#legendPanel" + this.index).css('visibility', 'visible');
	$('#hidelegend' + this.index).text('Hide Legend');
	$('#resetlegend' + this.index).removeClass('disabled-link');
}
RGPanel.prototype.hideLegendPanel = function(){
	$("#legendPanel" + this.index).css('visibility', 'hidden');
	$('#hidelegend' + this.index).text('Show Legend');
	$('#resetlegend' + this.index).addClass('disabled-link');
}
RGPanel.prototype.resetLegendPanel = function(){
	$("#legendPanel" + this.index).css({
		left: '', 
		top: '',
		width: '',
		height: ''
	});
}
RGPanel.prototype.registerCollapseForRelTabs = function() {
	var rgPanel = this;
	$('#rel-tab-content'+rgPanel.index).on('show', function() {
		$('#rel-tab-collapse-link'+rgPanel.index).removeClass('toggle_up').addClass('toggle');
		console.log("toggle");
	});
	$('#rel-tab-content'+rgPanel.index).on('hide', function() {
		$('#rel-tab-collapse-link'+rgPanel.index).removeClass('toggle').addClass('toggle_up');
		console.log("toggle up");
	});
	$('#rel-tab-content'+rgPanel.index).on('shown', function() {
		RGPanel.redrawVisibleTables();
		console.log("fn redrawvisible...");
	});
	$('#rel-tab-content'+rgPanel.index).parent().find('h2.box_head').dblclick(function(){
        $('#rel-tab-collapse-link'+rgPanel.index).click();
        console.log("click");
    });
    $('#rel-tab-content'+rgPanel.index).parent().find('.nodes-list-tab, .find-list-tab, .graph-search-tab, .links-list-tab, .graph-time-tab').on('click', function(){
        if($('#rel-tab-content'+rgPanel.index).css('height') == '0px') {
        	$('#rel-tab-collapse-link'+rgPanel.index).click();
        	console.log("click withh css check");
        }
    });
}
RGPanel.prototype.registerGraphNodeEdit = function(node, nodeSpan) {
	var rgpanel = this;
	$(nodeSpan).find('.tree-column-edit').click(function() {
		var nodeUUId = $(this).find('img').attr('uuid');
		var attrNode = $('#graph-search-tab' + rgpanel.rgView.vizuuid).find('.search-graph-tree').dynatree("getTree").getNodeByKey(nodeUUId);
		var attributeEditDialog;
		if (attrNode != undefined && attrNode.data.criteria != undefined) {
			attributeEditDialog = $('#attribute-edit-dialog' + nodeUUId);
			attributeEditDialog.modal();
			return;
		}
		var title;
		if (node.getParent().data.isLink != undefined && node.getParent().data.isLink) {
			title = node.getParent().data.title + "-" + node.getParent().data.toLink;
		} else {
			title = node.getParent().data.title;
		}
		var nodeView = {
			graphSearchName: $(nodeSpan).find('.tree-title .dynatree-title').text(),
			nodeName : title,
			attrName : node.data.title
		}
		var html = Mustache.render($('#graph-search-attribute-edit-popup-template').html(), nodeView);
		attributeEditDialog = $(html).clone();		
		attributeEditDialog.attr('id', 'attribute-edit-dialog' + nodeUUId ).attr('uuid', nodeUUId).attr('name', nodeView.graphSearchName);
		attributeEditDialog.find('#treeFinish').click(function() {
			var rootNode = $('#graph-search-tab' + rgpanel.rgView.vizuuid).find('.search-graph-tree').dynatree("getTree");
			var attributeNode = rootNode.getNodeByKey(nodeUUId);
			var attributeCriteria = rgpanel.getAttributeCriteria(attributeEditDialog, attributeNode);			
			attributeNode.data.criteria = attributeCriteria;
			if (attributeCriteria.length > 0) {
				$('img[uuid="' + nodeUUId +'"]').addClass('highlighted');
			} else {
				$('img[uuid="' + nodeUUId +'"]').removeClass('highlighted');
			}			
			attributeEditDialog.modal('hide');
		});
		attributeEditDialog.find('#add-criteria').click(function() {			
			var criterionTemplateHtml = Mustache.render($('#graph-search-criterion-template').html());
			var $criteriaHTML = $(criterionTemplateHtml);
			$criteriaHTML.find('#delete').click(function() {
				$(this).parent().parent().parent().remove();
			});
			$criteriaHTML.find('#add-value').click(function() {
				var listitemHTML = Mustache.render($('#graph-search-value-list-item').html());
				listitemHTML = $(listitemHTML);
				listitemHTML.find('#delete-val').click(function() {
					$(this).parent().parent().remove();
				});
				$criteriaHTML.find('#add-value').before(listitemHTML);
			});
			attributeEditDialog.find('.attr-criterion').append($criteriaHTML);
			attributeEditDialog.find('.graph-search-criterion:last').ScrollTo();
		});		
		attributeEditDialog.modal();		
	});	
}

RGPanel.prototype.getAttributeCriteria = function(attributeEditDialog, attributeNode) {
	var rgpanel = this;
	var criteriaDiv = attributeEditDialog.find('.attr-criterion');
	var criteriaArr = [];
	var attributeName = attributeEditDialog.attr('name');
	criteriaDiv.children().each(function () {
		var criterion = $(this);
		var attrCriterion = {};
		attrCriterion.class = "csi.server.common.dto.graph.search.AttributeCriterion";
		attrCriterion.exclude = criterion.find('#exclude').is(":checked");
		attrCriterion.operator = criterion.find('#operator').val();
		var staticValArr = [];
		criterion.find('#value span').children().each(function () {
			var item = $(this);
			if (item.find('input[type="text"]').length != 0) {
				staticValArr.push(item.find('input').val());
			}			
		});
		attrCriterion.staticValues = staticValArr;
		attrCriterion.attribute = rgpanel.getAttribute(attributeName, attributeNode);
		criteriaArr.push(attrCriterion);
	});
	return criteriaArr;
}

RGPanel.prototype.getAttribute = function (attrName, attributeNode) {
	var attribute = {};
	var attrDef = attributeNode.data.attrDef;
	if (attrDef != undefined) {
		attribute.aggregateFunction = attrDef.aggregateFunction;
		attribute.bySize = attrDef.bySize;
		attribute.byStatic = attrDef.byStatic;
		attribute.byTransparency = attrDef.byTransparency;
		attribute.clientProperties = attrDef.clientProperties;
		attribute.fieldDef = attrDef.fieldDef;
		attribute.hideEmptyInTooltip = attrDef.hideEmptyInTooltip;
		attribute.includeInTooltip = attrDef.includeInTooltip;
		attribute.kind = attrDef.kind;
		attribute.name = attrDef.name;		
	} else {
		attribute.bySize = false;
		attribute.byStatic = false;
		attribute.byTransparency = false;
		attribute['class'] = "csi.server.common.model.attribute.AttributeDef";
		attribute.clientProperties = {};
		attribute.defaultIncludeInTooltip = true;
		attribute.hideEmptyInTooltip = false;
		attribute.includeInTooltip = true;
		attribute.name = RGPanel.fixedNodeLinkAttributeServerParamMap[attrName];
		attribute.includeInTooltip = true;
	}	
	attribute.uuid = utils.guidGenerator();
	return attribute;	
	
}
