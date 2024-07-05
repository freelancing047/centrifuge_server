function LinkEditorDirection(modalDiv,linkName, vizdef) {
	this.linkName = linkName;
	this.vizDef = vizdef;
	this.modalDiv = modalDiv;
	this.dirDef = null;
	this.index = 0;
	this.cols = null;
	this.linkId = null;
	this.isValid = true;
}

LinkEditorDirection.prototype.populateTabInfo = function(sourcedetail,targetdetail) {
	var visualization = this.vizDef;
	var linkName = this.linkName;
	var linkEditorDirection = this;
	var linkDefs = visualization.linkDefs;
	var modalDiv = this.modalDiv;
	var dirDef = null;
	var source = sourcedetail.attr('title');
	var target = targetdetail.attr('title');
	var sourcenodeurl = $(sourcedetail).css('background-image').match(/(((ftp|https?):\/\/)[\-\w@:%_\+.~#?,&\/\/=]+)|((mailto:)?[_.\w-]+@([\w][\w\-]+\.)+[a-zA-Z]{2,3})/g);
	var targetnodeurl = $(targetdetail).css('background-image').match(/(((ftp|https?):\/\/)[\-\w@:%_\+.~#?,&\/\/=]+)|((mailto:)?[_.\w-]+@([\w][\w\-]+\.)+[a-zA-Z]{2,3})/g);
	modalDiv.find('#source_node').attr("src",sourcenodeurl);
	modalDiv.find('#target_node').attr("src",targetnodeurl);
	linkEditorDirection.populateColumnsDropDown(linkEditorDirection.modalDiv);
	linkEditorDirection.modalDiv.find('#dynamic-div').hide();
	$.each(linkDefs, function(key, value) {
		if(value.nodeDef1.name == source && value.nodeDef2.name==target){
			dirDef = value.directionDef;
			linkEditorDirection.index = key;
			linkEditorDirection.dirDef = dirDef;
			linkEditorDirection.linkId = value.uuid;
		}
	});
	if(dirDef != null){
			var stext = dirDef.fieldDef.staticText;
			if (stext == "FORWARD"){
				modalDiv.find('#forward').attr('checked',true);
				modalDiv.find('#right_dir').parent().removeClass('disabled');
				modalDiv.find('#left_dir').parent().addClass('disabled');
				modalDiv.find('#quest').parent().css('display','none');
			}
			else if (stext == "REVERSE" ){
				modalDiv.find('#reverse').attr('checked',true);
				modalDiv.find('#left_dir').parent().removeClass('disabled');
				modalDiv.find('#right_dir').parent().addClass('disabled');
				modalDiv.find('#quest').parent().css('display','none');
			}
			else if (stext == null){
				modalDiv.find('#dynamic').attr('checked',true);
				modalDiv.find('#dynamic-div').show();
				modalDiv.find('#direction-field').val(dirDef.fieldDef.fieldName);
				this.getFilterConstraints(modalDiv, dirDef.fieldDef.fieldName);
			}
	}
	else if (dirDef == null){
		modalDiv.find('#undirected').attr('checked',true);
		modalDiv.find('#right_dir').parent().addClass('disabled');
		modalDiv.find('#left_dir').parent().addClass('disabled');
		modalDiv.find('#quest').parent().css('display','none');
	}
	modalDiv.find('input[type="radio"]').click(function(){
		linkEditorDirection.modalDiv.find('#dynamic-div').hide();
		linkEditorDirection.isValid = true;
		modalDiv.find('#treeFinish').trigger('change');
		if(modalDiv.find('#undirected').is(':checked')){
			linkEditorDirection.dirDef = null;
			modalDiv.find('#right_dir').parent().addClass('disabled');
			modalDiv.find('#left_dir').parent().addClass('disabled');
			modalDiv.find('#quest').parent().css('display','none');
		}
		else if(modalDiv.find('#forward').is(':checked')){
			linkEditorDirection.dirDef = linkEditorDirection.getDirDef("FORWARD");
			modalDiv.find('#right_dir').parent().removeClass('disabled');
			modalDiv.find('#left_dir').parent().addClass('disabled');
			modalDiv.find('#quest').parent().css('display','none');
		}
		else if(modalDiv.find('#reverse').is(':checked')){
			linkEditorDirection.dirDef = linkEditorDirection.getDirDef("REVERSE");
			modalDiv.find('#left_dir').parent().removeClass('disabled');
			modalDiv.find('#right_dir').parent().addClass('disabled');
			modalDiv.find('#quest').parent().css('display','none');
		}
		else if(modalDiv.find('#dynamic').is(':checked')){
			linkEditorDirection.isValid = false;
			modalDiv.find('#treeFinish').trigger('change');
			linkEditorDirection.dirDef = linkEditorDirection.getDirDef("DYNAMIC");
			var fieldName = modalDiv.find('#direction-field').val();
			linkEditorDirection.modalDiv.find('#dynamic-div').show();
			modalDiv.find('#quest').parent().css('display','table');
			modalDiv.find('#right_dir').parent().addClass('disabled');
			modalDiv.find('#left_dir').parent().addClass('disabled');
			if(modalDiv.find('#dynamic-forward' + linkEditorDirection.linkId).val() || modalDiv.find('#dynamic-reverse' + linkEditorDirection.linkId).val()){
				linkEditorDirection.isValid = true;
				modalDiv.find('#treeFinish').trigger('change');
			}
		}
	});
	
	modalDiv.find('#direction-field').change(function(){
		var fieldName = modalDiv.find('#direction-field').val();
		linkEditorDirection.isValid = false;
		modalDiv.find('#treeFinish').trigger('change');
		linkEditorDirection.dirDef = linkEditorDirection.getDirDef("DYNAMIC");
		linkEditorDirection.dirDef.forwardValues = [];
		linkEditorDirection.dirDef.reverseValues = [];
		linkEditorDirection.getFilterConstraints(modalDiv,fieldName);
	});
	modalDiv.find('#dynamic-forward' + this.linkId).change(function() {
		linkEditorDirection.handleDropdownMenus(modalDiv);
	});
	modalDiv.find('#dynamic-reverse'  +this.linkId).change(function(event) {
		linkEditorDirection.handleDropdownMenus(modalDiv);
	});
}

LinkEditorDirection.prototype.doSave = function(vizDef) {
	var linkEditorDirection = this;
	if(linkEditorDirection.modalDiv.find('#dynamic').is(':checked')){
		linkEditorDirection.dirDef = linkEditorDirection.getDirDef("DYNAMIC");
	}
	vizDef.linkDefs[linkEditorDirection.index].directionDef = linkEditorDirection.dirDef;
	return vizDef;
}

LinkEditorDirection.prototype.getDirDef = function(staticText) {
	var modalDiv = this.modalDiv;
	var dirDef = {};
	dirDef.class = "csi.server.common.model.attribute.DirectionDef";
	dirDef.clientProperties = {}; 
	dirDef.forwardValues = null;	//values from modalDiv.find('#dynamic-forward') 
	dirDef.reverseValues = null;	//values from modalDiv.find('#dynamic-reverse')
		if(modalDiv.find('#dynamic').is(':checked')){
			dirDef.forwardValues = modalDiv.find('#dynamic-forward' + this.linkId).val();
			dirDef.reverseValues = modalDiv.find('#dynamic-reverse' + this.linkId).val();
		}
	dirDef.uuid = utils.guidGenerator();
	dirDef.fieldDef = {};
	if(staticText == "FORWARD" || staticText == "REVERSE"){
		dirDef.fieldDef.anonymous = false;
		dirDef.fieldDef.cacheScale = 0;
		dirDef.fieldDef.cacheSize = 0;
		dirDef.fieldDef.class = "csi.server.common.model.FieldDef";
		dirDef.fieldDef.clientProperties = {};
		dirDef.fieldDef.fieldName = "";
		dirDef.fieldDef.fieldType = "STATIC";
		dirDef.fieldDef.functionType = "CONCAT";
		dirDef.fieldDef.functions = [];
		dirDef.fieldDef.functions.class = "csi.server.common.model.ConcatFunction";
		dirDef.fieldDef.functions.clientProperties = {};
		dirDef.fieldDef.functions.fields = [];
		dirDef.fieldDef.functions.name = null;
		dirDef.fieldDef.functions.ordinal =  0;
		dirDef.fieldDef.functions.separator = "&#32;";
		dirDef.fieldDef.functions.sortedFields = [];
		dirDef.fieldDef.functions.uuid = utils.guidGenerator();
		dirDef.fieldDef.hiddenConditional = null;
		dirDef.fieldDef.ordinal = 20;
		dirDef.fieldDef.rawScript = false;
		dirDef.fieldDef.scriptSeparator = null;
		dirDef.fieldDef.scriptText = null;
		dirDef.fieldDef.scriptType = null;
		dirDef.fieldDef.staticText = staticText;
		dirDef.fieldDef.tableLocalId = null;
		dirDef.fieldDef.uuid = utils.guidGenerator();
		dirDef.fieldDef.valueType = null;
	}	
	else if(staticText =="DYNAMIC"){
		var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
		var fieldName = this.modalDiv.find('#direction-field').val();
		dirDef.fieldDef = utils.getFieldDef(fieldDefs,fieldName);
	}
	return dirDef;
}

LinkEditorDirection.prototype.populateColumnsDropDown = function(modalDiv){
	var columnsDropDown = modalDiv.find('#direction-field');
	var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (obj.fieldName == "" || obj.fieldName == null)});
	$.each(cols , function() {
		if(this.valueType  === "string"){
			columnsDropDown.append($('<option>').val(this.fieldName).append(this.fieldName)[0].outerHTML);
		}
	});
}

LinkEditorDirection.prototype.getFilterConstraints = function(modalDiv,fieldName){
	var linkDirection = this;
	var dirDef = this.dirDef;
	var forwardValues = dirDef.forwardValues ? dirDef.forwardValues : [];
	var reverseValues = dirDef.forwardValues ? dirDef.reverseValues : [];
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var fieldDef = utils.getFieldDef(fieldDefs,fieldName);
/*	request for ajax post call	*/
	var requestJSON = {};
	requestJSON.caseSensitive = false;
	requestJSON.class = "csi.server.common.dto.FilterConstraintsRequest";
	requestJSON.dvUuid = window.dataview.myData.resultData.uuid ;
	requestJSON.vizUuid = this.vizDef.uuid;
	requestJSON.filters = [];
		var filObj = {};
		filObj.class = "csi.server.common.model.filter.FilterField";
		filObj.clientProperties = {};
		filObj.endValue = null;
		filObj.field = fieldDef;
		filObj.selectedValues=[]
		filObj.ordinal = 0;
		filObj.startValue = null;
		filObj.uuid = utils.guidGenerator();
	requestJSON.filters.push(filObj);
	requestJSON.limit = 50;
/*	end of request	*/
	var url = "/Centrifuge/actions/viz/getFilterConstraints?_f=json";
	   var doSuccess = function(linkDirection) {
	        return function(data) {
	        	var cols = data.resultData[0].availableValues;
	        	linkDirection.cols = cols;
	        	var lid = (linkDirection.linkId).replace(/-/g,'_');
	        	modalDiv.find("#dynamic_forward" + lid + "_chzn").remove();
	        	modalDiv.find("#dynamic_reverse" + lid + "_chzn").remove();  
	        	var columnsDropDown1 = modalDiv.find('#dynamic-forward' + linkDirection.linkId).empty();
        		var columnsDropDown2 = modalDiv.find('#dynamic-reverse' + linkDirection.linkId).empty();
        		columnsDropDown1.removeClass("chzn-done");
        		columnsDropDown2.removeClass("chzn-done");
        		columnsDropDown1.chosen().trigger('liszt:updated');
	        	columnsDropDown2.chosen().trigger('liszt:updated');
        		columnsDropDown1.attr('disabled','disabled');
        		columnsDropDown2.attr('disabled','disabled');
	        	if(!_.isEmpty(cols)){
	        		columnsDropDown1.removeAttr('disabled');
	        		columnsDropDown2.removeAttr('disabled');
	        		var opts = _.difference(cols,forwardValues,reverseValues);
	        		$.each(opts, function(key,value) {
	        			columnsDropDown1.append($('<option></option>').val(value).append(value)[0].outerHTML);
	        			columnsDropDown2.append($('<option></option>').val(value).append(value)[0].outerHTML);
	        		});
        			$.each(forwardValues, function() {
        				columnsDropDown1.append($('<option></option>').val(this).attr('selected','selected').append(""+this)[0].outerHTML);
        			});
        			$.each(reverseValues, function() {
    					columnsDropDown2.append($('<option></option>').val(this).attr('selected','selected').append(""+this)[0].outerHTML);
		        	});
	        	}
	        	columnsDropDown1.chosen().trigger('liszt:updated');
	        	columnsDropDown2.chosen().trigger('liszt:updated');
	        };
	    };
	    $.ajax({
	          type: "POST",
	          processData: false,
	          url: url,
	          contentType: 'application/json; charset=utf-8',
	          dataType: 'json',
	          data: JSON.stringify(requestJSON),
	          success: doSuccess(linkDirection),
	          error: function(data) {
	              // alert ("Error while creating new visualization");
	          }
	    });
                
	
}
LinkEditorDirection.prototype.handleDropdownMenus = function(modalDiv){
	var columnOptions = this.cols ? this.cols : [];
	this.isValid = false;
	modalDiv.find('#treeFinish').trigger('change');
	var frwdOption = modalDiv.find('#dynamic-forward' + this.linkId).val() ? modalDiv.find('#dynamic-forward' + this.linkId).val() : [];
	var revOption = modalDiv.find('#dynamic-reverse' + this.linkId).val() ? modalDiv.find('#dynamic-reverse' + this.linkId).val() : [];
	var columnsDropDown1 = modalDiv.find('#dynamic-forward' + this.linkId).empty();
	var columnsDropDown2 = modalDiv.find('#dynamic-reverse' + this.linkId).empty();
	var optRev = _.difference(columnOptions,frwdOption,revOption);
	var optForward = _.difference(columnOptions,revOption,frwdOption);
	$.each(frwdOption, function() {
		columnsDropDown1.append($('<option></option>').val(this).attr('selected','selected').append(""+this)[0].outerHTML);
	});
	$.each(optForward,function(){
		columnsDropDown1.append($('<option></option>').val(this).append(""+this)[0].outerHTML);
	});
	$.each(revOption, function() {
		columnsDropDown2.append($('<option></option>').val(this).attr('selected','selected').append(""+this)[0].outerHTML);
	});
	$.each(optRev,function(){
		columnsDropDown2.append($('<option></option>').val(this).append(""+this)[0].outerHTML);
	});
	if(columnsDropDown1.val() || columnsDropDown2.val()){
		this.isValid = true;
		modalDiv.find('#treeFinish').trigger('change');
	}
	modalDiv.find("#dynamic-forward" + this.linkId).chosen().trigger('liszt:updated');
	modalDiv.find("#dynamic-reverse" + this.linkId).chosen().trigger('liszt:updated');
}

LinkEditorDirection.prototype.findIsValid = function(){
	return this.isValid;
}