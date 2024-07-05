<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="csi.server.business.service.InternationalizationService" %>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="csi.server.business.service.InternationalizationService"%>
<%@page import="csi.server.business.service.MapActionsService"%>
<%@page import="csi.server.common.model.map.MapLayerInfo"%>
<%@page import="csi.server.common.model.map.PlaceSizeInfo"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Map"%>
<%@page import="csi.shared.core.visualization.map.MapConfigDTO"%>
<%@page import="org.springframework.web.util.HtmlUtils"%>
<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    InternationalizationService i18n = (InternationalizationService) context.getBean("csi.server.business.service.InternationalizationService");

    List<String> keys = new ArrayList<String>();
    keys.add("mapViewHome");
    keys.add("mapViewSelect");
    keys.add("mapViewDeselect");
    keys.add("menuKeyConstants_select_all");
    keys.add("menuKeyConstants_deselect_all");
    keys.add("tooltip_Type");
    Map<String, String> properties = i18n.getProperties(request.getLocale(), keys);

    String homeText = HtmlUtils.htmlEscape(properties.get("mapViewHome"));
    String selectText = HtmlUtils.htmlEscape(properties.get("mapViewSelect"));
    String deselectText = HtmlUtils.htmlEscape(properties.get("mapViewDeselect"));
    String selectAllText = HtmlUtils.htmlEscape(properties.get("menuKeyConstants_select_all"));
    String deselectAllText = HtmlUtils.htmlEscape(properties.get("menuKeyConstants_deselect_all"));

    MapActionsService mas = (MapActionsService) context.getBean("csi.server.business.service.MapActionsService");

    MapConfigDTO mapConfig = mas.getMapConfig();
    List<MapLayerInfo> mapLayerInfos = mas.getMapLayerInfos(request.getParameter("viz_uuid"));
    PlaceSizeInfo placeSizeInfo = mas.getPlaceSizeInfo(request.getParameter("viz_uuid"));
%>

<%
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Cache-Control", "no-cache, no-store, must-revalidate" );
    response.setDateHeader( "Expires", 0 );
    Set<String> alreadyPosted = new HashSet<String>();
    for (MapLayerInfo mapLayerInfo : mapLayerInfos) {
    	String url = mapLayerInfo.getUrl();
    	if (url != null && !url.isEmpty() && !alreadyPosted.contains(url)) {
    		response.setHeader( "Access-Control-Allow-Origin", url );
		alreadyPosted.add(url);
	}
    }
%>
<html>
    <head>
        <link rel="stylesheet" href="js/arcgis_js_api/3.23/dijit/themes/tundra/tundra.css">
        <link rel="stylesheet" href="js/arcgis_js_api/3.23/esri/css/esri.css">
        <link rel="stylesheet" href="css/csimap.css">
        <link rel="stylesheet" href="css/csi.css">
        <script src="js/arcgis_js_api/3.23/extras34/proj4.js"></script>
        <script src="js/arcgis_js_api/3.23/init.js"></script>
        <script type="text/javascript">
            var layerInfos = [];
            var urlaccessibledone = {};
            var layerInfo;
            var i18nType = "<%= HtmlUtils.htmlEscape(properties.get("tooltip_Type")) %>";
<%
            int index = 0;
            for (MapLayerInfo mapLayerInfo : mapLayerInfos) {
%>
            layerInfo = {};
            layerInfo.id = "<%= mapLayerInfo.getId() %>";
            layerInfo.key = "<%= mapLayerInfo.getKey() %>";
            layerInfo.url = "<%= mapLayerInfo.getUrl() %>";
            layerInfo.type = "<%= mapLayerInfo.getType() %>";
            layerInfo.layername = "<%= mapLayerInfo.getLayername() %>";
            layerInfo.visible = <%= mapLayerInfo.isVisible() %>;
            layerInfo.opacity = <%= mapLayerInfo.getOpacity() %> / 100;
            layerInfos[<%=index%>] = layerInfo;
            testurlaccessibility(<%=index%>);
<%
		index++;
            }
%>
            var basemapId;
            var basemapUrl;
            var basemapType;
            var basemapOpacity;
            proceedIfDone();

            function qs(search_for) {
                var query = window.location.search.substring(1);
                var parms = query.split('&');
                for (var i = 0; i < parms.length; i++) {
                    var pos = parms[i].indexOf('=');
                    if (pos > 0 && search_for == parms[i].substring(0, pos)) {
                        return parms[i].substring(pos + 1);
                    }
                }
                return "";
            }

            function testurlaccessibility(i) {
                urlaccessibledone[i] = false;
                var url = layerInfos[i].url;
                if (url == null || url == "") {
                    layerInfos[i].urlaccessible = false;
                    urlaccessibledone[i] = true;
                } else {
                    try {
                        var request;
                        if (window.XMLHttpRequest)
                            request = new XMLHttpRequest();
                        else
                            request = new ActiveXObject("Microsoft.XMLHTTP");
                        request.open('GET', url, true);
                        request.onreadystatechange = function() {
                            if (request.status === 200)
                                layerInfos[i].urlaccessible = true;
                            else
                                layerInfos[i].urlaccessible = false;
                            urlaccessibledone[i] = true;
                        }
                        request.send();
                    } catch (error) {
                        layerInfos[i].urlaccessible = false;
                        urlaccessibledone[i] = true;
                    }
                }
            }

            function proceedIfDone() {
                if (isDone()) {
                    proceed();
                } else {
                    setTimeout(proceedIfDone, 500);
                }
            }

            function isDone() {
                var j = layerInfos.length - 1;
                while (j >= 0) {
                    if (!urlaccessibledone[j]) return false;
                    j--;
                }
                return true;
            }

            function proceed() {
                var basemapSet = false
                var i = layerInfos.length - 1;
                while (i >= 0 && !basemapSet) {
                    if (layerInfos[i].urlaccessible && layerInfos[i].visible && (layerInfos[i].type == "ArcGISTiled" || layerInfos[i].type == "OpenStreetMap")) {
                        if (layerInfos[i].key == "") {
                            basemapId = layerInfos[i].id;
                            basemapUrl = layerInfos[i].url;
                        } else {
                            basemapId = layerInfos[i].key;
                            basemapUrl = "";
                        }
                        basemapType = layerInfos[i].type;
                        basemapOpacity = layerInfos[i].opacity;
                        basemapSet = true;
                        layerInfos = layerInfos.slice(0, i);
                        break;
                    }
                    i--;
                }

                if (basemapSet) {
                    tileServiceGood();
                } else {
                    basemapId = "erehwon";
                    basemapUrl = "erehwon";
                    basemapType = "";
                    layerInfos = {};
                    tileServiceGood();
                }
            }

            function tileServiceGood() {
                var mapModuleLoaded = false;
                var detailLevel = <%= mapConfig.getDetailLevel() %>;
                var frontendToggleThreshold = <%= mapConfig.getFrontendToggleThreshold() %>;
                var frontendZoomThreshold = <%= mapConfig.getFrontendZoomThreshold() %>;
		var locatorUrl = "<%= mapConfig.getLocatorUrl() %>";
                var minPlaceSize = <%= placeSizeInfo.getMin() %>;
                var maxPlaceSize = <%= placeSizeInfo.getMax() %>;
                window.addEventListener("message", function(event) {
                    if (mapModuleLoaded) {
                        if (event.data == "reload") {
                            reloadHandler();
                        } else if (event.data == "deselectAll") {
                            deselectAllHandler();
                        } else if (event.data == "selectAll") {
                            selectAllHandler();
                        } else if (event.data == "applySelection") {
                            applySelectionHandler();
                        } else if (event.data == "legendUpdated") {
                            legendUpdatedHandler();
                        } else if (event.data == "mapContainerResizing") {
                            mapContainerResizingHandler();
                        } else if (event.data == "mapContainerResized") {
                            mapContainerResizedHandler();
                        } else if (event.data == "resizeHappened") {
                            resizeHappenedHandler();
                        } else if (event.data == "getBase64") {
                            getBase64Handler();
                        } else if (event.data == "zoomHome") {
                            zoomHomeHandler();
                        } else if (event.data == "zoomIn") {
                            zoomInHandler();
                        } else if (event.data == "zoomOut") {
                            zoomOutHandler();
                        } else if (event.data == "rangeChange") {
                            rangeChangeHandler();
                        } else if (event.data == "toggleSelection") {
                            toggleSelectionHandler();
                        } else if (event.data == "toggleSearch") {
                            toggleSearchHandler();
                        } else if (event.data == "checkNeedReload") {
                            checkNeedReloadHandler();
                        } else if (event.data.startsWith("combinedPlaceClicked")) {
                            var arr = event.data.split(";");
                            var operation = arr[1];
                            combinedPlaceClickedHandler(operation);
                        } else if (event.data.startsWith("newPlaceClicked")) {
                            var arr = event.data.split(";");
                            var operation = arr[1];
                            newPlaceClickedHandler(operation);
                        } else if (event.data.startsWith("updatedPlaceClicked")) {
                            var arr = event.data.split(";");
                            var operation = arr[1];
                            updatedPlaceClickedHandler(operation);
                        } else if (event.data.startsWith("associationClicked")) {
                            var arr = event.data.split(";");
                            var associationKey = arr[1];
                            var operation = arr[2];
                            associationClickedHandler(associationKey, operation);
                        } else if (event.data.startsWith("placeClicked")) {
                            var arr = event.data.split(";");
                            var placeId = arr[1];
                            var typename = arr[2];
                            var operation = arr[3];
                            placeClickedHandler(placeId, typename, operation);
                        } else if (event.data.startsWith("trackClicked")) {
                            var arr = event.data.split(";");
                            var trackid = arr[1];
                            var typename = arr[2];
                            var operation = arr[3];
                            trackClickedHandler(trackid, typename, operation);
                        } else if (event.data == "setSelectionModePan") {
                            setSelectionModePan();
                        } else if (event.data == "setSelectionModeCircle") {
                            setSelectionModeCircle();
                        } else if (event.data == "setSelectionModeRectangle") {
                            setSelectionModeRectangle();
                        } else if (event.data == "setSelectionModePolygon") {
                            setSelectionModePolygon();
                        }
                    }
                }, false);
                var reloadHandler;
                var deselectAllHandler;
                var selectAllHandler;
                var applySelectionHandler;
                var legendUpdatedHandler;
                var mapContainerResizingHandler;
                var mapContainerResizedHandler;
                var resizeHappenedHandler;
                var getBase64Handler;
                var zoomHomeHandler;
                var zoomInHandler;
                var zoomOutHandler;
                var rangeChangeHandler;
                var toggleSelectionHandler;
                var toggleSearchHandler;
                var checkNeedReloadHandler;
                var combinedPlaceClickedHandler;
                var newPlaceClickedHandler;
                var updatedPlaceClickedHandler;
                var associationClickedHandler;
                var placeClickedHandler;
                var trackClickedHandler;
                require(['extras34/trackmapModule', 'extras34/Exporter', 'extras34/geometryService', 'dojo/_base/xhr', "esri/SpatialReference", "esri/geometry/Point", "esri/geometry/Extent", "dojo/domReady!"], function(MapModule, Exporter, GeometryService, xhr, SpatialReference, Point, Extent) {
                    function createAndUpdateMap(sequenceNumber, extent, zoom, isNewExtent) {
                        var mapModuleParams = {
                            mapDivContainerId: "mapDivContainer",
                            mapId: "mapDiv",
                            searchId: "searchDiv",
                            dv_uuid: qs("dv_uuid"),
                            viz_uuid: qs("viz_uuid"),
                            homeButtonId: "homeButton",
                            extent: extent,
                            zoom: zoom,
                            cmenuId: "cmenu",
                            deselectId: "deselect",
                            selectId: "select",
                            deselectAllId: "deselectAll",
                            selectAllId: "selectAll",
                            sequenceNumber: sequenceNumber,
                            detailLevel: detailLevel,
                            frontendToggleThreshold: frontendToggleThreshold,
                            frontendZoomThreshold: frontendZoomThreshold,
                            basemapId: basemapId,
                            basemapUrl: basemapUrl,
                            basemapType: basemapType,
                            basemapOpacity: basemapOpacity,
                            layerInfos: layerInfos,
                            minPlaceSize: minPlaceSize,
                            maxPlaceSize: maxPlaceSize,
			    locatorUrl: locatorUrl,
			    i18nType: i18nType,
			    isNewExtent: isNewExtent
                        };
                        var exportModuleParams = {
                            mapDivContainerId: "mapDivContainer",
                            mapId: "mapDiv",
                            dv_uuid: qs("dv_uuid"),
                            viz_uuid: qs("viz_uuid"),
                        };
                        var exporter = new Exporter(exportModuleParams);
                        var mapModule = new MapModule(mapModuleParams);
                        mapModule.updateMap();
                        reloadHandler = function() {
                            mapModule.reloadMap();
                        }
                        deselectAllHandler = function() {
                            mapModule.deselectAll();
                        }
                        selectAllHandler = function() {
                            mapModule.selectAll();
                        }
                        applySelectionHandler = function() {
                            mapModule.applySelection();
                        }
                        legendUpdatedHandler = function() {
                            mapModule.legendUpdated();
                        }
                        mapContainerResizingHandler = function() {
			}
                        mapContainerResizedHandler = function() {
                            mapModule.resizeMapDiv();
                        }
                        resizeHappenedHandler = function() {
                            mapModule.resizeHappened();
                        }
                        getBase64Handler = function() {
                            exporter.doExport();
                        }
                        zoomHomeHandler = function() {
                            mapModule.zoomHome();
                        }
                        zoomInHandler = function() {
                            mapModule.zoomIn();
                        }
                        zoomOutHandler = function() {
                            mapModule.zoomOut();
                        }
                        rangeChangeHandler = function() {
                            mapModule.rangeUpdated();
                        }
                        toggleSelectionHandler = function() {
                            mapModule.toggleSelection();
                        }
                        toggleSearchHandler = function() {
                            mapModule.toggleSearch();
                        }
                        checkNeedReloadHandler = function() {
                            mapModule.checkNeedReload();
                        }
                        combinedPlaceClickedHandler = function(operation) {
                            mapModule.combinedPlaceClicked(operation);
                        }
                        newPlaceClickedHandler = function(operation) {
                            mapModule.newPlaceClicked(operation);
                        }
                        updatedPlaceClickedHandler = function(operation) {
                            mapModule.updatedPlaceClicked(operation);
                        }
                        associationClickedHandler = function(associationKey, operation) {
                            mapModule.associationClicked(associationKey, operation);
                        }
                        placeClickedHandler = function(placeId, typename, operation) {
                            mapModule.placeClicked(placeId, typename, operation);
                        }
                        trackClickedHandler = function(trackid, typename, operation) {
                            mapModule.trackClicked(trackid, typename, operation);
                        }
                        setSelectionModePan = function() {
                            mapModule.setSelectionModePan();
                        }
                        setSelectionModeCircle = function() {
                            mapModule.setSelectionModeCircle();
                        }
                        setSelectionModeRectangle = function() {
                            mapModule.setSelectionModeRectangle();
                        }
                        setSelectionModePolygon = function() {
                            mapModule.setSelectionModePolygon();
                        }
                        mapModuleLoaded = true;
                    }

                    function getExtentInfo(sequenceNumber) {
                        var vizid = qs("viz_uuid");
                        xhr.get({
                            url: "rest/map/getExtentInfo/" + qs("dv_uuid") + "/" + vizid + "/" + sequenceNumber,
                            handleAs: "json",
                            load: function(data) {
                                var extent = null;
                                var zoom = null;
				var isNewExtent = true;
                                if (data != null) {
                                    if (data.placeTypeLimitReached) {
                                        window.parent.hideSpinner(vizid);
                                        window.parent.mapViewHideLoading(vizid);
                                        window.parent.placeTypeLimitReached(vizid);
                                    } else if (data.trackTypeLimitReached) {
                                        window.parent.hideSpinner(vizid);
                                        window.parent.mapViewHideLoading(vizid);
                                        window.parent.trackTypeLimitReached(vizid);
                                    } else {
                                        var xmin;
                                        var xmax;
                                        var ymin;
                                        var ymax;
                                        var wkid = 4326;
                                        if (data.newExtent) {
                                            xmin = data.initialExtent.xmin;
                                            xmax = data.initialExtent.xmax;
                                            ymin = data.initialExtent.ymin;
                                            ymax = data.initialExtent.ymax;
                                            //wkid = data.initialExtent.spatialReference.wkid;
                                            zoom = data.initialExtent.zoom;
                                        } else {
                                            xmin = data.extent.xmin;
                                            xmax = data.extent.xmax;
                                            ymin = data.extent.ymin;
                                            ymax = data.extent.ymax;
                                            //wkid = data.extent.spatialReference.wkid;
                                            zoom = data.extent.zoom;
					    isNewExtent = false;
                                        }
                                        if (wkid == 4326) {
                                            var spatialReference = new SpatialReference();
                                            spatialReference.wkid = wkid;
                                            extent = new Extent(xmin,ymin,xmax,ymax,spatialReference);
                                            createAndUpdateMap(++sequenceNumber, extent, zoom, isNewExtent);
                                        } else {
                                            var geometryService = new GeometryService();
                                            var outputpoints = [];
                                            outputpoints[0] = geometryService.project(xmin, ymin);
                                            outputpoints[1] = geometryService.project(xmax, ymax);
                                            var x0 = outputpoints[0].x;
                                            var x1 = outputpoints[1].x;
                                            var minx;
                                            var maxx;
                                            if (x0 > x1) {
                                                maxx = x0;
                                                minx = x1;
                                            } else {
                                                minx = x0;
                                                maxx = x1;
                                            }
                                            var miny = outputpoints[0].y;
                                            var maxy = outputpoints[1].y;
                                            var spatialReference = new SpatialReference();
                                            spatialReference.wkid = wkid;
                                            extent = new Extent(minx,miny,maxx,maxy,outSR);
                                            createAndUpdateMap(++sequenceNumber, extent, zoom, isNewExtent);
                                        }
                                    }
                                } else {
                                    createAndUpdateMap(++sequenceNumber, extent, zoom, isNewExtent);
                                }
                            }
                        });
                    }
                    xhr.get({
                        url: "rest/map/getSequenceNumber/" + qs("dv_uuid") + "/" + qs("viz_uuid"),
                        handleAs: "json",
                        load: function(sequenceNumber) {
                            getExtentInfo(++sequenceNumber);
                        }
                    });
                });
            }
        </script>
    </head>
    <body class="tundra">
        <div id="mapContainer" style="position: absolute; overflow: hidden; left: 0px; top: 0px; right: 0px; bottom: 0px;">
            <div id="searchDiv" style="display:none"></div>
            <div id="mapDivContainer">
                <div id="mapDiv">
		</div>
            </div>
            <div id="toolStrip" style="position: absolute; left: 0px; top: 0px; display:block; overflow: hidden; zoom: 1; color: #333333; visibility: hidden;">
                <div style="position: relative; left: 0; top: 0; display: block; overflow: hidden; width: 26px; height: 128px;">
                    <div style="position: absolute !important; left: 0px; top: 0px; display: inline-block; margin: 0px;">
                        <a id="homeButton" href="javascript:;" class="btn rightControlButton" title="<%= homeText %>" style="width:12; height:13">
                            <i class="icon-home"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div id="cmenu" class='popupContent' style="display:none;">
                <div>
                    <ul class='dropdown-menu' style='display: block;'>
                        <li style="display:none;"><a id="select"><%= selectText %></a></li>
                        <li style="display:none;"><a id="deselect"><%= deselectText %></a></li>
                        <li style="display:none;"><a id="selectAll"><%= selectAllText %></a></li>
                        <li style="display:none;"><a id="deselectAll"><%= deselectAllText %></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </body>
</html>
