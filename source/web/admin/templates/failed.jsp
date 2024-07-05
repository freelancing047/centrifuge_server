<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="csi.server.business.service.InternationalizationService" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="java.util.Locale" %>
<%@page import="csi.server.business.service.InternationalizationService" %>
<%@page import="java.util.Map" %>


<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    InternationalizationService i18n = (InternationalizationService) context.getBean("csi.server.business.service.InternationalizationService");

    Map<String, String> properties = i18n.getProperties(request.getLocale());

    String loginTitle = properties.get("loginTitle");
    String loginButtonText = properties.get("loginButtonText");
    String loginFailedText = properties.get("loginFailedText");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link rel="stylesheet" href="../css/ionicons.min.css">
    <link href=' ../fonts/ionicons.woff' rel='stylesheet' type='text/css'>
    <title>Centrifuge - <%= loginTitle %>
    </title>
    <link href="../css/style.css" rel="stylesheet" type="text/css">
</head>

<body>
<div class="loginPage">
    <div class="loginWrap">
        <div class="logo">
            <h1>
                <img src="../images/logo.png" alt=""/>
            </h1>
        </div>
        <div class="formWrap">
            <div class="formTitle">
                <h3><%= loginTitle %>
                </h3>
            </div>
            <div class="inputWrap">
                <form id="loginform" name="loginform" method="post"
                      action="j_security_check">
                    <p class="alertBox"><%= loginFailedText %>
                    </p>
                    <label class="input-bg" style="margin:10px 22px 10px;">
                        <span class="icon ion-person loginIcons"></span>
                        <input class="username" id="j_username" name="j_username" type="input"/>
                    </label>

                    <label class="input-bg">
                        <span class="icon ion-key loginIcons"></span>
                        <input class="password" id="j_password" name="j_password" type="password"/>
                    </label>

                    <input class="loginbtn" type="submit" value="<%= loginButtonText %>"/>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
