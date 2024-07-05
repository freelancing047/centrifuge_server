class com.ammap.Utils {
	
	// parse true
	static function parseTrue (value, if_undefined:Boolean):Boolean {
		var res:Boolean = false;
		if(value == true){
			res = true;
		}
		if(Utils.stripSymbols(value.toLowerCase(), " ") == "true"){
			res = true;
		}
		if(Utils.stripSymbols(value.toLowerCase(), " ") == "yes"){
			res = true;
		}
		if(value > 0){
			res = true;
		}
		if(value == "" or value == undefined){
			res = if_undefined;
		}
		return (res);
	}
	
	static function toNumber(value, if_nan):Number{
		var res = Number(value);
		if(isNaN(res) == true){
			res = if_nan;
		}
		return(res);
	}
	
	static function toColor(value, if_undefined){
		var res = Number("0x" + value.substr(-6));
		if(isNaN(res) == true){
			res = if_undefined;
		}
		return(res);
	}
	
	static function checkUndefined(value, if_undefined){
		if(value == undefined){
			return(if_undefined);
		}
		return(value);
	}
	
	
	static function changeBoolean(value:Boolean):Boolean{
		if(value == true){
			return(false);
		}
		if(value == false){
			return(true);
		}		
	}
	
	static function replace(string:String, find, replace){
		return string.split(find).join(replace);
	}
	
	static function stripSymbols (string:String, symbol:String):String {
		var tempArray:Array = string.split(symbol);
		return (tempArray.join(""));
	}		
	
	static function validateFileName(string:String):String{
		string = Utils.replace(string, "<", "&lt;");
		string = Utils.replace(string, ">", "&gt;");		
		return string;
	}	
	
	static function formatNumber(num:Number, decimals_separator:String, thousands_separator:String){
		
		if(decimals_separator == undefined){
			decimals_separator = ",";
		}
		if(thousands_separator == undefined){
			thousands_separator = " ";
		}		
		
		// check if negative
		
		if(num < 0){
			var negative = "-";
		}
		else {
			var negative = "";
		}
		
		num = Math.abs(num);
		
		var number_str = num.toString();
		var array = number_str.split(".");		
		var formated:String = "";		
		
		var string = array[0].toString();
		
		for(var i = string.length; i >= 0; i = i - 3){
			if(i != string.length){
				if(i != 0){
					formated = string.substring(i-3, i) + thousands_separator + formated;
				}
				else{
					formated = string.substring(i-3, i) + formated;
				}
			}
			else {
				formated = string.substring(i-3, i);
			}
		}
		if(array[1] != undefined){
			formated = formated + decimals_separator + array[1];
		}
		
		formated = negative + formated;
		
		return(formated);
	}
	
	public static function tweekAddress(address:String, path:String){
		if(address.substr(0,7) != "http://" or address.substr(0,8) != "https://" ){
			address = path + address;
		}
		return(address);
	}

	public static function isArray(obj){
		if(typeof(obj) == "object"){
			if(isNaN(obj.length) == true){
				return(false);
			}
			else{
				return(true);
			}
		}
		else{
			return(false);
		}
	}
	
	// convert object to array
	public static function objectToArray(obj:Object){
		if (typeof(obj) == "object" && Utils.isArray(obj) == false){		
			var temp_obj:Object = obj;
			obj = new Array();
			obj.push(temp_obj);
		}
		return(obj);
	}
	
	public static function fitToBounds(number:Number, min:Number, max:Number):Number {		
		if(number < min){
			number = min;
		}
		if(number > max){
			number = max;
		}
		return(number);
	}		
	
	public static function roundTo(number:Number, precision:Number){
		return(Math.round(number * Math.pow(10,precision)) / Math.pow(10, precision));
	}
	
	public static function getRef (obj, string){
		// check if more than one
		if(string.indexOf(".") != -1){
			var str = substring(string, 0, string.indexOf("."));
			var remaining_str = substring(string, string.indexOf(".") + 2, string.length);
		}
		else{
			var str = string;
			var remaining_str = undefined;
		}
		
		// check if string is Array
		if(str.indexOf("[") != -1 && str.indexOf("]") != -1){
			var obj_ref = str.substring(0, str.indexOf("["));			
			var index = str.substring(str.indexOf("[")+1, str.indexOf("]"));
			var ref = obj[obj_ref][index];
		}
		else{
			var ref = obj[str];
		}
		// 
		if(remaining_str != undefined){
			return(getRef(ref, remaining_str));
		}
		return(ref);
	}	
	
	public static function rotateText(text_field, bg_color, target_mc, name, depth){
		text_field.autoSize = "left";
		text_field.multiline = false;
		text_field.wordWrap = false;
		text_field.textWidth = 2000;
		var bmp = new flash.display.BitmapData(text_field._width, text_field._height, true, bg_color);
		bmp.draw(text_field);
			
		var copy = target_mc.createEmptyMovieClip(name, depth);
		copy.attachBitmap(bmp, 0);
		copy._rotation = - 90;
		text_field._visible = false;
		copy._x = text_field._x;
		copy._y = text_field._y;		
		return(copy);
	}
	
	public static function formatText(config, text, title, value, percents, description){
			if(description != undefined){
				text = Utils.replace(text, "{description}", description);
			}		
			if(title != undefined){
				text = Utils.replace(text, "{title}", title);
			}
			else{
				text = Utils.replace(text, "{title}", "");
			}
			if(value != undefined && isNaN(value) != true){
				text = Utils.replace(text, "{value}", Utils.formatNumber(value, config.decimals_separator, config.thousands_separator));
			}
			else{
				text = Utils.replace(text, "{value}", "");
			}
			text = Utils.replace(text, "{percents}", Utils.formatNumber(Utils.roundTo(percents, config.precision), config.decimals_separator, config.thousands_separator));
			

		return(text);
	}
	
	public static function longitudeToPixels(projection, longitude, left_longitude:Number, right_longitude:Number, width:Number){
		if(typeof(longitude) == "object"){
			var array = new Array();
			for(var i = 0; i < longitude.length; i++){
				array[i] = (Number(longitude[i]) - left_longitude) / (right_longitude - left_longitude) * width;
			}
			return array;
		}
		else{
			return (Number(longitude) - left_longitude) / (right_longitude - left_longitude) * width;
		}
	}
	
	public static function latitudeToPixels(projection, latitude, top_latitude:Number, bottom_latitude:Number, height:Number){
		if(typeof(latitude) == "object"){
			var array = new Array();
			for(var i = 0; i < latitude.length; i++){
				if(projection == "mercator"){
					array[i] = (mercatorLatToY(latitude[i]) - mercatorLatToY(top_latitude)) / (mercatorLatToY(bottom_latitude) - mercatorLatToY(top_latitude)) * height;
				}
				else{
					array[i] = (Number(latitude[i]) - top_latitude) / (bottom_latitude - top_latitude) * height;
				}
			}
			return array;
		}
		else{
			if(projection == "mercator"){
				return (mercatorLatToY(latitude) - mercatorLatToY(top_latitude)) / (mercatorLatToY(bottom_latitude) - mercatorLatToY(top_latitude)) * height;
			}
			else{
				return (Number(latitude) - top_latitude) / (bottom_latitude - top_latitude) * height;				
			}
		}
	}	
	
	
	public static function mercatorLatToY(lat){		
		lat = Number(lat);
		if (lat > 89.5){
			lat = 89.5;
		}
		if (lat < -89.5){
			lat = -89.5;
		}		
		
		var lat_r = Utils.degreesToRadians(lat);
	
		var y = 0.5 * Math.log((1 + Math.sin(lat_r)) / (1 - Math.sin(lat_r)));
		
		var y_dg = radiansToDegrees(y / 2);		
		
		return(y_dg);
	}
	
	public static function pixelsToMercatorLatitude(projection, y, top_latitude:Number, bottom_latitude:Number, height:Number){
		var lat = y * (mercatorLatToY(bottom_latitude) - mercatorLatToY(top_latitude)) / height + mercatorLatToY(top_latitude);
		var lat_r = lat * 2 * Math.PI / 180;		
		var lat_m = (2 * (Math.atan(Math.exp(lat_r)))) - 0.5 * Math.PI;
		lat_m = radiansToDegrees(lat_m);
		return(lat_m);
	}
	
	public static function degreesToRadians(degrees){
		return (degrees / 180) * Math.PI;
	}
	
	public static function radiansToDegrees(radians){
		return (radians  / Math.PI) * 180;
	}	
	
	
	public static function getCoordinate(value, full){
		// if there is ! in the beginning, then calculate right or bottom
		if(value.substr(0,1) == "!"){
			value = full - Number(value.substr(1));
		}
		
		// if values is set in percents, recalculate to pixels
		if(value.substr(-1) == "%"){
			var coord = full * Number(value.substr(0, value.length - 1)) / 100;
		}
		else{
			var coord = Number(value);
			if(isNaN(coord)){
				coord = undefined;
			}
		}
		return coord;
	}
	
	public static function getCoordinateFromArray(array, full){
		var new_array = new Array();
		for (var i = 0; i < array.length; i++){
			new_array[i] = getCoordinate(array[i], full);
		}
		return new_array;
	}
	
	
	public static function checkIfClickable(data_source){
		if(data_source.url != undefined || (data_source.description != undefined && data_source.description != "" && data_source.text_box != false) || data_source.zoom != undefined){			
			return(true);
		}
		if(data_source.movies.movie.length > 0 || data_source.labels.label.length > 0 || data_source.lines.line.length > 0){
			return (true);
		}
		return false;
	}
}