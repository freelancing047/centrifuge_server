var GeoPinIconPaths = ['icon1','icon2','icon3'];

function MapChartViewDefJson(data) {
  this.data = data;

}

MapChartViewDefJson.prototype.getViewDefJson = function(viewDefModal) {
  var viewdef = new Object();
  viewdef.class = "csi.server.common.model.visualization.MapChartViewDef";
  viewdef.CsiUUID = utils.guidGenerator();
  viewdef.clientProperties = this.getClientProperties(viewDefModal);
  viewdef.chartType = "MAP_CHART";
  viewdef.mapName = $(viewDefModal).find('#geoMapChartName').val();
  viewdef.suppressNulls = $(viewDefModal).find('#suppressNulls').is(":checked");
  viewdef.chartFunction = $(viewDefModal).find('#heatmapFunction option:selected').val();
  viewdef.bubbleFunction = $(viewDefModal).find('#bubblemapFunction option:selected').val();
  var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
  var lat = utils.getFieldDef(fieldDefs, $(viewDefModal).find('#plotmapLatitude').val());
  if (lat != null) {
    viewdef.latitudeField = lat.fieldName;
  }
  var lon = utils.getFieldDef(fieldDefs, $(viewDefModal).find('#plotmapLongitude').val());
  if (lon != null) {
    viewdef.longitudeField = lon.fieldName;
  }
  viewdef.icon = GeoPinIconPaths[$(viewDefModal).find('#plotmapIcon').prop('selectedIndex')];
  viewdef.cell = this.getChartDimensionJson(viewDefModal, fieldDefs, $(viewDefModal).find('#heatmapState').val());
  viewdef.bubbleCell = this.getChartDimensionJson(viewDefModal, fieldDefs, $(viewDefModal).find('#bubblemapField').val());
  viewdef.toolTipFields = this.getTooltips(fieldDefs, $(viewDefModal).find('#tooltipField'));
  var dims = [];
  dims.push(this.getChartDimensionJson(viewDefModal, fieldDefs, $(viewDefModal).find('#heatmapMeasure').val()));
  viewdef.dimensions = dims;
  return viewdef;
};

MapChartViewDefJson.prototype.getChartDimensionJson = function(viewDefModal, fieldDefs, fieldName) {
  var chartdim = new Object();
  chartdim.class = "csi.server.common.model.chart.ChartDimension";
  chartdim.CsiUUID = utils.guidGenerator();
  chartdim.clientProperties = this.getClientProperties(viewDefModal);
  chartdim.dimName = fieldName;
  chartdim.ordinal = "0";
  chartdim.sortOrder = "ASC";
  chartdim.bundleFunction = "CONCAT";
  var dims = [];
  chartdim.dimensionFields = dims.push(this.getDimensionFieldJson(viewDefModal, fieldDefs, fieldName));
  return chartdim;
};

MapChartViewDefJson.prototype.getDimensionFieldJson = function(viewDefModal, fieldDefs, fieldName) {
  var dimField = new Object();
  dimField.class = "csi.server.common.model.DimensionField";
  dimField.clientProperties = this.getClientProperties(viewDefModal)
  dimField.fieldDef = utils.getFieldDef(fieldDefs, fieldName);
  dimField.ordinal = "0";
  return dimField;
};

MapChartViewDefJson.prototype.getTooltips = function(fieldDefs, plotmapTooltips) {
  var tooltips = [];
  $.each( plotmapTooltips.find('div'), function(index, fieldDefName) {
    tooltips.push(utils.getFieldDef(fieldDefs, $(fieldDefName).text()));
  });
  return tooltips;
};
MapChartViewDefJson.prototype.getClientProperties = function(viewDefModal) {
  var clientProps = new Object();
  clientProps.class = "csi.server.common.model.dto.CsiMap";
  clientProps['render.threshold'] = $(viewDefModal).find('#threshold').val();
  clientProps['vizBox.height'] = 500;
  clientProps['vizBox.left'] = 383;
  clientProps['vizBox.loadOnStartup'] = $(viewDefModal).find('#loadOnStartUp').is(":checked");
  clientProps['vizBox.top'] = 10;
  clientProps['vizBox.width'] = 800;  
  return clientProps;
};