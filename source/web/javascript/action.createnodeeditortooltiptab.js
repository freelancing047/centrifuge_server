function NodeEditorToolTipTab(nodeEditorDiv, nodeName, vizdef ){
	this.nodeEditorDiv = nodeEditorDiv;
	this.nodeName = nodeName;
	this.vizDef = vizdef;
	this.isValid = true;
}

NodeEditorToolTipTab.prototype.populateTabInfo = function(){
	this.initializeTab();
	this.getTableData();
}

NodeEditorToolTipTab.prototype.initializeTab = function(){
	var nodeEditorToolTipTab = this;
	var nodeEditorDiv = this.nodeEditorDiv;
	var oTable = this.nodeEditorDiv.find('#tooltipfields-table').dataTable({
	"aoColumns" : [{
		"sTitle" : "Display Name",
		"mDataProp": "Display" ,
        "sWidth": "160px",
	 }, {
		"sTitle" : "Static",
		"mDataProp": "Static",   
        "sWidth": "35px",
        "sClass":"node_tt_static",
		"fnRender" : function(obj) {
			var sReturn = "";
			if(obj.aData['Static']){
			    sReturn = $('<input></input>').attr('type','checkbox').val('').attr('checked','checked').attr('id', '2')[0].outerHTML;
			}
			else{	
			    sReturn = $('<input></input>').attr('type','checkbox').val('').attr('id', '2')[0].outerHTML;
			}
			return sReturn;
	    }
	},  {
		"sTitle" : "Field/Value",
		"mDataProp": "Field",
        "sWidth": "160px",
			
	}, {
		"sTitle" : "Delete",
		"mDataProp": "Delete",
        "sWidth": "35px",
        "sClass":"node_tt_delete",
		"fnRender" : function(obj) {
			var sReturn = $('<img></img>').attr('src', '../images/node-delete.png').attr('id', '5')[0].outerHTML;
			return sReturn;
		}
	}],
	"fnCreatedRow": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {	
	    var rowdata = aData;
	    // Delete row
		$(nRow).find("#5").click(function() {
			var nTd = this;
			if(_.isEqual($(nRow).parent().find('tr').length,1)){
				nodeEditorToolTipTab.isValid = true;
				nodeEditorToolTipTab.nodeEditorDiv.find('#treeFinish-ne').trigger('change');
			}
			else{
				nodeEditorToolTipTab.isValid = true;
				nodeEditorToolTipTab.nodeEditorDiv.find('#treeFinish-ne').trigger('change');
				$.each($(nRow).parent().find('tr'),function(){
					if(_.isEmpty($(this).find('td:eq(0)').text()) &&  !_.isEqual($(this)[0],$(nTd).parent().parent()[0]) ){
						nodeEditorToolTipTab.isValid = false;
						nodeEditorToolTipTab.nodeEditorDiv.find('#treeFinish-ne').trigger('change');
					}
				});
    		}
			nodeEditorToolTipTab.doDeleteToolTip(oTable, nRow);
		});	
		//Editor work click handling
		$(nRow).find("#2").click(function() {
			nodeEditorToolTipTab.doChangeStaticTextCheckBox(oTable, nRow, rowdata.fieldoptions);
		});													
		$(nRow).find('td:eq(0)').live( 'click', function () {	
			nodeEditorToolTipTab.doEditDisplayName(oTable, nRow);
		});
		$(nRow).find('td:eq(2)').live( 'click', function () {
			nodeEditorToolTipTab.doEditFieldName(oTable, nRow, rowdata.fieldoptions);
		});  
		$(nRow).find('td:eq(0)').find('#displayedit').live( 'focusout', function () {
			nodeEditorToolTipTab.doFocusOutDisplayName(oTable, this);
			nodeEditorToolTipTab.isValid = true;
			nodeEditorToolTipTab.nodeEditorDiv.find('#treeFinish-ne').trigger('change');
			$.each($(nRow).parent().find('tr'),function(){
				if(_.isEmpty($(this).find('td:eq(0)').text())){
					nodeEditorToolTipTab.isValid = false;
					nodeEditorToolTipTab.nodeEditorDiv.find('#treeFinish-ne').trigger('change');
				}
			});
		});			    	  		  
		$(nRow).find('td:eq(2)').find('#fieldedit').live( 'focusout', function () {
			nodeEditorToolTipTab.doFocusOutFieldName(oTable, this);
	    });
        $(nRow).find('td:eq(2)').find('#fieldselect').live( 'focusout', function () {
        	nodeEditorToolTipTab.doFocusOutFieldName(oTable, this);
        });							
	},
		"sScrollY" : "100px",
		"aaData" : [],
                "bAutoWidth": false,
		"sDom" : '<flp><"clear">',
		"bPaginate" : false,
		"aaSorting": []
	});	
	this.nodeEditorDiv.find('#addTooltipBtn').click(function(){
		var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;	
		var columnoptions = utils.getFieldNameColumnOptions(columns);
		var oTable = nodeEditorDiv.find('#tooltipfields-table').dataTable();
		var data = {};
		data.Display = "";
		data.Static = false;
		data.Field = columns[0].fieldName;
		data.Delete = "";
		data.StaticText = "";
		data.fieldoptions = columnoptions;
		oTable.fnAddData(data);
		oTable.find('tr:last td:first').trigger('click');
		nodeEditorToolTipTab.isValid = false;
		$(nodeEditorDiv).find('#treeFinish-ne').trigger('change');
	});
}

NodeEditorToolTipTab.prototype.getTableData = function(){
	var vizDef = this.vizDef;
	var nodeEditorDiv = this.nodeEditorDiv;
	var nodeName = this.nodeName;
	var nodeDefs = vizDef.nodeDefs;
	var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;	
	var oTable = nodeEditorDiv.find('#tooltipfields-table').dataTable();
	var columnoptions = utils.getFieldNameColumnOptions(columns);
	var attributeDefs = "";
	$.each(nodeDefs,function(){
		if(this.name == nodeName){
			attributeDefs=this.attributeDefs;
		}	
	});
	$.each( attributeDefs, function() {
		if(!((this.name).match("^csi.internal"))){
			if(this.kind == null){
				var data = {};
				if(this.fieldDef.staticText != null){
					data.Field = this.fieldDef.staticText;
				}
				else{
					data.Field = this.fieldDef.fieldName;
				}
				data.Display = this.name;						
				data.Static = this.fieldDef.staticText != null ? true : false;//if not null--> checkbox checked													
				data.Delete = "";					
				data.StaticText =  this.fieldDef.staticText;
				data.fieldoptions = columnoptions;
				oTable.fnAddData(data);
			}
		}
	});
}

NodeEditorToolTipTab.prototype.doSave = function(vizdef){
	var nodeDefs = vizdef.nodeDefs;
	var attrDefs = [];
	var nodeName = this.nodeName;
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
    var oTable = this.nodeEditorDiv.find('#tooltipfields-table').dataTable();
    if (oTable.fnSettings() == null){
		return;
	}
    var nTr = oTable.fnGetNodes();        
    $.each(nTr, function(){
	    var uuid = utils.guidGenerator();
	    var staticText = null;
	    var fieldName = "";
	    var fieldval = ""; 
	    if($(this).find('td:eq(2)').find('select').length > 0){ 
	    	fieldval = $(this).find('td:eq(2)').find('select').val();
	    }
	    else{
	    	fieldval = $(this).find('td:eq(2)').find('input').length > 0 ? $(this).find('td:eq(2)').find('input').val() : $(this).find('td:eq(2)').text();
	    }
	    if($(this).find('#2')[0].checked){
	    	staticText = fieldval;            
	    }    
	    else{            	
	    	fieldName = fieldval;
	    }           
	    if(staticText == null){            	
	    	 var fieldDef = utils.getFieldDef(fieldDefs,fieldName);
	    }
	    else{                     	           
	    	var fieldDef =  utils.getFieldDefByStaticText(fieldDefs,staticText);
	    	if(fieldDef == null){
	    		var newfielddef = {};
	    		newfielddef.anonymous = false;
	    		newfielddef.cacheOrdinal = null;
	    		newfielddef.cacheScale = 0;
	    		newfielddef.cacheSize = 0;
	    		newfielddef.cacheType = null;
	    		newfielddef.class = "csi.server.common.model.FieldDef";
	    		newfielddef.clientProperties = {};
	    		newfielddef.columnLocalId = null;
	    		newfielddef.columnName = null;
	    		newfielddef.displayFormat = null;
	    		newfielddef.dsLocalId = null;
	    		newfielddef.dsLocalName = null;
	    		newfielddef.fieldName = "";
	    		newfielddef.fieldType = "STATIC";
	    		newfielddef.functionType = "CONCAT";
	    		newfielddef.functions = [];
	    		newfielddef.functions.class = "csi.server.common.model.ConcatFunction";
	    		newfielddef.functions.clientProperties = {};
	    		newfielddef.functions.fields = [];
	    		newfielddef.functions.name = null;
	    		newfielddef.functions.ordinal =  0;
	    		newfielddef.functions.separator = "&#32;";
	    		newfielddef.functions.sortedFields = [];
	    		newfielddef.functions.uuid = utils.guidGenerator();
	    		newfielddef.hiddenConditional = null;
	    		newfielddef.ordinal = 20;
	    		newfielddef.rawScript = false;
	    		newfielddef.scriptSeparator = null;
	    		newfielddef.scriptText = null;
	    		newfielddef.scriptType = null;
	    		newfielddef.staticText = staticText;
	    		newfielddef.tableLocalId = null;
	    		newfielddef.uuid = utils.guidGenerator();
	    		newfielddef.valueType = null;
	    		fieldDef = newfielddef;
	    	}
	    }            
	    var name = $(this).find('td:eq(0)').find('input').length > 0 ? $(this).find('td:eq(0)').find('input').val() : $(this).find('td:eq(0)').text();                     
	    var newAttrDef = {};
	    newAttrDef.aggregateFunction = null;
	    newAttrDef.class = "csi.server.common.model.attribute.AttributeDef";
	    newAttrDef.bySize = true;
		newAttrDef.byTransparency = false;
		newAttrDef.byStatic = true;
	    newAttrDef.clientProperties = {};
	    newAttrDef.defaultIncludeInTooltip = true;
	    newAttrDef.fieldDef = fieldDef;         
	    newAttrDef.hideEmptyInTooltip = false;
	    newAttrDef.includeInTooltip = true;
	    newAttrDef.kind = null;
	    newAttrDef.name = name;
	    newAttrDef.referenceName = null;
	    newAttrDef.uuid = uuid;
	    attrDefs.push(newAttrDef);           
    });  
    var nodeDefIdx = utils.getNodeDefIdx(nodeDefs, nodeName);
    $.each(nodeDefs[nodeDefIdx].attributeDefs, function() {
        if(this.kind == null && (this.name.match("^csi.internal"))!=null){
        	attrDefs.push(this);
        }
    });
    vizdef.nodeDefs[nodeDefIdx].attributeDefs = attrDefs;
    return vizdef;
}

NodeEditorToolTipTab.prototype.triggerFocusOut = function(oTable , iDisplayIndex){
	var iDisplayIndex = iDisplayIndex;
	var oTable = oTable;
	var length = oTable.find('tr').length;
	if(length > 1){
		for(var i = 0; i < length-1; i++){
			  if(i != iDisplayIndex){
				  oTable.find('tbody tr:eq('+i+') td:eq(0) > input[type="text"]').focusout();
				  oTable.find('tbody tr:eq('+i+') td:eq(2)').find('select').focusout();
				  oTable.find('tbody tr:eq('+i+') td:eq(2) > input[type="text"]').focusout();
			  }
		}
    }
}

NodeEditorToolTipTab.prototype.doEditDisplayName = function(oTable, nRow){	
   var index;
   if(oTable) {	
	   index = oTable.fnGetPosition(nRow);
	   this.triggerFocusOut(oTable, index);
   }
   $(nRow).find('td:eq(2)').find('#fieldselect').focusout();
   $(nRow).find('td:eq(2)').find('#fieldedit').focusout();												 
   var data = $(nRow).find('td:eq(0)')[0].innerHTML;
   if($(nRow).find('#displayedit').length > 0){
	 return;
   }
   $(nRow).find('td:eq(0)').html($('<input></input>').attr('type','text').addClass('span2').attr('id','displayedit').val(data));
   $(nRow).find('td:eq(0) input[type="text"]').focus();
}

NodeEditorToolTipTab.prototype.doChangeStaticTextCheckBox = function(oTable, nRow, fieldOptions){	
   var index = oTable.fnGetPosition(nRow);
   if($(nRow).find('#2')[0].checked){
	   oTable.fnUpdate("", index, 2);
	   $(nRow).find('td:eq(2)').find('#fieldselect').focusout(); 													
   }
   else{
	   oTable.fnUpdate($(fieldOptions)[0].value, index, 2);
	   $(nRow).find('td:eq(2)').find('#fieldedit').focusout();													 
   }
}

NodeEditorToolTipTab.prototype.doEditFieldName = function(oTable, nRow, fieldOptions){	
   if(oTable) {	
	   var index = oTable.fnGetPosition(nRow);
	   this.triggerFocusOut(oTable, index);
   }
   $(nRow).find('td:eq(0)').find('#displayedit').focusout();												   
   var data = $(nRow).find('td:eq(2)')[0].innerHTML;
   if($(nRow).find('#fieldedit').length > 0 || $(nRow).find('#fieldselect').length > 0){
	   return;
   }
   if($(nRow).find('#2')[0].checked){
	   $(nRow).find('td:eq(2)').html($('<input></input>').attr('type','text').addClass('span2').attr('id','fieldedit').val(''));
   }  	
   else{															 
	   $(nRow).find('td:eq(2)').html($('<select></select').attr('id','fieldselect').addClass('span2').append(fieldOptions));													 
	   $(nRow).find('td:eq(2)').find('#fieldselect option[value="'+data+'"]').attr("selected", "selected");													   
   }
}

NodeEditorToolTipTab.prototype.doDeleteToolTip = function(oTable, nRow){	
   var index = oTable.fnGetPosition(nRow);
   oTable.fnDeleteRow(index);
}

NodeEditorToolTipTab.prototype.doFocusOutFieldName = function(oTable, nTd){
   if($(nTd).val() != null){
	   if($(nTd).parent()[0] != null){
		   var index = oTable.fnGetPosition($(nTd).parent().parent()[0]);
		   var newval = $(nTd).val();
		   $(nTd).html('');
		   oTable.fnUpdate(newval, index, 2);
	   }
   }
}

NodeEditorToolTipTab.prototype.doFocusOutDisplayName = function(oTable, nTd){	
		if($(nTd).val() != null){
			if($(nTd).parent()[0] != null){
				var index = oTable.fnGetPosition($(nTd).parent().parent()[0]);
			   	var newval = $(nTd).val();
			   	$(nTd).html('');
			   	oTable.fnUpdate(newval, index, 0);
		   	}
		}
}

NodeEditorToolTipTab.prototype.findIsValid = function(){
	return this.isValid;
}