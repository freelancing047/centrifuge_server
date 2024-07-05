
The registration and enablement of extensions is defined below.  These steps are required to ensure that Centrifuge is notified of extensions that should be registered during the initialization process.  

    i.  Modify Centrifuge to ensure that user provided configuration modules
    are loaded during initialization.  Edit the web.xml file, located in
    webapps/Centrifuge/WEB-INF, changing:

    <listener>
    <listener-class>csi.config.app.StandardServletListener</listener-class>
    </listener>

    to 

    <listener>
    <listener-class>csi.config.app.ConfiguredServletListener</listener-class>
    </listener>

    ii.  Register your module configuration.  Below the listener declaration
    add the following:

    <context-param> 
        <param-name>guice-modules</param-name>
        <param-value>
            csi.config.app.modules.AuthorizationModule,
            example.AuditConfig
        </param-value>
    </context-param>

    Note that the first value is important.  This ensures that you maintain
    consistent ACL checks for resources available to users; removing this will
    disable ACL checks when attempting to perform operations on resources such
    as Dataviews.

    The second value registers the sample extensions contained in this 
    project.


    iii. Deploy the generated jar to Centrifuge's application directory:
    webapps/Centrifuge/WEB-INF/lib


