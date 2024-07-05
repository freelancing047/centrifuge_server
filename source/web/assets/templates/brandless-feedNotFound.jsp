<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	    <link href="<%=request.getContextPath()%>/report.css" media="screen" rel="Stylesheet" type="text/css" />
	    <title>RSS Error</title>
	</head>
	<body>
	<h1 align="center">Centrifuge Server: Error Retrieving RSS Feed Item</h1>
        <p align="left">
            <%= request.getAttribute("error-message") %>
        </p>
        <div class="footer">
            <%@include file="/assets/footer.html"%>
        </div>
    </body>
</html>