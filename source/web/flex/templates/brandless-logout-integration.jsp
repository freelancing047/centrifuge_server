<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-cache" );
   response.setDateHeader( "Expires", 0 );
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>

	<SCRIPT language="JavaScript">
    
        // Enter Over Image
        pic1= new Image(315,65); 
        pic1.src="<%=request.getContextPath()%>/admin/img/landingLoginOver.png"; 
        
    </SCRIPT>

	<style>
		body
		{
		margin:0;
		overflow:hidden;
		background-color: #277DAA;
		background-repeat: no-repeat;
		}
		
		.loginButton
		{
		height: 65px;
		width: 315px;
		background-image:url(<%=request.getContextPath()%>/admin/img/landingLoginUp.png);
		border-top-width: 0px;
		border-right-width: 0px;
		border-bottom-width: 0px;
		border-left-width: 0px;
		border-top-style: none;
		border-right-style: none;
		border-bottom-style: none;
		border-left-style: none;
		}
		
		.loginButton:hover
		{
		background-image:url(<%=request.getContextPath()%>/admin/img/landingLoginOver.png);
		}

	</style>

</head>

<body>

<%session.invalidate();%>

<center>
	 <br /> <br /> 
     <font face="Arial, Helvetica, sans-serif" color="#FFFFFF" size="3"><strong>You have logged out.</strong></font> <br /> <br />
  <a href="../flex/index.jsp" target="_self"><div class="loginButton"></div></a>
</center>

</body>
</html>
