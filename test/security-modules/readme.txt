
                           Test Classification/Security Labels Setup Steps:
(1) Compile the test classification/security labels classes located at c:\git\test\security-modules with:

    ant compile  -- compile the example classes
	
	ant jar -- create the test jar from example classes
	
(2) Copy c:\git\test\security-modules\target\test-module.jar into
    [server_home]\webapps\Centrifuge\WEB-INF\lib folder
	
(3) Enable classification and labels required flag in centrifuge.xml file:

    <extensionsConfig>
       <classificationRequired>true</classificationRequired>
       <labelsRequired>true</labelsRequired>
    </extensionsConfig>
	
(4) Configure listener and custom modules in [server_home]\webapps\Centrifuge\WEB-INF\web.xml file:

    <!--
    <listener>
        <listener-class>csi.config.app.StandardServletListener</listener-class>
    </listener>
	-->
	
	<listener>
        <listener-class>csi.config.app.ConfiguredServletListener</listener-class>
    </listener>

    <listener>
        <listener-class>csi.startup.Bootstrap</listener-class>
    </listener>
	
	<context-param>
        <param-name>guice-modules</param-name>
        <param-value>csi.config.app.modules.AuthorizationModule,example.TestConfig</param-value>
    </context-param>

(5) Restart server after step (1) to (4) are completed
(6) Use classy phone data to start the classification testing	