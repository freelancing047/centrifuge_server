var action = "pan";
var visualizationDef;
var fieldConstraints = new Array();
var fieldDefs = new Array();
//contains as many filters as many fieldDefs.
var filterFields = new Array();
//array containing the already created filter fields
var builtFilterFields = new Array();
var globalFilterIndex = 0;
var dropDownToFilterMap = new Array();

$(document).ready(function () {
		
    (function(img) {
		img.onerror = function() {
			this._loading = false;
		}		
		
		img.onabort = function() {
			this._loading = false;
		}
	
		img.onload = function() {
			this._loading = false;
			this.curw = this.width;
			this.curh = this.height;
			this.style.visibility = 'visible';
		}
    })(document.getElementById('display'));
		
    window.onresize = function(event) {
    	var img = document.getElementById('display');
    	if (img._resizeTimer) {
    		clearTimeout(img._resizeTimer);
    	}
    	img._resizeTimer = setTimeout(
			function() {
				if (!img) {
					return;
				}
				
				var neww = img.parentNode.offsetWidth;
				var newh = img.parentNode.offsetHeight;
				var curw = img.curw;
				var curh = img.curh;

				if (!neww && !newh && !curh && !curh && neww == curw && neww == curh) {
					return;
				}
				
				refreshImage();
		}, 100);
    }
		
	$('#display')[0]._mouseinfo = {};
	$('#display').imgAreaSelect({
		handles: false, 
		onSelectStart: function (img, selection) {
			img._mouseinfo.regionSelecting = true;
			img._mouseinfo.startx = selection.x1;
			img._mouseinfo.starty = selection.y1;
			
			if (img._mousemoveTimer) {
				clearTimeout(img._mousemoveTimer);
			}
			
			if (action == 'pan') {
				$(img).imgAreaSelect({ hide: true });
			} else {
				$(img).imgAreaSelect({ hide: false });
			}
		}, 
        onSelectEnd: function (img, selection) {
			if (img._mousemoveTimer) {
				clearTimeout(img._mousemoveTimer);
			}
    		  
			if (selection.width > 0 && selection.height > 0) {
				if (action == 'zoom') {
					zoomToRegion(selection.x1, selection.y1, selection.x2, selection.y2);
					
				} else if (action == 'select') {
					var reset = true;
					if (document._keys && document._keys.ctrlKey) {
						reset = false;
					}
					selectRegion(selection.x1, selection.y1, selection.x2, selection.y2, reset);
					
				} else if (action == 'pan') {
					var minfo = img._mouseinfo;
					var dx = selection.width;
					var dy = selection.height;
					
					if (dx == 0 && dy == 0) {
						return;
					}
					
					if (minfo.startx != selection.x1) {
						dx = -dx;
					}
					
					if (minfo.starty != selection.y1) {
						dy = -dy;
					}
					
					pan(dx, dy);
				}
			}
		  
			$(img).imgAreaSelect({ hide: true });
			img._mouseinfo.regionSelecting = false;
		}
	});

    $(document).keydown(function (event) {
        if (event.target.tagName != "INPUT") {
            event.preventDefault();
            document._keys = {
                ctrlKey: event.ctrlKey,
                shiftKey: event.shiftKey,
                altKey: event.altKey,
                metaKey: event.metaKey,
                keyCode: event.keyCode
            };
        }
    });

    $(document).keyup(function (event) {

        if (event.target.tagName != "INPUT") {
            event.preventDefault();

            document._keys = {
                ctrlKey: event.ctrlKey,
                shiftKey: event.shiftKey,
                altKey: event.altKey,
                metaKey: event.metaKey,
                keyCode: event.keyCode
            };
        }


    });

    $("#display").mousedown( function(event) {
		event.preventDefault();
			  
		var img = event.currentTarget || event.target;
			  
		if (img._mousemoveTimer) {
			clearTimeout(img._mousemoveTimer);
		}
			  
		img._mouseinfo.downx = event.pageX;
		img._mouseinfo.downy = event.pageY;
	  
	});

	$("#display").mouseup( function( event ) {
		event.preventDefault();
		 
		var img = event.currentTarget || event.target;
		  
		// ie needs this
		document._keys = { 
			ctrlKey: event.ctrlKey,
			shiftKey: event.shiftKey,
			altKey: event.altKey,
			metaKey: event.metaKey,
			keyCode: event.keyCode
		};
	  
	  	if (img._mousemoveTimer) {
			clearTimeout(img._mousemoveTimer);
		}
		  
	 	if (img._mouseinfo.regionSelecting == true) {
			return;
		}
	  
	  	if (whichButton(event) == 'left') {
			if (event.pageX == img._mouseinfo.downx && event.pageY == img._mouseinfo.downy) {
				var location = $(img).offset();
			  	var x = event.pageX - location.left;
			  	var y = event.pageY - location.top;
			  
			  	var reset = true;
			  	if (event.ctrlKey) {
					reset = false;
			  	}
			  
			  	selectItem(x, y, reset);
		  	}
	  	}
	});
	  
	  
  	$("#display").mousemove( function( event ) {
		event.preventDefault();
	  
	  	var img = event.currentTarget || event.target;
	  
	  	if (img._mousemoveTimer) {
			clearTimeout(img._mousemoveTimer);
	  	}

	  	if (img._mouseinfo.regionSelecting == true) {
			return;
	  	}
	  
	  	if (event.ctrlKey || event.shiftKey || event.altKey || event.button) {
			return;
	  	}
	  
	  	img._mousemoveTimer = setTimeout( function() {
			var location = $(img).offset();
		  	var x = event.pageX - location.left;
		  	var y = event.pageY - location.top;
		  	var hoverx = img._mouseinfo.hoverx;
		  	var hovery = img._mouseinfo.hovery;
		  
		  	if (hoverx != x && hovery != y) {
			  	img._mouseinfo.hoverx = x;
			  	img._mouseinfo.hovery = y;
			  	findItem(x, y);
		  	}
	  	}, 1000);
	});
	  
	$("#display").mouseout( function(event) {
		event.preventDefault();
		  
		var img = event.currentTarget || event.target;
		if (img._mousemoveTimer) {
			clearTimeout(img._mousemoveTimer);
		}
	});

	$('#display').mousewheel(function(event, delta, deltaX, deltaY) {
		event.preventDefault();
		if (!delta || delta == 0) {
			return;
		}
		
		var img = event.currentTarget || event.target;
		if (img._mousewheelTimer) {
			clearTimeout(img._mousewheelTimer);
		}
		
		img._mousewheelTimer = setTimeout(function() {
			var percent = delta * 0.10;
			zoom(percent)
		}, 100);
	});

    $('#displayFilters').click(function() {
        displayFilters();
    });

    $('#resetFilters').click(function() {
        resetFilters();
    });
});
	
	function whichButton(event) {
		if( (!$.browser.msie && event.button == 0) || ($.browser.msie && event.button == 1) ) {
			return 'left';     
		} else if(event.button == 2) {
			return 'right'; 		
		}
		
		return 'none';
	}	

	function selectItem(x, y, reset) {
		csi.relgraph.selectItemAt(vizuuid, x, y, reset, {
			onsuccess: function() {
				refreshImage();
			}
		});
	}

	function findItem(x, y) {
		csi.relgraph.findItemAt(vizuuid, x, y, {
			onsuccess: function(data) {
				if (data.resultData) {
					var tips = new Array();
					
					createTips(data.resultData.tooltips, tips);
					
					var tippanel = $("#tooltipPanel");
					tippanel.css('left', x + 5);
					tippanel.css('top', y + 5);
					
					/* hack to get IE to use max/min width and height css style */
					var minw;
					var minh;
					var maxw;
					var maxh; 
					if (tippanel.currentStyle) {
						minw = tippanel.currentStyle.minWidth;
						minh = tippanel.currentStyle.minHeight;
						maxw = tippanel.currentStyle.maxWidth;
						maxh = tippanel.currentStyle.maxHeight;						
						tippanel.style.width = minw;
						tippanel.style.height = minw;
					}
					
					tippanel.html(tips.join(""));
					
					/* part of IE max/min hack */
					if (maxw && tippanel.width() > parseInt(maxw)) {
						tippanel.style.width = maxw;
					}					

					if (maxh && tippanel.height() > parseInt(maxh)) {
						tippanel.style.height = maxh;
					}
					
					tippanel.show(100);		
				} else {
					$("#tooltipPanel").text("").hide(100);
				}
			}			
		});
	}
	
	function createTips(obj, tips) {
		
		if (obj instanceof Array) {
				$.each(obj, function(index, avalue) {
					createTip(null,avalue, tips);
				});
		} else if (typeof obj =="object") {
			tips[tips.length]="<table class='tiplist'>";
			$.each(obj, function(key, value) {
				createTip(key, value, tips);
			});		
			tips[tips.length]="</table>";
		} else {
			createTip(null, value, tips)
		}
		
	}
	
	function createTip(key, value, tips) {
		if (key) {
			tips[tips.length] = "<tr class='tipitem'><td class='tipkey'><b>" + key + ":&nbsp;&nbsp;</b></td>";
		} else {
			tips[tips.length] = "<tr class='tipitem'><td class='tipitem'></td>";
		}
		tips[tips.length] = "<td class='tipitem'>";
		if (typeof value == "object") {
			tips[tips.length]="<table class='tiplist'>";
			createTips(value, tips);
			tips[tips.length]="</table>";
		} else {
			tips[tips.length] = value;			
		}
		tips[tips.length]="</td></tr>";
		
	}

	function clearSelection() {
		if (!testForGraph()){return false;}
		
		csi.relgraph.clearSelection(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
		$("#tooltipPanel").text("").hide(1000);
	}

	function selectAll() {
		if (!testForGraph()){return false;}
		
		csi.relgraph.selectAll(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});		
		
	}

	function hideSelection() {
		csi.relgraph.hideSelected(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}

	function unhideSelection() {
		csi.relgraph.unhideSelected(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}
    
	function loadGraph() {
		csi.relgraph.loadGraph(dvuuid, vizuuid, {
			onsuccess: function(data) {
				refreshImage();
				getLegendData();
			}			
		});
	}

	function toggleLabels() {
		csi.relgraph.toggleLabels(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}

	function getLegendData() {
		csi.relgraph.legendData(vizuuid, {
			onsuccess: function(data) {
				var legend = '';
				$(data.resultData.nodeLegendItems).each(function() {
                	var shapeSource =  '/Centrifuge/WidgetControllerServlet?action=render&shape='+this.shape +'&color='+this.color+'&image='+this.iconURI;
                	legend = legend + "<span class='legendItem'><img class='legendImage' onclick=\"selectNodesByType('" + this.typeName + "');return false;\" src='"
						+ shapeSource + "'></img>&nbsp;&nbsp;<a href='javascript:void(0)' onclick=\"selectNodesByType('" + this.typeName + "');return false;\">"
						+ this.typeName + "</a> ( " 
						+ this.count + " of " 
						+ this.totalCount + ")</span></br>";
					});
				$("#legendBody").html(legend);
				$("#legendPanel").show();
			}
		});
	}

	function refreshImage() {
		// get the legend when the image is reloaded
		if ($("#legendPanel").is("visible")) {
			getLegendData();
		}
	
	    var img = document.getElementById('display');
	    if (!img) {
	    	return;
	    }
	        
	    if (img._loadtimer) {
		    clearTimeout(img._loadtimer);
		}
		
	    img._loadtimer = setTimeout(
			function() {
		    	var container = img.parentNode;
		    	
				if (img._loading && img._loading == true) {
					return;
				}
				img._loading = true;
				
		    	var w = container.offsetWidth;
		    	var h = container.offsetHeight;
				img.src = csi.relgraph.graphImageUrl(dvuuid, vizuuid, w, h);
				
			}, 300);
	}

	function fitToSize() {
		if (!testForGraph()){return false;}
		
		csi.relgraph.fitToSize(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}

	function fitToSelection() {
		if (!testForGraph()){return false;}
		
		csi.relgraph.fitToSelected(vizuuid, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}
	
	function selectNodesByType(type) {
		var reset = true;
		if (document._keys && document._keys.ctrlKey == true) { 
			reset = false;
		}
		csi.relgraph.selectNodesByType(dvuuid, vizuuid, type, reset, {
			onsuccess: function(data) {
				refreshImage();
			}
		});
	}	

	function selectRegion(x1, y1, x2, y2, reset) {
		csi.relgraph.selectRegion(vizuuid, x1, y1, x2, y2, reset, {
			onsuccess: function(data) {
				refreshImage();
			}			
		});
	}

	function zoomToRegion(x1, y1, x2, y2) {
		csi.relgraph.zoomToRegion(vizuuid, x1, y1, x2, y2, {
			onsuccess: function(data) {
				refreshImage();
			}			
		});
	}

	function pan(dx, dy) {
		csi.relgraph.pan(vizuuid, dx, dy, {
			onsuccess: function(data) {
				refreshImage();
			}			
		});
	}

	function zoom(percent) {
		csi.relgraph.zoomPercent(vizuuid, percent, {
			onsuccess: function(data) {
				refreshImage();
			}			
		});
	}

	function startLayout(type) {
		if (!testForGraph()){return false;}
		
		csi.relgraph.doLayout(vizuuid, type, {
			onsuccess: function(data) {
				refreshImage();
			}			
		});
	}
	
	function saveGraph() {
		if (!testForGraph()){return false;}
		
		csi.relgraph.saveGraph(vizuuid, {
			onsuccess: function(data, status, xhr) {
				alert('Graph saved');
			},			
		
			onerror: function(xhr, status) {
				alert('Error saving graph');
			}
			
		});
	}

/**
 * Function called when user clicks the Filters menu -> Display Filters.
 * Initializes the visualization (Relgraph visualization that has been chosen by the user).
 * Then calls again the server to get the current dataview and initializes the fieldDefs array with the dataview fields sorted naturally.
 * Then creates the visual components for the existing filters, if there are any.
 */
function displayFilters() {
    if (!testForGraph()){return false;}
    //reset the global Filter Index, it could have been modified previously, but every time we enter the filters we want to start with index zero.
    globalFilterIndex = 0;
    //find filters already saved and initialize builtFilterFields
    csi.viz.getVisualization(vizuuid, {
        onsuccess: function(data) {
            visualizationDef = data.resultData;
            builtFilterFields = visualizationDef.filterFields;
            csi.dataview.getDataView(dvuuid, {
			onsuccess: function(data) {
                var items = data.resultData.meta.modelDef.fieldDefs;
                var fields = new Array();
                for (var i=0; i<items.length; i++ ){
                    if (items[i].fieldType && items[i].fieldType == "COLUMN_REF"){
                        fields[fields.length] = items[i];
                    }
                }
                fieldDefs = fields.sort(sortfunction);
                createExistingFiltersPanels();
            $("#filterPanel").show();
			},
            onerror: function(xhr, status) {
				alert('Error getting dataview');
			}
		});
        },
        onerror: function(xhr, status) {
            alert('Error getting visualization');
        }
    });
}

/**
 * Function called when user clicks the "Add filter" button. It first searches for a field index that has not already been used in a filter.
 * Then updates the dropDownToFilterMap (map between ui drop down list id and the filter that corresponds to it).
 * Then creates the filter ui components and increases the globalFilterIndex -> keeps track of the used ids for the drop down lists.
 */
function addFilter() {
    var index = findFirstUnusedIndex();
    addDropDownToFilterMapItem(globalFilterIndex, builtFilterFields.length);
    createFilterControls(globalFilterIndex, index);
    globalFilterIndex++;
}

function addDropDownToFilterMapItem(selectId, filterId){
    dropDownToFilterMap[selectId] = filterId;
}

function findFirstUnusedIndex(){
    //compare existing filters with available fields and return the first available field's index that is not already used in a filter.
    var found = false;
    for (var i=0; i<fieldDefs.length; i++){
        for (var j=0; j<builtFilterFields.length; j++){
            found = false;
            if (builtFilterFields[j].field.uuid == fieldDefs[i].uuid){
                found = true;
                break;
            }
        }
        if (!found){
            return i;
        }
    }
    //should never get to this point, unless user has defined a filter for every single field.
    return 0;
}

/**
 * Creates a ui drop down list with the given id and option list.
 * @param id  the id of the drop down list.
 * @param optionFieldDefs list of items in the drop down list.
 */
function createSelect(id, optionFieldDefs){
    var select = document.createElement("select");
    select.setAttribute("id", id);
    for (var i = 0; i < optionFieldDefs.length; i++) {
        var elOptNew = document.createElement('option');
        elOptNew.text = optionFieldDefs[i].fieldName;
        elOptNew.value = optionFieldDefs[i].valueType;
        select.options[select.options.length] = elOptNew;
    }
    return select;
}

/**
 * Creates the UI components for a filter.
 * @param index    integer corresponding to the next drop down list that will be created (during this method)
 * @param selectedIndex   Each filter has a drop down list containing all dataview fields.
 *                        This index indicates the selected item in the current filter's drop down list.
 */
function createFilterControls(index, selectedIndex) {
    var root = document.getElementById('filterContentDiv');
    var filter = document.createElement("div");
    filter.id = "filter"+index;
    filter.className = "filterDiv";
    filter.appendChild(document.createTextNode("Select Field:"));
    var select =  createSelect("select"+index, fieldDefs);
    select.selectedIndex = selectedIndex;
    select.onchange =  function(){
        var selIdx = this.id.substring(6, this.id.length); //the id is like selectX where X is the index that we need to substring.
        getFilterConstraints(selIdx, this.selectedIndex);
    };
    filter.appendChild(select);
    addRemoveButton(filter, index);
    root.appendChild(filter);
    //having the selected index of the drop down, need to calculate its available values
    getFilterConstraints(index, selectedIndex);
}

/**
 * Creates a remove button with a remove function associated on click event.
 * @param parent represents the div where the button will be added.
 * @param index  represents the filter index
 */
function addRemoveButton(parent, index){
    var removeButton = document.createElement("input");
    removeButton.type = "button";
    removeButton.id = "removeButton"+index;
    removeButton.value = "Remove";
    removeButton.onclick = function(){
        //remove filter by index;
        builtFilterFields.splice(dropDownToFilterMap[index], 1);
        updateDropDownToFilterMap(index);
        //remove visual content of this filter.
        var filterContent = document.getElementById("filterContentDiv");
        var filter = document.getElementById("filter"+index);
        filterContent.removeChild(filter);
    }
    parent.appendChild(removeButton);
}

/**
 * Updates the drop down to filter map when a filter is removed. The starting point for update is the removed filter index,
 * this position will be replaced by -1, and all subsequent indexes will be decremented by 1.
 * No element is removed from the map, the drop down id will never be used after its corresponding filter has been removed.
 * Example: Having [0->0, 1->1, 2->2, 3->3, 4->4], removing filter with index 1 the map will be: [0->0, 1->-1, 2->1, 3->2, 4->3].
 * If then filter 0 will be removed, map will be: [0>-1, 1->-2, 2->0, 3->1, 4->2]. Only 3 filters left but they correspond to select nr: 2,3,4.
 * @param removedFilterIndex  the index of the filter in the builtFilterFields's array.
 */
function updateDropDownToFilterMap(removedFilterIndex){
    dropDownToFilterMap[removedFilterIndex] = -1;
    for (var selectIndex = removedFilterIndex + 1; selectIndex < dropDownToFilterMap.length; selectIndex++){
        dropDownToFilterMap[selectIndex] = dropDownToFilterMap[selectIndex] -1;
    }
}

/**
 * Function called from displayFilters to create the UI components for the previously saved filters.
 */
function createExistingFiltersPanels(){
    if (builtFilterFields && builtFilterFields.length > 0){
        csi.viz.getFilterConstraints(dvuuid, vizuuid, builtFilterFields, {
                    onsuccess: function(data) {
                        fieldConstraints = data.resultData;
                        //initialize filter component for the selected element in the select list.
                        createElementsForExistingFilters();
                    },
                    onerror: function(xhr, status) {
                        alert('Error getting filter constraints');
                    }
                });
    }
}

/**
 * Creates UI components for existing filters.
 */
function createElementsForExistingFilters() {
    for (var i = 0; i < builtFilterFields.length; i++) {
        var root = document.getElementById('filterContentDiv');
        var filter = document.createElement("div");
        filter.id = "filter" + i;
        filter.className = "filterDiv";
        filter.appendChild(document.createTextNode("Select Field:"));
        var selectDef = createSelect("select" + i, fieldDefs);
        selectDef.selectedIndex = getFieldIndex(i);
        selectDef.onchange = function() {
            var idx = this.id.substring(6, this.id.length);
            getFilterConstraints(idx, this.selectedIndex);
        };
        filter.appendChild(selectDef);
        root.appendChild(filter);
        addRemoveButton(filter, i);
        addDropDownToFilterMapItem(i, i);
        generateFilterConstraint(i);
        //increment global filter index for each created filter to make sure that no select items are created with the same index suffix.
        globalFilterIndex++;
    }
}

/**
 * Finds the matching fieldDefs index of the selected item from filterConstraints.
 * @param filterConstraintsIndex
 */
function getFieldIndex(filterConstraintsIndex){
    for (var idx=0; idx<fieldDefs.length; idx++){
        if (fieldDefs[idx].fieldName == fieldConstraints[filterConstraintsIndex].filterField.field.fieldName){
            return idx;
        }
    }
    return 0;
}

/**
 * Creates a new FilterField object with no selection and the fieldDef object at the given index position
 * @param selectedIndex the index corresponding to the fieldDef object to be included in the FilterField object.
 */
function buildNewFilter(selectedIndex){
    var fieldDef = fieldDefs[selectedIndex];
    return new FilterField(new Array(), fieldDef);
}

/**
 * Makes request to server to find the filter constraints.
 * The client gives the filterFields and the server calculates the constraints for these fields.
 * The constraints include ranges for numeric and date/time fields, and available values for the string fields.
 * @param filterIndex         the index (id of the drop down list) of the current filter that is making the request.
 * @param selectedItemIndex   selected field from the drop down list.
 */
function getFilterConstraints(filterIndex, selectedItemIndex) {
    var currentFilter = buildNewFilter(selectedItemIndex);
    var currentFilterIndex = dropDownToFilterMap[filterIndex];
    builtFilterFields[currentFilterIndex] = currentFilter;
    csi.viz.getFilterConstraints(dvuuid, vizuuid, builtFilterFields, {
        onsuccess: function(data) {
            fieldConstraints = data.resultData;
            //initialize filter component for the selected element in the select list.
            generateFilterConstraint(filterIndex);
        },
        onerror: function(xhr, status) {
            alert('Error getting filter constraints');
        }
    });
}


/**
 * Constructor for FilterField objects.
 * @param selectedValues array of strings containig the selected values from a drop down list corresponding to a string field.
 * @param fieldDef       the field for which user defines the filter.
 */
function FilterField(selectedValues, fieldDef){
        this["class"] = "csi.server.common.model.filter.FilterField";
        //this.ordinal = ordinal;
        this.field = fieldDef;
        this.selectedValues = selectedValues;
    }

/**
 * Deletes the children of a container.
 * @param parent the DOM object representing the container.
 */
function deleteChildren(parent) {
    if (parent.hasChildNodes()) {
        while (parent.childNodes.length >= 1) {
            parent.removeChild(parent.firstChild);
        }
    }
}

/**
 * Creates a row with a label, input and a default value.
 * @param text           the label of this row.
 * @param inputId        the input text id.
 * @param value          the input value. Expect null for new filters, expect a value for existing filters.
 * @param defaultValue   The minimum/maximum value for this filter.
 */
function createTextRow(text, inputId, value, defaultValue){
    var row, cell;
    row = document.createElement('tr');
    cell = document.createElement('td');
    cell.appendChild(document.createTextNode(text));
    row.appendChild(cell);
    cell = document.createElement('td');
    var minRange = document.createElement("input");
    minRange.type = "text";
    minRange.id = inputId;
    minRange.removeAttribute("readonly");
    if (value) {
        minRange.value = value;
    } else {
        minRange.value = defaultValue;
    }
    cell.appendChild(minRange);
    row.appendChild(cell);
    cell = document.createElement('td');
    cell.appendChild(document.createTextNode("( "+defaultValue+" )"));
    row.appendChild(cell);
    return row;
}

/**
 * Creates UI components for a certain filter. This function is called after the drop down list of the filter is created.
 * Having the id of the drop down list, finds the selected item from it and creates UI components function of the selected item type.
 * For number and date/time type it creates the input for the minimum and maximum value,
 * and for the string type it generates another drop down list containing the available values.
 * @param selectIndex
 */
function generateFilterConstraint(selectIndex){
    var fieldSelect = document.getElementById("select"+selectIndex);
    var fieldDefValueType = fieldSelect.options[fieldSelect.selectedIndex].value;
    var fieldConstraint = getFieldConstraint(fieldDefs[fieldSelect.selectedIndex]);
    if (!fieldConstraint){
        alert("Error getting fieldConstraint!");
    }
    var filterRangeDiv = document.getElementById("filterRangeDiv"+selectIndex);
    //if filterRangeDiv does not exist, create it.
    if (!filterRangeDiv){
        filterRangeDiv = document.createElement("div");
        filterRangeDiv.setAttribute("id", "filterRangeDiv"+selectIndex);
        var filterDiv = document.getElementById("filter"+selectIndex);
        filterDiv.appendChild(filterRangeDiv);
    }
    //make sure all previous children of filterRangeDiv are deleted.
    deleteChildren(filterRangeDiv);
    var tab, row;
    if (fieldDefValueType == "number"){
        //create numeric fields for minRange and MaxRange
        tab = document.createElement('table');
        row = createTextRow("Minimum:", "minRange"+selectIndex, fieldConstraint.filterField.startValue ,fieldConstraint.rangeMin);
        tab.appendChild(row);
        row = createTextRow("Maximum:", "maxRange"+selectIndex, fieldConstraint.filterField.endValue, fieldConstraint.rangeMax);
        tab.appendChild(row);
        filterRangeDiv.appendChild(tab);
    } else if (fieldDefValueType == "date/time"){
        //create date/time fields for minRange and MaxRange
        tab = document.createElement('table');
        var formatedStartDate = formatDate(new Date(parseFloat(fieldConstraint.rangeMin)));
        row = createTextRow("Start date (MM/dd/yyyy hh:mm:ss):", "minRange"+selectIndex, autoCompleteValue(fieldConstraint.filterField.startValue, formatedStartDate), formatedStartDate);
        tab.appendChild(row);
        var formatedEndDate = formatDate(new Date(parseFloat(fieldConstraint.rangeMax)));
        row = createTextRow("End date (MM/dd/yyyy hh:mm:ss):", "maxRange"+selectIndex, autoCompleteValue(fieldConstraint.filterField.endValue, formatedEndDate), formatedEndDate);
        tab.appendChild(row);
        filterRangeDiv.appendChild(tab);
    } else if (fieldDefValueType == "string"){
        //create drop down list containing all values for a string field.
        var filterValuesDiv = document.createElement("div");
        filterValuesDiv.className = "filterValues";
        var availableValuesList = document.createElement("select");
        availableValuesList.setAttribute("id", "availableValues"+selectIndex);
        availableValuesList.setAttribute("multiple", "multiple");
        availableValuesList.setAttribute("size", 4);
        var availableValues = fieldConstraint.availableValues;
        if (availableValues) {
            availableValues.sort(sortStrings);
            for (var i = 0; i < availableValues.length; i++) {
                var elOptNew = document.createElement('option');
                elOptNew.text = availableValues[i];
                elOptNew.value = availableValues[i];
                availableValuesList.options[availableValuesList.options.length] = elOptNew;
            }
        }
        //iterate through the items in drop down list and mark as selected the selectedValues.
        var selectedValues = fieldConstraint.filterField.selectedValues;
        if (selectedValues) {
            for (var k = 0; k < selectedValues.length; k++) {
                for (var j=0; j<availableValuesList.options.length; j++){
                    if (selectedValues[k] == availableValuesList.options[j].text){
                        availableValuesList.options[j].selected = true;
                        break;
                    }
                }
            }
        }
        filterValuesDiv.appendChild(availableValuesList);
        filterRangeDiv.appendChild(filterValuesDiv);
    }
}

/**
 * Having the field, find the corresponding filterConstraint.
 * @param fieldDef  a dataview fieldDef.
 */
function getFieldConstraint(fieldDef){
    for (var i=0; i<fieldConstraints.length; i++){
        if (fieldConstraints[i].filterField.field.uuid == fieldDef.uuid){
            return fieldConstraints[i];
        }
    }
    return null;
}

/**
 * Called when user clicks the Save button. It sends the current filters to the server to be persisted.
 */
function saveFilters(){
    //validate that no duplicate filters exist (Cannot allow 2 filters for the same fieldDef).
    if (!validateFilterFields()){
        return;
    }
    //when parsing the user input, if improper values found, return
    if (!calculateSelectedValues()){
        return;
    }
    //change the visualizationDef to contain the new filters and send it to server to persist.
    updateFiltersAndSaveSettings(builtFilterFields);
}

/**
 * iterate through all fieldDef and all corresponding FilterFields to get the selected values from the drop down lists.
 * then update the builtFilterFields by setting the selectedValues property.
 */
function calculateSelectedValues() {
    //index of the drop down list. Some of them have been removed so need to properly match the filter index with drop down list index.
    var index = 0;
    //if no remove was done, the index will correspond to k all the time, otherwise need to find the appropriate index for the corresponding filter.
    for (var k = 0; k < builtFilterFields.length; k++, index++) {
        index = findFilterDropDownIndex(k);
        var dropDownList = document.getElementById("select" + index);
        var fieldDefValueType = dropDownList.options[dropDownList.selectedIndex].value;
        if (fieldDefValueType == "number") {
            var minValue = document.getElementById("minRange" + index).value;
            var maxValue = document.getElementById("maxRange" + index).value;
            var minValueNumber =  validateNumberInput(minValue, builtFilterFields[k].field, true);
            var maxValueNumber =  validateNumberInput(maxValue, builtFilterFields[k].field, false);
            if (minValueNumber == null || maxValueNumber == null){
                return false;
            }
            builtFilterFields[k].startValue = minValueNumber.toString();
            builtFilterFields[k].endValue = maxValueNumber.toString();
        } else if (fieldDefValueType == "date/time") {
            minValue = document.getElementById("minRange" + index).value;
            maxValue = document.getElementById("maxRange" + index).value;
            var minValueDate = validateDateInput(minValue, builtFilterFields[k].field, true);
            var maxValueDate = validateDateInput(maxValue, builtFilterFields[k].field, false);
            if (!minValueDate || !maxValueDate){
                return false;
            }
            builtFilterFields[k].startValue = minValueDate.getTime().toString();
            builtFilterFields[k].endValue = maxValueDate.getTime().toString();
        } else if (fieldDefValueType == "string") {
            var listValues = document.getElementById("availableValues" + index);
            if (listValues) {
                var selectedValues = new Array();
                for (var j = 0; j < listValues.options.length; j++) {
                    if (listValues.options[j].selected) {
                        selectedValues[selectedValues.length] = listValues.options[j].text;
                    }
                }
                builtFilterFields[k].selectedValues = selectedValues;
            } else {
                alert("not found drop down with id=availableValues" + index);
            }
        }
    }
    return true;
}

/**
 * Find the matching drop down list id having the index filter.
 * @param index   index of the drop down list
 */
function findFilterDropDownIndex(index){
    for (var k = 0; k<dropDownToFilterMap.length; k++){
        if (dropDownToFilterMap[k] == index){
            return k;
        }
    }
    return 0;
}

/**
 * validate that no duplicate filters exist (Cannot allow 2 filters for the same fieldDef).
 */
function validateFilterFields(){
    for (var i = 0; i < builtFilterFields.length-1; i++) {
        for (var j = i+1; j < builtFilterFields.length; j++) {
            if (builtFilterFields[i].field.fieldName == builtFilterFields[j].field.fieldName) {
                alert("There are duplicate filters! Saving is aborted.");
                return false;
            }
        }
    }
    return true;
}

/**
 * Checks that the user value is a number and that doesn't exceed the allowed ranges, provided by the filterConstraint.
 * @param value      the user input value.
 * @param fieldDef   the fieldDef corresponding to this filter -> need it to retrieve the filter range.
 * @param min        boolean, tells if the validation is done for the minimum or maximum value.
 *                   If true -> validation is done for minimum value.
 */
function validateNumberInput(value, fieldDef, min){
    //verify that the user input is a number
    var numberValue = Number(value);
    if (!numberValue){
        alert("Please correct number input fields.");
        return null;
    }
    //verify that the given input is in the expected range
    var fieldConstraint = getFieldConstraint(fieldDef);
    if (min){
        if (numberValue < Number(fieldConstraint.rangeMin)){
            return Number(fieldConstraint.rangeMin);
        }
    } else {
        if (numberValue > Number(fieldConstraint.rangeMax)){
            return Number(fieldConstraint.rangeMax);
        }
    }
    return numberValue;
}

/**
 * Checks that the  user value is a date and that doesn't exceed the allowed ranges, provided by the filterConstraint.
 * @param value      the user input value.
 * @param fieldDef   the fieldDef corresponding to this filter -> need it to retrieve the filter range.
 * @param min        boolean, tells if the validation is done for the minimum or maximum value.
 *                   If true -> validation is done for minimum value.
 */
function validateDateInput(value, fieldDef, min){
    if (!isValidDate(value)){
        return null;
    }
    //verify that the given input is in the expected range
    var date = customDateParse(value);
    var fieldConstraint = getFieldConstraint(fieldDef);
    if (min){
        if (date.getTime() < Number(fieldConstraint.rangeMin)){
            return new Date(Number(fieldConstraint.rangeMin));
        }
    } else {
        if (date.getTime() > Number(fieldConstraint.rangeMax)){
            return new Date(Number(fieldConstraint.rangeMax));
        }
    }
    return date;
}

/**
 * Checks for the following valid date format: MM/DD/YY hh:mm:ss
 * @param dateStr  string that needs to be validated as date.
 */
function isValidDate(dateStr) {
 var datePat = /^(\d{1,2})(\/)(\d{1,2})\2(\d{4})( )(\d{1,2})(:)(\d{1,2})():(\d{1,2})$/;

    var matchArray = dateStr.match(datePat); // check the format
    if (matchArray == null) {
        alert("Date is not in a valid format.")
        return false;
    }
    month = matchArray[1]; // parse date into variables
    day = matchArray[3];
    year = matchArray[4];
    if (month < 1 || month > 12) { // check month range
        alert("Month must be between 1 and 12.");
        return false;
    }
    if (day < 1 || day > 31) {
        alert("Day must be between 1 and 31.");
        return false;
    }
    if ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31) {
        alert("Month " + month + " doesn't have 31 days!")
        return false
    }
    if (month == 2) { // check for february 29th
        var isleap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
        if (day > 29 || (day == 29 && !isleap)) {
            alert("February " + year + " doesn't have " + day + " days!");
            return false;
        }
    }
    return true;  // date is valid
}

/**
 * Parses the string received as parameter to make a Date object by the pattern: MM/dd/yyyy hh:mm:ss
 * @param input string to be parsed as date
 */
function customDateParse(input) {
    var datePat = /^(\d{1,2})(\/)(\d{1,2})\2(\d{4})( )(\d{1,2})(:)(\d{1,2})():(\d{1,2})$/;
    var m = input.match(datePat);
    return new Date(m[4], m[1] - 1, m[3], m[6], m[8], m[10]);
}

/**
 * replaces the filters from visualizationDef with a new Array and sends it to the server to persist.
 */
function resetFilters() {
    if (!testForGraph()) {
        return false;
    }
    if (!visualizationDef) {
        csi.viz.getVisualization(vizuuid, {
                    onsuccess: function(data) {
                        visualizationDef = data.resultData;
                        updateFiltersAndSaveSettings(new Array());
                    },
                    onerror: function(xhr, status) {
                        alert('Error getting visualization');
                    }
                });
    } else {
        updateFiltersAndSaveSettings(new Array());
    }
}

/**
 * change the filterFields in visualizationDef and send the updated visualizationDef back to server to merge it.
 */
function updateFiltersAndSaveSettings(array) {
    visualizationDef.filterFields = array;
    csi.viz.saveSettings(dvuuid, visualizationDef, {
        onsuccess: function(data) {
            closeFilterPanel();
            loadGraph();
        },
        onerror: function(xhr, status) {
            alert('Error saving settings');
        }
    });
}

/**
 * Hides the filterPanel and removes its content.
 */
function closeFilterPanel() {
    $("#filterPanel").hide();
    var filterContentDiv = document.getElementById("filterContentDiv");
    deleteChildren(filterContentDiv);
}

/**
 * Format the received date parameter using MM/dd/yyyy pattern.
 * @param date
 */
function formatDate(date) {
    if (date) {
        var curr_date = date.getDate();
        var curr_month = date.getMonth();
        curr_month++;
        var curr_year = date.getFullYear();
        var curr_hour = date.getHours();
        var curr_minute = date.getMinutes();
        var curr_seconds = date.getSeconds();
        return  curr_month + "/" + curr_date + "/" + curr_year + " " + curr_hour + ":" + curr_minute + ":" + curr_seconds;
    }
    return null;
}

/**
 * Fnction used to determine the preloaded value of a date/time field.
 * If it is a saved filter, the function returns the formatted user's value.
 * If it is a new filter, the function returns the default value received as param.
 * @param value
 * @param defaultDate
 */
function autoCompleteValue(value, defaultDate){
    if (value){
        var date = new Date(Number(value));
        return formatDate(date);
    } else {
        return defaultDate;
    }
}

/**
 * Sorts ascending two fields.
 * @param a Object of type FieldDef.
 * @param b Object of type FieldDef.
 */
function sortfunction(a, b) {
    var fieldNameA = a.fieldName, fieldNameB = b.fieldName;
    return sortStrings(fieldNameA, fieldNameB);
}

function sortStrings(string1, string2){
    string1 = string1.toLowerCase();
    string2 = string2.toLowerCase();
    if (string1 < string2) //sort string ascending
        return -1;
    if (string1 > string2)
        return 1;
    return 0
}

	function exportGraphData() {
		alert('exporting data');
	}

	function exportAsImage() {
		alert('exporting as image');
	}

	function hideLegend() {
		$("#legendPanel").css('visibility', 'hidden');
	}

	function enableLegend() {
		if (!testForGraph()){return false;}
		
		$("#legendPanel").css('visibility', 'visible');
	}

	function collapseExpandLegend(target) {
		if (target.innerHTML == '-') {
			$("#legendBody").hide();
			target.innerHTML = '+';
		} else {
			$("#legendBody").show();
			target.innerHTML = '-';
		}
	}

	function setDragMode(mode) {
		action = mode;
		
		if (mode == 'pan') {
			$('#display').css('cursor', 'move');
		} else {
			$('#display').css('cursor', 'crosshair');
		}
		
		$('input:radio[value="' + mode + '"]').attr('checked', 'checked');
		
		return false;
	}
	
	function downloadGraphImage() {
		if (!testForGraph()){return false;}
		var d = $('#display')[0];
		displayH = d.curh;
		displayW = d.curw; 
		csi.relgraph.downloadGraphImage(dvuuid, vizuuid, displayW, displayH);
	}
		
	function downloadSelectedData() {
		if (!testForGraph()){return false;}
		csi.relgraph.downloadSelectedData(dvuuid, vizuuid);
	}

	function downloadGraphData() {
		csi.relgraph.downloadGraphData(vizuuid);
	}
	
	function testForGraph(){
		if(vizuuid=='null'||vizuuid==null){
			alert("No relationship graphs found.")
			return false;
		}
		
		return true;
	}
	
	function onSpinOffMenuItemClick() {
	    csi.relgraph.hasSelection(vizuuid, {
	        onsuccess: function(data) {
	            if(data.resultData == true){
	            	showSpinOffPanel();
	            }
	            else{
	            	alert('No selection exists to create spinoff with.');
	            }
	        },
	        onerror: function(xhr, status) {
	            alert('Error performing spinoff.');
	        }
	    });
	}
	
	function showSpinOffPanel(){
		var currentViewName = parent.frames['left'].getCurrentViewName();
		$('#spinnerPanel').show();
		
		csi.dataview.getUniqueDataviewName(currentViewName+" Spinoff", {
			onsuccess: function(data, status, xhr){
				$('#spinnerPanel').hide();
				$("#spinoffName").val(data.resultData);
		
				$('#spinoffPanel').show();
				$('#spinoffErrors').hide();
			},
			
			onerror: function(xhr, status) {
				$('#spinoffErrors').text('We couldn\'t get a unique dataview name').show();
				$('#spinoffErrors').show();
			}
		});
	}
	
	function onSpinOffCancelBtnClick() {
		$('#spinoffPanel').hide();
	}
	
	function onSpinOffCreateBtnClick() {
		$('#spinoffErrors').hide();
		var name = $('#spinoffName').val();
		
		csi.dataview.dataviewNameExists(name, {
			onsuccess: function(data, status, xhr) {
				$('#spinnerPanel').show();
				$('#spinoffPanel').hide();
				doSpinoff(name);
			},
	
			onerror: function(xhr, status) {
				$('#spinoffErrors').text('This dataview name is not available, please choose another one.').show();
				$('#spinoffErrors').show();
			}
		});
	}
	
	function doSpinoff(dvName) {
		csi.dataview.spinoff(dvuuid, vizuuid, dvName, {
			onsuccess: function(data, status, xhr) {
				$('#spinnerPanel').hide();
				alert("A spinoff dataview has been created.");
			},
	
			onerror: function(xhr, status) {
				$('#spinoffErrors').text('We could not create the spinoff.').show();
				$('#spinoffErrors').show();
			}
		});
	}

