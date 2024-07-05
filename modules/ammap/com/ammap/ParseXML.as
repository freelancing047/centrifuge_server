import com.ammap.Utils;
import com.ammap.XML2Object;
import com.ammap.Colors;

class com.ammap.ParseXML {
	
	private var __data:Object;
	private var __config:Object;
	private var __xml:XML;
	private var __parsed_xml:Object;
	
	function ParseXML(xml:XML, config:Object){
		__config = config;
		__xml = xml;
		__parseXML();
	}

	private function __parseXML (){
		__data = new Object();
		// parse xml with default parser
		__parsed_xml = new XML2Object().parseXML(__xml).map;
		
		__data.map_file = __parsed_xml.attributes.map_file;
		__data.zoom_x = Utils.checkUndefined(__parsed_xml.attributes.zoom_x);
		__data.zoom_y = Utils.checkUndefined(__parsed_xml.attributes.zoom_y);
		__data.lat = __parsed_xml.attributes.lat;
		__data.long = __parsed_xml.attributes.long;		
		
		if(__data.lat == undefined && __data.zoom_y == undefined){
			__data.zoom_y = "0%";
		}
		if(__data.long == undefined && __data.zoom_x == undefined){
			__data.zoom_x = "0%";
		}		
		
		__data.zoom = Utils.toNumber(Utils.stripSymbols(__parsed_xml.attributes.zoom, "%"));
		__data.parent = __data;
		__data.oid = __parsed_xml.attributes.oid;
		__data.url = __parsed_xml.attributes.url;
		
		__data.tl_long = Utils.toNumber(__parsed_xml.attributes.tl_long, -180);
		__data.tl_lat = Utils.toNumber(__parsed_xml.attributes.tl_lat, 90);
		__data.br_long = Utils.toNumber(__parsed_xml.attributes.br_long, 180);
		__data.br_lat = Utils.toNumber(__parsed_xml.attributes.br_lat, -90);

		__data.by_id = new Object();
		
		__parseAreas(__parsed_xml.areas.area, __data);
		__parseLabels(__parsed_xml.labels.label, __data);
		__parseLines(__parsed_xml.lines.line, __data);
		__parseMovies(__parsed_xml.movies.movie, __data);
	}
	
	// PARSE AREAS
	private function __parseAreas(obj:Object, pobj:Object){
		// if there is only one area, convert object to array
		if (typeof(obj) == "object" && Utils.isArray(obj) == false){
			obj = Utils.objectToArray(obj);
		}
		
		pobj.areas = new Object();
		pobj.areas.area =  new Array();
		
		var sum = 0;
		
		for (var i = 0; i < obj.length; i++){
			pobj.areas.area[i] = new Object();
			pobj.areas.area[i].type = "area";						
			pobj.areas.area[i].mc_name = obj[i].attributes.mc_name;
			pobj.areas.area[i].parent = pobj;
			pobj.areas.area[i].oid = obj[i].attributes.oid;
			pobj.areas.area[i].link_with = obj[i].attributes.link_with.split(",");						
			pobj.areas.area[i].color = Utils.toColor(obj[i].attributes.color);
			pobj.areas.area[i].color_hover = Utils.toColor(obj[i].attributes.color_hover, __config.area.color_hover);
			pobj.areas.area[i].alpha = Utils.toNumber(obj[i].attributes.alpha, 100);
			pobj.areas.area[i].title = Utils.checkUndefined(obj[i].attributes.title, "");
			pobj.areas.area[i].url = obj[i].attributes.url;
			pobj.areas.area[i].target = Utils.checkUndefined(obj[i].attributes.target, "");
			pobj.areas.area[i].description = Utils.checkUndefined(obj[i].description.data, "");
			pobj.areas.area[i].value = Utils.toNumber(obj[i].attributes.value);
			pobj.areas.area[i].zoom_x = obj[i].attributes.zoom_x;
			pobj.areas.area[i].zoom_y = obj[i].attributes.zoom_y;
			pobj.areas.area[i].lat = obj[i].attributes.lat;
			pobj.areas.area[i].long = obj[i].attributes.long;			
			pobj.areas.area[i].zoom = Utils.toNumber(Utils.stripSymbols(obj[i].attributes.zoom, "%"));			
			pobj.areas.area[i].text_box = Utils.parseTrue(obj[i].attributes.text_box, __config.text_box.enabled);
			pobj.areas.area[i].text_box_x = Utils.checkUndefined(obj[i].attributes.text_box_x, __config.text_box.x);
			pobj.areas.area[i].text_box_y = Utils.checkUndefined(obj[i].attributes.text_box_y, __config.text_box.y);
			pobj.areas.area[i].text_box_width = Utils.checkUndefined(obj[i].attributes.text_box_width, __config.text_box.width);
			pobj.areas.area[i].text_box_height = Utils.checkUndefined(obj[i].attributes.text_box_height, __config.text_box.height);
			pobj.areas.area[i].object_list = Utils.parseTrue(obj[i].attributes.object_list, true);
			
			if(__config.area.active_only_if_value_set == true && isNaN(Number(pobj.areas.area[i].value)) == true){
				var show_balloon = false;
			}
			else{
				var show_balloon = true;
			}
			
			pobj.areas.area[i].balloon = Utils.parseTrue(obj[i].attributes.balloon, show_balloon);
			
			if(isNaN(pobj.areas.area[i].value) == false){
				sum += pobj.areas.area[i].value;
			}

			__data.by_id[obj[i].attributes.oid] = pobj.areas.area[i];
			
			// check for children
			if(typeof(obj[i].movies.movie) == "object"){
				__parseMovies(obj[i].movies.movie, pobj.areas.area[i]);
			}
			if(typeof(obj[i].labels.label) == "object"){
				__parseLabels(obj[i].labels.label, pobj.areas.area[i]);
			}
			if(typeof(obj[i].lines.line) == "object"){
				__parseLines(obj[i].lines.line, pobj.areas.area[i]);
			}
		}		
		// calculate percents
		var max_percent = 0;
		for (var i = 0; i < pobj.areas.area.length; i++){
			if(isNaN(pobj.areas.area[i].value) == false){
				pobj.areas.area[i].percent = pobj.areas.area[i].value / sum * 100;
				if(pobj.areas.area[i].percent > max_percent){
					max_percent = pobj.areas.area[i].percent;
				}
			}
			else{
				pobj.areas.area[i].percent = 0;
			}
		}
		__config.max_percent = max_percent;		
		// set colors
		for (var i = 0; i < pobj.areas.area.length; i++){
			if(pobj.areas.area[i].color == undefined){
				pobj.areas.area[i].color = Colors.getIntermediateColor(__config.area.color_light, __config.area.color_solid, pobj.areas.area[i].percent / max_percent * 100);
			}
			pobj.areas.area[i].color_selected = Utils.toColor(obj[i].attributes.color_selected, Utils.checkUndefined(__config.area.color_selected, pobj.areas.area[i].color));			
		}
	}	
	
	// PARSE LABELS
	private function __parseLabels(obj:Object, pobj:Object){
		/// LABELS //
		// convert to array
		if (typeof(obj) == "object" && Utils.isArray(obj) == false){
			obj = Utils.objectToArray(obj);
		}
		
    	pobj.labels = new Object();
		pobj.labels.label = new Array();
		for(var i = 0; i < obj.length; i++){
			pobj.labels.label[i] = new Object();
			pobj.labels.label[i].parent = pobj;
			pobj.labels.label[i].type = "label";			
			pobj.labels.label[i].oid = obj[i].attributes.oid;
			pobj.labels.label[i].link_with = obj[i].attributes.link_with.split(",");						
			pobj.labels.label[i].remain = Utils.parseTrue(obj[i].attributes.remain, true);						
			pobj.labels.label[i].color = Utils.toColor(obj[i].attributes.color, __config.text_color);
			pobj.labels.label[i].bg_color = Utils.toColor(obj[i].attributes.bg_color);
			pobj.labels.label[i].bg_alpha = Utils.toNumber(obj[i].attributes.bg_alpha, 100);
			pobj.labels.label[i].color_hover = Utils.toColor(obj[i].attributes.color_hover);
			pobj.labels.label[i].url = obj[i].attributes.url;
			pobj.labels.label[i].target = Utils.checkUndefined(obj[i].attributes.target, "");						
			pobj.labels.label[i].text = obj[i].text.data;
			pobj.labels.label[i].description = obj[i].description.data;
			pobj.labels.label[i].title = Utils.checkUndefined(obj[i].attributes.title, "");			
			
			pobj.labels.label[i].x = obj[i].attributes.x;
			pobj.labels.label[i].y = obj[i].attributes.y;
			pobj.labels.label[i].long = obj[i].attributes.long;
			pobj.labels.label[i].lat = obj[i].attributes.lat;
			pobj.labels.label[i].zoom_x = obj[i].attributes.zoom_x;
			pobj.labels.label[i].zoom_y = obj[i].attributes.zoom_y;
			pobj.labels.label[i].zoom = Utils.toNumber(Utils.stripSymbols(obj[i].attributes.zoom, "%"));			
			pobj.labels.label[i].width = Utils.getCoordinate(obj[i].attributes.width, __config.width);
			
			pobj.labels.label[i].align = obj[i].attributes.align.toLowerCase();
			pobj.labels.label[i].rotate = Utils.parseTrue(obj[i].attributes.rotate);			
			pobj.labels.label[i].width = obj[i].attributes.width;
			pobj.labels.label[i].text_size = Utils.toNumber(obj[i].attributes.text_size, __config.text_size);
			pobj.labels.label[i].fixed_size = Utils.parseTrue(obj[i].attributes.fixed_size, true);
			pobj.labels.label[i].balloon = Utils.parseTrue(obj[i].attributes.balloon, true);	
			// text box
			pobj.labels.label[i].text_box = Utils.parseTrue(obj[i].attributes.text_box, __config.text_box.enabled);
			pobj.labels.label[i].text_box_x = Utils.checkUndefined(obj[i].attributes.text_box_x, __config.text_box.x);
			pobj.labels.label[i].text_box_y = Utils.checkUndefined(obj[i].attributes.text_box_y, __config.text_box.y);
			pobj.labels.label[i].text_box_width = Utils.checkUndefined(obj[i].attributes.text_box_width, __config.text_box.width);
			pobj.labels.label[i].text_box_height = Utils.checkUndefined(obj[i].attributes.text_box_height, __config.text_box.height);
			pobj.labels.label[i].object_list = Utils.parseTrue(obj[i].attributes.object_list, true);			
			
			__data.by_id[obj[i].attributes.oid] = pobj.labels.label[i];
			
			// check for children
			if(typeof(obj[i].movies.movie) == "object"){
				__parseMovies(obj[i].movies.movie, pobj.labels.label[i]);
			}
			if(typeof(obj[i].labels.label) == "object"){
				__parseLabels(obj[i].labels.label, pobj.labels.label[i]);
			}
			if(typeof(obj[i].lines.line) == "object"){
				__parseLines(obj[i].lines.line, pobj.labels.label[i]);
			}			
		}				
	}	
	
	// parse LINES
	private function __parseLines(obj:Object, pobj:Object){
		/// LINES //
		// convert to array
		if (typeof(obj) == "object" && Utils.isArray(obj) == false){
			obj = Utils.objectToArray(obj);
		}
		
    	pobj.lines = new Object();
		pobj.lines.line = new Array();
		for(var i = 0; i < obj.length; i++){
			pobj.lines.line[i] = new Object();
			pobj.lines.line[i].remain = Utils.parseTrue(obj[i].attributes.remain, true);						
			pobj.lines.line[i].x = Utils.stripSymbols(obj[i].attributes.x, " ").split(",");			
			pobj.lines.line[i].y = Utils.stripSymbols(obj[i].attributes.y, " ").split(",");
			pobj.lines.line[i].long = Utils.stripSymbols(obj[i].attributes.long, " ").split(",");
			pobj.lines.line[i].lat = Utils.stripSymbols(obj[i].attributes.lat, " ").split(",");
			pobj.lines.line[i].width = Utils.toNumber(obj[i].attributes.width, __config.line.width);
			pobj.lines.line[i].alpha = Utils.toNumber(obj[i].attributes.alpha, __config.line.alpha);
			pobj.lines.line[i].color = Utils.toColor(obj[i].attributes.color, __config.line.color);
			pobj.lines.line[i].arrow = Utils.checkUndefined(obj[i].attributes.arrow.toLowerCase(), __config.line.arrow);
			
			if(pobj.lines.line[i].arrow != "end" && pobj.lines.line[i].arrow != "middle" && pobj.lines.line[i].arrow != "start" && pobj.lines.line[i].arrow != "both"){
				pobj.lines.line[i].arrow = undefined;
			}
			pobj.lines.line[i].dashed = Utils.parseTrue(obj[i].attributes.dashed, __config.line.dashed);
			pobj.lines.line[i].arrow_color = Utils.toColor(obj[i].attributes.arrow_color, __config.line.arrow_color);
			pobj.lines.line[i].arrow_alpha = Utils.toNumber(obj[i].attributes.arrow_alpha, __config.line.arrow_alpha);
			pobj.lines.line[i].arrow_size = Utils.toNumber(obj[i].attributes.arrow_size, __config.line.arrow_size);
			pobj.lines.line[i].fixed_size = Utils.parseTrue(obj[i].attributes.fixed_size, __config.line.fixed_size);			
			pobj.lines.line[i].curved = Utils.parseTrue(obj[i].attributes.curved, __config.line.curved);						
		}		
	}
	
	// parse movies
	private function __parseMovies(obj:Object, pobj:Object){
		/// MOVIES
		// if there is only one area, convert object to array
		if (typeof(obj) == "object" && Utils.isArray(obj) == false){
			obj = Utils.objectToArray(obj);
		}
		
		pobj.movies = new Object();
		pobj.movies.movie =  new Array();
		
		var sum = 0;
		
		for (var i = 0; i < obj.length; i++){
			pobj.movies.movie[i] = new Object();

			pobj.movies.movie[i].parent = pobj;
			pobj.movies.movie[i].oid = obj[i].attributes.oid;			
			pobj.movies.movie[i].type = "movie";
			pobj.movies.movie[i].link_with = obj[i].attributes.link_with.split(",");						
			pobj.movies.movie[i].file = obj[i].attributes.file;
			pobj.movies.movie[i].flash_vars = obj[i].attributes.flash_vars;
			pobj.movies.movie[i].remain = Utils.parseTrue(obj[i].attributes.remain, true);			
			pobj.movies.movie[i].color = Utils.toColor(obj[i].attributes.color, __config.movie.color);
			pobj.movies.movie[i].color_hover = Utils.toColor(obj[i].attributes.color_hover, __config.movie.color_hover);
			pobj.movies.movie[i].color_selected = Utils.toColor(obj[i].attributes.color_selected, Utils.checkUndefined(__config.movie.color_selected, pobj.movies.movie[i].color));
			pobj.movies.movie[i].alpha = Utils.toNumber(obj[i].attributes.alpha, 100);
			pobj.movies.movie[i].title = Utils.checkUndefined(obj[i].attributes.title, "");
			pobj.movies.movie[i].url = obj[i].attributes.url;
			pobj.movies.movie[i].target = Utils.checkUndefined(obj[i].attributes.target, "");			
			pobj.movies.movie[i].description = Utils.checkUndefined(obj[i].description.data, "");
			pobj.movies.movie[i].value = Utils.toNumber(obj[i].attributes.value);
			pobj.movies.movie[i].x = obj[i].attributes.x, __config.width;
			pobj.movies.movie[i].y = obj[i].attributes.y, __config.height;
			pobj.movies.movie[i].zoom_x = obj[i].attributes.zoom_x;
			pobj.movies.movie[i].zoom_y = obj[i].attributes.zoom_y;
			pobj.movies.movie[i].zoom = Utils.toNumber(Utils.stripSymbols(obj[i].attributes.zoom, "%"));			
			pobj.movies.movie[i].width = obj[i].attributes.width;
			pobj.movies.movie[i].height = obj[i].attributes.height;
			pobj.movies.movie[i].rotation = obj[i].attributes.rotation;			
			pobj.movies.movie[i].long = obj[i].attributes.long;
			pobj.movies.movie[i].lat = obj[i].attributes.lat;
			pobj.movies.movie[i].center = Utils.parseTrue(obj[i].attributes.center);			
			pobj.movies.movie[i].fixed_size = Utils.parseTrue(obj[i].attributes.fixed_size, false);
			// text box
			pobj.movies.movie[i].text_box = Utils.parseTrue(obj[i].attributes.text_box, __config.text_box.enabled);
			pobj.movies.movie[i].text_box_x = Utils.checkUndefined(obj[i].attributes.text_box_x, __config.text_box.x);
			pobj.movies.movie[i].text_box_y = Utils.checkUndefined(obj[i].attributes.text_box_y, __config.text_box.y);			
			pobj.movies.movie[i].text_box_width = Utils.checkUndefined(obj[i].attributes.text_box_width, __config.text_box.width);
			pobj.movies.movie[i].text_box_height = Utils.checkUndefined(obj[i].attributes.text_box_height, __config.text_box.height);

			pobj.movies.movie[i].object_list = Utils.parseTrue(obj[i].attributes.object_list, true);						

			
			if(__config.movie.show_balloon_if_value_set == true && isNaN(Number(pobj.movies.movie[i].value)) == true){
				var show_balloon = false;
			}
			else{
				var show_balloon = true;
			}			
			pobj.movies.movie[i].balloon = Utils.parseTrue(obj[i].attributes.balloon, show_balloon);
			
			__data.by_id[obj[i].attributes.oid] = pobj.movies.movie[i];
			
			// check for children
			if(typeof(obj[i].movies.movie) == "object"){
				__parseMovies(obj[i].movies.movie, pobj.movies.movie[i]);
			}
			if(typeof(obj[i].labels.label) == "object"){
				__parseLabels(obj[i].labels.label, pobj.movies.movie[i]);
			}
			if(typeof(obj[i].lines.line) == "object"){
				__parseLines(obj[i].lines.line, pobj.movies.movie[i]);
			}					
		}		
	}
	
	// get data object
	public function get obj ():Object{
		return(__data);
	}
}