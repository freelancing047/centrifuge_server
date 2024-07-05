class com.ammap.Shape {
	private var __mc:MovieClip;
	private var __x:Array;
	private var __y:Array;
	private var __bg_color:Number = 0x000000;
	private var __border_width:Number = 0;
	private var __border_color:Number;
	private var __bg_alpha:Number = 100;
	private var __border_alpha:Number = 100; 	
	
	function Shape (target_mc:MovieClip,
						name:String,
						depth:Number, 
						x:Array,
						y:Array, 
						bg_color:Number,
						border_width:Number,
						border_color:Number,
						bg_alpha:Number,
						border_alpha:Number) {
		
		__x 			= x;
		__y 			= y;
		__bg_color 		= bg_color			||	__bg_color;
		__border_width 	= border_width 		||  __border_width;
		__border_color 	= border_color		!== undefined ? border_color 	: __bg_color;
		__bg_alpha		= bg_alpha			!== undefined ? bg_alpha 		: __bg_alpha;
		__border_alpha	= border_alpha 		!== undefined ? border_alpha 	: __border_alpha;		
		
		// create object
		__mc = target_mc.createEmptyMovieClip(name, depth);	
		
		__init();
	}
	
	function get mc () {
		return __mc;
	}
	
	private function __init () {

		if (__border_width == undefined || __border_width == null) {
			__mc.lineStyle(0, __bg_color, __border_alpha);
		}
		else {
			__mc.lineStyle(__border_width, __border_color, __border_alpha);
		}
		
		if (__bg_color != undefined || __bg_color != null) {
			__mc.beginFill(this.__bg_color, __bg_alpha);
		}
		__mc.moveTo(__x[0], __y[0]);
		
		for (var i = 1; i < __x.length; i++) {
			__mc.lineTo(__x[i], __y[i]);
		}
		
		if (__bg_color != undefined || __bg_color != null) {
			__mc.endFill();
		}
	}
}