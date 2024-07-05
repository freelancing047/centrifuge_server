// define Array.indexOf for browsers that don't support it
// natively
if(!Array.prototype.indexOf) { 
    Array.prototype.indexOf = function(value) { 
        for(var i = 0; i < this.length; i++) { 
            if(this[i] === value) { 
                return i; 
            } 
        } 
        return -1; 
    };
}




/**
 * CsiProtocol class
 */
 
function CsiProtocol() {

}

CsiProtocol.cometEnabled = true;
CsiProtocol.debugging = false;
CsiProtocol.enableJSSend = false;


/** Maximum number of reconnect trials */
CsiProtocol.maxCometErrors = 10;
CsiProtocol.maxSendErrors = 3;

/** Interval to retry commet reconnection [ms] */
CsiProtocol.reconnectTimerOnError = 300;
CsiProtocol.sendRetryTime = 300;


/**
 * static properties and methods
 */ 

CsiProtocol.cometServlet = '/Centrifuge/actions/comet';
CsiProtocol.owner = null;

CsiProtocol.init = function() {
    // Create a protocol instance at the highest possible
    // window which is accessible by this window.  In single
    // server access the resulting window will be the same as
    // window.top.  In the case that this is a crossdomain iframe
    // the result will be the topmost window that has the same 
    // domain as this window.

	var parentWin = window;
	
	// TODO: The code bellow must be revisited for the case of crossdomain iframes.
	// Right now it does nothing but an infinite loop, since window.parent is always != null 
	/* try {
		 while (parentWin && parentWin != null) {
			if (parentWin.parent != null && parentWin.parent.location != null) {				
				parentWin = parentWin.parent;
			}
		} 
	} catch (e) { } */
 
    CsiProtocol.owner = parentWin;
    //alert ("attaching protocol to " + CsiProtocol.owner.location );
    if ( ! CsiProtocol.owner.csiProtocol ) {
        CsiProtocol.owner.csiProtocol = new CsiProtocol();   
        CsiProtocol.owner.csiProtocol.executeCometRequest();
    }
}

CsiProtocol.instance = function() {
    return CsiProtocol.owner.csiProtocol;
}

/**
 * instance properties and methods
 */
 
// Generate a unique client id so the server can support
// multiple client windows (tabs) that use the same session id.
// Http session id is not unique enough since a user could open 
// two browser tabs that connect to the server using the same session.
CsiProtocol.prototype.clientId = jQuery.uuid();

/* associative array containing element with "callback" function */
CsiProtocol.prototype.callbackElems = new Object();   




var preparedError = "<?xml version='1.0' encoding='UTF-8'?><csi.server.task.api.TaskStatus2><taskStatus>TASK_STATUS_ERROR</taskStatus><resultData>Server connection lost. Please refresh your web browser page</resultData></csi.server.task.api.TaskStatus2>";

// Comet function used to fetch/unmarshall the response
// pass to FLEX application with unmarshalled parameters
// and resends the request to comet servlet

CsiProtocol.prototype.executeCometRequest = function() {
    this.sendCometRequest(0);
}

CsiProtocol.prototype.sendCometRequest = function(attempt) {
    if (!attempt) {
        attempt = 0;
    }
    
    var taskId;
    var taskStatus;
    var data;

    $.ajax({
        url: CsiProtocol.cometServlet,
        type: 'POST',
        contentType: 'text/xml',
        processData: false,        
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Cache-Control', 'no-cache');
            xhr.setRequestHeader('Pragma', 'no-cache');
            xhr.setRequestHeader('If-Modified-Since', 'Sat, 1 Jan 2005 00:00:00 GMT');
            xhr.setRequestHeader('Expires', 'Sat, 1 Jan 2005 00:00:00 GMT');
            xhr.setRequestHeader('X-Client-Id', CsiProtocol.instance().clientId);
        },

        complete: function(xhr, textStatus) {
            if (textStatus == 'timeout') {
                xhr.abort();
                window.setTimeout(function() {
                        //console.log('COMET RETRY ' + attempt + ': ' + textStatus );
                        CsiProtocol.instance().sendCometRequest(0);
                    }, 1);  
            } else if (xhr.status!=200) {
                if (attempt < CsiProtocol.maxCometErrors) {
                    window.setTimeout(function() {
                        //console.log('COMET RETRY ' + attempt + ': ' + textStatus + '('+ xhr.status + ') ' + xhr.responseText);
                        CsiProtocol.instance().sendCometRequest(attempt + 1);
                    }, CsiProtocol.reconnectTimerOnError);                 
                } else {
                    // notify all clients that we can no longer connect
                    // to the server
                    
                    //console.log('COMET GIVE UP: notifying all of connection error');
                    CsiProtocol.instance().notifyConnectError(null);
                }
            } else {            
                // check if this is the login page.  If so, the session
                // is no longer valid.
                if (xhr.getResponseHeader('X-Auth-Required') == 'true') {
                    //console.log('COMET ERROR: Authorization Required');
                    CsiProtocol.instance().notifyAuthRequired();
                    // exit without trying to reconnect
                    return;
                }

                // process the response async to minimize 
                // lag between executing the client callback
                // and the reconnecting comet connection
                
                window.setTimeout(function() {
                     CsiProtocol.instance().sendCometRequest(0);
                }, 1);      
                         
                // process the response                     
                taskId = xhr.getResponseHeader('X-Task-Id');                
                taskStatus = xhr.getResponseHeader('X-Task-Status');
                data = xhr.responseText;
                               
                if (!taskId || !data || !taskStatus) {
                    // this is ok...server may have aborted connection
                    // so do nothing since the client will reconnect
                    return;
                }                
                
                try {
                    
                    var cbElem = CsiProtocol.instance().callbackElems[taskId];
                    if (cbElem != null) {
                        cbElem.callBack(taskId,data,taskStatus);
                    }
                } finally {
                    if (taskStatus!='TASK_STATUS_UPDATE') {
                        delete CsiProtocol.instance().callbackElems[taskId];
                    }          
                }
            }
        }
    });
}  

CsiProtocol.prototype.registerTaskCallback = function(taskId, elem) {
    this.callbackElems[taskId] = elem;
}

CsiProtocol.prototype.notifyAuthRequired = function() {
    // for auth error all clients need notification.  For now
    // we just return the same status error to all client so
    // all flex clients will display a connection error message.
    //
    // TODO: In the future we should display a login 
    // iframe so the user can attempt to reconnect to the session.  
    // This will require more infra-structure on the server
    // since the server currently stores lots of 
    // variables in the HTTP session. 
    
    var distincts = this.getDistinctCallbacks();
    for (var i=0; i<distincts.length; i++) {
        distincts[i].callBack(null,preparedError,'TASK_STATUS_ERROR');            
    } 
}

CsiProtocol.prototype.getDistinctCallbacks = function() {
    var array = new Array(); 
    
    for (var id in this.callbackElems) {             
        var elem = this.callbackElems[id];
        if (elem != null && array.indexOf(elem) == -1) {
            array[array.length] = elem;
        }
    }
    return array;
}

CsiProtocol.prototype.notifyConnectError = function(taskId) {
    if (taskId == null) {    
        // This indicates that the server or network is down.
        // so all clients need notification.  For now
        // we just return the same status error to all client so
        // all flex clients will display a connection error message.
        //
        // TODO: In the future we should display a single error
        // iframe. 
        
        var distincts = this.getDistinctCallbacks();
        for (var i=0; i<distincts.length; i++) {
            distincts[i].callBack(taskId,preparedError,'TASK_STATUS_ERROR');            
        }   
    } else {
        var cb = this.callbackElems[taskId];
        if (cb != null) {
            cb.callBack(taskId, preparedError, 'TASK_STATUS_ERROR');
        }
    }
}

CsiProtocol.prototype.notifyRequestError = function(taskId, statusCode, responseText) {
    // notifies the client that there was an error
    // either when the client submitted a request 
    elem = this.callbackElems[taskId];
    if (elem != null) {
        elem.callBack(taskId,preparedError,'TASK_STATUS_ERROR');
        delete this.callbackElems[taskId];
    }
}



/*
 * End CsiProtocol class
 */
 
function getCometClientId() { 
    return CsiProtocol.instance().clientId;
} 

function checkJsSendEnabled() {
    return CsiProtocol.enableJSSend;
}
    
//Returns value for comet availablility flag
//Called using external interface from FLEX
function checkCometEnabled() {
    return CsiProtocol.cometEnabled;
}


function serverCall(taskId, argument, xmlData) {
    var elem = document.getElementById('Centrifuge');
    CsiProtocol.instance().registerTaskCallback(taskId, elem);
    
    if (CsiProtocol.enableJSSend == true) {
        sendRequest(taskId, argument, xmlData)
    }
    return taskId;
}

function sendRequest(taskId, argument, xmlData, attempt) {
    //console.log('sending request: ' + taskId);
    if (!attempt) {
        attempt = 0;                        
    }
    
    var messagingUrl;
    if (argument.indexOf("?")!=-1) {
        messagingUrl = argument+"&clientId="+CsiProtocol.instance().clientId+"&taskId="+taskId+"&taskOperation=execute"
    } else {
        messagingUrl = argument+"?clientId="+CsiProtocol.instance().clientId+"&taskId="+taskId+"&taskOperation=execute"
    }

    var rtype = "POST";
    if (typeof xmlData == 'undefined' || xmlData == null || xmlData.length == 0) {
        rtype = "GET";
        xmlData = null;
    }

    var connection = $.ajax({
       url: messagingUrl
       , type: rtype
       , contentType: 'text/xml'
       , processData: false
       , data: xmlData
       
       , beforeSend: function(xhr) {            
            xhr.setRequestHeader('Cache-Control', 'no-cache');
            xhr.setRequestHeader('Pragma', 'no-cache');
            xhr.setRequestHeader('If-Modified-Since', 'Sat, 1 Jan 2005 00:00:00 GMT');
            xhr.setRequestHeader('Expires', 'Sat, 1 Jan 2005 00:00:00 GMT');

            
            // this avoids IE improperly closing the connection
            // and leaving the server blocked on the inputstream
            if ($.browser.msie && rtype=='POST') {
                xhr.setRequestHeader("Connection", 'close');
            }
        }
       , complete: function(xhr, textStatus) 
            {
            if (textStatus == 'timeout') {
                xhr.abort();
                CsiProtocol.instance().notifyConnectError(taskId);
                return;
            } else if (xhr.status == 12030) {
                    // setting the request header to include "Connection: close" should
                    // avoid this but we'll leave the code here just in case.
                    //
                    // handle IE error 
                    //    12030 ERROR_INTERNET_CONNECTION_ABORTED
                    //          The connection with the server has been terminated.
                    //
                    // IE does not clean up the connection properly so the server
                    // still gets the request and blocks trying to read from the 
                    // input stream.  This ties up one messaging servlet thread
                    // till the blocking read times out. 
                    if (attempt < CsiProtocol.maxSendErrors) {
                        //console.log('RETRYING SEND: ' + attempt + ' Status: ' + textStatus + '('+ xhr.status + ') ' + xhr.responseText);
                        window.setTimeout( function() {
                            sendRequest(taskId, argument, xmlData, attempt + 1);
                        }, CsiProtocol.sendRetryTime);
                    } else {
                        //console.log('SEND GIVE UP: notifying task of connection error');
                        CsiProtocol.instance().notifyConnectError(taskId);
                    } 
                    return;
                }
              
                if (xhr.status!=200) {            
                    //console.log('SEND ERROR: ' + textStatus + '('+ xhr.status + ') ' + xhr.responseText);
                    CsiProtocol.instance().notifyRequestError(taskId, xhr.status, xhr.responseText);
                    return;
                }
                
                // check if this is the login page.  If so, the session
                // is no longer valid.
                if (xhr.getResponseHeader('X-Auth-Required') == 'true') {
                    //console.log('SEND ERROR: Authorization Required');
                    CsiProtocol.instance().notifyAuthRequired(taskId);
                }    
            }        
    }); 
      
    return taskId;
}

/**
 * initialize the protocol as soon as this script is loaded
 **/
jQuery(document).ready(function() {
	if (CsiProtocol.cometEnabled) {
	    CsiProtocol.init(); 
	}
});


