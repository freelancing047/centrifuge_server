define(["dojo/_base/declare",
    "esri/geometry/Point",
    "esri/geometry/ScreenPoint",
    "esri/geometry/Extent",
    "esri/tasks/query"],
  function(declare,
    Point,
    ScreenPoint,
    Extent,
    Query) {
    return declare(null, {
      map: null,
      constructor: function(map) {
        this.map = map;
      },
      setMap: function(mapIn) {
        this.map = mapIn;
      },
      getExtent: function(screenPoint, pixel) {
        var screenPoint1 = new ScreenPoint(screenPoint.x - (pixel / 2), screenPoint.y - (pixel / 2));
        var screenPoint2 = new ScreenPoint(screenPoint.x + (pixel / 2), screenPoint.y + (pixel / 2));
        var mapPoint1 = this.map.toMap(screenPoint1);
        var mapPoint2 = this.map.toMap(screenPoint2);
        var extent = Extent(mapPoint1.x,mapPoint2.y,mapPoint2.x,mapPoint1.y,this.map.extent.spatialReference);
        return extent;
      }, 
      arePointsWithinPixels: function(firstMapPoint, secondMapPoint, pixels) {
        if (firstMapPoint === null || secondMapPoint === null) return false;
        firstMapPoint = firstMapPoint.normalize();
        secondMapPoint = secondMapPoint.normalize();
        var firstMapScreenPoint = this.map.toScreen(firstMapPoint);
        var secondMapScreenPoint = this.map.toScreen(secondMapPoint);
        var deltaX = secondMapScreenPoint.x - firstMapScreenPoint.x;
        var deltaY = secondMapScreenPoint.y - firstMapScreenPoint.y;
        var distanceSquare = Math.pow(deltaX, 2) + Math.pow(deltaY, 2);
        var pixelsSquare = Math.pow(pixels, 2);
        return pixelsSquare > distanceSquare;
      }
    });
  });
