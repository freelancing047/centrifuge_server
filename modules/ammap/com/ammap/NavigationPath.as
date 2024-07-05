import com.ammap.Button;
import com.ammap.Utils;

class com.ammap.NavigationPath {
	
	private var __mc:MovieClip;
	private var __config:Object;
	private var __data:Object;
	private var __count:Number;
    public var addListener:Function;
    public var broadcastMessage:Function;	

	function NavigationPath (target_mc:MovieClip, name:String, depth:Number, config:Object) {
		
		AsBroadcaster.initialize(this);
		
		__mc = target_mc.createEmptyMovieClip(name, depth);
		__config = config;
		__count = 0;
		
		__mc._x = Utils.getCoordinate(__config.navigation_path.x, __config.width);
		__mc._y = Utils.getCoordinate(__config.navigation_path.y, __config.height);

	}	
	
	private function __init(data_source:Object){
		var main_obj = this;
		
		var text = data_source.title;
		
		if(__count > 0 && text != ""){
			text = text + __config.navigation_path.separator;
		}
		
		var path_button = new Button(__mc, "path_button_mc" + __count, __count, text);
		path_button.cornerRadius = 0;
		path_button.borderWidth = 0;
		path_button.borderAlpha = 0;
		path_button.bgAlpha = path_button.bgAlphaHover = path_button.bgAlphaActive = __config.navigation_path.alpha;
		path_button.bgColor = path_button.bgColorHover = path_button.bgColorActive = __config.navigation_path.color;		
		path_button.hPadding = __config.navigation_path.padding;
		path_button.vPadding = 1;		
		path_button.textColor = __config.navigation_path.text_color;
		path_button.font = __config.font;
		path_button.textColorHover = __config.navigation_path.text_color_hover;
		path_button.textSize = __config.navigation_path.text_size;
		path_button.bold = true;
		
		if(__count == 0){
			path_button.textUnderline = true;
		}		
		path_button.show();

		path_button.data_source = data_source;
		
		var listener = new Object();			
		listener.press = function (evtObj) {
			main_obj.broadcastMessage("onClick", evtObj.target.data_source);
		}
		
		path_button.addEventListener ("press", listener);		
		
		// move previous
		if(__count > 0){
			var row = 0;
			for (var i = __count - 1; i > -1; i--){
				var x = __mc["path_button_mc" + (i + 1)]._width + __mc["path_button_mc" + (i + 1)]._x + 1;
				// move to nex tilne if doesn't fit
				if(__mc["path_button_mc" + i]._width + x > __config.width - __mc._x){
					x = 0;
					row ++;
				}
				
				__mc["path_button_mc" + i]._x = x;
				__mc["path_button_mc" + i]._y = row * (__config.navigation_path.text_size + 10);
			}
		}
		
		// check parent
		if(data_source.parent != data_source){
			__count++;
			__init(data_source.parent);
		}
	}

	public function registerClick(data_source:Object){
		// clear current path
		clearPath();
		__count = 0;
		__init(data_source);
	}
	
	public function clearPath(){
		for(var i:String in __mc){
			removeMovieClip(__mc[i]);
		}
	}
	
	public function get mc (){
		return __mc;
	}
}