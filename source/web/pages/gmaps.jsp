<%
   String rguuid  = (String) request.getParameter("rguuid");
%>



<div style="border:1px solid #CCCCCC;">
<div id="map_container" style="width: 800px; height: 580px;"/>
<div id="controls" style="font: 10px small caps bold Verdana;background-color:#CCCCCC;padding-top:2px;padding-bottom:1px;">	
		&nbsp;
		<div style="float:left;padding-right:20px;padding-left:10px;">Show markers <input type="checkbox" id="showMarkersCheckbox" checked onclick="reprocessLines(drawData);"/></div>
		<div style="float:left;">Show lines <input type="checkbox" id="showLinesCheckbox" onclick="reprocessLines(drawData);"/></div>
	
</div>
<div id="cache"></div>
</div>

<script type="text/javascript">

var tooltiptemplate='<div style="padding-top:20px;font-size:12px;">'+
						'<div style="float:left;padding-right:10px;">'+
							'<img width="30" height="30" src="'+controller+'?action=render&*image*"/>'+					
						'</div>'+						
						'<div style="float:left;padding-left:5px;border-left:1px solid #CCCCCC;">'+
							'<b>Name :</b>*text*</br>'+
							'<b>Latitude :</b>*latitude*</br>'+
							'<b>Longitude :</b>*longitude*</br>'+
						'</div>'+
						'<div style="clear:both"/>'+
					'</div>';

function getFormattedTooltip(rawTooltip, imgPath) {	
	
	var els = rawTooltip.split(';');
	var output = '<div>';	
	for (var i=0; i<els.length-1; i++) {
		var el = els[i].split(',');
		
		var fromtemplate = tooltiptemplate.replace('*latitude*',el[0]);		
		fromtemplate = fromtemplate.replace('*longitude*',el[1]);
		fromtemplate = fromtemplate.replace('*text*',el[2].replace(/%%/g,','));
		fromtemplate = fromtemplate.replace('*image*',imgPath[parseInt(el[3])]);
		
		output = output+fromtemplate;		
		
	}
	
	
	
	return output+'</div>';
	
}

/*
 *  Google Maps data structure.
 */
function GoogleMapsData() {
    this.lat = [];
    this.lng = [];
    this.imagePath = [];
	this.displayImage = [];
    this.startPoint = [];
    this.endPoint = [];
    this.toolTip = [];
	this.rguuid = '<%=rguuid%>';
}

var drawData;

var lines = [];
var lineVis = [];

var icons = [];
var iconsVis = [];


function reloadMap() {
    var googleMapsData = new GoogleMapsData();
    doPostCall('loadData','GoogleMapsData',googleMapsData);
}


/*
 *  Creates new marker on the map and attaches an HTML type tooltip
 *
 *  point - latlong type object describing the location of the marker
 *  html - html to render in tooltip
 *  markerOptions - marker options to attach (image, color)
 */
function createMarker(point,html,markerOptions) {
    var marker = new GMarker(point, markerOptions);
    GEvent.addListener(marker, "click", function() {
     marker.openInfoWindowHtml(html);
    });
    return marker;
}


function lineRectangleIntersect(x0,y0,x1,y1, r,t,l,b) {
	var m = (y1-y0) / (x1-x0);
	var c = y0 -(m*x0);

	if(m>0) {
	   var top_intersection = (m*l  + c);
	   var bottom_intersection = (m*r  + c);
	} else {
	   var top_intersection = (m*r  + c);
	   var bottom_intersection = (m*l  + c);
	}

	if(y0<y1) {
	   var toptrianglepoint = y0;
	   var bottomtrianglepoint = y1;
	} else {
	   var toptrianglepoint = y1;
	   var bottomtrianglepoint = y0;
	}

	var topoverlap;
	var botoverlap;

	topoverlap = top_intersection>toptrianglepoint ? top_intersection : toptrianglepoint;
	botoverlap = bottom_intersection<bottomtrianglepoint ? bottom_intersection : bottomtrianglepoint;

	return (topoverlap<botoverlap) && (!((botoverlap<t) || (topoverlap>b)));
}



/*
 *  Processes map data received from the server based
 *  on DataArray data structure
 */
function processMapData(data) {

	drawData = data;

    var lat = data.lat;
    var lng = data.lng;
    var startPoint = data.startPoint;
    var endPoint = data.endPoint;
    var displayImage = data.displayImage;
    var toolTip = data.toolTip;
	var imgPath = data.imagePath;

	var blat1 = map.getBounds().getNorthEast().lat();
	var blng1 = map.getBounds().getNorthEast().lng();

	var blat2 = map.getBounds().getSouthWest().lat();
	var blng2 = map.getBounds().getSouthWest().lng();
	
	var imgCacheDiv = '';
	
	for (var i=0; i<imgPath.length; i++) {
		imgCacheDiv = imgCacheDiv + '<img src="'+controller + '?action=render&' + imgPath[parseInt(displayImage[i])]+'" style="display:none"/>';
	}
	
	$('#cache').html(imgCacheDiv);

		
	/* Parse data structure and add markers on the map */
	for (var i = 0; i < lat.length; i++) {
	
		if ((lat[i] != null) && (lng[i] != null)) {
		
			var customMarker = new GIcon();
			
			customMarker.image = controller + '?action=render&' + imgPath[parseInt(displayImage[i])];
			
			
			
			customMarker.iconSize = new GSize(25, 25);
									
			customMarker.iconAnchor = new GPoint(15, 15);
			customMarker.infoWindowAnchor = new GPoint(15, 15);
			
			//customMarker.infoShadowAnchor = new GPoint(0, 0);
			//customMarker.shadowSize = new GSize(5, 5);
			
			
			var latlng = new GLatLng(lat[i], lng[i]);
			markerOptions = {
				icon: customMarker,
				title: toolTip[i]
			};
			var tt = getFormattedTooltip(toolTip[i],imgPath)
			var marker = createMarker(latlng, tt, markerOptions);
			//map.addOverlay(marker);
			icons[i] = marker;
			iconsVis[i] = 0;
			
		}
	}



	/* Parse line connections */
	for (var i = 0; i < startPoint.length; i++) {
		var lat1 = lat[startPoint[i]];
		var lng1 = lng[startPoint[i]];
		
		var lat2 = lat[endPoint[i]];
		var lng2 = lng[endPoint[i]];
		
		
		if (lat1 != null && lng1 != null && lat2 != null && lng != null) {
			var polyline = new GPolyline([new GLatLng(lat1, lng1), new GLatLng(lat2, lng2)], "#0000FF", 1);
			lines[i] = polyline;
			lineVis[i] = 0;
		}
		
		
	}
	

	reprocessLines(drawData);


}

function reprocessLines(data) {

	var lat = data.lat;
	var lng = data.lng;
	var startPoint = data.startPoint;
	var endPoint = data.endPoint;
	var displayImage = data.displayImage;
	var toolTip = data.toolTip;
	
	/* Latitude and longitudes for NE and SE point of the viewport */
	var blat1 = map.getBounds().getNorthEast().lat();
	var blng1 = map.getBounds().getNorthEast().lng();
	var blat2 = map.getBounds().getSouthWest().lat();
	var blng2 = map.getBounds().getSouthWest().lng();

	if (showMarkersCheckbox.checked == true) {
	
		for (var i = 0; i < lat.length; i++) {
			if ((lat[i] != null) && (lng[i] != null)) {
				if (inside(lat[i], lng[i], blat2, blat1, blng2, blng1) && (iconsVis[i] == 0)) {
					map.addOverlay(icons[i]);
					iconsVis[i] = 1;
				}
				else 
					if (iconsVis == 1) {
						map.removeOverlay(icons[i]);
						iconsVis[i] = 0;
					}
			}
		}
	} else {
		for (var i = 0; i < lat.length; i++) {
			map.removeOverlay(icons[i]);
			iconsVis[i] = 0;
		}
	}

	if (showLinesCheckbox.checked == true) {
		/* Parse line connections */
		for (var i = 0; i < startPoint.length; i++) {
			var lat1 = lat[startPoint[i]];
			var lng1 = lng[startPoint[i]];
			
			var lat2 = lat[endPoint[i]];
			var lng2 = lng[endPoint[i]];
			
			if (lines[i] != null) {
				if (inside(lat1, lng1, blat2, blat1, blng2, blng1) ||
				inside(lat2, lng2, blat2, blat1, blng2, blng1) ||
				lineRectangleIntersect(lat1, lng1, lat2, lng2, blat1, blng1 - 10, blat2, blng2 + 10)) {
					if (lineVis[i] == 0) {
						map.addOverlay(lines[i]);
						lineVis[i] = 1;
					}
				}
				else {
					map.removeOverlay(lines[i]);
					lineVis[i] = 0;
				}
			}
			
		}
	} else {
		for (var i = 0; i < startPoint.length; i++) {
			map.removeOverlay(lines[i]);
			lineVis[i] = 0;			
		}
	}

}

function inside(x,y, bx1,bx2,by1,by2) {
	var i=0;
	if (x>bx1 && x<bx2 && y>by1 && y<by2) {
		i=1;
	}
	return i;
}

function refreshGoogleMaps(data) {
	var googleMapsData = new GoogleMapsData();
	doPostCall('loadData','GoogleMapsData',googleMapsData);
}


var map = new GMap2(document.getElementById("map_container"));
if (GBrowserIsCompatible()) {
    map.setCenter(new GLatLng(36.44, -90.13), 9);
    map.setUIToDefault();
}
GMap2.prototype.getDivPixelCenter = GMap2.prototype.w;
GMap2.prototype.fromDivPixelsToLatLngBounds = GMap2.prototype.Zd;
GPolyline.prototype.getVectors = GPolyline.prototype.kb;
GPolyline.prototype.getPoints = GPolyline.prototype.ib;
GPolyline.prototype.getSomething = GPolyline.prototype.jb; 

GEvent.addListener(map, "zoomend", function() {
	reprocessLines(drawData);
});

GEvent.addListener(map, "dragend", function() {
	reprocessLines(drawData)
});

var googleMapsData = new GoogleMapsData();
doPostCall('loadData','GoogleMapsData',googleMapsData);



</script>
