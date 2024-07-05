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
	"esri/layers/FeatureLayer",
	"dojo/_base/lang",
	"dojo/number",
	"esri/renderers/HeatmapRenderer"
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
	FeatureLayer,
	lang,
	number,
	HeatmapRenderer
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
        });
        map.on("mouse-move", function(evt) {
            if (evt.shiftKey && !evt.ctrlKey) {
                window.parent.desensitizeFloatingObjects(vizid);
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
            if (!searchFunction.isSearchBarOnFocus() && !document.hasFocus()) {
                homeButton.focus();
            }
        });
        map.on("click", function(evt) {
            if (dataFunction.isToolbarOn()) {
                dataFunction.deactivateToolbar();
            }
            window.parent.showHeader(vizid);
            window.parent.hideMenu(vizid);
            if (evt.ctrlKey === true || evt.metaKey === true) {
                !map.isPan && map.enablePan();
                !map.isRubberBandZoom && map.enableRubberBandZoom();
            }
        });
        map.on("mouse-out", function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            dataFunction.clearRubberband();
            if (dataFunction.isToolbarOn()) {
                dataFunction.deactivateToolbar();
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
        map.on("mouse-up", mapMouseDragEndHandler);
    }

    function getPointsIntersected(extent) {
        var pointsToManipulate = [];
        var featureHash = dataFunction.getFeatureHash();
        var spatialReference = new SpatialReference({
            wkid: 4326
        });
        Object.keys(featureHash).forEach(function(objectId) {
            var feature0 = featureHash[objectId];
            var isAMapPoint = 'spatialReference'in feature0.geometry;
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
            panEnd();
            zoomOrPanEnd();
            panExtent = null;
        }
        map.on('zoom-start', function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
            map.removeLayer(featureLayer);
            map.removeLayer(selectedFeatureLayer);
            zooming = true;
            window.setTimeout(function() {
                if (zooming) {
                    window.parent.mapViewShowLoading(vizid);
                    map.disableMapNavigation();
                }
            }, 150);
        });
        map.on('zoom-end', function(evt) {
            window.parent.sensitizeFloatingObjects(vizid);
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
            if (zoom == extentFunction.getCurrentZoom()) {
                var extent = extentFunction.getCurrentExtent();
                zoomOrPanEndWork();
                refreshMap(extent, zoom, f1callback);
            } else {
                extentFunction.setPaddingsUsingZoom(zoom);
                var extent = extentFunction.getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, map.extent.spatialReference.wkid);
                var newpaddingLeftStr = extentFunction.getNewPaddingLeft() + "px";
                var newpaddingRightStr = extentFunction.getNewPaddingRight() + "px";
                if (paddingLeftStr != newpaddingLeftStr || paddingRightStr != newpaddingRightStr) {
                    extentFunction.clearMapContainerDivPadding();
                    if (extentFunction.isPaddingsNotZero()) {
                        extentFunction.setMapDivContainerToNewPaddings();
                    }
                    zoomOrPanEndWork();
                    refreshMap(extent, zoom, f1callback);
                } else {
                    zoomOrPanEndWork();
                    refreshMap(extent, zoom, f1callback);
                }
            }
        });
        useNormalZoomOrPanEnd();
        mapPanAndZoomInitialized = true;
    }

    function f1callback() {
        map.reposition();
        zooming = false;
        window.parent.mapViewHideLoading(vizid);
        map.enableMapNavigation();
        map.disableClickRecenter();
        map.disableDoubleClickZoom();
        map.disableKeyboardNavigation();
        dataFunction.getUpdate();
    }
    var refreshingMap = false;

    function refreshMap(extent, zoom, callback) {
        if (!refreshingMap) {
            refreshingMap = true;
            var mapId = map.id;
            if (typeof extent == 'undefined' || extent == null) {
                extent = map.extent;
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
            featureLayer = null;
            selectedFeatureLayer = null;
            map = new Map(mapId,mapOptions);
            map.autoResize = false;
            map.disableClickRecenter();
            map.disableDoubleClickZoom();
            map.disableKeyboardNavigation();
            panEndEventHandler = null;
            dataFunction.setMap(map);
            dataFunction.setBasemap(basemapId, basemapUrl, basemapType, basemapOpacity);
            extentFunction.setMap(map);
            searchFunction.setMap(map);
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
            dataFunction.getUpdate();
        });
    }

    function initDocument() {
        document.addEventListener("contextmenu", function(evt) {
            evt.preventDefault();
            var screenPoint = new ScreenPoint(evt.clientX - extentFunction.getNewPaddingLeft(),evt.clientY);
            var mapPoint = map.toMap(screenPoint);
            currentDisplayPoint = mapPoint;
            deselectAllMenuItem.parentElement.style.display = "inline";
            selectAllMenuItem.parentElement.style.display = "inline";
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
            dialogShown = true;
            return false;
        });
    }

    function select() {
        dataFunction.startProcessingSelection();
        var geometry = dataFunction.getSelectionGeometry().getExtent();
        var points = getPointsIntersected(geometry);
        var response = {
            "features": points,
            "links": []
        };
        dataFunction.selectInBuffer(response);
    }

    function toggle(inBuffer, turnOn) {
        var k = 0;
        var featureHash = dataFunction.getFeatureHash();
        while (k < inBuffer.length) {
            var key = inBuffer[k];
            var f = featureHash[key];
            if (f) {
                var intValue = parseInt(key, 10);
                if (turnOn === null) {
                    if (selectedFeatureKeys.has(intValue)) {
                        selectedFeatureKeys.delete(intValue);
                        f.attributes.selected = false;
                    } else {
                        selectedFeatureKeys.add(intValue);
                        f.attributes.selected = true;
                    }
                } else if (turnOn) {
                    if (!selectedFeatureKeys.has(intValue)) {
                        selectedFeatureKeys.add(intValue);
                        f.attributes.selected = true;
                    }
                } else {
                    if (selectedFeatureKeys.has(intValue)) {
                        selectedFeatureKeys.delete(intValue);
                        f.attributes.selected = false;
                    }
                }
            }
            k++;
        }
        selectedFeatures = [];
        selectedFeatureKeys.forEach(function(value1, value2, set) {
            var feature = featureHash[value1];
            if (feature) {
                selectedFeatures.push(lang.clone(feature));
            }
        });
        createSelectedFeatureLayer();
        dataFunction.applyEditCallback();
    }

    function setSelected(inBuffer) {
        var featureHash = dataFunction.getFeatureHash();
        var oldSelectedFeatureLayer = selectedFeatureLayer;
        if (oldSelectedFeatureLayer != null) {
            oldSelectedFeatureLayer.suspend();
        }
        selectedFeatures = [];
        selectedFeatureKeys = new Set();
        var j = 0;
        while (j < inBuffer.length) {
            var key = inBuffer[j];
            var f = featureHash[key];
            if (f) {
                selectedFeatures.push(lang.clone(f));
                selectedFeatureKeys.add(parseInt(f.attributes.objectID));
            }
            j++;
        }
        createSelectedFeatureLayer();
        map.addLayer(selectedFeatureLayer);
        if (oldSelectedFeatureLayer != null) {
            map.removeLayer(oldSelectedFeatureLayer);
        }
        dataFunction.applyEditCallback();
    }
    var isMapHidden = false;
    var zoomOrPanEnd = null;

    function useSimpleZoomOrPanEnd() {
        zoomOrPanEnd = function() {
            useNormalZoomOrPanEnd();
        }
    }

    function useNormalZoomOrPanEnd() {
        zoomOrPanEnd = function() {
            checkGetUpdate2();
        }
    }

    function panEnd() {
        zoomOrPanEndWork();
        extentFunction.saveExtentAndZoom();
        panning = false;
    }

    function zoomEnd() {
        zoomOrPanEndWork();
        extentFunction.saveExtentAndZoom();
        window.parent.mapViewHideLoading(vizid);
        map.enableMapNavigation();
        map.disableClickRecenter();
        map.disableDoubleClickZoom();
        map.disableKeyboardNavigation();
        zooming = false;
    }

    function checkGetUpdate2() {
        if (itemsInViz > frontendZoomThreshold && !refreshingMap) {
            if (featureLayer !== null) {
                refreshMap(panExtent);
            }
            dataFunction.getUpdate();
        }
    }
    var emptyLayer = null;
    var featureLayer = null;
    var selectedFeatureLayer = null;
    var isResizeHappend = false;
    var doRefreshMap = false;
    var useSummary = false;
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
        if (jsonFS) {
            cacheNotAvailable = jsonFS.cacheNotAvailable;
            if (sequenceNumber == null || sequenceNumber < jsonFS.sequenceNumber) {
                sequenceNumber = jsonFS.sequenceNumber;
            }
            useSummary = false;
            summaryLevel = detailLevel;
            useMultitypeDecorator = jsonFS.useMultitypeDecorator;
            useLinkupDecorator = jsonFS.useLinkupDecorator;
            window.parent.setUseSummary(vizid, useSummary && summaryLevel != detailLevel);
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
            } else if (jsonFS.typeLimitReached) {
                window.parent.hideSpinner(vizid);
                window.parent.mapViewHideLoading(vizid);
                window.parent.typeLimitReached(vizid);
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
                            itemsExtentsReload2(true);
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
                    setFeatureLayers(jsonFS);
                    refreshingMap = false;
                    if (!mapPanAndZoomInitialized) {
                        initializeMapPanAndZoom();
                    }
                    if (itemsInViz > 1000) {
                        setTimeout(function() {
                            window.parent.mapViewHideLoading(vizid);
                            map.enableMapNavigation();
                            map.disableClickRecenter();
                            map.disableDoubleClickZoom();
                            map.disableKeyboardNavigation();
                            dataFunction.setLoading(false);
                        }, 300);
                    } else {
                        window.parent.mapViewHideLoading(vizid);
                        map.enableMapNavigation();
                        map.disableClickRecenter();
                        map.disableDoubleClickZoom();
                        map.disableKeyboardNavigation();
                        dataFunction.setLoading(false);
                    }
                }
            }
        }
    }

    function initializeValues(jsonFS) {
        window.parent.hideBackButton(vizid);
        var features = jsonFS.layer.features;
        maxValue = null;
        minValue = null;
        for (var i = 0; i < features.length; i++) {
            var feature = features[i];
            var attributes = feature.attributes;
            var value = attributes.hits;
            if (maxValue && maxValue != null) {
                if (maxValue < value) {
                    maxValue = value;
                }
            } else {
                maxValue = value;
            }
            if (minValue && minValue != null) {
                if (minValue > value) {
                    minValue = value;
                }
            } else {
                minValue = value;
            }
        }
        if (minValue == maxValue)
            minValue = minValue - 1;
        dataFunction.newFeatureHash();
        var featureHash = dataFunction.getFeatureHash();
        selectedFeatures = [];
        selectedFeatureKeys = new Set();
        var spatialReference = new SpatialReference({
            "wkid": 4326
        });
        for (var i = 0; i < features.length; i++) {
            var f = features[i];
            var geometry = new Point(f.geometry.x,f.geometry.y,spatialReference);
            f.geometry = geometry;
            var attributes = f.attributes;
            var value = attributes.hits;
            attributes["Intensity"] = value - minValue;
            attributes.selected = false;
            var k = attributes.objectID;
            featureHash[k] = f;
        }
        i = 0;
        while (i < jsonFS.layer.selectedFeatures.length) {
            var k = jsonFS.layer.selectedFeatures[i];
            var f = featureHash[k];
            f.attributes.selected = true;
            selectedFeatures.push(lang.clone(f));
            selectedFeatureKeys.add(parseInt(k));
            i++;
        }
        var emptyColor = Color.fromHex("#ff8300");
        emptyColor.a = 0;
        var fullColor = Color.fromHex("#ff8300");
        selectedRenderer = new HeatmapRenderer({
            blurRadius: blurCtrl.value,
            maxPixelIntensity: maxCtrl.value - minValue,
            minPixelIntensity: minCtrl.value - minValue,
            field: "Intensity"
        });
        var heatmapColors = jsonFS.heatmapColors;
        var firstColor = Color.fromHex(heatmapColors[0]);
        firstColor.a = 0;
        var secondColor = Color.fromHex(heatmapColors[0]);
        var thirdColor = Color.fromHex(heatmapColors[1]);
        var fourthColor = Color.fromHex(heatmapColors[2]);
        var fifthColor = Color.fromHex(heatmapColors[3]);
        var sixthColor = Color.fromHex(heatmapColors[4]);
        renderer = new HeatmapRenderer({
            colors: [firstColor, secondColor, thirdColor, fourthColor, fifthColor, sixthColor],
            blurRadius: blurCtrl.value,
            maxPixelIntensity: maxCtrl.value - minValue,
            minPixelIntensity: minCtrl.value - minValue,
            field: "Intensity"
        });
        var sliders = document.querySelectorAll(".blurInfo p~input[type=range]");
        var addLiveValue = function(ctrl) {
            var val = ctrl.previousElementSibling.querySelector("span");
            ctrl.addEventListener("input", function(evt) {
                val.innerHTML = evt.target.value;
            });
        };
        for (var i = 0; i < sliders.length; i++) {
            addLiveValue(sliders.item(i));
        }
        blurCtrl.addEventListener("change", function(evt) {
            var r = +evt.target.value;
            dojo.xhrPost({
                url: "rest/map/setHeatmapBlurValue/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: r,
                handleAs: "text"
            });
            if (r !== renderer.blurRadius) {
                renderer.blurRadius = r;
                featureLayer.redraw();
            }
            if (r !== selectedRenderer.blurRadius) {
                selectedRenderer.blurRadius = r;
                selectedFeatureLayer.redraw();
            }
        });
        maxCtrl.addEventListener("change", function(evt) {
            var r = +evt.target.value;
            var minCtrlValue = parseFloat(minCtrl.value);
            if (r <= minCtrlValue) {
                r = minCtrlValue + 1;
                maxValueCtrl.innerHTML = r;
                maxCtrl.value = r;
            }
            dojo.xhrPost({
                url: "rest/map/setHeatmapMaxValue/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: r,
                handleAs: "text"
            });
            var newValue = r - minValue;
            if (newValue !== renderer.maxPixelIntensity) {
                renderer.maxPixelIntensity = newValue;
                featureLayer.redraw();
            }
            if (newValue !== selectedRenderer.maxPixelIntensity) {
                selectedRenderer.maxPixelIntensity = newValue;
                selectedFeatureLayer.redraw();
            }
        });
        minCtrl.addEventListener("change", function(evt) {
            var r = +evt.target.value;
            var maxCtrlValue = parseFloat(maxCtrl.value);
            if (r >= maxCtrlValue) {
                r = maxCtrlValue - 1;
                minValueCtrl.innerHTML = r;
                minCtrl.value = r;
            }
            dojo.xhrPost({
                url: "rest/map/setHeatmapMinValue/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: r,
                handleAs: "text"
            });
            var newValue = r - minValue;
            if (newValue !== renderer.minPixelIntensity) {
                renderer.minPixelIntensity = newValue;
                featureLayer.redraw();
            }
            if (newValue !== selectedRenderer.minPixelIntensity) {
                selectedRenderer.minPixelIntensity = newValue;
                selectedFeatureLayer.redraw();
            }
        });
        var savedBlurValue = jsonFS.blurValue;
        if (savedBlurValue) {
            blurValueCtrl.innerHTML = savedBlurValue;
            blurCtrl.value = savedBlurValue;
            selectedRenderer.blurRadius = savedBlurValue;
            renderer.blurRadius = savedBlurValue;
        }
        var maxNumber = maxValue;
        var minNumber = minValue;
        var savedMaxValue = parseFloat(jsonFS.maxValue);
        if (typeof savedMaxValue != 'undefined' && savedMaxValue != null && !isNaN(savedMaxValue)) {
            maxNumber = savedMaxValue;
        }
        var savedMinValue = parseFloat(jsonFS.minValue);
        if (typeof savedMinValue != 'undefined' && savedMinValue != null && !isNaN(savedMinValue)) {
            minNumber = savedMinValue;
            if (minValue > savedMinValue) {
                minValue = savedMinValue;
            }
        }
        maxCtrl.max = maxNumber * 10;
        maxCtrl.min = minNumber;
        maxValueCtrl.innerHTML = maxNumber;
        maxCtrl.value = maxNumber;
        selectedRenderer.maxPixelIntensity = maxNumber - minValue;
        renderer.maxPixelIntensity = maxNumber - minValue;
        minCtrl.max = maxNumber * 10;
        minCtrl.min = minNumber;
        minValueCtrl.innerHTML = minNumber;
        minCtrl.value = minNumber;
        selectedRenderer.minPixelIntensity = minNumber - minValue;
        renderer.minPixelIntensity = minNumber - minValue;
    }

    function dontUpdateLegend() {
    }

    function deselect() {
        var geometry = dataFunction.getSelectionGeometry().getExtent();
        var points = getPointsIntersected(geometry);
        var response = {
            "features": points,
            "links": []
        };
        dataFunction.deselectInBuffer(response)
    }

    function toggleNodes(nodes, turnOn) {
        toggle(nodes, turnOn);
    }

    function itemsExtentsReload() {
        xhr.get({
            url: "rest/map/getItemsInViz/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            handleAs: "json",
            load: function(newItemsInViz) {
                if (newItemsInViz > frontendZoomThreshold) {
                    xhr.get({
                        url: "rest/map/getExtentInfo/" + qs("dv_uuid") + "/" + qs("viz_uuid") + "/" + getNextSequenceNumber(),
                        handleAs: "json",
                        load: function(newExtentInfo) {
                            extentFunction.setMapExtentInfo(newExtentInfo.newExtent, newExtentInfo.initialExtent, newExtentInfo.extent);
                            var mapRefreshed = extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
                            if (!mapRefreshed) {
                                dataFunction.getUpdate();
                            }
                        }
                    });
                } else {
                    if (itemsInViz > frontendZoomThreshold) {
                        xhr.get({
                            url: "rest/map/getExtentInfo/" + qs("dv_uuid") + "/" + qs("viz_uuid") + "/" + getNextSequenceNumber(),
                            handleAs: "json",
                            load: function(newExtentInfo) {
                                var zoom = newExtentInfo.initialExtent.zoom;
                                if (zoom == -1) {
                                    zoom = 0;
                                }
                                var maxWidth = 256 * Math.pow(2, zoom);
                                while (mapDivContainer.offsetWidth > maxWidth) {
                                    zoom++;
                                    maxWidth = 256 * Math.pow(2, zoom);
                                }
                                var wgs = new SpatialReference({
                                    "wkid": 4326
                                });
                                var initialExtent = newExtentInfo.initialExtent;
                                var extent = new Extent(initialExtent.xmin,initialExtent.ymin,initialExtent.xmax,initialExtent.ymax,wgs);
                                refreshAndUpdateMap(extent, zoom);
                            }
                        });
                    } else {
                        xhr.get({
                            url: "rest/map/getExtentInfo/" + qs("dv_uuid") + "/" + qs("viz_uuid") + "/" + getNextSequenceNumber(),
                            handleAs: "json",
                            load: function(newExtentInfo) {
                                extentFunction.setMapExtentInfo(newExtentInfo.newExtent, newExtentInfo.initialExtent, newExtentInfo.extent);
                                var mapRefreshed = extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
                                if (!mapRefreshed) {
                                    dataFunction.getUpdate();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    var mapDiv;
    var mapDivContainer;
    var cmenu;
    var deselectAllMenuItem;
    var selectAllMenuItem;
    var dialogShown = false;
    var inDialog = false;
    var heatmapDiv;
    var layerDefinition;
    var fs;
    var maxValue;
    var minValue;
    var selectedFeatures = null;
    var selectedFeatureKeys = null;
    var selectedRenderer;
    var configHeatmapIsHidden = true;
    var blurValueCtrl;
    var blurCtrl;
    var maxValueCtrl;
    var maxCtrl;
    var minValueCtrl;
    var minCtrl;
    var blurCheckbox;
    var z;

    function zoomOrPanEndWork() {
        if (blurCheckbox.checked) {
            var zDifference = map.getZoom() - z;
            var multiplier = Math.pow(2, zDifference);
            var multiplierSquared = Math.pow(multiplier, 2);
            renderer.blurRadius *= multiplier;
            renderer.maxPixelIntensity *= multiplierSquared;
            renderer.minPixelIntensity *= multiplierSquared;
            if (featureLayer != null) {
                featureLayer.redraw();
                blurValueCtrl.innerHTML = renderer.blurRadius;
                blurCtrl.value = renderer.blurRadius;
                maxValueCtrl.innerHTML = renderer.maxPixelIntensity;
                if (maxCtrl.max < renderer.maxPixelIntensity) {
                    maxCtrl.max = renderer.maxPixelIntensity;
                }
                maxCtrl.value = renderer.maxPixelIntensity;
                minValueCtrl.innerHTML = renderer.minPixelIntensity;
                minCtrl.value = renderer.minPixelIntensity;
            }
            if (selectedFeatureLayer != null) {
                selectedRenderer.blurRadius *= multiplier;
                selectedRenderer.maxPixelIntensity *= multiplierSquared;
                selectedRenderer.minPixelIntensity *= multiplierSquared;
                selectedFeatureLayer.redraw();
            }
            z = map.getZoom();
            currentExtent = map.extent;
            var data = {
                blur: renderer.blurRadius,
                max: renderer.maxPixelIntensity,
                min: renderer.minPixelIntensity
            };
            dojo.xhrPost({
                url: "rest/map/setHeatmapValues/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: data,
                handleAs: "text"
            });
        }
    }

    function processSelectedLinks(a) {
    }

    function setFeatureLayers(jsonFS) {
        initializeValues(jsonFS);
        layerDefinition = {
            "geometryType": "esriGeometryPoint",
            "spatialReference": {
                "wkid": 4326
            },
            "fields": [{
                "name": "objectID",
                "alias": "ObjectID",
                "type": "esriFieldTypeOID"
            }],
            "outFields": ["hits"]
        };
        var layer = {};
        layer.geometryType = "esriGeometryPoint";
        layer.features = [];
        var featureHash = dataFunction.getFeatureHash();
        Object.keys(featureHash).forEach(function(k) {
            var f = featureHash[k];
            layer.features.push(f);
        });
        fs = new FeatureSet(layer);
        createFeatureLayer();
        createSelectedFeatureLayer();
    }

    function createFeatureLayer() {
        var featureCollection = {
            layerDefinition: layerDefinition,
            featureSet: fs
        };
        var newFeatureLayer = new FeatureLayer(featureCollection);
        newFeatureLayer.setRenderer(renderer);
        map.addLayer(newFeatureLayer);
        if (featureLayer != null) {
            map.removeLayer(featureLayer);
        }
        featureLayer = newFeatureLayer;
    }

    function createSelectedFeatureLayer() {
        var selectedFs = new FeatureSet();
        selectedFs.features = selectedFeatures;
        selectedFs.exceededTransferLimit = fs.exceededTransferLimit;
        selectedFs.fieldAliases = fs.fieldAliases;
        selectedFs.geometryType = fs.geometryType;
        selectedFs.spatialReference = fs.spatialReference;
        var selectedFeatureCollection = {
            layerDefinition: layerDefinition,
            featureSet: selectedFs
        };
        if (selectedFeatureLayer != null) {
        }
        var newSelectedFeatureLayer = new FeatureLayer(selectedFeatureCollection);
        newSelectedFeatureLayer.setRenderer(selectedRenderer);
        map.addLayer(newSelectedFeatureLayer);
        if (selectedFeatureLayer != null) {
            map.removeLayer(selectedFeatureLayer);
        }
        selectedFeatureLayer = newSelectedFeatureLayer;
    }

    function toggleConfigHeatmap() {
        if (configHeatmapIsHidden) {
            heatmapDiv.style.display = "inline";
            heatmapDiv.style.position = "absolute";
            heatmapDiv.style.left = (map.width - 284) + "px";
            heatmapDiv.style.top = 20;
            configHeatmapIsHidden = false;
        } else {
            heatmapDiv.style.display = "none";
            configHeatmapIsHidden = true;
        }
    }
    var needReload = false;
    var detailLevel;

    function setNotInDialog() {
        cmenu.style.display = "none";
        dialogShown = false;
        inDialog = false;
    }

    function setInDialog() {
        inDialog = true;
    }
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
            mapDivContainer = dom.byId(params.mapDivContainerId);
            extentFunction = new ExtentFunction(dvid,vizid,refreshAndUpdateMap,getNextSequenceNumber,mapDivContainer);
            extentFunction.setMinZoom(4);
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
                    var span = zoomAndExtent.extent.xmax - zoomAndExtent.extent.xmin;
                    var wholeWorldX = extentFunction.getWholeWorldX(102100);
                    if (span > wholeWorldX * 0.99) {
                        zoomAndExtent.extent.xmax = extentFunction.getXMax2D(102100);
                        zoomAndExtent.extent.xmin = extentFunction.getXMin2D(102100);
                    }
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
            cmenu = dom.byId(params.cmenuId);
            on(cmenu, "mouseenter", setInDialog);
            on(cmenu, "mouseleave", setNotInDialog);
            deselectAllMenuItem = dom.byId(params.deselectAllId);
            on(deselectAllMenuItem, "click", this.deselectAll);
            selectAllMenuItem = dom.byId(params.selectAllId);
            on(selectAllMenuItem, "click", this.selectAll);
            heatmapDiv = dom.byId(params.heatmapDivId);
            heatmapDiv.style.display = "none";
            configHeatmapIsHidden = true;
            blurValueCtrl = dom.byId("blurValue");
            blurCtrl = dom.byId("blurControl");
            maxValueCtrl = dom.byId("maxValue");
            maxCtrl = dom.byId("maxControl");
            minValueCtrl = dom.byId("minValue");
            minCtrl = dom.byId("minControl");
            blurCheckbox = dom.byId("blurCheckbox");
            on(blurCheckbox, "change", function() {
                z = map.getZoom();
            });
            window.parent.disableLegend(vizid);
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
                itemsExtentsReload(true);
                needReload = false;
            }
        },
        deselectAll: function() {
            dataFunction.deselectAll();
            cmenu.style.display = "none";
        },
        selectAll: function() {
            dataFunction.selectAll();
            cmenu.style.display = "none";
        },
        applySelection: function() {
            dontUpdateLegend();
            dataFunction.getUpdate();
        },
        resizeMapDiv: function() {
            if (mapDivContainer.offsetWidth > 0) {
                if (!configHeatmapIsHidden) {
                    toggleConfigHeatmap();
                }
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
                        var currentExtent = extentFunction.getCurrentExtent();
                        if (currentExtent != null)
                            extentOut = extentFunction.getRightExtent(currentExtent, map.extent.spatialReference.wkid);
                        else
                            extentOut = extentFunction.getRightExtent(extentFunction.getInitialExtent(), map.extent.spatialReference.wkid);
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
                    refreshAndUpdateMap(extent, zoom);
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
        zoomHome: function() {
            extentFunction.revertToInitialExtent(map.extent.spatialReference.wkid);
        },
        zoomIn: function() {
            extentFunction.zoomIn();
        },
        zoomOut: function() {
            extentFunction.zoomOut();
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
        heatmapPanel: function() {
            toggleConfigHeatmap();
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
