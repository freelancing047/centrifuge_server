import com.ammap.Text;
import com.ammap.SimpleButton;
import com.ammap.Shape;
import com.ammap.Rectangle;
import com.ammap.Line;

class com.ammap.Button extends SimpleButton {
	
	private var __label:Text;
	
	private var __h_padding:Number = 10;
	private var __v_padding:Number = 4;
		
	// text	
	private var __wrap:Boolean = false;
	
	private var __text:String;	
	private var __text_bold:Boolean;	
	private var __text_size:Number;
	private var __text_align:String = "center";
	private var __text_font:String;
	
	private var __text_underline:Boolean = false;
	private var __text_color:Number = 0x000000;

	private var __text_underline_hover:Boolean = false;
	private var __text_color_hover:Number = 0x990000;

	private var __text_underline_active:Boolean = false;
	private var __text_color_active:Number = 0xFF0000;

	private var __text_underline_pushed:Boolean = false;
	private var __text_color_pushed:Number = 0x990000;

	private var __text_underline_pushed_hover:Boolean = false;
	private var __text_color_pushed_hover:Number = 0x990000;
	
	// icon
	private var __icon_mc:MovieClip;
	private var __icon_id:String;
	private var __icon_spacing:Number = 5;
	private var __icon_color:Number;
	
	private var __text_depth:Number = 15;
	private var __icon_depth:Number = 40;
	private var __arrow_depth:Number = 50;	
	private var __mask_depth:Number = 60;		

	
	// arrow
	private var __arrow:String;
	private var __arrow_color:Number;
	private var __arrow_width:Number;
	
	function Button (target_mc:MovieClip, name:String, depth:Number, text:String, width:Number, height:Number){		
		
		super(target_mc, name, depth, width, height);
		
		__text = text;
	}
	
	private function __init () {
		// attach icon if defined
		
		var icon_width = 0;
		var icon_height = 0;		
		var icon_spacing = 0;
		
		if(__icon_id != undefined){
			__icon_mc = __mc.attachMovie(__icon_id, "icon", __icon_depth);
			
			__icon_mc._x = __h_padding + __border_width + 2;
			__icon_mc._y = __v_padding + __border_width + 2;
			
			icon_width = __icon_mc._width;
			icon_height = __icon_mc._height;
			icon_spacing = __icon_spacing;
			
			var color = new Color (__icon_mc);
			color.setRGB(__icon_color);
		}
		
		// label //////////////////////////////////////
		if(__text != "" and __text != undefined){
			if(__width != undefined) {
				var label_width = __width - 2*(__h_padding + __border_width) - icon_width - __icon_spacing;
			}
			else {
				var label_width = 0;
			}
			
			if(__height != undefined) {
				var label_height = __height - 2*(__h_padding + __border_width);
			}
			else {
				var label_height = 0;
			}
			
			var xx = __h_padding + __border_width + icon_width + icon_spacing;
			var yy = __border_width + __v_padding;
			
			var label_mc = __mc.createEmptyMovieClip("label_mc", __text_depth);
			
			__label = new Text (label_mc, "label", 0, xx, yy, label_width, label_height);
			__label.color 		= __text_color;
			__label.font 		= __text_font;
			__label.underline 	= __text_underline;
			__label.wrap 		= __wrap;
			if(__text_bold != undefined){
				__label.bold 		= __text_bold;
			}
			__label.size 		= __text_size;
			__label.align 		= __text_align;
			__label.htmlTxt 	= __text;
			
			if(__height > __label.textHeight){
				__label.y = (__height - __label.textHeight)/2 - 2;
			}
		}
		
		// attach arrow
		__arrow_width = 0;
		
		if(__arrow != undefined){
			var arrow_mc = __mc.createEmptyMovieClip("arrow_mc", __arrow_depth);
			// draw arrow
			if(__arrow == "bottom"){
				var x = [0,8,4,0];
				var y = [0,0,4,0];
			}
			if(__arrow == "top"){
				var x = [0,8,4,0];
				var y = [4,4,0,4];
			}
			
			var arrow = new Shape(arrow_mc, "arrow_mc", 1, x,y, __arrow_color, 0,0,__border_alpha,0);
			__arrow_width = 18;
		}

		// fix w and h if undefined //////////////////////////////////
		if(__width == undefined) {
			__width = 2*(__h_padding + __border_width) + icon_width + __arrow_width;
			
			__width += __label.textWidth+2;
			
			if(__text != "" and __text != undefined){
				__width += icon_spacing;
			}
		}
		
		if(__height == undefined) {
			__label.height;
			
			if(__label.height > icon_height){	
				__height = 2*(__v_padding + __border_width) + __label.height;			
			}
			else{
				__height = 2*(__v_padding + __border_width) + icon_height;			
			}
		}
		
		
		// label mask
		//var label_mask = new Rectangle (__mc, "mask_mc", __mask_depth, __width, __height, __bg_color, __border_width, __bg_color, __corner_radius);		
		//label_mc.setMask(label_mask.mc);
		
		if(__arrow != undefined){			
			var line_mc = new Line(arrow_mc, "line_mc", 2, [__border_width/2,__border_width/2],[__border_width, __height - __border_width], __border_width, __border_color, __border_alpha);
			arrow_mc._x = __width - __arrow_width - 2*__border_width;
			arrow.mc._y = (__height - 4) / 2;
			arrow.mc._x = (__arrow_width - arrow.mc._width) / 2 + __border_width;					
		}
		
		if(__icon_id != undefined){
			__icon_mc._y = (__height - __icon_mc._height) / 2 + 1;
		}
		
		super.__init();
	}

	// change colors and other properties 
	private function __changeButton (status:String) {
		// text
		__label.underline = this["__text_underline" + status];
		__label.color = this["__text_color" + status];	
		
		super.__changeButton(status);
	}
	
	public function attachIcon (id:String, color:Number){
		__icon_color = color;
		__icon_id = id;
	}
	
	public function set iconColor (color:Number){
		var c = new Color(__icon_mc);
		c.setRGB(color);
	}
	
	public function changeIcon (frame:String){
		__icon_mc.gotoAndStop(frame);
	}	

	// setters
	public function arrow (type:String, color:Number){
		__arrow = type;
		__arrow_color = color;
	}	
	public function set hPadding(param:Number) {
		__h_padding = param;
	}		
	public function set vPadding(param:Number) {
		__v_padding = param;
	}			
	public function set txt(param:String) {
		__text = param;
		__label.htmlTxt = __text;
	}
	public function set wrap(param:Boolean) {
		__wrap = param;
	}	
	public function set bold(param:Boolean) {
		__text_bold = param;
	}	
	public function set textSize(param:Number) {
		__text_size = param;
	}	
	public function set textAlign(param:String) {
		__text_align = param;
	}		
	public function set font(param:String) {
		__text_font = param;
	}			
	public function set textUnderline(param:Boolean) {
		__text_underline = param;
	}	
	public function set textColor(param:Number) {
		__text_color = param;
	}	
	public function set textUnderlineHover(param:Boolean) {
		__text_underline_hover = param;
	}	
	public function set textColorHover(param:Number) {
		__text_color_hover = param;
	}	
	public function set textUnderlineActive(param:Boolean) {
		__text_underline_active = param;
	}	
	public function set textColorActive(param:Number) {
		__text_color_active = param;
	}	
	public function set textUnderlinePushed(param:Boolean) {
		__text_underline_pushed = param;
	}	
	public function set textColorPushed(param:Number) {
		__text_color_pushed = param;
	}
	public function set textUnderlinePushedHover(param:Boolean) {
		__text_underline_pushed_hover = param;
	}	
	public function set textColorPushedHover(param:Number) {
		__text_color_pushed_hover = param;
	}	
	public function get label():Text {
		return(__label);
	}	
	public function get arrowWidth():Number{
		return (__arrow_width);
	}
	public function shrink(){
		__height = undefined;
		__width = __label.x + __label.textWidth + 5 + (__h_padding + __border_width) * 2;
		__init();
	}	
}