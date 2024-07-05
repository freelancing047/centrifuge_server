var columnNames = new Array(); 
var dc_selectedColumns = new Array();
var dc_m_selectedColumns = new Array();
var cat_sel_index=0, mea_sel_index=0, cat_m_sel_index=0;
var chart_type;
var chartJsonData;
var chartVizIndex = 1000;

function openSettings(vizId, vizIndex,wkstIndex) {
	var viz = utils.getVisualisation(vizId).visualization;
	var chartUtils  = new ChartUtils();
	var chartObj = new Object();
	if (viz["class"] == "csi.server.common.model.DrillDownChartViewDef") {
				    		chartObj.name = viz.name;
				    		chartObj.position = (viz.position == undefined ? 0 : viz.position);
				    		chartObj.viz = viz.uuid;
				    		chartObj.vizIndex = vizIndex;
				    		//chartObj.ws = worksheet;
				    		chartObj.wsIndex = wkstIndex;
				    		chartObj.chartFunction = chartUtils.getChartFunction(viz.metrics);
				    		chartObj.chartType = viz.chartType;
				    		chartObj.dimensions = chartUtils.getDimensions(viz.dimensions);
				    		chartObj.cell = chartUtils.getMetrics(viz.metrics);
				    	}
	var chartDrawObj = 	chartUtils.createChartDrawObject(0, 'layout' + chartObj.wsIndex + '_chart' + chartObj.position, chartObj.wsIndex, chartObj.viz, chartObj.position, chartObj.vizIndex,
					chartObj.name, chartObj.chartType, chartObj.chartFunction, chartObj.dimensions, chartObj.cell);
	$('#drillChartName')[0].value = chartObj.name;
	selectChartType(chartObj.chartType, chartObj.chartType + "_button_span");
	dc_selectedColumns.splice(0,chartObj.dimensions.length);
	dc_m_selectedColumns.splice(0, chartObj.cell.length);
	$("#dc_categories_outer_div").html("");
	$("#dc_measures_outer_div").html("");
	for(i=0;i<chartObj.dimensions.length;i++){
		addCategory(chartObj.dimensions[i]);
	}
	for(i=0;i<chartObj.cell.length;i++){
		addMeasure(chartObj.dimensions[i]);
	}
	$("#saveDrillChart").attr('onclick', "updateChart('"+vizId+"', '"+vizIndex+"', '" + chartObj.wsIndex +"');");
	$('#DrillChartModal').modal();
	
}
function updateChart(vizId, vizIndex,wkstIndex){
	var viz = utils.getVisualisation(vizId).visualization;
	var chartUtils  = new ChartUtils();
	var chartObj = new Object();
	var id = window.dataview.myData.resultData.uuid;
    var ws = utils.getWorksheet4Visualization(vizId);
    
	
	if (viz["class"] == "csi.server.common.model.DrillDownChartViewDef") {
				    		chartObj.name = viz.name;
				    		chartObj.position = (viz.position == undefined ? 0 : viz.position);
				    		chartObj.viz = viz.uuid;
				    		chartObj.vizIndex = vizIndex;
				    		//chartObj.ws = worksheet;
				    		chartObj.wsIndex = utils.getWorksheetIndex(ws.worksheet.uuid);
				    		chartObj.chartFunction = chartUtils.getChartFunction(viz.metrics);
				    		chartObj.chartType = viz.chartType;
				    		chartObj.dimensions = chartUtils.getDimensions(viz.dimensions);
				    		chartObj.cell = chartUtils.getMetrics(viz.metrics);
				    	}
	var chartDrawObj = 	chartUtils.createChartDrawObject(0, 'layout' + chartObj.wsIndex + '_chart' + chartObj.position, chartObj.wsIndex, chartObj.viz, chartObj.position, chartObj.vizIndex,
					chartObj.name, chartObj.chartType, chartObj.chartFunction, chartObj.dimensions, chartObj.cell);
	
	$('#DrillChartModal').modal('hide');
	$("#saveDrillChart").attr('onclick', "$('#DrillChartModal').modal('hide');openDrillChart($('#DrillChartModal').data('parent'), 'create');");
	openDrillChart('layout' + chartObj.wsIndex + '_chart' + chartObj.position, 'update', chartDrawObj);
}
function clearDrillChartInput(){
	$('#drillChartName')[0].value = "";
	dc_selectedColumns.splice(0,dc_selectedColumns.length);
	dc_m_selectedColumns.splice(0, dc_m_selectedColumns.length);
	$("#dc_categories_outer_div").html("");
	$("#dc_measures_outer_div").html("");
	chart_type = "";
	$(".button_span").removeClass('highlight_button_span');
}
function sortDrillChart(vizId, vizIndex,wkstIndex){
	
	/**TO DO : The following piece needs to be moved to chart utils and used by this function and 2 above**/
	var viz = utils.getVisualisation(vizId).visualization;
	var chartUtils  = new ChartUtils();
	var chartObj = new Object();
	if (viz["class"] == "csi.server.common.model.DrillDownChartViewDef") {
				    		chartObj.name = viz.name;
				    		chartObj.position = (viz.position == undefined ? 0 : viz.position);
				    		chartObj.viz = viz.uuid;
				    		chartObj.vizIndex = vizIndex;
				    		//chartObj.ws = worksheet;
				    		chartObj.wsIndex = wkstIndex;
				    		chartObj.chartFunction = chartUtils.getChartFunction(viz.metrics);
				    		chartObj.chartType = viz.chartType;
				    		chartObj.dimensions = chartUtils.getDimensions(viz.dimensions);
				    		chartObj.cell = chartUtils.getMetrics(viz.metrics);
				    	}
	var chartDrawObj = 	chartUtils.createChartDrawObject(0, 'layout' + chartObj.wsIndex + '_chart' + chartObj.position, chartObj.wsIndex, chartObj.viz, chartObj.position, chartObj.vizIndex,
					chartObj.name, chartObj.chartType, chartObj.chartFunction, chartObj.dimensions, chartObj.cell);
	
	
	var sortDims = $('#chartSortCategories').find('.asc-desc');
	var sortArray = []
	sortDims.each(function(){
        var $img = $(this).find('img');
        var dim = $img.attr('dim');
        var sortOrder;
        var sort;
		if($img.attr('src') == "../images/sorting.png"){
			sortOrder = 'ASCENDING';
		}else{
			sortOrder = 'DESCENDING';
		}
		sort = { name: dim,
                  order: sortOrder
               };
        sortArray.push(sort);
		
    });


	var chartContainerId = chartDrawObj.container + '_chartDiv';
	var masterContainerId = "master-container"+chartContainerId; 				
	var param ={sorting:  sortArray};
		param = JSON.stringify(param);
		var imageParam={width:600,height:50}
		imageParam = JSON.stringify(imageParam);
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/summary",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) { 
            		var summ = eval(response);
            		chartDrawObj.count = summ.dimensions[0].count;
            	},
            	error: function (response) {
            		bootbox.alert("Error getting summary.");
            	}
        		});
		$.ajax( {
					type: 'POST',
					url: '/Centrifuge/charting/'+chartDrawObj.viz+'/image?asynch=true',
					dataType: 'json',
					contentType: 'application/json',
					data: imageParam,
					processData: false,
					success: function(data,status,jqXhr) {
						if( data == null && jqXhr.getResponseHeader("Location") != null) {
							var jobUrl = jqXhr.getResponseHeader("Location");
							manualUrl = jobUrl;
							chartDrawObj.imageUrl = jobUrl;
							imageFetch( manualUrl, 500, "HEAD", masterContainerId );
						} else {
							var text = JSON.stringify(data);
							$('#'+masterContainerId).text(text);
						}

					},
					error: function (response) {
						bootbox.alert("Error getting chart overview image.")
            	}
				});
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) { 
            		drawChart(eval(response), chartDrawObj);
            	},
            	error: function (response) {
            		console.log(response.responseText);
            		bootbox.alert("Error getting data for the chart.");
            	}
        		});
      

}
function populateSortingPanelColumns(vizuuid){
	var viz = utils.getVisualisation(vizuuid).visualization;
	var chartUtils  = new ChartUtils();
	var dimensions = chartUtils.getDimensions(viz.dimensions);
	$('#chartSortCategories').html('');
	for(i=0;i<dimensions.length; i++){
		$('#chartSortCategories').append('<div class="inlinerows"><span class="span4">'+dimensions[i]+'<\/span>'+'<a class="floatRt asc-desc"><img src="../images/sorting.png" width="18" height="16" dim="'+dimensions[i]+'"><\/a><\/div>');
	}	
	$('#chart-sort-modal').find('.asc-desc').unbind('click');
				$('#chart-sort-modal').find('.asc-desc').click(function() {
					var $img = $(this).find('img');
					if($img.attr('src') == "../images/sorting.png") {
						$img.attr('src', "../images/sorting_reverse.jpg");
					} else {
						$img.attr('src', "../images/sorting.png");
					}
					
				});
}
function addCategory(dimValue){
	var div = $('<div>').attr('id','dc_categories_div_'+cat_sel_index);
	var sel = $('<select>').attr('class','dc_categories').attr('id','dc_categories_'+cat_sel_index);
	var selected = false;
	var selectedVal;
	$(columnNames).each(function() {
		var op = $("<option>").attr('value',this.val).text(this.text);
		if(dc_selectedColumns.length ==0){
			if(dimValue && (dimValue == this.val)){
				
			dc_selectedColumns.push(this.val);
			selectedVal = this.val;
			op.attr('selected', 'true');
			selected = true;}
		}else{
			var val = this.val;
			var addOption = true
			$(dc_selectedColumns).each(function() {
				if(this==val){
					addOption = false;
				}
			});
			if(addOption && !selected && (dimValue && (dimValue == val))){
				dc_selectedColumns.push(val);
				selectedVal = val;
				op.attr('selected', 'true');
				selected = true;
			}
		}
	 	sel.append(op);
	});
	div.append("<span class='span2'>&nbsp;<\/span>").append(sel).append("<a onclick='$(\"#chart-bundle-editor-modal\").modal();'> <img src='../images/edit.png' ><\/a>&nbsp;<a onclick='removeCategory("+selectedVal + "," + cat_sel_index+");'>&nbsp;<img src='../images/ico_delete.jpg' ><\/a>&nbsp;<img src='../images/ico_move.png' >").append("<br>");
	$("#dc_categories_outer_div").append(div);
	if(dc_selectedColumns.length == columnNames.length){
		$("#dc_add_category").attr("disabled", "disabled");
	}
         $("#dc_categories_outer_div").sortable();
	cat_sel_index++;
}
function removeCategory(selectedVal, index){
	var i=0
	$(dc_selectedColumns).each(function() {
				if(this==selectedVal){
					
					dc_selectedColumns.splice(i,1);
				}
				i++;
			});
	$("#dc_categories_div_"+index).remove();
	if(dc_selectedColumns.length != columnNames.length){
		$("#dc_add_category").removeAttr("disabled");
	}
}
function removeMeasure(selectedVal, index){
	var i=0
	$(dc_m_selectedColumns).each(function() {
				if(this==selectedVal){
					
					dc_m_selectedColumns.splice(i,1);
				}
				i++;
			});
	$("#dc_measures_div_"+index).remove();
	if(dc_m_selectedColumns.length != columnNames.length){
		$("#dc_add_measure").removeAttr("disabled");
	}
}
function addMeasure(metricName){
	var div = $('<div>').attr('id','dc_measures_div_'+mea_sel_index);
	var sel = $('<select>').attr('class','dc_m_categories');
	var selected = false;
	var selectedVal;
	$(columnNames).each(function() {
		var op = $("<option>").attr('value',this.val).text(this.text);
		if(dc_m_selectedColumns.length ==0){
			if(metricName && (metricName == this.val)){
			dc_m_selectedColumns.push(this.val);
			selectedVal = this.val;
			op.attr('selected', 'true');
			selected = true;}
		}else{
			var val = this.val;
			var addOption = true
			$(dc_m_selectedColumns).each(function() {
				if(this==val){
					addOption = false;
				}
			});
			if(addOption && !selected && (metricName && (metricName == val))){
				dc_m_selectedColumns.push(val);
				selectedVal = val;
				op.attr('selected', 'true');
				selected = true;
			}
		}
	 	sel.append(op);
	 	registerChangeEventForMeasuresColumnSelect(sel);
	});
	
	div.append("<span class='span2'>&nbsp;<\/span>").append("<select id='dc_selectedMeasures_"+mea_sel_index+"' class='dc_selectedMeasures'><option value='Count'>Count<\/option><option value='Count Distinct'>Count Distinct<\/option><\/select>&nbsp;").append(sel).append("<a onclick='removeMeasure("+selectedVal + "," + mea_sel_index+");'>&nbsp;<img src='../images/ico_delete.jpg' ><\/a>&nbsp;<img src='../images/ico_move.png' >").append("<br>");

	$("#dc_measures_outer_div").append(div);
	//if(dc_m_selectedColumns.length == columnNames.length){
	//one measure for now..
	if(dc_m_selectedColumns.length == 1){
		$("#dc_add_measure").attr("disabled", "disabled");
	}
        $("#dc_measures_outer_div").sortable();
	mea_sel_index++;
}

function registerChangeEventForMeasuresColumnSelect(sel) {
	sel.change(function() {
		var columnName = $(this).val();
		var smallSelectOptions = getSmallSelectOptions();
		var largeSelectOptions = getLargeSelectOptions();
		var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))});
		var seletdCol = _.find(cols, function(col) { return col.fieldName == columnName});
		if(seletdCol.valueType == 'number' || seletdCol.valueType == 'integer') {
			$(this).prev().html(getLargeSelectOptions());
		} else {
			$(this).prev().html(getSmallSelectOptions());
		}
	});
}
function getSmallSelectOptions() {
	return getOptions(['Count', 'Count Distinct']);
}
function getLargeSelectOptions() {
	return getOptions(['Count', 'Count Distinct', 'Sum', 'Percent', 'Average', 'Maximum', 'Minimum', 'StdDev', 'Variance']);
}
function getOptions(measuresArray) {
	var dummyDiv = $('<div>');
	$.each(measuresArray, function() {
		dummyDiv.append($('<option>').text(this).attr('value', this));
	});
	return dummyDiv.html();
}
	function  selectChartType(value, container) {
		chart_type = value;
		$(".button_span").removeClass('highlight_button_span');
		$("#"+container).addClass('highlight_button_span');
	}
	function trimDrillValues(chartDrawObj, drillLevel){
		chartDrawObj.drillValue.splice(drillLevel, chartDrawObj.drillLevel);
		for(var i=drillLevel+1; i<=chartDrawObj.drillLevel; i++){
			$("#" + chartDrawObj.container+"_breadCrumbsDrillSpan_" + i).html("<span>  <\/span>")
		}
		chartDrawObj.drillLevel = drillLevel;
	}
	function setChart(chart, name, categories, data, color, imageUrl, masterContainerId) {
		chart.xAxis[0].setCategories(categories, false);
		chart.series[0].remove(false);
		chart.addSeries({
			name : name,
			data : data,
			color : color || RandomColor()
		}, false);
		
		
		chart.redraw();
		if(imageUrl){
			imageFetch( imageUrl, 500, "HEAD", masterContainerId );
		}
	}
	RandomColor = function() {
    colors = ['red', 'yellow', 'blue', 'green']
    return colors[Math.floor(Math.random()*colors.length)];
}

function openDrillChart(chartContainerIdval, chartAction, chartObj) {
	var createChart = new CreateChart();
	createChart.doTask(chartContainerIdval, chartAction, chartObj);
	}	
function CreateChart(){
}
CreateChart.prototype.doTask =function(chartContainerIdval, chartAction, chartObj) {
	
	var chartUtils = new ChartUtils();
	var categories = [], chartFunctions = [], measures = [];
	$('select.dc_categories option:selected').each(
             function(){
                  categories.push($(this).text());
             });
    $('select.dc_selectedMeasures option:selected').each(
             function(){
                  chartFunctions.push($(this).text());
             });
    $('select.dc_m_categories option:selected').each(
             function(){
                  measures.push($(this).text());
             });
	if(chartObj!=null){
		//var chartDrawObj = 	chartObj;
		('while updating position ' + chartObj.position + "***");
		var chartDrawObj = 	chartUtils.createChartDrawObject(0, chartContainerIdval, chartObj.wsIndex , chartObj.viz, chartObj.position, chartObj.vizIndex,
					$('#drillChartName').val(), chart_type, chartFunctions, categories, measures);
	}else{
		var chartDrawObj = 	chartUtils.createChartDrawObject(0, chartContainerIdval,($.data($('#new-visualization-dialogue')[0], 'worksheetUuid')), '', $.data($('#new-visualization-dialogue')[0], 'position'), chartVizIndex,
					$('#drillChartName').val(), chart_type, chartFunctions, categories, measures);
	}
	this.addVisualization(chartDrawObj, chartAction);
}
CreateChart.prototype.addVisualization = function(chartDrawObj, chartAction){
	var dv = $.data(document.body, 'dataview');
    var id = dv.getUrlVars()["uuid"];
    var worksheetUuid = chartDrawObj.worksheet || dv.myData.resultData.meta.modelDef.worksheets[0].uuid;
    var chartJson = new ChartJson(dv.myData);
    var requestJson;
    var action = this;
	if(chartAction.toString() == 'create' || chartAction.toString() == 'update'){
		requestJson = chartJson.getVisualDefJson(chartDrawObj, chartAction);
		var requestJsonClone = JSON.parse(JSON.stringify(requestJson));
		chartVizIndex ++;
		//create new visualization
		if(chartAction.toString() == 'create'){
	    	var url = "/Centrifuge/actions/viz/addVisualization?_f=json&filterChanged=false&dvUuid="+id+"&worksheetUuid="+worksheetUuid;
	   }else if (chartAction.toString() == 'update'){
	   		var url = "/Centrifuge/actions/viz/saveSettings?_f=json&filterChanged=false&dvUuid="+id+"&worksheetUuid="+worksheetUuid+"&position="+chartDrawObj.position;
	   }
	    var doSuccess = function(action, chartJson) {
	        return function(data) {
	        	chartDrawObj.viz = requestJson['uuid'];
	            action.saveChart(requestJson, chartDrawObj);
	        };
	    };
	    $.ajax({
	          type: "POST",
	          processData: false,
	          url: url,
	          contentType: 'application/json; charset=utf-8',
	          dataType: 'json',
	          data: JSON.stringify(requestJsonClone),
	          success: doSuccess(action, chartJson),
	          error: function(data) {
	          	bootbox.alert("Error while creating new visualization");
	          }
	    });
    }else{
		//redraw pre-added visualization
    	action.saveChart(requestJson, chartDrawObj);
    }

}
CreateChart.prototype.saveChart = function(requestJson, chartDrawObj){
	this.loadChart(requestJson, chartDrawObj);
}
CreateChart.prototype.loadChart = function(requestJson, chartDrawObj){
	var chartContainerId = chartDrawObj.container + '_chartDiv';
	var masterContainerId = "master-container"+chartContainerId; 				
	var param ={ };
		param = JSON.stringify(param);
		var imageParam={width:600,height:50}
		imageParam = JSON.stringify(imageParam);
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/summary",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) { 
            		var summ = eval(response);
            		chartDrawObj.count = summ.dimensions[0].count;
            	},
            	error: function (response) {
            		bootbox.alert("Error getting summary.");
            	}
        		});
		$.ajax( {
					type: 'POST',
					url: '/Centrifuge/charting/'+chartDrawObj.viz+'/image?asynch=true',
					dataType: 'json',
					contentType: 'application/json',
					data: imageParam,
					processData: false,
					success: function(data,status,jqXhr) {
						if( data == null && jqXhr.getResponseHeader("Location") != null) {
							var jobUrl = jqXhr.getResponseHeader("Location");
							manualUrl = jobUrl;
							chartDrawObj.imageUrl = jobUrl;
							imageFetch( manualUrl, 500, "HEAD", masterContainerId );
						} else {
							var text = JSON.stringify(data);
							$('#'+masterContainerId).text(text);
						}

					},
					error: function (response) {
						bootbox.alert("Error getting chart overview image.")
            	}
				});
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) { 
            		drawChart(eval(response), chartDrawObj);
            	},
            	error: function (response) {
            		bootbox.alert("Error getting data for the chart.");
            	}
        		});
        param ={width:600,height:50};
		param = JSON.stringify(param);
        
}
function imageFetch(url, timeout, option, masterContainerId) {
				var tempUrl = url +"?nowait";

				$.ajax( {
					type: option,
					url : tempUrl,
					processData: false,
					success: function(data,status,jqXhr) {
						if( jqXhr.status == 202 ) {
							resend( url, timeout,  "HEAD", masterContainerId);
						} else if( jqXhr.status == 200) {
							var $img = $("<img />");
							$img.attr( {
								src: url+"?wait=1000",
								alt: "Overview",
								style: "width:100%"
							});
							$('#'+masterContainerId).html($img);
						}
					},
					error: function (response) {
						bootbox.alert("Error getting image itself.");
            	}
				});
				
			}
			
function resend(url, timeout, option, masterContainerId) {
				var nextTimeout = Math.min( 5000, timeout*2);
				setTimeout( imageFetch, timeout, url, nextTimeout, option, masterContainerId);
			} 
CreateChart.prototype.getdonutData = function(chartData, chartDrawObj){
	var keys = [];
    var colors = Highcharts.getOptions().colors;
    for (var key in chartData.points[0]) {
		keys.push(key);
	}
	var donutData = new Object();
	donutData["donutLevel1Data"] = [];
	donutData["donutLevel2Data"] = [];
	for(var i=0; i<chartData.points.length; i++){
		var param ={ 
			drill:[chartData.points[i][keys[1]]]
			};
			param = JSON.stringify(param);
			$.ajax({
	            	type: 'POST',              
	            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
	            	async: false,
	            	dataType: 'json',
	            	contentType: 'application/json',
	            	data: param,
	            	processData: false,
	            	success: function (response) { 
	            		var donutLevel2Data = eval(response);
	            		var dkeys = [];
	            		for (var key in donutLevel2Data.points[0]) {
							dkeys.push(key);
						}
						for(var j=0; j<donutLevel2Data.points.length; j++){
							var brightness = 0.2 - (j / donutLevel2Data.points.length) / 5 ;
                   			donutData["donutLevel2Data"].push({
									        name: donutLevel2Data.points[j][dkeys[1]],
									        y: donutLevel2Data.points[j][dkeys[0]],
									        color: Highcharts.Color(colors[i]).brighten(brightness).get()
									    });
						}
	            	},
	            	error: function (response) {
	            		//alert(response.responseText);
	                	//alert('Error getting data for the donut chart.');
	            	}
	        		});
	    
		donutData["donutLevel1Data"].push({
								        name: chartData.points[i][keys[1]],
								        y: chartData.points[i][keys[0]],
								        color: colors[i]
								    });
	}
	return donutData;
};
CreateChart.prototype.getDrillChart = function(chart, chartDrawObj, drillVal, name){
	 // restore
		var chartContainerId = chartDrawObj.container + '_chartDiv';
		var masterContainerId = "master-container"+chartContainerId; 
		var chartDrillData;
		var param ={ drill: drillVal
		};
		var imageParam={
			width:600,height:50,drill: drillVal
		};
		imageParam = JSON.stringify(imageParam);
		param = JSON.stringify(param);
		$.ajax({
								            	type: 'POST',              
								            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
								            	dataType: 'json',
								            	contentType: 'application/json',
								            	data: param,
								            	processData: false,
								            	success: function (response) { 
								            		var summ = eval(response);
								            		chartDrawObj.count = summ.size;
								            		console.log("drill point count : " + chartDrawObj.count);
								            		
								            	},
								            	error: function (response) {
								            		bootbox.alert("Error getting summary.")
								            	}
								        		});
		//replace data call with summary call when the service is ready
		/*$.ajax({
								            	type: 'POST',              
								            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/summary",
								            	dataType: 'json',
								            	contentType: 'application/json',
								            	data: param,
								            	processData: false,
								            	success: function (response) { 
								            		var summ = eval(response);
								            		chartDrawObj.count = summ.dimensions[0].count;
								            		console.log("drill point count : " + chartDrawObj.count);
								            		
								            	},
								            	error: function (response) {
								            		//alert(response.responseText);
								                	//alert('Error getting summary.');
								            	}
								        		});*/
		$.ajax( {
					type: 'POST',
					url: '/Centrifuge/charting/'+chartDrawObj.viz+'/image?asynch=true',
					dataType: 'json',
					contentType: 'application/json',
					data: imageParam,
					processData: false,
					success: function(data,status,jqXhr) {
						if( data == null && jqXhr.getResponseHeader("Location") != null) {
							var jobUrl = jqXhr.getResponseHeader("Location");
							manualUrl = jobUrl;
							chartDrawObj.imageUrl = manualUrl;
							imageFetch( manualUrl, 500, "HEAD", masterContainerId );
						} else {
							var text = JSON.stringify(data);
							$('#'+masterContainerId).text(text);
						}

					},
					error: function (response) {
						bootbox.alert("Error getting chart overview image.")
            	}
				});
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) {
            		chartDrillData = eval(response);
            								var keys = [], xValues = [], yValues = [], pieData = [], bubbleData=[];
							            	var colors = Highcharts.getOptions().colors;
							            	for (var key in chartDrillData.points[0]) {
											   keys.push(key);
											}
											for(var i=0; i<chartDrillData.points.length; i++){
												var dim = keys[0][0]=='d'?chartDrillData.points[i][keys[0]]:chartDrillData.points[i][keys[1]]
												var metric = keys[0][0]=='m'?chartDrillData.points[i][keys[0]]:chartDrillData.points[i][keys[1]];
												xValues.push(dim);
												yValues.push({
														        y: metric,
														        color: colors[i]
														    });
												pieData.push([dim, metric]);
												/*bubbleData.push({
													x: isNaN(dim)?i:dim,
													y: isNaN(metric)?i:metric,
													marker: {
																radius: 10,
																fillColor: colors[i]
															}
												});*/
												
					
            	
									            }
									            var tempUrl = chartDrawObj.imageUrl;
									            var tempDrillLevel = chartDrawObj.drillLevel;
									            var bcHtml = $('<a>').text((name + ' ( for ' + drillVal + ' )').toString()).click(function(){trimDrillValues(chartDrawObj, tempDrillLevel);setChart(chart, name, xValues, yValues, null, tempUrl, masterContainerId);});
												$("#" + chartDrawObj.container+"_breadCrumbsDrillSpan_" + chartDrawObj.drillLevel).html("<span> < <\/span>");
												$("#" + chartDrawObj.container+"_breadCrumbsDrillSpan_" + chartDrawObj.drillLevel).append(bcHtml);
												setChart(chart, name, xValues, yValues);
												
            	},
            	error: function (response) {
            		bootbox.alert(response.responseText)
            		bootbox.alert("Error getting data for the chart.")
            	}
        		});
}
function resetDetailChart(winObj, chartDrawObj, chart) {
	var chartDetailData;
	var chartOffset, chartSize;
	var windowOffset, windowSize, parentWindowSize;
	var dataSize;
	windowOffset = winObj.position().left;
	windowSize = winObj.width();
	parentWindowSize = winObj.parent().width();
	
	
	var param ={ 
											"drill": chartDrawObj.drillValue
		};
		param = JSON.stringify(param);
		$.ajax({
								            	type: 'POST',              
								            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
								            	dataType: 'json',
								            	contentType: 'application/json',
								            	data: param,
								            	async: false,
								            	processData: false,
								            	success: function (response) { 
								            		var summ = eval(response);
								            		chartDrawObj.count = summ.size;
								            		console.log("drill point count : " + chartDrawObj.count);
								            		
								            	},
								            	error: function (response) {
								            		//alert(response.responseText);
								            		bootbox.alert("Error getting summary.")
								            	}
								        		});
		//replace data call with summary call when the service is ready
		/*$.ajax({
								            	type: 'POST',              
								            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/summary",
								            	dataType: 'json',
								            	contentType: 'application/json',
								            	data: param,
								            	processData: false,
								            	success: function (response) { 
								            		var summ = eval(response);
								            		chartDrawObj.count = summ.dimensions[0].count;
								            		console.log("drill point count : " + chartDrawObj.count);
								            		
								            	},
								            	error: function (response) {
								            		//alert(response.responseText);
								                	//alert('Error getting summary.');
								            	}
								        		});*/
	
	
	dataSize = chartDrawObj.count;
	
	chartOffset = Math.round((windowOffset/parentWindowSize) * dataSize);
	chartSize = Math.round((windowSize/parentWindowSize) * dataSize);
	
	console.log( "chartOffset: " + chartOffset + " chartSize: " + chartSize);
	
		 // restore
											var chartDrillData;
											var param ={ "offset": chartOffset,
											"size": chartSize,
											"drill": chartDrawObj.drillValue
		};
		//param = {};
		param = JSON.stringify(param);
		$.ajax({
            	type: 'POST',              
            	url: "/Centrifuge/charting/"+chartDrawObj.viz+"/data",
            	dataType: 'json',
            	contentType: 'application/json',
            	data: param,
            	processData: false,
            	success: function (response) {
            		chartDetailData = eval(response);
            								var keys = [], xValues = [], yValues = [], pieData = [], bubbleData=[];
							            	var colors = Highcharts.getOptions().colors;
							            	for (var key in chartDetailData.points[0]) {
											   keys.push(key);
											}
											for(var i=0; i<chartDetailData.points.length; i++){
												var dim = keys[0][0]=='d'?chartDetailData.points[i][keys[0]]:chartDetailData.points[i][keys[1]]
												var metric = keys[0][0]=='m'?chartDetailData.points[i][keys[0]]:chartDetailData.points[i][keys[1]];
												xValues.push(dim);
												yValues.push({
														        y: metric,
														        color: colors[i]
														    });
												pieData.push([dim, metric]);
												/*bubbleData.push({
													x: isNaN(dim)?i:dim,
													y: isNaN(metric)?i:metric,
													marker: {
																radius: 10,
																fillColor: colors[i]
															}
												});*/
					
            	
									            }
									            
									            setChart(chart, "name", xValues, yValues);
            	},
            	error: function (response) {
            		bootbox.alert("Error getting data for the chart with offsets.")
            	}
        		});
		
} 
var breadCrumbs = [];
drawChart = function(chartData, chartDrawObj){
				var createChart = new CreateChart();
				var chart;
				var w = $('#left_content .vizPanels:visible').width();
				var h = 270;
				var worksheetIndex = chartDrawObj.worksheet;
				var position = chartDrawObj.position;
				var vizUuid = chartDrawObj.viz;
				var headerView = {
					panelName: 'Chart',
					vizId: vizUuid,
					vizIndex: chartDrawObj.vizIndex,
					worksheetIndex: worksheetIndex,
					vizPosition: position
				};
				var content = Mustache.render($('#small-chart-panel-template').html(), headerView);
				var newPanelId = 'layout' + worksheetIndex + '_panel' + position;
				if($('#' + newPanelId).length == 0) {
					var newColDiv = $('<div>').addClass('span12 column ui-sortable').attr('id', 'col' + position);
					var newPanelDiv = $('<div>').addClass('box grad_colour_dark_blue chart_table vizPanels')
					newPanelDiv.attr('id', newPanelId);
					newColDiv.append(newPanelDiv);
					$('#layout' + worksheetIndex + ' #left_content > div.span12').append(newColDiv);
				}
				
				
				$("#" + newPanelId).html('');
				
				$content = $(content);
				$content.find('a.sorting-panel-link').click(function() {
					var vizuuid = $(this).attr('vizuuid');
					var vizIndex = $(this).attr('vizIndex');
					var workSheetIndex = $(this).attr('workSheetIndex');
					populateSortingPanelColumns(vizuuid);
					$('#chart-sort-modal').data('vizuuid',vizuuid);
					$('#chart-sort-modal').data('vizIndex',vizIndex);
					$('#chart-sort-modal').data('workSheetIndex',workSheetIndex);
					$('#chart-sort-modal').modal();
				});
				$("#" + newPanelId).append($content);
				$("#" + chartDrawObj.container).removeAttr('style').html($content);
				$("#" + chartDrawObj.container).parent().removeClass('right-side-boxes');
				var newChartDiv = $('<div>').attr('id', chartDrawObj.container + '_chartDiv');
				
				var breadCrumbsDiv = $('<div>').attr('id', chartDrawObj.container + '_breadCrumbsDiv');
				for(var i=0; i<chartDrawObj.categories.length; i++){
					breadCrumbsDiv.append("<span id='" +chartDrawObj.container + '_breadCrumbsDrillSpan_' + i+ "'>&nbsp;<\/span>");
				}
				$("#collapseDiv" + chartDrawObj.vizIndex).append(breadCrumbsDiv);
				$("#collapseDiv" + chartDrawObj.vizIndex).append(newChartDiv);
				$("#" + newPanelId).append(newChartDiv);
				var chartContainerId = chartDrawObj.container + '_chartDiv';
				var $container = $('#'+chartContainerId).css('position', 'relative');
				
    			var detailContainerId = "detail-container"+chartContainerId;
    			var sizeContainerId = "size-container"+chartContainerId;
        		var $detailContainer = $('<div id="'+detailContainerId+'">').appendTo($container);
        		var $masterChartContainer = $('<div id="masterChartContainer'+chartContainerId+'">')
        		.css({ width: '100%', height:'200px', border:'1px' })
        		.appendTo($container);
		        var masterContainerId = "master-container"+chartContainerId; 
		        var $sizeController = $("<div class='size_controller' id='"+sizeContainerId+"'>").appendTo($masterChartContainer);
		        $sizeController.html("<a href='#' class='controller control-left'><\/a><a href='#' class='controller control-right'><\/a>");
		        $sizeController.resizable({ 
		        	handles: 'e, w',
					containment: "#masterChartContainer"+chartContainerId,
					"resize" : function(event,ui) { 
            			resetDetailChart($(event.target), chartDrawObj, chart);
      				}});
					
		        var $masterContainer = $("<div id='"+ masterContainerId +"'>")
            	.css({ position: 'absolute', height: '100%', width: '100%' })
            	.appendTo($masterChartContainer);
            	
				var keys = [], xValues = [], yValues = [], pieData = [], bubbleData=[];
				var donutData = new Object();
            	var colors = Highcharts.getOptions().colors;
            	for (var key in chartData.points[0]) {
				   keys.push(key);
				}
				for(var i=0; i<chartData.points.length; i++){
					var dim = keys[0][0]=='d'?chartData.points[i][keys[0]]:chartData.points[i][keys[1]]
					var metric = keys[0][0]=='m'?chartData.points[i][keys[0]]:chartData.points[i][keys[1]];
					xValues.push(dim);
					yValues.push({
							        y: metric,
							        color: colors[i]
							    });
					pieData.push([dim, metric]);
					
					bubbleData.push({
						x: isNaN(dim)?i:dim,
						y: isNaN(metric)?i:metric,
						marker: {
									radius: 10,
									fillColor: colors[i]
								}
					});
					
            	//alert(JSON.stringify(chartData)); 
            	
            }
            
            	
				var categories = xValues;
				var name = chartDrawObj.categories[chartDrawObj.drillLevel];
				breadCrumbs.push(name);
				var data = yValues;
				var tempUrl = chartDrawObj.imageUrl;
				var bcHtml = $('<a>').text(name).click(function(){trimDrillValues(chartDrawObj,0);setChart(chart, name, categories, data, null, tempUrl, masterContainerId);});
				$("#" + chartDrawObj.container+"_breadCrumbsDrillSpan_" + chartDrawObj.drillLevel).html(bcHtml);
				var yName = chartDrawObj.chartFunction + " : " + chartDrawObj.measures[0];
				var title = chartDrawObj.chartTitle;
				var chartType = chartDrawObj.chartType;
				if(chartType == 'pie' && chartDrawObj.categories.length > 1){
	            	donutData = createChart.getdonutData(chartData, chartDrawObj);
	            }
				//alert(chartContainerId);
		if(chartType == 'line'){
			chart = new Highcharts.Chart({
            chart: {
                renderTo: "detail-container"+chartContainerId,
                type: 'line',
                marginRight: 130,
                marginBottom: 25
            },
            plotOptions: {
                series: {
				                allowPointSelect: true
				           }
            },
            title: {
                text: title,
                x: -20 //center
            },
            xAxis : {
						categories : categories,
						labels: {
				        	rotation: -45,
				            align: 'right',
				            style: {
				            	fontSize: '13px',
				                fontFamily: 'Verdana, sans-serif'
				           	}
			          	}
					},
            yAxis: {
                title : {
							text : yName 
						},
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function() {
                        return '<b>'+ this.series.name +'</b><br/>'+
                        this.x +': '+ this.y;
                }
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'top',
                x: -10,
                y: 100,
                borderWidth: 0
            },
            series : [ {
						name : name,
						data : data,
						color : colors[0]
					} ]
        });
			
		}else if(chartType == 'pie' && chartDrawObj.categories.length > 1){
			//its a donut
			var colors = Highcharts.getOptions().colors;
    
        // Create the chart
        	chart = new Highcharts.Chart({
            chart: {
                renderTo: "detail-container"+chartContainerId,
                type: chartType
            },
            title: {
                text: title
            },
            plotOptions: {
                series: {
				                allowPointSelect: true
				           },
				pie: {
                    shadow: false
                }
            },
            tooltip: {
        	    valueSuffix: ''
            },
            series: [{
                name: name,
                data: donutData.donutLevel1Data,
                size: '60%',
                dataLabels: {
                    formatter: function() {
                        return this.y > 5 ? this.point.name : null;
                    },
                    color: 'white',
                    distance: -30
                }
            }, {
                name: name,
                data: donutData.donutLevel2Data,
                innerSize: '60%',
                dataLabels: {
                    formatter: function() {
                        // display only if larger than 1
                        return this.y > 1 ? '<b>'+ this.point.name +':</b> '+ this.y  : null;
                    }
                }
            }]
        });
					}else if (chartType == 'pie'){
						// make the container smaller and add a second container for the master chart
        
			chart = new Highcharts.Chart({
            chart: {
                renderTo: "detail-container"+chartContainerId,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: chartType
            },
            title: {
                text: title
            },
            tooltip: {
        	    pointFormat: '{series.name}: <b>{point.percentage}%</b>',
            	percentageDecimals: 1
            },
            plotOptions: {
                series: {
				                allowPointSelect: true
				           },
				pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        formatter: function() {
                            return '<b>'+ this.point.name +'</b>: '+ this.percentage +' %';
                            
                        }
                    }
                }
            },
            series: [{
                type: 'pie',
                name: name,
                data: pieData
            }]
        });
					
					
					}else if(chartType == 'bubble'){
					
					 chart = new Highcharts.Chart({
            chart: {
                renderTo: "detail-container"+chartContainerId,
                type: 'scatter',
                zoomType: 'xy'
            },
            title: {
                text: title
            },
            xAxis: {
                title: {
                    enabled: true,
                    text: name
                },
                startOnTick: true,
                endOnTick: true,
                showLastLabel: true,
				labels: {
		        	rotation: -45,
		            align: 'right',
		            style: {
		            	fontSize: '13px',
		                fontFamily: 'Verdana, sans-serif'
		           	}
	          	}
            },
            yAxis: {
                title: {
                    text :  yName
						}
            },
            tooltip: {
                formatter: function() {
					                return '<b>'+ this.series.name +'</b><br/>'+
					            this.x +' : '+ this.y;
					      }
            },
            legend: {
                layout: 'vertical',
                align: 'left',
                verticalAlign: 'top',
                x: 100,
                y: 70,
                floating: true,
                backgroundColor: '#FFFFFF',
                borderWidth: 1
            },
            plotOptions: {
                series: {
				                allowPointSelect: true
				           },
				scatter: {
                    marker: {
                        radius: 5,
                        states: {
                            hover: {
                                enabled: true,
                                lineColor: 'rgb(100,100,100)'
                            }
                        }
                    },
                    states: {
                        hover: {
                            marker: {
                                enabled: false
                            }
                        }
                    }
                }
            },
            series: [{
					   	  name: name,
					      data: bubbleData
					
					   }]
        });
    
				}else{
					var selectedValue;
					chart = new Highcharts.Chart(
				{
					chart : {
						renderTo : "detail-container"+chartContainerId,
						type : chartType,
						events: {
				            redraw: function(event) {
				                for(j=0;j<chart.series.length;j++){
									for(i=0;i<chart.series[j].data.length;i++){
								    Highcharts.addEvent(chart.series[j].data[i].tracker.element, 'dblclick', function(e){
								    	if(chartDrawObj.drillLevel == (chartDrawObj.categories.length-1)) return;//no more levels to drill into
														chartDrawObj.drillLevel ++;
														chartDrawObj.drillValue.push(selectedValue);
														chartDrawObj.count = createChart.getDrillChart(chart, chartDrawObj, chartDrawObj.drillValue, chartDrawObj.categories[chartDrawObj.drillLevel]);
														
								    });
								    
								   }
				  				}
				            }
				           }
					},
					
					title : {
						text : title
					},
					xAxis : {
						categories : categories,
						labels: {
		                    rotation: -45,
		                    align: 'right',
		                    style: {
		                        fontSize: '13px',
		                        fontFamily: 'Verdana, sans-serif'
		                    }
	               		}
					},
					yAxis : {
						title : {
							text :  yName
						}
					},
					plotOptions : {
						series: {
				                allowPointSelect: true
				           },
						column : {
							cursor : 'pointer',
							point : {
								events : {
									click : function() {
										
									}
								}
							},
							dataLabels : {
								enabled : true,
								color : colors[0],
								style : {
									fontWeight : 'bold'
								},
								formatter : function() {
									return this.y;
								}
							}
						},
						bar : {
							cursor : 'pointer',
							point : {
								events : {
									click : function() {
										
									}
								}
							},
							dataLabels : {
								enabled : true,
								color : colors[0],
								style : {
									fontWeight : 'bold'
								},
								formatter : function() {
									return this.y;
								}
							}
						}
					},
					tooltip : {
						formatter : function() {
							selectedValue = this.x;
							var point = this.point, s = this.x + ':<b>'
									+ this.y;
							/*if (point.drilldown) {
								s += 'Click to view ' + point.category
										+ ' versions';
							} else {
								s += 'Click to return to browser brands';
							}*/
							return s;
						}
					},
					series : [ {
						name : name,
						data : data,
						color : colors[0]
					} ],
					exporting : {
						enabled : false
					}
				}, function(chart){
					for(j=0;j<chart.series.length;j++){
					for(i=0;i<chart.series[j].data.length;i++){
				    Highcharts.addEvent(chart.series[j].data[i].tracker.element, 'dblclick', function(e){
				    	if(chartDrawObj.drillLevel == (chartDrawObj.categories.length-1)) return;//no more levels to drill into
										chartDrawObj.drillLevel ++;
										chartDrawObj.drillValue.push(selectedValue);
										chartDrawObj.count = createChart.getDrillChart(chart, chartDrawObj, chartDrawObj.drillValue, chartDrawObj.categories[chartDrawObj.drillLevel]);
										
				    });
				    
				   }
				  }
				    
				}
				);
				}  
				jQuery("div[id="+chartContainerId+"]").each(function(ind,obj){
					  if(ind == 1){
					  	$(this).remove();
				      }
				});	
}
function ChartJson(data){
	this.data = data;	
	this.utils = new Utils();
	this.nodes = [];
	this.nodesMap = new Object();
	this.position = $.data($('#new-visualization-dialogue')[0], 'position');
}
ChartJson.prototype.getVisualDefJson = function(chartDrawObj, chartAction){
	var visualDefJson = new Object();
	visualDefJson['bundleDefs'] = [];
	visualDefJson['class'] = "csi.server.common.model.DrillDownChartViewDef";
	
	var clientProps = new Object();
	clientProps['render.threshold'] = 2000;
	clientProps['vizBox.height'] = 500;
	clientProps['vizBox.left'] = 383;
	//not implemented yet
	//clientProps['vizBox.loadOnStartup'] = chartObj!=null?true:$('#drillChartModal').find('#loadOnStartUp').is(":checked");
	clientProps['vizBox.top'] = 10;
	clientProps['vizBox.width'] = 800;
	visualDefJson['clientProperties'] = clientProps;
	
	/*var cell = new Object();
	cell['clientProperties'] = {}
	cell['dimensionFields'] = [this.getDimensionFieldsJson(chartDrawObj.measures[0],'COLUMN_REF','string')]
	cell["dimName"] = chartDrawObj.measures[0];
	cell["sortOrder"] = "ASC";
	cell['ordinal'] = 0;
	//cell['uuid'] = this.utils.guidGenerator();
	visualDefJson['cell'] = cell;*/
	
	visualDefJson['dimensions'] = [];
	//visualDefJson['dimensions'].push(this.getDimensionJson('type of candy','COLUMN_REF','string'));
	for(i=0; i<chartDrawObj.categories.length; i++){
		visualDefJson['dimensions'].push(this.getDimensionJson(chartDrawObj.categories[i],'COLUMN_REF','string'));
	}
	
	visualDefJson['metrics'] = [];
	for(i=0; i<chartDrawObj.measures.length; i++){
		visualDefJson['metrics'].push(this.getMetricsJson(chartDrawObj.measures[i],chartDrawObj.chartFunction[i], 'COLUMN_REF','string'));
	}
	
	visualDefJson['filterFields'] = [];
	visualDefJson['isAttached'] = false;
	
	visualDefJson['name'] = chartDrawObj.chartTitle;
	visualDefJson['type'] = "CHART";
	visualDefJson['chartType'] = chartDrawObj.chartType;
	//visualDefJson['chartFunction'] = chartDrawObj.chartFunction;
	if(chartAction.toString() == 'create'){
		visualDefJson['uuid'] = this.utils.guidGenerator();
		visualDefJson.position = this.position;
	}else if(chartAction.toString() == 'update'){
		visualDefJson['uuid'] = chartDrawObj.viz;
		visualDefJson.position = chartDrawObj.position;
	}
	
	
	return visualDefJson;
	
}

ChartJson.prototype.getMetricsJson = function(fieldName, metricfunction, fieldType, valueType){
	var metric = new Object();
	metric['class'] = "csi.server.common.model.chart.ChartMeasure";
	metric['clientProperties'] = {}
	//dimension['dimensionFields'] = [this.getDimensionFieldsJson(fieldName,fieldType, valueType)]
	metric['measureField'] = this.getFieldDefJson(fieldName, fieldType, valueType);
	metric["name"] = fieldName;
	metric["measureFunction"] = metricfunction;
	//metric["sortOrder"] = "ASC";
	metric['ordinal'] = 0;
	//dimension['uuid'] = this.utils.guidGenerator();
	return metric;
}

ChartJson.prototype.getDimensionJson = function(fieldName,fieldType, valueType){
	var dimension = new Object();
	dimension['class'] = "csi.server.common.model.chart.ChartField";
	dimension['clientProperties'] = {}
	//dimension['dimensionFields'] = [this.getDimensionFieldsJson(fieldName,fieldType, valueType)]
	dimension['dimension'] = this.getFieldDefJson(fieldName, fieldType, valueType);
	dimension["dimName"] = fieldName;
	dimension["sortOrder"] = "ASC";
	dimension['ordinal'] = 0;
	//dimension['uuid'] = this.utils.guidGenerator();
	return dimension;
}
/*
ChartJson.prototype.getDimensionFieldsJson = function(fieldName,fieldType, valueType){
	var dimensionFields = new Object();
	dimensionFields['class'] = "csi.server.common.model.DimensionField";
	dimensionFields['clientProperties'] = {}
	dimensionFields['ordinal'] = 0;
	//dimensionFields['uuid'] = this.utils.guidGenerator();
	dimensionFields['fieldDef'] = this.getFieldDefJson(fieldName, fieldType, valueType); 
	return dimensionFields;
}*/
ChartJson.prototype.getFieldDefJson = function(fieldName,fieldType, valueType){
	//needs to be fixed
	fieldName = fieldName.toString();
	//return this.createFieldDefJson(fieldName,fieldType, valueType);
	var fieldDefJson = this.data['resultData']['meta']['modelDef']['fieldDefMap'][fieldName.toLowerCase()];
	if(fieldName != null){
		fieldDefJson['fieldName'] = fieldName;
	}		
	return fieldDefJson;
}
ChartJson.prototype.createFieldDefJson = function(fieldName, fieldType, valueType){
	var fieldDefJson = new Object();
	fieldDefJson['cacheScale'] = 0;
	fieldDefJson['cacheSize'] = 0;
	fieldDefJson['class'] = "csi.server.common.model.FieldDef";	
	fieldDefJson['clientProperties'] = {} 	
	fieldDefJson['functionType'] = "CONCAT";
	fieldDefJson['functions'] = [this.getConcatFunctionJSON()];
	fieldDefJson['ordinal'] = 0;
	//fieldDefJson['uuid'] = this.utils.guidGenerator();	
	fieldDefJson['fieldName'] = fieldName;
	fieldDefJson['fieldType'] = fieldType;
	fieldDefJson['valueType'] = valueType;
	return fieldDefJson;
	
}
ChartJson.prototype.getConcatFunctionJSON = function() {
	var cf = new Object();
	cf["class"]="csi.server.common.model.ConcatFunction";
	cf["clientProperties"] = new Object();
	cf["fields"]=[];
	cf["name"] = null;
	cf["ordinal"] = 0;
	cf["separator"] = "&#32;";
	cf["uuid"]=this.utils.guidGenerator();
	return cf;
}
function ChartUtils() {
}
ChartUtils.prototype.getDimensions = function(dimensionsObj){
	var dimensions = [];
	$.each(dimensionsObj, function(index, dim){
		dimensions.push(dim.dimName);
	});
	return dimensions;
}
ChartUtils.prototype.getMetrics = function(metricObj){
	var metrics = [];
	$.each(metricObj, function(index, metric){
		metrics.push(metric.name);
	});
	return metrics;
}
ChartUtils.prototype.getChartFunction = function(metricObj){
	var metrics = [];
	$.each(metricObj, function(index, metric){
		metrics.push(metric.measureFunction);
	});
	return metrics;
}
/*ChartUtils.prototype.getCell = function(cellObj){
	var cell = [];
	cell.push(cellObj.dimName);
	return cell;
}*/
ChartUtils.prototype.createChartDrawObject = function(drillLevel, containerId, worksheetId, vizId, position, vizIndex, title, chartType, chartFunction, category, measure){
	var cdo = new Object();
	cdo.drillLevel = drillLevel;
	cdo.drillValue = [];
	cdo.container = containerId;
	cdo.worksheet = worksheetId;
	cdo.position = position;
	cdo.vizIndex = vizIndex;
	cdo.viz = vizId;
	cdo.chartTitle = title;
	cdo.chartType = chartType; 
	cdo.chartFunction = chartFunction;
	cdo.categories = category;
	cdo.measures = measure;
	cdo.count = 0;
	cdo.imageUrl = "";
	return cdo;
}
ChartUtils.prototype.recreateChart = function(chartObj){
	var createChart = new CreateChart();
	var chartDrawObj = 	this.createChartDrawObject(0, 'layout' + chartObj.wsIndex + '_chart' + chartObj.position, chartObj.wsIndex, chartObj.viz, chartObj.position, chartObj.vizIndex,
					chartObj.name, chartObj.chartType, chartObj.chartFunction, chartObj.dimensions, chartObj.cell);
	createChart.addVisualization(chartDrawObj, 'load');
}
