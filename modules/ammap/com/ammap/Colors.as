import flash.geom.Transform;
import flash.geom.ColorTransform;

class com.ammap.Colors {

	public static function getIntermediateColor(start_color:Number, end_color, percent:Number):Number{
		
		if(isNaN(percent) == true){
			percent = 0;
		}
	
		var diff = new Object();
		var start_tf:ColorTransform = new ColorTransform();
		start_tf.rgb = start_color;
		
		var end_tf:ColorTransform = new ColorTransform();
		end_tf.rgb = end_color;		  
		  
		start_tf.redOffset 		+= (end_tf.redOffset - start_tf.redOffset) * percent / 100;
		start_tf.greenOffset 	+= (end_tf.greenOffset - start_tf.greenOffset) * percent / 100;
		start_tf.blueOffset 	+= (end_tf.blueOffset - start_tf.blueOffset) * percent / 100;
		
		return(start_tf.rgb);
	}
	
	public static function setBrightness (target_mc:MovieClip, value:Number){

		var brightness:Color = new Color (target_mc);
		var percent = 100 - Math.abs(value);
		var offset = 0;
		if (value > 0) offset = 256 * (value / 100);
		var trans = new Object();
		trans.ra = trans.ga = trans.ba = percent;
		trans.rb = trans.gb = trans.bb = offset;
		brightness.setTransform(trans);		
	}	
}