function CreateTimePlayer(vizuuid) {
	this.vizuuid = vizuuid;
	this.tpHTML;
	this.doCreate();
	this.timePlayer = new TimePlayer(this.vizuuid); 
}
CreateTimePlayer.prototype.doCreate = function() {
	$('#graph-time-player-control-tab'+ this.vizuuid).show();
}

CreateTimePlayer.prototype.doOpen = function() {
	this.doRegister();
	this.fetchColumns();
	this.timePlayer.doGetRangeByField();
}
CreateTimePlayer.prototype.doClose = function() {
	//do the stop player call
	$('#graph-time-player-control-tab'+ this.vizuuid).hide();
}
CreateTimePlayer.prototype.doRegister = function() {
	var tp = this;
	$('#from-date'+ tp.vizuuid).datepicker().on('changeDate', function(ev){
		$(this).attr('data-date',ev.date);
		if (_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time") || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
			$('#from-date-display' + tp.vizuuid).text($.formatDateTime('D, M. d, yy At gg:ii:ss.u a',ev.date));
		}
		else {
			$('#from-date-display' + tp.vizuuid).text($.formatDateTime('M. d, yy',ev.date));
		}
		$('#to-date' + tp.vizuuid).datepicker('setStartDate',$.formatDateTime('yy-mm-dd',ev.date));		
		$(this).datepicker('hide');		
		if (new Date($('#to-date'+ tp.vizuuid).attr('data-date')) < new Date($(this).attr('data-date'))){
			$('#to-date'+ tp.vizuuid).attr('data-date',ev.date);
			if (_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time") || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
				$('#to-date-display' + tp.vizuuid).text($.formatDateTime('D, M. d, yy At gg:ii:ss.u a',ev.date));
			}
			else{
				$('#to-date-display' + tp.vizuuid).text($.formatDateTime('M. d, yy',ev.date));
			}
			$('#to-date'+ tp.vizuuid).datepicker('update',$.formatDateTime('yy-mm-dd',ev.date));
		}
	});
	$('#to-date'+ tp.vizuuid).datepicker().on('changeDate', function(ev){
		$(this).attr('data-date',ev.date);
		if(_.isEqual($('#start-option' +tp.vizuuid).find(":selected").attr('type'),"date/time") || _.isEqual($('#end-option' +tp.vizuuid).find(":selected").attr('type'),"date/time")){
			$('#to-date-display' + tp.vizuuid).text($.formatDateTime('D, M. d, yy At gg:ii:ss.u a',ev.date));
		}
		else{
			$('#to-date-display' + tp.vizuuid).text($.formatDateTime('M. d, yy',ev.date));
		}
		$(this).datepicker('hide');
	});
	var timePlayerHideTimer; // timer for hiding options div
	$("#time-player"+ tp.vizuuid).mouseover(function() {
		clearTimeout(timePlayerHideTimer);
		$("#time-player"+ tp.vizuuid +"#start-option-div"+ tp.vizuuid).slideDown();
	});
	$("#time-player"+ tp.vizuuid).mouseout(function() {
		timePlayerHideTimer = setTimeout(function() {
			$("#time-player"+ tp.vizuuid +"#start-option-div"+ tp.vizuuid).slideUp();
		}, 2000);
	});
	$('#play'+ this.vizuuid).click(function(){
		$('#progress-status'+tp.vizuuid).css('visibility','visible');
		if ($(this).hasClass('pauseControl')){
			$(this).removeClass('pauseControl');
			$(this).addClass('playControl');
			tp.timePlayer.doPausePlayer();
		} else {
			$(this).removeClass('playControl');
			$(this).addClass('pauseControl')
			tp.timePlayer.doStart();
		}
	});
	$('#stop'+ this.vizuuid).click(function(){
		$('#progress-status'+tp.vizuuid).css('visibility','hidden');
		tp.timePlayer.doStopPlayer();
	});
	$('#cumulative'+ this.vizuuid).click(function(){
		$('#timespan-element' + tp.vizuuid).hide();
		$('#timespan-duration' + tp.vizuuid).hide();
	});
	$('#time-span'+ this.vizuuid).click(function(){
		$('#timespan-element' + tp.vizuuid).show();
		$('#timespan-duration' + tp.vizuuid).show();
	});
	$('#stepsize-value' + tp.vizuuid).hide();
	$('#timeplayer-tab-step-select'+ this.vizuuid).change(function(){
		var fromDate = new Date($('#from-date'+tp.vizuuid).attr('data-date'));
		var toDate = new Date($('#to-date'+tp.vizuuid).attr('data-date'));
		var isDateTime = false;
		if (_.isEqual($('#start-option'+ tp.vizuuid).find("option:selected").attr('type'),'date/time') && !_.isEqual($('#end-option'+ tp.vizuuid).find("option:selected").attr('type'),'date/time')){
			isDateTime = true;
		}
		if ($(this).val() != 'occurance'){
			$('#stepsize-value' + tp.vizuuid).show();
			if ($(this).val() == 'millisecond'){
				tp.changeFormatOfTimeDisplay('D, M. d, yy At gg:ii:ss.u a',fromDate,toDate);
			}
			else if ($(this).val() == 'second'){
				tp.changeFormatOfTimeDisplay('D, M. d, yy At gg:ii:ss a',fromDate,toDate);
			}
			else if($(this).val() == 'minute'){
				tp.changeFormatOfTimeDisplay('M. d, yy At gg:ii a',fromDate,toDate);
			}
			else if($(this).val() == 'hour'){
				tp.changeFormatOfTimeDisplay('M. d, yy At gg a',fromDate,toDate);
			}
			else if($(this).val() == 'day'){
				if (isDateTime){
					tp.changeFormatOfTimeDisplay('M. d, yy At gg a',fromDate,toDate);
				}
				else{
					tp.changeFormatOfTimeDisplay('M. d, yy',fromDate,toDate);					
				}
				
			}
			else if($(this).val() == 'month'){
				tp.changeFormatOfTimeDisplay('M. d, yy',fromDate,toDate);
			}
			else if($(this).val() == 'year'){
				tp.changeFormatOfTimeDisplay('M, yy',fromDate,toDate);
			}
		}
		else{
			if (isDateTime){
				tp.changeFormatOfTimeDisplay('D, M. d, yy At gg:ii:ss.u a',fromDate,toDate);
			}
			else{
				tp.changeFormatOfTimeDisplay('M. d, yy',fromDate,toDate);
			}
			$('#stepsize-value' + tp.vizuuid).hide();
		}
	});
	$('#duration-value' + this.vizuuid).hide();
	$('#select-duration'+ this.vizuuid).change(function(){
		if ($(this).val() != 'entire'){
			$('#duration-value' + tp.vizuuid).show();
		}
		else{
			$('#duration-value' + tp.vizuuid).hide();
		}
	});
	$('#start-option'+this.vizuuid).change(function(){
		var startFieldName = $(this).val();
		$('#select-duration'+tp.vizuuid).empty();
		$('#time-span-size-select'+tp.vizuuid).empty();
		$('#timeplayer-tab-step-select'+tp.vizuuid).empty();
		$('#end-option'+ tp.vizuuid).empty();
		$('#end-option'+ tp.vizuuid).append($('<option/>').attr('selected','selected'));
		var fieldDef = utils.getFieldDef(window.dataview.myData.resultData.meta.modelDef.fieldDefs, startFieldName);
		if (_.isEqual(fieldDef.valueType, "date/time")){
			$('#select-duration'+tp.vizuuid).append( new Option('Entire Span','entire') );
			tp.addOptionsForDateTimeSizeValue('#select-duration'+tp.vizuuid);
			tp.addOptionsForSizeValue('#select-duration'+tp.vizuuid);
			tp.addOptionsForDateTimeSizeValue('#time-span-size-select'+tp.vizuuid);
			tp.addOptionsForSizeValue('#time-span-size-select'+tp.vizuuid);
			$('#timeplayer-tab-step-select'+tp.vizuuid).append( new Option('Occurance','occurance') );
			tp.addOptionsForDateTimeSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
			tp.addOptionsForSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
			$('#time-span-size-select'+tp.vizuuid).find('option[value="day"]').attr('selected','selected')
			tp.timePlayer.doGetRangeByField();
		}
		else{
			$('#select-duration'+tp.vizuuid).append( new Option('Entire Span','entire') );
			tp.addOptionsForSizeValue('#select-duration'+tp.vizuuid);
			tp.addOptionsForSizeValue('#time-span-size-select'+tp.vizuuid);
			$('#timeplayer-tab-step-select'+tp.vizuuid).append( new Option('Occurance','occurance') );
			tp.addOptionsForSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
				tp.timePlayer.doGetRangeByField();
		}
		$(_.reject($(this).find('option'), function(obj) {return $(obj).val() == startFieldName;})).each(function(){
			var option = $('<option></option>').attr('value', $(this).val()).text($(this).val()).attr('type',$(this).attr('type'));
			$('#end-option'+ tp.vizuuid).append(option);
		});
		if ($('#timeplayer-tab-step-select'+ tp.vizuuid).val() == 'occurance'){
			$('#stepsize-value' + tp.vizuuid).hide();
		}
	});
	$('#end-option'+this.vizuuid).change(function(){
		if (!_.isEmpty($(this).val())){
			if (_.isEqual($('#start-option'+ tp.vizuuid).find("option:selected").attr('type'),'date/time') && !_.isEqual($('#end-option'+ tp.vizuuid).find("option:selected").attr('type'),'date/time')){
				$('#time-player-invalid-fields-warning').find('#startField').text($('#start-option'+ tp.vizuuid).val());
				$('#time-player-invalid-fields-warning').find('#endField').text($('#end-option'+ tp.vizuuid).val());
				$('#time-player-invalid-fields-warning').modal();
				$('#end-option'+ tp.vizuuid).val('');
			}
			else {
				tp.timePlayer.doGetRangeByField();
			}
		}
		else {
			tp.timePlayer.doGetRangeByField();
		}
	});
}
CreateTimePlayer.prototype.fetchColumns = function() {
	var tp = this;
	var cols = _.filter(window.dataview.myData.resultData.meta.modelDef.fieldDefs, function(obj) { 
							return ((_.isEqual(obj.valueType, "date") || _.isEqual(obj.valueType, "time") || _.isEqual(obj.valueType, "date/time")))});
	$('#start-option'+ tp.vizuuid).empty();
	$('#end-option'+ tp.vizuuid).empty();
	$('#end-option'+ tp.vizuuid).append($('<option/>').attr('selected','selected'));
	$.each(cols, function() {
		var option = $('<option></option>').attr('value', this.fieldName).text(this.fieldName).attr('type',this.valueType);
		$('#start-option'+ tp.vizuuid).append(option);
		if($('#start-option'+ tp.vizuuid).val() != this.fieldName){
			var option = $('<option></option>').attr('value', this.fieldName).text(this.fieldName).attr('type',this.valueType);
			$('#end-option'+ tp.vizuuid).append(option);
		}
	});
	$('#select-duration'+tp.vizuuid).empty();
	$('#time-span-size-select'+tp.vizuuid).empty();
	$('#timeplayer-tab-step-select'+tp.vizuuid).empty();
	if(_.isEqual($('#start-option'+ tp.vizuuid).attr('type'),"date/time")){
		$('#select-duration'+tp.vizuuid).append( new Option('Entire Span','entire') );
		tp.addOptionsForDateTimeSizeValue('#select-duration'+tp.vizuuid);
		tp.addOptionsForSizeValue('#select-duration'+tp.vizuuid);
		tp.addOptionsForDateTimeSizeValue('#time-span-size-select'+tp.vizuuid);
		tp.addOptionsForSizeValue('#time-span-size-select'+tp.vizuuid);
		$('#timeplayer-tab-step-select'+tp.vizuuid).append( new Option('Occurance','occurance') );
		tp.addOptionsForDateTimeSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
		tp.addOptionsForSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
	}
	else{
		$('#select-duration'+tp.vizuuid).append( new Option('Entire Span','entire') );
		tp.addOptionsForSizeValue('#select-duration'+tp.vizuuid);
		tp.addOptionsForSizeValue('#time-span-size-select'+tp.vizuuid);
		$('#timeplayer-tab-step-select'+tp.vizuuid).append( new Option('Occurance','occurance') );
		tp.addOptionsForSizeValue('#timeplayer-tab-step-select'+tp.vizuuid);
	}
}

CreateTimePlayer.prototype.addOptionsForDateTimeSizeValue = function(id){
	$(id).append( new Option('Millisecond(s)','millisecond') );
	$(id).append( new Option('Second(s)','second') );
	$(id).append( new Option('Minute(s)','minute') );
	$(id).append( new Option('Hour(s)','hour') );
}

CreateTimePlayer.prototype.addOptionsForSizeValue = function(id){
	$(id).append( new Option('Day(s)','day') );
	$(id).append( new Option('Month(s)','month') );
	$(id).append( new Option('Year(s)','year') );
}

CreateTimePlayer.prototype.changeFormatOfTimeDisplay = function(format,fromDate,toDate){
	$('#from-date-display' + this.vizuuid).text($.formatDateTime(format,fromDate));
	$('#to-date-display' + this.vizuuid).text($.formatDateTime(format,toDate));
	
}
