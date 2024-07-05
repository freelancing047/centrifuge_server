
package com.oculusinfo.ncompass.authentication;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.oculusinfo.ncompass.authentication package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.oculusinfo.ncompass.authentication
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UserDataResponse }
     * 
     */
    public UserDataResponse createUserDataResponse() {
        return new UserDataResponse();
    }

    /**
     * Create an instance of {@link AuthenticateFault }
     * 
     */
    public AuthenticateFault createAuthenticateFault() {
        return new AuthenticateFault();
    }

    /**
     * Create an instance of {@link AuthenticateResponse }
     * 
     */
    public AuthenticateResponse createAuthenticateResponse() {
        return new AuthenticateResponse();
    }

    /**
     * Create an instance of {@link UserDataByNameRequest }
     * 
     */
    public UserDataByNameRequest createUserDataByNameRequest() {
        return new UserDataByNameRequest();
    }

    /**
     * Create an instance of {@link UserData }
     * 
     */
    public UserData createUserData() {
        return new UserData();
    }

    /**
     * Create an instance of {@link UserData.Extensions }
     * 
     */
    public UserData.Extensions createUserDataExtensions() {
        return new UserData.Extensions();
    }

    /**
     * Create an instance of {@link UserDataFault }
     * 
     */
    public UserDataFault createUserDataFault() {
        return new UserDataFault();
    }

    /**
     * Create an instance of {@link UserDataRequest }
     * 
     */
    public UserDataRequest createUserDataRequest() {
        return new UserDataRequest();
    }

    /**
     * Create an instance of {@link AuthenticateRequest }
     * 
     */
    public AuthenticateRequest createAuthenticateRequest() {
        return new AuthenticateRequest();
    }

}
