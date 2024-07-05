<?xml version="1.0" ?>


<%@page import="java.security.*" %>
<%@page import="java.util.*" %>
<%@page import="javax.persistence.Query"%>
<%@page import="javax.persistence.EntityManager"%>
<%@page import="javax.persistence.EntityManagerFactory"%>
<%@page import="centrifuge.dao.jpa.ResourceService"%>
<%@page import="centrifuge.model.DataView"%>
<%@page import="centrifuge.model.Connection"%>
<%@page import="csi.server.common.identity.User"%>
<%@page import="centrifuge.security.Authorization"%>
<%@page import="csi.server.ws.filter.ApplicationFilter"%>
<%@ page language="java" contentType="text/xml"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%

//	Principal principal = request.getUserPrincipal();
	
	
try {
    
	List<DataView> views = DataViews.list();

    ResourceService service = new ResourceService();

    Authorization authZ = (Authorization)request.getAttribute( ApplicationFilter.AUTHORIZATION_TOKEN );

    views = service.filterDataViews( authZ, "read", views );


	User user = Users.findByName( request.getUserPrincipal().getName() );
	
%>

<%@page import="centrifuge.dao.DataViews"%>
<%@page import="centrifuge.dao.Users"%>
<%@page import="centrifuge.model.LastAccessed"%>
<views>

<%

    for( DataView view : views )
	{
	    Connection connection = view.getConnection();
	    centrifuge.model.Query queryInfo = view.getQuery();
	    pageContext.setAttribute( "queryInfo", queryInfo);
	    pageContext.setAttribute( "connection", connection );
	    
%>
	<view>
		<dataviewName><%= view.getName() %></dataviewName>
        <uuid><%=view.getTargetUuid()%></uuid>
		<connectionName><%= view.getConnection().getName() %></connectionName>
		<queryName><%= view.getQuery().getName() %></queryName>
		<metaName><%=view.getMetaData().getName()%></metaName>
        
        
		<rowCount><%= view.getRowCount(user)  %></rowCount>
		<lastOpened><%=  view.getDateLastOpened(user) %></lastOpened>

		<dataviewID><%= view.getId() %></dataviewID>
		<dataviewComment><%= view.getRemark() %></dataviewComment>
        
		<connection>
            <id><c:out value="${connection.id}" escapeXml="true" /></id>
			<name><c:out value="${connection.name}" escapeXml="true" /></name>
			<type><c:out value="${connection.connType}" escapeXml="true" /></type>
			<url><c:out value="${connection.URL}" escapeXml="true" /></url>
			<comment><c:out value="${connection.remark}" escapeXml="true" /></comment>
		</connection>
	
		<query>
			<id><c:out value="${queryInfo.id}" escapeXml="true" /></id>
			<name><c:out value="${queryInfo.name}" escapeXml="true" /></name>
			<text><c:out value="${queryInfo.text}" escapeXml="true" /></text>
			<comment><c:out value="${queryInfo.remark}" escapeXml="true" /></comment>
		</query>
	</view>
<%
	}
} catch ( Throwable t ) {
    t.printStackTrace();
}
%>	
</views>
