import com.ammap.Line;

class com.ammap.DashedLine {
	
	private var __mc:MovieClip;
	
	function DashedLine (target_mc:MovieClip, name:String, depth:Number, x:Array, y:Array, width:Number, dash_length:Number, color:Number){
		
		__mc = target_mc.createEmptyMovieClip (name, depth);				
		
		for(var i = 1; i < x.length; i++){
			
			var dashed_line_mc = __mc.createEmptyMovieClip ("dashedLine", i);

			dashed_line_mc._x = x[i - 1];
			dashed_line_mc._y = y[i - 1];			
			
			var dashCount = Math.sqrt(Math.pow(x[i] - x[i - 1], 2) + Math.pow(y[i] - y[i - 1], 2)) / dash_length;
			
			var xstep = (x[i] - x[i - 1]) / dashCount;
			var ystep = (y[i] - y[i - 1]) / dashCount;	
			
			for(var j:Number = 1; j < dashCount; j = j + 2){
				var dashed_line = new Line(dashed_line_mc, "line" + j, j, [(j - 1) * xstep, j * xstep], [(j - 1) * ystep, j * ystep], width, color);
			}
		}
	}
	
	public function get mc (){
		return __mc;
	}
}