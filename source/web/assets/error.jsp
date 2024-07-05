<?xml version="1.0" encoding="utf-8" ?>
<%
    response.setHeader("Content-Type", "text/xml");
%>
<response status="fail">
  <error code="<%= request.getAttribute("error-code") %>" msg="<%= request.getAttribute("error-message") %>"/>
</response>
