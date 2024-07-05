/**
 * TODO docs
 */
define([
        "dojo/_base/declare",
        "extras34/commonModule",
        "dojo/dom"
    ],
    function(
        declare,
        commonModule,
        dom
    ){
    //our configs
    var dvid;
    var vizid;
    var mapDivContainer;
    var mapDiv;

    function getMapGrid() {
        var mapGrid = dom.byId("mapDiv_layers");
        var gridDiv = mapGrid.children[0];
        return gridDiv;
    }

    // to be called after the overlay is drawn.
    function finalizeBase64(base64) {
        var mapGrid = dom.byId("mapDiv_root");
        window.parent.exportPNG(vizid, base64, mapGrid.style.width.replace('px', ''), mapGrid.style.height.replace('px', ''));
    }

    /*
         *    Parse the integers out of the translate CSS function.
         *     NOTE: will break if used with single value eg translate(12px);
         */
    function getTranslateValue(cssProp, axis) {
        cssProp = cssProp.split('(')[1];
        var transform = cssProp.replace(')', '');
        var split = transform.split(',');
        var xTrans = split[0].replace('px', '');
        var yTrans = split[1].replace('px', '');

        var mgt = getMapGrid().style.transform
        mgt = mgt.split('(')[1];
        var tr = mgt.replace('translate(', '').replace(')', '');
        var sp = tr.split(',');
        // so the idea here is that if we have something other tahn 0, we need to account for that so we don't end up having werid sizing
        var xOffset = sp[0].replace('px', '');
        var yOffset = sp[1].replace('px', '');

        // console.log(tr, 'xOffset', xOffset, " Y Offset", yOffset);
        // console.log("x ", xTrans, " y ", yTrans);

        if (axis == "x") {
            return Number(xTrans) + Number(xOffset);
        } else if (axis == "y") {
            return Number(yTrans) + Number(yOffset);
        }
    }

    return declare(commonModule, {

        constructor: function(params) {
            // maybe not needed here after all
            dvid = params.dv_uuid;
            vizid = params.viz_uuid;
            mapDivContainer = dom.byId(params.mapDivContainerId);
            mapDiv = dom.byId(params.mapId);
        },

        doExport: function() {
            // what do we need here - we need to build the canvas base map

            //layers will have a child div for the basemap layer, and then another one for svg
            var mapGrid = getMapGrid();
            var gridDiv = mapGrid.children[0];

            console.log(mapGrid, mapGrid.style.transform);

            // interesitng observations trying now

            // create the canvas and size it to the map viz size
            var can = document.createElement('canvas');

            var sizes = dom.byId("mapDiv_root");

            can.width = sizes.style.width.replace('px', '');
            can.height = sizes.style.height.replace('px', '');

            var ctx = can.getContext('2d');

            // keep total to make sure we export after everything is loaded.
            var imageCount = gridDiv.children.length;

            for (var i = 0; i < gridDiv.children.length; i++) {
                var gridCell = gridDiv.children[i];
                var gridCellSrc = gridCell.src;

                // create the image for the current tile.
                var img = new Image();
                img.style.transform = gridCell.style.transform;
                img.style.width = gridCell.style.width;
                img.style.height = gridCell.style.height;

                //https://stackoverflow.com/questions/20424279/canvas-todataurl-securityerror
                // if this isn't set, canvas will fail to convert to base64 because its 'tainted"
                img.setAttribute('crossOrigin', 'anonymous');

                img.onload = function() {

                    var xTrans = getTranslateValue(this.style.transform, 'x');
                    var yTrans = getTranslateValue(this.style.transform, 'y');
                    // console.log('drawing offsets: ', xTrans, yTrans);
                    ctx.drawImage(this, xTrans, yTrans);

                    imageCount--;
                    if (imageCount == 0) {
                        var svg = dom.byId('mapDiv_gc');
                        var svg_xml = (new XMLSerializer()).serializeToString(svg);

                        img = new Image();

                        img.onload = function() {
                            ctx.drawImage(img, 0, 0);
                            finalizeBase64(can.toDataURL('image/png'));
                            // console.log('canvas exported.. ' + base64mapExport.length);
                        };

                        // set the source of the image as base64 of the svg layer on the map
                        img.src = 'data:image/svg+xml;base64,' + btoa(svg_xml);
                    }
                }

                // set the image source to whatever the image src was in the grid.
                img.src = gridCellSrc;
            }
        }
    });
});
