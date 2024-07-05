/**
 * @name csi.relgraph
 * @namespace
 * @description Relationship Graph API
 */
(function(root){

	var invokefn = root.service.invoke;
	
	var graph = root.relgraph ||
	{
	
		/**
		 * Internal use only.
		 */
		serviceurl: 'services/graphs2/actions',
		
		/**
		 * @name csi.relgraph.loadGraph
		 * @function
		 * @description Prepares the specified relationship graph for use.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to load.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		loadGraph: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.dvuuid = dvid;
			options.query.vduuid = vizid;
			invokefn(graph, 'loadGraph', options);
		},
		
		/**
		 * @name csi.relgraph.findItemAt
		 * @function
		 * @description Finds the node or link at the specifed x/y coordinate.  If multiple items
		 * overlap, it will return the topmost item.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} x The x coordinate, offset from the upper left of the current viewport.
		 * @param {number} y The y coordinate, offset from the upper left of the current viewport.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		findItemAt: function(vizid, x, y, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.x = x;
			options.query.y = y;
			invokefn(graph, 'findItem2', options);
		},
		
		/**
		 * @name csi.relgraph.selectItemAt
		 * @function
		 * @description Selects the node or link at the specifed x/y coordinate.  If multiple items
		 * overlap, it will select the topmost item.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} x The x coordinate, offset from the upper left of the current viewport.
		 * @param {number} y The y coordinate, offset from the upper left of the current viewport.
		 * @param {boolean} reset true to clear the current selection before selecting the chosen graph item, or
		 * false to add the chosen graph item to the current selection.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectItemAt: function(vizid, x, y, reset, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.x = x;
			options.query.y = y;
			options.query.reset = reset;
			invokefn(graph, 'selectItemAt', options);
		},

		/**
		 * @name csi.relgraph.clearSelection
		 * @function
		 * @description Clears the current graph selection.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		clearSelection: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'clearSelection', options);
		},
		
		/**
		 * @name csi.relgraph.selectAll
		 * @function
		 * @description Selects all items (nodes and links) in the specified graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectAll: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'selectAll', options);
		},
		
		/**
		 * @name csi.relgraph.hideSelected
		 * @function
		 * @description Marks the currently selected items as hidden.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		hideSelected: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'hideSelection', options);
		},
		
		/**
		 * @name csi.relgraph.hideUnSelected
		 * @function
		 * @description Marks the currently unselected items as hidden.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		hideUnSelected: function(vizid, options){
		    options = options || {};
		    options.query = options.query || {};
		    options.query.vduuid = vizid;
		    invokefn(graph, 'hideUnSelected', options);
		},
        
		/**
		 * @name csi.relgraph.unhideSelected
		 * @function
		 * @description Marks the currently selected items as unhidden.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		unhideSelected: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'unhideSelection', options);
		},
		/**
		 * @name csi.relgraph.unhideAll
		 * @function
		 * @description Operation used to unhide all nodes and links that are hidden.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		unhideAll: function(vizid, options){
		    options = options || {};
		    options.query = options.query || {};
		    options.query.vduuid = vizid;
		    invokefn(graph, 'unhideAll', options);
		},

		/**
		 * @name csi.relgraph.manuallyBundleSelection
		 * @function
		 * @description Manually bundles selected nodes and links.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} bundleName The name of the bundle.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		manuallyBundleSelection: function(vizid, bundleName, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
            options.query.bundleName = bundleName;
			invokefn(graph, 'manuallyBundleSelection', options);
		},
 
 		/**
		 * @name csi.relgraph.bundleEntireGraphBySpec
		 * @function
		 * @description Bundles nodes and links by specified bundle definition.
		 * @param {String} dvid The UUID of the dataview to operate on.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		bundleEntireGraphBySpec: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
            options.query.dvuuid   = dvid;
			invokefn(graph, 'bundleEntireGraphBySpec', options);
		},
        
  		/**
		 * @name csi.relgraph.bundleSelectionBySpec
		 * @function
		 * @description Bundles selected nodes and links by specified bundle definition..
		 * @param {String} dvid The UUID of the dataview to operate on.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		bundleSelectionBySpec: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
            options.query.dvuuid   = dvid;
			invokefn(graph, 'bundleSelectionBySpec', options);
		},   
        
 		/**
		 * @name csi.relgraph.unbundleEntireGraph
		 * @function
		 * @description Unbundles entire graph's nodes and links.
		 * @param {String} dvid The UUID of the dataview to operate on.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		unbundleEntireGraph: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
            options.query.dvuuid   = dvid;
			invokefn(graph, 'unbundleEntireGraph', options);
		},             
         
 		/**
		 * @name csi.relgraph.unbundleSelection
		 * @function
		 * @description Unbundles selected nodes and links.
		 * @param {String} dvid The UUID of the dataview to operate on.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		unbundleSelection: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
            options.query.dvuuid   = dvid;
			invokefn(graph, 'unbundleSelection', options);
		},       
                      		
		/**
		 * @name csi.relgraph.selectRegion
		 * @function
		 * @description Selects all graph items (nodes and links) in the region bounded by x1,y1 and x2,y2.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} x1 The x coordinate of the first bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} y1 The y coordinate of the first bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} x2 The x coordinate of the second bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} y2 The y coordinate of the second bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {boolean} reset true to clear the current selection before selecting the chosen graph item(s),
		 * or false to add the chosen graph item(s) to the current selection.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectRegion: function(vizid, x1, y1, x2, y2, reset, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.x1 = x1;
			options.query.y1 = y1;
			options.query.x2 = x2;
			options.query.y2 = y2;
			options.query.reset = reset;
			invokefn(graph, 'selectRegion', options);
		},
		
		/**
		 * @name csi.relgraph.itemInfo
		 * @function
		 * @description Retrieves information about the specified node or link.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} itemid The ID of the node or link
		 * @param {String} type 'node' to retrieve information about a node, or 'link' to retrieve information
		 * about an link.
		 * @param {boolean} tips true to return tooltip information, false to omit tooltip information.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		itemInfo: function(vizid, itemid, type, tips, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.itemid = itemid;
			options.query.type = type;
			options.query.tips = tips || false;
			invokefn(graph, 'itemInfo', options);
		},
		
		/**
		 * @name csi.relgraph.selectionInfo
		 * @function
		 * @description Retrieves information about the current selection, including all selected item keys and item IDs.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectionInfo: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'selectionInfo', options);
		},
		
		/**
		 * @name csi.relgraph.legendData
		 * @function
		 * @description Retrieves information required to display a graph legend, including node type names, node counts, shapes,
		 * colors, and icon URIs (if any).
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		legendData: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'legendData', options);
		},

		/**
         * @name csi.relgraph.toggleLabels
         * @function
         * @description Toggles display of labels on the graph.
         * @param {String} vizid The UUID of the relationship graph to operate on.
         * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers. See {@link csi.service.invoke}
         * for more details.
         * @since 1.0
         */
        toggleLabels: function(vizid, options) {
            options = options || {};
            options.query = options.query || {};
            options.query.vduuid = vizid;
            invokefn(graph, 'showLabels', options);
        },

		/**
		 * @name csi.relgraph.fitToSize
		 * @function
		 * @description Configures the graph to fit inside the last known viewport size.  Viewport size is set via a call
		 * to {@link csi.relgraph.graphImageUrl}()
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		fitToSize: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'fitToSize', options);
		},
		
		/**
		 * @name csi.relgraph.fitToSelected
		 * @function
		 * @description Configures the graph so that all selected items fit inside the last known viewport size.  Viewport
		 * size is set via a call to {@link csi.relgraph.graphImageUrl}()
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		fitToSelected: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'fitToSelection', options);
		},
		
		/**
		 * @name csi.relgraph.zoomToRegion
		 * @function
		 * @description Zooms the graph so the specified bounds fit inside the last known viewport size.  Viewport
		 * size is set via a call to {@link csi.relgraph.graphImageUrl}()
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} x1 The x coordinate of the first bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} y1 The y coordinate of the first bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} x2 The x coordinate of the second bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {number} y2 The y coordinate of the second bounds coordinate, offset from the upper left of
		 * the current viewport.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		zoomToRegion: function(vizid, x1, y1, x2, y2, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.x1 = x1;
			options.query.y1 = y1;
			options.query.x2 = x2;
			options.query.y2 = y2;
			options.query.id = 'default.selection';
			invokefn(graph, 'zoomToRegion', options);
		},
		
		/**
		 * @name csi.relgraph.zoomPercent
		 * @function
		 * @description Increases or decreases the graph size based on the specified percentage.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} percent How far to zoom in or out -- a negative number will zoom out, and a positive number will zoom in.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		zoomPercent: function(vizid, percent, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.percent = percent;
			invokefn(graph, 'zoomPercent', options);
		},
		/**
		 * @name csi.relgraph.zoomTo
		 * @function
		 * @description Increases or decreases the graph size based on the specified scale.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} zoom How far to zoom in or out -- a negative number will zoom out, and a positive number will zoom in.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		zoomTo: function(vizid, zoom, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.zoom = zoom;
			invokefn(graph, 'zoomTo', options);
		},
		
		/**
		 * @name csi.relgraph.pan
		 * @function
		 * @description Pans the visible graph by the specified amounts in the x and y dimensions.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} dx How many pixels to pan left/right.  A negative number will pan left, a positive number will pan right.
		 * @param {number} dy How many pixels to pan up/down.  A negative number will pan up, a positive number will pan down.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		pan: function(vizid, dx, dy, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.x = dx;
			options.query.y = dy;
			invokefn(graph, 'panTo', options);
		},
		
		/**
		 * @name csi.relgraph.doLayout
		 * @function
		 * @description Lays out the chosen graph using the specified layout algorithm.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} type One of the following:<br><ul>
		 * 						<li>"circular" for a circular layout</li>
		 * 						<li>"forceDirected" for a force-directed layout</li>
		 * 						<li>"radial" for a radial hierarchy</li>
		 * 						<li>"hierarchical" for a linear hierarchy</li>
		 * 						<li>"scramble" for a scramble & place layout</li>
		 *	 					<li>"centrifuge" for a Centrifuge layout</li>
		 * </ul>
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		doLayout: function(vizid, type, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.action = 'layout';
			options.query.value = type;
			options.query.x = 0;
			options.query.y = 0;
			options.query.componentId = 0;
			invokefn(graph, 'componentLayoutAction', options);
		},
		
		/**
		 * @name csi.relgraph.layout
		 * @namespace
		 * @description Convenience functions for laying out a relationship graph.
		 */
		layout: {
			/**
			 * @name csi.relgraph.layout.circular
			 * @function
			 * @description A convenience function for running a circular layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			circular: function(vizid, options){
				graph.doLayout(vizid, 'circular', options);
			},
			
			/**
			 * @name csi.relgraph.layout.forceDirected
			 * @function
			 * @description A convenience function for running a force directed layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			forceDirected: function(vizid, options){
				graph.doLayout(vizid, 'forceDirected', options);
			},
			
			/**
			 * @name csi.relgraph.layout.radialHierarchy
			 * @function
			 * @description A convenience function for running a radial hierarchy layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			radialHierarchy: function(vizid, options){
				graph.doLayout(vizid, 'radial', options);
			},
			
			/**
			 * @name csi.relgraph.layout.linearHierarchy
			 * @function
			 * @description A convenience function for running a linear hierarchy layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			linearHierarchy: function(vizid, options){
				graph.doLayout(vizid, 'hierarchical', options);
			},
			
			/**
			 * @name csi.relgraph.layout.scrambleAndPlace
			 * @function
			 * @description A convenience function for running a scramble and place layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			scrambleAndPlace: function(vizid, options){
				graph.doLayout(vizid, 'scramble', options);
			},
			
			/**
			 * @name csi.relgraph.layout.centrifuge
			 * @function
			 * @description A convenience function for running a Centrifuge layout on the specified relationship graph.
			 * @param {String} vizid The UUID of the relationship graph to operate on.
			 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
			 * for more details.
			 * @since 1.0
			 */
			centrifuge: function(vizid, options){
				graph.doLayout(vizid, 'centrifuge', options);
			}
		},
		
		/**
		 * @name csi.relgraph.listNodes
		 * @function
		 * @description Retrieves information about all nodes in the current graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		listNodes: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'nodeListing', options);
		},
		
		/**
		 * @name csi.relgraph.listLinks
		 * @function
		 * @description Retrieves information about all links in the current graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		listLinks: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'edgeListing', options);
		},
		
		/**
		 * @name csi.relgraph.nodeNeighbors
		 * @function
		 * @description Retrieves a list of IDs for all first-degree neighbors of the specified node.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} nodeid The ID of the node whose neighbors should be retrieved.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		nodeNeighbors: function(vizid, nodeid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.nodeId = nodeid;
			invokefn(graph, 'nodeNeighbors', options);
		},
		
		/**
		 * @name csi.relgraph.selectNodesByType
		 * @function
		 * @description Selects all nodes of the specified type.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} type The type of the nodes to select.  A list of available node types can be retrieved
		 * by calling {@link csi.relgraph.summary}()
		 * @param {boolean} reset true to clear the current selection before selecting the chosen node(s), or
		 * false to add the chosen node(s) to the current selection.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectNodesByType: function(dvid, vizid, type, reset, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.dvuuid = dvid;
			options.query.nodeType = type;
			options.query.addToSelection = (!reset);
			invokefn(graph, 'selectNodesByType', options);
		},
		
		/**
		 * @name csi.relgraph.selectLinkById
		 * @function
		 * @description Selects the link with the specified ID.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} id The ID of the link to select.
		 * @param {boolean} reset true to clear the current selection before selecting the chosen link, or
		 * false to add the chosen link to the current selection.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectLinkById: function(vizid, id, reset, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.id = id;
			options.query.reset = reset;
			invokefn(graph, 'selectLinkById', options);
		},
		
		/**
		 * @name csi.relgraph.selectNodeById
		 * @function
		 * @description Selects the node with the specified ID.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} id The ID of the node to select.
		 * @param {boolean} reset true to clear the current selection before selecting the chosen node, or
		 * false to add the chosen node to the current selection.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectNodeById: function(vizid, id, reset, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.id = id;
			options.query.reset = reset;
			invokefn(graph, 'selectNodeById', options);
		},
		
		/**
		 * @name csi.relgraph.computeSNA
		 * @function
		 * @description Computes SNA metrics for the specified graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		computeSNA: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'computeSNA', options);
		},
		
		/**
		 * @name csi.relgraph.selectNeighbors
		 * @function
		 * @description Selects all neighbors up to the specified number of degrees away from the current selection.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} degrees The number of degrees to traverse and select.  For example, 1 will select only the
		 * immediate neighbors of the current selection.  2 will select both the current selection's immediate
		 * neighbors and the immediate neighbors of the current selection's immediate neighbors.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectNeighbors: function(vizid, degrees, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.degrees = degrees;
			invokefn(graph, 'selectVisibleNeighbors', options);
		},
		
		/**
		 * @name csi.relgraph.summary
		 * @function
		 * @description Retrieves a summary of information about the chosen graph.  Information includes the number of
		 * visible and total nodes, the number of visible and total links, and node types.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		summary: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'summary', options);
		},
		
		/**
		 * @name csi.relgraph.nodePosition
		 * @function
		 * @description Retrieves the x/y coordinates of the specified node relative to the graph's origin, as well
		 * as the x/y coordinates relative to the current viewport.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} nodeid The ID of the node whose coordinates to retrieve.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		nodePosition: function(vizid, nodeid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.nodeid = nodeid;
			invokefn(graph, 'getNodePosition', options);
		},
		
		/**
		 * @name csi.relgraph.placeNode
		 * @function
		 * @description Moves the specified node to the chosen location on the graph canvas.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {String} nodeid The ID of the node whose coordinates to retrieve.
		 * @param {number} x The new x position for the node.
		 * @param {number} y The new y position for the node.
		 * @param {boolean} abs (optional) false to place the node relative to the current viewport (the default),
		 * or true to place the node relative to the graph's origin.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		placeNode: function(vizid, nodeid, x, y, abs, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			options.query.nodeid = nodeid;
			options.query.x = x;
			options.query.y = y;
			options.query.abs = abs;
			invokefn(graph, 'setNodePosition', options);
		},
		
		/**
		 * @name csi.relgraph.selectedCsvUrl
		 * @function
		 * @description Creates a URL to retrieve a CSV file of information about all selected nodes and links.
		 * @returns The URL in a string.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		selectedCsvUrl: function(dvid, vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.serviceurl = "actions/savedata";
			options.query.dvUuid = dvid;
			options.query.vizUuid = vizid;
			var url = csi.service.makeServiceUrl(graph, 'getTableDataCsv', options);
			url = csi.service.applyUrlParams(url, options.query);
			return url;
		},
		
		/**
		 * @name csi.relgraph.exportGraphUrl
		 * @function
		 * @description Creates a URL to retrieve a compressed data file of raw Centrifuge graph information.
		 * @returns The URL in a string.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		exportGraphUrl: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			var url = csi.service.makeServiceUrl(graph, 'exportGraph', options)
			url = csi.service.applyUrlParams(url, options.query);
			return url;
		},
		
		/**
		 * @name csi.relgraph.graphImageUrl
		 * @function
		 * @description Creates a URL to retrieve an image of the graph with the specified viewport size.
		 * <br><b>IMPORTANT</b>: Note that the viewport size is stored and used for subsequent API calls.
		 * @returns The URL in a string.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} vw The width of the current viewport, in pixels.
		 * @param {number} vh The height of the current viewport, in pixels.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		graphImageUrl: function(dvid, vizid, vw, vh, options){
			options = options || {};
			options.query = options.query || {};
			options.query.dvuuid = dvid;
			options.query.vduuid = vizid;
			options.query.vw = vw;
			options.query.vh = vh;
			options.query._r = (new Date()).getTime();
			var url = csi.service.makeServiceUrl(graph, 'getDisplay', options);
			url = csi.service.applyUrlParams(url, options.query);
			return url;
		},
		/**
		 * @name csi.relgraph.getDisplay
		 * @function
		 * @description Creates a URL to retrieve an image of the graph with the specified viewport size.
		 * <br><b>IMPORTANT</b>: Note that the viewport size is stored and used for subsequent API calls.
		 * @returns The URL in a string.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} vw The width of the current viewport, in pixels.
		 * @param {number} vh The height of the current viewport, in pixels.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		getDisplay: function(dvid, vizid, vw, vh, options){
			options = options || {};
			options.query = options.query || {};
			options.query.dvuuid = dvid;
			options.query.vduuid = vizid;
			options.query.vw = vw;
			options.query.vh = vh;
			options.query._r = (new Date()).getTime();
			invokefn(graph, 'getDisplay', options);
		},
		
		/**
		 * @name csi.relgraph.saveGraph
		 * @function
		 * @description Saves the current graph state.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.0
		 */
		saveGraph: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'saveGraph', options)
		},
		
		/**
		 * @name csi.relgraph.downloadGraphImage
		 * @function
		 * @description Prompts the browser to save an image of the current graph with the specified size.
		 * <br><b>IMPORTANT</b>: Note that this changes the current viewport size.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @param {number} w The width of the image to save, in pixels.
		 * @param {number} h The height of the image to save, in pixels.
		 * @since 1.0
		 */
		downloadGraphImage: function(dvid, vizid, w, h){
			var url = graph.graphImageUrl(dvid, vizid, w, h);
			csi.downloadFile(url);
		},
		
		/**
		 * @name csi.relgraph.downloadSelectedData
		 * @function
		 * @description Prompts the browser to save a CSV file of information about the current graph selection.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @since 1.0
		 */
		downloadSelectedData: function(dvid, vizid){
			var url = graph.selectedCsvUrl(dvid, vizid);
			csi.downloadFile(url);
		},
		
		/**
		 * @name csi.relgraph.downloadGraphData
		 * @function
		 * @description Prompts the browser to save a .dat file of compressed, raw Centrifuge graph data.
		 * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
		 * @param {String} vizid The UUID of the relationship graph to operate on.
		 * @since 1.0
		 */
		downloadGraphData: function(vizid){
			var url = graph.exportGraphUrl(vizid);
			csi.downloadFile(url);
		},
		
		/**
		 * @name csi.relgraph.version
		 * @function
		 * @description Returns the current Centrifuge relationship graph API version.
		 * @returns A string containing the version.
		 * @since 1.0
		 */
		version: function(){
			return csi.version();
		},

		/**
		 * @name csi.relgraph.getDragImage
		 * @function
		 * @description Gets the Drag graph image.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.2
		 */
		getDragImageUrl: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			var url = csi.service.makeServiceUrl(graph, 'getDragImage', options);
			url = csi.service.applyUrlParams(url, options.query);
			return url;
		},
		/**
		 * @name csi.relgraph.dragStart
		 * @function
		 * @description Invoked before the node drag starts.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.2
		 */
		dragStart: function(vizid, startx, starty, options){
				options = options || {};
				options.query = options.query || {};
				options.query.vduuid = vizid;
				options.query.startx = startx;
				options.query.starty = starty;
				invokefn(graph, 'dragStart', options)
		},
		/**
		 * @name csi.relgraph.dragEnd
		 * @function
		 * @description Invoked after the node drag ends.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.2
		 */
		dragEnd: function(vizid, endx, endy, options){
				options = options || {};
				options.query = options.query || {};
				options.query.vduuid = vizid;
				options.query.endx = endx;
				options.query.endy = endy;
				invokefn(graph, 'dragEnd', options)
		},
		/**
		 * @name csi.relgraph.hasSelection
		 * @function
		 * @description Checks if a relationship graph has selected nodes or links.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.2
		 */
		 hasSelection: function(vizid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vizid;
			invokefn(graph, 'hasSelection', options)
		}
	};
	 
	csi.relgraph = graph;
}(csi));
