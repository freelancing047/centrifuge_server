import com.ammap.Rectangle;
import com.ammap.Button;
import com.ammap.Text;

class com.ammap.PopUp {
	
	private var __bg_color:Number = 0xFFFFFF;
	private var __width:Number = 300;
	private var __text_color:Number = 0x000000;
	private var __margins:Number = 15;
	private var __mc:MovieClip;
	private var __x:Number;
	private var __y:Number;	
	
	function PopUp(target_mc:MovieClip, name:String, depth:Number, message:String, x:Number, y:Number, width:Number, bg_color:Number, text_color:Number){
		__bg_color = bg_color;
		__text_color = text_color;
		__width = width;
		__x = x;
		__y = y;
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__init(message);
	}
	
	
	private function __init(message:String){
		// create text field
		var popup_text = new Text(__mc, "popup_text", 10, __x + __margins, __y + __margins, __width - 2*__margins)
		popup_text.txt = message;
		popup_text.color = __text_color;
		popup_text.align = "center";
		// create button
		
		var height = 2 * __margins + popup_text.textHeight;
		// create background
		var bg = new Rectangle (__mc, "bg", 0, __width, height, __bg_color);
		bg.mc._x = __x;
		bg.mc._y = __y;		
	}
	
	public function get mc ():MovieClip {
		return(__mc);
	}
	public function remove (){
		removeMovieClip(__mc);
	}
}