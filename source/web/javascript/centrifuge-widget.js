/**
 * @fileoverview This file is to be used in conjunction with a licensed copy of Centrifuge Server
 *
 *
 * @author Luke Orman luke.orman@centrifugesystems.com
 * @version 0.1
 */

/*load = function(url) {
 load.getScript(url);
 }

 // dynamically load any javascript file.
 load.getScript = function(filename) {
 var script = document.createElement('script')
 script.setAttribute("type","text/javascript")
 script.setAttribute("src", filename)
 if (typeof script!="undefined")
 document.getElementsByTagName("head")[0].appendChild(script)
 }*/

function loadScript(url, callback) {
	var head = document.getElementsByTagName("head")[0];
	var script = document.createElement("script");
	script.src = url;

	// Attach handlers for all browsers
	var done = false;
	script.onload = script.onreadystatechange = function() {
		if( !done && ( !this.readyState
		|| this.readyState == "loaded"
		|| this.readyState == "complete") ) {
			done = true;

			// Continue your code
			callback();

			// Handle memory leak in IE
			script.onload = script.onreadystatechange = null;
			head.removeChild( script );
		}
	};
	if (typeof checkObj == "undefined" || !checkObj) {
		head.appendChild(script);
	}
}

//check to make sure jQuery is not already loaded
//TODO: make sure other libraries are not already loaded

//dynamically add the jQuery library
loadScript("/Centrifuge/javascript/jquery.js", function()
{
        loadScript("/Centrifuge/javascript/jquery.json.js", function()
		{
		        loadScript("/Centrifuge/javascript/jquery.timers.js", function()
				{
				        loadScript("/Centrifuge/javascript/jquery-uuid.js", function()
						{
						        loadScript("/Centrifuge/javascript/comet.js", function()
								{
								        loadScript("/Centrifuge/javascript/swfobject.js", function()
										{
										   loadScript("/Centrifuge/javascript/jquery.cookie.js", function()
													{
													        //alert('everything loaded');
													});
										});
								});
						});
				});
		});
});
/*if(!jQuery.toJSON) {
//add the jQuery json libs
load("/Centrifuge/javascript/jquery.json.js");
}
if(!jQuery.timer) {
//add jQuery timers library to support the comet protocol
load("/Centrifuge/javascript/jquery.timers.js");
}
if(!jQuery.uuid) {
//add jQuery uuid library to support the comet protocol
load("/Centrifuge/javascript/jquery-uuid.js");
}
if(typeof CsiProtocol == "undefined") {
//add the comet protocol libraries
load("/Centrifuge/javascript/comet.js");
}
if(typeof swfobject == "undefined") {
//add the swfobject.js lib to load the flash content
load("/Centrifuge/javascript/swfobject.js");
}*/

/**
 * Construct a new Centrifuge template object.
 * @class This a wrapper for the Centrifuge class.  This should be used when creating a dataview from a template.
 * @constructor
 * @base centrifuge
 * @throws MemoryException if there is no more memory
 * @return New centrifuge object
 * @param {string} templateUuid The unique id of a Template
 * @param {string} dvName The name you would like to assign the Dataview
 * @param {string} docElement The id of the HTML element where we will place the Centrifuge widget
 */
function DataviewTemplate(templateUuid, dvName, docElement) {
	var template = new centrifuge(templateUuid, "", dvName, docElement);

	template.createDvFromTemplate = function(callback) {
		var url = "/Centrifuge/actions/json/createDataviewFromTemplate";
		var postData = { "dvTemplateUuid": templateUuid, "dvName" : dvName };
		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(postData),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(dvUuid) {
				centrifuge.prototype.dvUuid = dvUuid.toString();
				callback();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	template.createDvFromTemplateName = function(callback) {
		var url = "/Centrifuge/actions/json/createDataviewFromTemplateName";
		var postData = { "dvTemplateName": templateUuid, "dvName" : dvName };
		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(postData),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(dvUuid) {
				centrifuge.prototype.dvUuid = dvUuid.toString();
				callback();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	return template;
}

/**
 * Construct a new Centrifuge dataview object.
 * @class This a wrapper for the Centrifuge class.  This should be used when loading a dataview that has already been created in Centrifuge.
 * @constructor
 * @base centrifuge
 * @throws MemoryException if there is no more memory
 * @return New centrifuge object
 * @param {string} dvUuid The unique id of a Dataview
 * @param {string} docElement The id of the HTML element where we will place the Centrifuge widget
 */
function Dataview(dvUuid, docElement) {
	var dv_obj = new centrifuge("", dvUuid, "", docElement);

	return dv_obj;
}

/**
 * Construct a new Centrifuge object.
 * @class This is the basic Centrifuge class.
 * @constructor
 * @throws MemoryException if there is no more memory
 * @return New centrifuge object
 * @param {string} templateUuid The unique id of a Template
 * @param {string} dvUuid The unique id of a Dataview
 * @param {string} dvName The name you would like to assign the Dataview
 * @param {string} docElement The id of the HTML element where we will place the Centrifuge widget
 */
function centrifuge(templateUuid, dvUuid, dvName, docElement) {
	centrifuge.prototype.docElement = docElement;
	centrifuge.prototype.dvName = dvName;
	centrifuge.prototype.templateUuid = templateUuid;
	centrifuge.prototype.dvUuid = dvUuid;

	return this;
}

/**
 * Construct a new Visualization object.
 * @class This is the basic Visualization class.
 * @constructor
 * @throws MemoryException if there is no more memory
 * @return New Visualization object
 * @param {string} vizUuid The unique id of a Visualization
 * @param {string} vizName The name of a Visualization
 * @param {boolean} isAttached The flag which signifies if a Visualization is attached
 */
function Visualization(vizUuid, vizName, isAttached) {
	this.vizUuid = vizUuid;
	this.vizName = vizName;
	this.isAttached = isAttached;

	this.reloadViz = function() {
		var reload = document.getElementById(centrifuge.prototype.docElement);
		try {
			reload.loadVisualization(this.vizUuid);
		} catch(err) {
			//IE error fix
		}
	}
	this.broadcastViz = function() {
		var broadcast = document.getElementById(centrifuge.prototype.docElement);
		broadcast.broadcastVisualization(this.vizUuid);
	}
	this.setVisualizationAttached = function(attachFlag, callback) {
		var url = "/Centrifuge/actions/json/setVisualizationAttached";
		var attachDef = new Object();
		attachDef.vizID = this.vizUuid;
		attachDef.attachFlag = attachFlag;

		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(attachDef),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(data) {
				callback();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	this.setSelectionFilter = function(fieldName, selectedValues) {
		var reloadViz = this;
		var url = "/Centrifuge/actions/json/setFilters"
		var filterDef = new Object();
		filterDef.dvUuid = centrifuge.prototype.dvUuid;
		filterDef.vizUuid = this.vizUuid;
		filterDef.filterFields = new Array();
		var filterField = new Object();
		filterField.name = fieldName;
		var filterString =  selectedValues;
		filterField.selectedValues = filterString.split(",");
		filterDef.filterFields[0] = filterField;

		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(filterDef),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(data) {
				reloadViz.reloadViz();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	this.setRangeFilter = function(fieldName, startValue, endValue) {
		var reloadViz = this;
		var url = "/Centrifuge/actions/json/setFilters"
		var filterDef = new Object();
		filterDef.dvUuid = centrifuge.prototype.dvUuid;
		filterDef.vizUuid = this.vizUuid;
		filterDef.filterFields = new Array();
		var filterField = new Object();
		filterField.name = fieldName;
		filterField.startValue = startValue;
		filterField.endValue = endValue;
		filterDef.filterFields[0] = filterField;

		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(filterDef),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(data) {
				reloadViz.reloadViz();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	
	this.clearFilters = function(callback, autoReload) {
		var reloadViz = this;
		var url = "/Centrifuge/actions/json/clearFilters";
		var vizUuid = this.vizUuid;

		//If autoReload is not specified do it by default.
		if(autoReload === undefined) {
			autoReload = true;
		}

		$.ajax({
			url: url,
			type: 'POST',
			data: $.toJSON(vizUuid),
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			success: function(data) {

				if(autoReload) {
					reloadViz.reloadViz();
				}

				if(callback !== undefined) {
					callback();
				}
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				alert(textStatus);
			}
		});
	}
	return this;
}

//returns Visualization object by name
centrifuge.prototype.getVisualization = function(vizName) {
	var viz;
	var url = "/Centrifuge/actions/json/getVisualizationProperties";
	var postData = { "dvUuid": centrifuge.prototype.dvUuid, "vizName" : vizName };
	$.ajax({
		url: url,
		type: 'POST',
		async: false,
		data: $.toJSON(postData),
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		success: function(vizProps) {
			if(vizProps.vizUuid!=undefined) {
				viz = new Visualization(vizProps.vizUuid, vizProps.vizName, vizProps.isAttached);
			} else {
				alert("Dataview not found ! Please reopen from examples.");
				try {
					$.cookie("dvUuid" + $("#username").val(), null);
				} catch(err) {
				}
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
		}
	});

	return viz;
}
//TODO: allow implementer to set the parameters of a template prior to load
centrifuge.prototype.setParameters = function(fieldName, fieldVal, callback) {
	var url = "/Centrifuge/actions/json/setInputParams"
	var paramDef = new Object();
	paramDef.dvUuid = centrifuge.prototype.dvUuid;
	paramDef.parameters = new Array();
	var param = new Object();
	param.name = fieldName;
	var paramString =  fieldVal
	param.selectedValues = paramString.split(",");
	paramDef.parameters[0] = param;

	$.ajax({
		url: url,
		type: 'POST',
		data: $.toJSON(paramDef),
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			callback();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
		}
	});
}
//TODO: may want to allow the implementer to load a dataview prior to launch
centrifuge.prototype.reload = function(callback) {
	var url = "/Centrifuge/actions/json/relaunchDV";
	$.ajax({
		url: url,
		type: 'POST',
		data: $.toJSON(centrifuge.prototype.dvUuid),
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			callback();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
		}
	});
}
//add the flash content to the appropriate element in the document
//TODO: currently the .swf loads and launches the dataview, should only launch
centrifuge.prototype.launch = function() {
	var flashvars = {};
	flashvars.dvUuid = centrifuge.prototype.dvUuid;
	flashvars.hideWorksheets = "false";
	swfobject.embedSWF("/Centrifuge/flex/CentrifugeWidget.swf", centrifuge.prototype.docElement, "100%", "100%", "9.0.0","/Centrifuge/flex/playerProductInstall.swf", flashvars, null, null, outputStatus);
}
centrifuge.prototype.launchApp = function() {
	var flashvars = {};
	flashvars.dvuuid = centrifuge.prototype.dvUuid;
	swfobject.embedSWF("/Centrifuge/flex/main.swf", centrifuge.prototype.docElement, "100%", "100%", "9.0.0","/Centrifuge/flex/playerProductInstall.swf", flashvars, null, null, outputStatus);
}
/**
 * @class This class exists to alert errors of any kind
 * @return Alert of error
 * @param {object} e The exception object
 */
function outputStatus(e) {
	if(e.success == false) {
		alert('Failed to load swf object');
	}
}

//deletes a dataview given a uuid
centrifuge.prototype.deleteDataview = function(callback) {
	var url = "/Centrifuge/actions/json/deleteDataview";
	$.ajax({
		url: url,
		type: 'POST',
		data: $.toJSON(centrifuge.prototype.dvUuid),
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			callback();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown) {
			alert(textStatus);
		}
	});
}