package csi.bridge.logging;

import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.oculusinfo.ncompass.als._3.ALEMessageType;
import com.oculusinfo.ncompass.als._3.ALSInterface;
import com.oculusinfo.ncompass.als._3.ALSService;
import com.oculusinfo.ncompass.als._3.StartApplication;

public class ApplicationListener implements ServletContextListener, BridgeConstants {
   protected static final Logger LOG = LogManager.getLogger(ApplicationListener.class);

    protected ALSInterface loggingService;

    protected Provider eventProvider;

    @Override
    public void contextDestroyed( ServletContextEvent contextEvent )
    {
       LOG.info( "No established protocol for reporting application shutdown to the Bridge-IC Application Logging Service" );
    }

    @Override
    public void contextInitialized( ServletContextEvent contextEvent )
    {

       LOG.info( "Configuring Bridge-IC Application Logging Service" );

        try {
            eventProvider = new Provider();
        } catch( DatatypeConfigurationException e ) {
           LOG.error( "Failed to property configure run-time for creating logging messages", e );
            return;
        }

        URL localURL = this.getClass().getClassLoader().getResource( BridgeConstants.ALS_WSDL_URL_LOCAL );
        ALSService factory = new ALSService( localURL, BridgeConstants.ALS_SERVICE_NAME );
        loggingService = factory.getALSPort();

        StartApplication start = eventProvider.getStartupEvent();

        ALEMessageType message = eventProvider.getMessageTemplate();
        message.setAnalysisLogEvent( start );
        loggingService.publishALE( message );
    }

}
