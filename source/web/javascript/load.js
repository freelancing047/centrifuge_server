function Load() {
}

Load.prototype.doLoad = function() {
        
	$('#xls').html( '<table cellpadding="0" cellspacing="0" border="0" width: "100%" class="display" id="xlslist"></table>' );
	this.registerLogout();
	this.listRecentlyOpenedDataView();
	this.createDataTable();
	this.registerFileUpload();
	this.getDatafiles();
	this.createTree();
	this.registerEventListener();
}

Load.prototype.createDataView = function() {
	var tableName = $('#worksheetName').text();
	var url = "/Centrifuge/actions/test/listColumnDefs?_f=json&type="+tableName;//type=value of the node that is activated
	var ej = new ExcelJson();
	var data = ej.getConnectionDefJSON($('#inputExcelToken').val(), $('#inputExcel').val());    

	$.ajax({
	type: "POST",
	processData: false,
	url: url ,
	contentType: 'application/json; charset=utf-8',
	dataType: 'json',
	data: data,
	complete: function(data) {
			columns = JSON.parse(data.responseText).resultData;
			sourceId = $('#inputExcelToken').val();
			sourceName = $('#inputExcel').val();
			tableName = $('#worksheetName').text();
			dataviewName = $('#dvName').val();
			dvJSON  = new DataViewExcelJson(sourceName, sourceId, tableName, columns, dataviewName);
			dv = JSON.stringify(dvJSON.getDVJSON());
			$.data(document.body, 'load').doSaveDataView(dv);
		}
	});
}
Load.prototype.doSaveDataView = function(dv) {
	var url = '/Centrifuge/actions/dataview/launch?_f=json';
	$.ajax({
		type: "POST",
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: dv,
		complete: function(data){
			$('#myModal8').modal('hide');
			$.data(document.body, 'load').listRecentlyOpenedDataView();
		}
		});
}

Load.prototype.createTree = function() {
	$("#dataSourceTree").dynatree({
		  onLazyRead: function(node) {
			  if(node.data['isWorksheet']) {
				  load.getTableDefs(node)	  
			  } else if (node.data['isTable']) {
				  load.getColumnDefs(node)
			  }
		  },
	    onActivate: function(node) {
	      if(node.data['isTable']) {
	      	$('#worksheetName').text(node.data.title);
	      	$("#treeFinish").removeAttr("disabled");
	      } else {
	      	$('#worksheetName').text('<None>');
    		$("#treeFinish").attr("disabled", "disabled");
	      }
	    }
	});
}


Load.prototype.registerLogout = function() {
	$("#log-out").click(function() {
		$.get("/Centrifuge/actions/user/logout", function(data) {
			location.reload();
		});
		return false;
	});
}

Load.prototype.listRecentlyOpenedDataView = function() {
	$.get('/Centrifuge/actions/dataview/listRecentlyOpenedDataViews?_f=json',function(data){
		$("#container-scrollpane").html('');
		$.each(data['resultData'], function(index, value) {
			   $("#container-scrollpane").append('<div id='+ value['uuid'] + ' class="container-fluid"><div class="thumbnail2"></div><b>' + value['name']+ '</b><br/>Please view Read more.. before reading this</div>');
			   $("#" + value['uuid']).click(function (event) { 
				   id = value['uuid'];
				   if (id != undefined) {
					   $('#uuid').val(id);
					   $.cookie('DataViewName',value['name'],{path:'/'});
					   $('#open-data-view').submit();
				   }
			   });
		});
	});
}

Load.prototype.createDataTable = function() {
	 var oTable = $('#xlslist').dataTable({
			"bFilter": false,
			"bPaginate":false,
			"sDom": '<flp><"clear">',
			"aoColumns": [
							{ 	"sTitle":"File Name",
								"mDataProp": "@name",
								"fnRender": function(obj) {
									var sReturn = obj.aData["@name"];
									return sReturn; 
								}
							},
							{ 	"sTitle":"Last Date",
								"mDataProp": "@lastmodified",
								"fnRender": function(obj) {
									var sReturn = obj.aData["@lastmodified"];
									return sReturn; 
								}
							}
						]
		 });
}

Load.prototype.registerFileUpload = function(){
	$('#fileUpload').ajaxForm({ complete: this.showResponse  });
    $('#fileUpload').change(function(){
    	$('#fileName').val($('#file').val().replace(/C:\\fakepath\\/i, ''));
        $('#fileUpload').attr('action', "/Centrifuge/services/uploadfile?"+$(this).serialize());            
        $(this).submit();
    });            
}
Load.prototype.registerEventListener = function(){
	$('#dvName').bind('keydown', function(){
		if ($('#dvName').val()!= null) {
			$("#next1").removeAttr("disabled");
			$.get('/Centrifuge/actions/dataview/listRecentlyOpenedDataViews?_f=json',function(data){
				$('#name-exist').remove();
				$.each(data['resultData'], function(index, value) {
					if(value['name'] == $('#dvName')[0].value)	{
						$("#next1").attr("disabled", "disabled");
						$('#div-dataview').append('<p id="name-exist" class="alertBox">Name already exists</p>');
					} 
				});
			});
		}
    });         
    $("#treeFinish").click(function(){
    	$.data(document.body, 'load').createDataView();
    });
}

Load.prototype.showResponse = function(responseText, statusText, xhr)  {
	$("#selectButton").attr("disabled", "disabled");
	$.data(document.body, 'load').getDatafiles();
}

Load.prototype.getDatafiles = function(){
	$.get('/Centrifuge/services/getfileinfo/datafiles',function(data){
		var oTable = $('#xlslist').dataTable();		
		if($(data).find('file') != []){
			var files = $(data).find('file');
			var filter_data = [];
			$.each(files, function(key,value){
				var fileDetail ={};					
				if($(value).attr('name').match(".xls$")){
					fileDetail['@lastmodified'] = $(value).attr('lastmodified');
					fileDetail['@name'] = $(value).attr('name');
					fileDetail['@path'] = $(value).attr('path');
					fileDetail['@size'] = $(value).attr('size');
					fileDetail['@token'] = $(value).attr('token');
					fileDetail['@urltoken'] = $(value).attr('urltoken');
					filter_data.push(fileDetail);
				}
			});	
			oTable.fnClearTable();
			oTable.fnAddData(filter_data);
			$("#xlslist tbody tr").click( function( e ) {
				if ( $(this).hasClass('row_selected') ) {
					$(this).removeClass('row_selected');
					$("#selectButton").attr("disabled", "disabled");
					$("#next2").attr("disabled", "disabled");
				}
				else {
					oTable.$('tr.row_selected').removeClass('row_selected');
					$(this).addClass('row_selected');
					$("#selectButton").removeAttr("disabled");
					$("#next2").removeAttr("disabled");
				}
			});
		}
	});			
}

Load.prototype.getSelectedXLS = function()
{
	var oTable = $('#xlslist').dataTable();
	var aTrs = oTable.fnGetNodes();
	var aLoad = this;
	$.each(aTrs, function(index, value) {
		if ( $(value).hasClass('row_selected') ){
			var addElement = $('#xlslist').dataTable().fnGetData(value);
			$('#inputExcel').val(addElement["@name"]);
			$('#inputExcelToken').val(addElement["@token"]);
			$('#myModal7').modal('hide');
			aLoad.showWS();
			return;
		}
	});
}

Load.prototype.showWS = function()  {
	var utils = new Utils();
	var ej = new ExcelJson();
	clientID = utils.guidGenerator();
	taskID = utils.guidGenerator();
	var url = "/Centrifuge/actions/test/listTableTypes?_f=json";
	var data = ej.getConnectionDefJSON($('#inputExcelToken').val(), $('#inputExcel').val());
	$("#dataSourceTree").dynatree("getRoot").removeChildren();
	var loader = this;
	$.ajax({
		  type: "POST",
		  processData: false,
		  url: url,
		  contentType: 'application/json; charset=utf-8',
		  dataType: 'json',
		  data: data,
		  success: function(data) {
			  $.each(data.resultData, function(index, value) {
				  var treeDta = new Object();
				  treeDta["title"] = value;
				  treeDta["isLazy"] = true;
				  treeDta["isWorksheet"] = true;
				  $("#dataSourceTree").dynatree("getRoot").addChild(treeDta);
			  });
		  },
		  error: function(data) {
		  	bootbox.alert(data)
		  }
	});
	return;
} 

Load.prototype.getTableDefs = function(node){
	var url = "/Centrifuge/actions/test/listTableDefs?_f=json&type="+node.data.title;//type=value of the node that is activated
	var ej = new ExcelJson();
	var data = ej.getConnectionDefJSON($('#inputExcelToken').val(), $('#inputExcel').val());    
	var buildNode = function(node) {
	    return function(data) {
			$.each(JSON.parse(data.responseText).resultData, function(index, value){
				  var treeDta = new Object();
				  treeDta["title"] = value['tableName'];
				  treeDta["isLazy"] = true;
				  treeDta["isTable"] = true;
				  node.addChild(treeDta);
			});
	    };
	};

	$.ajax({
	type: "POST",
	processData: false,
	url: url ,
	contentType: 'application/json; charset=utf-8',
	dataType: 'json',
	data: data,
	complete: buildNode(node)
	});
}

Load.prototype.getColumnDefs = function(node){
	var url = "/Centrifuge/actions/test/listColumnDefs?_f=json&table="+node.data.title;//table=value of the table(ie; the value of the node activated) 
	var ej = new ExcelJson();
	var data = ej.getConnectionDefJSON($('#inputExcelToken').val(), $('#inputExcel').val());   
	var buildNode = function(node) {
	    return function(data) {
			$.each(JSON.parse(data.responseText).resultData, function(index, value){
				  var treeDta = new Object();
				  treeDta["title"] = value['columnName'];
				  treeDta["isLazy"] = false;
				  node.addChild(treeDta);
			});
	    };
	};

	$.ajax({
	type: "POST",
	processData: false,
	url: url ,
	contentType: 'application/json; charset=utf-8',
	dataType: 'json',
	data: data,
	complete: buildNode(node)
	});
}
