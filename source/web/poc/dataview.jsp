<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Centrifuge</title>
        <link href="../css/jquery.dataTables.css" rel="stylesheet">
        <link href="../css/style.css" rel="stylesheet" type="text/css">
        <link href="../css/bootstrap.css" rel="stylesheet">
        <link href="../css/custom.css" rel="stylesheet">
        <link href="../css/admin_intenso_main.css" rel="stylesheet">
        <link href="../css/chosen.css" rel="stylesheet">
        <link href="../css/datepicker.css" rel="stylesheet">


        <link rel="stylesheet" href="../css/nanoscroller/nanoscroller.css">
        <link rel="stylesheet" href="../css/nanoscroller/style.css">


        <!-- OpenLayers for relgraph display -->
        <script src="../javascript/OpenLayers/OpenLayers.js"></script>

        <!-- For tree view -->
        <link href="../css/dynatree/skin-vista/ui.dynatree.css" rel="stylesheet"/>
        <!-- For color picker --->
        <link href="../css/colorpicker.css" rel="stylesheet"/>
        <!-- New simple color picker ---->
        <link rel="stylesheet" type="text/css" href="../css/jquery.simplecolorpicker.css"/>
        <link rel="stylesheet" type="text/css"
              href="../javascript/jquery.imgareaselect-0.9.3/css/imgareaselect-csi.css"/>
        <link href="../css/TableTools.css" rel="stylesheet">
        <link href="../css/geomap.css" rel="stylesheet">
        <link rel="stylesheet" href="../css/ionicons.min.css">
        <link href=' ../fonts/ionicons.woff' rel='stylesheet' type='text/css'>

        <script src="../javascript/jquery-1.8.1.js"></script>
        <script src="../javascript/jquery-ui-1.8.23.custom.min.js"></script>
        <script src="../javascript/underscore.js"></script>
        <script src="../javascript/bootstrap-transition.js"></script>
        <script src="../javascript/bootstrap-alert.js"></script>
        <script src="../javascript/bootstrap-modal.js"></script>
        <script src="../javascript/bootstrap-dropdown.js"></script>
        <script src="../javascript/bootstrap-scrollspy.js"></script>
        <script src="../javascript/bootstrap-tab.js"></script>
        <script src="../javascript/bootstrap-tooltip.js"></script>
        <script src="../javascript/bootstrap-popover.js"></script>
        <script src="../javascript/bootstrap-button.js"></script>
        <script src="../javascript/bootstrap-collapse.js"></script>
        <script src="../javascript/bootstrap-carousel.js"></script>
        <script src="../javascript/bootstrap-typeahead.js"></script>
        <script src="../javascript/bootstrap-datepicker.js"></script>
        <!-- To fix scroll bar -->
        <script src="../javascript/nanoScroller/jquery.nanoscroller.min.js"></script>
        <script src="../javascript/nanoScroller/overthrow.min.js"></script>
        <!-- For tree view -->
        <script src="../javascript/jquery.cookie.js"></script>
        <script src="../javascript/jquery.dynatree.min.js"></script>
        <script src="../javascript/jquery.ddslick.min.js"></script>
        <!--- For node diagram --->
        <script type="text/javascript"
                src="../javascript/jquery.jsPlumb-1.3.14-all.js"></script>
        <script type="text/javascript" src="../javascript/jsplumb_node_init.js"></script>
        <!-- For color picker --->
        <script type="text/javascript" src="../javascript/colorpicker.js"></script>
        <!-- New simple color picker --->
        <script src="../javascript/jquery.simplecolorpicker.js"></script>
        <script src="../javascript/jquery.dataTables.js"></script>
        <script src="../javascript/jquery.formatDateTime.js"></script>
        <script src="../javascript/ZeroClipboard.js"></script>
        <script src="../javascript/TableTools.js"></script>
        <script src="../javascript/mustache.js"></script>
        <script src="../javascript/jquery.scrollto.js"></script>
        <!-- Files for custom context menu -->
        <script src="../javascript/jquery.contextMenu.js" type="text/javascript"></script>
        <link href="../css/jquery.contextMenu.css" rel="stylesheet" type="text/css"/>
        <script type="text/javascript" src="../javascript/filterCriterion.js"></script>
        <script type="text/javascript" src="../javascript/filter.js"></script>
        <script type="text/javascript" src="../javascript/action.filterDataTable.js"></script>
        <script type="text/javascript" src="../javascript/utils.js"></script>
        <script type="text/javascript" src="../javascript/dataview.js"></script>
        <script type="text/javascript" src="../javascript/relationgraph-json.js"></script>
        <script type="text/javascript" src="../javascript/load.js"></script>
        <script type="text/javascript" src="../javascript/api/csi.js"></script>
        <script type="text/javascript" src="../javascript/api/csi.relgraph.js"></script>
        <script type="text/javascript" src="../javascript/api/csi.viz.js"></script>
        <script type="text/javascript" src="../javascript/api/csi.dataview.js"></script>
        <script type="text/javascript" src="../javascript/api/csi.timeplayer.js"></script>
        <script type="text/javascript" src="../javascript/rg-panel.js"></script>
        <script type="text/javascript" src="../javascript/rgPanelFullWidth.js"></script>
        <script type="text/javascript" src="../javascript/rgview.js"></script>
        <script type="text/javascript" src="../javascript/action.deselectall.js"></script>
        <script type="text/javascript" src="../javascript/action.selectall.js"></script>
        <script src="../javascript/chosen.jquery.js"></script>
        <script type="text/javascript"
                src="../javascript/action.hideselection.js"></script>
        <script type="text/javascript"
                src="../javascript/action.unhideselection.js"></script>
        <script type="text/javascript" src="../javascript/action.createrg.js"></script>
        <script type="text/javascript"
                src="../javascript/jquery.imgareaselect-0.9.3/scripts/jquery.imgareaselect.js"></script>
        <script type="text/javascript" src="../javascript/action.drag-graph.js"></script>
        <script type="text/javascript" src="../javascript/jquery.mousewheel.js"></script>
        <script type="text/javascript" src="../javascript/action.zoom.js"></script>
        <script type="text/javascript" src="../javascript/action.fittosize.js"></script>
        <script type="text/javascript"
                src="../javascript/action.rgdrag.start.js"></script>
        <script type="text/javascript" src="../javascript/action.rgdrag.end.js"></script>
        <script type="text/javascript" src="../javascript/action.select.pan.js"></script>
        <script type="text/javascript"
                src="../javascript/action.select.region.js"></script>
        <script type="text/javascript"
                src="../javascript/action.rgdrag.start.js"></script>
        <script type="text/javascript"
                src="../javascript/action.rgmouse.move.js"></script>
        <script type="text/javascript" src="../javascript/action.finditemrg.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createtipsrg.js"></script>
        <script type="text/javascript"
                src="../javascript/action.zoomtoregion.js"></script>
        <script type="text/javascript"
                src="../javascript/action.selectnodesbytype.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createlinktable.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodetable.js"></script>
        <script type="text/javascript" src="../javascript/action.listlinks.js"></script>
        <script type="text/javascript"
                src="../javascript/action.selectnodebyid.js"></script>
        <script type="text/javascript"
                src="../javascript/action.selectlinkbyid.js"></script>
        <script type="text/javascript" src="../javascript/action.layout.js"></script>
        <script type="text/javascript"
                src="../javascript/action.fitToSelection.js"></script>
        <script type="text/javascript" src="../javascript/action.computeSNA.js"></script>
        <script type="text/javascript"
                src="../javascript/action.linkhideunhidetask.js"></script>
        <script type="text/javascript"
                src="../javascript/action.hideunselected.js"></script>
        <script type="text/javascript"
                src="../javascript/action.nodehideunhidetask.js"></script>
        <script type="text/javascript"
                src="../javascript/action.collapseExpandLegend.js"></script>
        <script type="text/javascript" src="../javascript/action.clicknoderg.js"></script>
        <script type="text/javascript"
                src="../javascript/action.findPathResults.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createPathsTable.js"></script>
        <script type="text/javascript" src="../javascript/action.listPaths.js"></script>
        <script type="text/javascript"
                src="../javascript/action.highlightpath.js"></script>
        <script type="text/javascript" src="../javascript/action.selectpath.js"></script>
        <script type="text/javascript"
                src="../javascript/action.addPathsToSelection.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createsearchresultstable.js"></script>
        <script type="text/javascript" src="../javascript/action.searchgraph.js"></script>
        <script type="text/javascript"
                src="../javascript/action.getgraphflags.js"></script>
        <script type="text/javascript"
                src="../javascript/action.bundleselectionmanually.js"></script>
        <script type="text/javascript"
                src="../javascript/action.bundlehandler.js"></script>
        <script type="text/javascript"
                src="../javascript/action.unbundlehandler.js"></script>
        <script type="text/javascript"
                src="../javascript/action.bundleentiregraphbyspec.js"></script>
        <script type="text/javascript"
                src="../javascript/action.bundleselectionbyspec.js"></script>
        <script type="text/javascript"
                src="../javascript/action.unbundleselection.js"></script>
        <script type="text/javascript"
                src="../javascript/action.unbundleentiregraph.js"></script>
        <script type="text/javascript"
                src="../javascript/action.selectNeighbors.js"></script>
        <script type="text/javascript"
                src="../javascript/action.revealNeighbours.js"></script>
        <script type="text/javascript" src="../javascript/action.rg.select.js"></script>
        <script type="text/javascript"
                src="../javascript/action.searchgraph.addall.js"></script>
        <script type="text/javascript"
                src="../javascript/action.searchgraph.addchosen.js"></script>
        <script type="text/javascript"
                src="../javascript/action.appearanceEditor.js"></script>
        <script type="text/javascript"
                src="../javascript/action.appearanceEditColor.js"></script>
        <script type="text/javascript" src="../javascript/action.savegraph.js"></script>
        <script type="text/javascript"
                src="../javascript/action.creatergsettingdialog.js"></script>
        <script type="text/javascript"
                src="../javascript/action.multiselectmodel.js"></script>
        <script type="text/javascript"
                src="../javascript/action.unbundlesinglenode.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditdialog.js"></script>
        <script type="text/javascript"
                src="../javascript/action.newLayoutActions.js"></script>
        <script type="text/javascript"
                src="../javascript/action.addworksheet.js"></script>
        <script type="text/javascript" src="../javascript/highcharts.js"></script>
        <script type="text/javascript" src="../javascript/charts.js"></script>
        <script type="text/javascript" src="../javascript/geomap.js"></script>
        <script type="text/javascript" src="../javascript/geomap-json.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditorlinkstab.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditorbasicstab.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditorbundlingtab.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditortooltiptab.js"></script>
        <script type="text/javascript"
                src="../javascript/action.renameworksheet.js"></script>
        <script type="text/javascript"
                src="../javascript/action.deleteworksheet.js"></script>
        <script type="text/javascript"
                src="../javascript/action.openworksheetasimage.js"></script>
        <script type="text/javascript"
                src="../javascript/action.createnodeeditorcomputedfieldstab.js"></script>
        <script type="text/javascript"
                src="../javascript/action.removevisualization.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditdialog.js"></script>
        <script type="text/javascript" src="../javascript/action.saveworksheetindex.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditdialog.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditorcomputedfieldstab.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditortooltiptab.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditorlinkstab.js"></script>
        <script type="text/javascript" src="../javascript/contextMenuFunctions.js"></script>
        <script type="text/javascript" src="../javascript/contextMenuNodeFunctions.js"></script>
        <script type="text/javascript" src="../javascript/contextMenuLinkFunctions.js"></script>
        <script type="text/javascript" src="../javascript/decideContextMenu.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditordirectiontab.js"></script>
        <script type="text/javascript" src="../javascript/action.sortSecondaryPanels.js"></script>
        <script type="text/javascript" src="../javascript/action.createlinkeditorbasicstab.js"></script>
        <script type="text/javascript" src="../javascript/action.refreshimage.js"></script>
        <script type="text/javascript" src="../javascript/action.selectinsecondarypanel.js"></script>
        <script type="text/javascript" src="../javascript/action.createtimeplayer.js"></script>
        <script type="text/javascript" src="../javascript/action.timeplayer.js"></script>
        <script type="text/javascript" src="../javascript/bootbox.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                utils = new Utils();
                dataview = new DataView();
                dataview.doLoad();
                $.data(document.body, 'dataview', dataview);
                $(".chart_type_button").click(function () {
                    selectChartType($(this).attr('value'), $(this).parent().attr('id'));
                });
            });
        </script>
    </head>
<body>
<script id="graph-max-limit-info-panel" type="text/html">
    <div id="graph-max-limit-info" class="graph-max-limit-info">
        <div style="text-align: left;"><br><br>The relationship graph has {{nodes-number}} items, which is too many to
            display at once.<br><br>The current configured threshold is {{threshold-number}} items. Use the Graph Search
            Tool to simplify the graph by identifying only the items that are of immediate interest to you.
        </div>
    </div>
</script>

<!-------------------------------------- Preload dialogue -------------------------------------------------->
<div id="preload-dialogue" class="modal hide small_modal">
    <span id="preloadText">Opening Dataview</span>
    <div class="progress progress-striped active">
        <div style="width: 100%;" class="bar"></div>
    </div>
</div>
<script type="text/javascript">
    $('#preload-dialogue').modal();
    var dataViewName = "";
    if ($.cookie('DataViewName') != undefined) dataViewName = $.cookie('DataViewName');
    $("#preloadText").text('Opening dataview "' + dataViewName + '"');
</script>
<!-------------------------------------- Preload dialogue end -------------------------------------------------->
<!--- Toolbar Template ---->
<script type="text/html" id="rg-toolbar-src">
    <ul class="nav nav-pills rg-toolbar subtabs">
        <li class="dropdown">
            <a class="dropdown-toggle action" data-toggle="dropdown">Action<b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a>Load</a>
                </li>
                <li>
                    <a id="savegraph{{index}}">Save</a>
                </li>
                <li class="divider"></li>
                <li>
                    <a>Broadcast As Filter</a>
                </li>
                <li>
                    <a>Broadcast As Selection</a>
                </li>
                <li>
                    <a>Clear Broadcast</a>
                </li>
                <li>
                    <a>Listen For Broadcasts</a>
                </li>
                <li class="divider"></li>
                <li>
                    <a>Spinoff...</a>
                </li>
                <li>
                    <a>Print</a>
                </li>
                <li>
                    <a>Publish...</a>
                </li>
                <li>
                    <a>Export To Csv</a>
                </li>
                <li>
                    <a>Open As Image</a>
                </li>
            </ul>
        </li>
        <li class="dropdown">
            <a class="dropdown-toggle edit" data-toggle="dropdown">Edit<b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a id="selectall{{index}}">Select All</a>
                </li>
                <li>
                    <a id="deselectall{{index}}" class="disabled-link">Deselect All</a>
                </li>
                <li>
                    <a class="disabled-link">Select All Visible Neighbors</a>
                <li>
                    <a id="hideselection{{index}}" class="disabled-link">Hide Selection</a>
                <li>
                    <a id="unhideselection{{index}}" class="disabled-link">Unhide Selection</a>
                <li>
                    <a class="disabled-link">Remove Selected Nodes</a>
                <li>
                    <a class="disabled-link">Clear Merge Highlights</a>
            </ul>
        </li>
        <li class="dropdown">
            <a class="dropdown-toggle configure" data-toggle="dropdown">Configure<b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a id="settings{{index}}">Settings...</a>
                </li>
                <li>
                    <a>Filters...</a>
                </li>
                <li class="divider"></li>
                <li>
                    <a id="loadonstartup{{index}}">Load On Startup</a>
                </li>
            </ul>
        </li>
        <li class="dropdown">
            <a class="dropdown-toggle tools" data-toggle="dropdown">Tools<b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a id="timeplayer{{index}}">Time Player</a>
                </li>
                <li>
                    <a id="hidelegend{{index}}">Hide Legend</a>
                </li>
                <li>
                    <a id="resetlegend{{index}}">Reset Legend</a>
                </li>
                <li>
                    <a id="appearanceeditor{{index}}" class="disabled-link">Apperance Editor...</a>
                </li>
                <li>
                    <a id="selectneighbor{{index}}" class="disabled-link">Select Neighbors...</a>
                </li>
                <li>
                    <a id="revealneighbor{{index}}" class="disabled-link">Reveal Neighbors...</a>
                </li>
                <li>
                    <a id="computeSNA{{index}}">Compute SNA Metrics</a>
                </li>
                <li>
                    <a id="bundleselection{{index}}" class="disabled-link">Bundle</a>
                </li>
                <li>
                    <a id="unbundleselection{{index}}" class="disabled-link">Unbundle</a>
                </li>

            </ul>
        </li>
        <li class="dropdown">
            <a class="dropdown-toggle layout" data-toggle="dropdown">Layout<b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a id="centrifugeLayout{{index}}">Centrifuge</a>
                </li>
                <li>
                    <a id="circularLayout{{index}}">Circular</a>
                </li>
                <li>
                    <a id="forceDirectedLayout{{index}}">Force Directed</a>
                <li>
                    <a id="hierarchicalLayout{{index}}">Linear Hierarchy</a>
                <li>
                    <a id="radialLayout{{index}}">Radial</a>
                <li>
                    <a id="scrambleLayout{{index}}">Scramble And Place</a>
            </ul>
        </li>
        <li class="dropdown">

            <a id="linkupDropDown{{index}}" class="dropdown-toggle linkup disabled-link" data-toggle="dropdown">Linkup<b
                    class="caret"></b></a>
        </li>
    </ul>
</script>
<!--- Toolbar template -------------------------->
<script id="menu-item-panel" type="text/html">
    <li>
        <a id={{id}}>{{value}}</a>
    </li>
</script>
<!--------- Toolbar template end------------------->
<!-------- Zoom tool panel template ----------------------->
<script id="zoom-tool-panel" type="text/html">
    <div class="toolpanel" id="rg-toolpanel{{index}}">
        <ul>
            <li id="plus-button{{index}}" class="button-press" title="Zoom In">
                <img src="../images/zoomin.png">
            </li>
            <li id="minus-button{{index}}" class="button-press" title="Zoom Out">
                <img src="../images/zoomout.png">
            </li>
            <li id="arrow-out-button{{index}}" class="button-press" title="Fit To Selected">
                <img src="../images/restore.png">
            </li>
            <li id="arrow-in-button{{index}}" class="button-press" title="Fit To Size">
                <img src="../images/focus.png">
            </li>
            <br clear="all"/>
            <li id="cursor-button{{index}}" class="button-press" title="Default">
                <img src="../images/pointer.png">
            </li>
            <li id="select-button{{index}}" class="button-press" title="Select">
                <img src="../images/select.png">
            </li>
            <li id="pan-button{{index}}" class="button-press" title="Pan">
                <img src="../images/move.png">
            </li>
            <li id="zoom-button{{index}}" class="button-press" title="Zoom">
                <img src="../images/search.png">
            </li>
        </ul>
    </div>
</script>
<!-------- Zoom tool panel template ----------------------->


<div class="landingPage">
    <div class="navbar top_nav">
        <div class="navbar-inner">
            <div class="container-fluid">
                <a class="btn btn-navbar" data-toggle="collapse"
                   data-target=".nav-collapse"> <span class="icon-bar"></span> <span
                        class="icon-bar"></span> <span class="icon-bar"></span>
                </a>

                <div class="nav-collapse topbar">
                    <ul class="nav">

                        <li class="dropdown"><a class="dropdown-toggle brand"
                                                data-toggle="dropdown">Centrifuge<b class="caret"></b></a>

                            <ul class="dropdown-menu">
                                <li><a>New Dataview...</a></li>
                                <li><a>New Dataview From Template...</a></li>
                                <li><a>Open Dataview...</a></li>
                                <li><a>Manage Dataviews and Templates...</a></li>
                                <li class="divider"></li>
                                <li><a>Import a Dataview or Template...</a></li>
                                <li><a>Export a Dataview or Template...</a></li>
                                <li class="divider"></li>
                                <li><a>Repository View</a></li>
                                <li><a>System Administration</a></li>
                            </ul>
                        </li>
                        <li class="dropdown analysistype"><a class="dropdown-toggle"
                                                             data-toggle="dropdown" id="dvName"><b
                                class="caret"></b></a>
                            <ul class="dropdown-menu">

                                <li><a>Save As...</a></li>
                                <li><a>Save As Template...</a></li>
                                <li><a>Publish...</a></li>
                                <li class="divider"></li>
                                <li><a>Edit Fields</a></li>
                                <li><a>Edit Data Sources...</a></li>
                                <li class="divider"></li>
                                <li><a>Refresh Data Sources</a></li>
                                <li><a>Close Dataview</a></li>
                                <li><a>Delete Dataview</a></li>
                            </ul>
                        </li>
                    </ul>

                </div>

                <div class="btn-toolbar pull-right nav-top">
                    <div class="btn-group">
                        <button class="btn btn-small"><i class="icon-refresh"></i> Refresh</button>
                    </div>
                    <div class="btn-group">
                        <button class="btn btn-small"><i class="icon-list-alt"></i> Fields</button>
                    </div>
                    <div class="btn-group">
                        <button class="btn btn-small" id="flex-editor">
                            <i class="icon-hdd"></i> Sources
                        </button>
                    </div>

                    <div class="btn-group">
                        <button class="btn btn-small dropdown-toggle" data-toggle="dropdown">
                            <span class="ion-help-circled"></span>
                            <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu">
                            <li><a>Change Password</a></li>
                            <li class="divider"></li>
                            <li><a id="log-out">Logout</a></li>
                        </ul>
                    </div>

                    <div class="btn-group">
                        <button class="btn btn-small dropdown-toggle" data-toggle="dropdown">
                            <i class="icon-info-sign"></i>
                        </button>
                        <ul class="dropdown-menu">
                            <li><a>Getting Started</a></li>
                            <li><a>User Guide</a></li>
                            <li><a>Tutorial</a></li>
                            <li class="divider"></li>
                            <li><a>Intergration Guide</a></li>
                            <li><a>Admin Guide</a></li>
                            <li class="divider"></li>
                            <li><a>About</a></li>
                        </ul>
                    </div>
                </div>
                <!--/.nav-collapse -->
            </div>
        </div>
    </div>

    <div class="toolbar" style="display: none;">
        <a class="btn" id="new_visulization"
           onclick="$('#new-visualization-dialogue').modal();"><i
                class="new_c"></i></a> <a class="btn"><i class="open_c"></i></a> <a
            class="btn"><i class="save_c"></i></a> <a class="btn"><i
            class="web_c"></i></a> <a class="btn"><i class="graph_c"></i></a> <a
            class="btn top_spacer"><i class="last_c"></i></a>
    </div>
    <div class="content  grad_colour_black">
        <ul class="nav nav-tabs navbar-secondlevel layouts-tabs">

            <li class="addLayout"><a class="btn addNewLayout"
                                     data-toggle="modal" href="#new-layout-dialogue">+</a>
            </li>
        </ul>

        <div class="tab-content layouts"></div>
    </div>
</div>
<!------------------------ Delete main panel visualization template ----------------------------------->
<div id="delete_main_panel_visualization" class="modal hide small_modal in delete_visualization">
    <h3 id="myModalLabel" class="box_head">Delete Visualization</h3>
    <div style="padding: 0px 15px;" class="modal-body">
        Are you sure that you want to delete the visualization?
        <div class="modal-footer" style="float: right">
            <a data-dismiss="modal" class="btn btn-inverse">Cancel</a>
            <a data-dismiss="modal" id="btn_delete_main_visualization" class="btn btn-inverse btn-primary "
               onClick="var rv = new RemoveVisualization(); rv.deleteMainViz($(this).attr('vizid'));" id="">Delete
                Visualization</a>
        </div>
    </div>
</div>
<!------------------------ Delete main panel visualization template end ----------------------------------->

<!------------------------ Delete visualization template ----------------------------------->
<div id="delete_visualization" class="modal hide small_modal in delete_visualization">
    <h3 id="myModalLabel" class="box_head">Delete Visualization</h3>
    <div style="padding: 0px 15px;" class="modal-body">
        Are you sure that you want to delete the visualization?
        <div class="modal-footer" style="float: right">
            <a data-dismiss="modal" class="btn btn-inverse">Cancel</a>
            <a data-dismiss="modal" id="btn_delete_visualization" class="btn btn-inverse btn-primary">Delete
                Visualization</a>
        </div>
    </div>
</div>

<!------------------------ Delete visualization template end ----------------------------------->

<!------------------------ Nodes list, linked list tab template ----------------------------------->
<script id="nodes-list-tab-template" type="text/html">
    <div class="box grad_colour_dark_blue relation-tabs" id="box_0"
         style="margin-bottom: 0px; position: absolute; bottom: 0px;z-index: 1000;">
        <div>
            <div id="graph-time-player-control-tab{{index}}" class="time-player-control">
                <div class="inlinerows">
                    <a id="play{{index}}" class="playControl"></a>
                    <a id="stop{{index}}" class="stopControl"></a>
                    <div class="floatLt progressBlock">
                        <div class="center">
                            <a class="icon-calendar from-date" id="from-date{{index}}" data-date-format="yyyy-mm-dd"
                               data-date="2012-02-20"></a>
                            <span class="date-display" id="date-display{{index}}">
(<span class="from-date-display" id="from-date-display{{index}}">2008-06-23</span> -
<span class="to-date-display" id="to-date-display{{index}}">2008-06-23</span>)
</span>
                            <a class="icon-calendar to-date" id="to-date{{index}}" data-date-format="yyyy-mm-dd"
                               data-date="2012-02-20"></a>
                        </div>
                        <div class="progress progress-info playProgress" id="progress{{index}}">
                            <div class="bar" id="progress-bar{{index}}" style="width: 0%;"></div>
                        </div>
                        <span id="status{{index}}" style="float: left;">Stopped</span>
                        <span id="progress-status{{index}}" style="visibility:hidden;"></span>
                    </div>
                </div>
            </div>
        </div>
        <h2 class="box_head round_top">
            <ul id="bottom-tab-headers{{index}}" class="nav nav-tabs bottom-tabs">
                <li id="nodes-list-tab-header{{index}}" class="active">
                    <a href="#nodes-tab{{index}}" data-toggle="tab" class="nodes-list-tab">
                        <p>Nodes</p>
                    </a>
                </li>
                <li id="links-list-tab-header{{index}}">
                    <a href="#links-tab{{index}}" data-toggle="tab" class="links-list-tab">
                        <p>Links</p>
                    </a>
                </li>
                <li id="find-list-tab-header{{index}}">
                    <a href="#find-path-tab{{index}}" data-toggle="tab" class="find-list-tab">
                        <p>Path</p>
                    </a>
                </li>
                <li id="graph-search-tab-header{{index}}">
                    <a href="#graph-search-tab{{index}}" data-toggle="tab" class="graph-search-tab">
                        <p>Search</p>
                    </a>
                </li>
                <li id="time-player-header{{index}}">
                    <a href="#graph-time-player-tab{{index}}" data-toggle="tab" class="graph-time-tab">
                        <p>Time Player</p>
                    </a>
                </li>
            </ul>
        </h2>

        <a id="rel-tab-collapse-link{{index}}" class="toggle_up box_button_2" title="Toggle"
           style="right: 10px; cursor: pointer;" href="#rel-tab-content{{index}}" data-toggle="collapse">&nbsp;</a>
    </div></div>
</script>
<!---------------------------------- Nodes list, linked list tab template end ------------------------------------>

<!----------------------------------- Bottom tabs Nodes table template ----------------------------------------------->
<script type="text/html" id="nodes-table-template">
    <div class="span4 advanced-menu-block" style="display: none;">
        <div class="inlinerows nav">
            <input name="" type="button" class="btn btn-inverse pull-left no-margin" value="Find" id="find">
            <input name="" type="button" class="btn btn-inverse pull-left" value="Clear" id="clear-selection">
            <input name="" type="button" class="btn btn-inverse pull-left advanced-hide-button" value="Hide"
                   id="hide-menu">
        </div>

        <div class="inlinerows">
            <div class="inlinerows">
					<span class="span5">
						<input name="" type="checkbox" value="" id="label">
						&nbsp; Label
					</span>
                <span class="span7">
						<input name="" type="checkbox" value="" id="labelFilterCase" disabled="disabled">
						&nbsp; Case Sensitive
					</span>
            </div>

            <div class="inlinerows">
                <select name="" class="input-large" id="labelFilterName" disabled="disabled">
                    <option value="STARTS_WITH">starts with</option>
                    <option value="ENDS_WITH">ends with</option>
                    <option value="CONTAINS">contains</option>
                    <option value="EQUALS">equals</option>
                    <option value="GT">is greater than</option>
                    <option value="GEQ">is greater than or equal to</option>
                    <option value="LT">is less than</option>
                    <option value="LEQ">is less than or equal to</option>
                    <option value="PATTERN_MATCH">pattern match</option>
                </select>
            </div>

            <div class="inlinerows">
                <input type="text" class="input-large" id="labelFilterVal" disabled="disabled"/>
            </div>
        </div>

        <div class="inlinerows">
            <div class="inlinerows">
					<span class="span5">
						<input name="" type="checkbox" value="" id="type">
						&nbsp; Type
					</span>
                <span class="span7">
						<input name="" type="checkbox" value="" id="typeFilterCase" disabled="disabled">
						&nbsp; Case Sensitive
					</span>
            </div>

            <div class="inlinerows">
                <select name="" class="input-large" id="typeFilterName" disabled="disabled">
                    <option value="STARTS_WITH">starts with</option>
                    <option value="ENDS_WITH">ends with</option>
                    <option value="CONTAINS">contains</option>
                    <option value="EQUALS">equals</option>
                    <option value="GT">is greater than</option>
                    <option value="GEQ">is greater than or equal to</option>
                    <option value="LT">is less than</option>
                    <option value="LEQ">is less than or equal to</option>
                    <option value="PATTERN_MATCH">pattern match</option>
                </select>
            </div>
            <div class="inlinerows">
                <select name="" class="input-large" id="typeFilterVal" disabled="disabled">
                </select>
                <!--input type="text" class="input-large" id="typeFilterVal" disabled="disabled"/ -->
            </div>
        </div>

        <div class="inlinerows">
            <div class="inlinerows">
					<span class="span8">
						<input name="" type="checkbox" value="" id="visibleNeighbors">
						&nbsp; Visible Neighbours </span>
                <span class="span2">
						<input type="number" min="0" max="512" step="1" id="visibleNeighborFilterVal"
                               class="input-medium" style="width:40px;" disabled="disabled"/>
            </div>
            <div class="inlinerows">
                <select name="" class="input-large" id="visibleNeighborsFilterName" disabled="disabled">
                    <option value="EQUALS">equals</option>
                    <option value="GT">is greater than</option>
                    <option value="GEQ">is greater than or equal to</option>
                    <option value="LT">is less than</option>
                    <option value="LEQ">is less than or equal to</option>
                </select>
            </div>
        </div>
    </div>

    <div class="span12 nodes-links-table-wrapper nodes-table-wrapper">
        <!-- put the current table code here.... -->
        <table id="nodes-table{{index}}" cellpadding="0" border="0" class="table table-striped list-tables">

        </table>
    </div>
</script>

<!----------------------------------- Bottom tabs Nodes table template end ----------------------------------------------->

<!----------------------------------- Bottom tabs Links table template ----------------------------------------------->
<script type="text/html" id="links-table-template">
    <div class="span4 advanced-menu-block" style="display: none;">
        <div class="inlinerows nav">
            <input name="" type="button" class="btn btn-inverse pull-left no-margin" value="Find" id="find-link">
            <input name="" type="button" class="btn btn-inverse pull-left" value="Clear" id="clear-link-filter">
            <input name="" type="button" class="btn btn-inverse pull-left advanced-hide-button" value="Hide"
                   id="hide-link-advance-menu">
        </div>
        <div class="inlinerows">
            <div class="inlinerows">
					<span class="span5">
						<input name="" type="checkbox" value="" id="source">
						&nbsp; Source </span>
                <span class="span7">
						<input name="" type="checkbox" value="" id="sourceCase" disabled="disabled">
						&nbsp; Case Sensitive </span>
            </div>
            <div class="inlinerows">
                <select name="" class="input-large" id="sourceFilterName" disabled="disabled">
                    <option value="STARTS_WITH">starts with</option>
                    <option value="ENDS_WITH">ends with</option>
                    <option value="CONTAINS">contains</option>
                    <option value="EQUALS">equals</option>
                    <option value="GT">is greater than</option>
                    <option value="GEQ">is greater than or equal to</option>
                    <option value="LT">is less than</option>
                    <option value="LEQ">is less than or equal to</option>
                    <option value="PATTERN_MATCH">pattern match</option>
                </select>
            </div>
            <div class="inlinerows">
                <input type="text" class="input-large" id="sourceFilterVal" disabled="disabled">
            </div>
        </div>
        <div class="inlinerows">
            <div class="inlinerows">
					<span class="span5">
						<input name="" type="checkbox" value="" id="target">
						&nbsp; Target </span>
                <span class="span7">
						<input name="" type="checkbox" value="" id="targetCase" disabled="disabled">
						&nbsp; Case Sensitive </span>
            </div>
            <div class="inlinerows">
                <select name="" class="input-large" id="targetFilterName" disabled="disabled">
                    <option value="STARTS_WITH">starts with</option>
                    <option value="ENDS_WITH">ends with</option>
                    <option value="CONTAINS">contains</option>
                    <option value="EQUALS">equals</option>
                    <option value="GT">is greater than</option>
                    <option value="GEQ">is greater than or equal to</option>
                    <option value="LT">is less than</option>
                    <option value="LEQ">is less than or equal to</option>
                    <option value="PATTERN_MATCH">pattern match</option>
                </select>
            </div>
            <div class="inlinerows">
                <input type="text" class="input-large" id="targetFilterVal" disabled="disabled">
            </div>
        </div>
    </div>

    <div class="span12 nodes-links-table-wrapper links-table-wrapper">
        <!-- put the current table code here.... -->
        <table id="links-table{{index}}" cellpadding="0" border="0" class="table table-striped list-tables">

        </table>
    </div>
</script>

<!----------------------------------- Bottom tabs Links table template end ----------------------------------------------->

<!----------------------------------------- Search filter template ------------------------------------------->
<script type="text/html" id="rg-table-find">
    <div class="search-div">
        <div class="pull-left search-left">
            <label class="pull-left">
                <strong>Find&nbsp;</strong>
            </label>
            <div class="input-append pull-left">
                <input type="text" placeholder="Type here" class="pull-left search-bar">
                <span class="add-on pull-left search-icon-span">
						<i class="icon-search"></i>
					</span>
            </div>
            <input type="button" value="Clear" class="btn btn-inverse pull-left search-clear">&nbsp;
            <input type="button" value="Advanced" class="btn btn-inverse pull-left advanced-search-button">
        </div>
        <div class="pull-right search-right">
            <label class="checkbox pull-left case-sort">
                <input type="checkbox" value="">
                Case Sort
            </label>&nbsp;
            <input type="button" id="unbundle{{index}}" value="Unbundle" class="btn btn-inverse pull-left"
                   disabled="disabled"
                   onClick="var ubs = new UnBundleSelection(); ubs.getRgView('{{viz}}'); ubs.doTask(); $(document).find('[value^=Unbundle]').attr('disabled', 'disabled');">
        </div>
    </div>
    <div class="inlinerows">
        <ul class="breadcrumb" id="toolbar-{{label}}-{{index}}"></ul>
    </div>
</script>
<!------------------------------------------ Search filter template ----------------------------------------->

<!------------------------------------------ Find Path tab template ----------------------------------------->
<script type="text/html" id="find-path-tab-template">
    <div class="inner-find-tab-wrapper">
        <ul class="nav nav-tabs bottom-tabs" id="find-path-tab-header{{index}}">
            <li class="active">
                <a href="#find-find-tab{{index}}" data-toggle="tab">
                    <p>Find</p>
                </a>
            </li>
            <li>
                <a href="#find-results-tab{{index}}" data-toggle="tab" class="find-results-tab">
                    <p>Results</p>
                </a>
            </li>
        </ul>

        <div class="tab-content inner-find-tab">
            <div id="find-find-tab{{index}}" class="tab-pane active">
                <div class="node_wrapper">
                    <div class="clear">
                        <span class="label_options">Start Node *</span>
                        <span class="chosen_node" id="startnode{{index}}">No start node chosen</span>
                        <input type="hidden" id="startnodeid{{index}}"></input>
                        <a class="choose_link" id="start-node-choose-link{{index}}">Choose...</a>
                    </div>

                    <div class="clear">
                        <span class="label_options">End Node *</span>
                        <span class="chosen_node" id="endnode{{index}}">No end node chosen</span>
                        <input type="hidden" id="endnodeid{{index}}"></input>
                        <a class="choose_link" id="end-node-choose-link{{index}}">Choose...</a>
                    </div>

                    <div class="clear">
                        <a id="find-path-show-advanced{{index}}" class="show-find-path-advanced">Show Advanced
                            Options</a>
                    </div>

                    <div id="find-path-advanced{{index}}" class="advanced_options_wrapper clear hide">
                        <div class="clear">
                            <span class="label_options">Maximum Paths *</span>
                            <input type="text" id="no_of_paths{{index}}" class="span3" value="1" required>
                        </div>

                        <div class="clear">
                            <span class="label_options"></span>
                            <input type="checkbox" class="input_checkbox find_path_lengths_checkbox">
                            <span class="length">Minimum Length</span>
                            <input type="number" id="min_length{{index}}" class="number_input" min="1" max="50"
                                   value="1" disabled/>
                        </div>
                        <div class="clear">
                            <span class="label_options"></span>
                            <input type="checkbox" class="input_checkbox find_path_lengths_checkbox">
                            <span class="length">Maximum Length</span>
                            <input type="number" id="max_length{{index}}" class="number_input" min="1" max="50"
                                   value="50" disabled/>
                        </div>
                        <div class="clear">
                            <span class="label_options"></span>
                            <input type="checkbox" class="input_checkbox find_path_lengths_checkbox"
                                   id="include-direction{{index}}" hidden>
                            <span class="length" id="direction-label{{index}}" hidden>Include Direction</span>
                        </div>
                    </div>
                    <div class="pull-right clear">
                        <input id="find-path-button{{index}}" type="button" class="btn btn-inverse find-path-button"
                               value="Find Paths">
                    </div>
                </div>
                <div class="node-switch-wrapper">
                    <img src="../images/up-down.png" title="Use Current Selection For Start And End Nodes"/>
                    <img id="switch-node{{index}}" src="../images/switch-nodes.png" title="Switch Start And End Nodes"/>
                </div>
            </div>

            <div id="find-results-tab{{index}}" class="tab-pane padding-small">
                <table id="result-find-data-table{{index}}" class="table table-striped list-tables" cellpadding="0"
                       border="0" style="width: 100%;">
                    <thead>
                    <tr>
                        <td>Name</td>
                        <td>Length</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td colspan="2" style="text-align: center">No Data Available</td>
                    </tr>
                    </tbody>
                </table>
                <div class="clear pull-right padding-small">
                    <input type="button" id="selectpath{{index}}" class="btn btn-inverse pull-left" value="Select Path"
                           disabled/>
                    <input type="button" id="addpathtoselection{{index}}" class="btn btn-inverse pull-left"
                           value="Add Paths to Selection" disabled/>
                </div>
            </div>
        </div>
    </div>
</script>
<!--------------------------------------------------- Find Path tab template ------------------------------------------------->

<!-------------------------------------------------- Graph Search Path template ---------------------------------------------->
<script id="graph-search-tab-template" type="text/html">
    <div class="span4">
        <div class="box grad_colour_dark_blue find-path-tab rg-tabs" style="display: block; float: left;">
            <div class="inner-find-tab-wrapper light-border darkenedBg pull-left">
                <div class="inlinerows">
                    <strong>Specify Search Criteria</strong>
                </div>
                <div class="inlinerows graph-search-content-wrapper">
                    <div class="inlineRows">
                        <div class="span2">
                            Find:
                        </div>

                        <div class="span4">
                            <div class="inlineRows">
                                <input name="first_options" type="radio" value="nodes"
                                       class="firstOption{{index}} floatLt" checked="checked">
                                &nbsp;Nodes
                                <br clear="all"/>
                                <input name="first_options" type="radio" value="links"
                                       class="firstOption{{index}} floatLt graphSearchLinkRadio">
                                &nbsp;Links
                            </div>
                        </div>

                        <div class="span2">
                            &nbsp;&nbsp;&nbsp;In:
                        </div>
                        <div class="span4">
                            <div class="inlineRows">
                                <input name="second_option" type="radio" value="false"
                                       class="secondOption{{index}} floatLt" checked="checked">
                                &nbsp;Data
                                <br clear="all"/>

                                <input name="second_option" type="radio" value="true"
                                       class="secondOption{{index}} floatLt">
                                &nbsp;Graph
                            </div>
                        </div>
                    </div>

                    <div class="lightBgBox search-graph-tree">

                    </div>
                </div>

                <div class="pull-right">
                    <input type="button" class="btn btn-inverse" value="Search" id="search{{index}}"/>
                    <input type="button" class="btn btn-inverse" value="Reset" id="reset{{index}}"/>
                </div>
            </div>
        </div>
    </div>

    <div class="span8 floatRt" style="margin-left: 2% !important;">
        <div class="box grad_colour_dark_blue find-path-tab rg-tabs">

            <div class="inner-find-tab-wrapper light-border darkenedBg graph-search-results-div">
                <div class="inlinerows">
                    <strong class="floatLt">Results</strong>
                    <strong class="floatRt">
                        <div class="btn-group">
                            <button id="graphSearchActions{{index}}" class="btn btn-mini">Actions</button>
                            <button class="btn btn-mini dropdown-toggle" data-toggle="dropdown">
                                <span class="caret"></span>
                            </button>
                            <ul id="searchOptions{{index}}" class="dropdown-menu" style="left: -92px;">
                                <li style="display:none;">
                                    <a id="searchGraphSelectAll{{index}}" class="disabled-link">Select All</a>
                                </li>
                                <li style="display:none;">
                                    <a id="searchGraphSelectChoosen{{index}}" class="disabled-link">Select Chosen</a>
                                </li>
                                <li>
                                    <a id="searchGraphAddAll{{index}}" class="disabled-link">Add All</a>
                                </li>
                                <li>
                                    <a id="searchGraphAddChoosen{{index}}" class="disabled-link">Add Choosen</a>
                                </li>
                            </ul>
                        </div>
                    </strong>
                </div>
                <div class="inlinerows  graph-search-content-wrapper">
                    <div class="lightBgBox search-graph-results-table">
                        <table id="graph-search-result-find-data-table{{index}}" class="table table-striped list-tables"
                               cellpadding="0" border="0">
                            <thead>
                            <tr>
                                <td>Label</td>
                                <td>Type</td>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td colspan="2" style="text-align: center;">No data available</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <br clear="all"/>
            </div>
        </div>

    </div>

</script>
<!-------------------------------------------------- Graph Search Path template end ---------------------------------------------->

<!--------------------------------------- Dynatree row template ---------------->
<script type="text/html" id="dynatree-row-template">
    <span class="dynatree-node-title-div">
			<span class="tree-title">
				<a class="dynatree-title">{{columnTitle}}</a>
			</span>
			<span class="tree-column-edit">
				<img src="../images/node-edit.png" / uuid="{{uuid}}">
			</span>
		</span>
</script>
<!--------------------------------------- Dynatree row template end ---------------->

<!--------------------------------------- Dynatree link row template ---------------->
<script type="text/html" id="dynatree-link-row-template">
    <span class="dynatree-link-title-div">
			<span class="tree-title">
				<a class="dynatree-title">{{fromLink}}</a>
			</span>
			<span class="link-icon">
				<img src="../images/link-icon.png"
			</span>
			<span class="tree-column-edit">
				<a class="dynatree-title">{{toLink}}</a>
			</span>
		</span>
</script>
<!--------------------------------------- Dynatree link row template end ---------------->

<!--------------------------------------- Graph search edit template ----------------->
<script type="text/html" id="graph-search-edit-column-template">
    <div id="graph-search-edit-modal-{{mainColumnName}}-{{columnName}}" class="modal hide">

    </div>
</script>
<!--------------------------------------- Graph search edit template end ------------->

<!------------------ Bundle dialogue template ----------------->
<script type="text/html" id="bundle-dialogue-template">
    <div id="bundle-dialogue{{index}}" class="modal hide small_modal bundle-dialogue">
        <h3 class="box_head" id="myModalLabel">Bundle</h3>
        <div class="modal-body">
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" disabled
                       id="bundleGraphBySpec{{index}}"/>
                <span class="bundle-label">Bundle Entire Graph By Spec</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" disabled
                       id="bundleSelectBySpec{{index}}"/>
                <span class="bundle-label">Bundle Selection By Spec</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" id="bundleManually{{index}}"/>
                <span class="bundle-label">Manual Bundle By Selection</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-name">
                <input id="bundlename{{index}}" type="text" placeholder="Untitled bundle" value="Untitled bundle"/>
            </div>

            <div class="pull-right">
                <input type="button" name="bundleOK{{index}}" value="OK" class="btn btn-inverse"
                       onclick="var bs = new BundleHandler(); bs.getRgView('{{viz}}'); bs.doTask('{{index}}');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#bundle-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Bundle dialogue template end ------------->
<!------------------ Small panel bundle dialogue template ----------------->
<script type="text/html" id="small-panel-bundle-dialogue-template">
    <div id="bundle-dialogue{{index}}" class="modal hide small_modal bundle-dialogue">
        <h3 class="box_head" id="myModalLabel">Bundle</h3>
        <div class="modal-body">
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" disabled
                       id="bundleGraphBySpec{{index}}"/>
                <span class="bundle-label">Bundle Entire Graph By Spec</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" disabled
                       id="bundleSelectBySpec{{index}}"/>
                <span class="bundle-label">Bundle Selection By Spec</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="bundleType{{index}}" id="bundleManually{{index}}"/>
                <span class="bundle-label">Manual Bundle By Selection</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-name">
                <input id="bundlename{{index}}" type="text" placeholder="Untitled bundle" value="Untitled bundle"/>
            </div>

            <div class="pull-right">
                <input type="button" name="bundleOK{{index}}" value="Ok" class="btn btn-inverse"
                       onclick="var bs = new BundleHandler('{{index}}'); bs.doTask('{{index}}');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#bundle-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Small panel bundle dialogue template end ------------->

<!------------------ Unbundle dialogue template ----------------->
<script type="text/html" id="unbundle-dialogue-template">
    <div id="unbundle-dialogue{{index}}" class="modal hide small_modal bundle-dialogue">
        <h3 class="box_head" id="myModalLabel">Unbundle</h3>
        <div class="modal-body">
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="unbundleType{{index}}" disabled
                       id="unbundleEntire{{index}}"/>
                <span class="bundle-label">Unbundle Entire Graph</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="unbundleType{{index}}" disabled
                       id="unbundleSelected{{index}}"/>
                <span class="bundle-label">Unbundle Selected Nodes</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="pull-right">
                <input type="button" name="unbundleOK{{index}}" value="OK" class="btn btn-inverse"
                       onclick="var bs = new UnBundleHandler(); bs.getRgView('{{viz}}'); bs.doTask('{{index}}');$('#unbundle-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#unbundle-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Unbundle dialogue template end ------------->
<!------------------ Small panel unbundle dialogue template ----------------->
<script type="text/html" id="small-panel-unbundle-dialogue-template">
    <div id="unbundle-dialogue{{index}}" class="modal hide small_modal bundle-dialogue">
        <h3 class="box_head" id="myModalLabel">Unbundle</h3>
        <div class="modal-body">
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="unbundleType{{index}}" disabled
                       id="unbundleEntire{{index}}"/>
                <span class="bundle-label">Unbundle Entire Graph</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="bundle-field">
                <input class="bundle-radio" type="radio" name="unbundleType{{index}}" disabled
                       id="unbundleSelected{{index}}"/>
                <span class="bundle-label">Unbundle Selected Nodes</span>
                <img class="bundle-right-image" src="../images/help.png">
            </div>
            <div class="pull-right">
                <input type="button" name="unbundleOK{{index}}" value="Ok" class="btn btn-inverse"
                       onclick="var bs = new UnBundleHandler();bs.getRgView('{{index}}');  bs.doTask('{{index}}'); $('#unbundle-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#unbundle-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Small panel unbundle dialogue template end ------------->

<!------------------ Appearance editor dialogue template ----------------->
<script type="text/html" id="app-editor-dialogue-template">
    <div id="appearance-editor-dialogue{{index}}" class="modal hide small_modal">
        <h2 class="box_head" id="myModalLabel">Appearance Editor</h2>
        <div class="modal-body">
            <p class="middle-text">Edit appearance of selected items.</p>
            <div class="editor-field">
                <span class="editor-label">Size:</span>
                <select class="span1" id="select-size{{index}}" value="1px">
                    {{#selectItems}}
                    <option value="{{value}}">{{text}}</option>
                    {{/selectItems}}
                </select>
            </div>
            <div class="editor-field">
                <span class="editor-label">Color</span>
                <div id="customWidget">
                    <div id="colorSelector{{index}}" class="color-selector">
                        <div style="background-color: #fefefe" id="select-color{{index}}"></div>
                    </div>
                    <div id="colorpickerHolder{{index}}" class="colorpicker-holder colorpicker-middle-align"></div>
                </div>
            </div>
            <div class="pull-right padding-small-top">
                <input type="button" value="Update" class="btn btn-inverse"
                       onClick="var ae = new AppearanceEditor(); ae.getRgView('{{viz}}'); ae.doTask();$('#appearance-editor-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Reset" class="btn btn-inverse"
                       onClick="$('#select-color{{index}}').css({'background-color': '#fefefe'});  $('#select-size{{index}}').val('');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#appearance-editor-dialogue{{index}}').modal('hide')"/>
            </div>
            <input type="hidden" id="appearance-color-int{{index}}" value="16711422"/>
        </div>
    </div>
</script>
<!------------------ Appearance editor dialogue template end ------------->

<!------------------ Reveal neighbors dialogue template ----------------->
<script type="text/html" id="reveal-neighbor-dialogue-template">
    <div id="reveal-neighbor-dialogue{{index}}" class="modal hide small_editor_modal">
        <h2 class="box_head" id="myModalLabel">Reveal Neighbors</h2>
        <div class="modal-body">
            <div class="bundle-field">
                <span class="neigh-label">Reveal hidden nodes and links</span>
                <select id="reveal-nsteps{{index}}" class="span1">
                    {{#selectItems}}
                    <option value="{{value}}">{{text}}</option>
                    {{/selectItems}}
                </select>
            </div>
            <div class="bundle-field">
                degree(s) away from the selected nodes
            </div>


            <div class="pull-right">
                <input type="button" value="OK" class="btn btn-inverse"
                       onClick="var rn = new RevealNeighbours(); rn.getRgView('{{viz}}'); rn.doTask($('#reveal-nsteps'+'{{viz}}').val());$('#reveal-neighbor-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#reveal-neighbor-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Reveal neighbors dialogue template end ------------->

<!------------------ Small Panel reveal neighbors dialogue template ----------------->
<script type="text/html" id="small-panel-reveal-neighbor-dialogue-template">
    <div id="reveal-neighbor-dialogue{{index}}" class="modal hide small_editor_modal">
        <h2 class="box_head" id="myModalLabel">Reveal Neighbors</h2>
        <div class="modal-body">
            <div class="bundle-field">
                <span class="neigh-label">Reveal hidden nodes and links</span>
                <select id="reveal-nsteps{{index}}" class="span1">
                    {{#selectItems}}
                    <option value="{{value}}">{{text}}</option>
                    {{/selectItems}}
                </select>
            </div>
            <div class="bundle-field">
                degree(s) away from the selected nodes
            </div>


            <div class="pull-right">
                <input type="button" value="Ok" class="btn btn-inverse"
                       onClick="var rn = new RevealNeighbours(); rn.getRgView('{{index}}'); rn.doTask();$('#reveal-neighbor-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#reveal-neighbor-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Small panel reveal neighbors dialogue template end ------------->

<!------------------- Rename worksheet template ---------------------->
<div id="renameWorksheetDialogue"
     class="modal hide small_editor_modal">
    <h2 class="box_head" id="myModalLabel">Rename Worksheet</h2>

    <div class="modal-body">
        <div class="bundle-field">
            <span class="neigh-label">New Worksheet Name</span><br> <input
                type="text" id="renameWorksheetName" required></input>
        </div>
        <div class="pull-right">
            <input type="button" id="renameWorksheetButton" value="Rename"
                   class="btn btn-inverse" data-dismiss="modal"/> <input type="button" value="Cancel"
                                                                         class="btn btn-inverse" data-dismiss="modal"/>
            <input type="hidden" value="" id="rLayoutId">
            <input type="hidden" value="" id="rWid">
        </div>
    </div>
</div>
<!------------------- Rename worksheet template end ---------------------->

<!------------------- Delete worksheet template ------------------------------->
<div id="deleteWorksheetDialogue"
     class="modal hide small_editor_modal">
    <h2 class="box_head" id="myModalLabel">Delete Worksheet</h2>

    <div class="modal-body">
        <div class="bundle-field">
				<span class="neigh-label">This will permanently delete the
					worksheet "<span class='delWorksheetName'></span>" and all its
					visualizations. Are you sure you want to delete "<span
                            class='delWorksheetName'></span>"?
				</span><br>
        </div>
        <div class="pull-right">
            <input type="button" value="Delete" class="btn btn-inverse"
                   id="deleteWorksheetButton" data-dismiss="modal"/> <input type="button" data-dismiss="modal"
                                                                            value="Cancel"
                                                                            class="btn btn-inverse"/>
        </div>
    </div>
</div>
<!------------------- Delete worksheet template end -------------------------->
<!------------------  Select neighborsdialogue template ----------------->
<script type="text/html" id="select-neighbor-dialogue-template">
    <div id="select-neighbor-dialogue{{index}}" class="modal hide small_editor_modal">
        <h2 class="box_head" id="myModalLabel">Select Neighbors</h2>
        <div class="modal-body">
            <div class="bundle-field">
                <span class="neigh-label">Select visible nodes</span>
                <select id="select-neighbor{{index}}" class="span1">
                    {{#selectItems}}
                    <option value="{{value}}">{{text}}</option>
                    {{/selectItems}}
                </select>
            </div>
            <div class="bundle-field">
                degree(s) away from the selected nodes
            </div>


            <div class="pull-right">
                <input type="button" value="Ok" class="btn btn-inverse"
                       onClick="var sn = new SelectNeighbors(); sn.getRgView('{{viz}}'); sn.doTask($('#select-neighbor'+ '{{viz}}').val());$('#select-neighbor-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#select-neighbor-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Select neighbors dialogue template end ------------->
<!------------------  Small panel select neighbors dialogue template ----------------->
<script type="text/html" id="small-panel-select-neighbor-dialogue-template">
    <div id="select-neighbor-dialogue{{index}}" class="modal hide small_editor_modal">
        <h2 class="box_head" id="myModalLabel">Select Neighbors</h2>
        <div class="modal-body">
            <div class="bundle-field">
                <span class="neigh-label">Select visible nodes</span>
                <select id="select-neighbor{{index}}" class="span1">
                    {{#selectItems}}
                    <option value="{{value}}">{{text}}</option>
                    {{/selectItems}}
                </select>
            </div>
            <div class="bundle-field">
                degree(s) away from the selected nodes
            </div>


            <div class="pull-right">
                <input type="button" value="Ok" class="btn btn-inverse"
                       onClick="var sn = new SelectNeighbors(); sn.getRgView('{{index}}'); sn.doTask($('#select-neighbor'+ '{{index}}').val());;$('#select-neighbor-dialogue{{index}}').modal('hide');"/>
                <input type="button" value="Cancel" class="btn btn-inverse"
                       onclick="$('#select-neighbor-dialogue{{index}}').modal('hide')"/>
            </div>
        </div>
    </div>
</script>
<!------------------ Small panel select neighbors dialogue template end ------------->


<!------------------- Legend Panel summary template ---------------------->
<script type="text/html" id="legendPanelSummaryTemplate">
    <span class="legendNumberItem">
			<span class="spacer"></span>
			<a onclick="selectNodesByType.doTask(event);return false;" style="float: left;">Total Nodes</a>
			<span class="legendNumber">( 312 of 312)</span>
		</span>
    <span class="legendNumberItem">
			<span class="spacer"><hr class="linkLine"/></span>
			<a onclick="selectNodesByType.doTask(event);return false;" style="float: left;">Link</a>
			<span class="legendNumber">( 312 of 312)</span>
		</span>
</script>

<!------------------- Legend Panel summary template end ------------------>

<!------------------- RG Settings modal template ---------------------------------->
<div id="alertModal" class="modal hide node_modal alertModal">
    <h3 class="box_head" id="myModalLabel">Relationship Graph</h3>

    <div class="modal-body">
        <div class="inlinerows">
            <span class="span1">Title</span> <input
                class="input-xlarge title_field" type="text"
                id="rgName" placeholder="Relationship Graph">
            <label class="checkbox floatRt"> <input type="checkbox" id="dontLoadAftrSave">
                Don't Load After Save&nbsp;&nbsp;&nbsp;
            </label>
            <label class="checkbox floatRt"> <input type="checkbox" id="loadOnStartUp" checked="checked">
                Load On Startup&nbsp;&nbsp;&nbsp;
            </label>
        </div>
        <div class="inlinerows">
            <div class="floatLt">
                <span class="span1">Theme</span> <select class="span2" id="theme">
                <option value="NoTypes">NoTypes</option>
                <option value="Circular">Circular</option>
                <option value="Baseline">Baseline</option>
            </select>
            </div>
            <div class="floatLt">
                <span class="floatLt">&nbsp;&nbsp;&nbsp;Render Threshold</span><span
                    class="floatLt"> <input type="number" id="rndrThreshold" class="number_input span2" min="1"
                                            max="10000" value="2000"/>
				</span>
            </div>
            <div class="floatRt" style="float: left; margin-left: 10px;">
                <span class=" floatLt">Background</span>
                <span id="settings-color-picker-main-div">
						<span class="simplecolorpicker icon"
                              style="width: 20px; height: 20px; border: 1px solid black; float: left;"></span>
						<div class="color-picker-picker-wrapper"
                             style="z-index: 999999; width: 266px; background: none repeat scroll 0% 0% rgb(255, 255, 255); border: 1px solid rgb(232, 232, 232); position: absolute; top: 120px; left: 506px; height: 174px; display: none;">
							<div class="accordion-inner" style="height: 152px; border-top: none;">
								<div class="color-picker">
									<select id="rg-settings-color-picker">
										<option value="#660000">#660000</option>
										<option value="#990000">#990000</option>
										<option value="#CC0000">#CC0000</option>
										<option value="#CC3333">#CC3333</option>
										<option value="#EA4C88">#EA4C88</option>
										<option value="#D10553">#D10553</option>
										<option value="#823CC8">#823CC8</option>
										<option value="#663399">#663399</option>
										<option value="#333999">#333999</option>
										<option value="#0066CC">#0066CC</option>
										<option value="#0099CC">#0099CC</option>
										<option value="#7AD9F9">#7AD9F9</option>
										<option value="#66CCCC">#66CCCC</option>
										<option value="#74E618">#74E618</option>
										<option value="#77CC33">#77CC33</option>
										<option value="#336600">#336600</option>
										<option value="#666600">#666600</option>
										<option value="#999900">#999900</option>
										<option value="#CCCC33">#CCCC33</option>
										<option value="#EAEA26">#EAEA26</option>
										<option value="#FFFF00">#FFFF00</option>
										<option value="#FFCC33">#FFCC33</option>
										<option value="#FF9900">#FF9900</option>
										<option value="#CE7C00">#CE7C00</option>
										<option value="#FF6600">#FF6600</option>
										<option value="#CC6633">#CC6633</option>
										<option value="#996633">#996633</option>
										<option value="#AA6117">#AA6117</option>
										<option value="#663300">#663300</option>
										<option value="#000000">#000000</option>
										<option value="#999999">#999999</option>
										<option value="#CCCCCC">#CCCCCC</option>
										<option value="#6393AA">#6393AA</option>
									</select>
								</div>

								<div class="rgb-select">
									<div class="color-preview"></div>
									<div class="color-component red-component">
										R&nbsp;
										<input type="text" class="span1 no-margin-input">
									</div>
									<div class="color-component green-component">
										G&nbsp;
										<input type="text" class="span1 no-margin-input">
									</div>
									<div class="color-component blue-component">
										B&nbsp;
										<input type="text" class="span1 no-margin-input">
									</div>
								</div>
							</div>
						</div>
					</span>
            </div>
        </div>

        <div class="node-container">
            <div class="left_container"></div>

            <div class="node-diagram" id="node_diagram"></div>
        </div>
    </div>

    <div class="modal-footer">
        <div style="float: right">
            <button id="treeFinish-rg" class="btn btn-inverse">Save</button>
            <button class="btn btn-inverse" data-dismiss="modal" onclick="$('#treeFinish-rg').removeAttr('disabled');"
                    class="close">Cancel
            </button>
        </div>
    </div>
</div>
<<<<<<< HEAD


<!-- --------------------------------------- GeoMap Chart settings ----------------------------------------->
<div id="GeoMapChartModal" class="modal hide wide_modal" style="height:75%;overflow: auto;">
    <h3 class="box_head" id="myModalLabel">Geo Map</h3>
    <div class="modal-body">
        <div class="inLinerows">
            <span class="span2">Title</span>
            <input id="geoMapChartName" class="input-xlarge title_field" type="text" placeholder="GeoMap Chart Name">
            <label class="checkbox floatRt"> <input type="checkbox" id="dontLoadAftrSave">
                Dont Load after Save
            </label>
            <label class="checkbox floatRt"> <input type="checkbox" id="loadOnStartUp" checked="checked">
                Load on Startup&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </label>
        </div>
        <div class="inlinerows">
            <span class="span6">&nbsp;</span>
        </div>
        <div class="container-fluid">
            <form class="form-horizontal">
                <div class="left_container span2 floatLt" id="fields_list"></div>
                <div class="right_container span3">
                    <div class="row-fluid">
                        <div class="control-group">
                            <label id="geolabel" style="margin-left:20px" class="control-label geolabel" for="geomaps">Map</label>
                            <div class="controls" style="margin-left:0px">
                                <select id="geomaps" class="geocontrol">
                                    <option selected="selected" value="USA">USA</option>
                                    <option value="World">World</option>
                                    <option value="China">China</option>
                                    <option value="Germany">Germany</option>
                                    <option value="Hungary">Hungary</option>
                                    <option value="India">India</option>
                                    <option value="Ireland">Ireland</option>
                                    <option value="Japan">Japan</option>
                                    <option value="New Zealand">New Zealand</option>
                                    <option value="Turkey">Turkey</option>
                                    <option value="UK">UK</option>
                                    <option value="UK Regions">UK Regions</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="container-fluid">
                        <div class="accordion" id="heatmapAccordion">
                            <div class="accordion-group" style="width:250px">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle geolabel" data-toggle="collapse"
                                       data-parent="heatmapAccordion" href="#heatmapParms">
                                        Heat Map Settings</a>
                                </div>
                                <div id="heatmapParms" class="accordion-body collapse">
                                    <div class="accordion-inner">
                                        <div class="row-fluid">
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel" for="heatmapState">State</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <input id="heatmapState" type="text" class="geocontrol"
                                                           placeholder="State">
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel"
                                                       for="heatmapMeasure">Measure</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <input id="heatmapMeasure" type="text" class="geocontrol"
                                                           placeholder="Measure">
                                                </div>
                                            </div>
                                            <div class="inlinerows">
                                                <label id="geolabel" class="control-label geolabel"
                                                       for="heatmapFunction">Function</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <select id="heatmapFunction" class="geocontrol">
                                                        <option selected="selected" value="Count">Count</option>
                                                        <option value="Count Distinct">Count Distinct</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="accordion" id="bubblemapAccordion">
                            <div class="accordion-group" style="width:250px">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle" data-toggle="collapse" data-parent="bubblemapAccordion"
                                       href="#bubblemapParms">
                                        Bubble Map Settings</a>
                                </div>
                                <div id="bubblemapParms" class="accordion-body collapse">
                                    <div class="accordion-inner">
                                        <div class="row-fluid">
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel"
                                                       for="bubblemapField">Bubble</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <input id="bubblemapField" class="geocontrol" type="text"
                                                           placeholder="Bubble Field"/>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="row-fluid">
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel"
                                                       style="text-align:left" for="bubblemapFunction">Function</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <select id="bubblemapFunction" class="geocontrol">
                                                        <option selected="selected" value="Count">Count</option>
                                                        <option value="Count Distinct">Count Distinct</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="accordion" id="plotmapAccordion">
                            <div class="accordion-group" style="width:250px">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle" data-toggle="collapse" data-parent="plotmapAccordion"
                                       href="#plotmapParms">
                                        Plot Map Settings</a>
                                </div>
                                <div id="plotmapParms" class="accordion-body collapse">
                                    <div class="accordion-inner">
                                        <div class="row-fluid">
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel"
                                                       for="plotmapLatitude">Latitude</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <input id="plotmapLatitude" class="geocontrol" type="text"
                                                           placeholder="Latitude"/>
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel"
                                                       for="plotmapLongitude">Longitude</label>
                                                <div class="controls" style="margin-left:0px">
                                                    <input id="plotmapLongitude" class="geocontrol" type="text"
                                                           placeholder="Longitude"/>
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <label id="geolabel" class="control-label geolabel" for="plotmapIcons">Icon</label>
                                                <select id="plotmapIcons" class="geocontrol">
                                                    <option selected="selected" value="Icon1">Icon1</option>
                                                    <option value="Icon2">Icon2</option>
                                                    <option value="Icon3">Icon3</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="control-group">
                                            <label id="geolabel" class="control-label geolabel" for="tooltipField">Tooltip
                                                Fields</label>
                                            <div class="controls" style="margin-left:0px">
                                                <div id="tooltipField" class="geocontrol geolistbox"></div>
                                                <button id="clearTooltips"
                                                        onclick="new CreateGeoMapSettings().tooltipsClear();"
                                                        type="button">Clear
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="inlinerows">
                            <label class="checkbox floatLt"> <input type="checkbox" id="suppressNulls">Suppress
                                Nulls</label>
                        </div>
                        <div class="inlinerows" style="width:250px">
                            <label class="floatLt" style="width:115px">Render Threshold</label>
                            <input type="text" id="threshold" style="margin-left:0px;width:100px">
                        </div>
                    </div>
                </div>
        </div>
        </form>
    </div>
    <div class="modal-footer">
        <div style="float: right">
            <button id="treeFinish-rg" class="btn btn-inverse"
                    onclick="$('#GeoMapChartModal').modal('hide');new CreateGeoMapSettings().saveSettings();">Save
            </button>
            <button class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</button>
        </div>
    </div>
</div>
</div>
<!-- --------------------------------------- End GeoMap Chart settings ------------------------------------>


<div id="DrillChartModal" class="modal hide wide_modal drillChartModal" style="height:85%;overflow: auto;">

    =======
    <!------------------- RG Settings modal template end ---------------------------------->

    <!------------------- Chart Settings modal template ---------------------------------->
    <div id="DrillChartModal" class="modal hide wide_modal drillChartModal" style="height:85%;overflow: auto;">
        >>>>>>> feature/sprint-14
        <h3 class="box_head" id="myModalLabel">Drill Chart</h3>

        <div class="modal-body">
            <div class="inlinerows">
                <span class="span2">Title</span>
                <input id="drillChartName" class="input-xlarge title_field" type="text" placeholder="Drill Chart">
            </div>
            <div class="inlinerows">
                <span class="span2">&nbsp;</span>
                <label class="checkbox floatLt">
                    <input type="checkbox" id="loadOnStartUp">
                    Load on Startup </label>
            </div>
            <div class="inlinerows">
                <span class="span2">&nbsp;</span>
                <label class="checkbox floatLt">
                    <input type="checkbox">
                    Don't Load After Save </label>
            </div>
            <div class="inlinerows">
                <span class="span2">Chart&nbsp;Type</span>
                <span id="bar_button_span" class="button_span">
					<button id="bar_button" class="chart_type_button" value="bar"><img src="../images/ico_3.png"
                                                                                       alt="Horizontal Bar Chart">
					</button></span>
                <span id="column_button_span" class="button_span">
					<button id="column_button" class="chart_type_button" value="column"><img src="../images/ico_2.png"
                                                                                             alt="Vertical Bar Chart">
					</button></span>
                <span id="area_button_span" class="button_span">
					<button id="area_button" class="chart_type_button" value="area"><img src="../images/ico_8.png"
                                                                                         alt="Area Chart">
					</button></span>
                <span id="line_button_span" class="button_span">
					<button id="line_button" class="chart_type_button" value="line"><img src="../images/ico_5.png"
                                                                                         alt="Line Chart">
					</button></span>
                <span id="pie_button_span" class="button_span">
					<button id="pie_button" class="chart_type_button" value="pie"><img src="../images/ico_4.png"
                                                                                       alt="Pie Chart">
					</button></span>
                <span id="bubble_button_span" class="button_span">
					<button id="bubble_button" class="chart_type_button" value="bubble"><img src="../images/ico_7.png"
                                                                                             alt="Bubble Chart">
					</button></span>
            </div>
            <div class="inlinerows">
                <span class="span2">Categories</span>
                <button id="dc_add_category" onclick="addCategory();">
                    Add Category
                </button>
            </div>

            <div class="inlinerows">
                <div id="dc_categories_outer_div">

                </div>
            </div>
            <div class="inlinerows">
                <span class="span2">Measures</span>
                <button id="dc_add_measure" onclick="addMeasure();">
                    Add Measure
                </button>
            </div>
            <div class="inlinerows">
                <div id="dc_measures_outer_div">

                </div>
            </div>

            <div class="inlinerows">
                <span class="span2">Render Threshold</span>
                <input type="text" placeholder="5000">
            </div>
            <div class="inlinerows">
                <span class="span2">&nbsp;</span>
                <label class="checkbox floatLt">
                    <input type="checkbox">
                    Suppress Nulls </label>
            </div>
            <div class="inlinerows">
                <span class="span2">&nbsp;</span>
                <label class="checkbox floatLt">
                    <input type="checkbox">
                    Show Legend </label>
            </div>
        </div>

        <div id="drillModalFooter" class="modal-footer">
            <div style="float: right">
                <a id="saveDrillChart" class="btn btn-inverse"
                   onclick="$('#DrillChartModal').modal('hide');openDrillChart($('#DrillChartModal').data('parent'), 'create');">Save</a>
                <a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
            </div>
        </div>
    </div>
    <!------------------- Chart Settings modal template ---------------------------------->

    <!----------------------------------------- Edit node HTML ------------------------------------------->
    <script id="edit-node-modal-div" type="text/html">
        <div class="modal hide edit-node-modal" style="top: 40%;">
            <h3 class="box_head new_box_head" id="myModalLabel">
                Edit Node
                <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                    <img src="../images/addlayout/close.png" width="18" height="18">
                </button>
            </h3>

            <div class="modal-body">
                <div class="inlinerows">
                    <div class="span4">
                        <div class="">
                            <div class="inlinerows">
                                <span class="span1 no-margin">Name</span>
                                <input class="input-medium edit-node-name " type="text" placeholder="">

                            </div>


                            <div class="inlinerows">
                                <span class="span1 no-margin">Scale</span>
                                <input type="checkbox" id="static">
                            </div>

                            <div class="inlinerows">
                                <span class="span1 no-margin">Static</span>
                                <input type="number" min=".2" max="5" step=".1" id="scaleStatic"
                                       style="display:none; float:left; margin-right: 3px;" class="input-medium"/>
                                <select id="scaleNonStatic" style="display:none;" class="input-medium">
                                    <option value="Number of Neighbors">Number of Neighbors</option>
                                    <option value="Betweenness">Betweenness</option>
                                    <option value="Closeness">Closeness</option>
                                    <option value="Eigenvector">Eigenvector</option>
                                    <option value="Count">Count</option>
                                </select>
                            </div>

                            <div class="inlinerows">
                                <span class="span1 no-margin">Labels</span>
                                <input type="checkbox" id="hideLabel">
                                Hide labels
                            </div>

                            <div class="inlinerows">
                                <span class="span1 no-margin">Tooltips</span>
                                <input type="checkbox" id="hideEmptyValue">
                                Hide empty values
                            </div>

                            <div class="preview">
                                <h5> Preview</h5>
                                <div class="previewarea">
                                    <img src="../images/icons/icons4.png" class="preview-image">
                                </div>
                            </div>
                            <br clear="all"/>
                        </div>
                    </div>

                    <div class="span4 no-margin pull-right">
                        <div class="accordion edit_node_accordion" id="accordion2_{{nodeName}}">
                            <div class="accordion-group">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle" data-toggle="collapse"
                                       data-parent="#accordion2_{{nodeName}}" href="#collapseOne_{{nodeName}}">
                                        Color<b class="caret"></b>
                                    </a>
                                </div>
                                <div id="collapseOne_{{nodeName}}" class="accordion-body collapse">
                                    <div class="accordion-inner" style="height: 152px;">
                                        <div class="color-picker">
                                            <select id="nodeColorPicker">
                                                <option value="#660000">#660000</option>
                                                <option value="#990000">#990000</option>
                                                <option value="#CC0000">#CC0000</option>
                                                <option value="#CC3333">#CC3333</option>
                                                <option value="#EA4C88">#EA4C88</option>
                                                <option value="#D10553">#D10553</option>
                                                <option value="#823CC8">#823CC8</option>
                                                <option value="#663399">#663399</option>
                                                <option value="#333999">#333999</option>
                                                <option value="#0066CC">#0066CC</option>
                                                <option value="#0099CC">#0099CC</option>
                                                <option value="#7AD9F9">#7AD9F9</option>
                                                <option value="#66CCCC">#66CCCC</option>
                                                <option value="#74E618">#74E618</option>
                                                <option value="#77CC33">#77CC33</option>
                                                <option value="#336600">#336600</option>
                                                <option value="#666600">#666600</option>
                                                <option value="#999900">#999900</option>
                                                <option value="#CCCC33">#CCCC33</option>
                                                <option value="#EAEA26">#EAEA26</option>
                                                <option value="#FFFF00">#FFFF00</option>
                                                <option value="#FFCC33">#FFCC33</option>
                                                <option value="#FF9900">#FF9900</option>
                                                <option value="#CE7C00">#CE7C00</option>
                                                <option value="#FF6600">#FF6600</option>
                                                <option value="#CC6633">#CC6633</option>
                                                <option value="#996633">#996633</option>
                                                <option value="#AA6117">#AA6117</option>
                                                <option value="#663300">#663300</option>
                                                <option value="#000000">#000000</option>
                                                <option value="#999999">#999999</option>
                                                <option value="#CCCCCC">#CCCCCC</option>
                                            </select>
                                        </div>

                                        <div class="rgb-select">
                                            <div class="color-preview"></div>
                                            <div class="color-component red-component">
                                                R&nbsp;
                                                <input type="text" class="span1 no-margin-input">
                                            </div>
                                            <div class="color-component green-component">
                                                G&nbsp;
                                                <input type="text" class="span1 no-margin-input">
                                            </div>
                                            <div class="color-component blue-component">
                                                B&nbsp;
                                                <input type="text" class="span1 no-margin-input">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="accordion-group">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle" data-toggle="collapse"
                                       data-parent="#accordion2_{{nodeName}}" href="#collapseTwo_{{nodeName}}">
                                        Shape<b class="caret"></b>
                                    </a>
                                </div>
                                <div id="collapseTwo_{{nodeName}}" class="accordion-body collapse">
                                    <div class="accordion-inner shaperow">
                                        <div class="inlinerows">
                                            <img src="../images/shapes/circle.png" class="shape" data-val="Circle">
                                            <img src="../images/shapes/diamond.png" class="shape" data-val="Diamond">
                                            <img src="../images/shapes/hexagon.png" class="shape" data-val="Hexagon">
                                            <img src="../images/shapes/pentagon.png" class="shape" data-val="Pentagon">
                                            <img src="../images/shapes/octagon.png" class="shape" data-val="Octagon">
                                            <img src="../images/shapes/house.png" class="shape"
                                                 data-val="Pentagon/House">
                                            <img src="../images/shapes/square.png" class="shape" data-val="Square">
                                            <img src="../images/shapes/star.png" class="shape" data-val="Star">
                                            <img src="../images/shapes/triangle.png" class="shape" data-val="Triangle">
                                            <span data-val="None" class="shape" data-val="None">None</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="accordion-group">
                                <div class="accordion-heading">
                                    <a class="accordion-toggle" data-toggle="collapse"
                                       data-parent="#accordion2_{{nodeName}}" href="#collapseThree_{{nodeName}}">
                                        Icon<b class="caret"></b>
                                    </a>
                                </div>
                                <div id="collapseThree_{{nodeName}}" class="accordion-body collapse">
                                    <div class="accordion-inner iconrow">
                                        <div style="float: right; padding-right: 6px;">
                                            <a id='node-edit-none-option'> None </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <div id="theme-enabled-node-msg" style="dispaly:none;">
                            For this node definition, shape, color, and icon are determined by the theme
                        </div>
                    </div>
                </div>

                <br clear="all"/>
                <ul class="nav nav-tabs new-nav-tabs" id="myTab2">
                    <li class="active">
                        <a href="#home_{{nodeName}}" data-toggle="tab">Basics</a>
                    </li>
                    <li>
                        <a href="#tooltip_{{nodeName}}" data-toggle="tab">Tooltip Fields</a>
                    </li>
                    <li>
                        <a href="#computed_{{nodeName}}" data-toggle="tab" id="computed-fields-tab">Computed Fields</a>
                    </li>
                    <li>
                        <a href="#linkups_{{nodeName}}" data-toggle="tab">Linkups</a>
                    </li>
                    <li>
                        <a href="#bundlings_{{nodeName}}" data-toggle="tab">Bundlings</a>
                    </li>
                </ul>

                <div class="tab-content node-editor-tabs">
                    <div class="tab-pane" id="bundlings_{{nodeName}}">
                        <div class="bundling-block">
                            <table width="100%" border="" class=" table table-striped" id="bundletab-table">
                                <thead>
                                <tr>
                                    <th style="width: 160px;">Field</th>
                                    <th style="width: 64px;">Delete</th>
                                </tr>
                                </thead>
                            </table>
                            <button class="btn btn-block btn-primary" type="button" id="addBundlingBtn">
                                Add Bundling Field
                            </button>
                        </div>

                        <div class="sorting-block">
                            <a class="move-up"></a>
                            <a class="move-down"></a>
                        </div>
                    </div>
                    <div class="tab-pane active" id="home_{{nodeName}}">
                        <div class="">
                            <div class="inlinerows">
                                <br/>
                                <span class="span1 rightText">Label</span>
                                <select class="span2" id="labelCategory">
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="labelValue">
                                </select>
                                <input type="text" value="" style="display:none;" id="labelText"/>

                            </div>
                            <div class="inlinerows ">
                                <span class="span1 rightText">ID</span>

                                <select class="span2" id="idCategory">
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="idValue">
                                </select>
                                <input type="text" value="" style="display:none;" id="idText"/>

                            </div>

                            <div class="inlinerows">
                                <span class="span1 rightText">&nbsp;</span><span class="span5  no-margin">
									<input type="checkbox" id="multi-type-node">

									Allow Multi-Typed Nodes</span>

                            </div>
                            <div class="inlinerows">
                                <span class="span1 rightText">Type</span>

                                <select class="span2" id="typeCategory">
                                    <option value="NONE">None</option>
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="typeValue" style="display:none;">
                                </select>
                                <input type="text" value="" style="display:none;" id="typeText"/>

                            </div>
                            <div class="inlinerows">
                                <span class="span1 rightText">URL</span>

                                <select class="span2" id="urlCategory">
                                    <option value="NONE">None</option>
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="urlValue" style="display:none;">
                                </select>
                                <input type="text" value="" style="display:none;" id="urlText"/>

                            </div>
                            <div class="inlinerows urlTextDiv urlTextMoveLeft" style="display: none;">
                                <span class="span1 rightText">URL Text</span>

                                <select class="span2" id="docCategory">
                                    <option value="NONE">None</option>
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="docValue" style="display:none;">
                                </select>
                                <input type="text" value="" style="display:none;" id="docText"/>


                            </div>
                        </div>

                    </div>
                    <div class="tab-pane" id="tooltip_{{nodeName}}">

                        <table width="100%" border="" class=" table table-striped" id="tooltipfields-table">
                            <thead>
                            <tr>
                                <th style="width: 160px; ">Display Name</th>
                                <th style="width: 64px; ">Static</th>
                                <th style="width: 160px; ">Field/ Value</th>
                                <th style="width: 64px; ">Delete</th>
                            </tr>
                            </thead>
                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="addTooltipBtn">
                            Add Tooltip Field
                        </button>

                    </div>
                    <div class="tab-pane" id="computed_{{nodeName}}">
                        <table width="100%" border="" class=" table table-striped" id="computed-fields-table">
                            <tr>
                                <th style="width: 140px;">Display Name</th>
                                <th style="width: 140px;">Field/ Value</th>
                                <th style="width: 140px;">Function</th>
                                <th style="width: 64px;">Show in Tooltip</th>
                                <th style="width: 64px;">Delete</th>
                            </tr>
                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="add-computed-field-button">
                            Add Computed Field
                        </button>

                    </div>
                    <div class="tab-pane" id="linkups_{{nodeName}}">

                        <table width="100%" border="" class=" table table-striped" id="linksup">
                            <thead>
                            <tr>
                                <td>Name</td>
                                <td></td>
                            </tr>
                            </thead>
                            <tbody>

                            </tbody>

                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="addLinkBtn">
                            Add Linkup
                        </button>

                    </div>

                </div>
            </div>

            <div class="modal-footer">
                <div style="float: right">
                    <a id="treeFinish-ne" class="btn btn-inverse">Save</a>
                    <a class="btn btn-inverse" data-dismiss="modal" class="close" id="cancel-ne">Cancel</a>
                </div>
            </div>

            <div class="modal hide" id="myModalPop" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
                 aria-hidden="true">
                <h3 class="box_head new_box_head" id="myModalLabel">
                    Edit Linkup
                    <button type="button" class="close modelClose"
                            onclick="$(this).parents('.modal').eq(0).modal('hide')" aria-hidden="true">
                        <img src="../images/addlayout/close.png" width="18" height="18">
                    </button>
                </h3>

                <div class="modal-body">
                    <p>
                        <span class="span1">Link to </span><select class="span4 floatLt" id="dvList"></select>
                        <button class="btn floatRt " id="updateFields">
                            Update Fields
                        </button>
                    </p>

                    <div class="inlinerows">
                        <span class="span2">Source Field*</span><select name="" class="span3" id="sourceField"></select>
                    </div>

                    <div class="inlinerows">
                        <span class="span2">Target Field*</span><select name="" class="span3" id="targetField"></select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-inverse" id="updateLink">
                        Update
                    </button>
                    <button class="btn" aria-hidden="true" onclick="$(this).parents('.modal').eq(0).modal('hide')"
                            id="cancelUpdate">
                        Cancel
                    </button>

                </div>
            </div>
        </div>


    </script>
    <!---------------------------------------- Edit Node HTML end -------------------------------------------->

    <!----------------------------------------- Mixed chart template start -------------------------------------------->
    <script type="text/html" id="mixed-chart-template">
        <div id="6" class="block box_content round_bottom">
            <h4 class="alert_title">Alert Name</h4>
            <table cellspacing="0" cellpadding="0" border="0" class="table alert-table">

                <tbody>
                <tr>
                    <th></th>
                    <th width="10%">ATM</th>
                    <th width="10%">CARD</th>
                    <th width="10%">CHECK</th>
                    <th width="10%">KYC</th>
                </tr>

                <tr>
                    <td class="alert-td">419 Scan</td>
                    <td class="checked-td"></td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">Altered Check</td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class="unchecked-td"></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">Empty Deposite</td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class="checked-td"></td>
                </tr>
                <tr>
                    <td class="alert-td">Forged Signature
                        <br>
                    </td>
                    <td class=""></td>
                    <td class="unchecked-td"></td>
                    <td class=""></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">High Appraisal</td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class="unchecked-td"></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">Alert
                        <br>
                    </td>
                    <td class=""></td>
                    <td class="checked-td"></td>
                    <td class=""></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">KYC: Account Opening</td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class="checked-td"></td>
                    <td class=""></td>
                </tr>
                <tr>
                    <td class="alert-td">419 Scan</td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class=""></td>
                    <td class=" unchecked-td"></td>
                </tr>
                <tr>
                    <td class="alert-td">419 Scan</td>
                    <td class=""></td>
                    <td class="checked-td"></td>
                    <td class=""></td>
                    <td class=""></td>
                </tr>
                </tbody>
            </table>
        </div>
    </script>
    <!----------------------------------------- Mixed chart template end -------------------------------------------->

    <!-------------------------------------- Site Manager template -------------------------------------------------->
    <script type="text/html" id="site-manager-template">
        <div class="block box_content round_bottom">
            <ul id="config" class="nav nav-pills subtabs">
                <li class="">
                    <a data-toggle="dropdown" class="dropdown-toggle action">Action</a>
                </li>
                <li class="">
                    <a data-toggle="dropdown" class="dropdown-toggle configure">Configure</a>
                </li>
                <li class="">
                    <a data-toggle="dropdown" class="dropdown-toggle tools">Tools</a>
                </li>
            </ul>
            <h4 class="save">Save New Filter Set</h4>
            <table class="table table-striped">
                <thead>
                <tr class="grey_bg">
                    <th colspan="2">Set Manager</th>
                    <th></th>
                    <th><img width="20" height="19" src="img/signal.png"></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td width="20">
                        <input type="checkbox" value="" name="">
                    </td>
                    <td>Filer Name</td>
                    <td class="center">6/06/21</td>
                    <td width="20" class="center"><img width="21" height="20" src="img/edit.png"></td>

                </tr>
                <tr>
                    <td>
                        <input type="checkbox" value="" name="">
                    </td>
                    <td>Filer Name</td>
                    <td class="center">6/06/21</td>
                    <td class="center"><img width="21" height="20" src="img/edit.png"></td>

                </tr>

                <tr>
                    <td>
                        <input type="checkbox" value="" name="">
                    </td>
                    <td>Filer Name</td>
                    <td class="center">6/06/21</td>
                    <td class="center"><img width="21" height="20" src="img/edit.png"></td>

                </tr>
                <tr>
                    <td>
                        <input type="checkbox" value="" name="">
                    </td>
                    <td>Filer Name</td>
                    <td class="center">6/06/21</td>
                    <td class="center"><img width="21" height="20" src="img/edit.png"></td>

                </tr>
                <tr>
                    <td>
                        <input type="checkbox" value="" name="">
                    </td>
                    <td>Filer Name</td>
                    <td class="center">6/06/21</td>
                    <td class="center"><img width="21" height="20" src="img/edit.png"></td>

                </tr>
                </tbody>
            </table>
        </div>
    </script>
    <!-------------------------------------- Site Manager template end -------------------------------------------------->

    <!-------------------------------------- New Visualization dialogue -------------------------------------------------->
    <div id="new-visualization-dialogue" class="modal hide small_modal">
        <div id="box_0" class="box grad_colour_dark_blue" style="">
            <h3 class="box_head" id="myModalLabel">Add a Visualization</h3>
            <a data-dismiss="modal" class="destroy box_button_1"
               title="Close">&nbsp;</a>

            <div class="modal-body" style="padding: 0px 15px;">
                <table class="table">
                    <tr>
                        <td><img src="../images/new_visualization_images/bar.png"></td>
                        <td onclick="$('#new-visualization-dialogue').modal('hide');$('#DrillChartModal').data('parent', $('#new-visualization-dialogue').data('parent'));$('#DrillChartModal').modal();">
                            Chart
                        </td>
                    </tr>

                    <tr>
                        <td><img src="../images/new_visualization_images/table.png"></td>
                        <td>Table</td>
                    </tr>
                    <tr>
                        <td><img
                                src="../images/new_visualization_images/relation_graph.png"></td>
                        <td
                                onclick="$('#new-visualization-dialogue').modal('hide'); new CreateRgSettings().reset(); $('#alertModal').modal();">
                            Relationship Graph
                        </td>
                    </tr>
                    <tr>
                        <td><img src="../images/new_visualization_images/node.png"></td>
                        <td>Timeline</td>
                    </tr>
                    <tr>
                        <td><img src="../images/new_visualization_images/node.png"></td>
                        <td onclick="$('#new-visualization-dialogue').modal('hide'); new CreateGeoMapSettings().reset(); $('#GeoMapChartModal').modal();">
                            Geo Map
                        </td>
                    </tr>
                    <tr>
                        <td><img src="../images/new_visualization_images/node.png"></td>
                        <td>Geo Spatial</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
    <!-------------------------------------- New Visualization dialogue end -------------------------------------------------->

    <!-------------------------------------- New layout dialogue ------------------------------------------------------------->
    <div id="new-layout-dialogue" class="modal hide">
        <div id="box_0" class="box grad_colour_dark_blue" style="">
            <h3 class="box_head" id="myModalLabel">Select a Layout</h3>
            <a data-dismiss="modal" class="destroy box_button_1"
               title="Close">&nbsp;</a>

            <div class="modal-body">
                <div class="inlinerows">
                    <input class="span6" type="text" placeholder="Name of New Worksheet" id="new-layout-name">
                </div>
                <div class="span6">
					<span class="span3">
						<a id="left-hand-layout">
							Left Panel <br/>
							<img src="../images/select_new_layout/left_col.png" width="139" height="70">
						</a>
					</span>

                    <span class="span2">
						<a id="right-hand-layout">
							Right Panel <br/>
							<img src="../images/select_new_layout/right_col.png" width="139" height="70">
						</a>
					</span>
                </div>

                <br clear="all"/> <br clear="all"/>

                <div class="span6">
					<span class="span3">
						<a id="equal-layout">
							Side By Side <br/>
							<img src="../images/select_new_layout/equal.png">
						</a>
					</span>
                    <span class="span2">
						<a id="single-layout">
							Single <br/>
							<img src="../images/select_new_layout/single.png">
						</a>
					</span>
                </div>

                <br clear="all"/>
            </div>

            <div class="modal-footer">
                <div style="float: right">
                    <a class="btn btn-inverse" data-dismiss="modal"
                       class="close">Cancel</a> <a id="treeFinish"
                                                   class="btn btn-inverse btn-primary new-layout-select">Select</a>
                </div>
            </div>
        </div>

    </div>
    <!-------------------------------------- New layout dialogue end ------------------------------------------------------------->

    <!------------------------------------- One Tab header layout --------------------------------------------->
    <script type="text/html" id="one-tab-header-layout">
        <li id="layoutTab{{worksheetUuid}}" class="dropdown">
            <a class="lName" data-toggle="tab" href="{{layoutHref}}">{{layoutName}}</a>
            <a data-toggle="dropdown" class="dropdown-toggle dashBoardToggle"><b class="caret"></b></a>
            <ul class="dropdown-menu">
                <li>
                    <a id="addVisualizationHeaderLink{{index}}" wsuuid="{{worksheetUuid}}">Add a Visualization</a>
                </li>
                <li>
                    <a wsuuid="{{worksheetUuid}}" id="openworksheetimage{{index}}">Open Worksheet as image</a>
                </li>
                <li>
                    <a wsuuid="{{worksheetUuid}}" class="renameWorksheet" id="renameWS{{worksheetUuid}}">Rename
                        Worksheet</a>
                </li>
                <li>
                    <a wsuuid="{{worksheetUuid}}" class="deleteWorksheet" id="deleteWS{{worksheetUuid}}">Delete
                        Worksheet</a>
                </li>
            </ul>
        </li>
    </script>
    <!------------------------------------- One Tab header layout end --------------------------------------------->

    <!-------------------------------------- Right hand layout template ----------------------------------------->
    <script type="text/html" id="right-hand-layout-template">
        <div id="{{layoutId}}" class="tab-pane worksheetTab rhl">
            <input type="hidden" id="layout_type{{index}}" value="RIGHT HAND LAYOUT" class="layout_type">
            <input type="hidden" id="work_sheet_id{{index}}" value="">
            <div class="row-fluid">
                <table width="100%" border="0">
                    <tbody>
                    <tr>
                        <!------------ Relation graph is added to this td's rel-panels class ---------------------------->
                        <td valign="top" id="right_content" class="main-panel-td"
                            style="padding-left: 0%;height: 500px;">
                            <div class="span12">
                                <div class="toggle_right_panel">
                                    <img id="toggle_right_panel{{index}}" src="../images/toggle_inverse.png"
                                         style="width: 36px; height: 32px;">
                                </div>
                                <div class="rel-panels vizPanels" id="layout{{index}}_panel0" style="height: 500px;"
                                     data-position="0">
                                    <div class="toggle_container" id="layout{{index}}_relpanel0" style="height: 100%;">
                                        <div id="layout{{index}}_chart0" style="text-align: center; width: 100%;">
                                            <input type="button" class="btn btn-large btn-primary addVisualization"
                                                   value="Add a Visualization" style="margin-top: 300px;"
                                                   onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '0');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                            <span class="vLayoutPosition" style="display:none">0</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </td>

                        <td valign="top" id="left_content" class="nano small-panel-td"
                            style="display: block;padding-left: 10px; height: 500px;">
                            <div class="span12 sortable content_side_bar overthrow">
                                <div id="col1" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel1"
                                         class="box grad_colour_dark_blue chart_table vizPanels" style=""
                                         data-position="1">
                                        <div class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart1" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '1');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">1</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div id="col2" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel2"
                                         class="box grad_colour_dark_blue chart_table vizPanels" style=""
                                         data-position="2">
                                        <div class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart2" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '2');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">2</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div id="col3" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel3" class="box grad_colour_dark_blue vizPanels"
                                         style="" data-position="3">
                                        <div id="5" class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart3" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '3');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">3</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div><!--/span-->
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div><!--/row-->
        </div>
    </script>
    <!-------------------------------------- Right hand layout end ----------------------------------------->

    <!-------------------------------------- Left hand layout template ---------------------------------------------->
    <script type="text/html" id="left-hand-layout-template">
        <div id="{{layoutId}}" class="tab-pane worksheetTab lhl">
            <input type="hidden" id="layout_type{{index}}" value="LEFT HAND LAYOUT" class="layout_type">
            <input type="hidden" id="work_sheet_id{{index}}" value="">
            <div class="row-fluid">
                <table width="100%" border="0">
                    <tbody>
                    <tr>
                        <td valign="top" id="left_content" class="nano small-panel-td"
                            style="display: block; height: 500px;">
                            <div class="span12 sortable content_side_bar overthrow">

                                <div id="col1" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel1"
                                         class="box grad_colour_dark_blue chart_table vizPanels" style=""
                                         data-position="1">
                                        <div class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart1" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '1');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">1</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div id="col2" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel2"
                                         class="box grad_colour_dark_blue chart_table vizPanels" style=""
                                         data-position="2">
                                        <div class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart2" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '2');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">2</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div id="col3" class="span12 column sortable-items" style="">
                                    <div id="layout{{index}}_panel3" class="box grad_colour_dark_blue vizPanels"
                                         style="" data-position="3">
                                        <div class="toggle_container right-side-boxes">
                                            <div id="layout{{index}}_chart3" style="text-align: center; width: 100%;">
                                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                                       value="Add a Visualization" style="margin-top: 100px;"
                                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '3');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                                <span class="vLayoutPosition" style="display:none">3</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div><!--/span-->
                        </td>

                        <!------------ Relation graph is added to this td's rel-panels class ---------------------------->
                        <td valign="top" id="right_content" class="main-panel-td" style="height: 500px;">
                            <div class="span12">
                                <div class="toggle">
                                    <img id="toggle{{index}}" style="width: 36px; height: 32px;margin-top: -1px;"
                                         src="../images/toggle.png">
                                    <img id="dummy-image" style="display: none;" src="../images/toggle_inverse.png">
                                </div>

                                <div class="rel-panels vizPanels" id="layout{{index}}_panel0" style="height: 500px;"
                                     data-position="0">
                                    <div class="toggle_container" id="layout{{index}}_relpanel0" style="height: 100%;">
                                        <div id="layout{{index}}_chart0" style="text-align: center; width: 100%;">
                                            <input type="button" class="btn btn-large btn-primary addVisualization"
                                                   value="Add a Visualization" style="margin-top: 300px;"
                                                   onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '0');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                            <span class="vLayoutPosition" style="display:none">0</span>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div><!--/row-->

        </div>
    </script>
    <!-------------------------------------- Left hand layout template end ---------------------------------------------->

    <!-------------------------------------- Single layout template ------------------------------------------------------>
    <script type="text/html" id="single-panel-layout-template">
        <div id="{{layoutId}}" class="tab-pane worksheetTab sl">
            <input type="hidden" id="layout_type{{index}}" value="SINGLE LAYOUT">
            <input type="hidden" id="work_sheet_id{{index}}" value="">
            <div class="rel-panels" id="layout{{index}}_panel0" style="height: 500px;">
                <div class="toggle_container" id="layout{{index}}_relpanel0" style="height: 100%">
                    <div id="layout{{index}}_chart0" style="text-align: center; width: 100%;">
                        <input type="button" class="btn btn-large btn-primary addVisualization"
                               value="Add a Visualization" style="margin-top: 300px;"
                               onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '0');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                        <span class="vLayoutPosition" style="display:none">0</span>
                    </div>
                </div>
            </div>
        </div>
    </script>
    <!-------------------------------------- Single layout template end ------------------------------------------------------>


    <!-------------------------------------- Equal panel layout template ----------------------------------------------------->
    <script type="text/html" id="equal-panel-layout-template">
        <div id="{{layoutId}}" class="tab-pane worksheetTab el">
            <input type="hidden" id="layout_type{{index}}" value="EQUAL LAYOUT" class="layout_type">
            <input type="hidden" id="work_sheet_id{{index}}" value="">
            <div class="row-fluid">
                <div class="equal-left">
                    <div class="rel-panels" id="layout{{index}}_panel0" style="height: 500px;">
                        <div class="toggle_container" id="layout{{index}}_relpanel0" style="height: 100%">
                            <div id="layout{{index}}_chart0" style="text-align: center; width: 100%;">
                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                       value="Add a Visualization" style="margin-top: 300px;"
                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '0');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                <span class="vLayoutPosition" style="display:none">0</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="equal-right">
                    <div class="rel-panels" id="layout{{index}}_panel1" style="height: 500px;">
                        <div class="toggle_container" id="layout{{index}}_relpanel1" style="height: 100%;">
                            <div id="layout{{index}}_chart1" style="text-align: center; width: 100%;">
                                <input type="button" class="btn btn-large btn-primary addVisualization"
                                       value="Add a Visualization" style="margin-top: 300px;"
                                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '1');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                                <span class="vLayoutPosition" style="display:none">1</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>
    <!-------------------------------------- Equal panel layout template end ------------------------------------------------->

    <!-------------------------------------- Small panel template ------------------------------------->
    <script type="text/html" id="small-panel-template">
        <h2 class="box_head round_top">
            <a class="left_icons dropdown-toggle" data-toggle="dropdown"><img src="../images/mixed.png"> </a>
            <ul class="dropdown-menu smallpanel-menu" role="menu" aria-labelledby="dLabel">
                <li class="actionLi">
                    <a class="actionIcon dropdown-toggle" data-toggle="dropdown">Action</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu" role="menu">
                        <li>
                            <a>Load</a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a class="disabled-link">Spinoff...</a>
                        </li>
                        <li>
                            <a>Print</a>
                        </li>
                        <li>
                            <a>Publish...</a>
                        </li>
                        <li>
                            <a>Export to Csv</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}">Open as Image</a>
                        </li>
                    </ul>
                </li>
                <li class="editLi">
                    <a class="editIcon">Edit</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu">
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var selectall = new SelectAll($(this).attr('vizid')); selectall.doTask();">Select
                                All</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" id="deselectall{{vizId}}"
                               onclick="var deselectall = new DeSelectAll($(this).attr('vizid')); deselectall.doTask();"
                               class="disabled-link">Deselect All</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link">Select All Visible Neighbors</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" id="hideselection{{vizId}}"
                               onclick="var hideselection = new HideSelection($(this).attr('vizid')); hideselection.doTask();"
                               class="disabled-link">Hide Selection</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" id="unhideselection{{vizId}}"
                               onclick="var unhideselection = new UnHideSelection($(this).attr('vizid')); unhideselection.doTask();"
                               class="disabled-link">Unhide Selection</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link">Remove Selected Nodes</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link">Clear Merge Highlights</a>
                        </li>
                    </ul>
                </li>
                <li class="configureLi">
                    <a class="configureIcon  dropdown-toggle" data-toggle="dropdown">Configure</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var createtrgSettings = new CreateRgSettings($(this).attr('vizid'));  createtrgSettings.generateSettingsDialog();">Settings</a>
                        </li>
                        <li>
                            <a>Filters</a>
                        </li>
                    </ul>

                </li>
                <li class="toolsLi">
                    <a class="toolsIcon">Tools</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu">
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link" id="selectneighbor{{vizId}}"
                               onclick="$('#select-neighbor-dialogue'+$(this).attr('vizid')).modal();">Select
                                Neighbors...</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link" id="revealneighbor{{vizId}}"
                               onclick="$('#reveal-neighbor-dialogue'+$(this).attr('vizid')).modal();">Reveal
                                Neighbors...</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" id="computeSNA{{vizId}}"
                               onclick="var computeSNA = new ComputeSNA($(this).attr('vizid')); computeSNA.doTask();">Compute
                                SNA Metrics</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link" id="bundleselection{{vizId}}"
                               onclick="$('#bundle-dialogue' + $(this).attr('vizid')).modal();">Bundle</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}" class="disabled-link" id="unbundleselection{{vizId}}"
                               onclick="$('#unbundle-dialogue' + $(this).attr('vizid')).modal();">Unbundle</a>
                        </li>
                    </ul>
                </li>
                <li class="layoutLi">
                    <a class="layoutIcon">Layout</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu">
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('centrifuge');">Centrifuge</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('circular');">Circular</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('forceDirected');">Force
                                Directed</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('hierarchical');">Linear
                                Hierarchy</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('radial');">Radial</a>
                        </li>
                        <li>
                            <a vizid="{{vizId}}"
                               onclick="var layout = new Layout($(this).attr('vizid')); layout.doTask('scramble');">Scramble
                                And Place</a>
                        </li>
                    </ul>
                </li>
                <li class="linkUpLi">
                    <a id="linkupDropDown{{vizId}}" class="linkupIcon disabled-link">Linkup</a>
                    <b class="right-caret"></b>
                </li>

            </ul>
            {{panelName}}
        </h2>

        <a class="broadcastIcon fourth_from_right dropdown-toggle" data-toggle="dropdown" title="Broadcast">&nbsp;</a>
        <ul class="dropdown-menu pull-right broadcast-menu smallpanel-menu" role="menu" aria-labelledby="dLabel">
            <li>
                <a class="stopListen">Stop Listening</a>
            </li>
            <li>
                <a class="startListen">Start Broadcasting</a>
            </li>
            <li>
                <a class="clearListen">Clear Broadcast</a>
            </li>

        </ul>


        <a class="switch_main third_from_right" title="Promote To Primary Visualization">&nbsp;</a>
        <a href="#collapseDiv{{vizIndex}}" class="toggle box_button_2" title="Toggle" data-toggle="collapse"
           id="collapseLink{{vizIndex}}">&nbsp;</a>
        <a onclick='new RemoveVisualization().confirmDeleteVisualization("{{vizId}}");' class="destroy box_button_1"
           title="Delete">&nbsp;</a>
        <div id="collapseDiv{{vizIndex}}" class="in">
            <div class="toggle_container">
                <canvas src="{{imgSrc}}" id={{vizId}} class="relGraphImage"/>
            </div>
        </div>
    </script>


    <script type="text/html" id="progress_bar_template">
        <div class="progress_loader">
            <img src="../images/loader.gif"/>
        </div>
    </script>

    <!------------------ Small Chart Panel Template -------------------------------------->
    <script type="text/html" id="small-chart-panel-template">
        <h2 class="box_head round_top">
            <a class="left_icons dropdown-toggle" data-toggle="dropdown"><img src="../images/mixed.png"> </a>
            <ul class="dropdown-menu smallpanel-menu chartsMenu" role="menu" aria-labelledby="dLabel">
                <li class="actionLi">
                    <a class="actionIcon dropdown-toggle" data-toggle="dropdown">Action</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu" role="menu">
                        <li>
                            <a>Load</a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a class="disabled-link">Spinoff...</a>
                        </li>
                        <li>
                            <a>Print</a>
                        </li>
                        <li>
                            <a>Publish...</a>
                        </li>
                        <li>
                            <a>Export to Csv</a>
                        </li>
                        <li>
                            <a>Open as Image</a>
                        </li>
                    </ul>
                </li>
                <li class="configureLi">
                    <a class="configureIcon  dropdown-toggle" data-toggle="dropdown">Configure</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                        <li>
                            <a onclick="openSettings('{{vizId}}','{{vizIndex}}','{{workSheetIndex}}');">Settings</a>
                        </li>
                        <li>
                            <a>Filters</a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a>Load On Startup</a>
                        </li>
                    </ul>
                </li>
                <li class="toolsLi">
                    <a class="toolsIcon">Tools</a>
                    <b class="right-caret"></b>
                    <ul class="dropdown-menu">
                        <li>
                            <a class="sorting-panel-link" vizuuid="{{vizId}}" vizIndex="{{vizIndex}}"
                               workSheetIndex="{{workSheetIndex}}">Sorting Panel</a>
                        </li>
                        <li>
                            <a>Fit To Size</a>
                        </li>
                        <li>
                            <a class="disabled-link">Reset Legend</a>
                        </li>
                    </ul>
                </li>
            </ul>
            {{panelName}}
        </h2>

        <a class="broadcastIcon fourth_from_right dropdown-toggle" data-toggle="dropdown" title="Broadcast">&nbsp;</a>
        <ul class="dropdown-menu pull-right broadcast-menu smallpanel-menu" role="menu" aria-labelledby="dLabel">
            <li>
                <a class="stopListen">Stop Listening</a>
            </li>
            <li>
                <a class="startListen">Start Broadcasting</a>
            </li>
            <li>
                <a class="clearListen">Clear Broadcast</a>
            </li>

        </ul>


        <a class="switch_main third_from_right" title="Promote To Primary Visualization">&nbsp;</a>
        <a href="#collapseDiv{{vizIndex}}" class="toggle box_button_2" title="Toggle" data-toggle="collapse"
           id="collapseLink{{vizIndex}}">&nbsp;</a>
        <a onclick='$("#delete_visualization #btn_delete_visualization").attr("vizid","{{vizId}}");$("#delete_visualization").modal();'
           class="destroy box_button_1" title="Delete">&nbsp;</a>

        <div id="collapseDiv{{vizIndex}}" class="in">

        </div>
    </script>
    <!------------------ Small Chart Panel Template end -------------------------------------->

    <!----------------------- Large panel empty layout ----------------------------------------------->
    <script type="text/html" id="large-panel-toolbar-layout">
        <div class="title-image-wrapper">
            <a id="config{{index}}" class="settings box_button_3" title="Settings">&nbsp;</a>
            <a class="destroy box_button_1" title="Delete"
               onclick="new RemoveVisualization().confirmDeleteVisualization('{{vizUuid}}');">&nbsp;</a>
        </div>
    </script>
    <!----------------------- Large panel empty layout end ----------------------------------------------->


    <!----------------------- Small panel empty layout ----------------------------------------------->
    <script type="text/html" id="small-panel-empty-layout">
        <div class="toggle_container right-side-boxes">
            <div id="layout{{workSheetIndex}}_chart{{vizPosition}}" style="text-align: center; width: 100%;">
                <input type="button" class="btn btn-large btn-primary addVisualization" value="Add a Visualization"
                       style="margin-top: 100px;"
                       onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '{{vizPosition}}');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                <span class="vLayoutPosition" style="display:none">{{vizPosition}}</span>
            </div>
        </div>
    </script>
    <!----------------------- Small panel empty layout end ----------------------------------------------->

    <!-------------------------- Modal window for flex editor --------------------------------------------->
    <div id="flexModal" class="flex_modal modal hide">
        <h3 class="box_head new_box_head" id="myModalLabel">
            Data sources editor
            <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                <img src="../images/addlayout/close.png" width="18" height="18">
            </button>
        </h3>
        <iframe id="flexIframe" src="">

        </iframe>
    </div>
    <!-------------------------- Modal window for flex editor end --------------------------------------------->


    <!-------------------------- Modal window for Link delete  --------------------------------------------->
    <div id="delete_link_modal" class="modal hide small_modal in delete_visualization">
        <h3 id="myDeleteLinkModalLabel" class="box_head">Delete Link</h3>
        <div style="padding: 0px 15px;" class="modal-body">
            Are you sure that you want to delete this link....?
            <div class="modal-footer" style="float: right">
                <a data-dismiss="modal" class="btn btn-inverse">Cancel</a>
                <a data-dismiss="modal" id="btn_delete_link_modal" class="btn btn-inverse btn-primary "
                   onClick="deleteLinkModal()">OK</a>
            </div>
        </div>
    </div>
    <!-------------------------- Modal window for Link delete end --------------------------------------------->


    <!-------------------------- Link edit modal popup --------------------------------------------------------->
    <script id="link-edit-popup-template" type="text/html">
        <div id="link-edit-modal{{linkName}}" class="modal hide link-edit-modal">
            <h3 class="box_head" id="myModalLabel">Edit Link
                <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                    <img src="../images/addlayout/close.png" width="18" height="18">
                </button>
            </h3>

            <div class="modal-body">
                <div class="rightAligned">
                    <div class="inlinerows">
                        <span class="span2 no-margin">Name</span>
                        <input class="input-medium" type="text" placeholder="" id="link-name">

                    </div>

                    <div class="inlinerows">
                        <span class="span2 no-margin">Tooltips</span>

                        <input type="checkbox" id="link-hideEmptyValue">
                        Hide empty values

                    </div>

                    <div class="inlinerows">
                        <br clear="all"/>
                        <span class="span2 no-margin">Static</span>
                        <span class="floatLt">
							<input type="checkbox" id="link-static" checked>
						</span>
                    </div>
                    <div class="inlinerows">
                        <span class="span2 no-margin">Scale</span>
                        <input type="number" min=".2" max="5" step=".1" id="link-static-scale"
                               style="display:none; float:left; margin-right: 3px;"/>
                        <select class="span1" id="link-non-static-scale" style="width:75px">
                            <option value="COUNT">Count</option>
                        </select>

                    </div>

                </div>
                <br clear="all"/>
                <ul class="nav nav-tabs new-nav-tabs" id="link-editor-tabs">
                    <li class="active">
                        <a href="#link-home{{linkName}}" data-toggle="tab">Basics</a>
                    </li>
                    <li>
                        <a href="#link-tooltip{{linkName}}" data-toggle="tab">Tooltip Fields</a>
                    </li>
                    <li>
                        <a href="#link-computed{{linkName}}" data-toggle="tab">Computed Fields</a>
                    </li>

                    <li>
                        <a href="#link-linkups{{linkName}}" data-toggle="tab">Linkups</a>
                    </li>
                    <li>
                        <a href="#link-directions{{linkName}}" data-toggle="tab">Direction</a>
                    </li>
                </ul>

                <div class="tab-content">
                    <div class="tab-pane" id="link-linkups{{linkName}}">
                        <table width="100%" border="" class=" table table-striped" id="link-linkups-table">
                            <thead>
                            <tr>
                                <td>Name</td>
                                <td></td>
                            </tr>
                            </thead>
                            <tbody>

                            </tbody>

                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="addLinkups-links">
                            Add Linkups
                        </button>

                    </div>
                    <div class="tab-pane active" id="link-home{{linkName}}">
                        <div class="">
                            <div class="inlinerows">
                                <br/>
                                <span class="span1 no-margin">Label</span>
                                <select class="span2" id="link-label-category">
                                    <option value="NONE">None</option>
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="link-label-value" style="display:none;">
                                </select>
                                <input type="text" value="" style="display:none;" id="link-label-text"/>
                            </div>
                            <div class="inlinerows">
                                <span class="span1 no-margin">Type</span>

                                <select class="span2" id="link-type-category">
                                    <option value="NONE">None</option>
                                    <option value="COLUMN_REF">Field</option>
                                    <option value="STATIC">Static</option>
                                </select>
                                <select class="span3" id="link-type-value" style="display:none;">
                                </select>
                                <input type="text" value="" style="display:none;" id="link-type-text"/>
                            </div>
                        </div>

                    </div>
                    <div class="tab-pane" id="link-tooltip{{linkName}}">

                        <table width="100%" border="" class=" table table-striped" id="link-tooltipfields-table">
                            <tr>
                                <th style="width: 160px; ">Display Name</th>
                                <th style="width: 64px; ">Static</th>
                                <th style="width: 160px; ">Field/ Value</th>
                                <th style="width: 64px; ">Delete</th>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>

                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>

                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="add-tooltip-field-button">
                            Add Tooltip Field
                        </button>

                    </div>
                    <div class="tab-pane" id="link-computed{{linkName}}">
                        <table width="100%" border="" class=" table table-striped" id="link-computed-fields-table">
                            <tr>
                                <th style="width: 140px; ">Display Name</th>
                                <th style="width: 140px; ">Function</th>
                                <th style="width: 140px; ">Field/ Value</th>
                                <th style="width: 64px; ">Show in Tooltip</th>
                                <th style="width: 64px; ">Delete</th>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>

                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>

                        </table>
                        <button class="btn btn-block btn-primary" type="button" id="add-computed-field-button">
                            Add Computed Field
                        </button>

                    </div>

                    <div class="tab-pane" id="link-directions{{linkName}}">
                        <div class="inlinerows">
                            <div class="floatLt ltClient">
                                <img id="source_node" src="" width="65" height="65"><br>{{source}}
                            </div>
                            <div class="left_arrow">
                                <!-- If we change it to class="left_arrow disabled" the left arrow will be hidden -->
                                <img id="left_dir" src="../images/left_dir.png">
                            </div>
                            <div class="directionsLine">
                   				 <span class="icon_wrap"><!-- just need to make display none if we dont need icons  -->
                   					 <img id="quest" src="../images/quest.png">
                                     <!-- just need to replace new icons dynamically if we have -->
                				 </span>
                            </div>
                            <div class="right_arrow">
                                <!-- If we change it to class="right_arrow disabled" the right arrow will be hidden -->
                                <img id="right_dir" src="../images/right_dir.png">
                            </div>
                            <div class="floatLt">
                                <img id="target_node" src="" width="65" height="65"><br>{{target}}
                            </div>
                        </div>
                        <br>
                        <div class="inlinerows">
                            <span class="span2 no-margin">&nbsp;</span>
                            <span class="span4  no-margin"><input name="link-direction" type="radio" id="undirected"> Undirected</span>
                        </div>

                        <br/>

                        <div class="inlinerows">
                            <span class="span2 no-margin">&nbsp;</span>
                            <span class="span4  no-margin"><input name="link-direction" type="radio" id="forward"> {{source}} to {{target}}</span>
                        </div>
                        <br>
                        <div class="inlinerows">
                            <span class="span2 no-margin">&nbsp;</span>
                            <span class="span4  no-margin">
								<input type="radio" name="link-direction" id="reverse">
								{{target}} to {{source}}</span>
                        </div>
                        <br>
                        <div class="inlinerows">
                            <span class="span2 no-margin">&nbsp;</span>
                            <span class="span4  no-margin">
								<input type="radio" name="link-direction" id="dynamic">
								Dynamic</span>
                        </div>
                        <br clear="all">
                        <br clear="all">
                        <div id="dynamic-div">
                            <div class="inlinerows">
                                <span class="span4 no-margin rightText">Direction Field</span>

                                <select class="span3" id="direction-field">
                                </select>

                            </div>
                            <div class="inlinerows">
                                <span class="span4 no-margin rightText">{{source}} to {{target}}</span>

                                <select id="dynamic-forward{{linkid}}" data-placeholder="Click to Select" multiple
                                        class="chzn-select  chzn-ltr span3" tabindex="10">
                                </select>

                            </div>
                            <div class="inlinerows">
                                <span class="span4 no-margin rightText">{{target}} to {{source}} </span>

                                <select data-placeholder="Click to Select" multiple class="chzn-select  chzn-ltr span3"
                                        id="dynamic-reverse{{linkid}}">
                                </select>

                            </div>
                        </div>
                    </div>
                    <br clear="all">

                </div>

            </div>

            <div class="modal-footer">
                <div style="float: right">
                    <a id="treeFinish" class="btn btn-inverse">Update</a>
                    <a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
                </div>
            </div>


            <div class="modal hide" id="link-linkups-popup" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
                 aria-hidden="true">
                <h3 class="box_head" id="myModalLabel">Edit Linkup
                    <button type="button" class="close modelClose"
                            onclick="$(this).parents('.modal').eq(0).modal('hide')" aria-hidden="true">
                        <img src="../images/addlayout/close.png" width="18" height="18">
                    </button>
                </h3>

                <div class="modal-body">
                    <p>
                        <span class="span1">Link to </span><select name="" class="span4 floatLt"
                                                                   id="dvList-links"></select>
                        <button class="btn floatRt " id="updateFields-links">
                            Update Fields
                        </button>
                    </p>
                    <div class="inlinerows">
                        <span class="span2">Source Field*</span><select name="" class="span3"
                                                                        id="sourceField-links"></select>
                    </div>

                    <div class="inlinerows">
                        <span class="span2">Target Field*</span><select name="" class="span3"
                                                                        id="targetField-links"></select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-inverse" id="updateLink-links">
                        Update
                    </button>
                    <button class="btn" aria-hidden="true" id="cancelUpdate-links"
                            onclick="$(this).parents('.modal').eq(0).modal('hide')">
                        Cancel
                    </button>

                </div>
            </div>
        </div>
    </script>
    <!-------------------------- Link edit modal popup end ----------------------------------------------------->

    <!------------------------------Context Menu for Canvas-->
    <script type="text/html" id="context-menu-template">
        <menu id="contextMenuTemplate{{vizuuid}}" type="context" style="display:none">
            <menuitem class="selectAll" label="Select All"></menuitem>
            <menuitem class="deselectAll" label="Deselect All"></menuitem>
            <menuitem class="hideSelection" label="Hide Selection" disabled></menuitem>
            <menuitem class="hideUnselected" label="Hide Unselected " disabled></menuitem>
            <menuitem class="bundle" label="Bundle Selected " disabled></menuitem>
        </menu>
    </script>
    <!-----------------------------Context Menu for Canvas end-->

    <!------------------------------Context Menu for Node-->
    <script type="text/html" id="context-menu-template-Node">
        <menu id="contextMenuTemplateNode{{vizuuid}}" type="context" style="display:none">
            <menuitem class="nodeSelect" label="Select"></menuitem>
            <menuitem class="nodeDeselect" label="Deselect" disabled></menuitem>
            <menuitem class="nodeHideSelect" label="Hide Selection" disabled></menuitem>
            <menuitem class="showOnly" label="Show Only"></menuitem>
            <menuitem class="bundle" label="Bundle Selected" disabled></menuitem>
            <menuitem class="unbundle" label="Unbundle" disabled></menuitem>
            <menuitem class="revealNeighbor" label="Reveal Neighbors" disabled></menuitem>
            <menuitem class="selectNeighbor" label="Select Neighbors" disabled></menuitem>
        </menu>
    </script>
    <!-----------------------------Context Menu for Node end-->

    <!------------------------------Context Menu for Link-->
    <script type="text/html" id="context-menu-template-Link">
        <menu id="contextMenuTemplateLink{{vizuuid}}" type="context" style="display:none">
            <menuitem class="linkSelect" label="Select"></menuitem>
            <menuitem class="linkDeselect" label="Deselect" disabled></menuitem>
            <menuitem class="linkHideSelect" label="Hide Selection" disabled></menuitem>
        </menu>
    </script>
    <!-----------------------------Context Menu for Link end-->

    <script type="text/html" id="empty-panel">
        <div id="layout{{index}}_panel{{position}}" class="box grad_colour_dark_blue chart_table vizPanels" style="">
            <div class="toggle_container" style="height: 100%">
                <div id="layout{{index}}_chart{{position}}" style="text-align: center; width: 100%;">
                    <input type="button" class="btn btn-large btn-primary addVisualization" value="Add a Visualization"
                           style="margin-top: 100px;"
                           onclick="$('#new-visualization-dialogue').data('parent', $(this).parent().attr('id')); $('#new-visualization-dialogue').data('position', '{{position}}');$('#new-visualization-dialogue').data('worksheetUuid', '{{worksheetUuid}}');$('#new-visualization-dialogue').modal();"/>
                    <span class="vLayoutPosition" style="display:none">1</span>
                </div>
            </div>
        </div>
    </script>


    <!----------------------------- Timeline template --------------------->
    <script type="text/html" id="graph-time-tab-template">
        <div id="graph-time-player-tab{{vizuuid}}" class="time-player panel-time-player">
            <div id="start-option-div{{vizuuid}}" class="inlinerows start-option-div">
                <div class="grey_bg alert-block">
                    <span class="floatLt">&nbsp;&nbsp;Start:&nbsp;&nbsp;</span>
                    <select class="span3" id="start-option{{vizuuid}}">
                    </select>
                    <div id="timespan-duration{{vizuuid}}" style="display:none;">
                        <span class="floatLt">&nbsp;&nbsp;End:&nbsp;&nbsp;</span>
                        <select id="end-option{{vizuuid}}" class="span3">
                        </select>
                        <span id="duration{{vizuuid}}" class="floatLt">&nbsp;&nbsp;Duration:&nbsp;&nbsp;</span>
                        <input type="number" class="span1" style="float: left;" value="1" id="duration-value{{vizuuid}}"
                               name="quantity" min="1" max="1000" style="visibility:hidden;">&nbsp;&nbsp;
                        <select id="select-duration{{vizuuid}}" class="span3">
                        </select>
                    </div>
                </div>
            </div>
            <div class="more-option-block" id="more-option-block{{vizuuid}}" style="display:block;">
                <ul class="nav nav-tabs inlinerows">
                    <li class="active"><a href="#timeplayer-tab-mode{{vizuuid}}" data-toggle="tab">Mode</a></li>
                    <li><a href="#timeplayer-tab-step{{vizuuid}}" data-toggle="tab">Step Size</a></li>
                    <li><a href="#timeplayer-tab-speed{{vizuuid}}" data-toggle="tab">Speed</a></li>
                </ul>
                <div class="tab-content">
                    <div id="timeplayer-tab-mode{{vizuuid}}" class="tab-pane active">
                        <input name="time-span" type="radio" value="" id="cumulative{{vizuuid}}" checked
                               class="floatLt">
                        <span class="floatLt">&nbsp;Cumulative</span>
                        <input name="time-span" type="radio" value="" id="time-span{{vizuuid}}" class="floatLt">
                        <span class="floatLt">&nbsp;Time Span </span>
                        <div id="timespan-element{{vizuuid}}" style="display:none;">
                            <input name="" type="checkbox" value="" class="floatLt" id="hide-inactive-items{{vizuuid}}">
                            <span class="floatLt">&nbsp;Hide Inactive Items</span>
                            <span class="floatLt">&nbsp;&nbsp;&nbsp;Time Span Size&nbsp;</span>
                            <input type="number" class="span1" style="float: left;" value="1"
                                   id="timespan-value{{vizuuid}}" name="quantity" min="1" max="1000">&nbsp;&nbsp;
                            <select id="time-span-size-select{{vizuuid}}" class="span2">
                            </select>
                        </div>
                    </div>
                    <div class="tab-pane" id="timeplayer-tab-step{{vizuuid}}">
                        <span class="floatLt">&nbsp;&nbsp;Step by:&nbsp;&nbsp;</span>
                        <input type="number" class="span1" style="float: left; display: block;"
                               id="stepsize-value{{vizuuid}}" value="1" name="quantity" min="1" max="1000">&nbsp;&nbsp;
                        <select id="timeplayer-tab-step-select{{vizuuid}}" class="span3">

                        </select>
                    </div>
                    <div id="timeplayer-tab-speed{{vizuuid}}" class="tab-pane">
                        <input id="timeplayer-tab-speed-slow{{vizuuid}}" type="radio" class="floatLt" value=""
                               name="tab-speed"> <span class="floatLt">&nbsp;Slow</span>
                        <input id="timeplayer-tab-speed-moderate{{vizuuid}}" type="radio" class="floatLt" value=""
                               name="tab-speed"><span class="floatLt">&nbsp;Moderate </span>
                        <input id="timeplayer-tab-speed-fast{{vizuuid}}" type="radio" class="floatLt" value=""
                               name="tab-speed" checked><span class="floatLt">&nbsp;Fast</span>
                    </div>

                </div>

            </div>
        </div>
    </script>
    <!----------------------------- Timeline template end ----------------->


    <!----------------------------- Graph Search Attribute Edit Popup template Start --------------------->
    <script type="text/html" id="graph-search-attribute-edit-popup-template">
        <div id="graph-search-edit-modal{{graphSearchName}}" class="modal hide" style="width:650px;"
             name={{graphSearchName}}>
            <h3 class="box_head" id="myModalLabel">Edit criteria for the '{{attrName}}' attribute
                <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                    <img src="../images/addlayout/close.png" width="18" height="18">
                </button>
            </h3>

            <div class="modal-body graph-search-edit-modal-body">
                <div class="inlinerows">
				<span class="inlinerows" id="heading">
					Find '{{nodeName}}' nodes where the '{{attrName}}' attribute matches all of the following:
				</span>
                    <div class="inlinerows attr-criterion">

                    </div>
                </div>
            </div>

            <div class="modal-footer">
			<span class="inlinerows">
				<button class="btn btn-block btn-primary" type="button" id="add-criteria">
					Add Criteria
				</button>
			</span>
                <div style="float: right">
                    <a id="treeFinish" class="btn btn-inverse">Update</a>
                    <a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
                </div>
            </div>
        </div>

    </script>
    <!----------------------------- Graph Search Attribute Edit Popup template End --------------------->

    <!----------------------------- Graph Search Criterion Section template Start --------------------->
    <script type="text/html" id="graph-search-criterion-template">
        <div class="bordered_block_large graph-search-criterion">
            <div class="inlinerows">
							<span class="span2 no-margin">
								<select class="input-small" id="operator">
								<option value="EQUALS">equals</option>
								<option value="BEGINS_WITH">starts with</option>
								<option value="CONTAINS">contains</option>
								<option value="ENDS_WITH">ends with</option>
								<option value="LT">is less than</option>
								<option value="LEQ">is less than or equal to</option>
								<option value="GT">is greater than</option>
								<option value="GEQ">is greater than or equal to</option>
								<option value="ISNULL">is null</option>
								<option value="EMPTY">is empty</option>
								<option value="NULL_OR_EMPTY">is null or empty</option>
							</select></span>
                <span class="span3 no-margin">
								<div class="borderedBlock inlinerows padding-small" id="value">
									<span class="inlinerows">
										<button class="btn btn-block btn-inverse" type="button" id="add-value">
											Add Value
										</button> </span>
								</div> </span>
                <span class="span2">
								<input type="checkbox" class="floatLt" id="exclude"/>
								&nbsp;Exclude </span>
                <a class="floatLt"><img src="../images/node-delete.png" width="16" height="16" id="delete"></a>
            </div>
        </div>
    </script>
    <!----------------------------- Graph Search Criterion Section template End --------------------->

    <script type="text/html" id="graph-search-value-list-item">
        <span class="inlinerows"><input type="text" class="input-block-level" style="width : 175px;"/> <a
                class="floatRt"> <img src="../images/node-delete.png" width="16" height="16"
                                      id="delete-val"> </a> </span>
    </script>


    <div id="chart-bundle-editor-modal" class="modal hide small_modal">
        <h3 class="box_head" id="myModalLabel">
            Bundle Editor
            <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                <img src="../images/addlayout/close.png" width="18" height="18">
            </button>
        </h3>

        <div class="modal-body">

            <div class="inlinerows">
                <input name="" type="radio" value="" class="floatLt">
                <span class="span3 no-margin">&nbsp;&nbsp;Do not bundle</span>
            </div>

            <div class="inlinerows">
                <input name="" type="radio" value="" class="floatLt">
                <span class="span3 no-margin">&nbsp;&nbsp;Select bundling criteria:</span>
            </div>

            <div class="inlinerows">
                <select name="" multiple class="input-block-level">
                    <option>Bundle by rounding up</option>
                    <option>Bundle by rounding down</option>
                </select>
            </div>

        </div>

        <div class="modal-footer">
            <div style="float: right">
                <a id="treeFinish" href="#" class="btn btn-inverse">OK</a>
                <a href="#" class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
            </div>
        </div>
    </div>

    <!----------------- Chart sorting bundle template --------------------->


    <!------------------ Chart sorting modal template ---------------------->
    <div id="chart-sort-modal" class="modal hide small_modal">
        <h3 class="box_head" id="myModalLabel"> Chart Sorting
            <button type="button" class="close modelClose" data-dismiss="modal" aria-hidden="true">
                <img src="../images/addlayout/close.png" width="18" height="18">
            </button>
        </h3>

        <div class="modal-body">
            <form>
                <div class="inlinerows">
                    <input name="sort-type" type="radio" value="" class="floatLt" checked>
                    <span class="span3 no-margin"><strong>&nbsp;&nbsp;Sort on Categories</strong></span>
                    <br clear="all"/>
                    <div id="chartSortCategories">

                    </div>

                </div>

                <div class="inlinerows">
                    <input name="sort-type" disabled type="radio" value="" class="floatLt">
                    <span class="span3 no-margin"><strong>&nbsp;&nbsp;Sort on Measure</strong></span>
                    <br clear="all"/>

                    <div class="inlinerows">
						<span class="span4">Count of all&nbsp;&nbsp;
							<a class="asc-desc">
								<img src="../images/sorting.png" width="18" height="16">
							</a>
						</span>

                    </div>
                </div>

            </form>
        </div>

        <div class="modal-footer">
            <div style="float: right">
                <a id="sortDrillChart" class="btn btn-inverse"
                   onclick="$('#chart-sort-modal').modal('hide');sortDrillChart($('#chart-sort-modal').data('vizuuid'), $('#chart-sort-modal').data('vizIndex'), $('#chart-sort-modal').data('workSheetIndex'));">OK</a>
                <a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
            </div>
        </div>
    </div>
    <!------------------ Chart sorting modal template end ---------------------->
    <!------------------- Time Player warning template -------------------------->
    <div id="time-player-invalid-fields-warning" class="modal hide">
        <h2 class="box_head" id="myModalLabel">Warning</h2>
        <div style="padding: 0px 15px;" class="modal-body">
            <br>Invalid Range (<span id="startField">startField</span> - <span id="endField">endField</span>): <br>
            <p>Please make sure that you have proper date/time values in the selected fields </p>
            <p> and the Start field values are earlier than the End field values.</p>
            <div class="modal-footer" style="float: right">
                <a data-dismiss="modal" class="btn btn-inverse" id="">Ok</a>
            </div>
        </div>
    </div>
    <!------------------- Time Player warning template end-------------------------->

</body>
</html>
