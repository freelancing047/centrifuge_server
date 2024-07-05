class com.ammap.Rectangle {
	private var __mc:MovieClip;
	private var __target_mc:MovieClip;
	private var __name:String;
	private var __depth:Number;
	private var __width:Number;
	private var __height:Number;
	private var __bg_color:Number;
	private var __border_width:Number;
	private var __border_color:Number;
	private var __corner_radius:Number = 0;
	private var __bg_alpha:Number = 100;
	private var __border_alpha:Number = 100;	
	
	function Rectangle (target_mc:MovieClip,
						name:String,
						depth:Number, 
						width:Number,
						height:Number, 
						bg_color:Number,
						border_width:Number,
						border_color:Number,
						corner_radius:Number,
						bg_alpha:Number,
						border_alpha:Number) {
		
		__target_mc		= target_mc;
		__name			= name;
		__depth 		= depth;
		__width 		= width;
		__height 		= height;
		__bg_color 		= bg_color;
		__border_width 	= border_width;
		__border_color 	= border_color;
		__corner_radius	 = corner_radius		!== undefined ? corner_radius 	: __corner_radius;
		__bg_alpha		 = bg_alpha				!== undefined ? bg_alpha 		: __bg_alpha;
		__border_alpha	 = border_alpha 		!== undefined ? border_alpha 	: __border_alpha;		

		__show();
	}
	
	function get mc () {
		return __mc;
	}
	
	private function __show () {
		// preset
		var w = __width;
		var h = __height;
		var x = 0;
		var y = 0;
		
		// set radius
		var r = __corner_radius;
		
		// recalculate coordinates to take border width into account
		if (__border_width > 1) {
			var bw:Number = Math.ceil(__border_width / 2);
			x = x + bw;
			y = y + bw;
			w = w - __border_width;
			h = h - __border_width;
		}

		if(__border_width == 1){
			w = w - 1;
			h = h - 1;
		}
		
		// create object
		__mc = __target_mc.createEmptyMovieClip(__name, __depth);
		
		// draw border
		if (__border_width == undefined || __border_width == null) {
			__mc.lineStyle(0, __bg_color, __border_alpha);
		}
		else {
			__mc.lineStyle(__border_width, __border_color, __border_alpha);
		}
		
		if (__bg_color != undefined || __bg_color != null) {
			__mc.beginFill(this.__bg_color, __bg_alpha);
		}
		__mc.moveTo(x + r, y);
		__mc.lineTo(w + x - r, y);
		if (r > 0) {
			__mc.curveTo(w + x, y, w + x, y + r);
		}
		__mc.lineTo(w + x, h + y - r);
		if (r > 0) {
			__mc.curveTo(w + x, h + y, w + x - r, h + y);
		}
		__mc.lineTo(x + r, h + y);
		if (r > 0) {
			__mc.curveTo(x, h + y, x, h + y - r);
		}
		__mc.lineTo(x, y + r);
		if (r > 0) {
			__mc.curveTo(x, y, x + r, y);
		}
		
		if (__bg_color != undefined || __bg_color != null) {
			__mc.endFill();
		}
	}
}