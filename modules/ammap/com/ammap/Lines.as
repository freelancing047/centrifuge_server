import com.ammap.Line;
import com.ammap.DashedLine;
import com.ammap.Utils;
import com.ammap.Bezier;

class com.ammap.Lines {

	private var __scene_lines_mc:MovieClip;
	private var __map_lines_mc:MovieClip;	
	private var __map_mc:MovieClip;
	private var __data:Object;
	private var __line_data:Object;	
	private var __config:Object;
	private var __arrows_to_resize:Array;

	function Lines(target_mc:MovieClip, map_mc:MovieClip, name:String, depth:Number, line_data:Object, config:Object, data:Object) {

		__scene_lines_mc = target_mc.createEmptyMovieClip(name, depth);
		__map_lines_mc = map_mc.createEmptyMovieClip("lines_mc", depth); // 2
			
		__map_mc = map_mc;
		__data = data;
		__line_data = line_data;
		__config = config;
		__arrows_to_resize = new Array();
		__init();
	}

	private function __init(upper_levels:Boolean) {
		for (var i = 0; i < __line_data.lines.line.length; i++) {
			if(upper_levels == true && __line_data.lines.line[i].remain == false){
				// do nothing
			}
			else{							
				var line_data = __line_data.lines.line[i];
	
				// if map x and y is not set, draw lines in lines_mc
				if(line_data.long == undefined && line_data.lat == undefined){
					var target_mc = __scene_lines_mc;
					var x = Utils.getCoordinateFromArray(line_data.x, __config.width);
					var y = Utils.getCoordinateFromArray(line_data.y, __config.height);
				}
				// if map x and y is set, draw lines in map_mc
				else{				
					var target_mc = __map_lines_mc;
					var x = Utils.longitudeToPixels(__config.projection, line_data.long, __data.tl_long, __data.br_long, __config.map_width);
					var y = Utils.latitudeToPixels(__config.projection, line_data.lat, __data.tl_lat, __data.br_lat, __config.map_height);
				}
			
				var depth = target_mc.getNextHighestDepth();
				
				if(line_data.curved == true){
					var line = new Bezier(target_mc, "line" + depth, depth, x, y, line_data.width, line_data.color);
				}
				else{
					if(line_data.dashed == true){
						var line = new DashedLine(target_mc, "line" + depth, depth, x, y, line_data.width, 5, line_data.color);
					}
					else{
						var line = new Line(target_mc, "line" + depth, depth, x, y, line_data.width, line_data.color);
					}
				}
				line.mc._alpha = line_data.alpha;
	
	
				// arrows
				if(line_data.arrow != undefined){				
					depth = target_mc.getNextHighestDepth();
					var arrow_mc = target_mc.createEmptyMovieClip("arrow_mc" + depth, depth);
		
					var arrow_container_mc = arrow_mc.attachMovie("arrow_mc", "arrow_mc", 0);
					
					arrow_container_mc._width = line_data.arrow_size;
					arrow_container_mc._height = line_data.arrow_size;
					
					// attach arrow to the end 
					if(line_data.arrow == "end" || line_data.arrow == "both"){
						// place to the end of the line
						arrow_mc._x = x[x.length - 1];
						arrow_mc._y = y[y.length - 1];
						arrow_mc._rotation = Math.atan((y[y.length - 1] - y[y.length - 2]) / (x[x.length - 1] - x[x.length - 2])) * 180 / Math.PI;
						
						if((x[x.length - 1] - x[x.length - 2]) < 0){
							arrow_mc._rotation += 180;
						}
					}
					
					if(line_data.arrow == "middle"){
	
						arrow_mc._x = x[x.length - 2] + (x[x.length - 1] - x[x.length - 2]) / 2;
						arrow_mc._y = y[y.length - 2] + (y[y.length - 1] - y[y.length - 2]) / 2;
						
						arrow_mc._rotation = Math.atan((y[y.length - 1] - y[y.length - 2]) / (x[x.length - 1] - x[x.length - 2])) * 180 / Math.PI;
						
						if((x[x.length - 1] - x[x.length - 2]) < 0){
							arrow_mc._rotation += 180;
						}
					}
					
					if(line_data.arrow == "start"){
	
						arrow_mc._x = x[0];
						arrow_mc._y = y[0];
						
						arrow_mc._rotation = Math.atan((y[1] - y[0]) / (x[1] - x[0])) * 180 / Math.PI;
						
						if((x[1] - x[0]) < 0){
							arrow_mc._rotation += 180;
						}
					}
	
					if(target_mc._parent == __map_mc && line_data.fixed_size == true){
						__arrows_to_resize.push(arrow_mc);
						arrow_mc._visible = false;
					}
	
					// color
					var color = new Color(arrow_mc);
					color.setRGB(line_data.arrow_color);
					// alpha
					arrow_mc._alpha = line_data.arrow_alpha;				
					
					
					if(line_data.arrow == "both"){
						depth = target_mc.getNextHighestDepth();					
						var arrow_mc = target_mc.createEmptyMovieClip("arrow_mc" + depth, depth);		
						var arrow_container_mc = arrow_mc.attachMovie("arrow_mc", "arrow_mc", 0);
					
						arrow_container_mc._width = line_data.arrow_size;
						arrow_container_mc._height = line_data.arrow_size;
					
						arrow_mc._x = x[0];
						arrow_mc._y = y[0];
					
						arrow_mc._rotation = Math.atan((y[1] - y[0]) / (x[1] - x[0])) * 180 / Math.PI;
						
						if((x[1] - x[0]) < 0){
							arrow_mc._rotation += 180;					
						}
						
						arrow_mc._rotation += 180;
						
						if(target_mc._parent == __map_mc && line_data.fixed_size == true){
							__arrows_to_resize.push(arrow_mc);
							arrow_mc._visible = false;
						}			
			
						// color
						var color = new Color(arrow_mc);
						color.setRGB(line_data.arrow_color);
						// alpha
						arrow_mc._alpha = line_data.arrow_alpha;									
					}
				}
			}			
		}
		// check parent
		if(__line_data.parent != __line_data){
			__line_data = __line_data.parent;
			__init(true);			
		}		
	}
	
	public function arrowsToResize():Array{
		return __arrows_to_resize;
	}
}