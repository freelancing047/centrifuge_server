class com.ammap.Bezier {

	private var __mc:MovieClip;

	function Bezier (target_mc:MovieClip, name:String, depth:Number, x:Array, y:Array, width:Number, color:Number, alpha:Number, fill_alpha) {
		
		if(alpha == undefined){
			alpha = 100;
		}	
		
		__mc = target_mc.createEmptyMovieClip (name, depth);
		if(fill_alpha >0){
			__mc.beginFill(color, fill_alpha);
		}
		
		__mc.lineStyle(width, color, alpha, true, "none");
		__mc.moveTo(x[0], y[0]);
		
		var points = new Array();
		for(var i = 0; i < x.length; i++){
			points.push({x:x[i], y:y[i]});
		}
		
		var interpolated_points = __interpolate(points);
		__drawBeziers (__mc, interpolated_points);		
	}
	
	private function __interpolate(points:Array):Array{
		var interpolated_points:Array = [];
		interpolated_points.push({x:points[0].x, y:points[0].y});

		var slope_x = points[1].x - points[0].x;
		var slope_y = points[1].y - points[0].y;		

		interpolated_points.push({x:points[0].x + slope_x/6, y:points[0].y + slope_y/6});
		
		for(var i = 1; i < points.length - 1; i++)
		{
			var point1 = points[i - 1];
			var point2 = points[i];
			var point3 = points[i + 1];
			
			slope_x = point3.x - point1.x;
			slope_y = point3.y - point1.y;

			interpolated_points.push({x:point2.x - slope_x/6, y:point2.y - slope_y/6}); 
			interpolated_points.push({x:point2.x, y:point2.y}); 
			interpolated_points.push({x:point2.x + slope_x/6, y:point2.y + slope_y/6});
		}
		
		slope_y = points[points.length - 1].y - points[points.length - 2].y;
		slope_x = points[points.length - 1].x - points[points.length - 2].x;
		
		interpolated_points.push({x:points[points.length - 1].x - slope_x/6, y:points[points.length - 1].y - slope_y/6});
		interpolated_points.push({x:points[points.length - 1].x, y:points[points.length - 1].y});
		
		return interpolated_points;
	}
	
		
	private function __drawBeziers(mc, interpolated_points){
		for(var j = 0; j < (interpolated_points.length - 1)/3; j++){			
			__drawBezierMidpoint(mc, interpolated_points[3*j],interpolated_points[3*j+1],interpolated_points[3*j+2],interpolated_points[3*j+3]); 
		}
	}
	
	private function __drawBezierMidpoint(mc, P0, P1, P2, P3){
		// calculates the useful base points
		var PA = __getPointOnSegment(P0, P1, 3/4);
		var PB = __getPointOnSegment(P3, P2, 3/4);
		
		// get 1/16 of the [P3, P0] segment
		var dx = (P3.x - P0.x)/16;
		var dy = (P3.y - P0.y)/16;
		
		// calculates control point 1
		var Pc_1 = __getPointOnSegment(P0, P1, 3/8);
		
		// calculates control point 2
		var Pc_2 = __getPointOnSegment(PA, PB, 3/8);
		Pc_2.x -= dx;
		Pc_2.y -= dy;
		
		// calculates control point 3
		var Pc_3 = __getPointOnSegment(PB, PA, 3/8);
		Pc_3.x += dx;
		Pc_3.y += dy;
		
		// calculates control point 4
		var Pc_4 = __getPointOnSegment(P3, P2, 3/8);
		
		// calculates the 3 anchor points
		var Pa_1 = __getMiddle(Pc_1, Pc_2);
		var Pa_2 = __getMiddle(PA, PB);
		var Pa_3 = __getMiddle(Pc_3, Pc_4);
	
		// draw the four quadratic subsegments		
		mc.curveTo(Pc_1.x, Pc_1.y, Pa_1.x, Pa_1.y);
		mc.curveTo(Pc_2.x, Pc_2.y, Pa_2.x, Pa_2.y);
		mc.curveTo(Pc_3.x, Pc_3.y, Pa_3.x, Pa_3.y);
		mc.curveTo(Pc_4.x, Pc_4.y, P3.x, P3.y);
	}
	
	private function __getPointOnSegment(P0, P1, ratio){
		return {x: (P0.x + ((P1.x - P0.x) * ratio)), y: (P0.y + ((P1.y - P0.y) * ratio))};
	}
	
	private function __getMiddle(P0, P1){
		return {x: ((P0.x + P1.x) / 2), y: ((P0.y + P1.y) / 2)};
	}
	
	public function get mc(){
		return(__mc);
	}
}