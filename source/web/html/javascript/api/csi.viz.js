/**
 * @name csi.viz
 * @namespace
 * @description Visualizations Graph API
 */
define(["api/csi"], function(csi) {
    var invokefn = csi.service.invoke;
    
    var viz = {
        /**
         * Internal use only.
         */
        serviceurl: 'actions/viz',
        
        /**
         * @name csi.viz.getFilterConstraints
         * @function
         * @description Builds the filter constraints for a given filter fields list.
         * The filter constraints represent possible values of the given field and its min/max ranges.
         * @param dvid         the dataview uuid.
         * @param vizid        the visualization (graph) uuid.
         * @param filterFields the fields of the dataview for which user defines the filters.
         * @param options      params of the request to the server.
         * @since 1.0
         */
        getFilterConstraints: function(dvid, vizid, filterFields, options){
            options = options || {};
            options.query = options.query || {};
            options.query.vduuid = vizid;
            var filterConstraintsRequest = new FilterConstraintsRequest(dvid, vizid, filterFields);
            var payload = JSON.stringify(filterConstraintsRequest);
            options.data = payload;
            options.method = 'POST';
            options.contentType = "application/json";
            options.contentLength = payload.length;
            invokefn(viz, 'getFilterConstraints', options);
        },
        
        /**
         * @name csi.viz.getVisualization
         * @function
         * @description Retrieves the visualization object based on its uuid.
         * @param vizid        Visualization (graph) uuid.
         * @param options      params of the request to server.
         * @since 1.2
         */
        getVisualization: function(vizid, options){
            options = options || {};
            options.query = options.query || {};
            options.query.uuid = vizid;
            invokefn(viz, 'getVisualization', options);
        },
        
        /**
         * @name csi.viz.saveSettings
         * @function
         * @description Persists the visualization vizDef.
         * @param dvid     the dataview uuid for which this visualization is defined.
         * @param vizDef   the visualization (graph) that needs to be saved.
         * @param options  params of the request to server.
         * @since 1.2
         */
        saveSettings: function(dvid, vizDef, options){
            options = options || {};
            options.query = options.query || {};
            options.query.dvUuid = dvid;
            var payload = JSON.stringify(vizDef);
            options.data = payload;
            options.method = 'POST';
            options.contentType = "application/json";
            options.contentLength = payload.length;
            invokefn(viz, 'saveSettings', options);
        }
    };
    
    /**
     * Constructor of a FilterConstraintRequest Object.
     * @param dvid      the dataview uuid for which this visualization is defined.
     * @param vizid     the visualization uuid for which the filters are defined.
     * @param filters   list of filterFields.
     * @since 1.2
     */
    function FilterConstraintsRequest(dvid, vizid, filters){
        this["class"] = "csi.server.common.dto.FilterConstraintsRequest";
        this.dvUuid = dvid;
        this.vizUuid = vizid;
        this.filters = filters;
    }
    
    csi.viz = viz;
    return viz;
});
