function  TimePlayer(vizuuid){
	this.dvuuid = window.dataview.myData.resultData.uuid;
	this.vizuuid = vizuuid;
	this.min;
	this.max;
	this.stop = false;
	this.pause = false;
	this.time = 1000;
}
TimePlayer.prototype.doGetRangeByField =  function(){
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var startFieldName = $('#start-option' +this.vizuuid).val();
	var startFieldUuid = utils.getFieldDef(fieldDefs, startFieldName).uuid;
	var endFieldName = $('#end-option' +this.vizuuid).val();
	var endFieldUuid = "";
	var durationValue = 1;
	var durationPeriod = "entire";
	if ($('#time-span'+this.vizuuid).is(':checked')) {
		if (!_.isEmpty(endFieldName)){
			endFieldUuid = utils.getFieldDef(fieldDefs, endFieldName).uuid;
		}
		durationPeriod = $('#select-duration'+this.vizuuid).val();
		if (durationPeriod != "entire"){
			durationValue = $('#duration-value'+this.vizuuid).val();
		}
	}
	var tp = this;
	var doSuccess = function() {
	    return function(data) {
	    	tp.max = data.resultData.max;
	    	tp.min = data.resultData.min;
	    	if(_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")  || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
	    		$('#from-date-display' + tp.vizuuid).text($.formatDateTime('D, M. d, yy At gg:ii:ss.u a',new Date(parseInt(tp.min))));
		    	$('#to-date-display' + tp.vizuuid).text($.formatDateTime('D, M. d, yy At gg:ii:ss.u a',new Date(parseInt(tp.max))));
	    	}
	    	else{
		    	$('#from-date-display' + tp.vizuuid).text($.formatDateTime('M. d, yy',new Date(parseInt(tp.min))));
		    	$('#to-date-display' + tp.vizuuid).text($.formatDateTime('M. d, yy',new Date(parseInt(tp.max))));
	    	}
	    	$('#from-date' + tp.vizuuid).attr('data-date',new Date(parseInt(tp.min)));
	    	$('#to-date' + tp.vizuuid).attr('data-date',new Date(parseInt(tp.max)));
	    	var startDate = $.formatDateTime('yy-mm-dd', new Date(parseInt(tp.min)));
	    	var endDate = $.formatDateTime('yy-mm-dd', new Date(parseInt(tp.max)));
	    	$('#from-date' + tp.vizuuid).datepicker('setStartDate',startDate);
	    	$('#from-date' + tp.vizuuid).datepicker('setEndDate',endDate );
	    	$('#to-date' + tp.vizuuid).datepicker('setStartDate',startDate);
	    	$('#to-date' + tp.vizuuid).datepicker('setEndDate',endDate);
	    	$('#from-date' + tp.vizuuid).datepicker('setValue',new Date(parseInt(tp.min))); 
	    	$('#to-date' + tp.vizuuid).datepicker('setValue',new Date(parseInt(tp.max)));
	    };
	};
	csi.timeplayer.getRangeByField(this.dvuuid, this.vizuuid, startFieldUuid, endFieldUuid, durationValue, durationPeriod, {
		onsuccess: doSuccess()
	});
}
TimePlayer.prototype.doActivatePlayer = function(startValue, endValue){
	$('#status' + this.vizuuid).text("Activating");
	var tp = this;
	this.stop = false;
	this.pause = false;
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var fieldName = $('#start-option' +this.vizuuid).val();
	var startField = utils.getFieldDef(fieldDefs, fieldName).uuid;
	var endField = "";
	var playbackMode = "CUMULATIVE";
	var stepMode = "RELATIVE";
	var durationValue = 1;
	var durationPeriod = "entire";
	var stepSizeValue = 1;
	var stepSizePeriod = $('#timeplayer-tab-step-select'+ this.vizuuid).val();
	if (stepSizePeriod == 'occurance'){
		stepSizePeriod = 'millisecond';
	}else{
		stepSizeValue = $('#stepsize-value'+this.vizuuid).val();
	}
	var frameSizeValue = 1;
	var frameSizePeriod = "year";
	var hideNonVisibleItems = false;
	if($('#time-span'+this.vizuuid).is(':checked')) {
		playbackMode = "TIME_SPAN";
		frameSizePeriod	= $('#time-span-size-select'+this.vizuuid).val();
		frameSizeValue	= $('#timespan-value'+this.vizuuid).val();
		hideNonVisibleItems = $('#hide-inactive-items'+this.vizuuid).is(':checked');
		durationPeriod = $('#select-duration'+this.vizuuid).val();
		if (durationPeriod != "entire"){
			durationValue = $('#duration-value'+this.vizuuid).val();
		}
		if($('#end-option'+this.vizuuid).val()){
			endField = utils.getFieldDef(fieldDefs, $('#end-option'+this.vizuuid).val()).uuid;
		}
	}
	var doSuccess = function() {
	    return function(data) {
			console.log(data);
	    	tp.doRefresh();
	    };
	};
	csi.timeplayer.activatePlayer(this.dvuuid, this.vizuuid, startField, endField, playbackMode, stepMode, durationValue, durationPeriod, stepSizeValue, stepSizePeriod, frameSizeValue, frameSizePeriod, startValue, endValue, hideNonVisibleItems, {
		onsuccess: doSuccess()
	});
}
TimePlayer.prototype.doStart = function(){
	this.setRefreshTime();
	if (this.pause  && !this.stop) {
		this.pause = false;
		this.doRefresh();
	} else {
		var startDate = new Date($('#from-date'+this.vizuuid).datepicker().attr('data-date')).getTime();
		var endDate = new Date($('#to-date'+this.vizuuid).datepicker().attr('data-date')).getTime();
		this.doActivatePlayer(startDate, endDate);
	}
	this.stop = false;
}

TimePlayer.prototype.doStepPlayer =  function(){
	var vizuuid = this.vizuuid;
	var tp = this;
	var doSuccess = function() {
	    return function(data) {
			new RefreshImage(vizuuid).doTask();
			console.log(data);
			var maxDays = (tp.max -  tp.min);
			var progressInDays = (data.resultData[0]-tp.min);
			var progressInPercent = parseInt((progressInDays/maxDays) * 100);
			$('#progress-bar'+vizuuid).css('width',progressInPercent+'%');
			if ($('#time-span'+vizuuid).is(':checked')){
				var START_DATE= new Date(parseInt(data.resultData[0]));
				var MAX_DATE = new Date(parseInt(tp.max));
				var today = new Date(parseInt(data.resultData[0]));
				var duration = parseInt($('#timespan-value'+vizuuid).val());
				var nxtDay = today;
				var timespan_mode = $('#time-span-size-select'+vizuuid).val();
				if (_.isEqual(timespan_mode,'day')){
					if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear()) && _.isEqual(START_DATE.getMonth(),MAX_DATE.getMonth())){
						if ((START_DATE.getDate()+duration) < MAX_DATE.getDate()){
							nxtDay = new Date(today.setDate(today.getDate()+duration));
						}
						else if ((START_DATE.getHours()+duration) < MAX_DATE.getHours()){
							nxtDay = new Date(today.setHours(today.getHours()+duration));
						}
						else if ((START_DATE.getMinutes()+duration) < MAX_DATE.getMinutes()){
							nxtDay = new Date(today.setMinutes(today.getMinutes()+duration));
						}
						else if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
							nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
						}
						else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
							nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
						}
					}
					else {
						nxtDay = new Date(today.setDate(today.getDate()+duration));
					}
				}
				else if (_.isEqual(timespan_mode,'month')){
					if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear())){
						if ((START_DATE.getMonth()+duration) < MAX_DATE.getMonth()){
							nxtDay = new Date(today.setMonth(today.getMonth()+duration));
						}
						else if ((START_DATE.getDate()+duration) < MAX_DATE.getDate()){
							nxtDay = new Date(today.setDate(today.getDate()+duration));
						}
						else if ((START_DATE.getHours()+duration) < MAX_DATE.getHours()){
							nxtDay = new Date(today.setHours(today.getHours()+duration));
						}
						else if ((START_DATE.getMinutes()+duration) < MAX_DATE.getMinutes()){
							nxtDay = new Date(today.setMinutes(today.getMinutes()+duration));
						}
						else if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
							nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
						}
						else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
							nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
						}
					}
					else {
						nxtDay = new Date(today.setMonth(today.getMonth()+duration));
					}
				}
				else if (_.isEqual(timespan_mode,'year')){
					if ((START_DATE.getFullYear()+duration) < MAX_DATE.getFullYear()){
						nxtDay = new Date(today.setFullYear(today.getFullYear()+duration));
					}
					else if ((START_DATE.getMonth()+duration) < MAX_DATE.getMonth()){
						nxtDay = new Date(today.setMonth(today.getMonth()+duration));
					}
					else if ((START_DATE.getDate()+duration) < MAX_DATE.getDate()){
						nxtDay = new Date(today.setDate(today.getDate()+duration));
					}
					else if ((START_DATE.getHours()+duration) < MAX_DATE.getHours()){
						nxtDay = new Date(today.setHours(today.getHours()+duration));
					}
					else if ((START_DATE.getMinutes()+duration) < MAX_DATE.getMinutes()){
						nxtDay = new Date(today.setMinutes(today.getMinutes()+duration));
					}
					else if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
						nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
					}
					else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
						nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
					}
				}
				if (_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time") || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
					if (_.isEqual(timespan_mode,'hour')){
						if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear()) && _.isEqual(START_DATE.getMonth(),MAX_DATE.getMonth()) && _.isEqual(START_DATE.getDate(),MAX_DATE.getDate())){
							if ((START_DATE.getHours()+duration) < MAX_DATE.getHours()){
								nxtDay = new Date(today.setHours(today.getHours()+duration));
							}
							else if ((START_DATE.getMinutes()+duration) < MAX_DATE.getMinutes()){
								nxtDay = new Date(today.setMinutes(today.getMinutes()+duration));
							}
							else if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
								nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
							}
							else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
								nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
							}
						}
						else{
							nxtDay = new Date(today.setHours(today.getHours()+duration));							
						}
					}
					else if (_.isEqual(timespan_mode,'minute')){
						if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear()) && _.isEqual(START_DATE.getMonth(),MAX_DATE.getMonth()) && _.isEqual(START_DATE.getDate(),MAX_DATE.getDate()) && _.isEqual(START_DATE.getHours(),MAX_DATE.getHours())){
							if ((START_DATE.getHours()+duration) < MAX_DATE.getHours()){
								nxtDay = new Date(today.setHours(today.getHours()+duration));
							}
							else if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
								nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
							}
							else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
								nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
							}
						}
						else{
							nxtDay = new Date(today.setMinutes(today.getMinutes()+duration));							
						}
					}
					else if (_.isEqual(timespan_mode,'second')){
						if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear()) && _.isEqual(START_DATE.getMonth(),MAX_DATE.getMonth()) && _.isEqual(START_DATE.getDate(),MAX_DATE.getDate()) && _.isEqual(START_DATE.getHours(),MAX_DATE.getHours()) && _.isEqual(START_DATE.getMinutes(),MAX_DATE.getMinutes())){
							if ((START_DATE.getSeconds()+duration) < MAX_DATE.getSeconds()){
								nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
							}
							else if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
								nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
							}
						}
						else {
							nxtDay = new Date(today.setSeconds(today.getSeconds()+duration));
						}
					}
					else if (_.isEqual(timespan_mode,'millisecond')){
						if (_.isEqual(START_DATE.getFullYear(),MAX_DATE.getFullYear()) && _.isEqual(START_DATE.getMonth(),MAX_DATE.getMonth()) && _.isEqual(START_DATE.getDate(),MAX_DATE.getDate()) && _.isEqual(START_DATE.getHours(),MAX_DATE.getHours()) && _.isEqual(START_DATE.getMinutes(),MAX_DATE.getMinutes()) && _.isEqual(START_DATE.getSeconds(),MAX_DATE.getSeconds())){
							if ((START_DATE.getMilliseconds()+duration) < MAX_DATE.getMilliseconds()){
								nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
							}
						}
						else {
							nxtDay = new Date(today.setMilliseconds(today.getMilliseconds()+duration));
						}
					}
				}
				var format = tp.doFindFormat();
				$('#progress-status'+vizuuid).text('Viewing '+$.formatDateTime(format,new Date(parseInt(data.resultData[0])))+' through '+$.formatDateTime(format,nxtDay));
			}
	       	else{
	       		if (_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time") || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
	       			$('#progress-status'+vizuuid).text('Viewing through '+$.formatDateTime('M. d, yy At gg:ii:ss.u a',new Date(parseInt(data.resultData[0]))));
	       		}
	       		else {
	       			$('#progress-status'+vizuuid).text('Viewing through '+$.formatDateTime('M. d, yy',new Date(parseInt(data.resultData[0]))));
	       		}
	       	}
			if (parseInt(data.resultData[0]) < parseInt(tp.max)){
				tp.doRefresh();
			} else {
				_.delay(function() { tp.doCleanUpOnComplete(); }, 1000); 
			}
	    };
	};
	csi.timeplayer.stepPlayer(this.vizuuid,{
		onsuccess: doSuccess()
	});
}
TimePlayer.prototype.doSeek =  function(){
	var doSuccess = function() {
	    return function(data) {
	    	// TODO
	    };
	};
	csi.timeplayer.seek(this.vizuuid, position, {
		onsuccess: doSuccess()
	});
}
TimePlayer.prototype.doStopPlayer =  function(){
	var vizuuid = this.vizuuid;
	var tp = this;
	var doSuccess = function() {
	    return function(data) {
	    	tp.stop = true;
	    	var play = $('#play'+ tp.vizuuid);
	    	$(play).removeClass('pauseControl');
	    	$(play).addClass('playControl');
	    	$('#progress-bar'+vizuuid).css('width','0%');
	    	$('#status' + vizuuid).text("Stopped");
	    };
	};
	csi.timeplayer.stopPlayer(this.vizuuid, {
		onsuccess: doSuccess()
	});
}
TimePlayer.prototype.doPausePlayer =  function(){
	$('#status' + this.vizuuid).text("Paused");
	this.pause = true;
}
TimePlayer.prototype.setRefreshTime =  function(){
	if ($("#timeplayer-tab-speed-fast" + this.vizuuid).is(":checked")){
		this.time = 1000;
	} else if ($("#timeplayer-tab-speed-slow" + this.vizuuid).is(":checked")){
		this.time = 20000;
	} else if ($("#timeplayer-tab-speed-moderate" + this.vizuuid).is(":checked")){
		this.time = 10000;
	}
}
TimePlayer.prototype.doRefresh =  function(){
	$('#status' + this.vizuuid).text("Playing");
	if (this.time == 10000){
		this.refreshSliderModerate();
	} else if (this.time == 20000){
		this.refreshSliderSlow();
	} else {
		this.refreshSliderFast();
	}
}

TimePlayer.prototype.refreshSliderSlow = _.debounce(function() {
	this.doStep();
}, 20000);

TimePlayer.prototype.refreshSliderModerate = _.debounce(function() {
	this.doStep();
}, 10000);

TimePlayer.prototype.refreshSliderFast = _.debounce(function() {
	this.doStep();
}, 1000);

TimePlayer.prototype.doStep =  function(){
	if (this.stop || this.pause){
		return;
	}
	console.log("I got called");
	this.doStepPlayer();
}
TimePlayer.prototype.doCleanUpOnComplete = function() {
	var play = $('#play'+ this.vizuuid);
	$(play).removeClass('pauseControl');
	$(play).addClass('playControl');
	$('#progress-bar'+this.vizuuid).css('width','0%');
	$('#status' + this.vizuuid).text("Stopped");
	$('#progress-status'+this.vizuuid).css('visibility','hidden');
}
TimePlayer.prototype.doFindFormat =  function(){
	var stepMethod = $('#timeplayer-tab-step-select'+ this.vizuuid).val();
	var isDateTime =  false;
	if (_.isEqual($('#start-option'+ this.vizuuid).find("option:selected").attr('type'),'date/time') && !_.isEqual($('#end-option'+ this.vizuuid).find("option:selected").attr('type'),'date/time')){
		isDateTime = true;
	}
	if(!stepMethod == 'occurance'){
		if (stepMethod == 'millisecond'){
			return 'D, M. d, yy At gg:ii:ss.u a';
		}
		else if (stepMethod == 'second'){
			return 'D, M. d, yy At gg:ii:ss a';
		}
		else if(stepMethod == 'minute'){
			return 'M. d, yy At gg:ii a';
		}
		else if(stepMethod == 'hour'){
			return 'M. d, yy At gg a';
		}
		else if(stepMethod == 'day'){
			if (isDateTime){
				return 'M. d, yy At gg a';
			}
			else{
				return 'M. d, yy';
			}
		}
		else if(stepMethod == 'month'){
			return 'M. d, yy';
		}
		else if(stepMethod == 'year'){
			return 'M, yy';
		}
	}
	else{
		if (isDateTime){
			return 'D, M. d, yy At gg:ii:ss.u a';
		}
		else{
			return 'M. d, yy';
		}
	}
}