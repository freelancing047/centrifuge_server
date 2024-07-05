import com.ammap.Rectangle;
import com.ammap.Text;

class com.ammap.Preloader {
	
	private var __mc:MovieClip;
	private var __bar_width:Number;
	private var __bar_height:Number = 5;
	private var __bar:Rectangle;
	private var __loader;
	private var __width:Number;
	private var __height:Number;
	private var __color:Number = 0x000000;
	private var __bg_color:Number;
	private var __interval:Number;
	private var __label:Text;
	private var __text:String;
	private var __show_info:Boolean = true;
	private var __separator:String = ": ";
	private var __bar_x:Number;
	private var __bar_y:Number;
		
	public function Preloader(target_mc:MovieClip,		  
					 		  name:String,
							  depth:Number,
							  loader,
							  width:Number,
					   		  height:Number,
							  color:Number,
							  bgColor:Number,
							  text:String){
		
		__loader = loader;
		__text = text;
		__width = width;
		__height = height;
		if(color != undefined){
			__color = color;
		}
		__bg_color = bgColor;
		
		if(__text == undefined){
			__text = "";
			__separator = "";
		}
				
		//create main preloader holder mc
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__init ();
	}
	
	
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	private function __init (){
		
		var bg = new Rectangle(__mc, "bg_mc", 0, __width, __height, __bg_color);
		
		// if color is not defined, bacground is transparent
		if (__bg_color == undefined or __bg_color == null){
			bg.mc._alpha = 0;
		}
		
		// this will disable all mouse behaviors for objects under preloader
		bg.mc.onRollOver = function(){
			this.useHandCursor = false;
		}
		
		// bar width is 1/2 of the whole preloader
		__bar_width = __width/2;
		// bar x, y (aligned to the v and h center)
		__bar_x = (__width - __bar_width)/2;
		__bar_y = (__height - __bar_height)/2;		
		
		// create bar outline
		var outline = new Rectangle (__mc, "outline_mc", 1, __bar_width, __bar_height, null, 1, __color);
		outline.mc._x = __bar_x;
		outline.mc._y = __bar_y;		
		
		// create bar
		__bar = new Rectangle (__mc, "bar_mc", 2, __bar_width, __bar_height-1, __color);
		__bar.mc._x = __bar_x;
		__bar.mc._y = __bar_y;
		__bar.mc._width = 0;
		
		// create label
		if(__text != "" or __show_info == true){
			__label = new Text (__mc, "label", 3, __bar_x, __bar_x, __bar_width);
			__label.txt = __text;
			__label.color = __color;		
			__label.x -= 2;
			__label.y = __bar_y - __label.textHeight - 3;
		}
		
		// set interval for progress checking
		__interval = setInterval (this, "updateBar", 50);
	}
	
	public function updateBar(){
		// how many percent loaded
		var percent = Math.round(__loader.getBytesLoaded() / __loader.getBytesTotal()*100);
		
		if(isNaN(percent) || percent == Infinity || percent < 0){
			percent = 0;
		}
		
		var total = __loader.getBytesTotal();
		if(total == undefined){
			total = 0;
		}
		
		if(__show_info == true){
			__label.txt = __text + __separator + percent + "% / " + Math.round(total/1024)+" kb";
			// update label y
			__label.y = __bar_y - __label.textHeight - 3;				
		}
		
		// set bar width
		__bar.mc._width = __bar_width * percent / 100;
	}
	
	// set __info
	public function set showInfo(param:Boolean){
		__show_info = param;
	}
	
	// remove preloader
	public function remove(){		
		clearInterval(__interval);

		if(__mc != undefined){
			removeMovieClip(__mc);
		}
	}
	
	public function get mc (){
		return __mc;
	}	
}