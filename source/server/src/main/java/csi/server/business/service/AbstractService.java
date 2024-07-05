/*
* @(#) Service.java,  23.03.2010
*
*/
package csi.server.business.service;

import com.thoughtworks.xstream.XStream;

import csi.server.common.exception.CentrifugeException;
import csi.server.task.api.TaskHelper;

/**
 * 
 * Provides common functionalities for all extending classes.
 */
public abstract class AbstractService {
	
    public AbstractService() {

    }

    /**
     * Override this to initialize the XStream instance
     * before it gets registered with the codec manager
     */
    public void initMarshaller(XStream xstream) {
        // do nothing...
    }

    protected String generateFullUrlFromAppRelative(String relativeUrlIn) throws CentrifugeException {
    	
        String myRequestUrl = TaskHelper.getCurrentContext().getRequestURL();
        int myOffset = myRequestUrl.indexOf("://");
        
        if (0 < myOffset) {
        	
        	myOffset = myRequestUrl.indexOf("/", (myOffset + 3));
        	
            if (0 < myOffset) {
            	
            	myOffset = myRequestUrl.indexOf("/", (myOffset + 1));
            	
                if (0 < myOffset) {
                	
                	return myRequestUrl.substring(0, myOffset) + relativeUrlIn;
                }
            }
        }
       	throw new CentrifugeException("");
    }

    protected String generateFullUrlFromTomcatRelative(String relativeUrlIn) throws CentrifugeException {
    	
        String myRequestUrl = TaskHelper.getCurrentContext().getRequestURL();
        int myOffset = myRequestUrl.indexOf("://");
        
        if (0 < myOffset) {
        	
        	myOffset = myRequestUrl.indexOf("/", (myOffset + 3));
        	
            if (0 < myOffset) {
                	
               	return myRequestUrl.substring(0, myOffset) + relativeUrlIn;
            }
        }
       	throw new CentrifugeException("");
    }
}
