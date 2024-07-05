<%
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Cache-Control", "no-cache" );
    response.setDateHeader( "Expires", 0 );
%>

<!DOCTYPE html>
<%--<!-- The DOCTYPE declaration above will set the     -->--%>
<%--<!-- browser's rendering engine into                -->--%>
<%--<!-- "Standards Mode". Replacing this declaration   -->--%>
<%--<!-- with a "Quirks Mode" doctype is not supported. -->--%>

<html>
  <head>
   
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Centrifuge</title>
    <link rel="stylesheet" type="text/css" href="./css/csi.css">
    <%--<!-- End block -->
    <!-- NOTE: Javascript libraries that are referenced via JSNI should be loaded here before the nocache.js -->
	<!-- D3 library -->
<!-- 	<script src="js/d3/d3.v3.min.js"></script> -->--%>
        <script type="text/javascript" language="javascript" src="js/ace/ace.js"></script>
        <script type="text/javascript" language="javascript" src="js/ace/theme-github.js"></script>
        <script type="text/javascript" language="javascript" src="js/ace/mode-sql.js"></script>
    
    <%--<!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->--%>
    <script type="text/javascript" language="javascript" src="csi/csi.nocache.js"></script>
    
  </head>

  <body>

<%--    <!-- OPTIONAL: include this if you want history support -->--%>
     <%--<!-- RECOMMENDED if your web app will not function without JavaScript enabled -->--%>
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>
  </body>
</html>