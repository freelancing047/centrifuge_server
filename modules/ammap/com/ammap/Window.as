import com.ammap.Rectangle;
import com.ammap.Text;
import com.ammap.Utils;
import com.ammap.ScrollPane;
import flash.filters.BlurFilter;

class com.ammap.Window {
	
	private var __mc:MovieClip;
	private var __bg_mc:MovieClip;	
	private var __content_mc:MovieClip;
	private var __mask_mc:MovieClip;
	private var __scroller_mc:MovieClip;
	private var __scroller_bg_mc:MovieClip;
	private var __scroll_pane_mc:MovieClip;
	private var __close_mc:MovieClip;
	private var __scroll_pane:Object;
	
	private var __color:Number;
	private var __alpha:Number;
	private var __corner_radius:Number;		
	
	private var __locked:Boolean;
	private var __selectable:Boolean;	
	
	private var __border_width:Number = 0;
	private var __border_color:Number = 0x000000;
	private var __border_alpha:Number = 50;
	
	private var __scrolling:Boolean;
	private var __width:Number;	
	private var __height:Number;
	
	private var __text:Text;
	private var __text_color:Number;
	private var __text_size:Number;
	
	private var __title_txt:String;
	
	private var __scroll_pane_width:Number;
	private var __scroll_pane_height:Number;	
	
	private var __close_button_color_hover:Number;
	private var __close_button_color:Number;
	
	private var __shadow_alpha:Number = 50;
	private var __shadow_blur:Number = 5;
	private var __shadow_distance:Number = 5;
	private var __shadow_color:Number = 0x000000;	
	
	private var __margin_width:Number = 10;	
	private var __scroller_height:Number;
	private var __scroller_width:Number = 16;
	private var __scroller_color:Number = 0x0E2E6C;
	private var __scroller_bg_color:Number = 0xDADADA;
	
	private var __previous_height:Number;
	private var __interval:Number;

    public var addListener:Function;
    public var broadcastMessage:Function;
	
	private var __html_text:String;
	
	function Window (target_mc:MovieClip, name:String, depth:Number, title:String, width:Number, height:Number, color:Number, alpha:Number, corner_radius:Number) {

		AsBroadcaster.initialize( this );
		
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__width = width - 1;
		__height = height - 1;
		__color = color;
		__alpha = alpha;		
		__corner_radius = corner_radius;		
		__title_txt = title;
	}

	private function __init(){
		var main_obj = this;

		// pane width
		__scroll_pane_width = __width - __margin_width * 2  - __border_width * 2;
		
		// title
		if(__title_txt == undefined){
			__title_txt = "";
		}
		var title = new Text(__mc, "title_tf", 5, __border_width + __margin_width, __border_width + __margin_width, __scroll_pane_width - 15);		
		title.size = __text_size;
		title.color = __text_color;
		title.bold = true;
		title.txt = __title_txt;		
		title.height;

		// scroll pane height
		__scroll_pane_height =  __height - __margin_width * 2 - __border_width * 2 - title.height - 5;
		
		// x
		__close_mc = __mc.attachMovie("x_mc", "x_mc", 80);
		__close_mc._x = __border_width + __margin_width + __scroll_pane_width - __scroller_width - 2;
		__close_mc._y = __border_width + __margin_width;
		
		var color = new Color(__close_mc);
		color.setRGB(__close_button_color);
		
		__close_mc.onRelease = function(){
			_global.ammap_kill_click = true;			
			main_obj.__mc._visible = false;
		}

		__close_mc.onRollOver = function(){
			var color = new Color(this);
			color.setRGB(main_obj.__close_button_color_hover);
		}

		__close_mc.onRollOut = __close_mc.onReleaseOutside = function(){
			var color = new Color(this);
			color.setRGB(main_obj.__close_button_color);
		}
		
		
		// background
		var bg = new Rectangle (__mc, "bg_mc", 1, __width, __height, __color, __border_width, __border_color, __corner_radius, __alpha, __border_alpha);
		__bg_mc = bg.mc;
		
		// shadow
		var shadow = new Rectangle (__mc, "shadow_mc", 0, __width, __height, __shadow_color, 0, 0, __corner_radius, __shadow_alpha, 0);
		shadow.mc._x = __shadow_distance;
		shadow.mc._y = __shadow_distance;
		
		var filter:BlurFilter = new BlurFilter(__shadow_blur,__shadow_blur,5);
		var filterArray:Array = new Array();
		filterArray.push(filter);
		shadow.mc.filters = filterArray;		

		// scroll pane
		__scroll_pane = new ScrollPane(__mc, "scroll_pane_mc", 10, __scroll_pane_width, __scroll_pane_height, 0, 0);
		__scroll_pane.scroller(__scroller_color, __scroller_bg_color)
		__scroll_pane.border(__scroller_bg_color,0,0);
		__scroll_pane.show();
		
		__scroll_pane_mc = __scroll_pane.mc;
		__scroll_pane_mc._x = __border_width + __margin_width;
		__scroll_pane_mc._y = __border_width + __margin_width + 5 + title.height;		
		
		// content
		__content_mc = __scroll_pane.content_mc;				

		// text field
		__text = new Text(__content_mc, "text_tf", 0, 0, 0, __scroll_pane_width - __scroll_pane.scrollerWidth - 10);		
		__text.size = __text_size;
		__text.color = __text_color;
		__text.htmlTxt = __html_text;
		__text.selectable = __selectable;
		
		__text.field.onSetFocus = function(){
			_global.drag_busy = main_obj;
			_global.ammap_kill_click = true;			
		}
				
		__text.height;
		__scroll_pane.update();		
		
		__bg_mc.onPress = function(){
			if(main_obj.__locked != true){
				startDrag(main_obj.__mc);
			}
			_global.drag_busy = main_obj;
		}
		__bg_mc.onRelease = bg.mc.onReleaseOutside = function(){
			stopDrag();
			_global.drag_busy = false;
			_global.ammap_kill_click = true;			
		}
		__bg_mc.onRollOver = function (){
			_global.wheel_busy = main_obj.__scroll_pane;
			this.useHandCursor = false;
		}	
		
		if(__scroll_pane.scrollerVisible == false){
			__text.width = __scroll_pane_width - 10;
		}
	}
	
	public function set height (param:Number){
		__height = param;
		__init();		
	}
	public function set width (param:Number){
		__width = param;
		__init();
	}	
	public function get mc():MovieClip{
		return (__mc);
	}

	public function border(color:Number, alpha:Number, width:Number){
		__border_alpha = alpha;
		__border_color = color;
		__border_width = width;
	}
	public function scroller(color:Number, bg_color:Number, width:Number){
		__scroller_color = color;
		__scroller_bg_color = bg_color;
		__scroller_width = width;
	}
	public function set margin_width (param){
		__margin_width = param;
	}	
	public function text (size:Number, color:Number){
		__text_size = size;
		__text_color = color;
	}	
	public function set content(param){
		__html_text = param;
	}	
	public function show(){
		__init();
	}
	public function shadow(color:Number, alpha:Number, blur:Number, distance:Number){
		__shadow_alpha = alpha;
		__shadow_color = color;
		__shadow_blur = blur;
		__shadow_distance = distance;		
	}
	public function closeButton(color, color_hover){
		__close_button_color = color;
		__close_button_color_hover = color_hover;		
	}
	public function hideX(){
		__close_mc._visible = false;
	}
	public function set locked(param){
		__locked = param;
	}
	public function set selectable(param){
		__selectable = param;
	}	
	public function die(){
		__scroll_pane.die();
	}
}