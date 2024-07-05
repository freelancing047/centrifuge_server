package csi.server.common.dto.resource;


import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Response to an upload request.
 * 
 * @author bstine
 *
 */

public class ImportStatus implements IsSerializable {

    public ImportStatusType status;
    public String itemName; // Name of item on server.
    public String className; // Class name from XML, if found.
    public String message; // Message suitable for display to client.
    public String uuid; // UUID of imported item, on successful import.
    public String repairMessage; //Denotes if a structural repair was nessesary, which field(s) were repaired.
    
}
