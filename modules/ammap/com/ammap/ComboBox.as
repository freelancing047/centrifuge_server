import com.ammap.Button;
import com.ammap.Text;
import com.ammap.Utils;
import com.ammap.ScrollPane;

class com.ammap.ComboBox {
	
	private var __mc:MovieClip;
    private var __item_names:Array;
    private var __item_values:Array;	
    private var __item_colors:Array;
	private var __type:String;
	private var __width:Number;
	private var __height:Number;
	private var __button:Button;
	private var __item_button:Array;
	private var __drop_down:ScrollPane;	
	private var __selected_item:Number = 0;
	private var __scroller_width:Number = 18;
	private var __icon:String;
	private var __listener:Object;	
	private var __bg_color:Number;
	private var __bg_alpha:Number;	
	private var __corner_radius:Number;
	private var __text_size:Number;
	private var __border_color:Number;
	private var __border_width:Number;
	private var __border_alpha:Number;
	private var __text_color:Number;
	private var __text_color_selected:Number;
	private var __text_color_hover:Number;
	private var __bg_color_selected:Number;
	private var __bg_color_hover:Number;	
	private var __scroller_color:Number;
	private var __scroller_bg_color:Number;
	private var __count:Number;

	
	public var addListener:Function;
    public var broadcastMessage:Function;
	
	
	function ComboBox (target_mc:MovieClip, name:String, depth:Number, width:Number, height:Number, type:String, style:Object) {
		AsBroadcaster.initialize( this );		
		__type = type;
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__width = width;
		__height = height;
		__item_names = new Array();
		__item_values = new Array();		
		__item_colors = new Array();
		
		__bg_color = style.color;
		__bg_alpha = style.alpha;
		__corner_radius = style.corner_radius;
		__text_size = style.text_size;
		__text_color = style.text_color;
		__text_color_selected = style.text_color_selected;		
		__text_color_hover = style.text_color_hover;				
		__border_color = style.border_color;
		__border_width = 0;
		__border_alpha = style.border_alpha;
		__scroller_color = style.scroller_color;
		__scroller_bg_color = style.scroller_bg_color;
		__bg_color_selected = style.color_selected;
		__bg_color_hover = style.color_hover;				
		__count = -1;
	}

	private function __init(){
		
		var main_obj = this;
		var button_height = 0;		
		// create main button if type is cb
		if(__type == "dropdown"){
			
			__listener = new Object();
			
			__listener.onMouseUp = function(){
				if(main_obj.__drop_down.bg_mc.hitTest(_xmouse, _ymouse) == false && main_obj.__button.mc.hitTest(_xmouse, _ymouse) == false){
					main_obj.__hideDropDown();
				}
			}
			Mouse.addListener (__listener);			
			
			__button = new Button(__mc, "button_mc", 2, __item_names[__selected_item], __width);
			if(__icon != undefined){
				__button.attachIcon(__icon, __item_colors[__selected_item]);
			}

			__button.wrap = false;
			__button.textSize = __text_size;
			__button.cornerRadius = __corner_radius;
			__button.vPadding = 3;
			__button.hPadding = 5;
			__button.borderColor = __border_color;
			__button.borderColorActive = __border_color;
			__button.borderColorHover = __border_color;
			__button.borderColorPushedHover = __border_color;
			__button.borderColorPushed = __border_color;
			__button.textColor = __text_color;
			__button.textColorActive = __text_color;
			__button.textColorHover = __text_color;
			__button.textColorPushedHover = __text_color;
			__button.textColorPushed = __text_color;
			__button.bgColor = __bg_color;
			__button.bgColorActive = __bg_color;
			__button.bgColorHover = __bg_color;
			__button.bgColorPushed = __bg_color;
			__button.bgColorPushedHover = __bg_color;
			
			__button.bgAlpha = __bg_alpha;
			__button.bgAlphaActive = __bg_alpha;
			__button.bgAlphaHover = __bg_alpha;
			__button.bgAlphaPushed = __bg_alpha;
			__button.bgAlphaPushedHover = __bg_alpha;
			__button.borderAlpha = __border_alpha;
			__button.borderWidth = __border_width;
			__button.textAlign = "left";
			__button.pushable = true;
			__button.unpushable = true;
			__button.arrow ("bottom", __border_color);
			__button.show();
			
			var listener = new Object();
			listener.onPush = function(){
				_global.wheel_busy = main_obj.__drop_down;
				_global.ammap_kill_click = true;				
				main_obj.__showDropDown();
			}
			listener.onUnpush = function(){
				_global.ammap_kill_click = true;				
				main_obj.__hideDropDown();
			}
			__button.addListener(listener);
			
			button_height = __button.mc._height;
		}

		// create drop down
		__drop_down = new ScrollPane(__mc, "dropdown_mc", 1, __width + 1, __height - button_height, __bg_color, __bg_alpha, __corner_radius);
		__drop_down.scroller(__scroller_color, __scroller_bg_color)
		__drop_down.border(__border_color, __border_alpha, __border_width);
		__drop_down.show();


		// lisens whether scroller is visible and resizes buttons
		var dropdown_listener = new Object();		
		dropdown_listener.scrollable = function(param:Boolean){
			main_obj.__resizeItems(param);
		}
		__drop_down.addListener(dropdown_listener);		

		
		var item_listener = new Object();

		item_listener.onPush = function(index){
			main_obj.__selectItem(index);			
			_global.ammap_kill_click = true;			
		}
		item_listener.onUnpush = function(index){
			main_obj.__deselectItem(index);			
			_global.ammap_kill_click = true;
		}		
		item_listener.onRollOver = function(index){
			_global.wheel_busy = main_obj.__drop_down;
			main_obj.broadcastMessage("onRollOver",  main_obj.__item_values[index]);	
		}				
		item_listener.onRollOut = function(index){
			main_obj.broadcastMessage("onRollOut",  main_obj.__item_values[index]);	
		}						
		
		__item_button = new Array();
		
		for (var i = 0; i < __item_names.length; i++){
			__item_button[i] = new Button(__drop_down.content_mc, "dropdown_item_mc" + i, i, __item_names[i], __width);
			__item_button[i].wrap = false;
			__item_button[i].textSize = __text_size;			
			__item_button[i].pushable = true;
			if(__type == "list"){
				__item_button[i].unpushable = true;			
			}
			if(__icon != undefined){
				__item_button[i].attachIcon(__icon, __item_colors[i]);
			}
			__item_button[i].textAlign = "left";
			__item_button[i].vPadding = 3;
			__item_button[i].borderWidth = 0;
			__item_button[i].hPadding = 5;
			__item_button[i].cornerRadius = 0;
			__item_button[i].borderAlpha = 0;
			__item_button[i].textColor = __text_color;
			__item_button[i].textColorActive = __text_color;
			__item_button[i].textColorHover = __text_color_hover;
			__item_button[i].textColorPushed = __text_color_selected;
			__item_button[i].textColorPushedHover = __text_color_selected;
			__item_button[i].bgAlpha = 0;
			__item_button[i].bgAlphaHover = 100;			
			__item_button[i].bgAlphaSelected = 100;	
			__item_button[i].bgColor = __bg_color;
			__item_button[i].bgColorActive = __bg_color;
			__item_button[i].bgColorHover = __bg_color_hover;
			__item_button[i].bgColorPushed = __bg_color_selected;
			__item_button[i].bgColorPushedHover = __bg_color_selected;
			__item_button[i].index = i;
			__item_button[i].show();
			__item_button[i].mc._y = __item_button[i].mc._height * i;
			__item_button[i].addListener(item_listener);
		}
		__drop_down.update();
		if(__type == "dropdown"){
			__drop_down.mc._y = __button.mc._height - __border_width;
			__hideDropDown();
		}		
	}
	
	private function __selectItem(index, broadcast:Boolean){
		__selected_item = index;


		if(__icon != undefined){
			__button.iconColor = (__item_colors[__selected_item]);
			__item_button[__selected_item].changeIcon(true);
		}

		if(__type == "dropdown"){
			__hideDropDown();

		}
		
		// unpush all other
		for (var i = 0; i < __item_names.length; i++){
			if(i != __selected_item){
				__item_button[i].pushed = false;
			}
		}
		if(__item_button[index] != undefined){
			__item_button[index].pushed = true;
			__button.txt = __item_names[index];	
		
			if(broadcast!= false){
				broadcastMessage("onChange", __item_values[index]);
				broadcastMessage("onSelect", __item_values[index]);	
			}
		}
	}
	
	public function itemSelected(index:Number, param:Boolean){
		__item_button[index].pushed = param;
	}
	
	public function valueSelected(value, param:Boolean){
		for(var i = 0; i < __item_values.length; i++){
			if(__item_values[i] == value){
				__item_button[i].pushed = param;								

				if(__icon != undefined){
					__item_button[i].changeIcon(param);
				}				
			}
		}
	}	
	
	private function __deselectItem(index){
		if(__icon != undefined){			
			__item_button[index].changeIcon(false);
		}		
		broadcastMessage("onDeselect", index, __item_names[index], __item_values[index]);
	}
	
	private function __resizeItems(scrollable:Boolean){
		if(scrollable == true){
			var width = __width - __scroller_width;	
		}
		else{
			var width = __width;			
//			__drop_down.shrink();			
		}
		for (var i = 0; i < __item_names.length; i++){
			__item_button[i].hitAreaWidth = width;
		}		
	}
	
	private function __showDropDown(){
		__drop_down.mc._visible = true;		
	}
	private function __hideDropDown(){
		_global.wheel_busy = false;
		__button.pushed = false;	
		__drop_down.mc._visible = false;
	}	
	public function addItem(name, value, selected_color){
		__item_names.push(name);
		__item_values.push(value);		
		__item_colors.push(selected_color);
		__count++;
		return(__count);
		
	}
	public function selectItem(index){
		__selectItem(index, false);
	}
	public function get mc(){
		return(__mc);
	}	
	public function show(){
		__init();		
	}
	public function get height(){
		if(__type == "dropdown"){
			return(__button.height);
		}
		else{
			return __drop_down.height;
		}
	}	
	public function set icon(param:String){
		__icon = param;
	}
	public function get itemCount(){
		return __count;
	}
	public function addItemCount(){
		__count ++;
	}	
	
	public function die(){
		Mouse.removeListener(__listener);
		if(__mc != undefined){
			removeMovieClip(__mc);
		}
		__drop_down.die();
	}
}
