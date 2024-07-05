<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="csi.server.common.publishing.Asset"%>
<%@page import="csi.server.common.publishing.Comment"%>
<%
 Asset asset = (Asset) request.getAttribute("asset");

 String assetURI = (String) request.getAttribute("assetURI");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <meta http-equiv="Pragma" content="no-cache"> <meta http-equiv="Cache-Control" content="no-cache"> 
        <meta http-equiv="Expires" content="Sat, 01 Dec 2001 00:00:00 GMT">    
           <title>Asset Report - <%= asset.getName() %></title>
        <link href="<%=request.getContextPath()%>/report.css" media="screen" rel="Stylesheet" type="text/css" />
    </head>
    <body>
            <div class="assetInfo">
                <div class="assetName"><%= asset.getName() %></div>
                <table class="assetTable">
                    <tbody>
                        <tr>
                            <td class="tableCell">Created by:</td>
                            <td class="tableCell"><%= asset.getCreatedBy() %></td>
                        </tr>
                        <tr>
                            <td class="tableCell">Created on:</td>
                            <td class="tableCell"><%= asset.getCreationTime() %></td>
                        </tr>
                        <tr>
                            <td class="tableCell">Description:</td>
                            <td class="tableCell"><%= asset.getDescription() %></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="assetInfo">
                <table class="assetTable">
                    <tbody>
                        <tr><td>Tags</td></tr>
                        <tr><td><%= asset.getAssetListString() %></td></tr>
                    </tbody>
                </table> 
            </div>
            <div class="assetInfo">
             <table class="assetTable">
                <tbody>
                <tr><td>Comments</td></tr> 
                 <%
                for ( Comment comment : asset.getComments() )
                {
                %>
                <tr>
                    <td class="tableCell"><%= comment.getCreator() %></td>
                    <td class="tableCell"><%= comment.getTimeStamp().toString() %></td>
                    <td class="tableCell"><%= comment.getText() %> </td>
                </tr>
                <%
                }
                %>
                </tbody>
            </table>
            </div>
            <div class="assetImage">
                <embed src="<%= assetURI %>" width="1000" height="621">
            </div>
        <div class="footer">
            <%@include file="/assets/footer.html"%>
        </div>
    </body>
</html>