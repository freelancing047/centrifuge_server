import com.ammap.Rectangle;
import com.ammap.Text;
import com.ammap.Utils;
import com.ammap.Line;

class com.ammap.ScrollPane {
	
	private var __mc:MovieClip;
	private var __content_mc:MovieClip;
	private var __scroller_mc:MovieClip;
	private var __scroller_bg_mc:MovieClip;
	private var __scroll_pane_mc:MovieClip;
	private var __short_lines_mc:MovieClip;	
	private var __bg_mc:MovieClip;
	private var __color:Number;
	private var __alpha:Number;
	private var __listener:Object;
	
	private var __border_width:Number = 0;
	private var __border_color:Number = 0x000000;
	private var __border_alpha:Number = 50;	
	private var __scrolling:Boolean;

	private var __width:Number;	
	private var __height:Number;						// initial height
	private var __height_real:Number;					// real height
	private var __scroller_width:Number = 18;
	private var __scroller_height:Number;
	private var __scroller_color:Number = 0x0E2E6C;
	private var __scroller_bg_color:Number = 0xDADADA;
	private var __corner_radius:Number;

    public var addListener:Function;
    public var broadcastMessage:Function;
	
	private var __html_text:String;
	
	function ScrollPane (target_mc:MovieClip, name:String, depth:Number, width:Number, height:Number, color:Number, alpha:Number, corner_radius) {

		AsBroadcaster.initialize( this );
		__corner_radius = corner_radius;
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__width = width - 1;
		__height = height - 1;
		__color = color;
		__alpha = alpha;
	}

	private function __init(){
		var main_obj = this;

		// scroll pane
		__scroll_pane_mc = __mc.createEmptyMovieClip("scroll_pane_mc", 10);
		
		// content
		__content_mc = __scroll_pane_mc.createEmptyMovieClip("content_mc", 0);

		// scroller bg
		var scroller_bg = new Rectangle (__scroll_pane_mc, "scroller_bg_mc", 20, __scroller_width, __height, __scroller_bg_color, 0,0,0,100,0);
		__scroller_bg_mc = scroller_bg.mc;
		__scroller_bg_mc._x =__width - __scroller_width - __border_width;

		// scroller
		var scroller = new Rectangle (__scroll_pane_mc, "scroller_mc", 30, __scroller_width, __height, __scroller_color, 0,0,0,100,0);
		__scroller_mc = scroller.mc;
		__scroller_mc._x =__width - __scroller_width - __border_width;
		
		// short lines
		__short_lines_mc = __scroll_pane_mc.createEmptyMovieClip("short_lines_mc", 35);
		__short_lines_mc._x = __scroller_mc._x;
		for (var i = 0; i < 4; i++){
			var line = new Line(__short_lines_mc, "line_mc" + i, i, [__scroller_width * 0.2,__scroller_width * 0.8], [0, 0], 0, __border_color);
			line.mc._y = i * 3;
		}		
	
		// behaviors
		__scroller_mc.onPress = function (){
			this.startDrag(0, main_obj.__scroller_mc._x, 0, main_obj.__scroller_mc._x, main_obj.__scroller_bg_mc._height - main_obj.__scroller_mc._height)
			main_obj.__scrolling = true;
			main_obj.broadcastMessage("onDragStart");
			_global.drag_busy = main_obj;			
		}
		__scroller_mc.onRelease = __scroller_mc.onReleaseOutside = function (){
			_global.ammap_kill_click = true;
			main_obj.__scrolling = false;
			stopDrag();
			main_obj.broadcastMessage("onDragStop");
			_global.drag_busy = "";			
		}
		
		// mouse listener
		__listener = new Object();
		__listener.onMouseMove = function (){
			if(main_obj.__scrolling == true){
				main_obj.__updatePosition();
			}
		}
		__listener.onMouseWheel = function(delta) {
			if(main_obj.__scroller_mc._visible == true && main_obj.__bg_mc.hitTest(_root._xmouse, _root._ymouse) == true && (_global.wheel_busy == main_obj || _global.wheel_busy == false)){
				
				_global.wheel_busy = main_obj;
				
				if( main_obj.__scroller_mc._y >= 0) {
					main_obj.__scroller_mc._y -= delta;
				}
				if( main_obj.__scroller_mc._y <= main_obj.__scroller_bg_mc._height - main_obj.__scroller_mc._height) {
					main_obj.__scroller_mc._y -= delta;
				}			
				if(main_obj.__scroller_mc._y < 0){
					main_obj.__scroller_mc._y = 0;
				}
				if(main_obj.__scroller_mc._y > main_obj.__scroller_bg_mc._height - main_obj.__scroller_mc._height){
					main_obj.__scroller_mc._y = main_obj.__scroller_bg_mc._height - main_obj.__scroller_mc._height;
				}
			main_obj.__updatePosition();
			}
			else{
				_global.wheel_busy = false;
			}
		}
		Mouse.addListener(__listener);
		
		__resize();
	}
	
	//RESIZE
	private function __resize(){
		var main_obj = this;
		// incase defined height is bigger then content height
		if(__height > __content_mc._height){
			__height_real = __content_mc._height;
		}
		else{
			__height_real = __height;
		}
		
		var main_obj = this;
		// scroller_height		
		__content_mc._height; // this is to workaround flash bug
		__scroller_height = __height_real * __height_real / __content_mc._height;
		__scroller_height = Utils.fitToBounds(__scroller_height, 15, __height_real);
		
		if(__scroller_height == __height_real){
			__scroller_mc._visible = false;
			__scroller_bg_mc._visible = false;
			__short_lines_mc._visible = false;
			main_obj.broadcastMessage("scrollable", false);			
		}
		else{
			__scroller_mc._visible = true;
			__scroller_bg_mc._visible = true;
			__short_lines_mc._visible = true;			
			main_obj.broadcastMessage("scrollable", true);
		}
		__scroller_mc._height = __scroller_height;
		__short_lines_mc._y =  (__scroller_mc._height - 8) / 2;
		
		// draw border
		var border = new Rectangle (__mc, "border_mc", 100, __width,__height_real, __color, __border_width, __border_color, __corner_radius, 0, __border_alpha);
		// background
		var bg = new Rectangle (__mc, "bg_mc", 0, __width, __height_real, __color, 0, __color, __corner_radius, __alpha, 0);		
		__bg_mc = bg.mc;
		
		// masks
		var mask = new Rectangle (__scroll_pane_mc, "mask_mc1", 200, __width, __height_real, 0, __border_width, 0, __corner_radius);		
		__content_mc.setMask(mask.mc);
		var mask = new Rectangle (__scroll_pane_mc, "mask_mc2", 210, __width, __height_real, 0, __border_width, 0, __corner_radius);		
		__scroller_mc.setMask(mask.mc);		
		var mask = new Rectangle (__scroll_pane_mc, "mask_mc3", 220, __width, __height_real, 0, __border_width, 0, __corner_radius);		
		__scroller_bg_mc.setMask(mask.mc);		
	}	
	
	private function __updatePosition(){
		__content_mc._y = - __scroller_mc._y * ((__content_mc._height - __height) / (__height - __scroller_mc._height));
		__short_lines_mc._y = __scroller_mc._y + (__scroller_mc._height - 8) / 2;
	}

	public function set width (param:Number){
		__width = param;
		__init();
	}	
	public function get mc():MovieClip{
		return (__mc);
	}
	public function get bg_mc():MovieClip{
		return (__bg_mc);
	}	
	public function get content_mc():MovieClip{
		return (__content_mc);
	}
	public function get scrollerVisible():Boolean{
		return(__scroller_mc._visible);
	}
	public function get scrollerWidth(){
		return(__scroller_mc._width);
	}
	public function get height():Number{
		return(__height_real);
	}
	
	public function get contentWidth(){
		if(__scroller_mc._visible == true){
			return(__width - __scroller_width - 10);
		}
		else{
			return(__width);
		}
	}
	
	public function border(color:Number, alpha:Number, width:Number){
		__border_alpha = alpha;
		__border_color = color;
		__border_width = width;		
	}
	public function scroller(color:Number, bg_color:Number){
		__scroller_color = color;
		__scroller_bg_color = bg_color;
	}
	public function show(){
		__init();
	}
	public function hide(){
		removeMovieClip(__mc);
	}
	public function update(){
		__resize();
	}		
	public function die(){
		Mouse.removeListener(__listener);
	}
}