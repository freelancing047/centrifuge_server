<%@ page import="csi.server.util.BuildNumber" %>
<%
   response.addHeader("X-Auth-Required", "true");
   response.addHeader("P3P","CP=\"STA\"");
%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
 
<!-- Preload Buttons -->
 <SCRIPT language="JavaScript">
    
        // Enter Over Image
        pic1= new Image(315,48); 
        pic1.src="<%=request.getContextPath()%>/admin/img/loginButtonAltOver.png"; 
        
    </SCRIPT>
    
    <style>
 body
{
 
  
 background-color: #277DAA;
 
}

 .backDiv
{
 height: 100%;
 width: 100%;
 position: absolute;
 left: 0px;
 top: 0px;
 right: 0px;
 bottom: 0px;
 z-index: 0;
 overflow: hidden;
}
  
 .backImage
{
 z-index: 0;
 position: absolute;
 left: 0px;
 top: 0px;
 right: 0px;
 bottom: 0px;
 height: 100%;
 width: 100%;
}

 .backGlobeDiv
{
 position: absolute;
 bottom: 0px;
 width: 100%;
 background-attachment: fixed;
 background-image: url(<%=request.getContextPath()%>/admin/img/backGlobe.png);
 background-repeat: no-repeat;
 background-position: center bottom;
 height: 100%;
 left: 0px;
 top: 0px;
 right: 0px;
}

 .backGlobe
{
 height: 591px;
 width: 1437px;
}

 .backGraphDiv
{
 position: absolute;
 bottom: 0px;
 width: 100%;
 background-attachment: fixed;
 background-image: url(<%=request.getContextPath()%>/admin/img/graphBack.png);
 background-repeat: no-repeat;
 background-position: center bottom;
 height: 100%;
 left: 0px;
 top: 0px;
 right: 0px;
}

 .backGraph
{
 height: 591px;
 width: 956px;
}

 .baseDiv
{
 position: absolute;
 left: 0px;
 top: 0px;
 width: 100%;
 right: 0px;
 bottom: 30%;
}

 .baseTopBack
{
 width: 100%;
 height: 100%;
 position: absolute;
 left: 0px;
 top: 0px;
 right: 0px;
 bottom: 0px;
}

 .logoDiv
{
 position: absolute;
 top: 10px;
 width: 100%;
 background-image: url(<%=request.getContextPath()%>/admin/img/banner.gif);
 background-repeat: no-repeat;
 background-position: center top;
 height: 217px;
 left: 0px;
 right: 0px;
}

 .logo
{
 height: 217px;
 width: 1012px;
}

 .baseShadowDiv
{
 width: 100%;
 position: absolute;
 top: 0px;
 left: 0px;
 right: 0px;
 bottom: 70%;
 height: 70%;
}

 .baseShadow
{
 width: 100%;
 height: 10px;
 left: 0px;
 right: 0px;
 bottom: -10px;
 position: absolute;
 background-image: url(<%=request.getContextPath()%>/admin/img/loginBaseShadow.png);
 background-repeat: repeat-x;
 top: 100%;
}
 
 .loginContentDiv
{
 width: 100%;
 position: absolute;
 bottom: 10px;
 left: 0px;
 top: 0px;
 right: 0px;
}

 .loginTable
{
 position: absolute;
 left: 0px;
 right: 0px;
 bottom: 0px;
}

 .loginFormBack
{
 width:315px;
 height:83px;
 background-image: url(<%=request.getContextPath()%>/admin/img/formBack.png);
 background-repeat: no-repeat;
}

 .loginFormTable
{
 width: 315px;
 font-family: Arial, Helvetica, sans-serif;
 font-size: 12px;
 color: #333333;
}

 .loginFormText
{
 height: 18px;
 width: 157px;
 background-image: url(<%=request.getContextPath()%>/admin/img/inputBack.png);
 background-repeat: repeat;
 border-top-width: 0px;
 border-right-width: 0px;
 border-bottom-width: 0px;
 border-left-width: 0px;
 border-top-style: none;
 border-right-style: none;
 border-bottom-style: none;
 border-left-style: none;
 padding-top: 3px;
 padding-left: 3px;
}

 .errorMessageDiv
{
 height:31px;
 windows:100%;
 font-family: Arial, Helvetica, sans-serif;
 font-size: 10px;
 color: #FF0000;
 font-style: italic;
 background-image:url(<%=request.getContextPath()%>/admin/img/errorBack.png);
 visibility:visible;
 overflow: hidden;
 width: 315px;
}

 .loginFormDivide
{
 height: 2px;
 width: 315px;
 background-image: url(<%=request.getContextPath()%>/admin/img/formDivide.png);
 background-repeat: no-repeat;
 position: absolute;
 left: 0px;
 right: 0px;
 bottom: 48px;
 background-position: center bottom;
 top: 0px;
 background-attachment: fixed;
}

 .loginButton
{
 height: 50px;
 width: 315px;
 background-image:url(<%=request.getContextPath()%>/admin/img/loginButtonAltUp.png);
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
 background-image:url(<%=request.getContextPath()%>/admin/img/loginButtonAltOver.png);
}

 .versionDiv
{
 font-family: Arial, Helvetica, sans-serif;
 font-size: 10px;
 font-style: normal;
 font-weight: bold;
 color:#FFFFFF;
 font-weight: normal;
 position: absolute;
 right: 0px;
}

 .copyRightDiv
{
 width: 100%;
 font-family: Arial, Helvetica, sans-serif;
 font-size: 10px;
 font-style: normal;
 font-weight: bold;
 color: #FFFFFF;
 left: 0px;
 right: 0px;
}
 </style>
    
    <!-- Preload Buttons -->
 <SCRIPT language="JavaScript">
    
        // Enter Over Image
        pic1= new Image(315,48); 
        pic1.src="<%=request.getContextPath()%>/admin/img/loginButtonOver.png"; 
        
    </SCRIPT>
     <!-- Preload Buttons -->
 <SCRIPT language="JavaScript">
    
        // Enter Over Image
        pic1= new Image(315,48); 
        pic1.src="<%=request.getContextPath()%>/admin/img/loginButtonOver.png"; 
        
    </SCRIPT>

 <script>
 function checkEnter(e){ //e is event object passed from function invocation
     var characterCode; //literal character code will be stored in this variable
    
    if(e && e.which){ 
        //if which property of event object is supported (NN4)
        characterCode = e.which; //character code is contained in NN4's which property
    } else {
        //character code is contained in IE's keyCode property
        characterCode = e.keyCode; 
    }

    if(characterCode == 13){ 
        //if generated character code is equal to ascii 13 (if enter key)
        document.loginform.submit(); //submit the form
        return false
    } else {
       return true
    }
}
 </script> 
     
</head>

<body>
<form id="loginform" name="loginform" method="post" action="j_security_check">

<center>
 		<table height="300">
			<tr>
				<td><img src="<%=request.getContextPath()%>/admin/img/brandless-banner.gif"/> </td>
			</tr>
		</table>
        <table border="0" cellpadding="0" cellspacing="0">
                <tr height="83">
                    <td width="50%"/>
                    <td width="315">                 

 <!-- LOGIN FORM -->
                       <div class="loginFormBack">
                       <table border="0" cellpadding="0" cellspacing="0" class="loginFormTable">
 <tr height="17"/>
                             <tr>
                                 <td width="50%"/>
                                 <td><a>Username:&nbsp;&nbsp;&nbsp;</a></td>
                                 <td><input id="j_username" name="j_username" type="input" class="loginFormText" onKeyPress="checkEnter(event)" /></td>
                                 <td width="50%"/>
                             </tr>
                             <tr height="10"/>
                             <tr>
                                 <td width="50%"/>
                                 <td><a>&nbsp;Password:&nbsp;</a></td>
                                 <td><input id="j_password" name="j_password" class="loginFormText" type="password" onKeyPress="checkEnter(event)" /></td>
                                 <td width="50%"/>
                             </tr>
 </table> 
                     </div>                   
                    </td>
                    <td width="50%"/>
                </tr>
               <tr height="31">
                    <td width="50%"/>
                        <td width="315"align="center">   
                        
                          <!-- Error Message -->
                            <div class="errorMessageDiv" id="errorMessageDiv" name="errorMessageDiv">
                                <table cellpadding="0" cellspacing="0" border="0" height="31">
                                    <tr height="31">
                                        <td>
                                            <a></a>                                        
                                     </td>
                                    </tr>
                                </table>                       
                            </div>                        
 </td>
                    <td width="50%"/>
                </tr>
                <tr height="50">
                 <td width="50%"/>
             <td width="315">
 <a href="javascript: document.loginform.submit()" target="_self"><div class="loginButton"></div></a>                
 </td>
                    <td width="50%">
                    
                             
 </td>
                </tr>
 </table>
 <table width="100%">    		<!-- Copy Right -->
 		<div class="copyRightDiv" align="center"> 
		<br><b><font face="Arial, Helvetica, sans-serif" size="1" color="#FFFFFF" >Version <%= BuildNumber.getVersion() %>, Build <%= BuildNumber.getBuildNumber() %></font></b>
		</div>	
</table>
   </form>

 <script type="text/javascript">
 document.loginform.j_username.focus();
 </script>

</center>
</body>
</html> 
 
