package csi.bridge.logging;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.oculusinfo.ncompass.als._3.ALEMessageType;
import com.oculusinfo.ncompass.als._3.StartApplication;

public class Provider
{
    protected DatatypeFactory typeFactory;
    
    public Provider() throws DatatypeConfigurationException 
    {
        typeFactory  = DatatypeFactory.newInstance();
    }
    
    public StartApplication getStartupEvent()
    {
        StartApplication start = new StartApplication();
        start.setApplication( BridgeConstants.CSI_SERVER );
        start.setApplicationName( BridgeConstants.CSI_SERVER );
        start.setApplicationVersion( BridgeConstants.CSI_SERVER_VERSION );
        
        return start;
        
    }
    
    public ALEMessageType getMessageTemplate()
    {
        ALEMessageType message = new ALEMessageType();
        
        XMLGregorianCalendar reportTime = getCurrentXMLTime();
        message.setOriginatingSystem( BridgeConstants.CSI_URN );
        message.setReportTime( reportTime );
        return message;
    }

    public XMLGregorianCalendar getCurrentXMLTime()
    {
        XMLGregorianCalendar xmlCalendar = typeFactory.newXMLGregorianCalendar( new GregorianCalendar() );
        return xmlCalendar;
    }
    
    
    
}
