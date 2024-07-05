var lineColor = "#2424ff";
var lineHoverColor = "#fe9898";
var lineClickedColor = "#98fe98";
function resetRenderMode(desiredMode, divsWithOperationClass) {
    var newMode = jsPlumb.setRenderMode(desiredMode);
    $(".rmode").removeClass("selected");
    $(".rmode[mode='" + newMode + "']").addClass("selected");
    var disableList = (newMode === jsPlumb.VML) ? ".rmode[mode='canvas'],.rmode[mode='svg']" : ".rmode[mode='vml']";
    $(disableList).attr("disabled", true);
    jsPlumbDemo.init(divsWithOperationClass);
};

function getAllNodes() {
	var nodes = $('.operation .node-label').map(function(index, element) {
		return $(element).text();
	});
	nodesObj = {
		nodes: nodes
	}
	return nodesObj;
}

function getallNodesWithPosition() {
	var nodes = $('.operation .node-label').map(function(index, element) {
		var imageURL = $(element).parent().css('background-image');
		var color;
		var shape;
		$.each(imageURL.split('&'), function(index, value) {
			if(value.indexOf('shape') != -1) {
				shape = value.substring(6);
			}else if(value.indexOf('color') != -1){
				color = value.substring(6, value.length - 1);
			}
		});
		
		return {name: $(element).text(), position: $(element).parent().position(), uuid: $(element).parent().attr('uuid'), color : color, shape : shape};
	});
	nodesObj = {
		nodes: nodes
	}
	return nodesObj;
}

function generateOperationsDiv(ui, nodeUiProps) {
	var nodeName = ui.draggable.find('span').text();
	var nodeDetailsObj = {
		index: 0,
		name: nodeName,
		top: ui.position.top,
		left: ui.position.left + 50,
		imageURL : nodeUiProps.nodeImageUrl,
		shape : nodeUiProps.shape,
		color : nodeUiProps.color,
		image : nodeUiProps.image
	}
	return createSettingsNodesDiv(nodeDetailsObj);
}

function createSettingsNodesDiv(nodeDetailsObj) {
	var anchor_div = $('<div>').addClass('anchor node-links');
	var anchor_wrapper = $('<div>').addClass('anchor-wrapper event-wrapper');
	var edit_div = $('<div>').addClass('node-edit node-links');
	var edit_wrapper = $('<div>').addClass('node-edit-wrapper event-wrapper');
	//var nodeName = ui.draggable.find('span').text();
	var label_div = $('<div>').addClass('node-label').text(nodeDetailsObj['name']);
	var delete_div = $('<div>').addClass('node-delete node-links');
	var delete_wrapper = $('<div>').addClass('node-delete-wrapper event-wrapper');
	edit_wrapper.click(function() {
		var name = $(this).parent().find('.node-label').text();
		var imageURL = $(this).parent().css('background-image');
		var shape = $(this).parent().attr('shape');
		var color = $(this).parent().attr('color');
		var icon = $(this).parent().attr('image');		
		var uuid = $(this).parent().attr('uuid');
		var editNodeDialog = new CreateNodeEditDialog(name, shape, color, icon, uuid);
		editNodeDialog.fetchVisualisation();
	});
	delete_wrapper.click(function() {
		var vizDef = $('#treeFinish-rg').data('vizDef');
		var nodeName = $(this).parent().find('.node-label').text();
		if (vizDef != undefined) {
			var nodeDefIdx = utils.getNodeDefIdx(vizDef.nodeDefs, nodeName);
			vizDef.nodeDefs.splice(nodeDefIdx, 1);
			if (vizDef.bundleDefs.length > 0) {
				$.each(vizDef.bundleDefs[0].operations, function (index, operation) {
					if (operation.nodeDef.name == nodeName) {
						vizDef.bundleDefs[0].operations.splice(index, 1);
					}
				});
			}
		}		
		deleteNodes($(this));
	});
	
	var operations_div = $('<div>').addClass('operation').attr('title', nodeDetailsObj['name']);		
	operations_div.css('background-image', 'url('+nodeDetailsObj.imageURL+')');
	operations_div.attr('shape', nodeDetailsObj.shape).attr('color', nodeDetailsObj.color).attr('image', nodeDetailsObj.image);
	$.each(['top', 'right', 'left', 'bottom'], function(index, position) {
		if(nodeDetailsObj[position]) {
			operations_div.css(position, nodeDetailsObj[position]);
		}
	});
	if(nodeDetailsObj.randomTop && nodeDetailsObj.randomLeft) {
		operations_div.offset({
			top: nodeDetailsObj.randomTop,
			left: nodeDetailsObj.randomLeft
		});
	}
	operations_div.append(label_div).append(anchor_div).append(anchor_wrapper);
	operations_div.append(edit_div).append(edit_wrapper);
	operations_div.append(delete_div).append(delete_wrapper);
	return operations_div;	
}

function getAllConnections() {
	var connsArray = [];
	var connections = jsPlumb.getConnections();
	$.each(connections, function(index, connection) {
		var startPoint = $('#'+ connection.endpoints[0].elementId);
		var endPoint = $('#'+ connection.endpoints[1].elementId);
		var startPointColumnName = startPoint.find('.node-label').text();
		var endPointColumnName = endPoint.find('.node-label').text();
		connsArray.push([startPointColumnName, endPointColumnName]);
	});
	var connsObject = {
		connections: connsArray
	}
	return connsObject;
}


function isNotAddedToDomAndFromLeftPanel(draggable) {
	var nodeName = draggable.find('span').text();
	var nodesArray = getAllNodes()['nodes'];
	var isNotInDom = ($.inArray(nodeName, nodesArray) == -1);
	var isFromLeftPanel = draggable.is('.left_container .inlinerows');
	return (isFromLeftPanel && isNotInDom);
}

function deleteNodes(deleteElement) {
	bootbox.confirm("Are you sure you want to delete this node?", function(confirmed) {
		if (confirmed) {
			var parentDiv = deleteElement.parent();
			jsPlumb.detachAllConnections(parentDiv.attr('id'));
			parentDiv.remove();
		}
	}); 
}
function generateTableToolTip(nodeName) {
	var vizDef = $('#treeFinish-rg').data('vizDef');
	var nodeDef = _.filter(vizDef.nodeDefs, function(obj){ return obj.name == nodeName });
	var tooltips = _.filter(nodeDef[0].attributeDefs, function(obj) {
		return !(obj.name == "csi.internal.xPos" ||
		obj.name == "csi.internal.yPos" ||
		obj.name == "csi.internal.Shape" ||
		obj.name == "csi.internal.Color" ||
		obj.name == "csi.internal.Scale" ||
		obj.name == "csi.internal.Icon")	
		});
	var table = $('<table></table>').addClass('tiplist');
	$.each(tooltips, function() {
		var key;
		var value = this.fieldDef.fieldName;
		if( (this.name).match("csi.internal") ){
			key = (this.name).replace("csi.internal.","");
		}else{
			key = this.name;
		}
		if(this.fieldDef.staticText){
		value = '"'+ this.fieldDef.staticText +'"';
		}  	
		var data = $('<tr></tr>').addClass('tipitem').append(
						$('<td>').addClass('tipkey').append(
							key +" : ").append(
								$('<span></span>').addClass('tipitem').append(value)));
		table.append(data);
	});		
	table = $('<div></div>').addClass('graphTooltip nodegraphToolTip').append(table)[0].outerHTML;
    return table;
}
$(function() {
	var edit_image = $("<img style=\"display:none\" src=\"../images/node-edit.png\"> ");
	var delete_image = $("<img  style=\"display:none\" src=\"../images/node-delete.png\">");
	var timeout = null;
	$('.node-diagram').on('mouseout', '.operation', function(event) {
		$(this).find('.node-links').css('visibility', 'hidden');
		$(this).find('.graphTooltip').remove();
		clearTimeout(timeout);
		$(this).prop('title', $(this).data('title'));
	});
	$('.node-diagram').on('mouseover', '.operation', function(event) {
		$(this).find('.node-links').css('visibility', 'visible');
		var ttp = generateTableToolTip(this.title);
		var jsplumbdiv = this;
		timeout = setTimeout(function(){$(ttp).appendTo(jsplumbdiv).css('display', 'block'); }, 2500);
		$(this).data('title', this.title).prop('title', null);
	});
	$('.operation').on('mouseout', '.node-links', function() {
		$(this).css('visibility', 'hidden');
	});
	$('.operation').on('mouseover', '.node-links', function() {
		$(this).css('visibility', 'visible');
	});
	
	
    window.jsPlumbDemo = {
        init : function(divsWithOperationClass) {
            jsPlumb.Defaults.DragOptions = {cursor: 'pointer', zIndex: 2000};
            // default to blue at one end and green at the other
            // jsPlumb.Defaults.EndpointStyles = [{ fillStyle:'#ffffff' }, { fillStyle:'#ffffff' }];
            // blue endpoints 7 px; green endpoints 11.
            
            jsPlumb.Defaults.Endpoints = [ "Blank", "Blank" ];
            
            
    
            //Set up the windows to be draggable.
            jsPlumb.draggable(divsWithOperationClass);
            
            divsWithOperationClass.draggable( "option", "containment", ".node-diagram" );
            
			jsPlumbDemo.initEndpoints(lineColor);

            jsPlumb.makeTarget(divsWithOperationClass, {
				anchor: [ 0.5, 0.5, 0.5, 0.5 ],
				beforeDrop: function(detailsObject) {
					var sourceId = detailsObject['sourceId'];
					var targetId = detailsObject['targetId'];
					var dropEndpoint = detailsObject['dropEndpoint'];
					
					//Initailize the linkdefs
					var vizdef = $.data($('#treeFinish-rg')[0], 'vizDef');
					var relgraph = new RelationGraphJson(window.dataview.myData);
					var sourceNode = $('#'+sourceId).find('.node-label').text();
					var targetNode = $('#'+targetId).find('.node-label').text();
					var connection = [sourceNode, targetNode];
					
					var nodeDef1Idx = utils.getNodeDefIdx(vizdef.nodeDefs, sourceNode);
					var nodeDef2Idx = utils.getNodeDefIdx(vizdef.nodeDefs, targetNode);					
					var linkDef = relgraph.createLinkDefJson(connection, null, vizdef.nodeDefs[nodeDef1Idx], vizdef.nodeDefs[nodeDef2Idx]);
					vizdef.linkDefs.push(linkDef);
					
					s_t_conns = jsPlumb.getConnections({source: sourceId, target: targetId});
					t_s_conns = jsPlumb.getConnections({source: targetId, target: sourceId});
					conns = s_t_conns.concat(t_s_conns);				
					
					return (conns.length == 0);
				}			
			});
			
	        // jsPlumb.bind("mouseenter", function(c) {
	        	// console.log(c);
	        // });
// 	        
	        // jsPlumb.bind("mouseexit", function(c) {
	        	// console.log(c);
	        // });
	        // $('.node-container').bind('click', function() {
	        	// $.each(jsPlumb.getConnections(), function(index, conn) {
	        		// conn.setPaintStyle({strokeStyle: lineColor, lineWidth: 1});
	        		// conn.setHoverPaintStyle({outlineColor: lineHoverColor, outlineWidth: 4});
	        	// });
	        // });
	        
	        
	        jsPlumb.bind("click", function(c) {
	        	$(".link-editor").hide();
				$(".link-delete").hide();
				$.each(jsPlumb.getConnections(), function(index, conn) {
					conn.setPaintStyle({strokeStyle: lineColor, lineWidth: 1});
					conn.setHoverPaintStyle({outlineColor: lineHoverColor, outlineWidth: 4});
				});
				c.setHoverPaintStyle({outlineColor: lineClickedColor, outlineWidth: 4});
				c.setPaintStyle({strokeStyle: lineColor, lineWidth: 1, outlineColor: lineClickedColor, outlineWidth: 4});
				var connlinkEditor = $(c.overlays['0'].canvas.outerHTML).attr('id')
				var connlinkDelete = $(c.overlays['1'].canvas.outerHTML).attr('id')
				$("#" + connlinkEditor).show();
				$("#" + connlinkDelete).show();
	        });
            
        } //End of INIT function
        
        
    };
    
    jsPlumbDemo.initEndpoints = function(nextColour) {
        $(".anchor").each(function(i,e) {
			var p = $(e).parent();
			
			jsPlumb.makeSource($(e), {
				parent:p,
				// anchor:"BottomCenter",
				anchor: [ 0.5, 0.5, 0.5, 0.5 ],
				connector: "Straight",
				connectorStyle: {strokeStyle: lineColor, lineWidth: 1},
				connectorHoverStyle: {outlineColor: lineHoverColor, outlineWidth: 4} ,
				connectorOverlays: [
					[
						"Custom",
						{
							create: function() {
								return edit_image.clone();
							},
							location: 0.45,
							cssClass: "link-editor",
							events: {
								click: function(overlay, originalEvent) {
									var source = overlay.component.source;
									var target = overlay.component.target;
									var linkName = source.attr('title') + '_' + target.attr('title');
									createLinkDialog = new CreateLinkEditDialog(source,target,linkName);
									createLinkDialog.doTask();
								}
							}
						}
					],
					[
						"Custom",
						{
							create: function() {
								return delete_image.clone();
							},
							location: 0.55,
							cssClass:"link-delete",
							events: {
								click: function(overlay, originalEvent) {
									bootbox.confirm("Are you sure you want to delete this link?", function(confirmed) {
					                    if(confirmed){
					                    	var source = overlay.component.source;
											var target = overlay.component.target;
											var node1 = source.attr('title');
											var node2 = target.attr('title');
											var vizdef = $.data($('#treeFinish-rg')[0], 'vizDef');
											$.each(vizdef.linkDefs, function(index, linkDef) {
												if( (linkDef.nodeDef1.name == node1) && (linkDef.nodeDef2.name == node2) ) {
													vizdef.linkDefs.splice(index, 1);
												}
											});
											jsPlumb.detach(overlay.component);
					                    }
					                });
								}
							}
						}
					]
				]
			});
		});		
    };
    
    jsPlumb.bind("ready", function() {
        // chrome fix.
        document.onselectstart = function () { return false; };
        // render mode
        resetRenderMode(jsPlumb.CANVAS, $('.operation'));
    });
    
});

function deleteLinkModal(){
	alert('del')
}

