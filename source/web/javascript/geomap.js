var tooltipData = [' ',' ',' ',' ',' ',' '];
var basicFunctions = ['Count','Count Distinct'];
var advFunctions = ['Sum','Average','Percent','Maximum','Minimum','StdDev','Variance'];

function CreateGeoMapSettings() {
  
};
 CreateGeoMapSettings.prototype.CreateGeoMapSettings = function(uuid){
	if (uuid != undefined) {
		this.uuid = uuid;
		var vizDef = utils.getVisualisation(this.uuid).visualization;
		this.vizDef = JSON.parse(JSON.stringify(vizDef));
	}
};
CreateGeoMapSettings.prototype.createFieldList = function() {
	$('#fields_list').empty();
	if (window.dataview.myData.resultData.meta.modelDef.fieldDefs.length > 0) {
		$.each(window.dataview.myData.resultData.meta.modelDef.fieldDefs, function() {
			if(this.fieldType == "COLUMN_REF"){
				var columnDiv = $('<div>').addClass('inlinerows');
				var image1 = $('<img>').attr('src', '../images/icon1.jpg').attr('width', "15").attr('height', "18").addClass('floatLt');
				var image2 = null;
				if(this.valueType == "integer"){
					image2 = $('<img>').attr('src', '../images/icon2integer.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				else if (this.valueType == "number"){
					image2 = $('<img>').attr('src', '../images/icon2number.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				else if (this.valueType == "string"){
					image2 = $('<img>').attr('src', '../images/icon2string.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				else if(this.valueType == "date"){
					image2 = $('<img>').attr('src', '../images/icon2date.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				else if(this.valueType == "time"){
					image2 = $('<img>').attr('src', '../images/icon2time.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				else if(this.valueType == "date/time"){
					image2 = $('<img>').attr('src', '../images/icon2date_time.jpg').attr('width', "21").attr('height', "18").addClass('floatLt');
				}
				else if(this.valueType == "boolean"){
					image2 = $('<img>').attr('src', '../images/icon2boolean.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
				}
				var titleSpan = $('<span>').css('float', 'left').text(this.fieldName).addClass('ellipsis columnName');
				$(titleSpan).draggable({
			    helper: "clone",
			    revert: "invalid",
			    start: function(event, ui) {
			      ui.helper.css('width', "200px");
			    }
			  });
				columnDiv.empty().append(image1).append(image2).append(titleSpan);
				$('#fields_list').append(columnDiv);
			}
		});
	}
};
CreateGeoMapSettings.prototype.reset = function(){
	$('#fields_list').css('backgroundColor', "#FFF"); //default bg colour
	this.createFieldList();
  this.registerDroppable();
  this.registerTooltip();
  $('#threshold').val('5000');
  $('#geoMapChartName').val("Geo Map Chart");
};
CreateGeoMapSettings.prototype.registerDroppable = function() {
  var dropit = {
      activeClass: "ui-state-default",
      hoverClass: "ui-state-hover",
      option: "tolerance touch",
      accept: function(draggable) {
        return true;
      },
      drop: function(event, ui) {
        $( this ).val( $(ui.draggable).text() );
        new CreateGeoMapSettings().updateFunctionList(this);
      }
    };
  
  $('#heatmapState').droppable(dropit);
  $('#heatmapMeasure').droppable(dropit);
  $('#bubblemapField').droppable(dropit);
  $('#plotmapLatitude').droppable(dropit);
  $('#plotmapLongitude').droppable(dropit);
};
CreateGeoMapSettings.prototype.registerTooltip = function() {
  var dropTooltip = {
      activeClass: "ui-state-default",
      hoverClass: "ui-state-hover",
      option: "tolerance touch",
      accept: function(draggable) {
        return true;
      },
      drop: function(event, ui) {
        tooltipData.push($(ui.draggable).text());
        new CreateGeoMapSettings().populateTooltipList( $('#tooltipField') );
     }
    };
  $('#tooltipField').droppable(dropTooltip);
};
CreateGeoMapSettings.prototype.tooltipsClear = function() {
  tooltipData = [' ',' ',' ',' ',' ',' '];
  $('#tooltipField').empty();
  this.populateTooltipList($('#tooltipField'));
};
CreateGeoMapSettings.prototype.populateTooltipList = function(node) {
  var fieldDiv = $('<div>').addClass('inlinerows');
  $.each(tooltipData, function(index, fname) {
    var field = $('<span>').css('float', 'left').text(fname);
    fieldDiv.empty().append(field);
    $(node).append(fieldDiv);
  });
};
CreateGeoMapSettings.prototype.updateFunctionList = function(node) {
  var id = $(node).attr("id");
  var fieldDef = utils.getFieldDef(window.dataview.myData.resultData.meta.modelDef.fieldDefs, $(node).val());
  var funcCtl = (id == "heatmapMeasure") ? $('#heatmapFunction') : null;
  if (funcCtl == null) {
    funcCtl = (id == "bubblemapField") ? $('#bubblemapFunction') : null;
  }
  if (funcCtl != null) {
    $(funcCtl).empty();
    this.populateFunctions(basicFunctions, funcCtl);
    if (this.isFieldNumeric(fieldDef)) {
      this.populateFunctions(advFunctions, funcCtl);
    }
  }
};
CreateGeoMapSettings.prototype.populateFunctions = function(funcs, funcCtl) {
  $.each(funcs, function(index, name) {
    var option = $('<option>');
    $(option).val(name).text(name);
    $(funcCtl).append(option);
  });
};
CreateGeoMapSettings.prototype.isFieldNumeric = function(fieldDef) {
  var type = fieldDef.valueType.toLowerCase();
  if (type == "integer" || type == "number") {
    return true;
  }
  return false;
};
CreateGeoMapSettings.prototype.saveSettings = function() {
  var viewDefModal = $('#GeoMapChartModal');
  var mapChartJson = new MapChartViewDefJson().getViewDefJson(viewDefModal);
  var url = "/Centrifuge/actions/viz/saveSettings?_f=json&dvUuid="+$.data(document.body, 'dataview').getUrlVars().uuid;
  var doSuccess = function(action, mapChartJson) {
      return function(data) {
        bootbox.alert("Geo Map viewDef saved");
      };
  };
  $.ajax({
        type: "POST",
        processData: false,
        url: url,
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        data: JSON.stringify(mapChartJson),
        success: doSuccess(this, mapChartJson),
        error: function(data) {
          bootbox.alert("Error while saving viewDef");
        }
  });

}; 
