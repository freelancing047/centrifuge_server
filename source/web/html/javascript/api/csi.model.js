define(["api/csi"], function(csi) {
    var invokefn = csi.service.invoke;
    var model = {
	/**
	 * Internal
	 */
	serviceurl: 'actions/model',
	
	/**
	 * Internal
	 */
	find: function(oid, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    invokefn(model.core, 'find', options);
	},
	
	/**
	 * Internal
	 */
	save: function(data, options) {
	    options = options || {};
	    options.data = data;
	    invokefn(model.core, 'save', options);
	},
	
	/**
	 * Internal
	 */
	saveAs: function(data, newName, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.name = newName;
	    options.data = data;
	    invokefn(model.core, 'saveCurrentAs', options);
	},
	
	/**
	 * Internal
	 */
	saveExistingAs: function(oid, newName, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    options.query.name = newName;
	    invokefn(model.core, 'saveAs', options);
	},
	
	/**
	 * Internal
	 */
	saveList: function(objlist, options) {
	    options = options || {};
	    options.data = objlist;
	    invokefn(model.core, 'saveList', options);
	},
	
	/**
	 * Internal
	 */
	remove: function(oid, options) {
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    invokefn(model.core, 'delete', options);
	},
	
	/**
	 * Internal
	 */
	uniqueResourceName: function(name) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.name = name;
	    invokefn(model.core, 'getUniqueResourceName', options);
	},
	
	/**
	 * Internal
	 */
	testResourceNames: function(namelist) {
	    options = options || {};
	    options.data = namelist;
	    invokefn(model.core, 'isUniqueResourceName', options);
	},
	
	/**
	 * Internal
	 */
	saveClientProperties: function(oid, data, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    invokefn(model.core, 'saveClientProperties', options);
	},
	
	/**
	 * Internal
	 */
	clientProperties: function(oid, data, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    invokefn(model.core, 'getClientProperties', options);
	},
	
	/**
	 * Internal
	 */
	cloneExisting: function(oid, options) {
	    options = options || {};
	    options.query = options.query || {};
	    options.query.uuid = oid;
	    invokefn(model.core, 'cloneObject', options);
	}
    };
    csi.model = model;
    return model;
});
