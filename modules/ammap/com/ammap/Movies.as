import com.ammap.Utils;
import com.ammap.Load;
import com.ammap.FadeColor;

class com.ammap.Movies {

	private var __target_mc:MovieClip;
	private var __scene_movies_mc:MovieClip;
	private var __map_movies_mc:MovieClip;
	private var __map_mc:MovieClip;
	private var __data:Object;
	private var __movie_data:Object;	
	private var __config:Object;
	private var __config_original;	
	private var __intervals:Array;
	private var __count:Number;	
	private var __data_original;		
	private var __movies_to_resize:Array;
	private var __zoom_level:Number;
    private var __embeded_movies:Array = ["rectangle", "rectangle_centered", "circle", "border", "target", "home"];
    private var __ammap_obj:Object;
	public var addListener:Function;
    public var broadcastMessage:Function;
		
	function Movies(target_mc:MovieClip, map_mc:MovieClip, name:String, depth:Number, movie_data:Object, config:Object, data:Object, config_original, data_original, ammap_obj) {
		AsBroadcaster.initialize(this);
		die();
		__target_mc = target_mc;
		__scene_movies_mc = target_mc.createEmptyMovieClip(name, depth);		
		__map_movies_mc = map_mc.createEmptyMovieClip("movies_mc", depth);
		__map_mc = map_mc;
		__data = data;
		__movie_data = movie_data;		
		__config = config;
		__movies_to_resize = new Array();
		__config_original = config_original;
		__data_original = data_original;
		__ammap_obj = ammap_obj;
		__init();
	}

	private function __init(upper_levels:Boolean) {
		
		var main_obj = this;
		__count = 0;
		__intervals = new Array();
		if(__movie_data.movies.movie.length > 0){

			for (var i = 0; i < __movie_data.movies.movie.length; i++){
				
				var movie_data = __movie_data.movies.movie[i];
				
				if(upper_levels == true && movie_data.remain == false){
					// do nothing
				}
				else{					
					// if long and lat is not set, load movies in movies_mc
					if(isNaN(movie_data.long) == true && isNaN(movie_data.lat) == true){
						var target_mc = __scene_movies_mc;
						var x = Utils.getCoordinate(movie_data.x, __config.width);
						var y = Utils.getCoordinate(movie_data.y, __config.height);
					}
					// if mlong and latis set, load movies in map_mc
					else{
						var target_mc = __map_movies_mc;
						var x = Utils.longitudeToPixels(__config.projection, movie_data.long, __data.tl_long, __data.br_long, __config.map_width);
						var y = Utils.latitudeToPixels(__config.projection, movie_data.lat, __data.tl_lat, __data.br_lat, __config.map_height);
					}
					var depth = target_mc.getNextHighestDepth();
					
					var movie_mc = target_mc.createEmptyMovieClip("movie_mc" + depth, depth);
					movie_data.mc = movie_mc;
					
					// movie is loaded in this container
					var container_mc = movie_mc.createEmptyMovieClip("container_mc", 0);
					// position movie
					movie_mc._x = x;
					movie_mc._y = y;
					
					movie_mc.data_source = movie_data;
					
					// incase this movie is in the map and fixed size is true place it to resizeable array
					if(target_mc._parent == __map_mc && movie_data.fixed_size == true){
						__movies_to_resize.push(movie_mc);
						// make invisible (it gets visible then resized)
						movie_mc._visible = false;
					}
					// set color
					if(movie_data.color != undefined){
						var color = new Color(movie_mc);
						color.setRGB(movie_data.color);
					}
					// set alpha
					movie_mc._alpha = movie_data.alpha;
	
					// behaviors
					// roll over
					// all behaviours are active only if
					
					if(Utils.checkIfClickable(movie_data) == false && movie_data.balloon == false){
						//void
					}
					else{
						movie_mc.onRollOver = function(){
							if(main_obj.__config.movie.disable_when_clicked == true && main_obj.__config.selected_data_source == this.data_source){							
								this.useHandCursor = false;
							}
							else{
								main_obj.broadcastMessage("onRollOver", this.data_source);
							}
						}
						// roll out
						movie_mc.onRollOut = movie_mc.onReleaseOutside = function(){
							main_obj.broadcastMessage("onRollOut", this.data_source);
							
							if(main_obj.__config.selected_data_source == this.data_source){
								//void
							}
							else if(this.data_source.color != undefined){
								this.fader = new FadeColor(this);
								this.fader.fadeTo(this.data_source.color, main_obj.__config.color_change_time_hover);
							}
						}
						
						if(Utils.checkIfClickable(movie_data) == true){
							movie_mc.onRelease = function(){
								_global.ammap_kill_click = true;
								main_obj.broadcastMessage("onGetURL", this.data_source);
							}
						}
					}
					if(__checkIfEmbeded(movie_data.file) == true){
						var movie = container_mc.attachMovie(movie_data.file, "movie_mc", 0);
						__passMovieData(movie, [movie_data]);
						__resizeMovie(movie, [movie_data]);
					}
					else{
						if(movie_data.file != undefined){
							var loader:Load = new Load ();
							loader.loadClip2(__config.path + movie_data.file, container_mc, this, "__passMovieData", false, undefined, [movie_data], "__resizeMovie");
						}
					}					
					if(movie_data.rotation != undefined){
						container_mc._rotation = movie_data.rotation;
					}					
					
					if(__config.selected_data_source == movie_data && movie_data.color_selected != undefined){
					    var color = new Color(movie_mc);
						color.setRGB(movie_data.color_selected);
					}
				}
			}
		}		
				
		// check parent
		if(__movie_data.parent != __movie_data){
			__movie_data = __movie_data.parent;
			__init(true);			
		}
	}
	
	private function __checkIfEmbeded(name:String){
		for (var i = 0; i < __embeded_movies.length; i++){
			if(name == __embeded_movies[i]){
				return true;
			}
		}
	}
	
	
	
	// PASS DATA TO LOADED MOVIES
	private function __passMovieData(movie_mc:MovieClip, params:Array){
		var movie_data = params[0];
		
		var flash_vars = movie_data.flash_vars.split("&");
		
		for(var i = 0; i < flash_vars.length; i++){
			var varval = flash_vars[i].split("=");
			movie_mc[varval[0]] = varval[1];
		}		
		
		
		if(movie_data.center == true){
			movie_mc._x = movie_mc._x - movie_mc._width / 2;
			movie_mc._y = movie_mc._y - movie_mc._height / 2;
		}
	
		movie_mc.config = __config;
		movie_mc.data = __data;
		movie_mc.config_original = __config_original;
		movie_mc.data_original = __data_original;
		
		// add reference to the main AmMap object
		movie_mc._ammap = __ammap_obj;
		
		__target_mc.all_movies.push(movie_mc);
	}
	
	private function __resizeMovie(movie_mc:MovieClip, params:Array){
		var movie_data = params[0];
		// resize
		if(movie_data.width != undefined){
			movie_mc._width = Utils.getCoordinate(movie_data.width, __config.width);
		}
		if(movie_data.height != undefined){
			movie_mc._height = Utils.getCoordinate(movie_data.height, __config.height);
		}		
	}
	
	public function moviesToResize():Array{
		return __movies_to_resize;
	}
	public function die(){
		for(var i = 0; i < __target_mc.all_movies.length; i++){
			__target_mc.all_movies[i].die();
		}
	}
}