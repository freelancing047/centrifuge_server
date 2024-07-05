<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="csi.server.business.service.InternationalizationService" %>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="java.util.Locale"%>
<%@page import="csi.server.business.service.InternationalizationService"%>
<%@page import="java.util.Map"%>
<%
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Cache-Control", "no-cache, no-store, must-revalidate" );
    response.setDateHeader( "Expires", 0 );
%>
<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    InternationalizationService i18n = (InternationalizationService) context.getBean("csi.server.business.service.InternationalizationService");
            
    Map<String, String> properties = i18n.getProperties(request.getLocale());

    String loginTitle = properties.get("loginTitle");
    String loginButtonText = properties.get("loginButtonText");
%>

<html>
	<head>
		<link rel="stylesheet" href="../css/ionicons.min.css">
		<link href=' ../fonts/ionicons.woff' rel='stylesheet' type='text/css'>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Centrifuge - <%= loginTitle %></title>
		<link href="../css/login.css" rel="stylesheet" type="text/css">
		<script language="Javascript">
			function capLock(e){
				kc = e.keyCode?e.keyCode:e.which;
				sk = e.shiftKey?e.shiftKey:((kc === 16));
				if(((kc >= 65 && kc <= 90) && !sk)||((kc >= 97 && kc <= 122) && sk))
					document.getElementById('caps').style.visibility = 'visible';
				else
					document.getElementById('caps').style.visibility = 'hidden';
			}
		</script>
	</head>

	<body OnLoad="document.loginform.j_username.focus();">
		<div class="background">
		    <div class="topHalf">
		    </div>
		    <div class="bottomHalf">
		    </div>
        </div>
		<div class="loginPage">
			<div class="loginWrap">
				<div class="logo">
					<h1>
						<img src="../images/login_logo.png" alt="" />
					</h1>
				</div>
				<div class="formWrap">
					<div class="formTitle">
						<h3><%= loginTitle %></h3>
					</div>
					<div class="inputWrap">
						<form id="loginform" name="loginform" method="post"
							action="j_security_check">

							<p id="caps" class="alertBox" style="visibility:hidden">Caps Lock is on</p>

							<label class="input-bg">
							  <img src="../images/user.png" alts="" />
							  <input class="username" id="j_username" name="j_username" type="input" placeholder="Username"/>
							</label>
							<img src"../images/line2.png" />
							<label class="input-bg" style="margin:10px 22px 10px;">
							  <img src="../images/key-2.png" alts="" />
							  <input class="password" id="j_password" name="j_password" type="password" placeholder="Password" onkeypress="capLock(event)"/>
							</label>
							<input class="loginbtn" type="submit" value="<%= loginButtonText %>" />
						</form>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
