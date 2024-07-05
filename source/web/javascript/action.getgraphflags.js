function GetGraphFlags(vizuuid){
	this.vizuuid = vizuuid;
}
GetGraphFlags.prototype.doTask = function(){
	var getGraphFlags = this;
	var vizuuid = this.vizuuid;
	var flags = {};
	flags.hasSelection = false;
	flags.canBundle = false;
	flags.hasHiddenItems = false;
	flags.hasBundles = false;
	flags.hasBundleDefs = false;
	flags.hasVisibleItems = false;
	flags.hasHiddenItemsInSelection = false;
	flags.hasBundleSelected = false;
	flags.hasOnlyLinksSelected = false;
	flags.hasOnlyNodesSelected = false;
	var vizdef = utils.getVisualisation(vizuuid);
	var bundleDef = vizdef.visualization.bundleDefs.length > 0 ?vizdef.visualization.bundleDefs[0].operations : vizdef.visualization.bundleDefs; 
	if(bundleDef.length > 0){
		flags.hasBundleDefs  = true;
	}
	var doSuccess = function(vizuuid, flags) {
		return function(data) {
			
			var url = "/Centrifuge/services/graphs2/actions/hasBundleSelected?&_f=json&vduuid="+vizuuid;
			$.ajax({
				type: "GET",
				processData: false,
				url: url,
				contentType: 'application/json; charset=utf-8',
				dataType: 'json',
				success: function(datas) {
					if(datas.resultData){
							flags.hasBundleSelected = true;
					}
					var resultData = data.resultData;
					var nodeLength = resultData.nodes==undefined ? 0 : resultData.nodes.length;
					var linksLength = resultData.links==undefined ? 0 : resultData.links.length;
					if(nodeLength > 0 || linksLength > 0){
						flags.hasSelection = true;
						if(nodeLength >= 2){
							flags.canBundle = true;
							
						}
					}
					if(nodeLength == 0 && linksLength > 0){
						
					}
					var url = "/Centrifuge/services/graphs2/actions/getGraphStateFlags?&_f=json&vduuid="+vizuuid;
					$.ajax({
						type: "GET",
						processData: false,
						url: url,
						contentType: 'application/json; charset=utf-8',
						dataType: 'json',
						success: function(data) {
						flags.hasBundles = data.resultData.hasBundles;
						flags.hasHiddenItems = data.resultData.hasHiddenItems;
						flags.hasHiddenItemsInSelection = data.resultData.hasHiddenItemsInSelection;
						flags.hasVisibleItems = data.resultData.hasVisibleItems;
						getGraphFlags.handleMenuItems(flags);
						getGraphFlags.handleContextMenu(flags);
						var bundleHandler = new BundleHandler();
						bundleHandler.registerDialog(vizuuid,flags);
						var unbundleHandler = new UnBundleHandler();
						unbundleHandler.registerDialog(vizuuid,flags);
						getGraphFlags.checkLinkUps(vizdef,flags);
						},
						error: function(data) {
							// alert ("Error");
						}
					});
				}
			});
		};
	};
	
	csi.relgraph.selectionInfo(vizuuid, {
		onsuccess: 	doSuccess(vizuuid,flags)
	});
}

GetGraphFlags.prototype.handleMenuItems = function(flags){
	var vizuuid = this.vizuuid;
	var dvuuid = window.dataview.myData.resultData.uuid;
	var vizDef = utils.getVisualisation(vizuuid).visualization;
	var loadOnStartup = vizDef.clientProperties["vizBox.loadOnStartup"];
	if(loadOnStartup) {
		$('#loadonstartup' + vizuuid).html("&#10004; Load On Startup");
	}
	else{
		$('#loadonstartup' + vizuuid).html("Load On Startup");
	}
	$('#alertModal').find('#loadOnStartUp').attr("checked", loadOnStartup);
	$('#bundleselection' + vizuuid).addClass('disabled-link');
	$('#unbundleselection' + vizuuid).addClass('disabled-link');
	$('#deselectall' + vizuuid).addClass('disabled-link');
	$('#hideselection' + vizuuid).addClass('disabled-link');
	$('#unhideselection' + vizuuid).addClass('disabled-link');
	$('#appearanceeditor' + vizuuid).addClass('disabled-link');
	$('#selectneighbor' + vizuuid).addClass('disabled-link');
	$('#revealneighbor' + vizuuid).addClass('disabled-link');
	if(flags.hasSelection){
		$('#deselectall' + vizuuid).removeClass('disabled-link');
		$('#hideselection' + vizuuid).removeClass('disabled-link');
		$('#appearanceeditor' + vizuuid).removeClass('disabled-link');
		$('#selectneighbor' + vizuuid).removeClass('disabled-link');
		$('#revealneighbor' + vizuuid).removeClass('disabled-link');
	}
	if(flags.hasHiddenItemsInSelection){
		$('#unhideselection' + vizuuid).removeClass('disabled-link');
	}
	$('#loadonstartup' + vizuuid).click(function(event){				
		var txt = $('#loadonstartup' + vizuuid).text();
		if(txt == "Load On Startup"){
			$('#loadonstartup' + vizuuid).html("&#10004;Load On Startup");
			$('#alertModal').find('#loadOnStartUp').attr('checked',true);
			vizDef.clientProperties["vizBox.loadOnStartup"] = true;
		}
		else{
			$('#loadonstartup' + vizuuid).html("Load On Startup");
			$('#alertModal').find('#loadOnStartUp').removeAttr('checked');
			vizDef.clientProperties["vizBox.loadOnStartup"] = false;
		}
		csi.viz.saveSettings(dvuuid, true, vizDef);
	});	
	
}

GetGraphFlags.prototype.checkLinkUps = function(vizdef,flags){
	var vizuuid = this.vizuuid;
	$("#linkupDropDown"+ vizuuid).addClass('disabled-link');
	var x = $('#linkupDropDown'+ this.vizuuid).parent();
	//$(x).find($('.dropdown-menu')).html('');
	if(flags.hasSelection == true){
		var nodeDefs = vizdef.visualization.nodeDefs;
		$.each(nodeDefs, function(key, nodes) {
			var moreDetailQueries = nodes.moreDetailQueries;
			var ulcontainer = $('<ul></ul>').addClass('dropdown-menu');
			if(moreDetailQueries.length > 0){
				$.each(moreDetailQueries, function(key, val) {
					var item = {
						'value': val.dataViewName,
						'id': val.dataViewDefId
					};
					var html = Mustache.render($('#menu-item-panel').html(),item); 
					$(ulcontainer).append(html);
				});
				if($(ulcontainer).find('a').length != 0){
					$(x).append(ulcontainer);
				}
				$("#linkupDropDown"+ vizuuid).removeClass('disabled-link');
			}
		});
	}
}

//function to handle menu items in context menu.
GetGraphFlags.prototype.handleContextMenu = function(flags){
	if(flags.hasSelection) {
		$('#contextMenuTemplate' + this.vizuuid + ' .hideSelection').removeAttr('disabled');
		$('#contextMenuTemplate' + this.vizuuid + ' .hideUnselected').removeAttr('disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .showOnly').removeAttr('disabled');
	}
	else{
		$('#contextMenuTemplate' + this.vizuuid + ' .hideSelection').attr('disabled','disabled');
		$('#contextMenuTemplate' + this.vizuuid + ' .hideUnselected').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .nodeSelect').removeAttr('disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .nodeDeselect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .nodeHideSelect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .showOnly').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .revealNeighbor').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .selectNeighbor').attr('disabled','disabled');
	}
	
	if(flags.canBundle){
		$('#contextMenuTemplate' + this.vizuuid + ' .bundle').removeAttr('disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .bundle').removeAttr('disabled');
	}
	else{
		$('#contextMenuTemplate' + this.vizuuid + ' .bundle').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + this.vizuuid + ' .bundle').attr('disabled','disabled');
	}
	
	if(flags.hasBundleSelected == false){
		$('#contextMenuTemplateNode' + this.vizuuid + ' .unbundle').attr('disabled','disabled');			
	}						
}
