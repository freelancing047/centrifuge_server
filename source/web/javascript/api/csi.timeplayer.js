/**
 * @name csi.timeplayer
 * @namespace
 * @description Dataview Graph API
 */
(function(root){

	var invokefn = root.service.invoke;
	
	var timeplayer = root.timeplayer ||
	{
	
		/**
		 * Internal use only.
		 */
		serviceurl: 'services/graph/timeplayer',
		
		/**
		 * @name csi.timeplayer.seek
		 * @function
		 * @description Retrieves the DataView object based on its uuid.
		 * @param {String} vduuid The UUID of the visualization.
		 * @param {String} pos The position.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		seek: function(vduuid, pos, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vduuid;
			options.query.pos = pos;
			invokefn(timeplayer, 'seek', options);
		},
		/**
		 * @name csi.timeplayer.getRangeByField
		 * @function
		 * @description Get teh range for teh time player.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		getRangeByField: function(dvuuid, vduuid, startFieldUUID, endFieldUUID, durationNumber, durationPeriod, options){
			options = options || {};
			options.query = options.query || {};
			options.query.dvuuid = dvuuid;
			options.query.vduuid = vduuid;
			options.query.startFieldUUID = startFieldUUID;
			options.query.endFieldUUID = endFieldUUID;
			options.query.durationNumber = durationNumber;
			options.query.durationPeriod = durationPeriod;
			invokefn(timeplayer, 'getRangeByField', options);
		},
		/**
		 * @name csi.timeplayer.activatePlayer
		 * @function
		 * @description Activate the time player.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		activatePlayer: function(dvuuid, vduuid, startField, endField, playbackMode, stepMode, durationNumber, 
				durationPeriod, stepSizeNumber, stepSizePeriod, frameSizeValue, frameSizePeriod, 
				startTime, stopTime, hideNonVisibleItems, options){
			options = options || {};
			options.query = options.query || {};
			options.query.dvuuid = dvuuid;
			options.query.vduuid = vduuid;
			options.query.startField = startField;
			options.query.endField = endField;
			options.query.playbackMode = playbackMode;
			options.query.stepMode = stepMode;
			options.query.durationNumber = durationNumber;
			options.query.durationPeriod = durationPeriod;
			options.query.stepSizeNumber = stepSizeNumber;
			options.query.stepSizePeriod = stepSizePeriod;
			options.query.frameSizeNumber = frameSizeValue;
			options.query.frameSizePeriod = frameSizePeriod;
			options.query.startTime = startTime;
			options.query.stopTime = stopTime;
			options.query.hideNonVisibleItems = hideNonVisibleItems;
			
			invokefn(timeplayer, 'activatePlayer', options);
		},
		/**
		 * @name csi.timeplayer.stepPlayer
		 * @function
		 * @description Moves the player by one step.
		 * @param {String} vduuid The UUID of the visualization.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		stepPlayer: function(vduuid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vduuid;
			invokefn(timeplayer, 'stepPlayer', options);
		},
		/**
		 * @name csi.timeplayer.stopPlayer
		 * @function
		 * @description Stops the player.
		 * @param {String} vduuid The UUID of the visualization.
		 * @param {Object} options Additional options to be passed to the invoke function, including onsuccess and onerror handlers.  See {@link csi.service.invoke}
		 * for more details.
		 * @since 1.1
		 */
		stopPlayer: function(vduuid, options){
			options = options || {};
			options.query = options.query || {};
			options.query.vduuid = vduuid;
			invokefn(timeplayer, 'stopPlayer', options);
		},
	};
	
	csi.timeplayer = timeplayer;
}(csi));
