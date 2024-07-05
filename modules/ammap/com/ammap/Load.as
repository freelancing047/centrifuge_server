import com.ammap.Preloader;
import com.ammap.Utils;
class com.ammap.Load {
	
	private var __preloader:Preloader;
	private var __loader;

	/// load vars ////////////////////////////////////
	public function loadVars (file_name:String, obj:Object, funct:String, time_stamp:Boolean, error_funct:String){
		
		file_name = Utils.validateFileName(file_name);
		
		var main_obj:Object = this;

		__loader = new LoadVars();

		__loader.onData = function(variable){
			
			main_obj.__preloader.remove();
			
			if(variable == undefined){				
				obj[error_funct]("Error loading file: " + file_name);
			}
			else {
				obj[funct](variable);
			}
		}
		
		__loader.load(file_name + __timeStamp(time_stamp));
	}
	
	/// load xml ////////////////////////////////////
	public function loadXML (file_name:String, obj:Object, funct:String, time_stamp:Boolean, error_funct:String){
		
		file_name = Utils.validateFileName(file_name);
		
		var main_obj:Object = this;

		__loader = new XML();
		__loader.ignoreWhite = true;
		
		__loader.onLoad = function(success:Boolean){

			main_obj.__preloader.remove();
			
			if(success == false){
				obj[error_funct]("Error loading file: " + file_name);
			}
			else{
				obj[funct](main_obj.__loader);
			}			
		}					
		__loader.load(file_name + __timeStamp(time_stamp));
	}	
	
	/// load swf, jpg, png, gif //////////////////////
	/// gif, jpg and progressive jpg only from fp 8 //
	
	public function loadClip (file_name:String, target_mc:MovieClip, obj:Object, funct:String, time_stamp:Boolean, error_funct:String, param:Array, smoothing:Boolean){
		
		file_name = Utils.validateFileName(file_name);
	
		var main_obj:Object = this;
		
		__loader = target_mc;
		
		var mcloader:MovieClipLoader = new MovieClipLoader();
		var listener:Object = new Object();

		listener.onLoadError = function(target_mc:MovieClip, errorCode:String, httpStatus:Number) {
			main_obj.__preloader.remove();
			obj[error_funct]("Error loading file: " + file_name);
		}
		
		listener.onLoadInit = function (){
			main_obj.__preloader.remove();
			obj[funct](target_mc, param);
			target_mc.forceSmoothing = smoothing;
		}
		
		mcloader.addListener(listener);
		mcloader.loadClip(file_name + __timeStamp(time_stamp), __loader);
	}	
	
	
	public function loadClip2 (file_name:String, target_mc:MovieClip, obj:Object, funct:String, time_stamp:Boolean, error_funct:String, param:Array, on_init:String){
		
		file_name = Utils.validateFileName(file_name);
	
		var main_obj:Object = this;
		
		__loader = target_mc;
		
		var mcloader:MovieClipLoader = new MovieClipLoader();
		var listener:Object = new Object();

		listener.onLoadError = function(target_mc:MovieClip, errorCode:String, httpStatus:Number) {
			main_obj.__preloader.remove();
			obj[error_funct]("Error loading file: " + file_name);
		}
		
		listener.onLoadComplete = function (){
			main_obj.__preloader.remove();
			obj[funct](target_mc, param);
		}
		
		listener.onLoadInit = function (){
			obj[on_init](target_mc, param);
		}		
		
		mcloader.addListener(listener);
		mcloader.loadClip(file_name + __timeStamp(time_stamp), __loader);
	}		
	
	
	/// preloader ////////////////////////////////////
	public function preloader(target_mc:MovieClip,								  
								  name:String,
								  depth:Number,
								  x:Number,
								  y:Number,
								  width:Number,
								  height:Number,
								  color:Number,
								  bgColor:Number,
								  text:String
								  ){
		
		__preloader = new Preloader (target_mc, name, depth, __loader, width, height, color, bgColor, text);
		__preloader.mc._x = x;
		__preloader.mc._y = y;
	}
	
	/// time_stamp //////////////////////////////////
	private function __timeStamp(param:Boolean):String{
		if(param == true and _url.substr(0,4).toLowerCase() == "http"){
			return("?"+getTimer()+""+random(Number.MAX_VALUE)+""+random(Number.MAX_VALUE));
		}
		else {
			return("");
		}		
	}
}