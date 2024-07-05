/**
 * @name csi.dataview
 * @namespace
 * @description Dataview Graph API
 */
define(["api/csi"], function(csi) {
    var invokefn = csi.service.invoke;
    
    var dataview = {
        /**
         * Internal use only.
         */
        serviceurl: 'actions/dataview',
        
        /**
         * @name csi.dataview.getDataView
         * @function
         * @description Retrieves the DataView object based on its uuid.
         * @param {String} dvid The UUID of the dataview to retrieve.
         * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
         * for more details.
         * @since 1.1
         */
        getDataView: function(dvid, options){
            options = options || {};
            options.query = options.query || {};
            options.query.uuid = dvid;
            invokefn(dataview, 'getDataView', options);
        },

	/**
	 * Similar to getDataView, but returns EVERYTHING (i.e. worksheets and all), and in XML instead of JSON
	 */
        openDataView: function(dvid, options){
            options = options || {};
            options.query = options.query || {};
            options.query.uuid = dvid;
            invokefn(dataview, 'openDataView', options);
        },
        
        /**
         * @name csi.dataview.dataviewNameExists
         * @function
         * @description Checks if a dataview name is available or not.
         * @param {String} name The name that will be checked.
         * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
         * for more details.
         * @since 1.2
         */
        dataviewNameExists: function(name, options){
            options = options || {};
            options.query = options.query || {};
            options.query.name = name;
            invokefn(dataview, 'dataviewNameExists', options)
        },
        
        /**
         * @name csi.dataview.getUniqueDataviewName
         * @function
         * @description Creates a unique name from the provided name.  If the provided name is already unique, it is returned.  If not, a number is appended in parentheses.
         * @param dvName Name to use as the starting point.
         * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
         * for more details.
         * @since 1.2
         */
        getUniqueDataviewName: function(dvName, options){
            options = options || {};
            options.query = options.query || {};
            options.query.dvName = dvName;
            invokefn(dataview, 'getUniqueDataviewName', options);
        },
        
        /**
         * @name csi.dataview.spinoff
         * @function
         * @description Creates a spinoff dataview based on the selection from the specified visualization.
         * @param {String} dvid The UUID of the dataview that contains the chosen relationship graph.
         * @param {String} vizid The UUID of the relationship graph to load.
         * @param {String} name The name of the new dataview.  Note that this name must be unique.
         * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
         * for more details.
         * @since 1.2
         */
        spinoff: function(dvid, vizid, name, options){
            options = options || {};
            options.query = options.query || {};
            options.query.spinoffName = name;
            var payload = {
                "class": "csi.server.common.dto.SpinoffRequestV2",
                dataViewUuid: dvid,
                visualizationUuid: vizid
            }
            options.data = JSON.stringify(payload);
            options.method = 'POST';
            options.contentType = "application/json";
            options.contentLength = payload.length;
            invokefn(dataview, 'spinoff', options)
        }
    };
    
    csi.dataview = dataview;
    return dataview;
});
