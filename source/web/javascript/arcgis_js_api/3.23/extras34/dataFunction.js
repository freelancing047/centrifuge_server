define(["dojo/_base/declare",
    'dojo/_base/xhr',
    "esri/toolbars/draw",
    "esri/symbols/SimpleMarkerSymbol",
    "esri/symbols/SimpleLineSymbol",
    "esri/symbols/SimpleFillSymbol",
    "esri/graphic",
    "esri/geometry/Point",
    "esri/geometry/Extent",
    "esri/geometry/Circle",
    "esri/geometry/Polygon",
    "dijit/Dialog",
    "esri/basemaps",
    "esri/Color"
  ],
  function(declare,
    xhr,
    Draw,
    SimpleMarkerSymbol,
    SimpleLineSymbol,
    SimpleFillSymbol,
    Graphic,
    Point,
    Extent,
    Circle,
    Polygon,
    Dialog,
    esriBasemaps,
    Color
  ) {
    var map;
    var dvid;
    var vizid;
    var useData;
    var dontUpdateLegend;
    var toggle;
    var setSelected;
    var select;
    var deselect;
    var loading = false;
    //  var currentBasemap;
    var currentBasemapUrl = null;
    var currentBasemapId = null;
    var applyEditInProgress = false;
    var nextCommand = "";
    var featureHash;
    var linkHash;
    var toolbar;
    var toolbarOn = false;
    var selectionGeometry;
    var selectionGraphic;
    var rubberbandGraphic = null;
    var rubberbandGraphicPolygon = null;
    var rubberbandStartPoint;
    var rubberbandEndPoint;
    var rubberband;
    var rubberbandVertices = null;
    var processingSelection = false;
    var frontendToggleThreshold = 1000;
    var selectionQuery;
    var toggleNodes;
    var selectedColorString = "#ff8300";
    var getNextSequenceNumber;
    var processSelectedLinks;
    var selectionMode = "pan";

    function turnToolbarOn(widget, toolName) {
        toolbarOn = true;
        //circleButton.style.visibility = "hidden";
        //extentButton.style.visibility = "hidden";
        //polygonButton.style.visibility = "hidden";
        var tool;
        if (widget == null) {
            tool = toolName;
        } else {
            if (widget.value) {
                tool = widget.value.toUpperCase().replace(/ /g, "_");
            } else {
                if (widget.id == "extentButton") {
                    tool = "PAN";
                }
            }
        }
        toolbar.activate(Draw[tool]);
        map.hideZoomSlider();
    }

    function turnToolbarOff() {
        toolbarOn = false;
        toolbar.deactivate();
        map.showZoomSlider();
    }

    function postNodeSelected(inBuffer) {
        dojo.xhrPost({
            url: "rest/map/setNodeSelected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            postData: dojo.toJson(inBuffer),
            handleAs: "text",
            load: function(data) {
                if (data != 'failure') {
                    var size = parseInt(data)
                    if (size > frontendToggleThreshold) {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    } else {
                        toggle(inBuffer, true);
                    }
                }
                if (selectionGraphic) {
                    map.graphics.remove(selectionGraphic);
                    selectionGraphic = null;
                }
            }
        });
    }

    function postNodeDeselected(inBuffer) {
        dojo.xhrPost({
            url: "rest/map/setNodeDeselected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            postData: dojo.toJson(inBuffer),
            handleAs: "text",
            load: function(data) {
                if (data != 'failure') {
                    var size = parseInt(data)
                    if (size > frontendToggleThreshold) {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    } else {
                        toggle(inBuffer, false);
                    }
                    if (selectionGraphic) {
                        map.graphics.remove(selectionGraphic);
                        selectionGraphic = null;
                    }
                }
            }
        });
    }
    var lastSequenceNumber;

    function __getUpdate() {
        lastSequenceNumber = getNextSequenceNumber();
        __getUpdate12(lastSequenceNumber);
    }

    function __getUpdate12(sequenceNumber) {
        if (typeof checkUpdate == 'undefined' || checkUpdate == null) {
            checkUpdate = true;
        }
        loading = true;
        window.parent.mapViewShowLoading(vizid);
        map.disableMapNavigation();
        xhr.get({
            url: "rest/map/getUpdate/" + dvid + "/" + vizid + "/" + sequenceNumber,
            handleAs: "json",
            load: function(data) {
                window.parent.mapViewHideLoading(vizid);
                map.enableMapNavigation();
                map.disableClickRecenter();
                if (sequenceNumber != lastSequenceNumber) {
                    useData(null);
                } else {
                    useData(data);
                }
            }
        });
    }

    function __getUpdate2(minx, miny, maxx, maxy) {
        lastSequenceNumber = getNextSequenceNumber();
        __getUpdate22(minx, miny, maxx, maxy, lastSequenceNumber);
    }

    function __getUpdate22(minx, miny, maxx, maxy, sequenceNumber) {
        loading = true;
        window.parent.mapViewShowLoading(vizid);
        map.disableMapNavigation();
        var postdata = [minx, miny, maxx, maxy];
        dojo.xhrPost({
            url: "rest/map/getUpdate2/" + dvid + "/" + vizid + "/" + sequenceNumber,
            postData: dojo.toJson(postdata),
            handleAs: "json",
            load: function(data) {
                window.parent.mapViewHideLoading(vizid);
                map.enableMapNavigation();
                map.disableClickRecenter();
                if (sequenceNumber != lastSequenceNumber) {
                    useData(null);
                } else {
                    useData(data);
                }
                window.parent.refreshMetrics(vizid);
            }
        });
    }

    function __getUpdate3() {
        lastSequenceNumber = getNextSequenceNumber();
        __getUpdate32(lastSequenceNumber);
    }

    function __getUpdate32(sequenceNumber) {
        if (typeof checkUpdate == 'undefined' || checkUpdate == null) {
            checkUpdate = true;
        }
        loading = true;
        window.parent.mapViewShowLoading(vizid);
        map.disableMapNavigation();
        xhr.get({
            url: "rest/map/getUpdate3/" + dvid + "/" + vizid + "/" + sequenceNumber,
            handleAs: "json",
            load: function(data) {
                window.parent.mapViewHideLoading(vizid);
                map.enableMapNavigation();
                map.disableClickRecenter();
                if (sequenceNumber != lastSequenceNumber) {
                    useData(null);
                } else {
                    useData(data);
                }
            }
        });
    }

    function __applyEditCallback() {
        if (loading) {
            nextCommand = "";
            applyEditInProgress = false;
        } else if (nextCommand == "applySelection") {
            nextCommand = "";
            applyEditInProgress = false;
            __applySelection();
        } else if (nextCommand == "selectAll") {
            nextCommand = "";
            applyEditInProgress = false;
            __selectAll();
        } else if (nextCommand == "deselectAll") {
            nextCommand = "";
            applyEditInProgress = false;
            __deselectAll();
        } else if (nextCommand.startsWith("combinedPlaceClicked")) {
            var arr = nextCommand.split(";");
            var operation = arr[1];
            nextCommand = "";
            applyEditInProgress = false;
            __combinedPlaceClicked(operation);
        } else if (nextCommand.startsWith("newPlaceClicked")) {
            var arr = nextCommand.split(";");
            var operation = arr[1];
            nextCommand = "";
            applyEditInProgress = false;
            __newPlaceClicked(operation);
        } else if (nextCommand.startsWith("updatedPlaceClicked")) {
            var arr = nextCommand.split(";");
            var operation = arr[1];
            nextCommand = "";
            applyEditInProgress = false;
            __updatedPlaceClicked(operation);
        } else if (nextCommand.startsWith("associationClicked")) {
            var arr = nextCommand.split(";");
            var associationKey = arr[1];
            var operation = arr[2];
            nextCommand = "";
            applyEditInProgress = false;
            __associationClicked(associationKey, operation);
        } else if (nextCommand.startsWith("placeClicked")) {
            var arr = nextCommand.split(";");
            var placeid = arr[1]
            var typename = arr[2];
            var operation = arr[3];
            nextCommand = "";
            applyEditInProgress = false;
            __placeClicked(placeid, typename, operation);
        } else if (nextCommand.startsWith("trackClicked")) {
            var arr = nextCommand.split(";");
            var trackid = arr[1]
            var typename = arr[2];
            var operation = arr[3];
            nextCommand = "";
            applyEditInProgress = false;
            __trackClicked(trackid, typename, operation);
        } else {
            applyEditInProgress = false;
        }
    }

    function __applySelection() {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "applySelection";
        } else {
            applyEditInProgress = true;
            xhr.get({
                url: "rest/map/getSelected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                handleAs: "json",
                load: function(response) {
                    setSelected(response.nodeSelection);
                    selectLinks(response.linkSelection);
                }
            });
        }
    }

    function __selectAll() {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "selectAll";
        } else {
            applyEditInProgress = true;
            dojo.xhrPost({
                url: "rest/map/selectAll/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __combinedPlaceClicked(operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "combinedPlaceClicked;" + operation;
        } else {
            applyEditInProgress = true;
            dojo.xhrPost({
                url: "rest/map/combinedPlaceClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber() + "/" + operation,
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __newPlaceClicked(operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "newPlaceClicked;" + operation;
        } else {
            applyEditInProgress = true;
            dojo.xhrPost({
                url: "rest/map/newPlaceClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber() + "/" + operation,
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __updatedPlaceClicked(operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "updatedPlaceClicked;" + operation;
        } else {
            applyEditInProgress = true;
            dojo.xhrPost({
                url: "rest/map/updatedPlaceClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber() + "/" + operation,
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __associationClicked(associationKey, operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "associationClicked;" + associationKey + ";" + operation;
        } else {
            applyEditInProgress = true;
            var postdata = {
                "id": 0,
                "typename": associationKey,
                "operation": operation
            };
            dojo.xhrPost({
                url: "rest/map/associationClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(postdata),
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __placeClicked(placeid, typename, operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "placeClicked;" + placeid + ";" + typename + ";" + operation;
        } else {
            applyEditInProgress = true;
            var postdata = {
                "id": placeid,
                "typename": typename,
                "operation": operation
            };
            dojo.xhrPost({
                url: "rest/map/placeClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(postdata),
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __trackClicked(trackid, typename, operation) {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "trackClicked;" + trackid + ";" + typename + ";" + operation;
        } else {
            applyEditInProgress = true;
            var postdata = {
                "id": trackid,
                "typename": typename,
                "operation": operation
            };
            dojo.xhrPost({
                url: "rest/map/trackClicked/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(postdata),
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function __deselectAll() {
        if (loading) {// do nothing
        } else if (applyEditInProgress) {
            nextCommand = "deselectAll";
        } else {
            applyEditInProgress = true;
            dojo.xhrPost({
                url: "rest/map/deselectAll/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                handleAs: "text",
                load: function(data) {
                    if (data == 'success') {
                        //toggle(Object.keys(featureHash), false);
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    }
                }
            });
        }
    }

    function selectLinks(linkResponse) {
        if (linkResponse != null) {
            processSelectedLinks(linkResponse);
        }
    }

    function postSelected(nodes, links) {
        //    window.parent.mapViewShowLoading(vizid);
        var postdata = {
            "nodes": nodes,
            "links": links
        };
        dojo.xhrPost({
            url: "rest/map/setSelected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            postData: dojo.toJson(postdata),
            handleAs: "json",
            load: function(response) {
                //        window.parent.mapViewHideLoading(vizid);
                if (response.numNodeChanged > 0) {
                    if (response.numNodeChanged > frontendToggleThreshold) {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    } else {
                        //            window.parent.mapViewShowLoading(vizid);
                        toggleNodes(nodes, true);
                        selectLinks(response.linkResponse);
                        //            window.parent.mapViewHideLoading(vizid);
                    }
                } else {
                    selectLinks(response.linkResponse);
                }
                processingSelection = false;
                if (selectionGraphic) {
                    map.graphics.remove(selectionGraphic);
                    selectionGraphic = null;
                }
            }
        });
    }

    function postDeselected(nodes, links) {
        //    window.parent.mapViewShowLoading(vizid);
        var numItems = nodes.length + links.length;
        var postdata = {
            "nodes": nodes,
            "links": links
        };
        dojo.xhrPost({
            url: "rest/map/setDeselected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            postData: dojo.toJson(postdata),
            handleAs: "json",
            load: function(response) {
                //        window.parent.mapViewHideLoading(vizid);
                if (response.numNodeChanged > 0) {
                    if (response.numNodeChanged > frontendToggleThreshold) {
                        dontUpdateLegend();
                        __getUpdate3();
                        __applyEditCallback();
                    } else {
                        //            window.parent.mapViewShowLoading(vizid);
                        toggleNodes(nodes, false);
                        selectLinks(response.linkResponse);
                        //            window.parent.mapViewHideLoading(vizid);
                    }
                } else {
                    selectLinks(response.linkResponse);
                }
                processingSelection = false;
                if (selectionGraphic) {
                    map.graphics.remove(selectionGraphic);
                    selectionGraphic = null;
                }
            }
        });
    }

    function getNodes(features, turnOn) {
        var nodes = [];
        for (var i = 0; i < features.length; i++) {
            feature = features[i];
            if (selectionGeometry.type == "extent" || selectionGeometry.contains(feature.geometry)) {
                if (turnOn) {
                    if (!feature.attributes.selected) {
                        nodes.push(feature.attributes.objectID);
                    }
                } else {
                    if (feature.attributes.selected) {
                        nodes.push(feature.attributes.objectID);
                    }
                }
            }
        }
        return nodes;
    }
    return declare(null, {
        constructor: function(mapIn, dvidIn, vizidIn, useDataIn, toggleIn, setSelectedIn, selectIn, deselectIn, dontUpdateLegendIn, toggleNodesIn, getNextSequenceNumberIn, processSelectedLinksIn) {
            map = mapIn;
            dvid = dvidIn;
            vizid = vizidIn;
            useData = useDataIn;
            toggle = toggleIn;
            setSelected = setSelectedIn;
            select = selectIn;
            deselect = deselectIn;
            dontUpdateLegend = dontUpdateLegendIn;
            toggleNodes = toggleNodesIn;
            getNextSequenceNumber = getNextSequenceNumberIn;
            processSelectedLinks = processSelectedLinksIn;
        },
        setMap: function(mapIn) {
            map = mapIn;
        },
        getUpdate: function() {
            __getUpdate();
        },
        getUpdate2: function(minx, miny, maxx, maxy) {
            __getUpdate2(minx, miny, maxx, maxy);
        },
        setBasemap: function(basemapId, basemapUrl, basemapType, basemapOpacity) {
            if (currentBasemapUrl != basemapUrl) {
                currentBasemapUrl = basemapUrl;
                if (basemapUrl === "") {
                    if (currentBasemapId != basemapId) {
                        currentBasemapId = basemapId;
                        map.setBasemap(basemapId);
                    }
                } else {
                    if (currentBasemapUrl != "erehwon") {
                        if (basemapType == "OpenStreetMap") {
                            esriBasemaps[basemapId] = {
                                baseMapLayers: [{
                                    "title": "OSM",
                                    "type": "OpenStreetMap",
                                    "layerType": "OpenStreetMap",
                                    "opacity": basemapOpacity,
                                    "visibility": true,
                                    "id": "OpenStreetMap"
                                }],
                                thumbnailUrl: "",
                                title: basemapId
                            };
                        } else {
                            esriBasemaps[basemapId] = {
                                baseMapLayers: [{
                                    "url": basemapUrl,
                                    "opacity": basemapOpacity
                                }],
                                thumbnailUrl: "",
                                title: basemapId
                            };
                        }
                        if (currentBasemapId != basemapId) {
                            currentBasemapId = basemapId;
                            map.setBasemap(basemapId);
                        }
                    }
                }
            }
        },
        deselectAll: function() {
            __deselectAll();
        },
        selectAll: function() {
            __selectAll();
        },
        combinedPlaceClicked: function(operation) {
            __combinedPlaceClicked(operation);
        },
        newPlaceClicked: function(operation) {
            __newPlaceClicked(operation);
        },
        updatedPlaceClicked: function(operation) {
            __updatedPlaceClicked(operation);
        },
        associationClicked: function(associationKey, operation) {
            __associationClicked(associationKey, operation);
        },
        placeClicked: function(placeid, typename, operation) {
            __placeClicked(placeid, typename, operation);
        },
        trackClicked: function(trackid, typename, operation) {
            __trackClicked(trackid, typename, operation);
        },
        applySelection: function() {
            __applySelection();
        },
        applyEditCallback: function() {
            __applyEditCallback();
        },
        getFeatureHash: function() {
            return featureHash;
        },
        getLinkHash: function() {
            return linkHash;
        },
        newFeatureHash: function() {
            featureHash = {};
        },
        newLinkHash: function() {
            linkHash = {};
        },
        createToolbar: function() {
            toolbar = new Draw(map,{
                showTooltips: false
            });
            toolbar.on("draw-end", function(evt) {
                turnToolbarOff();
                //circleButton.style.visibility = "visible";
                //extentButton.style.visibility = "visible";
                //polygonButton.style.visibility = "visible";
                var selectionSymbol;
                switch (evt.geometry.type) {
                case "point":
                case "multipoint":
                    selectionSymbol = new SimpleMarkerSymbol();
                    break;
                case "polyline":
                    selectionSymbol = new SimpleLineSymbol();
                    break;
                default:
                    selectionSymbol = new SimpleFillSymbol();
                    break;
                }
                selectionGeometry = evt.geometry;
                selectionGraphic = new Graphic(selectionGeometry,selectionSymbol);
                map.graphics.add(selectionGraphic);
                select();
            });
        },
        activateToolbar: function(evt, toolName) {
            if (!processingSelection) {
                if (toolName == null) {
                    turnToolbarOn(this, null);
                } else {
                    turnToolbarOn(null, toolName);
                }
            }
        },
        deactivateToolbar: function() {
            turnToolbarOff();
        },
        isToolbarOn: function() {
            return toolbarOn;
        },
        setSelectionQuery: function(value) {
            selectionQuery = value;
        },
        selectInBuffer: function(response) {
            var features = response.features;
            var nodes = getNodes(features, true);
            var links = response.links;
            selectionQuery = null;
            postSelected(nodes, links);
        },
        selectInBuffer2: function(inBuffer) {
            postNodeSelected(inBuffer);
        },
        selectInBuffer3: function(nodes) {
            selectionQuery = null;
            postSelected(nodes, []);
        },
        deselectInBuffer: function(response) {
            var features = response.features;
            var nodes = getNodes(features, false);
            var links = response.links;
            selectionQuery = null;
            //      window.parent.mapViewHideLoading(vizid);
            postDeselected(nodes, links);
        },
        deselectInBuffer2: function(inBuffer) {
            postNodeDeselected(inBuffer);
        },
        deselectInBuffer3: function(nodes) {
            selectionQuery = null;
            postDeselected(nodes, []);
        },
        getSelectionMode: function() {
            return selectionMode;
        },
        getSelectionGeometry: function() {
            return selectionGeometry;
        },
        drawRubberband: function(evt) {
            if (rubberbandStartPoint == null) {
                return;
            }
            if (rubberbandGraphic !== null) {
                map.graphics.remove(rubberbandGraphic);
            }

            rubberbandEndPoint = new Point(evt.mapPoint.x,evt.mapPoint.y,evt.mapPoint.spatialReference);

            if (rubberbandVertices == null) {
                rubberbandVertices = [[[rubberbandEndPoint.x, rubberbandEndPoint.y]]];
            } else {
                var v = rubberbandVertices[0];
                v[v.length] = [rubberbandEndPoint.x, rubberbandEndPoint.y];
            }

            var xmin = rubberbandStartPoint.x < rubberbandEndPoint.x ? rubberbandStartPoint.x : rubberbandEndPoint.x;
            var ymin = rubberbandStartPoint.y < rubberbandEndPoint.y ? rubberbandStartPoint.y : rubberbandEndPoint.y;
            var xmax = rubberbandStartPoint.x > rubberbandEndPoint.x ? rubberbandStartPoint.x : rubberbandEndPoint.x;
            var ymax = rubberbandStartPoint.y > rubberbandEndPoint.y ? rubberbandStartPoint.y : rubberbandEndPoint.y;


            if (selectionMode == "circle") {
                var ydelt = ymax - ymin;
                var xdelt = xmax - xmin;
                var centerX = xmin + xdelt/2;
                var centerY = ymin + ydelt/2;
                var centerPoint = new Point(centerX, centerY, evt.mapPoint.spatialReference);

                var radius = ydelt/2 > xdelt/2 ? ydelt/2 : xdelt/2;
                var symbol = new SimpleFillSymbol().setColor(null).outline.setColor("black");
                rubberband = new Circle({
                    center: centerPoint,
                    radius: radius
                });
                rubberbandGraphic = new Graphic(rubberband, symbol);
                map.graphics.add(rubberbandGraphic);
            }
            var selectionModeInt = window.parent.getSelectionMode(vizid);
            if (selectionModeInt == 0) {
                setSelectionModePan();
            } else if (selectionModeInt == 1) {
                setSelectionModeRectangle();
            } else if (selectionModeInt == 2) {
                setSelectionModeCircle();
            } else if (selectionModeInt == 3) {
                setSelectionModePolygon();
            }

            if (selectionMode == "rectangle") {
                rubberband = new Extent(xmin,ymin,xmax,ymax,map.spatialReference);
                rubberbandGraphic = new Graphic(rubberband,new SimpleFillSymbol(
                //          SimpleFillSymbol.STYLE_FORWARD_DIAGONAL,
                //          new SimpleLineSymbol(SimpleLineSymbol.STYLE_SOLID, Color.fromHex(selectedColorString), 1),
                //          Color.fromHex(selectedColorString)
                                     ));
                map.graphics.add(rubberbandGraphic);
            }

            if (selectionMode == "polygon") {
                if (rubberbandGraphicPolygon !== null) {
                    map.graphics.remove(rubberbandGraphicPolygon);
                }

                rubberband = new Polygon({
                    rings: rubberbandVertices,
                    spatialReference: map.spatialReference
                  });

                 rubberbandGraphicPolygon = new Graphic(rubberband, new SimpleFillSymbol());

                map.graphics.add(rubberbandGraphicPolygon);
            }
        },
        clearRubberband: function() {
            if (rubberbandStartPoint !== null) {
                rubberbandStartPoint = null;
                rubberbandEndPoint = null;
                if (rubberbandGraphic !== null) {
                    map.graphics.remove(rubberbandGraphic);
                }
            }

            if (rubberbandVertices !== null) {
                rubberbandVertices = null;
                if (rubberbandGraphicPolygon !== null) {
                   map.graphics.remove(rubberbandGraphicPolygon);
                }
            }
        },
        setRubberbandStartPoint: function(evt) {
            rubberbandStartPoint = new Point(evt.mapPoint.x,evt.mapPoint.y,evt.mapPoint.spatialReference);
        },
        isRubberbandStarted: function() {
            return rubberbandStartPoint !== null;
        },
        selectByRubberband: function() {
            selectionGeometry = rubberband;
            if (selectionGeometry != null) {
                if (selectionMode == "polygon") {
                    selectionGraphic = rubberbandGraphicPolygon;
                } else {
                    selectionGraphic = rubberbandGraphic;
                }
                rubberbandStartPoint = null;
                rubberbandEndPoint = null;
                rubberband = null;
                select();
            }
        },
        deselectByRubberband: function() {
            selectionGeometry = rubberband;
            if (selectionGeometry != null) {
                if (selectionMode == "polygon") {
                    selectionGraphic = rubberbandGraphicPolygon;
                } else {
                    selectionGraphic = rubberbandGraphic;
                }
                rubberbandStartPoint = null;
                rubberbandEndPoint = null;
                rubberband = null;
                deselect();
            }
        },
        isProcessingSelection: function() {
            return processingSelection;
        },
        startProcessingSelection: function() {
            processingSelection = true;
        },
        toggleClickBuffer: function(nodes, links) {
            var postdata = {
                "nodes": nodes,
                "links": links
            };
            dojo.xhrPost({
                url: "rest/map/toggleSelected/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(postdata),
                handleAs: "json",
                load: function(response) {
                    if (response.numNodeChanged > 0) {
                        if (nodes.length > frontendToggleThreshold) {
                            dontUpdateLegend();
                            __getUpdate();
                        } else {
                            if (response.nodeChangeType == 'reload') {
                                dontUpdateLegend();
                                __getUpdate();
                            } else {
                                if (response.nodeChangeType == 'selected') {
                                    toggle(nodes, true);
                                } else if (response.nodeChangeType == 'unselected') {
                                    toggle(nodes, false);
                                }
                                selectLinks(response.linkResponse);
                            }
                        }
                    } else {
                        selectLinks(response.linkResponse);
                    }
                }
            });
        },
        setFrontendToggleThreshold: function(value) {
            frontendToggleThreshold = value;
        },
        setLoading: function(value) {
            loading = value;
        },
        selectLinks: function(links) {
            dojo.xhrPost({
                url: "rest/map/selectLinks/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(links),
                handleAs: "json",
                load: function(data) {
                    selectLinks(data);
                }
            });
        },
        deselectLinks: function(links) {
            dojo.xhrPost({
                url: "rest/map/deselectLinks/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
                postData: dojo.toJson(links),
                handleAs: "json",
                load: function(data) {
                    selectLinks(data);
                }
            });
        },
        setSelectionModePan: function() {
            selectionMode = "pan";
        },
        setSelectionModeCircle: function() {
            selectionMode = "circle";
        },
        setSelectionModeRectangle: function() {
            selectionMode = "rectangle";
        },
        setSelectionModePolygon: function() {
            selectionMode = "polygon";
        }
    });
});
