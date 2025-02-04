
package com.oculusinfo.ncompass.authentication;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.4-hudson-208-
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "AuthenticateFault", targetNamespace = "http://oculusinfo.com/ncompass/authentication")
public class AuthenticationFaultMsg
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private AuthenticateFault faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public AuthenticationFaultMsg(String message, AuthenticateFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public AuthenticationFaultMsg(String message, AuthenticateFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.oculusinfo.ncompass.authentication.AuthenticateFault
     */
    public AuthenticateFault getFaultInfo() {
        return faultInfo;
    }

}
