function initMap(divID, uuid) {
    var urlFormat = "/Centrifuge/services/graphs2/actions/getTile?vduuid=" + uuid + "&tw=256&th=256&z=${z}&x=${x}&y=${y}";

    var layer = new OpenLayers.Layer.XYZ("relgraph", urlFormat, {
	"tileSize": new OpenLayers.Size(256, 256),
	"displayOutsideMaxExtent": true,
	"maxExtent": new OpenLayers.Bounds(0, 0, 1e16, 1e16),
	"transitionEffect": "resize",
	    "buffer": 1
    });

    var map = new OpenLayers.Map({"div": "map", "layers": [layer]});

    return map;
}

initMap("map", "d6900467-30fc-4d7d-bb44-d3b2a2bc2a7c");
