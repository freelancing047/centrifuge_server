import com.ammap.Text;
import com.ammap.Rectangle;
import com.ammap.Utils;

class com.ammap.Legend {
	
	private var __mc:MovieClip;	
	private var __entries_mc:MovieClip;		

	private var __config:Object;
	
	private var __width:Number;
	private var __height:Number;
	
	private var __spacing:Number;
	private var __margins:Number;	

	private var __key_size:Number;	
	private var __max_column_width:Number = 0;

	private var __text_width:Number;
	private var __total_entries:Number;
	private var __legend_entry_mc:Array;
	
	function Legend (target_mc:MovieClip, name:String, depth:Number, config:Object) {		
		// main mc
		__mc = target_mc.createEmptyMovieClip(name, depth);	
		__config = config;
		
		__legend_entry_mc = new Array();
		
		__mc._x = Utils.getCoordinate(__config.legend.x, __config.width);
		__mc._y = Utils.getCoordinate(__config.legend.y, __config.height);		
		__width = Utils.getCoordinate(__config.legend.width, __config.width);
		
		__spacing = __config.legend.spacing;
		__margins = __config.legend.margins;				
		__key_size = __config.legend.key.size;
		
		__text_width = __width - 2 * __margins - __key_size - __spacing;
		
		__init();
	}
		
	/////////////////////////////////////////////////////////
	/// INIT ////////////////////////////////////////////////
	/////////////////////////////////////////////////////////
	private function __init(){
		__total_entries = 0;
		
		__entries_mc = __mc.createEmptyMovieClip("entries_mc", 10);
		
		for(var i = 0; i < __config.legend.entries.entry.length; i++) {
			__total_entries++;
			// legend entry holder
			__legend_entry_mc[i] = __entries_mc.createEmptyMovieClip ("legendEntry_mc"+i, i + 100);			
			// key
			if(__config.legend.key.border_color != undefined){
				var border_alpha = 100;
			}
			else{
				var border_alpha = 0;
			}
			
			var entry_key = new Rectangle (__legend_entry_mc[i], "rectangle", 0, __key_size, __key_size, __config.legend.entries.entry[i].color, 0, __config.legend.key.border_color, 0, 100, border_alpha);
			// text			
			var entry_text = new Text (__legend_entry_mc[i], "text", 1, __key_size + __spacing, 0, __text_width, __height, "left", __config);

			entry_text.size = __config.legend.text_size;
			entry_text.color = __config.legend.text_color;
			entry_text.htmlTxt = __config.legend.entries.entry[i].title;
			
			var text_width = entry_text.textWidth;
			
			if(text_width == undefined){
				text_width = 0;
			}
			
			if(entry_text.width > text_width){
				entry_text.width = text_width + 5;
			}
			else{
				text_width = entry_text.width;
			}				
			
			// calculate max width of a column
			if(text_width + __key_size + 2 * __spacing + 10 > __max_column_width){
				__max_column_width = text_width + __key_size + 2 * __spacing + 10;
			}
		}
		__arrange();
	}
		
	//////////////////////////////////////////////////////
	// arrange ///////////////////////////////////////////
	//////////////////////////////////////////////////////
	private function __arrange (){
		
		var max_row_height		 = 0;
		var previous_row_height	 = 0;		
		var previous_row_y		 = __margins;
		var lwidth = __width;
		if(lwidth == undefined){
			lwidth = __config.width - 2 * __margins;
		}
		
		var column_count			 = Math.floor((lwidth - __margins * 2 + __spacing)/__max_column_width);

		var current_column		 = 1;
		
		if(column_count > __total_entries){
			column_count = __total_entries;
		}		
		
		if(__config.legend.max_columns > 0 && column_count > __config.legend.max_columns){
			column_count = __config.legend.max_columns;
		}
		

		
		
		for(var i = 0; i < __config.legend.entries.entry.length; i++){
			//get row height
			if(__legend_entry_mc[i]._height > max_row_height){
				max_row_height = __legend_entry_mc[i]._height;
			}
			__legend_entry_mc[i]._x = __margins + __max_column_width * (current_column - 1);			
			__legend_entry_mc[i]._y = previous_row_y + previous_row_height;
					
			current_column++;
				
			if(current_column > column_count){
				if(i == 0){
					previous_row_y = __margins;
				}
				else {
					previous_row_y = __legend_entry_mc[i]._y;
				}

				previous_row_height = max_row_height + __spacing;					
					
				current_column = 1;
				max_row_height = 0;
			}
			var last_entry = __legend_entry_mc[i];
		}
		__height = last_entry._y + previous_row_height + __margins - __spacing - 1;

		__drawBg();		

	}
	
	///////////////////////////////////////////////////
	// draw background ////////////////////////////////
	///////////////////////////////////////////////////
	private function __drawBg(){
		if(__width == undefined){
			__width = __mc._width + __config.legend.margins * 2;
		}
		var bg = new Rectangle(__mc, "bg_mc", 0, __width, __height, __config.legend.color, 0, __config.legend.border_color, 0, __config.legend.alpha, __config.legend.border_alpha);
	}
	
	public function get mc(){
		return (__mc);
	}
}
