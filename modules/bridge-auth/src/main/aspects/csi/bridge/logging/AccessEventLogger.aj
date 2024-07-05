package csi.bridge.logging;

import java.net.URL;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import centrifuge.model.dataview.DataView;
import centrifuge.model.dataview.persistence.DataViewHelper;
import csi.server.common.model.jdbc.Credentials;

import com.oculusinfo.ncompass.als._3.ALEMessageType;
import com.oculusinfo.ncompass.als._3.ALSInterface;
import com.oculusinfo.ncompass.als._3.ALSService;
import com.oculusinfo.ncompass.als._3.Access;
import com.oculusinfo.ncompass.als._3.Entity;
import com.oculusinfo.ncompass.als._3.Resource;

public aspect AccessEventLogger
{
    protected Logger localLog = Logger.getLogger( AccessEventLogger.class );

    protected ALSInterface logService;

    protected Provider provider;

    public AccessEventLogger()
    {
        URL alsURL = this.getClass().getClassLoader().getResource( "META-INF/services/als.wsdl" );
        QName alsName = new QName( "http://oculusinfo.com/ncompass/als/3.0", "ALSService" );
        ALSService service = new ALSService( alsURL, alsName );
        logService = service.getALSPort();
        try {
            provider = new Provider();
        } catch( DatatypeConfigurationException dtce ) {

        }
    }

    pointcut launch( DataViewHelper dvHelper, DataView dv, Credentials credentials ): 
        target( dvHelper ) && 
        args( dv, credentials ) &&
        execution( DataView DataViewHelper.openDataView( DataView, Credentials ) );

    before( DataViewHelper helper, DataView dv, Credentials credentials ): launch( helper, dv, credentials )
    {
        
        if( dv != null ) {

            if( localLog.isInfoEnabled() ) {
                localLog.info( "Creating ALE Access event for publication" );
            }

            Access access = new Access();
            if( provider != null ) {
                access.setOccurrenceTime( provider.getCurrentXMLTime() );
            }

            Resource resource = new Resource();
            StringBuffer buf = new StringBuffer();
            buf.append( "Opening Dataview " ).append( dv.getName() );
            if( dv.getRemarks() != null && dv.getRemarks().length() > 0 ) {
                buf.append( "\n\tComments: " ).append( dv.getRemarks() );
            }
            resource.setDescription( buf.toString() );

            buf = new StringBuffer();
            buf.append( "UUID: " ).append( dv.getUuid() );

            resource.setComment( buf.toString() );
            resource.setEntityType( "Resource" );

            List<Entity> resources = access.getEntity();
            resources.add( resource );

            ALEMessageType message = getMessageTemplate();
            message.setAnalysisLogEvent( access );

            if( localLog.isInfoEnabled() ) {
                localLog.info( "Publishing ALE Access event" );
            }
            logService.publishALE( message );

        }

    }

    protected ALEMessageType getMessageTemplate()
    {
        ALEMessageType message = new ALEMessageType();
        message.setOriginatingSystem( BridgeConstants.CSI_URN );

        return message;
    }

}
