<%@ page import="csi.server.business.service.InternationalizationService" %>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="java.util.Locale"%>
<%@page import="csi.server.business.service.InternationalizationService"%>
<%@page import="java.util.Map"%>


<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    InternationalizationService i18n = (InternationalizationService) context.getBean("csi.server.business.service.InternationalizationService");
            
    Map<String, String> properties = i18n.getProperties(request.getLocale());

    String logoutTitle = properties.get("logoutTitle");
    
    String logoutMessageText = properties.get("logoutMessageText");
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Centrifuge - <%= logoutTitle %></title>
<link href="../css/style.css" rel="stylesheet" type="text/css">
</head>

<body>
	<div class="loginPage">
		<div class="loginWrap">
			<div class="logo">
				<h1>
					<img src="../images/logo.png" alt="" />
				</h1>
			</div>
			<div class="formWrap">
				<div class="formTitle">
					<h3><%= logoutTitle %></h3>
				</div>
				<div class="loginLink">
					<a href="/Centrifuge"><%= logoutMessageText %></a>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
<%
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Cache-Control", "no-cache" );
    response.setDateHeader( "Expires", 0 );
    session.invalidate();
%>
