import com.ammap.Text;
import com.ammap.Utils;
import com.ammap.Rectangle;

class com.ammap.Labels {

	private var __scene_labels_mc:MovieClip;
	private var __map_labels_mc:MovieClip;
	private var __map_mc:MovieClip;
	private var __data:Object;
	private var __label_data:Object;	
	private var __config:Object;
	private var __labels_to_resize:Array;
	private var __bg_to_resize:Array;
    public var addListener:Function;
    public var broadcastMessage:Function;
	

	function Labels(target_mc:MovieClip, map_mc:MovieClip, name:String, depth:Number, label_data:Object, config:Object, data:Object) {
		
		AsBroadcaster.initialize(this);
		
		__scene_labels_mc = target_mc.createEmptyMovieClip(name, depth);		
		__map_labels_mc = map_mc.createEmptyMovieClip("labels_mc", depth); //3
		
		__map_mc = map_mc;
		__data = data;
		__label_data = label_data;		
		__config = config;
		__labels_to_resize = new Array();
		__bg_to_resize = new Array();
		__init(false);
	}

	private function __init(upper_levels:Boolean) {

		var main_obj = this;
	
	
		for (var i = 0; i<__label_data.labels.label.length; i++) {
			
			if(upper_levels == true && __label_data.labels.label[i].remain == false){
				// do nothing
			}
			else{			
				if (__label_data.labels.label[i].text != undefined) {				

					
					var label_data = __label_data.labels.label[i];
				
					// if map x and y is not set, load movies in movies_mc
					if(isNaN(label_data.lat) && isNaN(label_data.long)){
						var target_mc = __scene_labels_mc;
						var x = Utils.getCoordinate(label_data.x, __config.width);
						var y = Utils.getCoordinate(label_data.y, __config.height);
					}
					// if map x and y is set, load movies in map_mc
					else{
						var target_mc = __map_labels_mc;
						var x = Utils.longitudeToPixels(__config.projection, label_data.long, __data.tl_long, __data.br_long, __config.map_width);
						var y = Utils.latitudeToPixels(__config.projection, label_data.lat, __data.tl_lat, __data.br_lat, __config.map_height);
					}
					
					var depth = target_mc.getNextHighestDepth();				
					var label_container_mc = target_mc.createEmptyMovieClip("label_container_mc" + depth, depth);
					var label_mc = label_container_mc.createEmptyMovieClip("label_mc", 2);
					label_container_mc._x = x;
					label_container_mc._y = y;					
					
					var width = Utils.getCoordinate(label_data.width, __config.width);
	
					if (isNaN(width) == true) {
						var width = __config.width - x;
					}					
					
					var label = new Text(label_mc, "label", 0, 0, 0, width, __config.height, "left", __config);	

					label.size = label_data.text_size;
					label.color = label_data.color;					
					label.htmlTxt = label_data.text;
					label.align = label_data.align;					
					
					label_mc.data_source = label_data;
					
					if(label.textWidth < label.width && label_data.width == undefined){
						label.width = label.textWidth + 5;
					}
					
					// rotate
					if (label_data.rotate == true) {
						var label = Utils.rotateText(label_mc["label"], __config.background.color, label_mc, "label", 1);
					}

					// draw bg
					if(label_data.bg_color != undefined && label_data.bg_alpha != 0){						
						var bg = new Rectangle(label_container_mc, "bg_mc", 1, label.field._width + __config.label.bg_margins_horizontal, label.field._height + __config.label.bg_margins_vertical, label_data.bg_color, 0,0,0,label_data.bg_alpha,0);
						label.x = __config.label.bg_margins_horizontal / 2;
						label.y = __config.label.bg_margins_vertical / 2;						
					}
					
					// add to array for resizing
					if(target_mc._parent == __map_mc && label_data.fixed_size == true){
						__labels_to_resize.push(label);
						__bg_to_resize.push(bg.mc);						
					}					
					
					label_mc.onRollOver = function(){
						main_obj.broadcastMessage("onRollOver", this.data_source);							
						
						if(this.data_source.color_hover != undefined){	
							var color = new Color(this);
							color.setRGB(this.data_source.color_hover);
						}
						if(Utils.checkIfClickable(this.data_source) != true){
							this.useHandCursor = false;
						}
					}
					
					label_mc.onRollOut = label_mc.onReleaseOutside = function(){
						main_obj.broadcastMessage("onRollOut", this.data_source);
						if(this.data_source.color_hover != undefined){
							var color = new Color(this);
							color.setRGB(this.data_source.color);
						}
					}
					
					if(Utils.checkIfClickable(label_data) == true){
						label_mc.onRelease = function(){
							_global.ammap_kill_click = true;
							main_obj.broadcastMessage("onGetURL", this.data_source);
						}
					}
				}
			}
		}
		
		// check parent
		if(__label_data.parent != __label_data){
			__label_data = __label_data.parent;
			__init(true);			
		}
	}
	
	
	
	
	public function resize(scale){
		// labels
		for(var i = 0; i < __labels_to_resize.length; i++){
			__labels_to_resize[i].field._xscale = scale;
			__labels_to_resize[i].field._yscale = scale;
			

			
			if(__bg_to_resize[i] != undefined){			
				__labels_to_resize[i].x = __config.label.bg_margins_horizontal / 2 * scale / 100;
				__labels_to_resize[i].y = __config.label.bg_margins_vertical / 2 * scale / 100;				
				__bg_to_resize[i]._width = __labels_to_resize[i].field._width + __config.label.bg_margins_horizontal * scale / 100;
				__bg_to_resize[i]._height = __labels_to_resize[i].field._height + __config.label.bg_margins_vertical * scale / 100;
			}
		}
	}
}