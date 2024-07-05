<%@page import="csi.config.Configuration"%>
<%@page import="csi.config.RelGraphConfig"%>
<%@ page import="java.security.*" %>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
		<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate" />
		<META HTTP-EQUIV="Expires" CONTENT="-1" />
		<title>JavaScript API Samples</title>
		
		<script type="text/javascript" src="/Centrifuge/javascript/jquery.js"></script>
		<script type="text/javascript" src="/Centrifuge/javascript/centrifuge-widget.js"></script>
		<script type="text/javascript" src="/Centrifuge/javascript/jquery.cookie.js"></script>
		
		<link href="resources/prettify.css" type="text/css" rel="stylesheet" />
		<script type="text/javascript" src="resources/prettify.js"></script>
		<script type="text/javascript">
			var cookie;
			var dataviewOpened = false;
			$(function() {
				var userCookie = "dvUuid" + $("#username").val() ;
				//if the dataview cookie exists, disable the create dataview button
				if ( $.cookie(userCookie)) {
						$('#cookie').html('<img src="resources/Ok-icon.png" /> Dataview has been successfully loaded');
						$("#createExamplesDV").attr( "disabled", "disabled" );
						cookie = $.cookie(userCookie);
						dataviewOpened = true;
				} else {
						$('#cookie').html('<img src="resources/alert-icon.png" /> Dataview does not exist');
						$("#createExamplesDV").removeAttr("disabled");
				}
					
				$('#home, #csi-logo').click(function() {
					$('#content').load('/Centrifuge/samples/widgets #content div', function() {
						if ( $.cookie(userCookie)) {
							$('#cookie').html('<img src="resources/Ok-icon.png" /> Dataview has been successfully loaded');
							$("#createExamplesDV").attr( "disabled", "disabled" );
							cookie = $.cookie(userCookie);
						} else {
							$('#cookie').html('<img src="resources/alert-icon.png" /> Dataview does not exist');
							$("#createExamplesDV").removeAttr("disabled");
						}
					});
				});
				$('#open').click(function() {
					if(dataviewOpened){
						$('#content').load('openDataview.html', function() {
							prettyPrint();
						});
					}else{
						alert("Dataview does not exist, please create it first.");
						return false;
					}
				});
				$('#template').click(function() {
					$('#content').load('openTemplate.html', function() {
							prettyPrint();
					});
				});
				$('#template_params').click(function() {
					$('#content').load('openTemplateWithParameters.html', function() {
							prettyPrint();
					});
				});
				$('#reload').click(function() {
					if(dataviewOpened){
						$('#content').load('reloadDataview.html', function() {
							prettyPrint();
						});
					}else{
						alert("Dataview does not exist, please create it first.");
						return false;
					}
				});
				$('#filter_viz').click(function() {
					if(dataviewOpened){
						$('#content').load('filterVisualization.html', function() {
							prettyPrint();
						});
					}else{
						alert("Dataview does not exist, please create it first.");
						return false;
					}
				});
				$('#refresh_viz').click(function() {
					if(dataviewOpened){
						$('#content').load('refreshVisualization.html', function() {
							prettyPrint();
						});
					}else{
						alert("Dataview does not exist, please create it first.");
						return false;
					}
				});
				$('#broadcast_viz').click(function() {
					if(dataviewOpened){
						$('#content').load('broadcastVisualization.html', function() {
							prettyPrint();
						});
					}else{
						alert("Dataview does not exist, please create it first.");
						return false;
					}
				});
			});
			
			var exdv = new DataviewTemplate("feb5118f-8f14-4afc-8ea2-382e99a1c5e1", "Examples Dataview", "Centrifuge");
	
			function createExamplesDV() {
				$('#cookie').html('<img src="resources/ajax-loader.gif" /> Loading Dataview...');
				exdv.createDvFromTemplate(function(){
					var userCookieName = "dvUuid" + $("#username").val() ;
					cookie = exdv.dvUuid;
					$.cookie(userCookieName, exdv.dvUuid, { expires: 5 });
					$("#createExamplesDV").attr( "disabled", "disabled" );
					$('#cookie').html('<img src="resources/Ok-icon.png" /> Dataview has been successfully loaded');
					dataviewOpened = true;
				});
			}
			
			function getPretty() {
				alert('making pretty');
				// add prettyprint class to all <pre><code></code></pre> blocks
				var prettify = false;
				$("pre code").parent().each(function() {
					$(this).addClass('prettyprint');
					prettify = true;
				});

				// if code blocks were found, bring in the prettifier ...
				if ( prettify ) {
					$.getScript("resources/prettify.js", function() { prettyPrint() });
				}
			}
		</script>
		<style type='text/css'>
			#container
			{
			width: 90%;
			margin: 10px auto;
			background-color: #fff;
			color: #333;
			border: 1px solid gray;
			line-height: 130%;
			}

			#top
			{
			padding: .5em;
			background-color: #ddd;
			border-bottom: 1px solid gray;
			text-align: center;
			height: 80px;
			}
			
			#top img
			{
			float: left;
			}

			#top h1
			{
			padding: 0;
			margin: 0;
			}

			#leftnav
			{
			float: left;
			width: 18%;
			margin: 0;
			padding: 1em;
			height: 100%;
			}

			#content
			{
			margin-left: 20%;
			min-height: 1400px;
			border-left: 1px solid gray;
			padding: 1em;
			}

			#footer
			{
			clear: both;
			margin: 0;
			padding: .5em;
			color: #333;
			background-color: #ddd;
			border-top: 1px solid gray;
			}

			#leftnav p { margin: 0 0 1em 0; }
			#content h2 { margin: 0 0 .5em 0; }
			.copyright {
				color:#4D4D4D;
				font-family:Verdana,Geneva,sans-serif;
				font-size:10px;
			}
			.title {
				font-family: sans-serif;
				font-size: 3em;
				position: relative;
				top: 50%;
			}
		</style>
	</head>
	<body>
		<input id="username" type="hidden" value="<%= request.getUserPrincipal().getName() %>"/>
		<div id="container">	
			<div id="top">
				<a href="#" id="csi-logo"><img src="resources/centrifuge.png" border="0"></img></a>
				<span class="title">Centrifuge JavaScript API Samples</span>
			</div>
			<div id="leftnav">
				<p><a href="#" id="home">Home</a></p>
				<b>Dataview Samples</b>
				<ol>
					<li><a href="#" id="open">Open Dataview</a></li>
					<li><a href="#" id="reload">Reload Dataview</a></li>
					<li><a href="#" id="template">Create Dataview from Template</a></li>
					<li><a href="#" id="template_params">Create Dataview from Template w/Parameters</a></li>
				</ol>
				<b>Visualization Samples</b>
				<ol>
					<li><a href="#" id="filter_viz">Filter Visualization</a></li>
					<li><a href="#" id="broadcast_viz">Broadcast Visualization</a></li>
					<li><a href="#" id="refresh_viz">Refresh Visualization</a></li>
				</ol>
			</div>
			<div id="content">
				<div>
					<h2>Welcome to the Centrifuge JavaScript API Samples and Documentation Application</h2>
					<p>On the left hand margin of this application you will see some navigation links for you to explore some sample implementation scenarios.  These scenarios are meant to give an idea of the types of applications that you can build with this API, but it is by no means an exhaustive list.  Please feel free to be creative and make suggestions on how we can make this API better by sending an email to support@centrifugesystems.com</p>
					<p>Before you get started, some of the examples rely on some additional files to work properly.  Please click on the button below to load the required files.</p>
				</div>
				<div>
					<button id="createExamplesDV" onClick="createExamplesDV()" disabled="disabled">Click to Create Dataview for Examples</button> <span id="cookie"></span>
				</div>
			</div>
			<div id="footer">
				<span class="copyright">Copyright 2012 Centrifuge Systems, Inc. All rights reserved.</span>
			</div>
		</div>
	</body>
</html>