define([
  "dojo/_base/declare",
  "dojo/_base/array",
  "esri/Color",
  "dojo/_base/connect",

  "esri/SpatialReference",
  "esri/geometry/Point",
  "esri/graphic",
  "esri/symbols/SimpleMarkerSymbol",
  "esri/symbols/TextSymbol",

  "esri/layers/GraphicsLayer"
], function (
  declare, arrayUtils, Color, connect,
  SpatialReference, Point, Graphic, SimpleMarkerSymbol, TextSymbol,
  GraphicsLayer
) {
  return declare([GraphicsLayer], {
    constructor: function(options) {
      this._clusterData = options.data || [];
      this._clusters = {};
      this._clustersById = [];
      this._clustersSize = 0;
      this._cacheDone = false;
      this._clusterLabelColor = options.labelColor || "#000";
      // labelOffset can be zero so handle it differently
      this._clusterLabelOffset = (options.hasOwnProperty("labelOffset")) ? options.labelOffset : -5;
      // graphics that represent a single point
      this._singles = []; // populated when a graphic is clicked
      this._showSingles = options.hasOwnProperty("showSingles") ? options.showSingles : true;
      // symbol for single graphics
      var SMS = SimpleMarkerSymbol;
      this._singleSym = options.singleSymbol || new SMS("circle", 10, null, new Color("#00a"));
      this._singleSelectedSym = options.singleSelectedSymbol || new SMS("circle", 10, null, new Color("#a00"));
      this._maxSingles = options.maxSingles || 3000;
      this._sr = options.spatialReference || new SpatialReference({ "wkid": 102100 });

      this._zoomEnd = null;
      this._key = null;
    },

    // override esri/layers/GraphicsLayer methods
    _setMap: function(map, surface) {
      // calculate and set the initial resolution
      this._clusterResolution = map.extent.getWidth() / map.width; // probably a bad default...
      this._clusterGraphics();

      // connect to onZoomEnd so data is re-clustered when zoom level changes
      this._zoomEnd = connect.connect(map, "onZoomEnd", this, function() {
        // update resolution
        this._clusterResolution = this._map.extent.getWidth() / this._map.width;
      });

      // GraphicsLayer will add its own listener here
      var div = this.inherited(arguments);
      return div;
    },

    _unsetMap: function() {
      this.inherited(arguments);
      connect.disconnect(this._zoomEnd);
    },

    // public ClusterLayer methods
    add: function(p) {
      if (p && p != null) {
      // Summary:  The argument is a data point to be added to an existing cluster. If the data point falls within an existing cluster, it is added to that cluster and the cluster's label is updated. If the new point does not fall within an existing cluster, a new cluster is created.
      //
      // if passed a graphic, use the GraphicsLayer's add method
      if ( p.declaredClass ) {
        this.inherited(arguments);
        return;
      }

      // add the new data to _clusterData so that it's included in clusters
      // when the map level changes
      this._clusterData.push(p);
      // look for an existing cluster for the new point
      var clusterKeys = p.values[this._key];
      for (clusterKey in clusterKeys) {
		var count = clusterKeys[clusterKey];
		if ( this._clusters.hasOwnProperty(clusterKey) ) {
          		var c = this._clusters[clusterKey];
	  		this._clusterAddPoint(p, c, count);
	  		// update the cluster's geometry
	  		this._updateClusterGeometry(c);
	  		// update the label
	  		this._updateLabel(c);
		} else {
			this._clusterCreate(p, clusterKey, count);
        		this._showCluster(p);
        	}
      }
      }
    },

    clear: function() {
      // Summary:  Remove all clusters and data points.
      this.inherited(arguments);
    },

    clearSingles2: function() {
    	this.clearSingles(this._singles);
    },

    clearSingles: function(singles) {
      // Summary:  Remove graphics that represent individual data points.
      var s = singles || this._singles;
      arrayUtils.forEach(s, function(g) {
        this.remove(g);
      }, this);
      this._singles.length = 0;
    },

    getGraphic: function(clusterId) {
    	var c = this._clustersById[clusterId];
    	return c.graphic.geometry;
    },

    showSingles: function(clusterId) {
      // remove any previously showing single features
      this.clearSingles(this._singles);

      // find single graphics that make up the cluster that was clicked
      // would be nice to use filter but performance tanks with large arrays in IE
      var singles = [];
      for ( var i = 0, il = this._clusterData.length; i < il; i++) {
        if ( this._clusterData[i].clusterIds.has(clusterId) ) {
          singles.push(this._clusterData[i]);
        }
      }
      if ( singles.length > this._maxSingles ) {
        alert("Sorry, that cluster contains more than " + this._maxSingles + " points. Zoom in for more detail.");
        return;
      } else {
        // stop the click from bubbling to the map
        this._addSingles(singles);
      }
    },

    setKey: function(key) {
    	if (this._key != key) {
    		this._key = key;
		this._cacheDone = false;
	        this.clear();
	        this._clusters = {};
		this._clustersById = [];
	        this._clustersSize = 0;
		this._clusterGraphics();
	}
    },

    queryLayer: function(geometry) {
    	var inBuffer = [];
	for ( var i in this._clusters ) {
        	var c = this._clusters[i];
		if(geometry.contains(c.point)){
			inBuffer.push(c);
		}
	}
	return inBuffer;
    },

    redrawAfterSelection : function(clusterHash, inBuffer) {
	var clustersAlreadyVisited = new Set();
	for (var i = 0; i < inBuffer.length; i++) {
		if (clusterHash != null) {
			var pointId = inBuffer[i];
			var p = clusterHash[pointId];
			if (p && p != null) {
			var clusterKeys = p.values[this._key];
			for (clusterKey in clusterKeys) {
				if (!clustersAlreadyVisited.has(clusterKey)) {
					var c = this._clusters[clusterKey];
					c.attributes.numSelected = 0;
					for (var j = 0; j < c.attributes.objectIDs.length; j++) {
						var objectID = c.attributes.objectIDs[j];
						var point = clusterHash[objectID];
						c.attributes.numSelected += point.numSelected;
					}
					clustersAlreadyVisited.add(clusterKey);
					this.remove(c.graphic);
					this._createAndAddClusterGraphic(c);
					if (c.labelGraphic != null) {
						this.remove(c.labelGraphic);
						this.add(c.labelGraphic);
					}
				}
			}
			}
		}
	}
    },

    // internal methods
    _clusterGraphics: function() {
    	if (this._key == null) return;
	if (this._cacheDone) return;
      // first time through, loop through the points
      for ( var j = 0, jl = this._clusterData.length; j < jl; j++ ) {
        // see if the current feature should be added to a cluster
        var p = this._clusterData[j];
	if (p && p != null) {
	p.clusterIds = new Set();
	var clusterKeys = p.values[this._key];
	for (clusterKey in clusterKeys) {
		var count = clusterKeys[clusterKey];
		if ( this._clusters.hasOwnProperty(clusterKey) ) {
          		var c = this._clusters[clusterKey];
	  		this._clusterAddPoint(p, c, count);
		} else {
			this._clusterCreate(p, clusterKey, count);
        	}
	}
	}
      }
      this._showAllClusters();
      this._cacheDone = true;
    },

    _clusterTest: function(p, cluster) {
	if (p && p != null) return (p.values[this._key] == cluster.attributes.clusterColumn);
	else return false;
    },

    // points passed to clusterAddPoint should be included
    // in an existing cluster
    // also give the point an attribute called clusterId
    // that corresponds to its cluster
    _clusterAddPoint: function(p, cluster, count) {
      // average in the new point to the cluster geometry
      var pointCount, x, y;
      pointCount = cluster.attributes.pointCount;
      x = (p.x + (cluster.x * pointCount)) / (pointCount + 1);
      y = (p.y + (cluster.y * pointCount)) / (pointCount + 1);
      cluster.x = x;
      cluster.y = y;

      // build an extent that includes all points in a cluster
      // extents are for debug/testing only...not used by the layer
      if ( p.x < cluster.attributes.extent[0] ) {
        cluster.attributes.extent[0] = p.x;
      } else if ( p.x > cluster.attributes.extent[2] ) {
        cluster.attributes.extent[2] = p.x;
      }
      if ( p.y < cluster.attributes.extent[1] ) {
        cluster.attributes.extent[1] = p.y;
      } else if ( p.y > cluster.attributes.extent[3] ) {
        cluster.attributes.extent[3] = p.y;
      }

      // increment the count
      cluster.attributes.pointCount++;
      cluster.attributes.clusterCount += count;

      cluster.attributes.objectIDs.push(p.objectID);
      cluster.attributes.numSelected += p.numSelected;

      // attributes might not exist
      if ( ! p.hasOwnProperty("attributes") ) {
        p.attributes = {};
      }
      // give the graphic a cluster id
      if (!p.clusterIds.has(cluster.attributes.clusterId)) {
      	p.clusterIds.add(cluster.attributes.clusterId);
      }
    },

    // point passed to clusterCreate isn't within the
    // clustering distance specified for the layer so
    // create a new cluster for it
    _clusterCreate: function(p, clusterKey, count) {
      this._clustersSize++;
      var clusterId = this._clustersSize;
      // console.log("cluster create, id is: ", clusterId);
      // p.attributes might be undefined
      if ( ! p.attributes ) {
        p.attributes = {};
      }
      p.clusterIds.add(clusterId);
      // create the cluster
      var cluster = {
        "x": p.x,
        "y": p.y,
        "attributes" : {
	  "pointCount": 1,
          "clusterCount": count,
          "clusterId": clusterId,
          "extent": [ p.x, p.y, p.x, p.y ],
	  "clusterColumn": clusterKey,
	  "objectIDs": [],
	  "numSelected": 0
        }
      };
      cluster.attributes.objectIDs.push(p.objectID);
      cluster.attributes.numSelected += p.numSelected;
      this._clusters[clusterKey] = cluster;
      this._clustersById.push(cluster);
    },

    _createAndAddClusterGraphic: function(c) {
      c.point = new Point(c.x, c.y, this._sr);
      c.graphic = new Graphic( c.point, null, c.attributes );
      this.add(c.graphic);
    },

    _createAndAddClusterLabelGraphic: function(c) {
      var label = new TextSymbol(c.attributes.clusterCount)
        .setColor(new Color(this._clusterLabelColor))
        .setOffset(0, this._clusterLabelOffset);
      c.labelGraphic = new Graphic( c.point, label, c.attributes );
      this.add(c.labelGraphic);
    },

    _showAllClusters: function() {
      for (var i in this._clusters) {
        var c = this._clusters[i];
	this._showCluster(c);
      }
    },

    _showCluster: function(c) {
      this._createAndAddClusterGraphic(c);
      // code below is used to not label clusters with a single point
      if ( c.attributes.clusterCount == 1 ) {
      	c.labelGraphic = null;
        return;
      }
      this._createAndAddClusterLabelGraphic(c);
    },

    _addSingles: function(singles) {
      // add single graphics to the map
      arrayUtils.forEach(singles, function(p) {
	var attributes = p.attributes;
	var symbol = this._singleSym;
	if (p.numSelected > 0) {
		symbol = this._singleSelectedSym;
	}
	attributes["ObjectID"] = p.objectID;
        var g = new Graphic(
          new Point(p.x, p.y, this._sr),
          symbol,
          attributes,
          this._singleTemplate
        );
        this._singles.push(g);
        if ( this._showSingles ) {
          this.add(g);
        }
      }, this);
    },

    _updateClusterGeometry: function(c) {
      // find the cluster graphic
      var cg = arrayUtils.filter(this.graphics, function(g) {
        return ! g.symbol &&
               g.clusterIds.has(c.attributes.clusterId);
      });
      if ( cg.length == 1 ) {
        cg[0].geometry.update(c.x, c.y);
      } else {
        console.log("didn't find exactly one cluster geometry to update: ", cg);
      }
    },

    _updateLabel: function(c) {
      // find the existing label
      var label = arrayUtils.filter(this.graphics, function(g) {
        return g.symbol &&
               g.symbol.declaredClass == "esri.symbol.TextSymbol" &&
               g.clusterIds.has(c.attributes.clusterId);
      });
      if ( label.length == 1 ) {
        // console.log("update label...found: ", label);
        this.remove(label[0]);
        var newLabel = new TextSymbol(c.attributes.clusterCount)
          .setColor(new Color(this._clusterLabelColor))
          .setOffset(0, this._clusterLabelOffset);
        this.add(
          new Graphic(
            new Point(c.x, c.y, this._sr),
            newLabel,
            c.attributes
          )
        );
        // console.log("updated the label");
      } else {
        console.log("didn't find exactly one label: ", label);
      }
    },

  });
});

