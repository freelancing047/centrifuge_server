
package com.oculusinfo.ncompass.authentication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://oculusinfo.com/ncompass/authentication}UserData"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userData"
})
@XmlRootElement(name = "UserDataResponse")
public class UserDataResponse {

    @XmlElement(name = "UserData", required = true)
    protected UserData userData;

    /**
     * Gets the value of the userData property.
     * 
     * @return
     *     possible object is
     *     {@link UserData }
     *     
     */
    public UserData getUserData() {
        return userData;
    }

    /**
     * Sets the value of the userData property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserData }
     *     
     */
    public void setUserData(UserData value) {
        this.userData = value;
    }

}
