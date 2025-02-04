
package com.oculusinfo.ncompass.authentication;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.4-hudson-208-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "AuthenticationInterface", targetNamespace = "http://oculusinfo.com/ncompass/authentication")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface AuthenticationInterface {


    /**
     * 
     * @param body
     * @return
     *     returns com.oculusinfo.ncompass.authentication.AuthenticateResponse
     * @throws AuthenticationFaultMsg
     */
    @WebMethod(operationName = "Authenticate", action = "http://oculusinfo.com/ncompass/authentication/Authenticate")
    @WebResult(name = "AuthenticateResponse", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
    public AuthenticateResponse authenticate(
        @WebParam(name = "AuthenticateRequest", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
        AuthenticateRequest body)
        throws AuthenticationFaultMsg
    ;

    /**
     * 
     * @param body
     * @return
     *     returns com.oculusinfo.ncompass.authentication.UserDataResponse
     * @throws UserDataFaultMsg
     */
    @WebMethod(operationName = "LookupUserData", action = "http://oculusinfo.com/ncompass/authentication/LookupUserData")
    @WebResult(name = "UserDataResponse", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
    public UserDataResponse lookupUserData(
        @WebParam(name = "UserDataRequest", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
        UserDataRequest body)
        throws UserDataFaultMsg
    ;

    /**
     * 
     * @param body
     * @return
     *     returns com.oculusinfo.ncompass.authentication.UserDataResponse
     * @throws UserDataFaultMsg
     */
    @WebMethod(operationName = "LookupUserDataByName", action = "http://oculusinfo.com/ncompass/authentication/LookupUserDataByName")
    @WebResult(name = "UserDataResponse", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
    public UserDataResponse lookupUserDataByName(
        @WebParam(name = "UserDataByNameRequest", targetNamespace = "http://oculusinfo.com/ncompass/authentication", partName = "body")
        UserDataByNameRequest body)
        throws UserDataFaultMsg
    ;

}
