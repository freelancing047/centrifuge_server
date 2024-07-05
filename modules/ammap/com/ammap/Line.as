class com.ammap.Line {
	
	private var __mc:MovieClip;

	function Line (target_mc:MovieClip, name:String, depth:Number, x:Array, y:Array, width:Number, color:Number, alpha:Number) {
		
		if(alpha == undefined){
			alpha = 100;
		}
		
		__mc = target_mc.createEmptyMovieClip (name, depth);				
		
		__mc.lineStyle(width, color, alpha, true, "none");	
		
		__mc.moveTo(x[0], y[0]);
			
		for (var i = 1; i < x.length; i++) {				
			__mc.lineTo(x[i], y[i]);
		}		
	}	
	
	public function get mc (){
		return __mc;
	}
}