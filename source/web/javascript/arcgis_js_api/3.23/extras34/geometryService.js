define(["dojo/_base/declare"],
  function(declare) {
    function fromEsriToWgs84(x, y) {
        var fromProjection = 'PROJCS["WGS_1984_Web_Mercator_Auxiliary_Sphere",GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Mercator_Auxiliary_Sphere"],PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",0.0],PARAMETER["Standard_Parallel_1",0.0],PARAMETER["Auxiliary_Sphere_Type",0.0],UNIT["Meter",1.0],AUTHORITY["ESRI","102100"]]';
        var toProjection = 'EPSG:4326';
        return proj4(fromProjection, toProjection, {
            x: x,
            y: y
        });
    }

    function fromWgs84ToEsri(x, y) {
        var fromProjection = 'EPSG:4326';
        var toProjection = 'PROJCS["WGS_1984_Web_Mercator_Auxiliary_Sphere",GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Mercator_Auxiliary_Sphere"],PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",0.0],PARAMETER["Standard_Parallel_1",0.0],PARAMETER["Auxiliary_Sphere_Type",0.0],UNIT["Meter",1.0],AUTHORITY["ESRI","102100"]]';
        return proj4(fromProjection, toProjection, {
            x: x,
            y: y
        });
    }
    return declare(null, {
        constructor: function() {
        },
        project: function(x, y) {
            return fromEsriToWgs84(x, y);
        },
        projectBack: function(x, y) {
            return fromWgs84ToEsri(x, y);
        }
    });
});
