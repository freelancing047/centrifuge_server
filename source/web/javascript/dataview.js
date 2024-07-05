function DataView() {
	this.myData = null;
	this.themeXML = null;
	this.currentTheme = "Circular";
}
DataView.prototype.doLoad = function() {
	var load = new Load();
	var chartUtils  = new ChartUtils();
	load.registerLogout();	
	canvasImageContainer = {};
	var createRG = new CreateRG();
	$('#treeFinish-rg').click(function(){
		$('#treeFinish-rg').attr("disabled","disabled");
		var vizDef = $('#treeFinish-rg').data('vizDef');
		if (vizDef == undefined || vizDef.isNew) {
			createRG.doTask();
		} else {
			createRG.saveRelationShipGraph(vizDef.uuid, vizDef);
		}
		$('#treeFinish-rg').removeData('vizDef');
	});
    this.registerDeleteWorksheet();
    this.registerRenameWorksheet();
    this.registerDeleteVisualization();
	var dataview = this;
	var id = this.getUrlVars()["uuid"];
	var url = '/Centrifuge/actions/dataview/openDataView?uuid='+ id +'&_f=json';	
	$.get(url,function(data){
		var dv = $.data(document.body, 'dataview');
		dv.myData = data;
		$('#dvName').text(dv.myData.resultData.name)
		$('#dvName').append('<b class="caret">'); // To display dataview name on the toolbar
		dv.populateDrillChartInput();
		dv.registerColorPicker();
		var themeURL = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/Circular.xml?asXML";
		$.ajax({
			url: themeURL,
			async : false,
			complete: function(data) {			
				dataview.themeXML = data.responseText;	
	                       
			}
		});
		$('#theme option[value="Circular"]').attr('selected', 'selected');
		$('#theme').unbind('change').change(function(){
			dataview.currentTheme = $(this).val();
			dataview.setTheme($(this).val());		
		});
        var worksheet1 = dv.myData.resultData.meta.modelDef.worksheets[0];
		var newLayoutActions = new NewLayoutActions(0);
		newLayoutActions.doTask();
        var worksheetName = (worksheet1.worksheetName=="Untitled Worksheet" ? "Untitled Worksheet" : worksheet1.worksheetName);
		newLayoutActions.addNewWorksheetHTML(worksheetName, "LEFT HAND LAYOUT", dv.myData.resultData.meta.modelDef.worksheets[0].uuid);
		$(".layouts-tabs a[href='#layout0']").tab('show');
		$('#flex-editor').click(function(){
			dv.openFlexEditor();
		});

		//if(window.dataview.myData.resultData.meta.modelDef.visualizations.length > 0) {				    
			    var worksheets = window.dataview.myData.resultData.meta.modelDef.worksheets;
			    var vizIndex = 0;
			    $.each(worksheets, function(wkstIndex, worksheet) {
			    	if(wkstIndex != 0) {
						var newLayoutActions = new NewLayoutActions(wkstIndex);
						newLayoutActions.doTask();
						var layout = (worksheet.layout || "LEFT HAND LAYOUT"); 
						newLayoutActions.addNewWorksheetHTML(worksheet.worksheetName, layout, worksheet.uuid);
					}
			    	var relImagesObj = new Object();
			    	relImagesObj["relImages"] = [];
			    	var chartObjs = [];
			    	$.each(worksheet.visualizations, function(index, viz) {
			    		if (viz["class"] == "csi.server.common.model.visualization.RelGraphViewDef") {
				    		var relImage = new Object();
				    		relImage.name = viz.name;
				    		relImage.position = (viz.position == undefined ? 0 : viz.position);
				    		relImage.imageSrc = '';
				    		relImage.viz = viz.uuid;
				    		relImage.vizIndex = vizIndex;
				    		relImage.ws = worksheet;
				    		relImage.wsIndex = wkstIndex;
				    		relImage.nodes = utils.getNodesList(viz.nodeDefs);
				    		relImage.links = utils.getLinksList(viz.linkDefs);
				    		relImage.nodeDefsMap = utils.getNodeDefsMap(viz.nodeDefs);
				    		relImage.linkDefsMap = utils.getLinkDefsMap(viz.linkDefs);
				    		relImagesObj.relImages.push(relImage);
				    		vizIndex += 1;
				    	}else if (viz["class"] == "csi.server.common.model.DrillDownChartViewDef") {
				    		var chartObj = new Object();
				    		chartObj.name = viz.name;
				    		chartObj.position = (viz.position == undefined ? 0 : viz.position);
				    		chartObj.viz = viz.uuid;
				    		chartObj.vizIndex = vizIndex;
				    		chartObj.ws = worksheet;
				    		chartObj.wsIndex = wkstIndex;
				    		chartObj.chartFunction = chartUtils.getChartFunction(viz.metrics);
				    		chartObj.chartType = viz.chartType;
				    		chartObj.dimensions = chartUtils.getDimensions(viz.dimensions);
				    		chartObj.cell = chartUtils.getMetrics(viz.metrics);
				    		chartObjs.push(chartObj);
				    		vizIndex += 1;
				    		
				    	}
			    	});
			    	if(relImagesObj.relImages.length > 0) {
						$.each(relImagesObj.relImages, function(index, relImage) {
							utils.drawLayout(relImage, false, true);	
						});
					}
					if(chartObjs.length > 0) {
						$.each(chartObjs, function(index, chartObj) {
							chartUtils.recreateChart(chartObj);	
						});
					}  
			    });
            //}
	});
}

DataView.prototype.getUrlVars = function () {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}
DataView.prototype.getRelationshipGraph = function() {
	var id = this.getUrlVars()["uuid"];
	var url = '/Centrifuge/actions/dataview/openDataView?uuid='+ id +'&_f=json'
	$.get(url,function(data){
		$.each(data['resultData']['meta']['modelDef']['visualizations'], function(index, value) { 
			 if (value['class'] == 'csi.server.common.model.visualization.RelGraphViewDef') {
				 $("#container-relationgraph").append('<li><b>'+ value['name']+'</b></li>');
			 }
		});
	});
}
/*
DataView.prototype.populateDrillChartInput = function() {
	if (this.myData == null){
		return;
	}
	var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))});
	$(cols).each(function(index, value) {
		columnNames[index] = {
						        val: index,
						        text: value.columnName
						    };
	});
};*/

DataView.prototype.populateDrillChartInput = function() {
	if (this.myData == null){
		return;
	}
	if (this.myData.resultData.meta.dataSetOps.length > 0) {
		var iCol = 0;
		$.each(this.myData.resultData.meta.dataSetOps[0].tableDef.columns, function(index, value) {
			columnNames[iCol] = {
							        val: value.columnName,
							        text: value.columnName
							    };
							    iCol++;
		});
	}
	
};

DataView.prototype.registerColorPicker=function() {
	var default_color = '#fefefe';
	var widt = false;
	$('#colorpickerHolder2').ColorPicker({
		flat: true,
		color: default_color,
		onSubmit: function(hsb, hex, rgb) {
			$('#colorSelector2 div').css('backgroundColor', '#' + hex);
			$('.node-diagram').css('backgroundColor', '#' + hex);
			$('#colorpickerHolder2').stop().animate({height: widt ? 0 : 173}, 500);
			widt = !widt;
		}
	});
	$('#colorpickerHolder2>div').css('position', 'absolute');
	$('#colorSelector2').bind('click', function() {
		$('#colorpickerHolder2').stop().animate({height: widt ? 0 : 173}, 500);
		widt = !widt;
	});
}
DataView.prototype.generateNodeExceedInfoPanel = function(relImage, nodeLimit, thresholdLimit) {
	var graphInfoHTML = $(Mustache.render($('#graph-max-limit-info-panel').html(), {'nodes-number': nodeLimit, 'threshold-number': thresholdLimit}));	
	var element = $('div#layout'+relImage.wsIndex+'_panel'+relImage.position).find("#relation-graph");
	$(element).parent().append(graphInfoHTML);
	$(element).hide();
	$('li#graph-search-tab-header' + relImage.vizIndex + ' > a').click();
}

DataView.prototype.generateLoadPanel = function(relImage, worksheetIndex) {
	var button = $('<button>').text("Load").addClass("btn btn-large btn-primary addVisualization");
	$.data(button[0], 'relImage', relImage);
	$(button).on('click', function () {
		var relImage = $.data(this, 'relImage');
		utils.drawLayout(relImage, true, true);
	});
	$('div#layout'+worksheetIndex+'_panel'+relImage.position).html('');
	$('div#layout'+worksheetIndex+'_panel'+relImage.position).append(button);
}

DataView.prototype.generateRelPanel = function(rgPanel, relImage, rgv) {
	var relPanel = rgPanel.generateRelPanel(relImage, relImage.vizIndex);
	$('div#layout'+relImage.wsIndex+'_panel'+relImage.position).html('');
	$('div#layout'+relImage.wsIndex+'_panel'+relImage.position).append(relPanel);
	
	$("#myContainer"+relImage.vizIndex).append($("#progress_bar_template").html());
	$('#myContainer' + relImage.vizIndex + ' > canvas').load(function() {
		$("#myContainer" + relImage.vizIndex).find(".progress_loader").hide();
	});
	rgv.doLoad(relImage);

	//Appending distinct context menu for canvas elements starts here.
	var contextHtml = Mustache.render($("#context-menu-template").html(), {vizuuid: rgv.vizuuid});
   	$('body').append(contextHtml);
    var contextHtmlNode = Mustache.render($("#context-menu-template-Node").html(), {vizuuid: rgv.vizuuid});
    $('body').append(contextHtmlNode);
    var contextHtmlLink = Mustache.render($("#context-menu-template-Link").html(), {vizuuid: rgv.vizuuid});
    $('body').append(contextHtmlLink);
   	//Appending distinct context menu for canvas elements ends here.
	
	var zoom = new Zoom(rgv.vizuuid);
	zoom.doRegister();
	//rgPanel.registerDraggable();
	rgPanel.createBundleDialogue();
	rgPanel.createUnbundleDialogue();
	rgPanel.createAppearanceEditorDialogue();
	rgPanel.createRevealNeighborsDialogue();
	rgPanel.createSelectNeighborsDialogue();
	rgPanel.bindToolbarIconClick();
	rgPanel.bindToolPanelIconClick();
	rgPanel.registerCollapseForRelTabs();
	rgPanel.redrawDataTables();
	rgPanel.registerSearchForDatatables();
	rgPanel.registerSelectedClickForDatatables();
	
	//Binding context menu to canvas starts
	$("#" + rgv.vizuuid).bind('contextmenu', function(e) {
		e.preventDefault();
		$.contextMenu( 'destroy');
		//decide which context menu to be shown over the canvas element.
		new DecideContextMenu(rgv.vizuuid).doTask(e);
	});
	//Binding context menu to canvas ends
	
	
   	$('div#layout'+relImage.wsIndex+'_panel'+relImage.position + ' #box_0').resizable({ handles: 'n' ,
            start: function(e, ui) {
                 $("#rel-tab-content" + relImage.vizIndex).css('height','auto');
                 $("#rel-tab-content" + relImage.vizIndex).collapse('show');
            },
            stop: function(e, ui) {
                $('div#layout'+relImage.wsIndex+'_panel'+relImage.position + ' #box_0').css('height','auto');
            }, 
            resize: function(event, ui) {
            $('div#layout'+relImage.wsIndex+'_panel'+relImage.position + ' #box_0 .dataTables_scrollBody').height($(this).height()-180);
            }
   	});
   	$('div#layout'+relImage.wsIndex+'_panel'+ relImage.position + ' #box_0').on('hide', function() {
            $('div#layout'+relImage.wsIndex+'_panel'+relImage.position + ' #box_0').css('top','');
            $('div#layout'+relImage.wsIndex+'_panel'+relImage.position + ' #box_0').css('bottom','0');
    });
    $(".graphLegend").resizable({
    	containment: "parent",
    	minHeight: 135
    });    
    $( ".graphLegend").on( "resize", function( event, ui ) {
    	var legendHeight = $(this).height();
    	$(this).find('.legendBody').height(legendHeight - 50);
    } );

}
DataView.prototype.generateRelPanelSmall = function(relImage) {
	var dataview = this;
	var w = $('#left_content .vizPanels:visible').width();
	var h = 270;
	var headerView = {
		panelName: relImage.name,
		vizId: relImage.viz,
		imgSrc: relImage.imageSrc,
		vizIndex: relImage.vizIndex,
		worksheetIndex: relImage.wsIndex,
		vizPosition: relImage.position
	};
	var content = Mustache.render($('#small-panel-template').html(), headerView);
	var newPanelId = 'layout' + relImage.wsIndex + '_panel' + relImage.position;
	if($('#' + newPanelId).length == 0) {
		var newColDiv = $('<div>').addClass('span12 column ui-sortable').attr('id', 'col' + relImage.position);
		var newPanelDiv = $('<div>').addClass('box grad_colour_dark_blue chart_table vizPanels')
		newPanelDiv.attr('id', newPanelId).attr('data-position', relImage.position);
		newColDiv.append(newPanelDiv);
		$('#layout' + relImage.wsIndex + ' #left_content > div.span12').append(newColDiv);
	}
	$("#" + newPanelId).html('');
	$("#" + newPanelId).append(content);
	$("#" + newPanelId + " .toggle_container").append($("#progress_bar_template").html());
	
	// $('#' + newPanelId).ScrollTo();
	var doGetImage = function() {
		new RefreshImage(relImage.viz).doTask();
		$('#collapseDiv'+relImage.vizIndex).on('show', function() {
			$('#collapseLink'+relImage.vizIndex).removeClass('toggle_closed').addClass('toggle');
		});
		$('#collapseDiv'+relImage.vizIndex).on('hide', function() {
			$('#collapseLink'+relImage.vizIndex).removeClass('toggle').addClass('toggle_closed');
		});
	    $('#' + newPanelId + ' .box_head').dblclick(function(){
	        $('#collapseLink'+relImage.vizIndex).click();
	    });
	    $('#' + newPanelId + ' .switch_main').click(function(){
	        dataview.switchWithMain(relImage);
	        $(this).css("display", "none")
	        return false;
	    });
	    dataview.registerSortablePanels(relImage.wsIndex, relImage.ws.uuid);
	};
	var doFitToSize = function() {
		csi.relgraph.fitToSize(relImage.viz, {
			onsuccess: function(data) {
				doGetImage();
			}			
		});
	};
	csi.relgraph.getDisplay(dataview.myData.resultData.uuid, relImage.viz, w, h, {
		onsuccess: function(data) {
			doFitToSize();
		}			
	});
	dataview.registerSmallPanelActions(relImage.viz);
	dataview.createBundleDialogueInSmallPanel(relImage.viz);
	dataview.createUnbundleDialogueInSmallPanel(relImage.viz);
	dataview.createSelectNeighborsDialogueInSmallPanel(relImage.viz);
	dataview.createRevealNeighborsDialogueInSmallPanel(relImage.viz);
}
DataView.prototype.registerSmallPanelActions = function(vizuuid) {
	$('#'+vizuuid)[0]._mouseinfo = {};
	$('#'+vizuuid).parent().append($('<div>').addClass('graphTooltip').attr('id','tooltipPanel' +vizuuid));
	var mouseMoveTask = new MouseMoveRG(vizuuid);
	var mm = function() {
		return function(event) {
			mouseMoveTask.doTask(event);
		};
	};
	$('#'+vizuuid).mousemove(mm());
	var selecttool = new SelectTool(vizuuid); 
	selecttool.doTask();
}
DataView.prototype.createBundleDialogueInSmallPanel = function(vizuuid) {
	var view = {
		index : vizuuid
	}
	var bundleDialogue = Mustache.render($('#small-panel-bundle-dialogue-template').html(), view);
	
	$('body').append(bundleDialogue);
}
DataView.prototype.createUnbundleDialogueInSmallPanel = function(vizuuid) {
	var view = {
		index: vizuuid,		
	}
	var unBundleDialogue = Mustache.render($('#small-panel-unbundle-dialogue-template').html(), view);
	
	$('body').append(unBundleDialogue);
}
DataView.prototype.createSelectNeighborsDialogueInSmallPanel = function(vizuuid) {
	var selectItems = [];
	for(i=1;i<6;i++) {
		selectItems.push({'text': i, 'value': i})
	};
	var view = {
		'index': vizuuid,
		'selectItems': selectItems,
	}
	var selectNeighborDialogue = Mustache.render($('#small-panel-select-neighbor-dialogue-template').html(), view);
	$('body').append(selectNeighborDialogue);
}
DataView.prototype.createRevealNeighborsDialogueInSmallPanel = function(vizuuid) {
	var selectItems = [];
	for(i=1;i<6;i++) {
		selectItems.push({'text': i, 'value': i})
	};
	var view = {
			'index': vizuuid,
			'selectItems': selectItems,
	}
	var revealNeighborDialogue = Mustache.render($('#small-panel-reveal-neighbor-dialogue-template').html(), view);
	$('body').append(revealNeighborDialogue);
}
DataView.prototype.openFlexEditor = function() {
	var url = "/Centrifuge/flexpoc/editor.jsp?dvuuid=" + this.getUrlVars()["uuid"];
	$('#flexIframe').attr('src', url);
	$('#flexModal').modal();
}
DataView.prototype.setTheme = function(themeName){
	var dataView = this;
	var url = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/"+themeName+".xml?asXML";		
	$.ajax({
		url: url,
		async : false,
		complete: function(data) {			
			dataView.themeXML = data.responseText;
			dataView.currentTheme = themeName;
			dataView.updateNodeImages();          
		}
	});
}
DataView.prototype.updateNodeImages=function() {
	var dataView = this;
	var nodes = $('#node_diagram').find(".operation");	
	$.each(nodes, function(index, node) {
		var nodeName = $(node).find('.node-label').text();		
		var nodeUiProps = utils.getNodeUiProps(nodeName);
		$(node).css('background-image', 'url("' + nodeUiProps.nodeImageUrl + '")');	
		$(node).attr('shape', nodeUiProps.shape);
		$(node).attr('color', nodeUiProps.color);
		$(node).attr('image', nodeUiProps.image);
		var vizDef = $.data($('#treeFinish-rg')[0], 'vizDef');
		var nodeDefIdx = utils.getNodeDefIdx(vizDef.nodeDefs, nodeName);
		var attributeDefsArr = vizDef.nodeDefs[nodeDefIdx].attributeDefs;
		var colorAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Color");
		var shapeAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Shape");
		var iconAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Icon");		
		vizDef.nodeDefs[nodeDefIdx].attributeDefs[colorAttrDefIdx].fieldDef.staticText = nodeUiProps.color + ""; 
		vizDef.nodeDefs[nodeDefIdx].attributeDefs[shapeAttrDefIdx].fieldDef.staticText = nodeUiProps.shape;
		if(iconAttrDefIdx != undefined) {
			vizDef.nodeDefs[nodeDefIdx].attributeDefs[iconAttrDefIdx].fieldDef.staticText = nodeUiProps.image;
		} else if (nodeUiProps.image != "null") {
			var relgraph = new RelationGraphJson(window.dataview.myData);
			var iconAttrDef = relgraph.getAttributeJson("csi.internal.Icon", nodeUiProps.image, null, "STATIC", "string");
			vizDef.nodeDefs[nodeDefIdx].attributeDefs.push(iconAttrDef);
		}
		
	});
	
}
DataView.prototype.switchWithMain = function(relImage) {
	var dv = this;
	var mainViz = utils.getMainVisualisation(relImage.ws.uuid);
	utils.showLoadingIndicator(relImage.wsIndex, relImage.position);
	utils.showLoadingIndicator(relImage.wsIndex, 0);
	var createrg = new CreateRG()
	if (mainViz != undefined){
		mainViz.position = relImage.position;
		utils.getVisualisation(mainViz.uuid).visualization.position = mainViz.position;
		createrg.saveSettings(mainViz.uuid, mainViz, false);
	} else {
		var smallPanelView = {
				workSheetIndex: relImage.wsindex,
				vizPosition: relImage.position
		};
		var emptyPanelHtml = Mustache.render($('#small-panel-empty-layout').html(), smallPanelView);
		$('#layout' + relImage.wsIndex + '_panel' + relImage.position).html('');
		$('#layout' + relImage.wsIndex + '_panel' + relImage.position).append(emptyPanelHtml);
	}
	var viz = utils.getVisualisation(relImage.viz);
	viz.visualization.position = "0";
	utils.getVisualisation4mWorksheet(relImage.ws.uuid, viz.visualization.uuid).position = viz.visualization.position;
	createrg.saveSettings(viz.visualization.uuid, viz.visualization, false);
}
DataView.prototype.registerSortablePanels = function(worksheetIndex, worksheetUuid) {
	var sortAction = new SortSecondaryPanels(worksheetIndex, worksheetUuid);
	sortAction.doTask();
}

DataView.prototype.registerDeleteVisualization = function() {
$("#deleteWorksheetButton").click(function(){
	     var dWid= new DeleteWorksheet($("#deleteWorksheetDialogue #deleteWorksheetButton").attr("wsuuid"));
	     dWid.doTask();
   });
}

DataView.prototype.registerRenameWorksheet = function() {
 $("#renameWorksheetButton").click(function(){
        var rwid= new RenameWorksheet($("#renameWorksheetDialogue #renameWorksheetButton").attr("wsuuid"));
        rwid.doTask();
    });
}

DataView.prototype.registerDeleteWorksheet = function() {
    $("#btn_delete_visualization").click(function(){
        var removeViz= new RemoveVisualization();
        removeViz.doTask($(this).attr('vizid'));
       });
}

