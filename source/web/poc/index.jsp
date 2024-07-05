<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Centrifuge</title>
<link href="../css/style.css" rel="stylesheet" type="text/css">
<link href="../css/bootstrap.css" rel="stylesheet">
<link href="../css/custom.css" rel="stylesheet">
<link href="../css/jquery.dataTables.css" rel="stylesheet">
<link href="../css/demo_table.css" rel="stylesheet">
<!-- For tree view -->
<link href="../css/dynatree/skin-vista/ui.dynatree.css" rel="stylesheet" />

<script src="../javascript/jquery-1.8.1.js"></script>
<script src="../javascript/jquery-ui-1.8.23.custom.min.js"></script>
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
<script src="../javascript/xml2json.js"></script>
<script src="../javascript/jquery.dataTables.js"></script>
<script src="../javascript/jquery.form.js"></script>
<!-- For tree view -->
<script src="../javascript/jquery.cookie.js"></script>
<script src="../javascript/jquery.dynatree.min.js"></script>
<script src="../javascript/utils.js"></script>
<script src="../javascript/excel-json.js"></script>
<script src="../javascript/load.js"></script>
<script src="../javascript/dataview-excel-json.js"></script>
</head>
<body>
<script type="text/javascript">
	$(document).ready(function() {
		utils = new Utils();
		load = new Load();
		load.doLoad();
		$.data(document.body, 'load', load);
	});


</script>
	<div class="landingPage">
		<div class="logout">
			<p class="welcome">Welcome <%= request.getUserPrincipal().getName() %></p>
			<a id ="log-out" ><img src="../images/logout.png" alt="logout" class="logoutBtn" /></a>
		</div>
    	<div class="popupWrap" style="display:block; margin-top: 100px;">
        	<div class="popup_panel">
				<div class="colby2 right_bordered">
					<h2 class="box_head panel_head">Getting Started</h2>
					<div class="container">
						<div class="container">
							<div class="container-fluid"  onclick="$('#myModal1').modal();" style="cursor: pointer">
								<b>New</b>
								<p>
									<img src="../images/new.png" width="33" height="34" align="left">Identify your data source or upload your own file for analysis
								</p>
							</div>
							<div class="container-fluid"   onclick="$('#myModal2').modal();" style="cursor: pointer">
								<b>New From Template</b>
								<p>
									<img src="../images/new2.png" width="33" height="34" align="left">Identify your data source or upload your own file for analysis
								</p>
							</div>
							<div class="container-fluid" onclick="$('#myModal3').modal();" style="cursor: pointer;">
								<b>Open</b>
								<p>
									<img src="../images/open.png" width="33" height="34" align="left">Identify your data source or upload your own file for analysis
								</p>
							</div>
							<div class="container-fluid">
								 <b>Explore Samples</b>
								<p>
									<img src="../images/explore.png" width="33" height="34" align="left">Identify your data source or upload your own file for analysis
								</p>
							</div>
						</div>

					</div>

				</div>
				<div class="colby2">
					<h2 class="box_head panel_head">Recently Opened</h2>
					<div id="container-scrollpane" class="container scrollpane">
					</div>
				</div>
				<footer>
					<br clear="all"/>
					<p>
						&copy; Centrifuge 2012
					</p>
				</footer>

			</div>
        </div>
    </div>
    
    <div class="modal hide" id="myModal3">
		<h3 id="myModalLabel" class="box_head">Dataviews</h3>

		<div class="modal-body">
			<p>
				Content will be loaded here...
			</p>
		</div>
		<div class="modal-footer">
			<button class="btn" data-dismiss="modal" aria-hidden="true">
				Close
			</button>
		</div>
	</div>
    
    <div class="modal hide" id="myModal1" >
		<h3 class="box_head" id="myModalLabel">Create New Dataview</h3>
	
		<p class="transitionBar">
			Enter a name
		</p>
		<div class="modal-body">
				<form class="form-horizontal">
					<div class="control-group">
						<label class="control-label" for="dvName">Name*</label>
						<div class="controls" id="div-dataview">
							<input type="text" id="dvName" placeholder="Untitled Dataview" style="width: 90%;" autocomplete="off" />
						</div>
					</div>
					<div class="control-group" >
						<div class="controls">
							<div>
								<button id= "next1" type="button" class="btn btn-inverse" disabled style="margin-left: 120px;" onclick="$('#myModal1').modal('hide'); $('#myModal6').modal();">
									Next
								</button>
								<div style="float: right">
									<a class="btn btn-inverse" disabled>Finish</a>
									<a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
								</div>
							</div>
						</div>
					</div>
				</form>
		</div>
		<div class="modal-footer">
			Clicking 'Finish' will open the data source editor for further editing
		</div>
	</div>
    
    
    
    
    
<div class="modal hide" id="myModal2" >
		<h3 id="myModalLabel" class="box_head"> New Dataview  From Template</h3>

		<div class="modal-body">
			<form class="form-horizontal">
				<div class="control-group">
					<label class="control-label" for="inputEmail">Name*</label>
					<div class="controls">
						<input type="text" id="inputEmail" placeholder="Email">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="inputPassword">Templates*</label>
					<div class="controls">
						<select name="" multiple>
							<option>Template1</option>
							<option>Template2</option>
							<option>Template3</option>
							<option>Template4</option>
							<option>Template5</option>
						</select>

					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="inputPassword">Template Description*</label>
					<div class="controls">
						<textarea name="" cols="" rows="" placeholder="Enter description here..."></textarea>							
 

					</div>
				</div>
				<div class="control-group">
					<div class="controls">
						<label class="checkbox">
							<input type="checkbox">
							Continue configuring data sources. </label>

					</div>
					<div style="float: right">
						<a class="btn btn-inverse">OK</a>
						<a  class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
					</div>
				</div>
			</form>
		</div>
	</div>

	<div class="modal hide" id="myModal6">
		<h3 class="box_head" id="myModalLabel">Create New Dataview</h3>
		<p class="transitionBar">
			Choose a datasource.
		</p>
		<div class="modal-body">
			<form class="form-horizontal">
				<div class="control-group">
					<label class="control-label" for="inputEmail">ConnectionType*</label>
					<div class="controls">
						<select class="span4" >
							<option>Microsoft Access</option>
							<option selected="selected">Microsoft Excel</option>
							<option>Microsoft SQL Server (jTDS)</option>
							<option>Oracle Thin</option>
							<option>PostgreSQL</option>
						</select>
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="inputEmail">File*</label>
					<div class="controls">
							<input type="text" id="inputExcel" placeholder="Choose an Excel file" class="span3 uneditable-input" readonly/>
							<input type="hidden" id="inputExcelToken" class="span3 uneditable-input" />
						<button type="button" onclick="$('#myModal7').modal();">Browse</button>
					</div>
				</div>
				
				<div class="control-group">
					<div class="controls">
						<div>
							<div style="margin-left: 80px; float: left;">
								<button type="button" class="btn btn-inverse" onclick="$('#myModal6').modal('hide'); $('#myModal1').modal();">
									Previous
								</button>
								<button id="next2" type="button" class="btn btn-inverse" disabled onclick="$('#myModal6').modal('hide'); $('#myModal8').modal();">
									Next
								</button>
							</div>
							
							<div style="float: right">
								<a  class="btn btn-inverse" disabled>Finish</a>
								<a  class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
		<div class="modal-footer">
			Clicking 'Finish' will open the data source editor for further editing
		</div>
	</div>
	
	<div class="modal hide" id="myModal7" style="width: 700px;">
		<h3 class="box_head" id="myModalLabel">Choose File</h3>
		<div class="modal-body" style="max-height: none;">
			<div class="control-group">
				<label class="radio">
					<input type="radio" name="optionsRadios" id="optionsRadios1" value="option1" checked="checked">
					Upload local file 
				</label>
			</div>
            <form id="fileUpload" action="" method="post" enctype="multipart/form-data" >
                <div class="control-group">
                    <input type="hidden" name="filename" id="fileName"/>
                    <input type="hidden" name="user" value="<%= request.getUserPrincipal().getName() %>" />
                    <input type="hidden" name="path" value="datafiles" />
                    <input type="hidden" name="overwrite" value="false" />
                    <input type="file"   name="file" class="span4" id="file">                    
                </div>
            </form>
			<div class="control-group">
				<label class="radio">
					<input type="radio" name="optionsRadios" id="optionsRadios2" value="option2">
					Use previously uploaded file.
				</label>
			</div>
			<div id ="xls" class="control-group scroll-xls-list">
				
			</div>
			<div style="float: right">
				<a class="btn btn-inverse" onclick="load.getSelectedXLS()" disabled id="selectButton">Select</a>
				<a  class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
			</div>
		</div>
	</div>
	<div id="myModal8" class="modal hide">
		<h3 class="box_head" id="myModalLabel">Create New Dataview</h3>
		<p class="transitionBar">Choose a data source worksheet</p>
		<div class="modal-body">
			<div id="dataSourceTree">
			
			</div>
			<div class="worksheet">
				Selected worksheet:
				<span id="worksheetName">&lt;None&gt;</span>
			</div>
			<div class="control-group">
				<div class="controls">
					<label class="checkbox">
						<input type="checkbox" value="">
						Continue configuring data sources
					</label>
				</div>
			</div>
			<div class="control-group">
				<div class="controls">
					<div>
						<button type="button" class="btn btn-inverse" style="margin-left: 220px;" onclick="$('#myModal8').modal('hide'); $('#myModal6').modal();">
							Previous
						</button>
						<div style="float: right">
							<a id="treeFinish"  class="btn btn-inverse" disabled>Finish</a>
							<a class="btn btn-inverse" data-dismiss="modal" class="close">Cancel</a>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="modal-footer">
			Clicking 'Finish' will open the data source editor for further editing
		</div>
		
	</div>
	<form id="open-data-view" action="dataview.jsp">
	      <input type="hidden" name="uuid" id="uuid" />
    </form>
</body>
</html>