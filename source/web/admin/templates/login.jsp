<%@ page import="csi.server.util.BuildNumberdNumber" %>

<%
   response.addHeader("X-Auth-Required", "true");
   response.addHeader("P3P","CP=\"STA\"");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Centrifuge :: Login</title>
    <link rel="icon" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon" />
    <style type="text/css">
        body {
            background-image: url(<%=request.getContextPath()%>/admin/img/bg.png);
            margin: 0;
            padding: 0;
            text-align: center;
        }

        .bluebar {
            background-color: #004672;
            height: 27px;
        }

        .logo {
            margin-top: 3px;
            margin-left: 14px;
            float: left;
        }

        .loginForm {
            position: absolute;
            height: 246px;
            top: 50%;
            margin-top: -123px;
            width: 363px;
            left: 50%;
            margin-left: -182px;
            background-color: #F7F7F7;
            font-family: Arial, Helvetica, sans-serif;
        }

        .loginFormText {
            height: 27px;
            width: 213px;
            background-image: url(<%=request.getContextPath()%>/admin/img/LoginFormFieldSkin.png);
            background-repeat: repeat;
            border-width: 0px;
            padding: 0px;
            text-indent: 5px;
        }

        .inputTable {
            margin: auto;
            color: #666666;
            font-size: 16px;
            padding-top: 10px;
            padding-bottom: 10px;
        }

        .loginFormHeading {
            background-color: #EBEBEB;
            padding: 30px;
            color: #666666;
            font-family: Arial, Helvetica, sans-serif;
            font-size: 17px;
            text-align: left;
        }

        .divider {
            background-color: #C6C6C6;
            height: 1px;
            padding: 0px;
            margin: 0px;
            border-width: 0px;
        }

        .loginButtonLabel{
            vertical-align: middle;
            display: table-cell;
            text-decoration: none;
            font-size: 14px;
            color: #FFFFFF;
        }

        .loginButtonBack {
            height: 35px;
            width: 301px;
            background-image: url(<%=request.getContextPath()%>/admin/img/LogInButtonSkin.png);
            margin: auto;
            display: table;
        }

        .copyRightDiv {
            position: absolute;
            width: 100%;
            font-family: Arial, Helvetica, sans-serif;
            font-size: 11px;
            font-style: normal;
            font-weight: normal;
            color: #E2EAE7;
            top: 90%;
        }
    </style>

    <script type="text/javascript">
        function checkEnter(e) { //e is event object passed from function invocation
            var characterCode; //literal character code will be stored in this variable

            if (e && e.which) {
                //if which property of event object is supported (NN4)
                characterCode = e.which; //character code is contained in NN4's which property
            } else {
                //character code is contained in IE's keyCode property
                characterCode = e.keyCode;
            }

            if (characterCode == 13) {
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
    <div class="bluebar">
        <img src="<%=request.getContextPath()%>/admin/img/HeaderLockup.png" class="logo" />
    </div>
    <div class="loginForm">
        <form id="loginform" name="loginform" method="post" action="j_security_check">
        <div class="loginFormHeading">Log In</div>
        <hr class="divider" />
        <!-- LOGIN FORM -->
        <table border="0" cellpadding="0" cellspacing="10" class="inputTable">
            <tr>
                <td>Username:</td>
                <td>
                    <input id="j_username" name="j_username" type="input" class="loginFormText" onkeypress="checkEnter(event)" /></td>
            </tr>
            <tr>
                <td>Password:</td>
                <td>
                    <input id="j_password" name="j_password" class="loginFormText" type="password" onkeypress="checkEnter(event)" /></td>
            </tr>
        </table>
        <div class="loginButtonBack">
                <a href="javascript: document.loginform.submit()" target="_self" class="loginButtonLabel">Log In</a>
        </div>
        </form>
    </div>
    <div class="copyRightDiv" align="center">
        Version <%= BuildNumber.getVersion() %>, Build <%= BuildNumber.getBuildNumber() %><br />
        Copyrigth &copy; 2012 Centrifuge Systems, Inc.  All rights reserved.
    </div>
    <script type="text/javascript">
        document.loginform.j_username.focus();
    </script>
</body>
</html> 
