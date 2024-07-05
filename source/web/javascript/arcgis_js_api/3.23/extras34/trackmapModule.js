define([
	// reference
	"dojo/_base/declare",
	"dojo/dom",
	"dojo/on",
	"extras34/commonModule",
	"extras34/dataFunction",
	"extras34/extentFunction",
	"extras34/searchFunction",
	"esri/Color",
	"esri/graphic",
	"esri/layers/GraphicsLayer",
	"esri/map",
	"esri/tasks/FeatureSet",
	"esri/tasks/query",
	"esri/SpatialReference",
	"esri/geometry/Extent",
	"dojo/_base/xhr",
	"esri/geometry/Point",
	"esri/geometry/ScreenPoint",
	"esri/layers/ArcGISTiledMapServiceLayer",
	"esri/layers/WMSLayer",
	"esri/layers/WMSLayerInfo",
	"esri/layers/OpenStreetMapLayer",
	"dojo/dom-class",
	"dojo/dom-style",
	"dijit/popup",
	"dijit/TooltipDialog",
	"extras34/symbolFunction",
	"extras34/queryFunction",
	"esri/renderers/ClassBreaksRenderer",
	"esri/renderers/SimpleRenderer",
	"esri/symbols/Font",
	"esri/symbols/SimpleLineSymbol",
	"esri/symbols/SimpleMarkerSymbol",
	"esri/symbols/TextSymbol",
	"esri/geometry/Polyline",
	"extras34/geometryService"
], function(
	// parameter
	declare,
	dom,
	on,
	commonModule,
	DataFunction,
	ExtentFunction,
	SearchFunction,
	Color,
	Graphic,
	GraphicsLayer,
	Map,
	FeatureSet,
	Query,
	SpatialReference,
	Extent,
	xhr,
	Point,
	ScreenPoint,
	ArcGISTiledMapServiceLayer,
	WMSLayer,
	WMSLayerInfo,
	OpenStreetMapLayer,
	domClass,
	domStyle,
	dijitPopup,
	TooltipDialog,
	SymbolFunction,
	QueryFunction,
	ClassBreaksRenderer,
	SimpleRenderer,
	Font,
	SimpleLineSymbol,
	SimpleMarkerSymbol,
	TextSymbol,
	Polyline,
	GeometryService
) {
    // body
    var map;
    var searchFunction;
    var dvid;
    var vizid;
    var extentFunction;
    var dataFunction;
    var homeButton;
    var renderer;
    var zooming = false;
    var panning = false;
    var frontendZoomThreshold;
    var itemsInViz;
    var mapPanAndZoomInitialized = false;
    var sequenceNumber = null;
    var i18nType = null;


    function getNextSequenceNumber() {
        return ++sequenceNumber;
    }

    function initMap() {
        map.on("load", function(evt) {
            map.graphics.enableMouseEvents();
            dataFunction.createToolbar();
            initDialog();
        });
        map.on("mouse-move", function(evt) {
            if (evt.shiftKey && !evt.ctrlKey) {
                window.parent.desensitizeFloatingObjects(vizid);
            }
            mousePoint = evt.mapPoint;
            if (dialogShown) {
                var doClearHover = false;
                if (typeof currentDisplayPoint != 'undefined' && currentDisplayPoint !== null) {
                    if (!queryFunction.arePointsWithinPixels(currentDisplayPoint, mousePoint, maxPlaceSize)) {
                        doClearHover = true;
                    }
                }
                if (typeof dialogOnGeom != 'undefined' && dialogOnGeom !== null) {
                    if (!queryFunction.arePointsWithinPixels(dialogOnGeom, mousePoint, currentSizes[dialogOnFeature.attributes.size - 1])) {
                        doClearHover = true;
                    }
                }
                if (doClearHover) {
                    dontclosedialog = false;
                    clearHover(null);
                }
            }
            var selectionMode = dataFunction.getSelectionMode();
            if (selectionMode !== "pan" || dataFunction.isToolbarOn()) {
                map.isPan && map.disablePan();
                map.isRubberBandZoom && map.disableRubberBandZoom();
                map.setMapCursor("crosshair");
            } else {
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
                map.setMapCursor("default");
            }
        });
        map.on("click", function(evt) {
            var selectPoint = false;
            if (dataFunction.isToolbarOn()) {
                dataFunction.deactivateToolbar();
                selectPoint = true;
            }
            window.parent.showHeader(vizid);
            window.parent.hideMenu(vizid);
            if (evt.ctrlKey === true || evt.metaKey === true || selectPoint) {
                var mapPoint = evt.mapPoint;
                mousePoint = mapPoint;
                currentDisplayPoint = mapPoint;
                currentQueryPoint1 = mapPoint;
                var screenPoint = map.toScreen(mapPoint);
                var extent = queryFunction.getExtent(screenPoint, maxPlaceSize);
                var points = getPointsIntersected(extent);
                var links = toggleLinkFeatures(mapPoint);
                var response = {
                    "features": points,
                    "links": links
                };
                toggleClickBuffer(response);
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
            }
        });
        map.on("mouse-over", function(evt) {
            if (!document.hasFocus()) {
                window.focus();
            }
        });
        map.on("mouse-out", function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            var mapPoint = evt.mapPoint;
            if (typeof (mapPoint.x) != undefined && mapPoint.x != null && typeof (mapPoint.y) != undefined && mapPoint.y != null) {
                var screenPoint = map.toScreen(mapPoint);
                if (screenPoint.x < 0 || screenPoint.x > map.width || screenPoint.y < 0 || screenPoint.y > map.height) {
                    inDialog = false;
                    dontclosedialog = false;
                    clearHover();
                    dataFunction.clearRubberband();
                    if (dataFunction.isToolbarOn()) {
                        dataFunction.deactivateToolbar();
                    }
                }
            }
            if (document.hasFocus()) {
                window.blur();
            }
        });
        map.on("mouse-drag-start", function(evt) {
            var selectionMode = dataFunction.getSelectionMode();
            if ((selectionMode !== "pan") && !dataFunction.isToolbarOn() && !dataFunction.isProcessingSelection()) {
                map.isPan && map.disablePan();
                map.isRubberBandZoom && map.disableRubberBandZoom();
                dataFunction.setRubberbandStartPoint(evt);
                map.setMapCursor("crosshair");
            } else {
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
                map.setMapCursor("move");
            }
        });
        map.on("mouse-drag", function(evt) {
            var selectionMode = dataFunction.getSelectionMode();
            if ((selectionMode !== "pan") && dataFunction.isRubberbandStarted()) {
                dataFunction.drawRubberband(evt);
                map.setMapCursor("crosshair");
            }
        });
        var mapMouseDragEndHandler = function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            var selectionMode = dataFunction.getSelectionMode();
            if ((selectionMode !== "pan") && dataFunction.isRubberbandStarted()) {
                dataFunction.drawRubberband(evt);
                map.setMapCursor("crosshair");
                if (evt.shiftKey) {
                    dataFunction.deselectByRubberband();
                } else {
                    dataFunction.selectByRubberband();
                }
            } else if (panning) {
                map.setMapCursor("move");
                if (panEndEventHandler == null) {
                    panning = false;
                } else {
                    panEndEventHandler();
                }
            }
        };
        map.on("mouse-drag-end", function(evt) {
            mapMouseDragEndHandler(evt);
            dataFunction.clearRubberband();
        });
        map.on("mouse-down", function(evt) {
            var selectionMode = dataFunction.getSelectionMode();
            if (selectionMode !== "pan") {
                map.isPan && map.disablePan();
                map.isRubberBandZoom && map.disableRubberBandZoom();
            }
        });
        map.on("mouse-up", function(evt) {
            mapMouseDragEndHandler(evt);
            !map.isPan && map.enablePan();
            !map.isRubberBandZoom && map.enableRubberBandZoom();
        });
        map.on("key-down", function(evt) {
            if (evt.ctrlKey || evt.keyCode == 17) {
                map.isPan && map.disablePan();
                map.isRubberBandZoom && map.disableRubberBandZoom();
            }
        });
        map.on("key-up", function(evt) {
            if (evt.ctrlKey || evt.keyCode == 17) {
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
                dataFunction.clearRubberband();
            }
        });
    }

    function getPointsIntersected(extent) {
        var pointsToManipulate = [];
        var featureHash = dataFunction.getFeatureHash();
        var spatialReference = new SpatialReference({
            wkid: 4326
        });
        Object.keys(featureHash).forEach(function(objectId) {
            var feature0 = featureHash[objectId];
            var isAMapPoint = 'spatialReference' in feature0.geometry;
            if (!isAMapPoint) {
                var geometry = new Point(feature0.geometry.x,feature0.geometry.y,spatialReference);
                feature0.geometry = geometry;
            }
            if (extent.intersects(feature0.geometry)) {
                pointsToManipulate.push(feature0);
            }
        });
        return pointsToManipulate;
    }

    function toggleLinkFeatures(sourcePoint) {
        var linksToToggle = [];
        var screenPoint = map.toScreen(sourcePoint);
        var x = screenPoint.x;
        var y = screenPoint.y;
        var featureHash = dataFunction.getFeatureHash();
        var linkHash = dataFunction.getLinkHash();
        Object.keys(linkHash).forEach(function(typeId) {
            var width = symbolFunction.getLineSymbolWidth(typeId);
            links = linkHash[typeId];
            links.forEach(function(pair) {
                var feature0 = featureHash[pair[0]];
                var feature1 = featureHash[pair[1]];
                if (isHit(x, y, feature0, feature1, width)) {
                    linksToToggle.push(feature0.geometry.x);
                    linksToToggle.push(feature0.geometry.y);
                    linksToToggle.push(feature1.geometry.x);
                    linksToToggle.push(feature1.geometry.y);
                }
            });
        });
        return linksToToggle;
    }

    function isHit(x, y, feature0, feature1, lineWidth) {
        var item0 = getVisualItem(feature0);
        var item1 = getVisualItem(feature1);
        var padding = 2;

        if (item0 == null || item1 == null || !inBounds(x, y, item0, item1, lineWidth, padding)) {
            return false;
        }

        var halfWidth = lineWidth / 2.0 + padding;
        var distance = distanceFromEdge(x, y, item0, item1);
        return halfWidth > distance;
    }

    function getVisualItem(feature) {
        if (typeof (feature) == "undefined" || feature == null)
            return null;
        var spatialReference = new SpatialReference({
            wkid: 4326
        });
        var mapPoint = new Point(feature.geometry.x,feature.geometry.y,spatialReference);
        var screenPoint = map.toScreen(mapPoint);
        return screenPoint;
    }

    function inBounds(x, y, item0, item1, lineWidth, padding) {
        var x0 = item0.x;
        var y0 = item0.y;
        var x1 = item1.x;
        var y1 = item1.y;
        var halfWidth = lineWidth / 2.0 + padding;
        var out = x + halfWidth < Math.min(x0, x1) || x - halfWidth > Math.max(x0, x1) || y + halfWidth < Math.min(y0, y1) || y - halfWidth > Math.max(y0, y1);
        if (!out) {
            //must be either strictly in x or strictly in y.
            out = !((x > Math.min(x0, x1) && x < Math.max(x0, x1)) || (y > Math.min(y0, y1) && y < Math.max(y0, y1)));
        }
        return !out;
    }

    function distanceFromEdge(x, y, item0, item1) {
        var x0 = item0.x;
        var y0 = item0.y;
        var x1 = item1.x;
        var y1 = item1.y;
        if (x1 - x0 == 0) {
            //prevent divide by zero
            return Math.abs(x1 - x);
        }
        var m = (y1 - y0) / (x1 - x0);
        var a = -m;
        var b = 1;
        var c = -y0 + m * x0;
        return Math.abs(a * x + b * y + c) / Math.sqrt(a * a + b * b);
    }

    function addLayersAbove() {
        var i = 0;
        while (i >= 0) {
            if (layerInfos[i].urlaccessible) {
                if (layerInfos[i].key == "") {
                    if (layerInfos[i].url != null && layerInfos[i].url != "" && layerInfos[i].visible) {
                        var tileLayer;
                        if (layerInfos[i].type == "ArcGISTiled") {
                            tileLayer = new ArcGISTiledMapServiceLayer(layerInfos[i].url,{
                                opacity: layerInfos[i].opacity
                            });
                        } else if (layerInfos[i].type == "OpenStreetMap") {
                            tileLayer = new OpenStreetMapLayer({
                                opacity: layerInfos[i].opacity
                            });
                        } else {
                            var layer1 = new WMSLayerInfo({
                                name: layerInfos[i].layername,
                                title: layerInfos[i].layername
                            });
                            var resourceInfo = {
                                extent: new Extent(-180,-90,180,90,{
                                    wkid: 4326
                                }),
                                layerInfos: [layer1]
                            };
                            tileLayer = new WMSLayer(layerInfos[i].url,{
                                resourceInfo: resourceInfo,
                                visibleLayers: [layerInfos[i].layername]
                            });
                        }
                        map.addLayer(tileLayer);
                    }
                }
            }
            i--;
        }
    }

    function addLayersBelow() {
        var i = layerInfos.length - 1;
        while (i >= 0) {
            if (layerInfos[i].urlaccessible) {
                if (layerInfos[i].key == "") {
                    if (layerInfos[i].url != null && layerInfos[i].url != "" && layerInfos[i].visible && layerInfos[i].urlaccessible) {
                        var tileLayer;
                        if (layerInfos[i].type == "ArcGISTiled") {
                            tileLayer = new ArcGISTiledMapServiceLayer(layerInfos[i].url,{
                                opacity: layerInfos[i].opacity
                            });
                        } else if (layerInfos[i].type == "OpenStreetMap") {
                            tileLayer = new OpenStreetMapLayer({
                                opacity: layerInfos[i].opacity
                            });
                        } else {
                            var layer1 = new WMSLayerInfo({
                                name: layerInfos[i].layername,
                                title: layerInfos[i].layername
                            });
                            var resourceInfo = {
                                extent: new Extent(-180,-90,180,90,{
                                    wkid: 4326
                                }),
                                layerInfos: [layer1]
                            };
                            tileLayer = new WMSLayer(layerInfos[i].url,{
                                resourceInfo: resourceInfo,
                                visibleLayers: [layerInfos[i].layername]
                            });
                        }
                        map.addLayer(tileLayer);
                    }
                } else {
                    if (layerInfos[i].visible && layerInfos[i].urlaccessible) {
                        var key = layerInfos[i].key;
                        var url = null;
                        var mapType = "ArcGISTiledMapServiceLayer";
                        if (key == "dark-gray") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer";
                        } else if (key == "gray") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer";
                        } else if (key == "hybrid") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";
                        } else if (key == "national-geographic") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer";
                        } else if (key == "oceans") {
                            url = "https://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Base/MapServer";
                        } else if (key == "streets") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
                        } else if (key == "terrain") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer";
                        } else if (key == "topo") {
                            url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
                        }
                        if (url != null && mapType == "ArcGISTiledMapServiceLayer") {
                            tileLayer = new ArcGISTiledMapServiceLayer(url,{
                                opacity: layerInfos[i].opacity
                            });
                            map.addLayer(tileLayer);
                        }
                    }
                }
            }
            i--;
        }
    }
    var panEndEventHandler;
    var panExtent = null;
    var oldPanCenter = null;

    function initializeMapPanAndZoom() {
        map.on('pan-start', function(evt) {
            panning = true;
        });
        map.on('pan', function(evt) {
            panExtent = evt.extent;
        });
        map.on('pan-end', function(evt) {
            panExtent = evt.extent;
        });
        panEndEventHandler = function() {
            zoom_sequencenumber = getNextSequenceNumber();
            handleDragEndOrZoomEnd(zoom_sequencenumber, panEndFunction);
        }
        map.on('zoom-start', function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            zooming = true;
            map.removeAllLayers();
            window.setTimeout(function() {
                if (zooming) {
                    window.parent.mapViewShowLoading(vizid);
                    map.disableMapNavigation();
                }
            }, 150);
        });
        map.on('zoom-end', function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            zooming = false;
            window.parent.mapViewHideLoading(vizid);
            map.enableMapNavigation();
            map.disableClickRecenter();
            map.disableDoubleClickZoom();
            map.disableKeyboardNavigation();
            zoom_sequencenumber = getNextSequenceNumber();
            handleDragEndOrZoomEnd2(zoom_sequencenumber, zoomEndFunction, evt);
        });
        mapPanAndZoomInitialized = true;
    }
    var zoom_sequencenumber;

    function handleDragEndOrZoomEnd(zsn, callback) {
        setTimeout(function() {
            if (zsn == zoom_sequencenumber) {
                callback();
            }
        }, 250);
    }

    function handleDragEndOrZoomEnd2(zsn, callback, evt) {
        setTimeout(function() {
            if (zsn == zoom_sequencenumber) {
                callback(evt);
            }
        }, 250);
    }
    var ignoreDragThreshold = 12;

    function panEndFunction() {
        window.setTimeout(function() {
            var ignore = true;
            var newPanCenter = map.extent.getCenter();
            var oldScreenPoint = map.toScreen(oldPanCenter);
            if (oldScreenPoint != null) {
                newScreenPoint = map.toScreen(newPanCenter);
                if (extentFunction.isPaddingsNotZero()) {
                    ignore = false;
                    panEnd();
                } else if (ignoreDragThreshold < Math.abs(oldScreenPoint.x - newScreenPoint.x) || ignoreDragThreshold < Math.abs(oldScreenPoint.y - newScreenPoint.y)) {
                    ignore = false;
                    panEnd();
                } else {
                    extentFunction.ignoreDragThreshold();
                    panEnd(false);
                }
            }
            if (!ignore) {
                window.parent.showSpinner(vizid);
                if (placesLayer !== null) {
                    refreshAndUpdateMap(panExtent, map.getZoom());
                } else {
                    getUpdate2();
                }
            }
            panExtent = null;
        }, 150);
    }

    function zoomEndFunction(evt) {
        var paddingLeftStr = mapDivContainer.style.paddingLeft;
        var paddingRightStr = mapDivContainer.style.paddingRight;
        var xmin = evt.extent.xmin;
        var ymin = evt.extent.ymin;
        var xmax = evt.extent.xmax;
        var ymax = evt.extent.ymax;
        var x1 = xmax - xmin;
        var y1 = ymax - ymin;
        var oldZoom = map.getZoom();
        var zoom = oldZoom;
        if (zoom == -1) {
            zoom = extentFunction.calculateSnugZoom(x1, y1, map.extent.spatialReference.wkid);
        } else {
            zoom = extentFunction.adjustZoomToNewSize(zoom);
        }
        extentFunction.setPaddingsUsingZoom(zoom);
        var extent = extentFunction.getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, map.extent.spatialReference.wkid);
        var newpaddingLeftStr = extentFunction.getNewPaddingLeft() + "px";
        var newpaddingRightStr = extentFunction.getNewPaddingRight() + "px";
        if (paddingLeftStr != newpaddingLeftStr || paddingRightStr != newpaddingRightStr) {
            extentFunction.clearMapContainerDivPadding();
            if (extentFunction.isPaddingsNotZero()) {
                extentFunction.setMapDivContainerToNewPaddings();
            }
            refreshMap(extent, zoom, f1callback1);
        } else if (oldZoom != zoom) {
            refreshMap(extent, zoom, f1callback1);
        } else {
            zoomEnd();
            summaryRefreshMap(zoom);
        }
    }

    function f1callback1() {
        map.reposition();
        zoomEnd();
        getUpdate2();
    }
    var refreshingMap = false;

    function refreshMap(extent, zoom, callback) {
        if (!refreshingMap) {
            refreshingMap = true;
            var mapId = map.id;
            if (typeof extent == 'undefined' || extent == null) {
                extent = map.extent;
            }

            if (extent.spatialReference.wkid == 102100) {
                var xMin2D = extentFunction.getXMin2D(102100);
                var xMax2D = extentFunction.getXMax2D(102100);
                var wholeWorldX = extentFunction.getWholeWorldX(102100);
                if (extent.xmin < xMin2D && extent.xmax > xMax2D) {
                    extent.xmin = xMin2D;
                    extent.xmax = xMax2D;
                } else {
                    while (extent.xmin < xMin2D - wholeWorldX) {
                        extent.xmin += wholeWorldX;
                        extent.xmax += wholeWorldX;
                    }
                    if (extent.xmax < xMin2D) {
                        extent.xmin += wholeWorldX;
                        extent.xmax += wholeWorldX;
                    }
                    while (extent.xmax > xMax2D) {
                        extent.xmin -= wholeWorldX;
                        extent.xmax -= wholeWorldX;
                    }
                }
            }

            var mapOptions = {
                slider: false,
                showAttribution: false,
                displayGraphicsOnPan: false,
                fadeOnZoom: true,
                basemap: map.getBasemap(),
                extent: extent
            };
            map.destroy();
            map = null;
            emptyLayer = null;
            associationsLayer = null;
            placesLayer = null;
            map = new Map(mapId,mapOptions);
            map.on("load", function(evt) {
                window.parent.mapRenderComplete(vizid);
            });
            map.autoResize = false;
            map.disableClickRecenter();
            map.disableDoubleClickZoom();
            map.disableKeyboardNavigation();
            panEndEventHandler = null;
            dataFunction.setMap(map);
            dataFunction.setBasemap(basemapId, basemapUrl, basemapType, basemapOpacity);
            extentFunction.setMap(map);
            searchFunction.setMap(map);
            queryFunction.setMap(map);
            initMap();
            addLayersBelow();
            mapPanAndZoomInitialized = false;
            if (typeof zoom != 'undefined' && zoom !== null && zoom != -1) {
                var deferred = map.setZoom(zoom);
                deferred.then(extentFunction.saveExtentAndZoom2(zoom, callback));
            } else {
                extentFunction.saveExtentAndZoom(callback);
            }
            return true;
        } else {
            return false;
        }
    }

    function refreshAndUpdateMap(extent, zoom) {
        refreshMap(extent, zoom, function() {
            getUpdate2(zoom);
        });
    }

    function initDocument() {
        document.addEventListener("contextmenu", function(evt) {
            evt.preventDefault();
            var screenPoint = new ScreenPoint(evt.clientX - extentFunction.getNewPaddingLeft(),evt.clientY);
            var mapPoint = map.toMap(screenPoint);
            currentDisplayPoint = mapPoint;
            currentQueryPoint1 = mapPoint;
            var extent = queryFunction.getExtent(screenPoint, maxPlaceSize);
            var points = getPointsIntersected(extent);
            var response = {
                "features": points
            };
            featureLayerContextMenu(response);
            return false;
        });
    }

    function select() {
        var geometry = dataFunction.getSelectionGeometry().getExtent();
        var points = getPointsIntersected(geometry);
        var links = getLinksToManipulate(geometry);
        var response = {
            "features": points,
            "links": links
        };
        dataFunction.selectInBuffer(response);
    }

    function getLinksToManipulate(extent) {
        var linksToManipulate = [];
        var featureHash = dataFunction.getFeatureHash();
        var linkHash = dataFunction.getLinkHash();
        var spatialReference = new SpatialReference({
            wkid: 4326
        });
        Object.keys(linkHash).forEach(function(typeId) {
            var width = symbolFunction.getLineSymbolWidth(typeId);
            links = linkHash[typeId];
            links.forEach(function(pair) {
                var feature0 = featureHash[pair[0]];
                var feature1 = featureHash[pair[1]];
                var polyline = new Polyline(spatialReference);
                polyline.addPath([[feature0.geometry.x, feature0.geometry.y], [feature1.geometry.x, feature1.geometry.y]]);
                if (extent.intersects(polyline)) {
                    linksToManipulate.push(feature0.geometry.x);
                    linksToManipulate.push(feature0.geometry.y);
                    linksToManipulate.push(feature1.geometry.x);
                    linksToManipulate.push(feature1.geometry.y);
                }
            });
        });
        return linksToManipulate;
    }

    function toggle(inBuffer, turnOn) {
        if (summaryLevel != detailLevel) {
            var k = 0;
            var featureHash = dataFunction.getFeatureHash();
            var changeToDeselectedKeys = [];
            var changeToSelectedKeys = [];
            while (k < inBuffer.length) {
                var key = inBuffer[k];
                var f = featureHash[key];
                var g = placeGraphics[key];
                if (f) {
                    if (turnOn === null) {
                        if (f.attributes.hasOwnProperty("selected") && f.attributes.selected) {
                            f.attributes.selected = false;
                            g.attributes.selected = false;
                            changeToDeselectedKeys.push(key);
                        } else {
                            f.attributes.selected = true;
                            g.attributes.selected = true;
                            changeToSelectedKeys.push(key);
                        }
                    } else if (turnOn) {
                        f.attributes.selected = true;
                        g.attributes.selected = true;
                        changeToSelectedKeys.push(key);
                    } else {
                        f.attributes.selected = false;
                        g.attributes.selected = false;
                        changeToDeselectedKeys.push(key);
                    }
                }
                k++;
            }
            redraw(changeToDeselectedKeys, changeToSelectedKeys);
            dataFunction.applyEditCallback();
        }
    }

    function setSelected(inBuffer) {
        var featureHash = dataFunction.getFeatureHash();
        var oldBuffer = Object.keys(featureHash);
        var changeToDeselectedKeys = [];
        var changeToSelectedKeys = [];
        var i = 0;
        while (i < oldBuffer.length) {
            var key = oldBuffer[i];
            var f = featureHash[key];
            var g = placeGraphics[key];
            f.attributes.selected = false;
            g.attributes.selected = false;
            changeToDeselectedKeys.push(key);
            i++;
        }
        var j = 0;
        while (j < inBuffer.length) {
            var key = inBuffer[j];
            var f = featureHash[key];
            var g = placeGraphics[key];
            if (f) {
                f.attributes.selected = true;
                g.attributes.selected = true;
                changeToSelectedKeys.push(key);
            }
            j++;
        }
        redraw(changeToDeselectedKeys, changeToSelectedKeys)
        dataFunction.applyEditCallback();
    }
    var isMapHidden = false;
    function panEnd(save) {
        if (save === undefined || save) {
            extentFunction.saveExtentAndZoom();
        }
        panning = false;
    }

    function zoomEnd() {
        extentFunction.saveExtentAndZoom();
        window.parent.mapViewHideLoading(vizid);
        map.enableMapNavigation();
        map.disableClickRecenter();
        map.disableDoubleClickZoom();
        map.disableKeyboardNavigation();
        zooming = false;
    }

    function checkGetUpdate2() {
        if (!refreshingMap && !extentFunction.isPaddingsNotZero()) {
            if (placesLayer !== null) {
                refreshAndUpdateMap(panExtent, map.getZoom());
            } else {
                getUpdate2();
            }
        }
    }
    var emptyLayer = null;
    var associationsLayer = null;
    var placesLayer = null;
    var oldAssociationsLayer = null;
    var oldPlacesLayer = null;
    var isResizeHappend = false;
    var doRefreshMap = false;
    var summaryLevel;
    var useMultitypeDecorator = false;
    var useLinkupDecorator = false;
    var cacheNotAvailable = false;
    var basemapId;
    var basemapUrl;
    var basemapType;
    var basemapOpacity;
    var layerInfos;

    function useData(jsonFS) {
        if (jsonFS == null || jsonFS.deferToNewCache) {
            cacheNotAvailable = true;
            map.enableMapNavigation();
            map.disableClickRecenter();
            map.disableDoubleClickZoom();
            map.disableKeyboardNavigation();
            isMapHidden = false;
            if (!mapPanAndZoomInitialized) {
                initializeMapPanAndZoom();
            }
            return;
        } else {
            cacheNotAvailable = jsonFS.cacheNotAvailable;
            if (sequenceNumber == null || sequenceNumber < jsonFS.sequenceNumber) {
                sequenceNumber = jsonFS.sequenceNumber;
            }
            summaryLevel = jsonFS.summaryLevel;
            useMultitypeDecorator = jsonFS.useMultitypeDecorator;
            useLinkupDecorator = jsonFS.useLinkupDecorator;
            window.parent.setUseSummary(vizid, summaryLevel != detailLevel);
            if (jsonFS.pointLimitReached) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                window.parent.pointLimitReached(vizid);
                isMapHidden = true;
                refreshingMap = false;
                dataFunction.setLoading(false);
                return;
            } else if (jsonFS.placeTypeLimitReached) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                window.parent.placeTypeLimitReached(vizid);
                isMapHidden = true;
                refreshingMap = false;
                dataFunction.setLoading(false);
                return;
            } else if (jsonFS.trackTypeLimitReached) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                window.parent.trackTypeLimitReached(vizid);
                isMapHidden = true;
                refreshingMap = false;
                dataFunction.setLoading(false);
                return;
            } else if (jsonFS.cacheNotAvailable) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                map.enableMapNavigation();
                map.disableClickRecenter();
                map.disableDoubleClickZoom();
                map.disableKeyboardNavigation();
                isMapHidden = false;
                if (!mapPanAndZoomInitialized) {
                    initializeMapPanAndZoom();
                }
                refreshingMap = false;
                dataFunction.setLoading(false);
                setTimeout(function() {
                    if (cacheNotAvailable) {
                        if (mapDivContainer.offsetWidth == 0) {
                            needReload = true;
                        } else {
                            window.parent.mapViewShowLoading(vizid);
                            map.disableMapNavigation();
                            getUpdate2();
                            needReload = false;
                        }
                    }
                }, 250);
                return;
            } else if (jsonFS.noData) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                window.parent.noData(vizid);
                isMapHidden = true;
                refreshingMap = false;
                dataFunction.setLoading(false);
                return;
            } else {
                if (isResizeHappend) {
                    isResizeHappend = false;
                    refreshingMap = false;
                    doRefreshMap = true;
                    dataFunction.setLoading(false);
                    extentFunction.setMapExtentInfo(jsonFS.extentInfo.newExtent, jsonFS.extentInfo.initialExtent, jsonFS.extentInfo.extent);
                    window.parent.processResize(vizid);
                } else {
                    window.parent.hideSpinner(vizid);
                    window.parent.mapViewHideLoading(vizid);
                    map.enableMapNavigation();
                    map.disableClickRecenter();
                    map.disableDoubleClickZoom();
                    map.disableKeyboardNavigation();
                    itemsInViz = jsonFS.itemsInViz;
                    window.parent.hasData(vizid);
                    isMapHidden = false;
                    if (itemsInViz > 1000) {
                        window.parent.mapViewShowLoading(vizid);
                        map.disableMapNavigation();
                    }
                    extentFunction.setMapExtentInfo(jsonFS.extentInfo.newExtent, jsonFS.extentInfo.initialExtent, jsonFS.extentInfo.extent);
                    if (emptyLayer == null) {
                        emptyLayer = new GraphicsLayer();
                        map.addLayer(emptyLayer);
                    }
                    if (associationsLayer !== null) {
                        oldAssociationsLayer = associationsLayer;
                        associationsLayer = null;
                    }
                    if (placesLayer !== null) {
                        oldPlacesLayer = placesLayer;
                        placesLayer = null;
                    }
                    initializeValues(jsonFS);
                    setAssociationLayer(jsonFS);
                    setPlacesLayer(jsonFS);
                    if (!mapPanAndZoomInitialized) {
                        initializeMapPanAndZoom();
                    }
                    if (updateLegend) {
                        window.parent.loadLegend(vizid, sequenceNumber);
                    } else {
                        updateLegend = true;
                    }
                    refreshingMap = false;
                    oldPanCenter = map.extent.getCenter();
                }
            }
        }
    }

    function initializeValues(jsonFS) {
        symbolFunction.initialize(jsonFS);
        var diff = maxPlaceSize - minPlaceSize;
        var increment = diff / 4;
        currentSizes = [];
        var currentSize = minPlaceSize;
        currentSizes.push(currentSize);
        currentSize += increment;
        currentSizes.push(currentSize);
        currentSize += increment;
        currentSizes.push(currentSize);
        currentSize += increment;
        currentSizes.push(currentSize);
        currentSize += increment;
        currentSizes.push(currentSize);
        window.parent.hideBackButton(vizid);
        var defaultSym = new SimpleMarkerSymbol().setSize(4);
        renderer = new ClassBreaksRenderer(defaultSym,function(value) {
            var attributes = value.attributes;
            var classBreak = attributes.size * 10;
            classBreak += 100 * attributes.typeId;
            return classBreak;
        });
        var typeSymbols = symbolFunction.getTypeSymbols();
        for (var i = 0, keys = Object.keys(typeSymbols), ii = keys.length; i < ii; i++) {
            var typeId = keys[i];
            if (typeSymbols.hasOwnProperty(typeId)) {
                var smsStyle;
                var shape = typeSymbols[typeId].shape;
                if (shape == "Diamond") {
                    smsStyle = SimpleMarkerSymbol.STYLE_DIAMOND;
                } else if (shape == "Square") {
                    smsStyle = SimpleMarkerSymbol.STYLE_SQUARE;
                } else {
                    smsStyle = SimpleMarkerSymbol.STYLE_CIRCLE;
                }
                var smsColor = Color.fromHex("#000000");
                smsColor.a = 0;
                var defaultOutline = new SimpleLineSymbol(symbolFunction.getSlsStyle(),smsColor,symbolFunction.getSlsSize());
                currentSize = currentSizes[0];
                var default1 = new SimpleMarkerSymbol(smsStyle,currentSize,defaultOutline,smsColor);
                currentSize = currentSizes[1];
                var default2 = new SimpleMarkerSymbol(smsStyle,currentSize,defaultOutline,smsColor);
                currentSize = currentSizes[2];
                var default3 = new SimpleMarkerSymbol(smsStyle,currentSize,defaultOutline,smsColor);
                currentSize = currentSizes[3];
                var default4 = new SimpleMarkerSymbol(smsStyle,currentSize,defaultOutline,smsColor);
                currentSize = currentSizes[4];
                var default5 = new SimpleMarkerSymbol(smsStyle,currentSize,defaultOutline,smsColor);
                var typeIdDistinguishingNumber = 100 * typeSymbols[typeId].id;
                renderer.addBreak(typeIdDistinguishingNumber + 1, typeIdDistinguishingNumber + 10, default1);
                renderer.addBreak(typeIdDistinguishingNumber + 11, typeIdDistinguishingNumber + 20, default2);
                renderer.addBreak(typeIdDistinguishingNumber + 21, typeIdDistinguishingNumber + 30, default3);
                renderer.addBreak(typeIdDistinguishingNumber + 31, typeIdDistinguishingNumber + 40, default4);
                renderer.addBreak(typeIdDistinguishingNumber + 41, typeIdDistinguishingNumber + 50, default5);
            }
        }
        dataFunction.newFeatureHash();
        dataFunction.newLinkHash();
        var featureHash = dataFunction.getFeatureHash();
        calculateIXMinAndIXMax();
        var i = 0;
        while (i < jsonFS.layer.features.length) {
            var f = jsonFS.layer.features[i];
            if (f.geometry.x > iXMax) {
                f.geometry.x -= 360;
            } else if (f.geometry.x < iXMin) {
                f.geometry.x += 360;
            }
            var k = f.attributes.objectID;
            featureHash[k] = f;
            featureHash[k].attributes.selected = false;
            featureHash[k].attributes.combined = false;
            featureHash[k].attributes.new = false;
            featureHash[k].attributes.updated = false;
            featureHash[k].geometry = new Point(f.geometry.x,f.geometry.y,new SpatialReference({
                wkid: 4326
            }));
            i++;
        }
        i = 0;
        while (i < jsonFS.layer.selectedFeatures.length) {
            var k = jsonFS.layer.selectedFeatures[i];
            featureHash[k].attributes.selected = true;
            i++;
        }
        i = 0;
        while (i < jsonFS.layer.combinedFeatures.length) {
            var k = jsonFS.layer.combinedFeatures[i];
            featureHash[k].attributes.combined = true;
            i++;
        }
        i = 0;
        while (i < jsonFS.layer.newFeatures.length) {
            var k = jsonFS.layer.newFeatures[i];
            featureHash[k].attributes.new = true;
            i++;
        }
        i = 0;
        while (i < jsonFS.layer.updatedFeatures.length) {
            var k = jsonFS.layer.updatedFeatures[i];
            featureHash[k].attributes.updated = true;
            i++;
        }
        typeIdToIconUrl = {};
        i = 0;
        while (i < jsonFS.layer.typeIdIconUrlPairs.length) {
            var typeIdIconUrlPair = jsonFS.layer.typeIdIconUrlPairs[i];
            typeIdToIconUrl[typeIdIconUrlPair.typeId] = typeIdIconUrlPair.iconUrl;
            i++;
        }
    }

    var iXMin;
    var iXMax;
    function calculateIXMinAndIXMax() {    
        var xmin;
        var xmax;
        if (map.extent.spatialReference.wkid == 102100) {
            var geometryService = new GeometryService();
            var outputpoints = [];
            outputpoints[0] = geometryService.project(map.extent.xmin, map.extent.ymin);
            outputpoints[1] = geometryService.project(map.extent.xmax, map.extent.ymax);
            xmin = outputpoints[0].x;
            xmax = outputpoints[1].x;
            var xMin2D = extentFunction.getXMin2D(102100);
            var xMax2D = extentFunction.getXMax2D(102100);
            var wholeWorldX = extentFunction.getWholeWorldX(102100);
            if (xmin > xmax) {
                if (xmin - xmax < 1 && (map.extent.xmax - map.extent.xmin) > wholeWorldX * .95) {
                    if (map.extent.xmax > xMax2D) {
                        xmax += 360;
                    } else if (map.extent.xmin < xMin2D) {
                        xmin -= 360;
                    }
                } else {
                    xmax += 360;
                    if (xmin < 180 && xmax >= 180) {
                        xmax -= 360;
                        xmin -= 360;
                    }
                }
            } else {
                if (xmax - xmin < 1 && (map.extent.xmax - map.extent.xmin) > wholeWorldX * .95) {
                    if (map.extent.xmax > xMax2D) {
                        xmax += 360;
                    } else if (map.extent.xmin < xMin2D) {
                        xmin -= 360;
                    }
                }
            }
        } else {
            xmin = map.extent.xmin;
            xmax = map.extent.xmax;
        }
        var middle;
        if (xmax < xmin) {
            middle = (xmax + (xmin - 360)) / 2;
        } else {
            middle = (xmax + xmin) / 2;
        }
        iXMax = middle + 180;
        iXMin = middle - 180;
    }

    function dontUpdateLegend() {
        updateLegend = false;
    }

    function deselect() {
        var geometry = dataFunction.getSelectionGeometry().getExtent();
        var points = getPointsIntersected(geometry);
        var links = getLinksToManipulate(geometry);
        var response = {
            "features": points,
            "links": links
        };
        dataFunction.deselectInBuffer(response);
    }

    function toggleNodes(nodes, turnOn) {
        if (summaryLevel != detailLevel) {
            var k = 0;
            var changeToDeselectedKeys = [];
            var changeToSelectedKeys = [];
            while (k < nodes.length) {
                var key = nodes[k];
                var featureHash = dataFunction.getFeatureHash();
                var f = featureHash[key];
                var g = placeGraphics[key];
                if (f) {
                    f.attributes.selected = turnOn;
                    g.attributes.selected = turnOn;
                    if (turnOn) {
                        changeToSelectedKeys.push(key);
                    } else {
                        changeToDeselectedKeys.push(key);
                    }
                }
                k++;
            }
            redraw(changeToDeselectedKeys, changeToSelectedKeys);
            dataFunction.applyEditCallback();
        }
    }

    function itemsExtentsReload2() {
        xhr.get({
            url: "rest/map/getExtentInfo/" + qs("dv_uuid") + "/" + qs("viz_uuid") + "/" + getNextSequenceNumber(),
            handleAs: "json",
            load: function(newExtentInfo) {
                if (newExtentInfo.placeTypeLimitReached) {
                    window.parent.hideSpinner(vizid);
                    window.parent.mapViewHideLoading(vizid);
                    window.parent.placeTypeLimitReached(vizid);
                    isMapHidden = true;
                    refreshingMap = false;
                    dataFunction.setLoading(false);
                } else if (newExtentInfo.trackTypeLimitReached) {
                    window.parent.hideSpinner(vizid);
                    window.parent.mapViewHideLoading(vizid);
                    window.parent.trackTypeLimitReached(vizid);
                    isMapHidden = true;
                    refreshingMap = false;
                    dataFunction.setLoading(false);
                } else {
                    extentFunction.setMapExtentInfo(newExtentInfo.newExtent, newExtentInfo.initialExtent, newExtentInfo.extent);
                    var mapRefreshed = extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
                    if (!mapRefreshed) {
                        dataFunction.getUpdate();
                    }
                }
            }
        });
    }
    function itemsExtentsReload3() {
        xhr.get({
            url: "rest/map/getSquisherExtent/" + qs("dv_uuid") + "/" + qs("viz_uuid") + "/" + getNextSequenceNumber(),
            handleAs: "json",
            load: function(extent) {
                    var zoomAndExtent = extentFunction.calculateZoomAndExtent(extent, 102100);
                    extentFunction.setPaddingsUsingZoom(zoomAndExtent.zoom);
                    if (extentFunction.isPaddingsNotZero()) {
                        extentFunction.setMapDivContainerToNewPaddings();
                    }
                    refreshAndUpdateMap(zoomAndExtent.extent, zoomAndExtent.zoom);
            }
        });
    }
    var mapDiv;
    var mapDivContainer;
    var symbolFunction;
    var queryFunction;
    var cmenu;
    var deselectMenuItem;
    var selectMenuItem;
    var deselectAllMenuItem;
    var selectAllMenuItem;
    var mousePoint = null;
    var dialogShown = false;
    var dialogOnGeom = null;
    var timerId = null;
    var closeDialogTimerId = null;
    var inDialog = false;
    var dialog;
    var highlightGraphic = null;
    var rightMenuOnKey = null;
    var dialogOnFeature = null;
    var currentDisplayPoint;
    var currentQueryPoint1;
    var dontclosedialog = false;
    var minPlaceSize;
    var maxPlaceSize;
    var currentSizes;
    var placeGraphicZ;
    var placeGraphicZs;
    var placeGraphics;
    var updateLegend = true;

    function getUpdate2(zoom) {
        var extentParam = map.extent;
        var callbackParams = {
            'zoom': zoom
        };
        extentFunction.useGeometryService2(extentParam, callbackParams, f1callback2);
    }

    function f1callback2(callbackParams, minx, miny, maxx, maxy) {
        dataFunction.getUpdate2(minx, miny, maxx, maxy);
    }

    function initDialog() {
        if (dialog != null) {
            return;
        }
        dialog = new TooltipDialog({
            id: "tooltipDialog",
            style: "position: absolute; width: 250px; font: normal normal normal 10pt Helvetica;z-index:100;overflow-y:auto;max-height:200px;",
            onMouseLeave: setNotInDialog,
            onMouseEnter: setInDialog
        });
        on(dialog, "show", function() {
            var connectorNodeOffsetTop = this.connectorNode.offsetTop;
            var contentsNodeOffsetTop = this.contentsNode.offsetTop;
            var deltay = connectorNodeOffsetTop - contentsNodeOffsetTop;
            if (navigator.userAgent.toLowerCase().indexOf("compatible; msie 10.0;") > 0) {
                if (deltay != -20) {
                    var parentNode = this.containerNode.parentElement;
                    parentNode = parentNode.parentNode;
                    parentNode = parentNode.parentNode;
                    parentNode = parentNode.parentNode;
                    var offsetTop = parentNode.offsetTop - 20;
                    dojo.style(parentNode, "top", offsetTop + "px");
                }
            } else if (deltay != -20) {
                var parentNode = this.containerNode.parentElement;
                parentNode = parentNode.parentNode;
                parentNode = parentNode.parentNode;
                parentNode = parentNode.parentNode;
                var offsetTop = parentNode.offsetTop - 20;
                dojo.style(parentNode, "top", offsetTop + "px");
            }
            var connectorNodeOffsetLeft = this.connectorNode.offsetLeft;
            var contentsNodeOffsetLeft = this.contentsNode.offsetLeft;
            var deltax = connectorNodeOffsetLeft - contentsNodeOffsetLeft;
            if (navigator.userAgent.toLowerCase().indexOf("compatible; msie 10.0;") > 0) {
                if (deltax != -4) {
                    var parentNode = this.containerNode.parentElement;
                    parentNode = parentNode.parentNode;
                    parentNode = parentNode.parentNode;
                    parentNode = parentNode.parentNode;
                    var offsetLeft = parentNode.offsetLeft + 20;
                    dojo.style(parentNode, "left", offsetLeft + "px");
                }
            } else if (deltax != -4) {
                var parentNode = this.containerNode.parentElement;
                parentNode = parentNode.parentNode;
                parentNode = parentNode.parentNode;
                parentNode = parentNode.parentNode;
                var offsetLeft = parentNode.offsetLeft + 20;
                dojo.style(parentNode, "left", offsetLeft + "px");
            }
            var mapHeight = map.height;
            var contentsNodeHeight = mapHeight / 2;
            var containerNodeHeight = contentsNodeHeight - 32;
            dojo.style(dialog, "max-height", contentsNodeHeight);
            domClass.add(this.contentsNode, "csi_tooltip");
            dojo.style(this.contentsNode, "max-height", contentsNodeHeight);
            dojo.style(this.containerNode, "max-height", containerNodeHeight);
            dojo.style(this.containerNode, "overflow-y", "auto");
        });
        on(dialog, "click", function(evt) {
            if (evt.ctrlKey === true || evt.metaKey === true) {
                var screenPoint = new ScreenPoint(evt.clientX - extentFunction.getNewPaddingLeft(), evt.clientY);
                var mapPoint = map.toMap(screenPoint);
                currentDisplayPoint = mapPoint;
                currentQueryPoint1 = mapPoint;
                var extent = queryFunction.getExtent(screenPoint, maxPlaceSize);
                var points = getPointsIntersected(extent);
                var response = {
                    "features": points
                };
                toggleClickBufferOnDialog(response);
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
            }
        });
        dialog.startup();
    }

    function toggleClickBuffer(response) {
        var feature;
        var features = response.features;
        var inBuffer = [];
        for (var i = 0; i < features.length; i++) {
            feature = features[i];
            if (queryFunction.arePointsWithinPixels(currentQueryPoint1, feature.geometry, currentSizes[feature.attributes.size - 1])) {
                inBuffer.push(feature.attributes.objectID);
            }
        }
        if (inBuffer.length > 0 || response.links > 0) {
            dataFunction.toggleClickBuffer(inBuffer, response.links);
        }
    }

    function showTooltip(evt) {
        // reset the previous timeout that would call for the popup.
        if (timerId !== null) {
            // if timeOutAction is not null or undefined, clear it out.
            window.clearTimeout(timerId);
            timerId = null;
        }
        if (rightMenuOnKey !== null)
            return;
        dialogOnFeature = evt.graphic;
        dialogOnGeom = evt.graphic.geometry;
        // use setTimeout to run the function that shows the popup after 1 second.
        timerId = window.setTimeout(function() {
            if (dialogOnFeature == null || mousePoint == null)
                return;
            //            var normalizedMousePoint = mousePoint.normalize();
            //            var normalizedGeom = normalizeGeom(dialogOnGeom);
            //            if (queryFunction.arePointsWithinPixels(normalizedGeom, normalizedMousePoint, currentSizes[dialogOnFeature.attributes.size - 1])) {
            if (queryFunction.arePointsWithinPixels(dialogOnGeom, mousePoint, currentSizes[dialogOnFeature.attributes.size - 1])) {
                if (rightMenuOnKey !== null)
                    return;
                showTooltipCallback(evt);
            }
        }, 150); // 1000 milliseconds = 1 second.
    }

    function normalizeGeom(oldGeom) {
        var newGeom = cloneGeom(oldGeom);
        if (newGeom.x < -180) {
            newGeom.x += 360;
        }
        return newGeom;
    }

    function cloneGeom(obj) {
        return {
            x: obj.x,
            y: obj.y,
            type: obj.type,
            spatialReference: obj.spatialReference
        };
    }

    function showTooltipCallback(evt) {
        xhr.get({
            url: "rest/map/getTooltip/" + dvid + "/" + vizid + "/" + getNextSequenceNumber() + "/" + evt.graphic.attributes.objectID,
            handleAs: "json",
            load: function(data) {
                if (rightMenuOnKey !== null)
                    return;
                var content = "";
                for (var k in data) {
                    // use hasOwnProperty to filter out keys from the Object.prototype
                    if (data.hasOwnProperty(k)) {
                        var colonWritten = false;
                        content += "<p><b>" + k + "</b>";
                        var dataKContent = data[k];
                        if (dataKContent.renderAsBundle) {
                            content += "<br>";
                            var k1Content = dataKContent.renderAsBundleInfo;
                            for (var item in k1Content) {
                                content += k1Content[item] + "<br>";
                            }
                        } else {
                            for (var k1 in dataKContent) {
                                if (k1 == "renderAsBundle")
                                    continue;
                                if (dataKContent.hasOwnProperty(k1)) {
                                    if (!colonWritten) {
                                        content += ":<br>";
                                        colonWritten = true;
                                    }if (k1 == "Type") {
                                        content += "<b>" + i18nType + "</b>:<br>";
                                    } else {
                                        content += "<b>" + k1 + "</b>:<br>";
                                    }
                                    var k1Content = dataKContent[k1];
                                    for (var item in k1Content) {
                                        content += k1Content[item] + "<br>";
                                    }
                                }
                            }
                        }
                        content += "</p>";
                    }
                }
                if (closeDialogTimerId !== null) {
                    // if timeOutAction is not null or undefined, clear it out.
                    window.clearTimeout(closeDialogTimerId);
                    closeDialogTimerId = null;
                }
                highlightDialogOn();
                dialog.setContent(content);
                var screenPoint = map.toScreen(mousePoint);
                var finalx = screenPoint.x - 10 + extentFunction.getNewPaddingLeft();
                var finaly = screenPoint.y + 10;
                domStyle.set(dialog.domNode, "opacity", 0.85);
                dijitPopup.open({
                    popup: dialog,
                    x: finalx,
                    y: finaly
                });
                dialogShown = true;
            }
        });
    }

    function clearHover(evt) {
        window.clearTimeout(timerId);
        timerId = null;
        if (dialogShown) {
            // reset the previous timeout that would call for the popup.
            if (closeDialogTimerId !== null) {
                // if timeOutAction is not null or undefined, clear it out.
                window.clearTimeout(closeDialogTimerId);
                closeDialogTimerId = null;
            }
            // use setTimeout to run the function that shows the popup after 1 second.
            closeDialogTimerId = window.setTimeout(function() {
                if (!inDialog) {
                    if (!dontclosedialog) {
                        closeDialog();
                        rightMenuOnKey = null;
                        dialogOnFeature = null;
                        dialogOnGeom = null;
                        dialogShown = false;
                        dontclosedialog = false;
                    }
                }
            }, 100);
        }
    }

    function closeDialog() {
        cmenu.style.display = "none";
        dijitPopup.close(dialog);
        if (highlightGraphic && map != null) {
            map.graphics.remove(highlightGraphic);
            highlightGraphic = null;
        }
    }

    function featureLayerContextMenu(response) {
        var feature;
        var features = response.features;
        var filteredFeatures = [];
        for (var i = 0; i < features.length; i++) {
            feature = features[i];
            if (queryFunction.arePointsWithinPixels(currentQueryPoint1, feature.geometry, currentSizes[feature.attributes.size - 1])) {
                filteredFeatures.push(feature);
            }
        }
        var maxZ = -1;
        var topmostFeature = null;
        for (var i = 0; i < filteredFeatures.length; i++) {
            feature = filteredFeatures[i];
            if (maxZ < placeGraphicZs[feature.attributes.objectID]) {
                topmostFeature = feature;
                maxZ = placeGraphicZs[feature.attributes.objectID];
            }
        }
        closeDialog();
        if (topmostFeature != null) {
            var key = topmostFeature.attributes.objectID;
            var featureHash = dataFunction.getFeatureHash();
            var f = featureHash[key];
            dialogOnFeature = f;
            dialogOnGeom = f.geometry;
            highlightDialogOn();
            //            if (f.attributes.selected) {
            //                deselectMenuItem.parentElement.style.display = "inline";
            //                selectMenuItem.parentElement.style.display = "none";
            //            } else {
            //                deselectMenuItem.parentElement.style.display = "none";
            //                selectMenuItem.parentElement.style.display = "inline";
            //            }
            deselectMenuItem.parentElement.style.display = "inline";
            selectMenuItem.parentElement.style.display = "inline";
            deselectAllMenuItem.parentElement.style.display = "none";
            selectAllMenuItem.parentElement.style.display = "none";
            rightMenuOnKey = [key];
        } else {
            deselectMenuItem.parentElement.style.display = "none";
            selectMenuItem.parentElement.style.display = "none";
            deselectAllMenuItem.parentElement.style.display = "inline";
            selectAllMenuItem.parentElement.style.display = "inline";
            rightMenuOnKey = null;
        }
        var screenPoint = map.toScreen(currentDisplayPoint);
        cmenu.style.display = "inline";
        cmenu.style.position = "absolute";
        if (map.width - screenPoint.x > cmenu.scrollWidth) {
            cmenu.style.left = (screenPoint.x + extentFunction.getNewPaddingLeft()) + "px";
        } else {
            cmenu.style.left = ((screenPoint.x + extentFunction.getNewPaddingLeft()) - cmenu.scrollWidth) + "px";
        }
        if (map.height - screenPoint.y > cmenu.scrollHeight) {
            cmenu.style.top = screenPoint.y + "px";
        } else {
            cmenu.style.top = (screenPoint.y - cmenu.scrollHeight) + "px";
        }
        dontclosedialog = true;
        dialogShown = true;
    }

    function highlightDialogOn() {
        if (highlightGraphic) {
            map.graphics.remove(highlightGraphic);
            highlightGraphic = null;
        }
        var iconUri = getIconUri(dialogOnFeature.attributes.typeId)
        var useIcon = iconUri != null && iconUri.length > 0;
        var iconSize = currentSizes[4];
        var isSelected = false;
        var ishighlighted = true;
        var isCombined = getIsSet(dialogOnFeature, "combined");
        var isNew = getIsSet(dialogOnFeature, "new");
        var isUpdated = getIsSet(dialogOnFeature, "updated");
        var highlightSymbol = symbolFunction.createPlaceSymbol(dialogOnFeature.attributes.typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, summaryLevel != detailLevel, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, 1);
        highlightGraphic = new Graphic(dialogOnFeature.geometry,highlightSymbol);
        map.graphics.add(highlightGraphic);
    }

    function getIconUri(typeId) {
        var iconUri = null;

        if (typeId in typeIdToIconUrl && typeIdToIconUrl[typeId] != null)
            iconUri = typeIdToIconUrl[typeId];

        return iconUri;
    }

    function getIsSet(f, key) {
        var retVal = false;

        var attributes = f.attributes;
        if (key in attributes && attributes[key] != null && attributes[key])
            retVal = true;

        return retVal;
    }

    function setNotInDialog() {
        closeDialog();
        rightMenuOnKey = null;
        dialogOnFeature = null;
        dialogOnGeom = null;
        dialogShown = false;
        inDialog = false;
    }

    function setInDialog() {
        inDialog = true;
    }

    function toggleClickBufferOnDialog(response) {
        var feature;
        var features = response.features;
        var filteredFeatures = [];
        for (var i = 0; i < features.length; i++) {
            feature = features[i];
            if (queryFunction.arePointsWithinPixels(currentQueryPoint1, feature.geometry, currentSizes[feature.attributes.size - 1])) {
                filteredFeatures.push(feature);
            }
        }
        var maxZ = -1;
        var topmostFeature = null;
        for (var i = 0; i < filteredFeatures.length; i++) {
            feature = filteredFeatures[i];
            if (maxZ < placeGraphicZs[feature.attributes.objectID]) {
                topmostFeature = feature;
                maxZ = placeGraphicZs[feature.attributes.objectID];
            }
        }
        var inBuffer = [];
        if (topmostFeature != null && dialogOnFeature == topmostFeature) {
            inBuffer.push(topmostFeature.attributes.objectID);
        }
        if (inBuffer.length > 0) {
            dataFunction.toggleClickBuffer(inBuffer, []);
        }
    }

    function setAssociationLayer(jsonFS) {
        if (!jsonFS.linkLimitReached) {
            if (!symbolFunction.hasLineDefinitions()) {
                return;
            }
            var featureHash = dataFunction.getFeatureHash();
            var linkHash = dataFunction.getLinkHash();
            associationsLayer = new GraphicsLayer();
            calculateIXMinAndIXMax();
            var i = 0;
            while (i < jsonFS.layer.associations.length) {
                var a = jsonFS.layer.associations[i];
                if (symbolFunction.foundLineSymbol(a)) {
                    var sourceGeometries = [];
                    var destinationGeometries = [];
                    var arr = [];
                    if (prepareLineDefinition(featureHash, linkHash, sourceGeometries, destinationGeometries, arr, a)) {
                        var lineDefinition = symbolFunction.createLineDefinition(map, a, sourceGeometries, destinationGeometries, a.typeId != -1, iXMin, iXMax);
                        drawLineGraphics(lineDefinition);
                    }
                }
                i++;
            }
        }
    }

    function prepareLineDefinition(featureHash, linkHash, sourceGeometries, destinationGeometries, arr, a) {
        linkHash[a.typeId] = arr;
        var j = 0;
        while (j < a.sourceObjectIds.length) {
            var sourceObjectId = a.sourceObjectIds[j];
            var destinationObjectId = a.destinationObjectIds[j];
            if (sourceObjectId == null) {
            } else if (destinationObjectId == null) {
            } else if (!(sourceObjectId in featureHash)) {
            } else if (!(destinationObjectId in featureHash)) {
            } else {
                sourceGeometries.push(featureHash[sourceObjectId].geometry);
                destinationGeometries.push(featureHash[destinationObjectId].geometry);
            }
            arr.push([sourceObjectId, destinationObjectId]);
            j++;
        }
        return sourceGeometries.length > 0;
    }

    function drawLineGraphics(lineDefinition) {
        associationsLayer.add(lineDefinition["lineGraphic"]);
        if (lineDefinition["arrowGraphic"] != null) {
            associationsLayer.add(lineDefinition["arrowGraphic"]);
        }
    }

    var typeIdToIconUrl;

    function deselectFunction() {
        dataFunction.deselectInBuffer3(rightMenuOnKey);
        rightMenuOnKey = null;
        cmenu.style.display = "none";
    }

    function selectFunction() {
        dataFunction.selectInBuffer3(rightMenuOnKey);
        rightMenuOnKey = null;
        cmenu.style.display = "none";
    }

    function redraw(changeToDeselectedKeys, changeToSelectedKeys) {
        var i = 0;
        while (i < changeToDeselectedKeys.length) {
            var key = changeToDeselectedKeys[i];
            var g = placeGraphics[key];
            if (g) {
                updatePlaceGraphics(g);
            }
            i++;
        }
        var j = 0;
        while (j < changeToSelectedKeys.length) {
            var key = changeToSelectedKeys[j];
            var g = placeGraphics[key];
            if (g) {
                updatePlaceGraphics(g);
            }
            j++;
        }
    }
    var setPlacesLayerInterval;

    function setPlacesLayer(jsonFS) {
        var alpha = jsonFS.nodeTransparency;
        prepareDownloadPlaceSymbols(jsonFS, alpha);
        symbolFunction.downloadPlaceSymbols();
        if (setPlacesLayerInterval) {
            clearInterval(setPlacesLayerInterval);
        }
        setPlacesLayerInterval = setInterval(function() {
            if (symbolFunction.isPlaceSymbolsReady()) {
                clearInterval(setPlacesLayerInterval);
                createPlacesLayer(jsonFS, alpha);
                if (oldAssociationsLayer !== null) {
                    map.removeLayer(oldAssociationsLayer);
                    oldAssociationsLayer = null;
                }
                if (oldPlacesLayer !== null) {
                    map.removeLayer(oldPlacesLayer);
                    oldPlacesLayer = null;
                }
                if (associationsLayer !== null) {
                    map.addLayer(associationsLayer);
                }
                //        addLayersAbove();
                map.addLayer(placesLayer);
                if (itemsInViz > 1000) {
                    setTimeout(function() {
                        window.parent.mapViewHideLoading(vizid);
                        map.enableMapNavigation();
                        map.disableClickRecenter();
                        map.disableDoubleClickZoom();
                        map.disableKeyboardNavigation();
                        window.requestAnimationFrame(function() {
                            window.setTimeout(function() {
                                window.parent.mapRenderComplete(vizid);
                                dataFunction.setLoading(false);
                            }, 1);
                        });
                    }, 300);
                } else {
                    window.parent.mapViewHideLoading(vizid);
                    map.enableMapNavigation();
                    map.disableClickRecenter();
                    map.disableDoubleClickZoom();
                    map.disableKeyboardNavigation();
                    window.requestAnimationFrame(function() {
                        window.setTimeout(function() {
                            window.parent.mapRenderComplete(vizid);
                            dataFunction.setLoading(false);
                        }, 1);
                    });
                }
            }
        }, 100);
    }

    function prepareDownloadPlaceSymbols(jsonFS, alpha) {
        var i = 0;
        while (i < jsonFS.layer.features.length) {
            var f = jsonFS.layer.features[i];
            var iconUri = getIconUri(f.attributes.typeId);
            var useIcon;
            if (iconUri != null && symbolFunction.getShapeString(f.attributes.typeId) == "None") {
                useIcon = true;
            } else {
                useIcon = currentSizes[f.attributes.size - 1] >= 8 && iconUri != null && iconUri.length > 0;
            }
            var iconSize = currentSizes[f.attributes.size - 1];
            var isSelected = getIsSet(f, "selected");
            var ishighlighted = false;
            var isCombined = getIsSet(f, "combined");
            var isNew = getIsSet(f, "new");
            var isUpdated = getIsSet(f, "updated");
            symbolFunction.prepareDownloadPlaceSymbol(f.attributes.typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, summaryLevel != detailLevel, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha);
            i++;
        }
    }

    function createPlacesLayer(jsonFS, alpha) {
        var featureHash = dataFunction.getFeatureHash();
        placesLayer = new GraphicsLayer();
        placeGraphicZ = 0;
        placeGraphicZs = {};
        placeGraphics = {};
        var i = 0;
        while (i < jsonFS.layer.features.length) {
            var objectID = jsonFS.layer.features[i].attributes.objectID;
            var f = featureHash[objectID];
            var iconUri = getIconUri(f.attributes.typeId);
            var useIcon;
            if (iconUri != null && symbolFunction.getShapeString(f.attributes.typeId) == "None") {
                useIcon = true;
            } else {
                useIcon = currentSizes[f.attributes.size - 1] >= 8 && iconUri != null && iconUri.length > 0;
            }
            var iconSize = currentSizes[f.attributes.size - 1];
            var isSelected = getIsSet(f, "selected");
            var ishighlighted = false;
            var isCombined = getIsSet(f, "combined");
            var isNew = getIsSet(f, "new");
            var isUpdated = getIsSet(f, "updated");
            var placeSymbol = symbolFunction.createPlaceSymbol(f.attributes.typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, summaryLevel != detailLevel, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha);
            var geometry = new Point(f.geometry.x,f.geometry.y,new SpatialReference({
                wkid: 4326
            }));
            var placeGraphic = new Graphic(geometry,placeSymbol);
            placeGraphic.attributes = f.attributes;
            placesLayer.add(placeGraphic);
            placeGraphicZs[placeGraphic.attributes.objectID] = placeGraphicZ;
            placeGraphicZ++;
            placeGraphics[placeGraphic.attributes.objectID] = placeGraphic;
            i++;
        }
        if (summaryLevel == detailLevel) {
            placesLayer.on("mouse-over", showTooltip);
        }
    }

    function updatePlaceGraphics(f) {
        var iconUri = getIconUri(f.attributes.typeId);
        var useIcon;
        if (iconUri != null && symbolFunction.getShapeString(f.attributes.typeId) == "None") {
            useIcon = true;
        } else {
            useIcon = currentSizes[f.attributes.size - 1] >= 8 && iconUri != null && iconUri.length > 0;
        }
        var iconSize = currentSizes[f.attributes.size - 1];
        var isSelected = getIsSet(f, "selected");
        var ishighlighted = false;
        var isCombined = getIsSet(f, "combined");
        var isNew = getIsSet(f, "new");
        var isUpdated = getIsSet(f, "updated");
        var placeSymbol = symbolFunction.createPlaceSymbol(f.attributes.typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, summaryLevel != detailLevel, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, 1);
        f.setSymbol(placeSymbol);
    }

    function summaryRefreshMap(zoom, extent) {
        window.parent.showSpinner(vizid);
        if (placesLayer !== null) {
            refreshAndUpdateMap(extent, zoom);
        } else {
            window.setTimeout(function() {
                getUpdate2(zoom);
            }, 150);
        }
    }

    function processSelectedLinks(a) {
        var lineDefinition = symbolFunction.getLineDefinition(-1);
        if (lineDefinition != null && associationsLayer != null && associationsLayer.graphics.length > 0)
            associationsLayer.remove(lineDefinition["lineGraphic"]);
        var featureHash = dataFunction.getFeatureHash();
        var linkHash = dataFunction.getLinkHash();
        var sourceGeometries = [];
        var destinationGeometries = [];
        var arr = [];
        if (prepareLineDefinition(featureHash, linkHash, sourceGeometries, destinationGeometries, arr, a)) {
            calculateIXMinAndIXMax();
            var lineDefinition = symbolFunction.createLineDefinition(map, a, sourceGeometries, destinationGeometries, false, iXMin, iXMax);
            drawLineGraphics(lineDefinition);
        }
    }

    var needReload = false;
    var detailLevel;
    return declare(commonModule, {
        //return
        constructor: function(params) {
            dvid = params.dv_uuid;
            vizid = params.viz_uuid;
            sequenceNumber = params.sequenceNumber;
            detailLevel = params.detailLevel;
            frontendZoomThreshold = params.frontendZoomThreshold;
            basemapId = params.basemapId;
            basemapUrl = params.basemapUrl;
            basemapType = params.basemapType;
            basemapOpacity = params.basemapOpacity;
            layerInfos = params.layerInfos;
            minPlaceSize = params.minPlaceSize;
            maxPlaceSize = params.maxPlaceSize;
            mapDivContainer = dom.byId(params.mapDivContainerId);
            extentFunction = new ExtentFunction(dvid,vizid,refreshAndUpdateMap,getNextSequenceNumber,mapDivContainer);
            mapDiv = dom.byId(params.mapId);
            var mapOptions = {
                slider: false,
                showAttribution: false,
                displayGraphicsOnPan: false,
                fadeOnZoom: true
            };
            if (params.extent != null && params.extent.spatialReference.wkid == 4326) {
                if (params.zoom == -1) {
                    var zoomAndExtent = extentFunction.calculateZoomAndExtent(params.extent, 102100);
                    extentFunction.setPaddingsUsingZoom(zoomAndExtent.zoom);
                    if (extentFunction.isPaddingsNotZero()) {
                        extentFunction.setMapDivContainerToNewPaddings();
                    }
                    mapOptions["zoom"] = zoomAndExtent.zoom;
                    mapOptions["extent"] = zoomAndExtent.extent;
                } else {
                    var currentZoom = extentFunction.adjustZoomToNewSize(params.zoom);
                    extentFunction.setPaddingsUsingZoom(currentZoom);
                    if (extentFunction.isPaddingsNotZero()) {
                        extentFunction.setMapDivContainerToNewPaddings();
                    }
                    var xmin = params.extent.xmin;
                    var xmax = params.extent.xmax;
                    var ymin = params.extent.ymin;
                    var ymax = params.extent.ymax;
                    var xcenter = (xmin + xmax) / 2;
                    var ycenter = (ymin + ymax) / 2;
                    var centerPoint = {
                        x: xcenter,
                        y: ycenter,
                        wkid: params.extent.spatialReference.wkid
                    };
                    var extent = extentFunction.calculateExtentByZoomAndCenterPoint(currentZoom, centerPoint);
                    mapOptions["zoom"] = currentZoom;
                    mapOptions["extent"] = extent;
                }
            } else {
                if (params.extent != null) {
                    mapOptions["extent"] = params.extent;
                }
                var zoom;
                if (params.zoom == null || params.zoom == -1) {
                    zoom = 0;
                } else {
                    zoom = params.zoom;
                }
                var maxWidth = 256 * Math.pow(2, zoom);
                while (mapDivContainer.offsetWidth > maxWidth) {
                    zoom++;
                    maxWidth = 256 * Math.pow(2, zoom);
                }
                var maxHeight = 256 * Math.pow(2, zoom);
                while (mapDivContainer.offsetHeight > maxHeight) {
                    zoom++;
                    maxHeight = 256 * Math.pow(2, zoom);
                }
                if (params.extent != null && params.extent.spatialReference.wkid == 4326) {
                    var deltax = params.extent.xmax - params.extent.xmin;
                    var newmin = Math.log(Math.tan((90 + params.extent.ymin) * Math.PI / 360));
                    var newmax = Math.log(Math.tan((90 + params.extent.ymax) * Math.PI / 360));
                    var deltay = newmax - newmin;
                    while (mapDivContainer.offsetWidth * 360 * 1.49629 / deltax > maxWidth * 2 && mapDivContainer.offsetHeight * 9.5 / deltay > maxWidth * 2) {
                        zoom++;
                        maxWidth = 256 * Math.pow(2, zoom);
                    }
                }
                mapOptions["zoom"] = zoom;
            }
            map = new Map(params.mapId,mapOptions);
            map.autoResize = false;
            map.disableClickRecenter();
            map.disableDoubleClickZoom();
            map.disableKeyboardNavigation();
            panEndEventHandler = null;
            dataFunction = new DataFunction(map,dvid,vizid,useData,toggle,setSelected,select,deselect,dontUpdateLegend,toggleNodes,getNextSequenceNumber,processSelectedLinks);
            this.setDataFunction(params, dataFunction);
            dataFunction.setBasemap(basemapId, basemapUrl, basemapType, basemapOpacity);
            dataFunction.setFrontendToggleThreshold(params.frontendToggleThreshold);
            initMap();
            addLayersBelow();
            mapPanAndZoomInitialized = false;
            initDocument();
            extentFunction.setMap(map);
            this.setExtentFunction(params, extentFunction);
            homeButton = dom.byId(params.homeButtonId);
            on(homeButton, "click", function() {
                extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
            });
            if (params.isNewExtent || !window.parent.isMapPinned(vizid)) {
                extentFunction.saveExtentAndZoom2(mapOptions["zoom"]);
            }
            searchFunction = new SearchFunction(map,params.searchId,params.viz_uuid,params.locatorUrl);
            this.setSearchFunction(searchFunction);
            symbolFunction = new SymbolFunction(getNextSequenceNumber);
            queryFunction = new QueryFunction(map);
            cmenu = dom.byId(params.cmenuId);
            on(cmenu, "mouseenter", setInDialog);
            on(cmenu, "mouseleave", setNotInDialog);
            deselectMenuItem = dom.byId(params.deselectId);
            on(deselectMenuItem, "click", deselectFunction);
            selectMenuItem = dom.byId(params.selectId);
            on(selectMenuItem, "click", selectFunction);
            deselectAllMenuItem = dom.byId(params.deselectAllId);
            on(deselectAllMenuItem, "click", this.deselectAll);
            selectAllMenuItem = dom.byId(params.selectAllId);
            on(selectAllMenuItem, "click", this.selectAll);
            i18nType = params.i18nType;
        },
        updateMap: function() {
        	if (mapDivContainer.offsetWidth > 0) {
            dataFunction.getUpdate();
          }
        },
        reloadMap: function() {
            if (mapDivContainer.offsetWidth == 0) {
                needReload = true;
            } else {
                window.parent.mapViewShowLoading(vizid);
                map.disableMapNavigation();
                itemsExtentsReload2();
                needReload = false;
            }
        },
        deselectAll: function() {
            dataFunction.deselectAll();
            rightMenuOnKey = null;
            cmenu.style.display = "none";
        },
        selectAll: function() {
            dataFunction.selectAll();
            rightMenuOnKey = null;
            cmenu.style.display = "none";
        },
        applySelection: function() {
            if (placesLayer !== null) {
                refreshAndUpdateMap(panExtent, map.getZoom());
            } else {
                getUpdate2();
            }
        },
        resizeMapDiv: function() {
        	if (mapDivContainer.offsetWidth > 0) {
            if (needReload) {
                this.reloadMap();
            } else {
                extentFunction.clearMapContainerDivPadding();
                extentFunction.setNewPaddingValues(0, 0);
                var currentCenterPoint = extentFunction.getCurrentCenterPoint();
                var currentZoom = extentFunction.getCurrentZoom();
                var extent;
                var zoom = null;
                if (currentCenterPoint != null && currentZoom != null && currentZoom != -1) {
                    zoom = extentFunction.adjustZoomToNewSize(currentZoom);
                    extentFunction.setPaddingsUsingZoom(zoom);
                    extent = extentFunction.calculateExtentByZoomAndCenterPoint(currentZoom, currentCenterPoint);
                } else {
                    var extentOut;
                    if (doRefreshMap) {
                        extentOut = extentFunction.getRightExtent(extentFunction.getInitialExtent(), map.extent.spatialReference.wkid);
                    } else {
                        var currentExtent = extentFunction.getCurrentExtent();
                        if (currentExtent != null)
                            extentOut = extentFunction.getRightExtent(currentExtent, map.extent.spatialReference.wkid);
                        else
                            extentOut = extentFunction.getRightExtent(extentFunction.getInitialExtent(), map.extent.spatialReference.wkid);
                    }
                    var xmin = extentOut.xmin;
                    var xmax = extentOut.xmax;
                    var ymin = extentOut.ymin;
                    var ymax = extentOut.ymax;
                    var x1 = xmax - xmin;
                    var y1 = ymax - ymin;
                    var oldZoom = map.getZoom();
                    if (x1 / y1 < mapDivContainer.offsetWidth / mapDivContainer.offsetHeight) {
                        // span deltay over mapDivContainer.offsetHeight
                        var x2 = Math.abs(mapDivContainer.offsetWidth / mapDivContainer.offsetHeight * y1);
                        zoom = extentFunction.calculateSnugZoom(x2, y1, map.extent.spatialReference.wkid);
                    } else {
                        // span deltax over mapDivContainer.offsetWidth
                        var y2 = Math.abs(mapDivContainer.offsetHeight / mapDivContainer.offsetWidth * x1);
                        zoom = extentFunction.calculateSnugZoom(x1, y2, map.extent.spatialReference.wkid);
                    }
                    extentFunction.setPaddingsUsingZoom(zoom);
                    extent = extentFunction.getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, map.extent.spatialReference.wkid);
                }
                if (extentFunction.isPaddingsNotZero()) {
                    extentFunction.setMapDivContainerToNewPaddings();
                }
                doRefreshMap = false;
                summaryRefreshMap(zoom, extent);
            }
          }
        },
        checkNeedReload: function() {
            if (needReload) {
                this.reloadMap();
            }
        },
        resizeHappened: function() {
            isResizeHappend = true;
        },
        legendUpdated: function() {
            dontUpdateLegend();
            if (placesLayer !== null) {
                refreshAndUpdateMap(panExtent, map.getZoom());
            } else {
                getUpdate2();
            }
        },
        zoomHome: function() {
            zoom_sequencenumber = getNextSequenceNumber();
            handleDragEndOrZoomEnd(zoom_sequencenumber, function() {
                extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
            });
        },
        zoomIn: function() {
            map.removeAllLayers();
            extentFunction.zoomIn();
        },
        zoomOut: function() {
            map.removeAllLayers();
            extentFunction.zoomOut();
        },
        rangeUpdated: function() {
            if (!window.parent.isMapPinned(vizid)) {
                window.parent.mapViewShowLoading(vizid);
                map.disableMapNavigation();
                itemsExtentsReload3();
                needReload = false;
            } else if (placesLayer !== null) {
                refreshAndUpdateMap(panExtent, map.getZoom());
            } else {
                getUpdate2();
            }
        },
        toggleSelection: function() {
            if (dataFunction.isToolbarOn()) {
                dataFunction.deactivateToolbar();
            } else {
                dataFunction.activateToolbar(null, "PAN");
            }
        },
        toggleSearch: function() {
            searchFunction.toggleSearch();
        },
        combinedPlaceClicked: function(operation) {
            dataFunction.combinedPlaceClicked(operation);
        },
        newPlaceClicked: function(operation) {
            dataFunction.newPlaceClicked(operation);
        },
        updatedPlaceClicked: function(operation) {
            dataFunction.updatedPlaceClicked(operation);
        },
        associationClicked: function(associationKey, operation) {
            dataFunction.associationClicked(associationKey, operation);
        },
        placeClicked: function(placeid, typename, operation) {
            dataFunction.placeClicked(placeid, typename, operation);
        },
        trackClicked: function(trackid, typename, operation) {
            dataFunction.trackClicked(trackid, typename, operation);
        },
        setSelectionModePan: function() {
            dataFunction.setSelectionModePan();
        },
        setSelectionModeCircle: function() {
            dataFunction.setSelectionModeCircle();
        },
        setSelectionModeRectangle: function() {
            dataFunction.setSelectionModeRectangle();
        },
        setSelectionModePolygon: function() {
            dataFunction.setSelectionModePolygon();
        }
    });
});
