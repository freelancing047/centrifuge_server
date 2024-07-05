This module contains some extensions to support ForecastLink
in Centrifuge.

Steps:

1.  deploy.clean


---------------------------------------------------------------
This task automatically performs the following steps also:
1.  Copy target/csi-forecastlink-ext.jar to <server>/webapps/Centrifuge/WEB-INF/lib

2.  Update the deployment descriptor for Centrifuge (web.xml)
    a.  Insert the resources/web-snippet.xml into the web.xml
    b.  Change the authentication method from Form to Basic auth; remove the login and error pages. 
        The resulting login-config element should look like:
        
            <login-config>
                <auth-method>BASIC</auth-method>
                <realm-name>Centrifuge</realm-name>
            </login-config>
            
3.  Update the server-wide configuration to revert our container extensions.  Edit the file
    <server>/conf/context.xml and comment/remove the following line:
    
            <Valve className="csi.container.tomcat.valve.CentrifugeAuthenticator" />
            
4.  Copy src/web/resources/icons/ForecastLink to <server>/webapps/Centrifuge/resources/icons/ForecastLink.
    This ensures that the icons are available to the server for rendering the graph.                       
5.  Copy the MySQL driver into the server's lib directory.            
