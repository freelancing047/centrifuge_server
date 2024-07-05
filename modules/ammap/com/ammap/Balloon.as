import com.ammap.Shape;
import com.ammap.Rectangle;
import com.ammap.Text;

class com.ammap.Balloon {
	
	private var __mc:MovieClip;
	private var __text_field:Text;

	private var __pointer_pos:String = "vertical"; // verctical
	private var __l:Number;	// boundig box x left
	private var __r:Number;	// boundig box x right
	private var __t:Number;	// boundig box y top
	private var __b:Number; // boundig box y bottom
	private var __max_width:Number = 200;
	private var __distance:Number = 15;	// distance from point to box
	private var __pointer_width:Number	 = 26; // pointer width near box
	private var __x:Number; // x to point
	private var __y:Number; // y to point
	private var __bx:Number; // x of box (optional)
	private var __by:Number; // y of box (optional)	
	private var __text:String;  
	private var __text_width:Number;	
	private var __text_height:Number;
	private var __text_color:Number;	
	private var __text_size:Number;
	private var __font:String;	
	private var __bg_color:Number = 0xFFFFFF;
	private var __bg_alpha:Number = 100;	
	private var __border_width:Number = 0;	
	private var __border_color:Number = 0xFFCC00;		
	private var __border_alpha:Number = 100;
	private var __v_margins:Number = 10; // balloon content vertical margins
	private var __h_margins:Number = 10; // balloon content horizontal margins
	private var __b_margins = 0; // bound margins
	private var __bg_mc:MovieClip;
	private var __corner_radius:Number = 0;
	private var __show_bubble:Boolean = false;
	
	/// construct ///
	public function Balloon(target_mc:MovieClip, name:String, depth:Number, text:String, l:Number, t:Number, r:Number, b:Number, max_width:Number){
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__text = text;
		
		// remove last br 
		if(__text.substr(-4) =="<br>"){
			__text = text.slice (0, -4);
		}
		
		__l = l;
		__r = r;
		__t = t;
		__b = b;
		__max_width = max_width || __max_width;
	}
	
	/// init ////
	private function __init(){

		// create text field
		__text_width = __max_width - __h_margins * 2 - __distance;
		__text_field = new Text (__mc, "textField", 10, 0, 0, __text_width, 0, "left");
		__text_field.color = __text_color;
		__text_field.size = __text_size;		
		__text_field.font = __font;				
		__text_field.htmlTxt = __text;
		// update text field width if textWidth is less then maxWidth
		if(__text_field.textWidth < __text_width) {
			__text_field.width = __text_field.textWidth + 5; // add 5 to avoid wrapping
		}
		
		__text_width = __text_field.width;
		__text_height = __text_field.height;
		
		// place text field
		__build();
	}
	
	private function __build(){

		var x:Array = new Array();
		var y:Array = new Array();
		
		
		// if pointer horizontal
		if(__pointer_pos == "horizontal"){

			// adjust y
			// by default align to center
			if(__corner_radius == 0){
				__text_field.y = __y - __text_height / 2;
			}
			else{
				__text_field.y = __y + __corner_radius + __border_width;
			}
			
			if(__by != undefined) {
				__text_field.y = __by + __v_margins;
			}
			if (__bx != undefined){
				__text_field.x = __bx + __h_margins;
			}
			
			// check bounds
			if(__text_field.y < __t + __b_margins + __v_margins){
				__text_field.y = __t + __b_margins + __v_margins;
			}
			if(__text_field.y + __text_height > __b - __b_margins - __v_margins){
				__text_field.y = __b - __b_margins - __text_height - __v_margins;
			}
			// calculate pointer root coords
			var py1 = __y - __pointer_width/2;
			if(py1 < __text_field.y - __v_margins){
				py1 = __text_field.y - __v_margins;
			}
			
			if(py1 > __text_field.y + __v_margins + __text_height - __pointer_width){
				py1 = __text_field.y + __v_margins + __text_height - __pointer_width;
			}
			
			var py2 = __y + __pointer_width/2;
			if(py2 > __text_field.y + __v_margins + __text_height){
				py2 = __text_field.y + __v_margins + __text_height;
			}
			if(py2 < __text_field.y - __v_margins + __pointer_width){
				py2 = __text_field.y - __v_margins + __pointer_width;
			}			
			
			// adjust x and create shape arrays
			if(__x >= (__r - __l)/2 + __l){
				if(__bx == undefined) {
					__text_field.x = __x - (__text_width + __h_margins + __distance + __corner_radius / 2);
				}
		
				x = [(__text_field.x - __h_margins),(__text_field.x + __h_margins + __text_width),(__text_field.x + __h_margins + __text_width), __x, (__text_field.x + __h_margins + __text_width), (__text_field.x + __h_margins + __text_width), (__text_field.x - __h_margins)];
				y = [(__text_field.y - __v_margins), (__text_field.y - __v_margins), py1, __y, py2, (__text_field.y + __v_margins + __text_height), (__text_field.y + __v_margins + __text_height),(__text_field.y - __v_margins)];
			}
			else{
				if(__bx == undefined) {				
					__text_field.x = __x + __h_margins + __distance + __corner_radius;
				}
		
				x = [(__text_field.x - __h_margins), (__text_field.x + __text_width + __h_margins), (__text_field.x + __text_width + __h_margins), (__text_field.x - __h_margins), (__text_field.x - __h_margins), __x, (__text_field.x - __h_margins), (__text_field.x - __h_margins)];
				y = [(__text_field.y - __v_margins), (__text_field.y - __v_margins), (__text_field.y + __text_height + __v_margins), (__text_field.y + __text_height + __v_margins), py2, __y, py1, (__text_field.y - __v_margins)];
				
			}
		}
		// vertical
		if(__pointer_pos == "vertical"){
			// adjust x
			// by default align to center
			__text_field.x = __x - __text_width/2;
			
			if(__by != undefined) {
				__text_field.y = __by + __v_margins;
			}
			if (__bx != undefined){
				__text_field.x = __bx + __h_margins;
			}			
			
			// check bounds
			if(__text_field.x < __l + __b_margins + __h_margins){
				__text_field.x = __l + __b_margins + __h_margins;
			}
			if(__text_field.x + __text_width > __r - __b_margins - __h_margins){
				__text_field.x = __r - __b_margins - __text_width - __h_margins;
			}
			// calculate pointer root coords			
			var px1 = __x - __pointer_width/2;
			if(px1 < __text_field.x - __h_margins){
				px1 = __text_field.x - __h_margins;
			}
			
			if(px1 > __text_field.x + __h_margins + __text_width - __pointer_width){
				px1 = __text_field.x + __h_margins + __text_width - __pointer_width;
			}
			
			var px2 = __x + __pointer_width/2;
			if(px2 > __text_field.x + __h_margins + __text_width){
				px2 = __text_field.x + __h_margins + __text_width;
			}
			if(px2 < __text_field.x - __h_margins + __pointer_width){
				px2 = __text_field.x - __h_margins + __pointer_width;
			}				
			
			
			
			// adjust y
			if(__y >= (__b - __t)/2 + __t){
				
				if(__by == undefined){
					__text_field.y = __y - (__text_height + __v_margins + __distance);
				}
				x = [(__text_field.x - __h_margins), (__text_field.x +  __text_width + __h_margins), (__text_field.x + __text_width + __h_margins), px2, __x, px1, (__text_field.x - __h_margins), (__text_field.x - __h_margins)];
				y = [(__text_field.y - __v_margins), (__text_field.y - __v_margins), (__text_field.y + __v_margins + __text_height), (__text_field.y + __v_margins + __text_height), __y, (__text_field.y + __v_margins + __text_height), (__text_field.y + __v_margins + __text_height), (__text_field.y - __v_margins)];
			}
			else{
				if(__by == undefined){				
					__text_field.y = __y + __v_margins + __distance;
				}
				x = [(__text_field.x - __h_margins), px1, __x, px2, (__text_field.x +  __text_width + __h_margins), (__text_field.x +  __text_width + __h_margins), (__text_field.x - __h_margins), (__text_field.x - __h_margins)];
				y = [(__text_field.y - __v_margins), (__text_field.y - __v_margins), __y, (__text_field.y - __v_margins), (__text_field.y - __v_margins), (__text_field.y + __v_margins + __text_height), (__text_field.y + __v_margins + __text_height), (__text_field.y - __v_margins)];
			}
		}

		if(__corner_radius == 0){
			var shape = new Shape (__mc, "bg_mc", 0, x, y, __bg_color, __border_width, __border_color, __bg_alpha, __border_alpha);
		}
		else{
			var shape = new Rectangle(__mc, "bg_mc", 0, __text_field.width + __h_margins * 2 + __corner_radius, __text_field.height + (__v_margins + __corner_radius) * 2, __bg_color, __border_width, __border_color, __corner_radius, __bg_alpha, __border_alpha);
			shape.mc._x = __text_field.x - __h_margins - __corner_radius / 2;
			shape.mc._y = __text_field.y - __v_margins - __corner_radius - 0.5;			
		}
		
		__bg_mc = shape.mc;
		
		if(__show_bubble == true){
			var bubble_mc = __mc.attachMovie("bullet_round", "bullet_round", 90);
			bubble_mc._x = __x;
			bubble_mc._y = __y;
			
			var color = new Color(bubble_mc);
			color.setRGB(__bg_color);
		}
	}
	
	public function pointTo(x:Number, y:Number){
		__x = x;
		__y = y - 0.5;
		__build();
	}
	
	public function show(){
		__init();
	}	
	/// setters /////////////////////////////////////////
	public function set pointerPosition (param:String){
		__pointer_pos = param;
	}
	public function set distance (param:Number){
		__distance = param;
	}
	public function set pointerWidth (param:Number){
		__pointer_width = param;
	}
	public function set boxX (param:Number){
		__bx = param;
	}	
	public function set boxY (param:Number){
		__by = param;
	}	
	public function set textColor (param:Number){
		__text_color = param;
	}	
	public function set textSize (param:Number){
		__text_size = param;
	}		
	public function set font (param:String){
		__font = param;
	}			
	public function set bgColor (param:Number){
		__bg_color = param;
	}	
	public function set bgAlpha (param:Number){
		__bg_alpha = param;
	}
	public function set borderWidth (param:Number){
		__border_width = param;
	}
	public function set borderColor (param:Number){
		__border_color = param;
	}	
	public function set borderAlpha (param:Number){
		__border_alpha = param;
	}	

	public function set vMargins (param:Number){
		__v_margins = param;
	}		
	public function set hMargins (param:Number){
		__h_margins = param;
	}			
	public function set bMargins (param:Number){
		__b_margins = param;
	}	
	
	public function set showBubble (param:Boolean){
		__show_bubble = param;
	}	
	
	public function get mc ():MovieClip {
		return(__mc);
	}
	public function get bg_mc():MovieClip {
		return(__bg_mc);
	}
	public function hide(){
		if(__mc != undefined){
			__mc._visible = false;
//			removeMovieClip(__mc);
		}
	}
	public function set cornerRadius(param){
		__corner_radius = param;
	}
	public function get yTop(){
		return(__text_field.y - __v_margins);
	}
	public function get yBot(){
		return(__text_field.y + __v_margins + __text_height);
	}	
}