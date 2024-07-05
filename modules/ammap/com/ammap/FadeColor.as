import flash.geom.Transform;
import flash.geom.ColorTransform;

class com.ammap.FadeColor {

	private var __target_mc:MovieClip;
	private var __color:Object;
	private var __count:Number;
	private var __total_count:Number;	
	private var __current;		
	private var __interval:Number;
	private var __diff:Object;
	private var __final;
	
	function FadeColor(target_mc:MovieClip){
 	    __target_mc = target_mc;
	}
	
	public function fadeTo(color:Number, time:Number, percent:Number){	
		__diff = new Object();
		
		if(percent == undefined){
			percent = 100;
		}
		percent = percent / 100;
		
		 var transformer:Transform = new Transform(__target_mc);
		 __current = transformer.colorTransform;
		 __color = new Color (__target_mc);
		 __color.setRGB(color);

		 var transformer:Transform = new Transform(__target_mc);
		 var final = transformer.colorTransform;
		 __final = final;
		
		 __diff.ro = (final.redOffset - __current.redOffset) * percent;
		 __diff.go = (final.greenOffset - __current.greenOffset) * percent;
		 __diff.bo = (final.blueOffset - __current.blueOffset) * percent;
		 
		 // return original color
		 transformer.colorTransform = __current;
		
		__total_count = time * 1000 / 20;
		__count = 0;

		
		if(__total_count > 0){
			__interval = setInterval(this, "__changeColor", 20);
		}
		else{
			 var transformer:Transform = new Transform(__target_mc);
			 var colorTransformer:ColorTransform = transformer.colorTransform;
			transformer.colorTransform = final;
		}
	}

	private function __changeColor(){
		if(__count < __total_count){
			__current.redOffset += __diff.ro / __total_count;
			__current.greenOffset += __diff.go / __total_count;
			__current.blueOffset += __diff.bo / __total_count;
			
			 var transformer:Transform = new Transform(__target_mc);
			 var colorTransformer:ColorTransform = transformer.colorTransform;
			transformer.colorTransform = __current;
			__count++;
		}
		else{
			 var transformer:Transform = new Transform(__target_mc);
			 var colorTransformer:ColorTransform = transformer.colorTransform;
			transformer.colorTransform = __final;			
			clearInterval(__interval);
		}
	}
	
	public function stop(){
		clearInterval(__interval);
	}
}