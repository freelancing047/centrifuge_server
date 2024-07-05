define([
  "esri/geometry/Point",
  "esri/geometry/webMercatorUtils",
  "esri/geometry/Extent",
  "extras34/geometryService",
  "esri/SpatialReference",
  "dojo/_base/declare"
], function(
  Point,
  webMercatorUtils,
  Extent,
  GeometryService,
  SpatialReference,
  declare) {
    var map;
    var dvid;
    var vizid;
    var initialExtent;
    var currentExtent;
    var currentZoom;
    var refreshAndUpdateMap;
    var getNextSequenceNumber;
    var mapDivContainer;
    var minZoom = 0;
    var maxZoom = 23;

    function useInitialExtent(extent) {
        return createExtent(extent.xmin, extent.ymin, extent.xmax, extent.ymax);
    }

    function createExtent(xmin, ymin, xmax, ymax) {
        var spatialReference = new SpatialReference({
            "wkid": 4326
        });
        var startExtent = new Extent(xmin,ymin,xmax,ymax,spatialReference);
        return startExtent;
    }

    function useExistingExtent(extent) {
        var spatialReference = new SpatialReference();
        spatialReference.wkid = extent.spatialReference.wkid;
        var xmin = extent.xmin;
        var xmax = extent.xmax;
        var ymin = extent.ymin;
        var ymax = extent.ymax;
        var extentIn = new Extent(xmin,ymin,xmax,ymax,spatialReference);
        var startExtent = __getRightExtent(extentIn, 102100);
        return startExtent;
    }

    function centerMap() {
        window.setTimeout(function() {
            var centerPoint;
            var zoom = map.getZoom();
            if (zoom != 0) {
                centerPoint = initialExtent.getCenter();
            } else {
                centerPoint = new Point(0,0);
            }
            map.centerAt(centerPoint);
        }, 500);
    }

    function __saveExtentAndZoom(extent, zoom, callback) {
        if (extent == null)
            return;
        if (extent.spatialReference.wkid == 4326) {
            __saveExtentAndZoom2(extent, zoom, callback);
        } else {
            var extentParam = {};
            extentParam.xmin = extent.xmin;
            extentParam.ymin = extent.ymin;
            extentParam.xmax = extent.xmax;
            extentParam.ymax = extent.ymax;
            extentParam.spatialReference = extent.spatialReference;
            __useGeometryService(extentParam, null, function(callbackParams, xmin, ymin, xmax, ymax) {
                var newExtent = createExtent(xmin, ymin, xmax, ymax)
                __saveExtentAndZoom2(newExtent, zoom, callback);
            });
        }
    }

    function __useGeometryService(extentParam, callbackParams, callback) {
        var extentOut = __getRightExtent(extentParam, 4326);
        callback(callbackParams, extentOut.xmin, extentOut.ymin, extentOut.xmax, extentOut.ymax);
    }

    function __saveExtentAndZoom2(extent, zoom, callback) {
        var data = {
            xmin: extent.xmin,
            ymin: extent.ymin,
            xmax: extent.xmax,
            ymax: extent.ymax,
            wkid: extent.spatialReference.wkid,
            zoom: zoom
        };
        dojo.xhrPost({
            url: "rest/map/setExtent/" + dvid + "/" + vizid + "/" + getNextSequenceNumber(),
            postData: data,
            handleAs: "text",
            load: function() {
                window.parent.refreshMetrics(vizid);
                if (callback != null)
                    callback();
            }
        });
    }
    var worldValuesAvailable = false;
    var xMin2D;
    var yMin2D;
    var xMax2D;
    var yMax2D;
    var xDelta2D;
    var yDelta2D;

    function calculateWorldValues() {
        var geometryService = new GeometryService();
        var minExtremeValues = geometryService.projectBack(-180.0000, -76.6798);
        var maxExtremeValues = geometryService.projectBack(180.0000, 76.6798);
        xMin2D = minExtremeValues.x;
        yMin2D = minExtremeValues.y;
        xMax2D = maxExtremeValues.x;
        yMax2D = maxExtremeValues.y;
        xDelta2D = xMax2D - xMin2D;
        yDelta2D = yMax2D - yMin2D;
        worldValuesAvailable = true;
    }

    function __getWholeWorldX(targetWkid) {
        if (targetWkid == 4326) {
            return 360;
        } else {
            if (!worldValuesAvailable)
                calculateWorldValues();
            return xDelta2D;
        }
    }

    function __getXMin2D(targetWkid) {
        if (targetWkid == 4326) {
            return -180;
        } else {
            if (!worldValuesAvailable)
                calculateWorldValues();
            return xMin2D;
        }
    }

    function __getXMax2D(targetWkid) {
        if (targetWkid == 4326) {
            return 180;
        } else {
            if (!worldValuesAvailable)
                calculateWorldValues();
            return xMax2D;
        }
    }

    function getYMin2D(targetWkid) {
        if (targetWkid == 4326) {
            return -90;
        } else {
            if (!worldValuesAvailable)
                calculateWorldValues();
            return yMin2D;
        }
    }

    function getYMax2D(targetWkid) {
        if (targetWkid == 4326) {
            return 90;
        } else {
            if (!worldValuesAvailable)
                calculateWorldValues();
            return yMax2D;
        }
    }

    function __calculateZoom(x, y, targetWkid) {
        var zoomx = 0;
        var stepx = __getWholeWorldX(targetWkid);
        while (stepx * mapDivContainer.offsetWidth / 256 > x) {
            zoomx++;
            stepx = stepx / 2;
        }
        var zoomy = 0;
        var stepy = __getWholeWorldX(targetWkid);
        while (stepy * mapDivContainer.offsetHeight / 256 > y) {
            zoomy++;
            stepy = stepy / 2;
        }
        if (zoomy < zoomx)
            return zoomy;
        else
            return zoomx;
    }

    function __calculateExtent(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid) {
        var bufferx = 0;
        var wholeWorldX = __getWholeWorldX(targetWkid);
        if (wholeWorldX >= x1) {
            var totalX = __getWholeWorldX(targetWkid) / Math.pow(2, zoom) * mapDivContainer.offsetWidth / 256;
            bufferx = (totalX - x1) / 2;
        }
        var totalY = wholeWorldX / Math.pow(2, zoom) * mapDivContainer.offsetHeight / 256;
        var buffery = (totalY - y1) / 2;
        var newymin = ymin - buffery;
        var newymax = ymax + buffery;
        if (newymin < getYMin2D(targetWkid)) {
            var moveToMax = getYMin2D(targetWkid) - newymin;
            newymin = getYMin2D(targetWkid);
            newymax += moveToMax;
            if (newymax > getYMax2D(targetWkid)) {
                newymax = getYMax2D(targetWkid);
            }
        }
        if (newymax > getYMax2D(targetWkid)) {
            var moveToMin = newymax - getYMax2D(targetWkid);
            newymin -= moveToMin;
            newymax = getYMax2D(targetWkid);
            if (newymin < getYMin2D(targetWkid)) {
                newymin = getYMin2D(targetWkid);
            }
        }
        var spatialReference = new SpatialReference({
            "wkid": targetWkid
        });
        var extent = new Extent(xmin - bufferx,newymin,xmax + bufferx,newymax,spatialReference);
        return extent;
    }

    function __calculateWholeWorldExtent(zoom, xmin, xmax, ymin, ymax, targetWkid) {
        var bufferx = (__getWholeWorldX(targetWkid) - 0.00001) / 2;
        var xcenter = (xmin + xmax) / 2;
        var totalY = __getWholeWorldX(targetWkid) / Math.pow(2, zoom) * mapDivContainer.offsetHeight / 256;
        var buffery = (totalY - (ymax - ymin)) / 2;
        var newymin = ymin - buffery;
        var newymax = ymax + buffery;
        if (newymin < getYMin2D(targetWkid)) {
            var moveToMax = getYMin2D(targetWkid) - newymin;
            newymin = getYMin2D(targetWkid);
            newymax += moveToMax;
            if (newymax > getYMax2D(targetWkid)) {
                newymax = getYMax2D(targetWkid);
            }
        } else if (newymax > getYMax2D(targetWkid)) {
            var moveToMin = newymax - getYMax2D(targetWkid);
            newymin -= moveToMin;
            newymax = getYMax2D(targetWkid);
            if (newymin < getYMin2D(targetWkid)) {
                newymin = getYMin2D(targetWkid);
            }
        }
        var spatialReference = new SpatialReference({
            "wkid": targetWkid
        });
        var extent = new Extent(xcenter - bufferx,newymin,xcenter + bufferx,newymax,spatialReference);
        return extent;
    }
    var newPaddingLeft = 0;
    var newPaddingRight = 0;

    function __calculateZoomAndExtent(extentIn, targetWkid) {
        var extentOut = __getRightExtent(extentIn, targetWkid);
        var xmin = extentOut.xmin;
        var ymin = extentOut.ymin;
        var xmax = extentOut.xmax;
        var ymax = extentOut.ymax;
        var x1 = xmax - xmin;
        var y1 = ymax - ymin;
        var zoom = __calculateSnugZoom(x1, y1, targetWkid);
        __setPaddingsUsingZoom(zoom);
        var extent = __getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid);
        __setMapDivContainerToNewPaddings();
        if (targetWkid == 102100) {
            if (extent.xmax > __getXMax2D(102100)) {
                extent.xmax -= __getWholeWorldX(102100);
                extent.xmin -= __getWholeWorldX(102100);
            }
            if (extent.xmin < __getXMin2D(102100) - __getWholeWorldX(102100)) {
                extent.xmax += __getWholeWorldX(102100);
                extent.xmin += __getWholeWorldX(102100);
            }
        }
        var zoomAndExtent = {
            zoom: zoom,
            extent: extent
        };
        return zoomAndExtent;
    }

    function __setPaddingsUsingZoom(zoom) {
        var maxWidth = 256 * Math.pow(2, zoom);
        if (mapDivContainer.offsetWidth > maxWidth) {
            var diff = mapDivContainer.offsetWidth - maxWidth;
            newPaddingLeft = Math.floor(diff / 2);
            newPaddingRight = diff - newPaddingLeft;
        } else {
            newPaddingLeft = 0;
            newPaddingRight = 0;
        }
    }

    function __getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid) {
        if (__isPaddingsNotZero()) {
            return __calculateWholeWorldExtent(zoom, xmin, xmax, ymin, ymax, targetWkid);
        } else {
            return __calculateExtent(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid);
        }
    }

    function __isPaddingsNotZero() {
        return newPaddingLeft != 0 || newPaddingRight != 0;
    }

    function __setMapDivContainerToNewPaddings() {
        mapDivContainer.style.paddingLeft = newPaddingLeft + "px";
        mapDivContainer.style.paddingRight = newPaddingRight + "px";
    }

    function __getRightExtent(extentIn, targetWkid) {
        var extentOut;
        if (extentIn.spatialReference.wkid == targetWkid) {
            extentOut = {
                xmin: extentIn.xmin,
                ymin: extentIn.ymin,
                xmax: extentIn.xmax,
                ymax: extentIn.ymax
            };
        } else {
            var geometryService = new GeometryService();
            var minValues;
            var maxValues;
            if (extentIn.spatialReference.wkid == 4326) {
                if (Math.abs(extentIn.xmax - extentIn.xmin - __getWholeWorldX(4326)) < 0.1) {
                    var xcenter = (extentIn.xmax + extentIn.xmin) / 2;
                    while (xcenter < 0 - __getWholeWorldX(4326))
                        xcenter += __getWholeWorldX(4326);
                    while (xcenter > 0)
                        xcenter -= __getWholeWorldX(4326);
                    if (extentIn.ymin < -76.6798)
                        extentIn.ymin = -76.6798;
                    if (extentIn.ymax > 76.6798)
                        extentIn.ymax = 76.6798;
                    minValues = geometryService.projectBack(xcenter, extentIn.ymin);
                    maxValues = geometryService.projectBack(xcenter, extentIn.ymax);
                    minValues.x -= (__getWholeWorldX(102100) / 2);
                    maxValues.x += (__getWholeWorldX(102100) / 2);
                } else {
                    if (extentIn.ymin < -76.6798)
                        extentIn.ymin = -76.6798;
                    if (extentIn.ymax > 76.6798)
                        extentIn.ymax = 76.6798;
                    minValues = geometryService.projectBack(extentIn.xmin, extentIn.ymin);
                    maxValues = geometryService.projectBack(extentIn.xmax, extentIn.ymax);
                }
                extentOut = {
                    xmin: minValues.x,
                    ymin: minValues.y,
                    xmax: maxValues.x,
                    ymax: maxValues.y
                };
            } else {
                if (Math.abs(extentIn.xmax - extentIn.xmin - __getWholeWorldX(102100)) < 0.1) {
                    var xcenter = (extentIn.xmax + extentIn.xmin) / 2
                    while (xcenter < 0 - __getWholeWorldX(102100))
                        xcenter += __getWholeWorldX(102100);
                    while (xcenter > 0)
                        xcenter -= __getWholeWorldX(102100);
                    minValues = geometryService.project(xcenter, extentIn.ymin);
                    maxValues = geometryService.project(xcenter, extentIn.ymax);
                    minValues.x -= (__getWholeWorldX(4326) / 2);
                    maxValues.x += (__getWholeWorldX(4326) / 2);
                } else {
                    minValues = geometryService.project(extentIn.xmin, extentIn.ymin);
                    maxValues = geometryService.project(extentIn.xmax, extentIn.ymax);
                }
                var x0 = minValues.x;
                var x1 = maxValues.x;
                if (isNaN(x0)) {
                    x0 = -180;
                }
                if (isNaN(x1)) {
                    x1 = 180;
                }
                var xmin;
                var xmax = x1;
                if (x0 > x1) {
                    xmin = x0 - 360;
                } else {
                    xmin = x0;
                }
                if (Math.abs(xmax - xmin - 360) < 0.00001 && (xmin + xmax) / 2 > 0) {
                    xmin -= 360;
                    xmax -= 360;
                }
                var ymin = minValues.y;
                var ymax = maxValues.y;
                if (isNaN(ymin) || ymin < -76.6798) {
                    ymin = -90;
                }
                if (isNaN(ymax) || ymax > 76.6798) {
                    ymax = 90;
                }
                extentOut = {
                    xmin: xmin,
                    ymin: ymin,
                    xmax: xmax,
                    ymax: ymax
                };
            }
        }
        return extentOut;
    }

    function __calculateSnugZoom(x1, y1, targetWkid) {
        var zoom;
        var deltaxOverDeltay = x1 / y1;
        var offsetWidthOverOffsetHeight = mapDivContainer.offsetWidth / mapDivContainer.offsetHeight;
        if (deltaxOverDeltay < offsetWidthOverOffsetHeight) {
            // span deltay over mapDivContainer.offsetHeight
            var x2 = Math.abs(mapDivContainer.offsetWidth / mapDivContainer.offsetHeight * y1);
            zoom = __calculateZoom(x2, y1, targetWkid);
            if (zoom < minZoom) {
                zoom = minZoom - 1;
            } 
            var maxHeight = 256 * Math.pow(2, zoom);
            while (maxHeight * y1 / __getWholeWorldX(targetWkid) > mapDivContainer.offsetHeight && zoom >= minZoom) {
                zoom--;
                maxHeight = 256 * Math.pow(2, zoom);
            }
            while (mapDivContainer.offsetHeight > maxHeight * 2) {
                zoom++;
                maxHeight = 256 * Math.pow(2, zoom);
            }
        } else {
            // span deltax over mapDivContainer.offsetWidth
            var y2 = Math.abs(mapDivContainer.offsetHeight / mapDivContainer.offsetWidth * x1);
            zoom = __calculateZoom(x1, y2, targetWkid);
            if (zoom < minZoom) {
                zoom = minZoom - 1;
            } 
            var maxWidth = 256 * Math.pow(2, zoom);
            while (maxWidth * x1 / __getWholeWorldX(targetWkid) > mapDivContainer.offsetWidth && zoom >= minZoom) {
                zoom--;
                maxWidth = 256 * Math.pow(2, zoom);
            }
            while (mapDivContainer.offsetWidth > maxWidth * 2) {
                zoom++;
                maxWidth = 256 * Math.pow(2, zoom);
            }
        }
        return zoom;
    }

    function zoomByFactor(oldextent, factor) {
        var xmin = oldextent.xmin;
        var xmax = oldextent.xmax;
        var xcenter = (xmin + xmax) / 2;
        var xdelta = xmax - xmin;
        var xbuffer = xdelta / 2 * factor;
        var newxmin = xcenter - xbuffer;
        var newxmax = xcenter + xbuffer;
        var ymin = oldextent.ymin;
        var ymax = oldextent.ymax;
        var ycenter = (ymin + ymax) / 2;
        var ydelta = ymax - ymin;
        var ybuffer = ydelta / 2 * factor;
        var newymin = ycenter - ybuffer;
        var newymax = ycenter + ybuffer;
        if (newymin < getYMin2D(oldextent.spatialReference.wkid)) {
            var moveToMax = getYMin2D(oldextent.spatialReference.wkid) - newymin;
            newymin = getYMin2D(oldextent.spatialReference.wkid);
            newymax += moveToMax;
            if (newymax > getYMax2D(oldextent.spatialReference.wkid)) {
                newymax = getYMax2D(oldextent.spatialReference.wkid);
            }
        }
        if (newymax > getYMax2D(oldextent.spatialReference.wkid)) {
            var moveToMin = newymax - getYMax2D(oldextent.spatialReference.wkid);
            newymin -= moveToMin;
            newymax = getYMax2D(oldextent.spatialReference.wkid);
            if (newymin < getYMin2D(oldextent.spatialReference.wkid)) {
                newymin = getYMin2D(oldextent.spatialReference.wkid);
            }
        }
        var newextent = new Extent(newxmin,newymin,newxmax,newymax,oldextent.spatialReference);
        var zoomAndExtent = __calculateZoomAndExtent(newextent, oldextent.spatialReference.wkid);
        refreshAndUpdateMap(zoomAndExtent.extent, zoomAndExtent.zoom);
        currentExtent = zoomAndExtent.extent;
        currentZoom = zoomAndExtent.zoom;
        __saveExtentAndZoom(currentExtent, currentZoom);
    }
    return declare(null, {
        constructor: function(did, vid, refreshAndUpdateMapIn, getNextSequenceNumberIn, mapDivContainerIn) {
            dvid = did;
            vizid = vid;
            refreshAndUpdateMap = refreshAndUpdateMapIn;
            getNextSequenceNumber = getNextSequenceNumberIn;
            mapDivContainer = mapDivContainerIn;
        },
        setMap: function(mapIn) {
            map = mapIn;
        },
        setMinZoom: function(minZoomIn) {
            minZoom = minZoomIn;
        }, 
        setMaxZoom: function(maxZoomIn) {
            maxZoom = maxZoomIn;
        },
        getToCurrentExtent: function(save) {
            if (currentExtent) {
                var mapZoom = map.getZoom();
                if (mapZoom != null) {
                    if (currentZoom != null) {
                        if (mapZoom == currentZoom) {
                            var x = (currentExtent.xmin + currentExtent.xmax) / 2;
                            var y = (currentExtent.ymin + currentExtent.ymax) / 2;
                            var mapPoint = new Point(x,y,currentExtent.spatialReference);
                            map.centerAt(mapPoint);
                        } else {
                            map.setExtent(currentExtent);
                        }
                    } else {
                        map.setExtent(currentExtent);
                    }
                } else {
                    map.setExtent(currentExtent);
                }
                if (save === undefined || save) {
                    __saveExtentAndZoom(currentExtent, currentZoom);
                }
            }
        },
        ignoreDragThreshold: function() {
            if (currentExtent) {
                map.setExtent(currentExtent);
            }
        },
        getCurrentExtent: function() {
            return currentExtent;
        },
        getCurrentCenterPoint: function() {
            if (currentExtent == null) {
                return null;
            }
            var xmin = currentExtent.xmin;
            var xmax = currentExtent.xmax;
            var ymin = currentExtent.ymin;
            var ymax = currentExtent.ymax;
            var xcenter = (xmin + xmax) / 2;
            var ycenter = (ymin + ymax) / 2;
            var centerPoint = {
                x: xcenter,
                y: ycenter,
                wkid: currentExtent.spatialReference.wkid
            };
            return centerPoint;
        },
        getCurrentZoom: function() {
            return currentZoom;
        },
        revertToInitialExtent: function(targetWkid) {
            var zoomAndExtent = __calculateZoomAndExtent(initialExtent, targetWkid);
            var zoom = zoomAndExtent.zoom;
            var extent = zoomAndExtent.extent;
            if (map.extent.xmin != extent.xmin || map.extent.xmax != extent.xmax || map.extent.ymin != extent.ymin || map.extent.ymax != extent.ymax) {
                refreshAndUpdateMap(extent, zoom);
                currentExtent = extent;
                currentZoom = zoom;
                __saveExtentAndZoom(currentExtent, currentZoom);
                return true;
            }
            return false;
        },
        setMapExtentInfo: function(isNewExtent, jsonFSInitialExtent, jsonFSExtent) {
            initialExtent = useInitialExtent(jsonFSInitialExtent);
            if (isNewExtent) {
                currentExtent = null;
            }
        },
        saveExtentAndZoom: function(callback) {
            currentExtent = map.extent;
            currentZoom = map.getZoom();
            if (currentZoom == -1) {
                window.setTimeout(function() {
                    currentExtent = map.extent;
                    currentZoom = map.getZoom();
                    __saveExtentAndZoom(currentExtent, currentZoom, callback);
                }, 150);
            } else {
                __saveExtentAndZoom(currentExtent, currentZoom, callback);
            }
        },
        saveExtentAndZoom2: function(zoom, callback) {
            currentExtent = map.extent;
            currentZoom = zoom;
            __saveExtentAndZoom(currentExtent, currentZoom, callback);
        },
        zoomIn: function() {
            if (map.getZoom() == -1) {
                zoomByFactor(map.extent, 0.5);
            } else {
                var newZoom = map.getZoom() + 1;
                if (newZoom <= maxZoom) {
                    map.setZoom(newZoom);
                    currentExtent = map.extent;
                    currentZoom = newZoom;
                    __saveExtentAndZoom(currentExtent, currentZoom);
                }
            }
        },
        zoomOut: function() {
            if (map.getZoom() == -1) {
                zoomByFactor(map.extent, 2);
            } else {
                var newZoom = map.getZoom() - 1;
                if (newZoom >= minZoom) {
                    map.setZoom(newZoom);
                    currentExtent = map.extent;
                    currentZoom = newZoom;
                    __saveExtentAndZoom(currentExtent, currentZoom);
                }
            }
        },
        getInitialExtent: function() {
            return initialExtent;
        },
        useGeometryService: function(extentParam, callbackParams, callback) {
            __useGeometryService(extentParam, callbackParams, callback);
        },
        useGeometryService2: function(extentParam, callbackParams, callback) {
            var extentOut = __getRightExtent(extentParam, 4326);
            var useWholeWorldX = false;
            if (extentParam.spatialReference.wkid == 102100) {
                var xmin = extentParam.xmin;
                var xmax = extentParam.xmax;
                var xdiff = Math.abs(xmax - xmin);
                var wholeWorldX = __getWholeWorldX(102100);
                if (xdiff > wholeWorldX * 0.95) {
                    useWholeWorldX = true;
                }
            }
            if (useWholeWorldX) {
                callback(callbackParams, -180, extentOut.ymin, 180, extentOut.ymax);
            } else {
                callback(callbackParams, extentOut.xmin, extentOut.ymin, extentOut.xmax, extentOut.ymax);
            }
        },
        calculateWholeWorldExtent: function(zoom, xmin, xmax, ymin, ymax, targetWkid) {
            return __calculateWholeWorldExtent(zoom, xmin, xmax, ymin, ymax, targetWkid);
        },
        calculateExtent: function(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid) {
            return __calculateExtent(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid);
        },
        calculateZoom: function(x, y, targetWkid) {
            return __calculateZoom(x, y, targetWkid);
        },
        calculateZoomAndExtent: function(extentIn, targetWkid) {
            return __calculateZoomAndExtent(extentIn, targetWkid);
        },
        getNewPaddingLeft: function() {
            return newPaddingLeft;
        },
        getNewPaddingRight: function() {
            return newPaddingRight;
        },
        setNewPaddingValues: function(newPaddingLeftIn, newPaddingRightIn) {
            newPaddingLeft = newPaddingLeftIn;
            newPaddingRight = newPaddingRightIn;
        },
        getRightExtent: function(extentIn, targetWkid) {
            return __getRightExtent(extentIn, targetWkid);
        },
        calculateSnugZoom: function(x1, y1, targetWkid) {
            return __calculateSnugZoom(x1, y1, targetWkid);
        },
        setPaddingsUsingZoom: function(zoom) {
            __setPaddingsUsingZoom(zoom);
        },
        getExtentByZoomAndPadding: function(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid) {
            return __getExtentByZoomAndPadding(zoom, x1, y1, xmin, xmax, ymin, ymax, targetWkid);
        },
        isPaddingsNotZero: function() {
            return __isPaddingsNotZero();
        },
        setMapDivContainerToNewPaddings: function() {
            __setMapDivContainerToNewPaddings();
        },
        clearMapContainerDivPadding: function() {
            if (mapDivContainer.style.paddingLeft != "0px" || mapDivContainer.style.paddingRight != "0px") {
                mapDivContainer.style.paddingLeft = "0px";
                mapDivContainer.style.paddingRight = "0px";
            }
        },
        adjustZoomToNewSize: function(currentZoom) {
            var zoom = currentZoom;
            var maxWidth = 256 * Math.pow(2, zoom);
            while (mapDivContainer.offsetWidth > maxWidth * 2) {
                zoom++;
                maxWidth = 256 * Math.pow(2, zoom);
            }
            if (zoom < minZoom) {
                zoom = minZoom - 1;
            } 
            return zoom;
        },
        calculateExtentByZoomAndCenterPoint: function(zoom, centerPoint) {
            var targetWkid = centerPoint.wkid;
            var totalX = __getWholeWorldX(targetWkid) / Math.pow(2, zoom) * mapDivContainer.offsetWidth / 256;
            var bufferx = totalX / 2;
            var xMin = centerPoint.x - bufferx;
            var xMax = centerPoint.x + bufferx;
            var totalY = __getWholeWorldX(targetWkid) / Math.pow(2, zoom) * mapDivContainer.offsetHeight / 256;
            var buffery = (totalY) / 2;
            var newymin = centerPoint.y - buffery;
            var newymax = centerPoint.y + buffery;
            if (newymin < getYMin2D(targetWkid)) {
                var moveToMax = getYMin2D(targetWkid) - newymin;
                newymin = getYMin2D(targetWkid);
                newymax += moveToMax;
                if (newymax > getYMax2D(targetWkid)) {
                    newymax = getYMax2D(targetWkid);
                }
            }
            if (newymax > getYMax2D(targetWkid)) {
                var moveToMin = newymax - getYMax2D(targetWkid);
                newymin -= moveToMin;
                newymax = getYMax2D(targetWkid);
                if (newymin < getYMin2D(targetWkid)) {
                    newymin = getYMin2D(targetWkid);
                }
            }
            var spatialReference = new SpatialReference({
                "wkid": targetWkid
            });
            var extent = new Extent(xMin,newymin,xMax,newymax,spatialReference);
            return extent;
        },
        getXMin2D: function(wkid) {
            return __getXMin2D(wkid);
        },
        getXMax2D: function(wkid) {
            return __getXMax2D(wkid);
        },
        getWholeWorldX: function(wkid) {
            return __getWholeWorldX(wkid);
        }
    });
});
