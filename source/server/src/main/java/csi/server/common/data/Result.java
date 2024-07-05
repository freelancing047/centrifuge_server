package csi.server.common.data;

/**
 * Provides a wrapper for returning results.  This keeps the actual data from 
 * being the document element; which is notoriously hard to get at and process.
 * <p>
 * This server as our top-level wrapper ala SOAP RPC Response message.
 * 
 * @author Tildenwoods
 *
 */
public class Result {

    public Object holder;
    public String operationStatus;
    public String errorMessage;

}
