import com.ammap.Load;
import com.ammap.Legend;
import com.ammap.PopUp;
import com.ammap.FadeColor;
import com.ammap.Colors;
import com.ammap.Balloon;
import com.ammap.Utils;
import com.ammap.Labels;
import com.ammap.Rectangle;
import com.ammap.Text;
import com.ammap.Lines;
import com.ammap.Movies;
import com.ammap.Window;
import com.ammap.ComboBox;
import com.ammap.NavigationPath;
import mx.transitions.Tween;


class com.ammap.Ammap {	
	private var __inited_interval:Number;
	private var __map_inited:Boolean;
	private var __mc:MovieClip;
	private var __movies:Movies;	
	private var __target_mc:MovieClip;
	private var __movies_mc:MovieClip;
	private var __map_mc:MovieClip;
	private var __map_container_mc:MovieClip;	
	private var __zoom_tool_mc:MovieClip;	
	private var __stage_listener:Object;
	private var __zoom_mc:MovieClip;
	private var __nav_mc:MovieClip;	
	private var __text_box_mc:MovieClip;
	private var __small_map_mc:MovieClip;
	private var __small_map_container_mc:MovieClip;	
	private var __small_map_mask_mc:MovieClip;
	private var __small_map_tool_mc:MovieClip;
	private var __rectangle_mc:MovieClip;
	private var __config:Object;
	private var __data:Object;
	private var __balloon_mc:MovieClip;
	private var __balloon:Balloon;	
	private var __object_list:Object;
	private var __developer_tf:Text;
	private var __movies_to_resize:Array;
	private var __labels:Object;
	private var __arrows_to_resize:Array;
	private var __interval:Number;
	private var __navigation_path:NavigationPath;
	private var __target;
	private var __time_out:Number;
	private var __tween:Tween;
	private var __tween_percent:Number;
	private var __click_interval:Number;
	private var __small_map_width:Number;
	private var __small_map_height:Number;	
	private var __mapxy:String;
	private var __xy:String;
	private var __xyp:String;
	private var __zoom_info:String;
	private var __long:Number;
	private var __lat:Number;
	private var __long_lat:String;	
	private var __bg_depth:Number = 0;
	private var __movies_depth:Number = 500;
	private var __labels_depth:Number = 600;		
	private var __lines_depth:Number = 700;	
	private var __map_depth:Number = 100;
	private var __overlay_depth:Number = 3000;
	private var __preloader_depth:Number = 1000;	
	private var __text_box_depth:Number = 950;
	private var __balloon_depth:Number = 2000;
	private var __zoom_tool_depth:Number = 800;
	private var __small_map_depth:Number = 900;
	private var __navigation_path_depth:Number = 960;
	private var __legend_depth:Number = 970;	
	private var __object_list_depth = 980;
	private var __developer_depth:Number = 4000;	
	private var __mouse_listener:Object;	
	private var __zooming:Boolean = false;
	private var __move_start_x:Number;
	private var __move_start_y:Number;	
	private var __map_move_start_x:Number;
	private var __map_move_start_y:Number;		
	private var __moved:Boolean;	
	private var __config_original;
	private var __data_original;
	private var __grid_step:Number;
	private var __zoom_level:Number = 100;	
	private var __zoom_map_x:Number;
	private var __zoom_map_y:Number;
	private var __map_x:Number = 0;
	private var __map_y:Number = 0;		
	private var __final_x:Number;
	private var __final_y:Number;
	private var __final_scale:Number;
	private var	__initial_x:Number;
	private var	__initial_y:Number;
	private var	__initial_scale:Number;
	private var __data_source:Object;	
	private var __wheel_busy:Boolean;
	private var __reloading_data:Boolean;
	private var __text_box:Object;
	private var __resize_interval:Number;
	private var __resize_time:Number;
	private var __map_completed:Boolean;
	private var __completed_interval:Number;
	private var __zoom_interval:Number;
	private var __click_handler:Object;
	private var __clicks_disabled:Boolean;
	private var __balloons_disabled:Boolean;
	private var __key_listener:Object;
	
	public var addListener:Function;
    public var broadcastMessage:Function;
	
	function Ammap (target_mc:MovieClip, name:String, depth:Number, config:Object, data:Object, config_original, data_original, reloading_data) {
		AsBroadcaster.initialize(this);
		__target_mc = target_mc;
		__config_original = config_original;
		__data_original = data_original;		
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__mc.all_movies = new Array();
		__data = data;
		__config = config;
		__reloading_data = reloading_data;
		
		// this is a trick for navigation path
		__data.title = __config.navigation_path.home_text;
		__data.top = true;
		
		// swap default depths
		var depths = [102, 502, 602, 702];
		var layers = __config.layers.split(',');
		var cnt = layers.length;
		for (var i = 0; i < cnt; i++) {
			this['__' + layers[i] + '_depth'] = depths[i];
		}
		
		__init();
	}	
	
	private function __init(){
		_global.wheel_busy = false;
		var main_obj = this;
		
		// create map_mc and container
		__map_mc = __mc.createEmptyMovieClip("map_mc", __map_depth);
		__map_container_mc = __map_mc.createEmptyMovieClip("map_container_mc", 0);
		__map_mc._visible = false;
		
		// load map, resize map after load, to fit it all
		var loader:Load = new Load ();
		loader.loadClip(__config.path + __data.map_file, __map_container_mc, this, "__resizeMap", false, "__error",undefined,__config.force_smoothing);
		
		if(__reloading_data != true){
			loader.preloader(__mc, "preloader_mc", __preloader_depth, 0, 0, __config.width, __config.height, __config.preloader_color, undefined, __config.strings.loading_map);
		}		
		
		// init mouse listener
		__initMouseListener();
		
		// load custom background
		if(__config.background.file != undefined){
			var bg_mc = __target_mc.createEmptyMovieClip("bg_mc", 0);	
			var loader:Load = new Load ();	
			loader.loadClip(__config.path + __config.background.file, bg_mc, this, "__initOverlay", bg_mc);
		}
		
		// load custom overlay
		if(__config.background.overlay_file != undefined){
			var overlay_mc = __mc.createEmptyMovieClip("overlay_mc", __overlay_depth);	
			var loader:Load = new Load ();
			loader.loadClip(__config.path + __config.background.overlay_file, overlay_mc, this, "__initOverlay", overlay_mc);
		}
		
		// developer tools
		if(__config.developer_mode == true){
			__developer_tf = new Text(__mc, "developer_tf", __developer_depth, 80, 80, __config.width - 70);
			__initKeyListener();
		}
		
		// navigation path
		if(__config.navigation_path.enabled == true){
			__navigation_path = new NavigationPath (__mc, "navigation_path_mc", __navigation_path_depth, __config);
		}

		var listener = new Object();
		listener.onClick = function(data_source){
			main_obj.__clickItem(data_source);
		}
		__navigation_path.addListener(listener);
		
		if(__config.redraw == true){
			__stageListener();
		}
		
		__initLegend();
	}
	
	private function __initOverlay (target_mc, param) {
		if (__config.background.overlay_stretch) {
			target_mc._width = __config.width;
			target_mc._height = __config.height;
		}
	}
	

	private function __initObjects(data:Object){

		var main_obj = this;

		var listener = new Object();			
		listener.onGetURL = function(data_source){
			main_obj.__clickItem(data_source);
		}
		listener.onRollOver = function(data_source:Object){
			main_obj.__hoverItem(data_source);
		}		
		listener.onRollOut = function(data_source:Object){
			main_obj.__balloon_mc._visible = false;
			if(main_obj.__config.text_box.hide_on_roll_out == true){
				main_obj.__hideTextBox();
			}
			main_obj.__rollOutItem(data_source);
		}				

		__labels = new Labels (__mc, __map_mc, "labels_mc", __labels_depth, data, __config, __data);
		__labels.addListener(listener);

		__movies.die();
		__movies = new Movies (__mc, __map_mc, "movies_mc", __movies_depth, data, __config, __data, __config_original, __data_original, this);			
		__movies.addListener(listener);
		__movies_to_resize = __movies.moviesToResize();
		
		// lines
		var lines = new Lines (__mc, __map_mc, "lines_mc", __lines_depth, data, __config, __data);
		__arrows_to_resize = lines.arrowsToResize();
	}	
	
	// resize map after loading
	private function __resizeMap(map_mc, resize_only:Boolean){
		
		var main_obj = this;
		
		if(__data_source == undefined){
			__data_source = __data;
		}
		
		// bg and border
		var bg = new Rectangle (__mc, "bg_mc", __bg_depth, __config.width, __config.height, __config.background.color, 1, __config.background.border_color, 0, __config.background.alpha, __config.background.border_alpha);
		if(__config.zoom.background_zooms_to_top == true){
			bg.mc.onRollOver = function(){
				//this.useHandCursor = false;
				this.useHandCursor = main_obj.__alwaysHand(false);
			}
			bg.mc.onRelease = function(){
				if (main_obj.__moved != true) {
					main_obj.broadcastMessage("amRegisterClick", [__config.map_id, '__background']);
					main_obj.__target_mc.externalCall("amRegisterClickAnywhere", [__config.map_id, '__background']);
				}
				main_obj.__getURL("#top");
			}
		}
		else {
			bg.mc.onRollOver = function(){
				//this.useHandCursor = false;
				this.useHandCursor = main_obj.__alwaysHand(false);
			}
			bg.mc.onRelease = function(){
				if (main_obj.__moved != true) {
					main_obj.broadcastMessage("amRegisterClick", [__config.map_id, '__background']);
					main_obj.__target_mc.externalCall("amRegisterClickAnywhere", [__config.map_id, '__background']);
				}
			}
		}
		
		if(__config.fit_to_screen == true){
			if((map_mc._width / __config.width) > (map_mc._height / __config.height)){
				var c =  __config.width / map_mc._width;
			}
			else{
				var c =  __config.height / map_mc._height;
			}			
			
			map_mc._width = map_mc._width * c;
			map_mc._height = map_mc._height * c;
		}
		

		var zoom_x = __map_mc._x / __config.map_width * 100 + "%";
		var zoom_y = __map_mc._y / __config.map_height * 100 + "%";	
		var final_x = __final_x / __config.map_width * 100 + "%";
		var final_y = __final_y / __config.map_height * 100 + "%";		
		
		
		__config.map_width = map_mc._width;
		__config.map_height = map_mc._height;		
		
		__initAreas();
		__initZoom();

		// zoom and position
		if(resize_only != true){
			if(__data.zoom == undefined){
				__data.zoom = __config.zoom.min;
			}
			if(__data_source.zoom_x == undefined){
				__map_mc._x = ((__config.width - __config.map_width ) / 2);

			}
			else{
				__map_mc._x = Utils.getCoordinate(__data_source.zoom_x, __config.map_width);
			}
			if(__data_source.zoom_y == undefined){
				__map_mc._y = ((__config.height - __config.map_height ) / 2);
			}			
			else{
				__map_mc._y = Utils.getCoordinate(__data_source.zoom_y, __config.map_height);
			}
			__zoom_map_x = (__config.width / 2 - __map_mc._x);
			__zoom_map_y = (__config.height / 2 - __map_mc._y);
			__zoomTo(__data_source.zoom, __data_source.zoom_x, __data_source.zoom_y, true);

			__initSmallMap();
			__initObjectList();
		}
		else{
			__map_mc._x = Utils.getCoordinate(zoom_x, __config.map_width);
			__map_mc._y = Utils.getCoordinate(zoom_y, __config.map_height);
			__final_x = Utils.getCoordinate(final_x, __config.map_width);
			__final_y = Utils.getCoordinate(final_y, __config.map_height);	
			__zoom_mc.dragger_mc._y = -((__zoom_level - 100) * __grid_step - __zoom_mc.minus_mc._y + __zoom_mc.dragger_mc._height);					
			__zoom_map_x = (__config.width / 2 - __map_mc._x) / (__zoom_level / 100);
			__zoom_map_y = (__config.height / 2 - __map_mc._y) / (__zoom_level / 100);
			__zoom_level = Utils.roundTo(__map_mc._xscale, 4);
			__initObjectList();
			__initLegend();
			__initSmallMap();			

		}
		if(__data.url != undefined && resize_only != true){
			__click_interval = setInterval(this, "__click_home", 500);
		}
		else{
			__initObjects(__data_source);
		}

		__resizeObjects();
		
		if(__map_inited != true && __inited_interval == undefined){
			__inited_interval = setInterval(this, "__initMap", 100);
		}
		if(__map_completed != true){
			clearInterval(__completed_interval);
			__completed_interval = setInterval(this, "__mapCompleted", 101);
		}			
	}

	private function __click_home(){
//		trace("__click_home");
		clearInterval(__click_interval);
		if(__data.url.substr(0,1) == "#"){
			__getURL(__data.url);
//			__data.url = "";
		}
	}


	
	// resize objects which are in the map and has fixed size
	private function __resizeObjects(){
//		trace("__resizeObjects");		
		// resize movies
		var scale = 10000 / __zoom_level;
		for(var i = 0; i < __movies_to_resize.length; i++){
			__movies_to_resize[i]._xscale = scale;
			__movies_to_resize[i]._yscale = scale;
			__movies_to_resize[i]._visible = true;
		}

		
		__labels.resize(scale);
		
		// arrows
		for(var i = 0; i < __arrows_to_resize.length; i++){
			__arrows_to_resize[i]._xscale = scale;
			__arrows_to_resize[i]._yscale = scale;
			__arrows_to_resize[i]._visible = true;			
		}	
	}
	
	// move map (then dragging)
	private function __makeMove(){
//		trace("__makeMove");
		// set __moved to true(in this case onRelease of movies and areas will not work)
		if(__config.drag_map != false){
			__moved = true;
		}
		// move map
		__map_mc._x = __map_move_start_x + (_root._xmouse - __move_start_x);
		__map_mc._y = __map_move_start_y + (_root._ymouse - __move_start_y);
		// set zoom x and y
		__zoom_map_x = (__config.width / 2 - __map_mc._x) / (__zoom_level / 100);
		__zoom_map_y = (__config.height / 2 - __map_mc._y) / (__zoom_level / 100);
		broadcastMessage("amRegisterMove", [__zoom_map_x, __zoom_map_y]);
		__updateRectangle();
	}		
	
	// MAKE ZOOM (when dragging zoom toolbar)
	private function __makeZoom(){
//		trace("__makeZoom");		
		__zoom_level = Utils.roundTo(__config.zoom.min + Math.floor((__zoom_mc.minus_mc._y - __zoom_mc.dragger_mc._y - __zoom_mc.dragger_mc._height)) / __grid_step, 4);
		__map_mc._xscale = __zoom_level;
		__map_mc._yscale = __zoom_level;
		//reposition the map
		
		__map_mc._x = -((__zoom_map_x * (__zoom_level / 100)) - __config.width / 2);
		__map_mc._y = -((__zoom_map_y * (__zoom_level / 100)) - __config.height / 2);
		
		__resizeObjects();
		
		__updateRectangle();
		broadcastMessage("amRegisterZoom", [__zoom_level]);

	}	
	

	// ZOOM TO
	private function __zoomTo(zoom_level, x, y, instant:Boolean){
		
//		trace("__zoomTo");		
		var main_obj = this;
		
		__final_x = Utils.getCoordinate(x, __config.map_width);
		__final_y = Utils.getCoordinate(y, __config.map_height);
		__final_scale = zoom_level;
		
		if(x == undefined){
			__final_x = -((__zoom_map_x * zoom_level / 100) - __config.width / 2);
		}
		
		if(y == undefined){
			__final_y = -((__zoom_map_y * zoom_level / 100) - __config.height / 2);
		}
		
		if(__config.zoom.effect == "easein"){
			var effect = mx.transitions.easing.Strong.easeIn;
		}
		if(__config.zoom.effect == "easeout"){
			var effect = mx.transitions.easing.Strong.easeOut;
		}
		__tween.stop();
		__tween = new Tween(this, "__tween_percent", effect, 0, 1, __config.zoom.time, true);		
		__tween.onMotionFinished = function(){
//			trace("clear");
			clearInterval(main_obj.__interval)
		}
		
		__initial_x = __map_mc._x;
		__initial_y = __map_mc._y;
		__initial_scale = __map_mc._xscale;
		
		if(instant == true){
			__tween.stop();
			__tween_percent = 1;
		}

		clearInterval(__interval);
		__interval = setInterval(this, "__moveMap", 20);
	}
	
	
	// MOVE AND RESIZE MAP 
	private function __moveMap(){	
	
		if(__map_mc._visible == false){
			__map_mc._visible = true;
		}
	
		if(__tween_percent == 1){
			clearInterval(__interval);			
		}
//		trace("__moveMap");
		__map_mc._x = __initial_x + (__final_x - __initial_x) * __tween_percent;
		__map_mc._y = __initial_y + (__final_y - __initial_y) * __tween_percent;
		
		var xscale = __initial_scale + (__final_scale - __initial_scale) * __tween_percent;
		__map_mc._xscale = xscale;
		__map_mc._yscale = xscale;		
		
		// current zoom_level	
		__zoom_level = Utils.roundTo(__map_mc._xscale, 4);
		__zoom_map_x = (__config.width / 2 - __map_mc._x) / (__zoom_level / 100);
		__zoom_map_y = (__config.height / 2 - __map_mc._y) / (__zoom_level / 100);		
		// resize objects
		__resizeObjects();
		// reposition zoom controll dragger
		__zoom_mc.dragger_mc._y = -((__zoom_level - __config.zoom.min) * __grid_step - __zoom_mc.minus_mc._y + __zoom_mc.dragger_mc._height);
		
		__updateRectangle();
		broadcastMessage("amRegisterZoom", [__zoom_level]);
	}
	

	
	//// INIT ZOOM CONTROLL
	private function __initZoom(){
		//trace("__initZoom");
		__zoom_tool_mc = __mc.createEmptyMovieClip("zoom_tool_mc", __zoom_tool_depth);
		// set position
		__zoom_tool_mc._x = Utils.getCoordinate(__config.zoom.x, __config.width);
		__zoom_tool_mc._y = Utils.getCoordinate(__config.zoom.y, __config.height);
		
		var main_obj = this;
		
		// get min 
		if(__config.zoom.min == undefined || __config.zoom.min == Infinity){
			if((__config.width / __config.map_width) < (__config.height / __config.map_height)){
				__config.zoom.min = Math.round(__config.width / __config.map_width * 100);
			}
			else{
				__config.zoom.min = Math.round(__config.height / __config.map_height * 100);
			}
		}		
		
		if(__config.zoom.enabled == true){
			// place zoom controll
			__zoom_mc = __zoom_tool_mc.attachMovie("zoom_mc", "zoom_mc", 0);
			// place "-" button
			__zoom_mc.minus_mc._y = Utils.getCoordinate(__config.zoom.height, __config.height) - __zoom_mc.minus_mc._height;
			// place "+" button		
			__zoom_mc.plus_mc._y = 0;
			// calculate value of one zoom level step
			__grid_step = (__zoom_mc.minus_mc._y - __zoom_mc.plus_mc._height) / (__config.zoom.max - __config.zoom.min + __config.zoom.grid_every);
			// set dragger height
			__zoom_mc.dragger_mc._height = __grid_step * __config.zoom.grid_every;
			// place dragger in starting position
			__zoom_mc.dragger_mc._y = __zoom_mc.minus_mc._y - __zoom_mc.dragger_mc._height;

			// make grid
			for (var i = __config.zoom.min; i <= __config.zoom.max; i++){
				if(i / __config.zoom.grid_every == Math.round(i / __config.zoom.grid_every)){
					var grid_mc = __zoom_mc.attachMovie("grid_mc", "grid_mc" + i, i);
					grid_mc._y = __zoom_mc.minus_mc._y - __zoom_mc.dragger_mc._height - (i - __config.zoom.min) * __grid_step;
					var color = new Color(grid_mc);
					color.setRGB(__config.zoom.outline_color);
					grid_mc._alpha = __config.zoom.outline_alpha;
				}
			}
			// place dragger above zoom grid
			__zoom_mc.dragger_mc.swapDepths(__config.zoom.max + 100);			
			// rotate 
			if(__config.zoom.rotate == true){			
				__zoom_mc._rotation = 90;
				__zoom_mc._x += __zoom_mc._width;			
				__zoom_mc.minus_mc.sign_mc._rotation = -90;			
				__zoom_mc.plus_mc.sign_mc._rotation = -90;			
			}		
		
			// set drag bounds
			var t = __zoom_mc.plus_mc._height;
			var b = __zoom_mc.minus_mc._y - __zoom_mc.dragger_mc._height;
			
			// drag
			__zoom_mc.dragger_mc.onPress = function(){
				// do not move when dragging
				main_obj.__zoom_map_x = (main_obj.__config.width / 2 - main_obj.__map_mc._x) / (main_obj.__zoom_level / 100);
				main_obj.__zoom_map_y = (main_obj.__config.height / 2 - main_obj.__map_mc._y) / (main_obj.__zoom_level / 100);				
				_global.drag_busy = this;
				main_obj.__zooming = true;
				startDrag(this,0,0,t,0,b);
			}		
			// stop drag
			__zoom_mc.dragger_mc.onRelease = __zoom_mc.dragger_mc.onReleaseOutside = function(){
				_global.ammap_kill_click = true;
				main_obj.__zooming = false;
				stopDrag();
				var color = new Color (this.bg_mc);
				color.setRGB(main_obj.__config.zoom.color);
			}
			// change color on roll over
			__zoom_mc.dragger_mc.onRollOver = __zoom_mc.plus_mc.onRollOver = __zoom_mc.minus_mc.onRollOver = function(){
				var color = new Color (this.bg_mc);
				color.setRGB(main_obj.__config.zoom.color_hover);
			}
			// change color on roll out			
			__zoom_mc.dragger_mc.onRollOut = __zoom_mc.plus_mc.onRollOut = __zoom_mc.minus_mc.onRollOut = function(){
				var color = new Color (this.bg_mc);
				color.setRGB(main_obj.__config.zoom.color);
			}				
			// plus release behavior
			__zoom_mc.plus_mc.onRelease = function(){
				_global.ammap_kill_click = true;
				main_obj.__zoomIn();
			}
			// minus release behavior
			__zoom_mc.minus_mc.onRelease = function(){
				_global.ammap_kill_click = true;
				main_obj.__zoomOut();
			}
		}
		// nav arrows
		if(__config.zoom.arrows_enabled == true){
			// place nav controll
			__nav_mc = __zoom_tool_mc.attachMovie("nav_mc", "nav_mc", 1);
			
			if(__config.zoom.home_link_enabled == false){
				__nav_mc.home_mc._visible = false;
			}
			
			// reposition zoom
			if(__config.zoom.rotate == false){
				__zoom_mc._x = __nav_mc._width / 3;
				__zoom_mc._y = __nav_mc._height + 15;
			}
			else{
				__zoom_mc._x = __nav_mc._width + 15 + __zoom_mc._width;
				__zoom_mc._y = __nav_mc._height / 3;				
			}
			
			// change color on roll over
			__nav_mc.top_mc.onRollOver = __nav_mc.bottom_mc.onRollOver = __nav_mc.left_mc.onRollOver = __nav_mc.right_mc.onRollOver = function(){
				var color = new Color (this.bg_mc);
				color.setRGB(main_obj.__config.zoom.color_hover);
			}
			// change color on roll out
			__nav_mc.top_mc.onRollOut = __nav_mc.top_mc.onReleaseOutside = __nav_mc.bottom_mc.onRollOut = __nav_mc.bottom_mc.onReleaseOutside = __nav_mc.left_mc.onRollOut = __nav_mc.left_mc.onReleaseOutside = __nav_mc.right_mc.onRollOut = __nav_mc.right_mc.onReleaseOutside = function(){
				var color = new Color (this.bg_mc);
				color.setRGB(main_obj.__config.zoom.color);
			}
			
			__nav_mc.top_mc.x_dir = 0;
			__nav_mc.top_mc.y_dir = 1;
			__nav_mc.bottom_mc.x_dir = 0;
			__nav_mc.bottom_mc.y_dir = -1;			
			__nav_mc.left_mc.x_dir = 1;
			__nav_mc.left_mc.y_dir = 0;			
			__nav_mc.right_mc.x_dir = -1;
			__nav_mc.right_mc.y_dir = 0;						
			
			// release behaviors
			__nav_mc.top_mc.onRelease = __nav_mc.bottom_mc.onRelease = __nav_mc.left_mc.onRelease = __nav_mc.right_mc.onRelease = function(){
				_global.ammap_kill_click = true;
				var x = main_obj.__map_mc._x + this.x_dir * main_obj.__config.width * main_obj.__config.zoom.step_size / 100;
				var y = main_obj.__map_mc._y + this.y_dir * main_obj.__config.height *  main_obj.__config.zoom.step_size / 100;
				main_obj.__zoomTo(main_obj.__zoom_level, x, y);
			}

			__nav_mc.home_mc.onPress = function(){
				if(main_obj.__config.zoom.locked != true){							
					startDrag(main_obj.__zoom_tool_mc);
				}
				this.old_x = main_obj.__zoom_tool_mc._x;
				this.old_y = main_obj.__zoom_tool_mc._y;				
				_global.drag_busy = this;
			}

			__nav_mc.home_mc.onRelease = function(){
				_global.ammap_kill_click = true;
				_global.drag_busy = false;
				stopDrag();
				if(main_obj.__zoom_tool_mc._x == this.old_x && main_obj.__zoom_tool_mc._y == this.old_y){
					main_obj.__getURL("#top");
				}
			}
		}
		
		
		// colors
		var outlines = [__zoom_mc.minus_mc.border_mc, 
						__zoom_mc.minus_mc.sign_mc, 
						__zoom_mc.plus_mc.border_mc,
						__zoom_mc.plus_mc.sign_mc,
						__zoom_mc.dragger_mc.border_mc,
						__nav_mc.top_mc.border_mc,
						__nav_mc.top_mc.arrow_mc,
						__nav_mc.bottom_mc.border_mc,
						__nav_mc.bottom_mc.arrow_mc,
						__nav_mc.left_mc.border_mc,
						__nav_mc.left_mc.arrow_mc,
						__nav_mc.right_mc.border_mc,
						__nav_mc.right_mc.arrow_mc,
						__nav_mc.home_mc];
		var fills = [__zoom_mc.minus_mc.bg_mc, __zoom_mc.plus_mc.bg_mc, __zoom_mc.dragger_mc.bg_mc, __nav_mc.top_mc.bg_mc ,__nav_mc.bottom_mc.bg_mc, __nav_mc.left_mc.bg_mc,__nav_mc.right_mc.bg_mc];

		for (var i = 0; i < outlines.length; i++){
			var color = new Color(outlines[i]);
			color.setRGB(__config.zoom.outline_color);
			outlines[i]._alpha = __config.zoom.outline_alpha;
		}
		
		for (var i = 0; i < fills.length; i++){
			var color = new Color(fills[i]);
			color.setRGB(__config.zoom.color);
			fills[i]._alpha = __config.zoom.alpha;
		}
	}	
	
	public function __zoomIn(){
		if(__zoom_level < __config.zoom.max){
			var zoom_level = Math.ceil(__zoom_level + __config.zoom.grid_every);
			__zoomTo(Utils.fitToBounds(zoom_level, __config.zoom.min, __config.zoom.max));
		}		
	}
	
	public function __zoomOut(){
		if(__zoom_level > __config.zoom.min){
			var zoom_level = Math.floor(__zoom_level - __config.zoom.grid_every);
			__zoomTo(Utils.fitToBounds(zoom_level, __config.zoom.min, __config.zoom.max));
		}		
	}
	
	private function __clickZoom(){
		clearInterval(__zoom_interval);
		if(__moved == false && __config.zoom.zoom_on_click == true){
			__zoom_interval = setInterval(this, "__clickZoom2", 100);
			broadcastMessage("amRegisterClick", [__config.map_id, '__background']);
			__target_mc.externalCall("amRegisterClick", [__config.map_id, '__background']);
		}
	}
	
	private function __clickZoom2(){
		clearInterval(__zoom_interval);
		if(_global.ammap_kill_click == true){
			_global.ammap_kill_click = false;
		}
		else{
			if(__zoom_level < __config.zoom.max){
				var zoom_level = Math.ceil(__zoom_level + __config.zoom.grid_every);
				if(zoom_level > __config.zoom.max){
					zoom_level = __config.zoom.max;
				}
			}						
			else{
				zoom_level = __config.zoom.max;
			}

			
			var nx = -((__zoom_map_x * zoom_level / 100) - __config.width / 2);
			var ny = -((__zoom_map_y * zoom_level / 100) - __config.height / 2);
			
			var x = nx + ((__config.width / 2) - __mc._xmouse) * zoom_level / __zoom_level;
			var y = ny + ((__config.height / 2) - __mc._ymouse) * zoom_level / __zoom_level;	
	
			__zoomTo(zoom_level, x, y);
		}
	}	
	
	
	// INIT LEGEND
	private function __initLegend(){
		// legend
		if(__config.legend.enabled == true && __config.legend.entries.entry.length > 0){
			new Legend(__mc, "legend", __legend_depth, __config);
		}		
	}
		
	// INIT OBJECT LIST
	private function __initObjectList(){
		if(__config.object_list.enabled == true){
			var main_obj = this;
			var width = Utils.getCoordinate(__config.object_list.width, __config.width);
			var height = Utils.getCoordinate(__config.object_list.height, __config.height);			
			__object_list.die();
			__object_list = new ComboBox(__mc, "object_list_mc", __object_list_depth, width, height, __config.object_list.type, __config.object_list);
			
			if(__config.object_list.home_text != "none"){
				__object_list.addItem(__config.object_list.home_text, __data);
				__data.object_list_index = 0;
			}


			__addItemsToList(__data, 0);

			__object_list.show();
			if(__config.object_list.home_text != "none"){			
				__object_list.selectItem(0);
			}
			__object_list.mc._x = Utils.getCoordinate(__config.object_list.x, __config.width) - 1;
			__object_list.mc._y = Utils.getCoordinate(__config.object_list.y, __config.height);			
			
			var main_obj = this;
			var listener = new Object();
			listener.onChange = function(data_source){
				main_obj.__clickItem(data_source);
			}
			listener.onRollOver = function(data_source){
				main_obj.__hoverItem(data_source, true);
			}			
			listener.onRollOut = function(data_source){
				main_obj.__rollOutItem(data_source);
			}						
			__object_list.addListener(listener);
		}
	}
		
	private function __addItemsToList(data_source, level){
		var add_on = "";
		for(var i = 0; i < level; i++){
			add_on +="  ";
		}
		if(level > 0){
			add_on += "- ";
		}
		
		if(__config.object_list.include_areas == true){
			for (var i = 0; i < data_source.areas.area.length; i++){
				if(data_source.areas.area[i].object_list == true && data_source.areas.area[i].title != ""){
					if(data_source.areas.area[i].title != undefined){
						data_source.areas.area[i].object_list_index = __object_list.addItem(add_on + "" + data_source.areas.area[i].title, data_source.areas.area[i]);
					}
					if(data_source.areas.area[i].movies.movie.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.areas.area[i], level+1);
					}
					if(data_source.areas.area[i].labels.label.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.areas.area[i], level+1);
					}				
				}
			}
		}
		if(__config.object_list.include_movies == true){
			for (var i = 0; i < data_source.movies.movie.length; i++){
				if(data_source.movies.movie[i].object_list == true && data_source.movies.movie[i].title != ""){				
					if(data_source.movies.movie[i].title != undefined){
						data_source.movies.movie[i].object_list_index = __object_list.addItem(add_on + "" + data_source.movies.movie[i].title, data_source.movies.movie[i]);
					}
					if(data_source.movies.movie[i].movies.movie.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.movies.movie[i], level+1);
					}
					if(data_source.movies.movie[i].labels.label.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.movies.movie[i], level+1);
					}				
				}
			}
		}		
		if(__config.object_list.include_labels == true){
			for (var i = 0; i < data_source.labels.label.length; i++){
				if(data_source.labels.label[i].object_list == true && data_source.labels.label[i].title != ""){				
					if(data_source.labels.label[i].title != undefined){
						data_source.labels.label[i].object_list_index = __object_list.addItem(add_on + "" + data_source.labels.label[i].title, data_source.labels.label[i]);
					}
					if(data_source.labels.label[i].movies.movie.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.labels.label[i], level+1);
					}
					if(data_source.labels.label[i].labels.label.length > 0 && level < __config.object_list.levels - 1){
						__addItemsToList(data_source.labels.label[i], level+1);
					}				
				}
			}
		}
		if(__object_list.itemCount == 0){
			__object_list.mc._visible = false;
		}
	}
		
	// INIT SMALL MAP
	private function __initSmallMap(){
//		trace("__initSmallMap");
		if(__config.small_map.enabled == true){
			
			var main_obj = this;
			__small_map_tool_mc = __mc.createEmptyMovieClip("small_map_tool_mc", __small_map_depth);
			__small_map_tool_mc._visible = false;
			__small_map_mc = __small_map_tool_mc.createEmptyMovieClip("small_map_mc", 0);
			__small_map_width = Utils.getCoordinate(__config.small_map.width, __config.width)  -  2 * __config.small_map.border_width;
			__small_map_height =__small_map_width * (__map_container_mc._height / __map_container_mc._width) -  2 * __config.small_map.border_width;
			
			// position small map
			if(__config.small_map.x == undefined){
				__small_map_tool_mc._x = __config.width - __small_map_width - __config.small_map.border_width;
			}
			else{
				__small_map_tool_mc._x = Utils.getCoordinate(__config.small_map.x, __config.width);
			}
			
			if(__config.small_map.y == undefined){
				__small_map_tool_mc._y = __config.height - __small_map_height  - __config.small_map.border_width;
			}
			else{
				__small_map_tool_mc._y = Utils.getCoordinate(__config.small_map.y, __config.height);
			}	
			// draw border square
			var border = new Rectangle (__small_map_mc, "border_mc", 0, __small_map_width + 2 * __config.small_map.border_width, __small_map_height + 2 * __config.small_map.border_width, __config.small_map.border_color);
			border.mc._x = - __config.small_map.border_width;
			border.mc._y = - __config.small_map.border_width;			

			border.mc.onRollOver = function(){			
				this.useHandCursor = false;
			}
			
			if(__config.small_map.locked != true){
				border.mc.onPress = function(){
					this.old_x = main_obj.__small_map_tool_mc._x;
					this.old_y = main_obj.__small_map_tool_mc._y;				
					_global.drag_busy = this;
					startDrag(main_obj.__small_map_tool_mc);
				}
	
				border.mc.onRelease = function(){
					stopDrag();
					_global.ammap_kill_click = true;
					_global.drag_busy = false;
				}			
			}
			
			// draw background
			var bg = new Rectangle (__small_map_mc, "bg_mc", 1, __small_map_width, __small_map_height, __config.background.color);
			
			var main_obj = this;
			
			bg.mc.onRollOver = function(){
				this.useHandCursor = false;
			}
			
			bg.mc.onPress = function(){
				_global.drag_busy = this;
				
				if(main_obj.__config.small_map.active != false){				
					var c = main_obj.__small_map_width / main_obj.__map_container_mc._width;
		
					var x = - (this._xmouse - main_obj.__rectangle_mc._width / 2) * main_obj.__zoom_level / 100 / c;
					var y = - (this._ymouse - main_obj.__rectangle_mc._height / 2) * main_obj.__zoom_level / 100 / c;						
						
					main_obj.__zoomTo(main_obj.__zoom_level, x, y);
				}
			}
			
			bg.mc.onRelease = function(){
				_global.ammap_kill_click = true;
				main_obj.broadcastMessage("amRegisterClick", [__config.map_id, '__smallmap']);
				main_obj.__target_mc.externalCall("amRegisterClickAnywhere", [__config.map_id, '__smallmap']);
			}

			// attach collapse button
			var collapse_button_mc = __small_map_tool_mc.attachMovie("collapse_mc", "collapse_mc", 2);
			var color = new Color(collapse_button_mc.bg_mc);
			color.setRGB(__config.small_map.border_color);
			
			var color = new Color(collapse_button_mc.arrow_mc);
			color.setRGB(__config.small_map.collapse_button_color);
			
			collapse_button_mc.onPress = function(){
				_global.drag_busy = this;
			}			
			
			collapse_button_mc.onRelease = function(){
				_global.ammap_kill_click = true;
				if(main_obj.__small_map_mc._visible == false){
					main_obj.__small_map_mc._visible = true;
				}
				else{
					main_obj.__small_map_mc._visible = false;
				}
			}

			if(__config.small_map.collapse_button_position == "tl"){
				collapse_button_mc._x = - __config.small_map.border_width;
				collapse_button_mc._y = - __config.small_map.border_width;
			}
			
			if(__config.small_map.collapse_button_position == "tr"){
				collapse_button_mc._x = __small_map_width - collapse_button_mc._width + __config.small_map.border_width;
				collapse_button_mc._y = - __config.small_map.border_width;
			}
			if(__config.small_map.collapse_button_position == "br"){
				collapse_button_mc._x = __small_map_width - collapse_button_mc._width + __config.small_map.border_width;
				collapse_button_mc._y = __small_map_height - collapse_button_mc._height + __config.small_map.border_width;
			}
			if(__config.small_map.collapse_button_position == "bl"){
				collapse_button_mc._y = __small_map_height - collapse_button_mc._height + __config.small_map.border_width;
				collapse_button_mc._x = - __config.small_map.border_width;				
			}
			
			
			// draw mask
			var mask = new Rectangle (__small_map_mc, "mask_mc", 5, __small_map_width, __small_map_height, __config.background.color);			
			var active_rectangle_mask_mc = mask.mc;
			
			var mask = new Rectangle (__small_map_mc, "mask_mc2", 6, __small_map_width, __small_map_height, __config.background.color);			
			__small_map_mask_mc = mask.mc;

			
			// load map
			__small_map_container_mc = __small_map_mc.createEmptyMovieClip("small_map_container_mc", 3);
	
			var loader:Load = new Load ();
			loader.loadClip(__config.path + __data.map_file, __small_map_container_mc, this, "__resizeSmallMap", false, "__error", [__small_map_width, __small_map_height]);			
			
			// color map 
			if(__config.small_map.color != undefined){
				var color = new Color(__small_map_container_mc);
				color.setRGB(__config.small_map.color);
			}
			
			// active area rectangle
			var rectangle = new Rectangle (__small_map_mc, "rectangle_mc", 10, __small_map_width, __small_map_height, 0, 0, __config.small_map.rectangle_color, 0, 0, 100);
			__rectangle_mc = rectangle.mc;
			
			__rectangle_mc.setMask(active_rectangle_mask_mc);		
			
			__updateRectangle();
		}
	}	
		
	private function __updateRectangle(){
//		trace("__updateRectangle");
		var c = __small_map_width / __map_container_mc._width;

		__rectangle_mc._x = -__map_mc._x / (__zoom_level / 100) * c;
		__rectangle_mc._y = -__map_mc._y / (__zoom_level / 100) * c;

		__rectangle_mc._width = __config.width / (__zoom_level / 100) * c;
		__rectangle_mc._height =__config.height / (__zoom_level / 100) * c;		
	}
	
	
	
	private function __resizeSmallMap(movie_mc:MovieClip, params:Array){
		__small_map_tool_mc._visible = true;
//		trace("__resizeSmallMap");		
		movie_mc.setMask(__small_map_mask_mc);
		movie_mc._width = params[0];
		movie_mc._height = params[1];		
	}
	

	// INIT MOUSE LISTENER
	private function __initMouseListener(){
//		trace("__initMouseLIstener");		
		var main_obj = this;
		
		__mouse_listener = new Object();
		__mouse_listener.onMouseMove = function() {
			if(main_obj.__balloon_mc._visible == true){
				main_obj.__balloon.pointTo(main_obj.__target_mc._xmouse, main_obj.__target_mc._ymouse);
			}
			if(main_obj.__zooming == true){
				main_obj.__makeZoom();
			}
			if(_global.drag_busy == this){
				main_obj.__makeMove();
			}			
			main_obj.__setDeveloperText();
		}
		
		__mouse_listener.onMouseDown = function(){

			main_obj.__moved = false;
			clearInterval(main_obj.__interval);		
			main_obj.__balloon_mc._visible = false;
			if( main_obj.__mc._xmouse < 0 || main_obj.__mc._xmouse > main_obj.__mc._width ||
			    main_obj.__mc._ymouse < 0 || main_obj.__mc._ymouse > main_obj.__mc._height ) {
				trace("oustide of map container");
				return;
			}
			if(main_obj.__config.drag_map ==  true){
				
				_global.drag_busy = this;
				main_obj.__stopTween();
			
				main_obj.__move_start_x = _root._xmouse;
				main_obj.__move_start_y = _root._ymouse;
				main_obj.__map_move_start_x = main_obj.__map_mc._x;
				main_obj.__map_move_start_y = main_obj.__map_mc._y;
			}
		}
		__mouse_listener.onMouseUp = function(){
			_global.drag_busy = false;			
			main_obj.__clickZoom();
		}
		
		if(__config.zoom.mouse_wheel_enabled == true){
			__mouse_listener.onMouseWheel = function(delta) {
				if( main_obj.__mc._xmouse < 0 || main_obj.__mc._xmouse > main_obj.__mc._width ||
			        main_obj.__mc._ymouse < 0 || main_obj.__mc._ymouse > main_obj.__mc._height ) {
					return;
				}
				
				if(_global.wheel_busy == false){
					if(delta > 0){
						main_obj.__zoomIn();
					}
					else{
						main_obj.__zoomOut();
					}
				}
			}
		}		
		Mouse.removeListener(__mouse_listener);
		Mouse.addListener(__mouse_listener);
		
		// Centrifuge Changes start
		//__map_mc.onRollOut = function() {
		//	Mouse.removeListener( main_obj.__mouse_listener );
		//};
		
		// was __mc
		//__map_mc.onRollOver = function() {
		//	Mouse.removeListener( main_obj.__mouse_listener );
		//	Mouse.addListener( main_obj.__mouse_listener );
		//};
		
		
	}
	
	// INIT KEY LISTENER (USED IN DEVELOPER MODE ONLY)
	private function __initKeyListener () {
		var main_obj = this;
		__key_listener = new Object();
		__key_listener.onKeyDown = function () {
			if (Key.isDown(Key.CONTROL)) {
				if (Key.getCode() == 49) {
					main_obj.copyLongLat();
				}
				else if (Key.getCode() == 50) {
					main_obj.copyStageXY();
				}
				else if (Key.getCode() == 51) {
					main_obj.copyStageXYP();
				}
				else if (Key.getCode() == 52) {
					main_obj.copyZoomInfo();
				}
			}
		}
		__key_listener.onKeyUp = function () {
			// do nothing
		}
		Key.removeListener(__key_listener);
		Key.addListener(__key_listener);
		
		// added bogus lines to workaround stupid Flash 32K error
		var a:Number;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
		a = 1;
	}
	
	// INIT AREAS
	private function __initAreas(){
//		trace("__initAreas");		
		var main_obj = this;
		if(__config.area.color_unlisted != undefined){
			for (var prop in __map_container_mc){
				if (__map_container_mc[prop] instanceof MovieClip){
					var color = new Color(__map_container_mc[prop]);
					color.setRGB(__config.area.color_unlisted);
				}
			}
		}
		
		for (var i = 0; i < __data.areas.area.length; i++){
			// assign movie clip
			
			var area_mc = __map_container_mc[__data.areas.area[i].mc_name];

			__data.areas.area[i].mc = area_mc;
			
			area_mc.data_source = __data.areas.area[i];
			
			
			// set color
			var color = new Color(area_mc);
			color.setRGB(__config.area.color_light);
			

			// set alpha
			area_mc._alpha = __data.areas.area[i].alpha;
			
			// behaviors
			// roll over
			area_mc.onRollOver = function(){							
				this.no_hover = false;
				if(main_obj.__config.area.disable_when_clicked == true){
					if(main_obj.__data_source == this.data_source){
						//this.useHandCursor = false;
						this.useHandCursor = main_obj.__alwaysHand(false);					
						this.no_hover = true;
					}				
					for(var i = 0; i < main_obj.__config.selected_data_source.link_with.length; i++){
						if(main_obj.__config.selected_data_source.link_with[i] == this.data_source.oid){
							//this.useHandCursor = false;
							this.useHandCursor = main_obj.__alwaysHand(false);
							this.no_hover = true;
						}
					}
				}				
				if(this.no_hover == false){
					//this.useHandCursor = true;
					this.useHandCursor = main_obj.__alwaysHand(true);	
					main_obj.__hoverItem(this.data_source);
				}
			}
			// roll out
			area_mc.onRollOut = area_mc.onReleaseOutside = function(){
				if((main_obj.__config.area.disable_when_clicked == true && main_obj.__data_source == this.data_source) || this.no_hover == true){
					// void
				}
				else{
					main_obj.__balloon_mc._visible = false;
					
					if(main_obj.__config.text_box.hide_on_roll_out == true){
						main_obj.__hideTextBox();
					}
					main_obj.__rollOutItem(this.data_source);
				}
			}
			// release
			area_mc.onRelease = function(){				
				if(main_obj.__config.area.disable_when_clicked == true && main_obj.__data_source == this.data_source){
					// void
					main_obj.broadcastMessage("amRegisterClick", [main_obj.__config.map_id, this.data_source.oid, this.data_source.title, this.data_source.value]);
					main_obj.__target_mc.externalCall("amRegisterClickAnywhere", [main_obj.__config.map_id, this.data_source.oid, this.data_source.title, this.data_source.value]);
				}
				else{				
					//this.useHandCursor = false;
					this.useHandCursor = main_obj.__alwaysHand(false);
					main_obj.__clickItem(this.data_source);	
				}
			}
			// fade
			area_mc.fader = new FadeColor(area_mc);
			
			if(__data_source != __data.areas.area[i]){
				area_mc.fader.fadeTo(__data.areas.area[i].color, __config.color_change_time_start);
				
			}
			else{
				color.setRGB(__data_source.color_selected);				
				__changeColorLinked(__data_source, "color_selected");				
			}
		}
	}
	// get url
	private function __getURL(url){
		// check if clicks are not disabled
		if (__clicks_disabled) {
			return;
		}
		
//		trace("__getURL");		
		clearInterval(__time_out);
	
		if(url == undefined){
			url = __data_source.url;
		}
		
		// in case home
		if(url == "#top"){
			__clickItem(__data, true);
			return;
		}
		if(url == "#parent"){
			__clickItem (__data_source.parent.parent);
			return;	
		}
		
		if(url.substr(0, 1) == "#"){
			var data_source = __data.by_id[url.substr(1)];
			if(data_source == undefined){
				__data.url = undefined;
				data_source = __data;
			}

			__clickItem (data_source);
			return;
		}
		
	
		// if url is set, get url
		if(url != undefined && Utils.stripSymbols(url, " ") != ""){
			// in case url starts with !, load new data file
			if(url.substr(0,1) == "!"){
				__movies.die();
				var temp = url.substr(1);
				if(temp.indexOf("||") != -1){
					__target_mc.data_file = temp.split("||")[0];					
					__target_mc.settings_file = temp.split("||")[1];
					__target_mc.map_created = false;
					__target_mc.reloading_settings = false;
					__target_mc.reloading_data = false;					
					__target_mc.reloading_all = false;										
					__target_mc.loadSettings();
				}
				else{
					__target_mc.reloading_data = false;
					__target_mc.data_file = url.substr(1);
					__target_mc.loadData();
				}
			}
			else{
				getURL(url, __data_source.target);
			}
		}
		__initObjects(__data_source);		
		
		// show text box
		if(__data_source.text_box == true && __data_source.description != undefined && __data_source.description != ""){
			__showTextBox(__data_source);
		}		
		__resizeObjects();
	}

	
	// click item
	private function __clickItem(data_source:Object, skip_click_anywhere:Boolean){
		// register click only if didn't moved the map
		if(__moved != true){
			__target_mc.externalCall("amRegisterClick", [__config.map_id, data_source.oid, data_source.title, data_source.value]);
			if (!skip_click_anywhere) {
			  __target_mc.externalCall("amRegisterClickAnywhere", [__config.map_id, data_source.oid, data_source.title, data_source.value]);
			}
			broadcastMessage("amRegisterClick", [__config.map_id, data_source.oid, data_source.title, data_source.value]);
		}
		
		// check if clicks are not disabled
		if (__clicks_disabled) {
			return;
		}
		
		if(__moved != true && Utils.checkIfClickable(data_source) == true){
			// color previous movie to it's original color
			if(__data_source.color != undefined){
				var color = new Color(__data_source.mc);
				color.setRGB(__data_source.color);
			}
			__changeColorLinked(__data_source, "color");
			
			// if selected color specified, change color
			if(data_source.color_selected != undefined){
				data_source.mc.fader.stop();
				var color = new Color(data_source.mc);
				color.setRGB(data_source.color_selected);
			}
			__map_mc.movies_mc._visible = false;
			__map_mc.lines_mc._visible = false;
			__map_mc.labels_mc._visible = false;

			// remove text box
			__hideTextBox();
			__data_source = data_source;
			__config.selected_data_source = __data_source;

			// if zoom params are set, make zoom
			_global.ammap_kill_click = true;
			if(data_source.zoom != undefined){
				if(data_source.zoom_x == undefined && data_source.zoom_y == undefined && data_source.lat != undefined && data_source.long != undefined){
					setZoomLongLat(data_source.zoom, data_source.long, data_source.lat);
				}
				else{
					__zoomTo(data_source.zoom, data_source.zoom_x, data_source.zoom_y);
				}
				clearInterval(__time_out);
				__time_out = setInterval(this, "__getURL", __config.zoom.time * 1000);
			}
			else{
				__getURL();
			}
			
			// clear path
			if(data_source.top == true){
				__navigation_path.clearPath();
			}
			else{
				// tell path
				__navigation_path.registerClick(data_source);
			}
			__changeColorLinked(data_source, "color_selected");
			
			if(__config.object_list.enabled == true){
				__object_list.selectItem(data_source.object_list_index);
			}
		}
	}
	
	private function __hideTextBox(){
		if(__text_box_mc != undefined && __text_box_mc != ""){
			__text_box_mc._visible = false;
		}		
	}
	
	
	// hover item
	private function __hoverItem(data_source:Object, object_list:Boolean){	

		var item_mc = data_source.mc;

		item_mc.fader.stop();

		if(Utils.checkIfClickable(data_source) != true){
			//item_mc.useHandCursor = false;
			item_mc.useHandCursor = __alwaysHand(false);
		}

		__target_mc.externalCall("amRegisterHover", [__config.map_id, data_source.oid, data_source.title, data_source.value]);
		broadcastMessage("amRegisterHover", [__config.map_id, data_source.oid, data_source.title, data_source.value]);

		if(data_source.balloon == true){
			if(data_source.color_hover != undefined){
				var color = new Color(item_mc);
				color.setRGB(data_source.color_hover);
			}			
			if(object_list != true){
				var balloon_text = Utils.formatText(__config, __config[data_source.type].balloon_text, data_source.title, data_source.value, data_source.percent, data_source.description);
				__showBalloon(balloon_text);
			}
		}
	
		__changeColorLinked(data_source, "color_hover");
		
		if(data_source.text_box == true && data_source.description != undefined && data_source.description != ""){
			if (__config.text_box.show_on_hover == true && __config.text_box.hide_on_roll_out == true) {
				__showTextBox(data_source, false);
			}
			else if (__config.text_box.show_on_hover == true) {
				__showTextBox(data_source, true);
			}
		}
	}
 
	private function __changeColorLinked(data_source:Object, color_name:String){
		if(data_source.link_with != undefined){
			for(var i = 0; i < data_source.link_with.length; i++){
				var linked_item_data_source = __data.by_id[data_source.link_with[i]];				
				linked_item_data_source.mc.fader.stop();
				if(linked_item_data_source[color_name] != undefined){
					var color = new Color(linked_item_data_source.mc);
					color.setRGB(linked_item_data_source[color_name]);
				}
			}
		}		
	}	
	
	private function __rollOutItem(data_source:Object){

		if(__config.selected_data_source == data_source){
			if(data_source.color_selected != undefined){
				data_source.mc.fader = new FadeColor(data_source.mc);
				data_source.mc.fader.fadeTo(data_source.color_selected, __config.color_change_time_hover);				
			}
			
			if(data_source.link_with != undefined){
				for(var i = 0; i < data_source.link_with.length; i++){
					var hover_item_data_source = __data.by_id[ data_source.link_with[i]];				
					if(hover_item_data_source.color_hover != undefined){
						hover_item_data_source.mc.fader = new FadeColor(hover_item_data_source.mc);
						hover_item_data_source.mc.fader.fadeTo(hover_item_data_source.color_selected, __config.color_change_time_hover);
					}
				}
			}			
		}
		else {
			if(data_source.color != undefined){
				data_source.mc.fader = new FadeColor(data_source.mc);
				data_source.mc.fader.fadeTo(data_source.color, __config.color_change_time_hover);
			}
			if(data_source.link_with != undefined){
				for(var i = 0; i < data_source.link_with.length; i++){
					var hover_item_data_source = __data.by_id[ data_source.link_with[i]];				
					if(hover_item_data_source.color_hover != undefined){
						hover_item_data_source.mc.fader = new FadeColor(hover_item_data_source.mc);
						hover_item_data_source.mc.fader.fadeTo(hover_item_data_source.color, __config.color_change_time_hover);
					}
				}
			}			
		}		

	}
	
	// SHOW BALLOON
	private function __showBalloon(balloon_text){		
//		trace("__showBalloon");	
		if (__config.balloon.enabled == true && __balloons_disabled != true){
	
			if(balloon_text != "" && balloon_text != undefined && balloon_text != " "){
				__balloon = new Balloon(__mc, "balloon_mc", __balloon_depth, balloon_text, 0, 0, __config.width, __config.height, Utils.getCoordinate(__config.balloon.max_width, __config.width), __config);
				__balloon.distance = 12;
				__balloon.hMargins = 12 - __config.balloon.corner_radius /2;
				__balloon.pointerWidth = 20;
				__balloon.vMargins = 5 - __config.balloon.corner_radius /2;
				__balloon.bgColor = __config.balloon.color;
				__balloon.borderColor = __config.balloon.border_color;
				__balloon.bgAlpha = __config.balloon.alpha;
				__balloon.borderAlpha = __config.balloon.border_alpha;
				__balloon.borderWidth = __config.balloon.border_width;				
				__balloon.textColor = __config.balloon.text_color;
				__balloon.textSize = __config.balloon.text_size;
				__balloon.pointerPosition = __config.balloon.arrow;
				__balloon.cornerRadius = __config.balloon.corner_radius;
				__balloon.show();
				__balloon.pointTo(__target_mc._xmouse, __target_mc._ymouse);
				__balloon_mc = __balloon.mc;
			}
		}
	}
	
	// SHOW TEXT BOX
	private function __showTextBox(data_source, show_x:Boolean){
//		trace("__showTextBox");
		var main_obj = this;
		
		var width = Utils.getCoordinate(data_source.text_box_width, __config.width);
		var height = Utils.getCoordinate(data_source.text_box_height, __config.height);
  	    __text_box.die();
		__text_box = new Window(__mc, "text_box_mc", __text_box_depth, data_source.title, width, height, __config.text_box.color, __config.text_box.alpha, __config.text_box.corner_radius);
		__text_box_mc = __text_box.mc;
		__text_box.border(__config.text_box.border_color, __config.text_box.border_alpha, __config.text_box.border_width);
		__text_box.scroller(__config.text_box.scroller_color, __config.text_box.scroller_bg_color, 15);
		__text_box.text(__config.text_box.text_size, __config.text_box.text_color);
		__text_box.shadow(__config.text_box.shadow_color,__config.text_box.shadow_alpha,__config.text_box.shadow_blur,__config.text_box.shadow_distance);
		__text_box.closeButton(__config.text_box.close_button_color,__config.text_box.close_button_color_hover);
		__text_box.locked = __config.text_box.locked;
		__text_box.selectable = __config.text_box.selectable;		
		__text_box.margin_width = __config.text_box.margin_width;
		__text_box_mc._x = Utils.getCoordinate(data_source.text_box_x, __config.width);
		__text_box_mc._y = Utils.getCoordinate(data_source.text_box_y, __config.height);
		
		__text_box.content = data_source.description;
		__text_box.show();
		
		if(show_x == false){
			__text_box.hideX();
		}		
		
	}
	/// ERROR
	private function __error(string){
//		trace("error");		
		__target_mc.externalCall("amError", [__config.map_id, string]);
		broadcastMessage("amError", [__config.map_id, string]);
		new PopUp(__mc, "popup_mc", __preloader_depth, string, (__config.width - __config.width * 0.8) / 2,  __config.height / 2.5,  __config.width * 0.8, 0xBBBB00, 0xFFFFFF);
	}	
	// SET DEVELOPER TEXT
	private function __setDeveloperText(){		
//		trace("__setDevText");	
		__xy =  "x=\"" + Math.round(__mc._xmouse) + "\" y=\"" + Math.round(__mc._ymouse) + "\"";

		__xyp = "x=\"" + Utils.roundTo(__mc._xmouse / __config.width * 100, 4) + "%\" y=\"" + Utils.roundTo(__mc._ymouse / __config.height * 100, 4) + "%\"";
		
		__zoom_info = "zoom=\"" + __zoom_level + "%\" zoom_x=\"" + Utils.roundTo(__map_mc._x / __config.map_width * 100, 2) + "%\" zoom_y=\"" + Utils.roundTo(__map_mc._y / __config.map_height * 100, 2) + "%\"";
		
		__long = Utils.roundTo((__map_mc._xmouse / __config.map_width) * (__data.br_long - __data.tl_long) + __data.tl_long, 4);
		
		__lat = Utils.roundTo((__map_mc._ymouse / __config.map_height) * (__data.br_lat - __data.tl_lat) + __data.tl_lat, 4);

		if(__config.projection == "mercator"){
			__lat = Utils.pixelsToMercatorLatitude(__config.projection, __map_mc._ymouse, __data.tl_lat, __data.br_lat, __config.map_height);
		}
		__long_lat = "long=\"" + __long + "\" lat=\"" + __lat + "\"";
		
        __developer_tf.htmlTxt = __long_lat + "<br>" + __xy + "<br>" + __xyp + "<br>" + __zoom_info + "<br><b>Right click to copy this info to clipboard</b>"; 
	}	
	
	private function __stageListener(){
//		trace("__StageLIstener");		
		Stage.removeListener(__stage_listener);
		var main_obj = this;		
		__stage_listener = new Object();		
		__stage_listener.onResize = function(){
			main_obj.__resize_time = 10;
		}
		
		Stage.addListener(__stage_listener);		
		clearInterval(__resize_interval);
		__resize_interval = setInterval(__countResizeTime, 20, [this]);
		__resize_time = 2;
	}

	private function __countResizeTime (params){
//		trace("__countresizetime");
		if(params[0].__resize_time > 0){
			params[0].__resize_time--;
		}
		if (params[0].__resize_time == 1){		
			// do resize
			params[0].__config.width = Stage.width;
			params[0].__config.height = Stage.height;
			params[0].__resizeMap(params[0].__map_container_mc, true);
		}
	}
	
	
	private function __initMap(){
		clearInterval(__inited_interval);
		__map_inited = true;

		__target_mc.externalCall("amMapCompleted", [__config.map_id]);
		broadcastMessage("amMapCompleted", [__config.map_id]);
	}
	
	// tell outside that chart is initialized
	private function __mapCompleted(){
		__map_completed = true;
		clearInterval(__completed_interval);		
		var process = __target_mc.__process;
		__target_mc.__process = undefined;		
		__target_mc.externalCall("amProcessCompleted", [__config.map_id, process]);
		broadcastMessage("amProcessCompleted", [__config.map_id, process]);
	}
	
	private function __alwaysHand(def:Boolean){
		if (__config.always_hand == true && __config.drag_map == true) {
			return true;
		}
		else {
			return def;
		}
	}	
	
	// die - remove all listeners
	public function die(){
		clearInterval(__zoom_interval);		
		__tween.stop();
		clearInterval(__resize_interval);
		__object_list.die();
		__movies.die();
 	    __text_box.die();
		__object_list.die();
		Stage.removeListener(__stage_listener);
		Mouse.removeListener(__mouse_listener);
		Key.removeListener(__key_listener);
	}	
	
	
	// PUBLIC FUNCTIONS
	public function clickObject(i){
		__moved = false;	   
		__getURL("#" + i);
	}	
	
	
	public function setZoom(zoom, zoom_x, zoom_y, instant){
		zoom = Utils.toNumber(Utils.stripSymbols(zoom, "%"));
		__zoomTo(zoom, zoom_x, zoom_y, Utils.parseTrue(instant));
	}	
	
	public function setZoomLongLat(zoom_level, long, lat, instant){
		var x = - (Utils.longitudeToPixels(__config.projection, long, __data.tl_long, __data.br_long, __config.map_width)  * zoom_level / 100) + __config.width / 2;
		var y = - (Utils.latitudeToPixels(__config.projection, lat, __data.tl_lat, __data.br_lat, __config.map_height) * zoom_level / 100) + __config.height / 2;
		
		__zoomTo(zoom_level, x, y, Utils.parseTrue(instant));	
	}		
	
	
	public function copyLongLat(){
		System.setClipboard(__long_lat);
	}
	public function copyStageXY(){
		System.setClipboard(__xy);
	}
	public function copyStageXYP(){
		System.setClipboard(__xyp);
	}
	public function copyZoomInfo(){
		System.setClipboard(__zoom_info);
	}	
	public function get long(){
		return(__long);
	}
	public function get lat(){
		return(__lat);
	}				
	public function get stage_x(){
		return(__mc._xmouse);
	}
	public function get stage_y(){
		return(__mc._ymouse);
	}
	public function get stage_xp(){
		return(Utils.roundTo(__mc._xmouse / __config.width * 100, 4) + "%");
	}
	public function get stage_yp(){
		return(Utils.roundTo(__mc._ymouse / __config.height * 100, 4) + "%");
	}	
	public function get zoom_x(){
		return(Utils.roundTo(__map_mc._x / __config.map_width * 100, 2) + "%");
	}	
	public function get zoom_y(){
		return(Utils.roundTo(__map_mc._y / __config.map_height * 100, 2) + "%");
	}
	public function get zoom_level(){
		return(__zoom_level + "%");
	}
	public function getCenterCoords () {
		var xx = (__config.width / 2 - __map_mc._x) / __zoom_level * 100;
		var yy = (__config.height / 2 - __map_mc._y) / __zoom_level * 100;
		var long = Utils.roundTo((xx / __config.map_width) * (__data.br_long - __data.tl_long) + __data.tl_long, 4);
		var lat = Utils.roundTo((yy / __config.map_height) * (__data.br_lat - __data.tl_lat) + __data.tl_lat, 4);
		if(__config.projection == "mercator"){
			lat = Utils.pixelsToMercatorLatitude(__config.projection, yy, __data.tl_lat, __data.br_lat, __config.map_height);
		}
		return [__config.map_id, long, lat, __zoom_level ];
	}
	public function getCurrentBounds () {
		// calc SW
		var sw_xx = (0 - __map_mc._x) / __zoom_level * 100;
		var sw_yy = (__config.height - __map_mc._y) / __zoom_level * 100;
		var sw_long = Utils.roundTo((sw_xx / __config.map_width) * (__data.br_long - __data.tl_long) + __data.tl_long, 4);
		var sw_lat = Utils.roundTo((sw_yy / __config.map_height) * (__data.br_lat - __data.tl_lat) + __data.tl_lat, 4);
		
		// calc NE
		var ne_xx = (__config.width - __map_mc._x) / __zoom_level * 100;
		var ne_yy = (0 - __map_mc._y) / __zoom_level * 100;
		var ne_long = Utils.roundTo((ne_xx / __config.map_width) * (__data.br_long - __data.tl_long) + __data.tl_long, 4);
		var ne_lat = Utils.roundTo((ne_yy / __config.map_height) * (__data.br_lat - __data.tl_lat) + __data.tl_lat, 4);
		
		if(__config.projection == "mercator"){
			sw_lat = Utils.pixelsToMercatorLatitude(__config.projection, sw_yy, __data.tl_lat, __data.br_lat, __config.map_height);
			ne_lat = Utils.pixelsToMercatorLatitude(__config.projection, ne_yy, __data.tl_lat, __data.br_lat, __config.map_height);
		}
		return [__config.map_id, sw_long, sw_lat, ne_long, ne_lat ];
	}
	public function get mc (){
		return __mc;
	}
	public function set data (data){
		__data_source = undefined;
		var old_map_file = __data.map_file;
		__data = data;
		// if the map file is new, init
		if(__data.map_file == old_map_file){
			__resizeMap(__map_container_mc, true);
		}
		else{
			die();
			__init();
		}
	}
	public function get mapInited(){
		return(__map_inited);
	}

	public function resize(width, height){
		__config.width = width;
		__config.height = height;
		__resizeMap(__map_container_mc, true);		
	}
	public function set completed(param:Boolean){
		__map_completed = param;
	}
	public function get map_id () {
		return __config.map_id;
	}
	public function disableClicks () {
		__clicks_disabled = true;
	}
	public function enableClicks () {
		__clicks_disabled = false;
	}
	public function disableBalloons () {
		__balloons_disabled = true;
	}
	public function enableBalloons () {
		__balloons_disabled = false;
	}
}