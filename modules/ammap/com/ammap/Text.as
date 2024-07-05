class com.ammap.Text {
	
	private var __text:TextField;
	private var __format:TextFormat;
	
	// default TextFormat  params
	private var __font = "Arial";
	private var __size = 11;
	private var __color = 0x000000;	
	private var __align = "left";		
	
	// default textField params	
	private var __selectable = false;
	private var __multiline = true;	
	private var __wrap = true;	
	private var __embed_fonts = false;		
	
	public function Text(target_mc:MovieClip, name:String, depth:Number, x:Number, y:Number, width:Number, height:Number, autosize:String, config:Object){

		__font = config.font || _global.font || __font;
		__size = config.text_size || __size;
		__color = config.text_color || __color;
		
		//create text field
		target_mc.createTextField (name, depth, x, y, width, height);

		// createTextField doesn't return textField if flash player is <8, assigning manually
		__text = target_mc[name];
		
		if((height == undefined or width == undefined or height == 0 or width == 0) and autosize == undefined){			
			autosize = "left";
		}
		
		if(width == 0 or width == undefined or isNaN(width) == true) {
			__wrap = false;
		}
		__text.multiline = __multiline;
		__text.autoSize = autosize;
		__text.selectable = __selectable;	
		__text.wordWrap = __wrap;
		__text.border = false;
		__text.embedFonts = __embed_fonts;		
		
		__format = new TextFormat();
		__format.font = __font;
		__format.size = __size;
		__format.color = __color;		
	}
	
	/////////////////////////////////////////////
	/// SETTERS /////////////////////////////////
	/////////////////////////////////////////////
	
	/// SET TEXT ////////////////////////////////
	
	public function set txt (param){
		__text.text = param;
		__text.setTextFormat(__format);
	}
	
	public function set htmlTxt (param){
		__text.html = true;
		param = "<font color='#"+__color.toString(16)+"' size='"+__size+"' face='"+__font+"'>" + param + "</font>";
		__text.htmlText = param;
		
		//__text.setTextFormat(__format);
	}	
	
	/// TEXT FORMAT /////////////////////////////
	/// size ////////////////////////////////////
	public function set size (param:Number){
		__format.size = param || __size;
		__size = param || size;
		__text.setTextFormat(__format);		
	}
	
	/// bold ////////////////////////////////////
	public function set bold (param:Boolean){
		__format.bold = param;
		__text.setTextFormat(__format);
	}
	
	/// underline ////////////////////////////////////
	public function set underline (param:Boolean){
		__format.underline = param;
		__text.setTextFormat(__format);
	}
	
	/// color ///////////////////////////////////
	public function set color (param:Number){
		if(param != undefined and isNaN(param) == false){
			__format.color = param;
			__color = param;
		}
		else{
			__format.color = __color;
		}
		__text.setTextFormat(__format);
	}
	
	/// color ///////////////////////////////////
	public function set font (param:String){
		if(param != undefined) {
			__format.font = param;
			__font = param;
			__text.setTextFormat(__format);
		}
	}	

	/// TEXT FIELD //////////////////////////////
	
	/// x ///////////////////////////////////////
	public function set x (param:Number){
		__text._x = param;
	}
	/// y ///////////////////////////////////////
	public function set y (param:Number){
		__text._y = param;
	}
	
	/// text height /////////////////////////////
	public function set textHeight (param:Number){
		__text.textHeight = param;
	}
	
	public function set wrap (param:Boolean){
		__text.wordWrap = param;
	}	

	public function set multiline (param:Boolean){
		__text.multiline = param;
	}			
	/// align ///////////////////////////////////
	public function set align (param:String){
		var format = new TextFormat()
		format.align = param;
		__text.setTextFormat(format);
	}	
	/// text width //////////////////////////////
	public function set textWidth (param:Number){
		__text.textWidth = param;
	}	
	/// field width //////////////////////////////
	public function set width (param:Number){
		__text._width = param;
	}
	
	/// selectable //////////////////////////////
	public function set selectable (param:Boolean){
		__text.selectable = param;
	}
	
	/// selectable //////////////////////////////
	public function set autoSize (param){
		__text.autoSize = param;
	}	
	
	/// x ///////////////////////////////////////	
	public function get x ():Number{
		return(__text._x);
	}
	/// y ////////////////////////
	public function get y ():Number{
		return(__text._y);
	}	
	/// text height //////////////
	public function get textHeight ():Number{
		return(__text.textHeight);
	}
	// width
	public function get width ():Number{
		return(__text._width);
	}
	// height
	public function get height ():Number{
		__text._height;
		return(__text._height);
	}
	/// text width //////////////
	public function get textWidth ():Number{
		return(__text.textWidth);
	}	
	public function get field ():TextField{
		return(__text);
	}
}
