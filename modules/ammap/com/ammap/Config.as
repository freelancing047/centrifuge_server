import com.ammap.Utils;

class com.ammap.Config {

	private var __settings:Object;
	private var __config:Object;
	
	function Config(settings:Object){
		__settings = settings;
		__config = new Object();
		__parseSettings();
	}
	
	private function __parseSettings(){
		__config.js_enabled	= Utils.parseTrue (__settings.js_enabled.data, true);		
		__config.developer_mode = Utils.parseTrue(__settings.developer_mode.data, false);
		__config.projection = Utils.checkUndefined(__settings.projection.data.toLowerCase(), "xy");		
		// width and height
		__config.width = Utils.toNumber(__settings.width.data);
		__config.height = Utils.toNumber(__settings.height.data);
		__config.font = Utils.checkUndefined(__settings.font.data, "Tahoma");
		_global.font = __config.font;
		__config.text_size = Utils.toNumber(__settings.text_size.data, 11);
		__config.text_color = Utils.toColor(__settings.text_color.data, 0x000000);
		__config.reload_data_interval = Utils.toNumber(__settings.reload_data_interval.data, 0);
		__config.add_time_stamp = Utils.parseTrue(__settings.add_time_stamp.data, false);		
		__config.decimals_separator = Utils.checkUndefined(__settings.decimals_separator.data, ",");
		__config.thousands_separator = Utils.checkUndefined(__settings.thousands_separator.data, " ");
		__config.mask = Utils.parseTrue(__settings.mask.data, true);		
		__config.preloader_on_reload = Utils.parseTrue(__settings.preloader_on_reload.data, false);
		__config.force_smoothing = Utils.parseTrue(__settings.force_smoothing.data, false);		
		__config.layers = Utils.checkUndefined(__settings.layers.data, "overlay,movies,labels,lines");
		
		if(__config.thousands_separator == "none"){
			__config.thousands_separator = "";
		}
		__config.redraw = Utils.parseTrue(__settings.redraw.data, false);
		__config.precision = Math.abs(Utils.toNumber(__settings.precision.data, 2));						
		__config.export_image_file = __settings.export_image_file.data;
		__config.color_change_time_start = Utils.toNumber(__settings.color_change_time_start.data, 2);
		__config.color_change_time_hover = Utils.toNumber(__settings.color_change_time_hover.data, 0.2);
		__config.fit_to_screen = Utils.parseTrue(__settings.fit_to_screen.data, true);
		__config.drag_map = Utils.parseTrue(__settings.drag_map.data, true);
		__config.always_hand = Utils.parseTrue(__settings.always_hand.data, false);		
		
		__config.strings = new Object();
		__config.strings.loading_map = Utils.checkUndefined(__settings.strings.loading_map.data, "Loading map");
		__config.strings.export_as_image = Utils.checkUndefined(__settings.strings.export_as_image.data, "Export as image");		
		__config.strings.collecting_data = Utils.checkUndefined(__settings.strings.collecting_data.data, "Collecting data");
		

		__config.area = new Object();
		__config.area.color_solid = Utils.toColor(__settings.area.color_solid.data, 0x990000);
		__config.area.color_light = Utils.toColor(__settings.area.color_light.data, 0xFFCC00);
		__config.area.color_hover = Utils.toColor(__settings.area.color_hover.data);
		__config.area.color_selected = Utils.toColor(__settings.area.color_selected.data);
		__config.area.color_unlisted = Utils.toColor(__settings.area.color_unlisted.data);		
		__config.area.balloon_text = __settings.area.balloon_text.data;
		__config.area.active_only_if_value_set = Utils.parseTrue(__settings.area.active_only_if_value_set.data, false);		
		__config.area.disable_when_clicked = Utils.parseTrue(__settings.area.disable_when_clicked.data, false);				
		
		
		__config.movie = new Object();
		__config.movie.balloon_text = __settings.movie.balloon_text.data;
		__config.movie.active_only_if_value_set = Utils.parseTrue(__settings.movie.active_only_if_value_set.data, false);
		__config.movie.disable_when_clicked = Utils.parseTrue(__settings.movie.disable_when_clicked.data, false);						
		__config.movie.color = Utils.toColor(__settings.movie.color.data);
		__config.movie.color_hover = Utils.toColor(__settings.movie.color_hover.data);
		__config.movie.color_selected = Utils.toColor(__settings.movie.color_selected.data);		
		
		
		__config.label = new Object();
		__config.label.balloon_text = __settings.label.balloon_text.data;
		__config.label.bg_margins_vertical = Utils.toNumber(__settings.label.bg_margins_vertical.data, 10);
		__config.label.bg_margins_horizontal = Utils.toNumber(__settings.label.bg_margins_horizontal.data, 10);
		
		__config.line = new Object();
		__config.line.color = Utils.toColor(__settings.line.color.data, 0x990000);
		__config.line.alpha = Utils.toNumber(__settings.line.alpha.data, 100);
		__config.line.width = Utils.toNumber(__settings.line.width.data, 0);
		__config.line.arrow = __settings.line.arrow.data.toLowerCase();
		__config.line.arrow_alpha = Utils.toNumber(__settings.line.arrow_alpha.data, 100);
		__config.line.arrow_size = Utils.toNumber(__settings.line.arrow_size.data, 8);		
		__config.line.arrow_color = Utils.toColor(__settings.line.arrow_color.data, __config.line.color);		
		__config.line.dashed = Utils.parseTrue(__settings.line.dashed.data, false);
		__config.line.fixed_size = Utils.parseTrue(__settings.line.dashed.fixed_size, true);
		__config.line.curved = Utils.parseTrue(__settings.line.curved.data, false);
		
		// BALLOON
		__config.balloon = new Object();
		__config.balloon.enabled = Utils.parseTrue(__settings.balloon.enabled.data, true);
		__config.balloon.color = Utils.toColor(__settings.balloon.color.data, 0x880000);
		__config.balloon.alpha = Utils.toNumber(__settings.balloon.alpha.data, 100);
		__config.balloon.max_width = Utils.checkUndefined(__settings.balloon.max_width.data, "30%");		
		__config.balloon.text_color = Utils.toColor(__settings.balloon.text_color.data, 0xFFFFFF);
		__config.balloon.text_size = Utils.toNumber(__settings.balloon.text_size.data, __config.text_size);	
		__config.balloon.arrow = Utils.checkUndefined(__settings.balloon.arrow.data, "vertical");		
		

		__config.balloon.border_color = Utils.toColor(__settings.balloon.border_color.data, __config.balloon.color);
		__config.balloon.border_alpha = Utils.toNumber(__settings.balloon.border_alpha.data, __config.balloon.alpha);
		__config.balloon.border_width = Utils.toNumber(__settings.balloon.border_width.data, 0);		
		__config.balloon.corner_radius = Utils.toNumber(__settings.balloon.corner_radius.data, 0);				
		
		
		// background
		__config.background = new Object();
		__config.background.color = Utils.toColor(__settings.background.color.data, 0x444444);		
		__config.background.alpha = Utils.toNumber(__settings.background.alpha.data, 0);
		__config.background.file = __settings.background.file.data;
		__config.background.stretch = Utils.parseTrue(__settings.background.stretch.data, false);
		__config.background.overlay_file = __settings.background.overlay_file.data;
		__config.background.overlay_stretch = Utils.parseTrue(__settings.background.overlay_stretch.data, false);
		__config.background.border_color = Utils.toColor(__settings.background.border_color.data, 0x000000);
		__config.background.border_alpha = Utils.toNumber(__settings.background.border_alpha.data, 0);
		
		// ZOOM
		__config.zoom = new Object();
		__config.zoom.enabled = Utils.parseTrue(__settings.zoom.enabled.data, true);
		__config.zoom.locked = Utils.parseTrue(__settings.zoom.locked.data, false);
		__config.zoom.arrows_enabled = Utils.parseTrue(__settings.zoom.arrows_enabled.data, true);		
		__config.zoom.mouse_wheel_enabled = Utils.parseTrue(__settings.zoom.mouse_wheel_enabled.data, true);				
		__config.zoom.home_link_enabled = Utils.parseTrue(__settings.zoom.home_link_enabled.data, true);				
		__config.zoom.step_size = Utils.toNumber(__settings.zoom.step_size.data, 10);		
		__config.zoom.color = Utils.toColor(__settings.zoom.color.data, 0x990000);
		__config.zoom.alpha = Utils.toNumber(__settings.zoom.alpha.data, 100);
		__config.zoom.color_hover = Utils.toColor(__settings.zoom.color_hover.data, 0xCC0000);
		__config.zoom.outline_color = Utils.toColor(__settings.zoom.outline_color.data, 0xFFFFFF);
		__config.zoom.outline_alpha = Utils.toNumber(__settings.zoom.outline_alpha.data, 100);
		__config.zoom.x = Utils.checkUndefined(__settings.zoom.x.data, 15);
		__config.zoom.y = Utils.checkUndefined(__settings.zoom.y.data, 22);
		__config.zoom.height = Utils.checkUndefined(__settings.zoom.height.data, 200);
		__config.zoom.min = Utils.toNumber(Utils.stripSymbols(__settings.zoom.min.data, "%"));
		__config.zoom.max = Utils.toNumber(Utils.stripSymbols(__settings.zoom.max.data, "%"), 2000);
		__config.zoom.grid_every = Utils.toNumber(Utils.stripSymbols(__settings.zoom.grid_every.data, "%"), 100);		
		__config.zoom.time = Utils.toNumber(__settings.zoom.time.data, 1);
		__config.zoom.effect = Utils.checkUndefined(__settings.zoom.effect.data.toLowerCase(), "easeout");
		__config.zoom.rotate = Utils.parseTrue(__settings.zoom.rotate.data, false);
		__config.zoom.background_zooms_to_top = Utils.parseTrue(__settings.zoom.background_zooms_to_top.data, false);
		__config.zoom.zoom_on_click = Utils.parseTrue(__settings.zoom.zoom_on_click.data, false);
		if(__config.zoom.zoom_on_click == true){
			__config.zoom.background_zooms_to_top = false;
		}

		// SMALL MAP
		__config.small_map = new Object();
		__config.small_map.enabled = Utils.parseTrue(__settings.small_map.enabled.data, true);
		__config.small_map.locked = Utils.parseTrue(__settings.small_map.locked.data, false);
		__config.small_map.active = Utils.parseTrue(__settings.small_map.active.data, true);		
		__config.small_map.x = __settings.small_map.x.data;
		__config.small_map.y = __settings.small_map.y.data;
		__config.small_map.width = Utils.checkUndefined(__settings.small_map.width.data, "25%");
		__config.small_map.color = Utils.toColor(__settings.small_map.color.data);
		__config.small_map.border_width = Utils.toNumber(__settings.small_map.border_width.data, 5);
		__config.small_map.border_color = Utils.toColor(__settings.small_map.border_color.data, 0xFFFFFF);
		__config.small_map.rectangle_color = Utils.toColor(__settings.small_map.rectangle_color.data, 0xFFFFFF);
		__config.small_map.collapse_button_color = Utils.toColor(__settings.small_map.collapse_button_color.data, 0x000000);				
		__config.small_map.collapse_button_position = Utils.checkUndefined(__settings.small_map.collapse_button_position.data.toLowerCase(), "br");
		

		// NAV PATH
		__config.navigation_path = new Object();
		__config.navigation_path.enabled = Utils.parseTrue(__settings.navigation_path.enabled.data, false);
		__config.navigation_path.x = Utils.checkUndefined(__settings.navigation_path.x.data, 70);
		__config.navigation_path.y = Utils.checkUndefined(__settings.navigation_path.y.data, 36);
		__config.navigation_path.color = Utils.toColor(__settings.navigation_path.color.data, 0x000000);
		__config.navigation_path.alpha = Utils.toNumber(__settings.navigation_path.alpha.data, 0);
		__config.navigation_path.padding = Utils.toNumber(__settings.navigation_path.padding.data, 0);		
		__config.navigation_path.text_size = Utils.toNumber(__settings.navigation_path.text_size.data, __config.text_size);
		__config.navigation_path.text_color = Utils.toColor(__settings.navigation_path.text_color.data, 0xFFFFFF);
		__config.navigation_path.text_color_hover = Utils.toColor(__settings.navigation_path.text_color_hover.data, 0xCC0000);
		__config.navigation_path.separator = Utils.checkUndefined(__settings.navigation_path.separator.data, "");
		__config.navigation_path.home_text = Utils.checkUndefined(__settings.navigation_path.home_text.data, "");		
		
		// export as image
		__config.export_as_image = new Object();
		__config.export_as_image.file = Utils.checkUndefined(__settings.export_as_image.file.data, __config.export_image_file);
		__config.export_as_image.target = Utils.checkUndefined(__settings.export_as_image.target.data, "");
		__config.export_as_image.x = Utils.toNumber(__settings.export_as_image.x.data);
		__config.export_as_image.y = Utils.toNumber(__settings.export_as_image.y.data);
		__config.export_as_image.color = Utils.toColor(__settings.export_as_image.color.data, 0xBBBB00);
		__config.export_as_image.alpha = Utils.toNumber(__settings.export_as_image.alpha.data, 0);
		__config.export_as_image.text_color = Utils.toColor(__settings.export_as_image.text_color.data, __config.text_color);
		__config.export_as_image.text_size = Utils.toNumber(__settings.export_as_image.text_size.data, __config.text_size);		
		
		
		
		// LEGEND
		__config.legend = new Object();
		__config.legend.enabled =  Utils.parseTrue(__settings.legend.enabled.data, true);
		__config.legend.x = Utils.checkUndefined(__settings.legend.x.data, 20);
		__config.legend.y = Utils.checkUndefined(__settings.legend.y.data, "!55");
		__config.legend.width = __settings.legend.width.data;
		__config.legend.max_columns = Utils.toNumber(__settings.legend.max_columns.data, 0);		
		__config.legend.color = Utils.toColor(__settings.legend.color.data, 0xFFFFFF);
		__config.legend.alpha = Utils.toNumber(__settings.legend.alpha.data, 50);
		__config.legend.border_color = Utils.toColor(__settings.legend.border_color.data, 0x000000);	
		__config.legend.border_alpha = Utils.toNumber(__settings.legend.border_alpha.data, 30);
		__config.legend.key = new Object();
		__config.legend.key.size = Utils.toNumber(__settings.legend.key.size.data, 16);
		__config.legend.key.border_color =  Utils.toColor(__settings.legend.key.border_color.data);
		__config.legend.text_color = Utils.toColor(__settings.legend.text_color.data, __config.text_color);
		__config.legend.text_size = Utils.toNumber(__settings.legend.text_size.data, __config.text_size);
		__config.legend.spacing = Utils.toNumber(__settings.legend.spacing.data, 10);		
		__config.legend.margins = Utils.toNumber(__settings.legend.margins.data, 10);
		__config.legend.entries = new Object();
		__config.legend.entries.entry = new Array();
		__settings.legend.entries.entry = Utils.objectToArray(__settings.legend.entries.entry);			
			
		for (var j = 0; j < __settings.legend.entries.entry.length; j++){
			__config.legend.entries.entry[j] = new Object();
			__config.legend.entries.entry[j].color = Utils.toColor(__settings.legend.entries.entry[j].attributes.color);
			__config.legend.entries.entry[j].title = __settings.legend.entries.entry[j].data;			
		}
		
		
		
		// text box
		__config.text_box = new Object();
		__config.text_box.enabled = Utils.parseTrue(__settings.text_box.enabled.data, true);
		__config.text_box.selectable = Utils.parseTrue(__settings.text_box.selectable.data, true);		
		__config.text_box.locked = Utils.parseTrue(__settings.text_box.locked.data, false);
		__config.text_box.show_on_hover = Utils.parseTrue(__settings.text_box.show_on_hover.data, false);
		__config.text_box.hide_on_roll_out = Utils.parseTrue(__settings.text_box.hide_on_roll_out.data, false);		
		__config.text_box.color = Utils.toColor(__settings.text_box.color.data, 0xFFFFFF);
		__config.text_box.alpha = Utils.toNumber(__settings.text_box.alpha.data, 100);
		__config.text_box.x = Utils.checkUndefined(__settings.text_box.x.data, "60%");
		__config.text_box.y = Utils.checkUndefined(__settings.text_box.y.data, "20%");
		__config.text_box.height = Utils.checkUndefined(__settings.text_box.height.data, "50%");
		__config.text_box.width = Utils.checkUndefined(__settings.text_box.width.data, "35%");
		__config.text_box.corner_radius = Utils.toNumber(__settings.text_box.corner_radius.data, 0);
		__config.text_box.border_width = Utils.toNumber(__settings.text_box.border_width.data, 0);
		__config.text_box.border_color = Utils.toColor(__settings.text_box.border_color.data, 0xDADADA);
		__config.text_box.border_alpha = Utils.toNumber(__settings.text_box.border_alpha.data, 100);		
		__config.text_box.margin_width = Utils.toNumber(__settings.text_box.margin_width.data, 10);
		__config.text_box.text_size = Utils.toNumber(__settings.text_box.text_size.data, __config.text_size);
		__config.text_box.text_color = Utils.toColor(__settings.text_box.text_color.data, __config.text_color);
		__config.text_box.scroller_color = Utils.toColor(__settings.text_box.scroller_color.data, 0x990000);
		__config.text_box.scroller_bg_color = Utils.toColor(__settings.text_box.scroller_bg_color.data, 0xDADADA);		
		__config.text_box.shadow_alpha = Utils.toNumber(__settings.text_box.shadow_alpha.data, 50);
		__config.text_box.shadow_blur = Utils.toNumber(__settings.text_box.shadow_blur.data, 5);
		__config.text_box.shadow_distance = Utils.toNumber(__settings.text_box.shadow_distance.data, 5);		
		__config.text_box.shadow_color = Utils.toColor(__settings.text_box.shadow_color.data, 0x000000);			
		__config.text_box.close_button_color = Utils.toColor(__settings.text_box.close_button_color.data, 0x000000);			
		__config.text_box.close_button_color_hover = Utils.toColor(__settings.text_box.close_button_color_hover.data, 0xCC0000);
		
		
		__config.object_list = new Object();
		__config.object_list.type = Utils.checkUndefined(__settings.object_list.type.data, "dropdown");
		__config.object_list.enabled = Utils.parseTrue(__settings.object_list.enabled.data, false);
		__config.object_list.color = Utils.toColor(__settings.object_list.color.data, 0xFFFFFF);
		__config.object_list.alpha = Utils.toNumber(__settings.object_list.alpha.data, 100);		
		__config.object_list.x = Utils.checkUndefined(__settings.object_list.x.data, "!200");
		__config.object_list.y = Utils.checkUndefined(__settings.object_list.y.data, "0");
		__config.object_list.height = Utils.checkUndefined(__settings.object_list.height.data, "100%");
		__config.object_list.width = Utils.checkUndefined(__settings.object_list.width.data, 200);		
		__config.object_list.border_color = Utils.toColor(__settings.object_list.border_color.data, 0xDADADA);
		__config.object_list.border_alpha = Utils.toNumber(__settings.object_list.border_alpha.data, 100);		
		__config.object_list.text_size = Utils.toNumber(__settings.object_list.text_size.data, __config.text_size);
		__config.object_list.text_color = Utils.toColor(__settings.object_list.text_color.data, __config.text_color);
		__config.object_list.scroller_color = Utils.toColor(__settings.object_list.scroller_color.data, 0x990000);
		__config.object_list.scroller_bg_color = Utils.toColor(__settings.object_list.scroller_bg_color.data, 0xDADADA);		
		__config.object_list.color_hover = Utils.toColor(__settings.object_list.color_hover.data, 0xDADADA);			
		__config.object_list.color_selected = Utils.toColor(__settings.object_list.color_selected.data, 0x000000);		
		__config.object_list.text_color_hover = Utils.toColor(__settings.object_list.text_color_hover.data, 0x000000);			
		__config.object_list.text_color_selected = Utils.toColor(__settings.object_list.text_color_selected.data, 0xFFFFFF);				
		__config.object_list.levels = Utils.toNumber(__settings.object_list.levels.data, 3);
		__config.object_list.include_areas = Utils.parseTrue(__settings.object_list.include_areas.data, true);
		__config.object_list.include_movies = Utils.parseTrue(__settings.object_list.include_movies.data, true);
		__config.object_list.include_labels = Utils.parseTrue(__settings.object_list.include_labels.data, true);
		__config.object_list.home_text = Utils.checkUndefined(__settings.object_list.home_text.data, "Home");		
		
		
		if (typeof(__settings.context_menu.menu) == "object" && Utils.isArray(__settings.context_menu.menu) == false){
			__settings.context_menu.menu = Utils.objectToArray(__settings.context_menu.menu);
		}

		__config.context_menu = new Object();		
		__config.context_menu.default_items = new Object();
		__config.context_menu.default_items.print = Utils.parseTrue(__settings.context_menu.default_items.print.data, true);
		__config.context_menu.default_items.zoom = Utils.parseTrue(__settings.context_menu.default_items.zoom.data, false);		
		
		__config.context_menu.menu = new Array();

		for(var i = 0; i < __settings.context_menu.menu.length; i++){
			__config.context_menu.menu[i] = new Object();
			__config.context_menu.menu[i].title = __settings.context_menu.menu[i].attributes.title;
			__config.context_menu.menu[i].function_name = __settings.context_menu.menu[i].attributes.function_name;
		}
	}
	// get config object
	public function get obj ():Object{
		return(__config);
	}
}
