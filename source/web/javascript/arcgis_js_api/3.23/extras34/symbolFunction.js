define(["dojo/_base/declare",
  'dojo/_base/xhr',
  "dojo/_base/lang",
  "esri/symbols/SimpleMarkerSymbol",
  "esri/symbols/PictureMarkerSymbol",
  "esri/symbols/SimpleLineSymbol",
  "esri/symbols/CartographicLineSymbol",
  "esri/geometry/Polyline",
  "esri/geometry/Multipoint",
  "esri/graphic",
  "esri/geometry/Point",
  "esri/geometry/ScreenPoint",
  "esri/Color",
  "esri/SpatialReference"
], function(declare,
  xhr,
  lang,
  SimpleMarkerSymbol,
  PictureMarkerSymbol,
  SimpleLineSymbol,
  CartographicLineSymbol,
  Polyline,
  Multipoint,
  Graphic,
  Point,
  ScreenPoint,
  Color,
  SpatialReference) {
    var iconProviderUrl;
    var defaultShapeString;
    var typeSymbols;
    var hasLineDefinitions;
    var associationSymbols;
    var idAssociationMap;
    var placeSymbolsMapLoading = {};
    var placeSymbolImageData = {};
    var mapShapeToIconPath = {};
    var slsStyle = SimpleLineSymbol.STYLE_SOLID;
    var slsSize = 2;
    var lineSymbols;
    var selectedLineSymbol;
    var selectedColorString = "#ff8300";
    var selectedLineWidth = 6;
    var getNextSequenceNumber;

    function __createSymbol(path, color, angle, size, isUseSummary) {
        var markerSymbol = new SimpleMarkerSymbol();
        markerSymbol.setPath(path);
        markerSymbol.setAngle(angle);
        markerSymbol.setSize(size);
        if (typeof isUseSummary != 'undefined' && isUseSummary !== null && isUseSummary) {
            var smsColor = Color.fromHex("#000000");
            smsColor.a = 0;
            markerSymbol.setColor(smsColor);
            var slsColor = new Color(color);
            var placeOutline = new SimpleLineSymbol(slsStyle,slsColor,slsSize);
            markerSymbol.setOutline(placeOutline);
        } else {
            markerSymbol.setColor(new Color(color));
            markerSymbol.setOutline(null);
        }
        return markerSymbol;
    }

    function createRegularPolygon(x, y, r, vertexCount, startAngle) {
        var iconPath = "M";
        var addAngle = 2 * Math.PI / vertexCount;
        var angle = startAngle;
        for (var i = 0; i < vertexCount; i++) {
            if (i > 0) {
                iconPath += "L";
            }
            iconPath += Math.round(r * Math.cos(angle)) + x;
            iconPath += ",";
            iconPath += Math.round(r * Math.sin(angle)) + y;
            angle += addAngle;
        }
        iconPath += "Z";
        return iconPath;
    }

    function createStarPolygon(x, y, r, innerR, vertexCount, startAngle) {
        var iconPath = "M";
        var addAngle = 2 * Math.PI / vertexCount;
        var angle = startAngle;
        var innerAngle = startAngle + Math.PI / vertexCount;
        for (var i = 0; i < vertexCount; i++) {
            if (i > 0) {
                iconPath += "L";
            }
            iconPath += Math.round(r * Math.cos(angle)) + x;
            iconPath += ",";
            iconPath += Math.round(r * Math.sin(angle)) + y;
            angle += addAngle;
            iconPath += "L";
            iconPath += Math.round(innerR * Math.cos(innerAngle)) + x;
            iconPath += ",";
            iconPath += Math.round(innerR * Math.sin(innerAngle)) + y;
            innerAngle += addAngle;
        }
        iconPath += "Z";
        return iconPath;
    }

    function getAngle(changeInX, changeInY) {
        var angle = 0;
        if (changeInX == 0 && changeInY > 0) {
            angle = 0;
        } else if (changeInX == 0 && changeInY < 0) {
            angle = 180;
        } else if (changeInX > 0 && changeInY == 0) {
            angle = 90;
        } else if (changeInX < 0 && changeInY == 0) {
            angle = 270;
        } else if (changeInX == 0 && changeInY == 0) {
            angle = -1;
        } else {
            var rad = Math.atan2(Math.abs(changeInX), Math.abs(changeInY));
            var deg = rad * (180 / Math.PI)
            if (changeInX > 0 && changeInY > 0) {
                angle = deg;
            } else if (changeInX > 0 && changeInY < 0) {
                angle = 180 - deg;
            } else if (changeInX < 0 && changeInY < 0) {
                angle = deg + 180;
            } else {
                angle = 360 - deg;
            }
        }
        return angle;
    }

    function buildPolyline(polyline, sourceGeometry, destinationGeometry, changeInX, changeInY, iXMin, iXMax) {
        var stepInX = changeInX / 10;
        var stepInY = changeInY / 10;
        var steps = 9;
        var currentX = sourceGeometry.x;
        var currentY = sourceGeometry.y;
        var stepX = currentX + 9 * stepInX;
        var stepY = currentY + 9 * stepInY;
        while (steps < 10) {
            if (stepX < iXMin) {
              var partialStepInX = iXMin - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              stepX = iXMax - (iXMin - stepX);
              polyline.addPath([[currentX, currentY],[iXMin, partialStepInY]]);
              polyline.addPath([[iXMax, partialStepInY],[stepX, stepY]]);
            } else if (stepX > iXMax) {
              var partialStepInX = iXMax - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              stepX = iXMin + (stepX - iXMax);
              polyline.addPath([[currentX, currentY],[iXMax, partialStepInY]]);
              polyline.addPath([[iXMin, partialStepInY],[stepX, stepY]]);
            } else {
              polyline.addPath([[currentX, currentY], [stepX, stepY]]);
            }
            currentX = stepX;
            currentY = stepY;
            stepX = currentX + stepInX;
            stepY = currentY + stepInY;
            steps++;
        }
        if (stepX < iXMin) {
              var partialStepInX = iXMin - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              polyline.addPath([[currentX, currentY],[iXMin, partialStepInY]]);
              polyline.addPath([[iXMax, partialStepInY],[destinationGeometry.x, destinationGeometry.y]]);
        } else if (stepX > iXMax) {
              var partialStepInX = iXMax - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              polyline.addPath([[currentX, currentY],[iXMax, partialStepInY]]);
              polyline.addPath([[iXMin, partialStepInY],[destinationGeometry.x, destinationGeometry.y]]);
        } else {
          if (currentX > destinationGeometry.x && currentX - destinationGeometry.x > 180) {
              var partialStepInX = iXMax - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              polyline.addPath([[currentX, currentY], [iXMax.x, destinationGeometry.y]]);
              polyline.addPath([[iXMin, currentY], [destinationGeometry.x, destinationGeometry.y]]);
          } else if (currentX < destinationGeometry.x && destinationGeometry.x - currentX > 180) {
              var partialStepInX = iXMin - currentX;
              var ratio = partialStepInX / stepInX;
              var partialStepInY = currentY + (ratio * stepInY);
              polyline.addPath([[currentX, currentY], [iXMin.x, destinationGeometry.y]]);
              polyline.addPath([[iXMax, currentY], [destinationGeometry.x, destinationGeometry.y]]);
          } else {
              polyline.addPath([[currentX, currentY], [destinationGeometry.x, destinationGeometry.y]]);
          }
        }
    }

    function composeIconUri(typeId, useIcon, originalIconUri, typeId, isSelected, ishighlighted, isUseSummary, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha) {
        var iconUri;
        if (useIcon) {
            iconUri = originalIconUri;
        } else {
            iconUri = "id=";
        }
        var shape = typeSymbols[typeId].shape;
        if (typeof isUseSummary != 'undefined' && isUseSummary !== null && isUseSummary && shape == "None" && !useIcon) {
            iconUri += "&sh=" + defaultShapeString;
        } else {
            iconUri += "&sh=" + shape;
        }
        var color = typeSymbols[typeId].color
        iconUri += "&col=" + color;
        if (isCombined && typeof useMultitypeDecorator != 'undefined' && useMultitypeDecorator !== null && useMultitypeDecorator) {
            iconUri += "&com=true";
        }
        if (isNew && typeof useLinkupDecorator != 'undefined' && useLinkupDecorator !== null && useLinkupDecorator) {
            iconUri += "&in=true";
        }
        if (isUpdated && typeof useLinkupDecorator != 'undefined' && useLinkupDecorator !== null && useLinkupDecorator) {
            iconUri += "&iu=true";
        }
        if (isSelected) {
            iconUri += "&s=true";
        }
        if (ishighlighted) {
            iconUri += "&h=true";
        }
        if (typeof isUseSummary != 'undefined' && isUseSummary !== null && isUseSummary) {
            iconUri += "&us=true";
        }
        if (alpha) {
            iconUri += "&a=" + alpha;
        }
        return iconUri;
    }

    function calculateIconSize(iconSize, isSelected, ishighlighted, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator) {
        iconSize += 4;
        var isNew = isNew && typeof useLinkupDecorator != 'undefined' && useLinkupDecorator !== null && useLinkupDecorator;
        var isUpdated = isUpdated && typeof useLinkupDecorator != 'undefined' && useLinkupDecorator !== null && useLinkupDecorator;
        if (isSelected || isNew || isUpdated) {
            if (isSelected && isNew && isUpdated) {
                iconSize += 12;
            } else if ((isSelected && isNew) || (isSelected && isUpdated) || (isNew && isUpdated)) {
                iconSize += 10;
            } else {
                iconSize += 8;
            }
        } else {
            if (ishighlighted) {
                iconSize += 6;
            }
        }
        if (isCombined && typeof useMultitypeDecorator != 'undefined' && useMultitypeDecorator !== null && useMultitypeDecorator) {
            iconSize += 2;
        }
        return iconSize;
    }
    function buildMapArrowCoordinate(mapArrowCoordinates, map, cosTheta, sinTheta, screenPointX, screenPointY, x, y) {
        var xp = x * cosTheta - y * sinTheta;
        var yp = x * sinTheta + y * cosTheta;
        var newScreenPointX = xp + screenPointX;
        var newScreenPointY = yp + screenPointY;
        var newScreenPoint = new ScreenPoint(newScreenPointX,newScreenPointY);
        var mapPoint = map.toMap(newScreenPoint);
        mapArrowCoordinates.push(mapPoint);
    }
    function createMapArrowCoordinates(map, sourceGeometry, destinationGeometry) {
        var mapArrowCoordinates = [];

        var spatialReference = new SpatialReference({
            wkid: 4326
        });
        var sourceGeometryX = sourceGeometry.x;
        var leftToRight = sourceGeometryX  < destinationGeometry.x;
        if (leftToRight) {
          if (destinationGeometry.x - sourceGeometryX > 180) {
            sourceGeometryX += 360;
          }
        } else {
          if (sourceGeometryX - destinationGeometry.x > 180) {
            sourceGeometryX -= 360;
          }
        }
        var sourcePoint = new Point(sourceGeometryX,sourceGeometry.y,spatialReference);
        var sourceScreenPoint = map.toScreen(sourcePoint);
        var sourceScreenPointx = sourceScreenPoint.x;
        var sourceScreenPointy = sourceScreenPoint.y;
        var destinationPoint = new Point(destinationGeometry.x,destinationGeometry.y,spatialReference);
        var destinationScreenPoint = map.toScreen(destinationPoint);
        var destinationScreenPointx = destinationScreenPoint.x;
        var destinationScreenPointy = destinationScreenPoint.y;

        var screenChangeInX = destinationScreenPointx - sourceScreenPointx;
        var screenChangeInY = destinationScreenPointy - sourceScreenPointy;
        var dx2 = Math.pow(screenChangeInX, 2);
        var dy2 = Math.pow(screenChangeInY, 2);
        var distance = Math.sqrt(dx2 + dy2);

        if (destinationScreenPointx >= 0 && destinationScreenPointx <= map.width && destinationScreenPointy >= 0 && destinationScreenPointy <= map.height && distance > arrowHeight + awayFromPoint) {
            var angle = getAngle(screenChangeInX, screenChangeInY * -1);
            var radians = angle * Math.PI / 180;
            var sinTheta = Math.sin(radians);
            var cosTheta = Math.cos(radians);
            arrowCoordinates.forEach(arrowCoordinate=>{
                var x = arrowCoordinate[0];
                var y = arrowCoordinate[1];
                buildMapArrowCoordinate(mapArrowCoordinates, map, cosTheta, sinTheta, destinationScreenPointx, destinationScreenPointy, x, y);
            });
        }
        return mapArrowCoordinates;
    }

    function buildArrowPolyline(mapArrowCoordinatesList) {
        var arrowPolyline = null;
        mapArrowCoordinatesList.forEach(mapArrowCoordinates=>{
            if (arrowPolyline == null) {
                arrowPolyline = new Polyline(mapArrowCoordinates[0].spatialReference);
            }
            var prevMapPoint = null;
            mapArrowCoordinates.forEach(mapArrowCoordinate=>{
                if (prevMapPoint != null) {
                    arrowPolyline.addPath([[prevMapPoint.x, prevMapPoint.y], [mapArrowCoordinate.x, mapArrowCoordinate.y]]);
                }
                prevMapPoint = mapArrowCoordinate;
            });
        });
        return arrowPolyline;
    }

    var arrowCoordinates;
    var arrowHeight = 10;
    var awayFromPoint = 6;
    function prepareArrowPath() {
        arrowCoordinates = [];
        arrowCoordinates.push([0, awayFromPoint]);
        arrowCoordinates.push([-2, arrowHeight + awayFromPoint]);
        arrowCoordinates.push([0, 3 + awayFromPoint]);
        arrowCoordinates.push([2, arrowHeight + awayFromPoint]);
    }

    return declare(null, {
        constructor: function(getNextSequenceNumberIn) {
            getNextSequenceNumber = getNextSequenceNumberIn;
            selectedLineSymbol = new CartographicLineSymbol(CartographicLineSymbol.STYLE_SOLID,Color.fromHex(selectedColorString),selectedLineWidth,CartographicLineSymbol.CAP_ROUND,CartographicLineSymbol.JOIN_MITER,5);
        },
        initialize: function(jsonFS) {
            iconProviderUrl = jsonFS.iconProviderUrl;
            defaultShapeString = jsonFS.defaultShapeString;
            typeSymbols = {};
            var typeSymbolsArray = jsonFS.typeSymbols;
            var typeSymbolsArrayLength = typeSymbolsArray.length;
            for (var i = 0; i < typeSymbolsArrayLength; i++) {
                var typeSymbol = typeSymbolsArray[i];
                typeSymbols[typeSymbol.id] = {
                    color: typeSymbol.color,
                    shape: typeSymbol.shape,
                    id: typeSymbol.id
                };
            }
            if (jsonFS.associationSymbols == null) {
                hasLineDefinitions = false;
            } else {
                hasLineDefinitions = true;
                associationSymbols = {};
                var associationSymbolsArray = jsonFS.associationSymbols;
                var associationSymbolsArrayLength = associationSymbolsArray.length;
                for (var j = 0; j < associationSymbolsArrayLength; j++) {
                    var associationSymbol = associationSymbolsArray[j];
                    associationSymbols[associationSymbol.id] = {
                        lineStyle: associationSymbol.lineStyle,
                        width: associationSymbol.width,
                        color: associationSymbol.color,
                        showDirection: associationSymbol.showDirection,
                        id: associationSymbol.id
                    };
                }
                idAssociationMap = {};
                lineSymbols = {};
                arrowLineSymbols = {};
                var typeId = 0;
                for (let typeId of Object.keys(associationSymbols)) {
                    if (associationSymbols[typeId] == null) {
                        lineSymbols.delete(typeId);
                        arrowLineSymbols.delete(typeId);
                    } else {
                        let associationSymbol = associationSymbols[typeId];
                        var smsLineStyleString = associationSymbol.lineStyle;
                        var smsLineStyle;
                        if (smsLineStyleString == "Dash") {
                            smsLineStyle = CartographicLineSymbol.STYLE_DASH;
                        } else if (smsLineStyleString == "Dot") {
                            smsLineStyle = CartographicLineSymbol.STYLE_DOT;
                        } else {
                            smsLineStyle = CartographicLineSymbol.STYLE_SOLID;
                        }
                        var smsColor = Color.fromHex("#" + associationSymbol.color.replace("#", ""));
                        smsColor.a = 1;
                        var smsWidth = associationSymbol.width;
                        var lineSymbol = new CartographicLineSymbol(smsLineStyle,smsColor,smsWidth,CartographicLineSymbol.CAP_ROUND,CartographicLineSymbol.JOIN_MITER,5);
                        var arrowLineSymbol = new CartographicLineSymbol(CartographicLineSymbol.STYLE_SOLID,smsColor,smsWidth * 2,CartographicLineSymbol.BUTT,CartographicLineSymbol.JOIN_MITER,5);
                        lineSymbols[typeId] = lineSymbol;
                        arrowLineSymbols[typeId] = arrowLineSymbol;
                    }
                }
            }
            prepareArrowPath();
        },
        hasLineDefinitions: function() {
            return hasLineDefinitions;
        },
        getTypeSymbols: function() {
            return typeSymbols;
        },
        getShapeString: function(index) {
            if (typeSymbols.hasOwnProperty(index)) {
                return typeSymbols[index].shape;
            }
            return null;
        },
        resetPlaceSymbolsMap: function() {},
        getSlsStyle: function() {
            return slsStyle;
        },
        getSlsSize: function() {
            return slsSize;
        },
        createSymbol: function(path, color, angle, size) {
            return __createSymbol(path, color, angle, size);
        },
        prepareDownloadPlaceSymbol: function(typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, isUseSummary, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha) {
            var placeSymbol = null;
            var shape = typeSymbols[typeId].shape;
            if (useIcon || isCombined || isNew || isUpdated || isSelected || ishighlighted || (shape != "Circle" && shape != "Square")) {
                var iconUri = composeIconUri(typeId, useIcon, iconUri, typeId, isSelected, ishighlighted, isUseSummary, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha);
                iconSize = calculateIconSize(iconSize, isSelected, ishighlighted, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator);
                iconUri += "&is=" + iconSize;
                if (!placeSymbolsMapLoading.hasOwnProperty(iconUri) && !placeSymbolImageData.hasOwnProperty(iconUri)) {
                    placeSymbolsMapLoading[iconUri] = true;
                }
            }
        },
        downloadPlaceSymbols: function() {
            var postdata = [];
            for (var item in placeSymbolsMapLoading) {
                postdata.push(item);
            }
            dojo.xhrPost({
                url: iconProviderUrl + "?bulkload=true",
                postData: dojo.toJson(postdata),
                handleAs: "json",
                load: function(data) {
                    for (var propt in data) {
                        var iconUri = propt;
                        var imageData = data[propt];
                        placeSymbolImageData[iconUri] = imageData;
                        delete placeSymbolsMapLoading[iconUri];
                    }
                }
            });
        },
        isPlaceSymbolsReady: function() {
            return Object.keys(placeSymbolsMapLoading).length === 0
        },
        createPlaceSymbol: function(typeId, useIcon, iconUri, iconSize, isSelected, ishighlighted, isUseSummary, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha) {
            var placeSymbol = null;
            var shape = typeSymbols[typeId].shape;
            if (useIcon || isCombined || isNew || isUpdated || isSelected || ishighlighted || (shape != "Circle" && shape != "Square")) {
                var iconUri = composeIconUri(typeId, useIcon, iconUri, typeId, isSelected, ishighlighted, isUseSummary, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator, alpha);
                iconSize = calculateIconSize(iconSize, isSelected, ishighlighted, isCombined, useMultitypeDecorator, isNew, isUpdated, useLinkupDecorator);
                iconUri += "&is=" + iconSize;
                if (!placeSymbolImageData.hasOwnProperty(iconUri)) {
                    placeSymbol = new PictureMarkerSymbol(iconProviderUrl + "?" + iconUri,iconSize,iconSize);
                } else {
                    if (placeSymbolImageData[iconUri] == "BROKEN_IMAGE") {
                        placeSymbol = new PictureMarkerSymbol("/Centrifuge/h5/img/LegendItem_TEXT.png",iconSize,iconSize);
                    } else {
                        placeSymbol = new PictureMarkerSymbol({
                            "imageData": placeSymbolImageData[iconUri]
                        });
                        placeSymbol.setHeight(iconSize);
                        placeSymbol.setWidth(iconSize);
                    }
                }
            } else {
                var smsStyle;
                if (shape == "Square") {
                    smsStyle = SimpleMarkerSymbol.STYLE_SQUARE;
                } else {
                    smsStyle = SimpleMarkerSymbol.STYLE_CIRCLE;
                }
                var color = typeSymbols[typeId].color;
                if (typeof isUseSummary != 'undefined' && isUseSummary !== null && isUseSummary) {
                    var slsColor = Color.fromHex("#" + color);
                    var placeOutline = new SimpleLineSymbol(slsStyle,slsColor,slsSize);
                    var smsColor = Color.fromHex("#000000");
                    smsColor.a = 0;
                    placeSymbol = new SimpleMarkerSymbol(smsStyle,iconSize,placeOutline,smsColor);
                } else {
                    var smsColor = Color.fromHex("#" + color);
                    if (alpha) {
                        smsColor.a = alpha;
                    }
                    placeSymbol = new SimpleMarkerSymbol(smsStyle,iconSize,null,smsColor);
                }
            }
            return placeSymbol;
        },
        foundLineSymbol: function(a) {
            if (a.typeId == -1) {
                return true;
            }
            var lineSymbol = lineSymbols[a.typeId];
            return lineSymbol != null;
        },
        createLineDefinition: function(map, a, sourceGeometries, destinationGeometries, showArrow, iXMin, iXMax) {
            var key = a.typeId;
            var spatialReference = new SpatialReference({
                wkid: 4326
            });
            var polyline = new Polyline(spatialReference);
            var mapArrowCoordinatesList = [];

            var i = 0;
            while (i < sourceGeometries.length) {
                var sourceGeometry = sourceGeometries[i];
                var destinationGeometry = destinationGeometries[i];

                var changeInX = destinationGeometry.x - sourceGeometry.x;
                var changeInY = destinationGeometry.y - sourceGeometry.y;
                var isChangeInXTooLarge = false;
                var leftToRight = destinationGeometry.x > sourceGeometry.x;
                if (leftToRight) {
                  if (destinationGeometry.x - sourceGeometry.x > 180) {
                    isChangeInXTooLarge = true;
                  }
                } else {
                  if (sourceGeometry.x - destinationGeometry.x > 180) {
                    isChangeInXTooLarge = true;
                  }
                }
                if (isChangeInXTooLarge) {
                  if (leftToRight) {
                    changeInX -= 360;
                  } else {
                    changeInX += 360;
                  }
                }
                buildPolyline(polyline, sourceGeometry, destinationGeometry, changeInX, changeInY, iXMin, iXMax);

                if (showArrow) {
                    var mapArrowCoordinates = createMapArrowCoordinates(map, sourceGeometry, destinationGeometry);
                    if (mapArrowCoordinates.length > 0) {
                        mapArrowCoordinatesList.push(mapArrowCoordinates);
                    }
                }

                i++;
            }
            var lineSymbol;
            if (key == -1) {
                lineSymbol = selectedLineSymbol;
            } else {
                lineSymbol = lineSymbols[key];
            }
            var lineGraphic = new Graphic(polyline,lineSymbol);

            var arrowGraphics = null;
            var arrowGraphic = null;
            if (showArrow) {
                var arrowPolyline = buildArrowPolyline(mapArrowCoordinatesList);
                var arrowLineSymbol = arrowLineSymbols[key];
                arrowGraphic = new Graphic(arrowPolyline,arrowLineSymbol);
            }

            idAssociationMap[key] = {
                "lineGraphic": lineGraphic,
                "arrowGraphics": arrowGraphics,
                "arrowGraphic": arrowGraphic
            };
            return idAssociationMap[key];
        },
        getLineDefinitionKeys: function() {
            if (idAssociationMap == null) {
                return [];
            }
            return Object.keys(idAssociationMap);
        },
        getLineDefinition: function(key) {
            if (idAssociationMap != null) {
                if (key in idAssociationMap) {
                    return idAssociationMap[key];
                } else {
                    return null;
                }
            }
        },
        getLineSymbolWidth: function(typeId) {
            if (typeId == -1) {
                return selectedLineWidth;
            } else {
                return associationSymbols[typeId].width;
            }
        },
        isLineShowDirection: function(typeId) {
            if (typeId == -1) {
                return false;
            } else {
                return associationSymbols[typeId].showDirection;
            }
        },
        hasArrows: function() {
            var keys = Object.keys(associationSymbols);
            var i = 0;
            while (i < keys.length) {
                if (associationSymbols[keys[i]].showDirection)
                    return true;
                i++;
            }
            return false;
        }
    });
});
