function NewLayoutActions(index) {
	this.index = index;
	this.untitled_worksheetname = "Untitled Worksheet";
	this.layoutTemplates = {
		"RIGHT HAND LAYOUT": "right-hand-layout-template",
		"LEFT HAND LAYOUT": "left-hand-layout-template",
		"SINGLE LAYOUT": "single-panel-layout-template",
		"EQUAL LAYOUT": "equal-panel-layout-template"
	}
	this.topWindowOffset = 96;
	this.workSheetName;
}
NewLayoutActions.prototype.doTask = function() {	
	var newLayoutActions = this;
	var dv = window.dataview;
	$('#right-hand-layout, #left-hand-layout, #single-layout, #equal-layout, .new-layout-select').unbind('click');
	$('#right-hand-layout').click(function() {
		addWorksheet = new AddWorksheet(dv.getUrlVars()["uuid"], "RIGHT HAND LAYOUT", newLayoutActions.getWorkSheetName());
		addWorksheet.doTask(newLayoutActions, "RIGHT HAND LAYOUT");
	});
	$('#left-hand-layout').click(function() {
		addWorksheet = new AddWorksheet(dv.getUrlVars()["uuid"], "LEFT HAND LAYOUT", newLayoutActions.getWorkSheetName());
		addWorksheet.doTask(newLayoutActions, "LEFT HAND LAYOUT");
	});
	$('#single-layout').click(function() {
		addWorksheet = new AddWorksheet(dv.getUrlVars()["uuid"], "SINGLE LAYOUT", newLayoutActions.getWorkSheetName());
		addWorksheet.doTask(newLayoutActions);
	});
	$('#equal-layout').click(function() {
		addWorksheet = new AddWorksheet(dv.getUrlVars()["uuid"], "EQUAL LAYOUT", newLayoutActions.getWorkSheetName());
		addWorksheet.doTask(newLayoutActions);
	});
	$('.new-layout-select').click(function() {
		addWorksheet = new AddWorksheet(dv.getUrlVars()["uuid"], "LEFT HAND LAYOUT", newLayoutActions.getWorkSheetName());
		addWorksheet.doTask(newLayoutActions);
	});
}
NewLayoutActions.prototype.addRightHandLayout = function(worksheetUuid) {
	//var worksheetName = this.getWorkSheetName();
	this.addNewWorksheetHTML(this.workSheetName, "RIGHT HAND LAYOUT", worksheetUuid);
	$(".layouts-tabs a[href='#layout" + this.index + "']").tab('show');
	$('#new-layout-dialogue').modal('hide');
	$('#new-layout-name').val('');
}
NewLayoutActions.prototype.addLeftHandLayout = function(worksheetUuid) {
	//var worksheetName = this.getWorkSheetName();
	this.addNewWorksheetHTML(this.workSheetName, "LEFT HAND LAYOUT", worksheetUuid);
	$(".layouts-tabs a[href='#layout" + this.index + "']").tab('show');
	$('#new-layout-dialogue').modal('hide');
	$('#new-layout-name').val('');
}
NewLayoutActions.prototype.addSinglePanelLayout = function(worksheetUuid) {
	//var worksheetName = this.getWorkSheetName();
	this.addNewWorksheetHTML(this.workSheetName, "SINGLE LAYOUT", worksheetUuid);
	$(".layouts-tabs a[href='#layout" + this.index + "']").tab('show');
	$('#new-layout-dialogue').modal('hide');
	$('#new-layout-name').val('');
}
NewLayoutActions.prototype.addEqualPanelLayout = function(worksheetUuid) {
	//var worksheetName = this.getWorkSheetName();
	this.addNewWorksheetHTML(this.workSheetName, "EQUAL LAYOUT", worksheetUuid);
	$(".layouts-tabs a[href='#layout" + this.index + "']").tab('show');
	$('#new-layout-dialogue').modal('hide');
	$('#new-layout-name').val('');
}
NewLayoutActions.prototype.getWorkSheetName = function() {
	var worksheetName;
	if($('#new-layout-name').val() == "") {
		//var layoutName = this.untitled_worksheetname;		
		var url = "/Centrifuge/actions/viz/getUniqueWorksheetName?_f=json&dvUuid=" + window.dataview.getUrlVars()["uuid"];
		var doSuccess = function (data) {
			worksheetName = (JSON.parse(data.responseText)).resultData;
		};
		$.ajax({
			url: url,
			async : false,
			complete: doSuccess  
		});
	} else {
		worksheetName = $('#new-layout-name').val();
	}
	this.workSheetName = worksheetName;
	return worksheetName;
}
NewLayoutActions.prototype.addNewWorksheetHTML = function(worksheetName, layout, worksheetUuid) {
	var layoutId = "layout"+ this.index;
	var layoutView = {
		index: this.index,
		layoutId: layoutId,
		layoutHref: "#"+layoutId,
		layoutName: worksheetName,
		worksheetUuid: worksheetUuid
	};
	var layoutTabHeaderHTML = Mustache.render($('#one-tab-header-layout').html(), layoutView); 
	$('.layouts-tabs li.addLayout').before(layoutTabHeaderHTML);
	var template = this.layoutTemplates[layout];
	var layoutHTML = $(Mustache.render($('#'+template).html(), layoutView));
	var normalizedWindowHeight = $(window).height() - this.topWindowOffset;
	layoutHTML.find('.rel-panels').css('height', normalizedWindowHeight);
	layoutHTML.find('#left_content, #right_content').css('height', normalizedWindowHeight);
	$('.layouts').append(layoutHTML);
	this.registerSortablePanels(worksheetUuid);
	this.registerPanelCollapse();
	this.registerToggleToFullscreen();
    this.registerResizeAction();
    this.registerAddVisualization();
    $('#preload-dialogue').modal('hide');
    $("#work_sheet_id" + layoutView.index).val(layoutView.worksheetUuid);
    $("#renameWS" + worksheetUuid).click(function(){
            $("#renameWorksheetDialogue #renameWorksheetButton").attr("wsuuid", $(this).attr("wsuuid"));
            $("#renameWorksheetDialogue #renameWorksheetName").val($("#layoutTab" + worksheetUuid + " .lName").text());
            $("#renameWorksheetDialogue").modal();
    });
   

	$("#deleteWS" + worksheetUuid).click(function() {
		$("#deleteWorksheetDialogue #deleteWorksheetButton").attr("wsuuid", $(this).attr("wsuuid"));
		var worksheetName = $("#layoutTab" + worksheetUuid + " .lName").text();
		bootbox.dialog("This will permanently delete the worksheet " + worksheetName + " and all its visualizations. Are you sure you want to delete " + worksheetName + "? ", [{
			"label" : "Delete Visualization",
			"class" : "btn-primary",
			"callback" : function() {
				var dWid= new DeleteWorksheet($("#deleteWorksheetDialogue #deleteWorksheetButton").attr("wsuuid"));
	     		dWid.doTask();
			}
		}, {
			"label" : "Cancel",
			"class" : "null",
			"callback" : function() {
			}
		}]);

		// $("#deleteWorksheetDialogue").modal();
		
	}); 

	$(".layouts-tabs").sortable({
		items : "li:not(.addLayout)",
		update : function(e, ui) {
			var layoutId = ui.item.attr('id');
			var saveWorksheetIndex = new SaveWorksheetIndex(window.dataview.myData.resultData.uuid, ui.item.index(), $(ui.item).find('.renameWorksheet').attr('wsuuid'));
			saveWorksheetIndex.doTask();
		}
	});
	
	
	$('#layoutTab' + worksheetUuid + ' a[data-toggle="tab"]').on('shown', function (e) {
		$('.nano').nanoScroller({
			preventPageScrolling : true
		});
	});
	 
}
NewLayoutActions.prototype.registerSortablePanels = function(worksheetUuid) {
	var sortAction = new SortSecondaryPanels(this.index, worksheetUuid);
	sortAction.doTask();
}
NewLayoutActions.prototype.registerPanelCollapse = function() {
	var obj = this;
	$('#collapseDiv'+obj.index+'_3').on('show', function() {
		$('#collapseLink'+obj.index+'_3').removeClass('toggle_closed').addClass('toggle');
	});
	$('#collapseDiv'+obj.index+'_3').on('hide', function() {
		$('#collapseLink'+obj.index+'_3').removeClass('toggle').addClass('toggle_closed');
	});
        $('#layout'+obj.index+' #box_0 .box_head').dblclick(function(){
            $('#collapseLink'+obj.index+'_3').click();
        });
}
NewLayoutActions.prototype.registerToggleToFullscreen = function() {
	var newLayout = this; 
	$('#toggle'+newLayout.index).click(function() {
		if($(this).attr('src') == '../images/toggle.png') {
			$('#layout'+newLayout.index+' #left_content').hide();
			$('#layout'+newLayout.index+' #right_content').css('width', '100%').css('padding-left', '0%');
			$(this).attr('src', '../images/toggle_inverse.png');
                        $("#arrow-out-button" +newLayout.index).click();
		} else {
			$('#layout'+newLayout.index+' #left_content').show();
			$('#layout'+newLayout.index+' #right_content').css('width', '71%').css('padding-left', '10px');
			$(this).attr('src', '../images/toggle.png');
                        $("#arrow-out-button" +newLayout.index).click();
		}
	});
	$('#toggle_right_panel' + newLayout.index).click(function() {
		if($(this).attr('src') == '../images/toggle_inverse.png') {
			$('#layout'+newLayout.index+' #left_content').hide();
			$('#layout'+newLayout.index+' #right_content').css('width', '100%').css('padding-left', '0%');
			$(this).attr('src', '../images/toggle.png');
                        $("#arrow-out-button" +newLayout.index).click();
		} else {
			$('#layout'+newLayout.index+' #left_content').show();
			$('#layout'+newLayout.index+' #right_content').css('width', '71%')
			$(this).attr('src', '../images/toggle_inverse.png');
                        $("#arrow-out-button" +newLayout.index).click();
		}
	});
}
NewLayoutActions.prototype.registerResizeAction = function() {
	var newLayout = this;
	
	$(window).resize(function () {
		var normalizedWindowHeight = $(window).height() - newLayout.topWindowOffset;
		$('#layout'+newLayout.index+' #left_content').css('height', normalizedWindowHeight);
		$('#layout'+newLayout.index+' #right_content').css('height', normalizedWindowHeight);
		$('#layout'+newLayout.index+' .rel-panels').css('height', normalizedWindowHeight);
		$('#layout'+newLayout.index).find('.toggle_container.relation_panel_div').css('height', (normalizedWindowHeight - 33)+'px');
		$('#layout'+newLayout.index).find('#relation-graph').css({'height': (normalizedWindowHeight - 66)+'px', 'overflow': 'hidden'});
		$('#layout'+newLayout.index).find('.zoom-element').css({'height': (normalizedWindowHeight - 68)+'px'});
		$('#layout'+newLayout.index).find('.absolute_div').css('position', 'absolute').css('height', (normalizedWindowHeight - 29)+'px');
	});
}
NewLayoutActions.prototype.registerAddVisualization = function() {
	var newLayout = this;
	$('#addVisualizationHeaderLink' + newLayout.index).click(function() {
		var worksheetUuid = $(this).attr('wsuuid');
		totaNumberPanels = $('#layout' + newLayout.index + ' .vizPanels').length;
		var emptyPanelPositions = [];
		
		$('#layout' + newLayout.index + ' .vLayoutPosition').each(function(i) {
			emptyPanelPositions.push($(this).text());
		});
		if(emptyPanelPositions.length == 0) {
			var newPosition = totaNumberPanels;
			$('#new-visualization-dialogue').data('parent', 'layout' + newLayout.index + '_chart' + newPosition); 
			$('#new-visualization-dialogue').data('position', newPosition.toString());
			$('#new-visualization-dialogue').data('worksheetUuid', worksheetUuid);
			$('#new-visualization-dialogue').modal();			
		} else {
			var firstEmptyPanelPos = emptyPanelPositions.sort()[0];
			$('#layout' + newLayout.index + '_panel' + firstEmptyPanelPos).find('.addVisualization').click();
		}
		
	});
}