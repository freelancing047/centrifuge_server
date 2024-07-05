import com.ammap.Rectangle;

class com.ammap.SimpleButton {

	private var __mc:MovieClip;
	private var __bg_mc:MovieClip;	
	private var __border_mc:MovieClip;
	private var __hit_area_mc:MovieClip;	

	private var __width:Number;
	private var __height:Number;	
	private var __corner_radius:Number = 8;
		
	private var __pushable:Boolean = false; 
	private var __unpushable:Boolean = false;
	private var __pushed:Boolean = false;	
	
	private var __index;

	private var __hit_area_height:Number;
	private var __hit_area_width:Number;
	// border
	private var __border_alpha:Number = 100;
	private var __border_width:Number = 4;

	// default
	private var __bg_color:Number = 0xFFFFFF;
	private var __bg_alpha:Number = 100;
	private var __border_color:Number = 0x777777;
	
	// hover
	private var __bg_color_hover:Number = 0xFFFFFF;
	private var __bg_alpha_hover:Number = 100;
	private var __border_color_hover:Number = 0xFF9900;	
	
	// active
	private var __bg_color_active:Number = 0xFFFFFF;
	private var __bg_alpha_active:Number = 100;
	private var __border_color_active:Number = 0xFF6600;
	
	// pushed
	private var __bg_color_pushed:Number = 0xFF9900;
	private var __bg_alpha_pushed:Number = 100;	
	private var __border_color_pushed:Number = 0xFF9900;		
	
	// pushed hover
	private var __bg_color_pushed_hover:Number = 0xFF9900;
	private var __bg_alpha_pushed_hover:Number = 100;	
	private var __border_color_pushed_hover:Number = 0xFF6600;			
	
	// depths
	private var __hit_area_depth:Number = 0;
	private var __bg_depth:Number = 10;
	private var __border_depth:Number = 110;
	
	public var addListener:Function;
    public var broadcastMessage:Function;
	
	function SimpleButton (target_mc:MovieClip, name:String, depth:Number, width:Number, height:Number){
		__mc = target_mc.createEmptyMovieClip(name, depth);		
		__width	= width;
		__height = height;
	}

	private function __init (){
		AsBroadcaster.initialize(this);	
		var main_obj = this;

		// hitArea
		if(__hit_area_width == undefined){
			__hit_area_width = __width;
		}		
		if(__hit_area_height == undefined){
			__hit_area_height = __height;
		}		
		
		var hit_area = new Rectangle (__mc, "hit_area_mc", __hit_area_depth, __hit_area_width, __hit_area_height, __bg_color, __border_width, __bg_color, __corner_radius);
		__hit_area_mc = hit_area.mc;
		__hit_area_mc._alpha = 0;		
		
		// bg
		var bg = new Rectangle (__mc, "bg_mc", __bg_depth, __width, __height, __bg_color, __border_width, __bg_color, __corner_radius, __bg_alpha, 0);

		// border //////////////////////////////////////

		var border = new Rectangle (__mc, "border_mc", __border_depth, __width, __height, null, __border_width, __border_color, __corner_radius);
		__border_mc = border.mc;
		__border_mc._alpha = __border_alpha;
		
		
		
		// behaviors ///////////////////////////////////
		__hit_area_mc.onRollOver = function () {
			main_obj.broadcastMessage ("onRollOver", main_obj.__index);
			
			if(main_obj.__pushed != true){
				main_obj.__changeButton("_hover");
			}
			else {
				main_obj.__changeButton("_pushed_hover");
			}
		}		
		__hit_area_mc.onPress = function () {
			main_obj.broadcastMessage ("onPress", main_obj.__index);
			main_obj.__changeButton("_active");
		}		
		__hit_area_mc.onRelease = function () {
			
			main_obj.broadcastMessage ("onRelease", main_obj.__index);
			
			if(main_obj.__pushable == true) {
				if (main_obj.__unpushable == true && main_obj.__pushed == true) {
					 main_obj.broadcastMessage ("onUnpush", main_obj.__index);					
					 main_obj.__pushed = false;
					 main_obj.__changeButton("_hover");					 
				}
				else {
					 main_obj.broadcastMessage ("onPush", main_obj.__index);
					 main_obj.__pushed = true;
					 main_obj.__changeButton("_pushed_hover");
				}
			}
			else{
				main_obj.__changeButton("_hover");
			}
		}		
		__hit_area_mc.onRollOut = __hit_area_mc.onReleaseOutside = function () {

			main_obj.broadcastMessage ("onRollOut", main_obj.__index);
			
			if (main_obj.__pushed == true) {
				main_obj.__changeButton("_pushed");
			}
			else {
				main_obj.__changeButton("");
			}
		}		
		
		// push if pushed
		if(__pushed == true){
			__changeButton ("_pushed");
		}
	}
	
	// change colors and other properties 
	private function __changeButton (status:String) {
		// bg
		var bg = new Rectangle (__mc, "bg_mc", __bg_depth, __width, __height, this["__bg_color" + status], 0, 0, __corner_radius, this["__bg_alpha" + status], 0);

		// border
		var color2 = new Color (__border_mc);
		color2.setRGB (this["__border_color" + status]);		
	}
	
	public function show() {
		__init();
	}
	
	// setters
	public function set pushable(param:Boolean) {
		__pushable = param;
	}
	public function set unpushable(param:Boolean) {
		__unpushable = param;
	}
	public function set pushed(param:Boolean) {
		__pushed = param;
		if(param == true){
			__changeButton ("_pushed");
		}
		else{
			__changeButton ("");
		}
	}	
	
	public function set hover(param:Boolean) {
		if(param == true){
			if(__pushed == true){
				__changeButton ("_pushed_hover");
			}
			else{
				__changeButton ("_hover");
			}
		}
		else {
			if(__pushed == true){
				__changeButton ("_pushed");
			}
			else {
				__changeButton ("");
			}
		}
	}
	
	public function set index (param){
		__index = param;
	}	
	public function set cornerRadius(param:Number) {
		__corner_radius = param;
	}	
	public function set borderAlpha(param:Number) {
		__border_alpha = param;
	}			
	public function set borderWidth(param:Number) {
		__border_width = param;
	}
	public function set bgColor(param:Number) {
		__bg_color = param;
	}	
	public function set bgAlpha(param:Number) {
		__bg_alpha = param;
	}	
	public function set borderColor(param:Number) {
		__border_color = param;
	}	
	public function set bgColorHover(param:Number) {
		__bg_color_hover = param;
	}	
	public function set bgAlphaHover(param:Number) {
		__bg_alpha_hover = param;
	}	
	public function set borderColorHover(param:Number) {
		__border_color_hover = param;
	}	
	public function set bgColorActive(param:Number) {
		__bg_color_active = param;
	}	
	public function set bgAlphaActive(param:Number) {
		__bg_alpha_active = param;
	}	
	public function set borderColorActive(param:Number) {
		__border_color_active = param;
	}	
	public function set bgColorPushed(param:Number) {
		__bg_color_pushed = param;
	}	
	public function set bgAlphaPushed(param:Number) {
		__bg_alpha_pushed = param;
	}	
	public function set borderColorPushed(param:Number) {
		__border_color_pushed = param;
	}	
	public function set bgColorPushedHover(param:Number) {
		__bg_color_pushed_hover = param;
	}	
	public function set bgAlphaPushedHover(param:Number) {
		__bg_alpha_pushed_hover = param;
	}	
	public function set borderColorPushedHover(param:Number) {
		__border_color_pushed_hover = param;
	}
	public function set hitAreaWidth(param:Number) {
		__hit_area_width = param;
		__hit_area_mc._width = param;		
	}	
	public function set hitAreaHeight(param:Number) {
		__hit_area_height = param;
		__hit_area_mc._height = param;
	}	
	public function get mc ():MovieClip {
		return (__mc);
	}
	public function get width ():Number {
		return (__width);
	}	
	public function get height ():Number {
		return (__height);
	}
}
