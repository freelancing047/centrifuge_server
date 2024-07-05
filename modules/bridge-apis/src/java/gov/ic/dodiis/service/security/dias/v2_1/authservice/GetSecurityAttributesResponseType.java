
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The security attributes for a user.  This includes clearance and formal access attributes.
 * 
 * <p>Java class for getSecurityAttributesResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSecurityAttributesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClearanceInfo" type="{http://dias.security.service.dodiis.ic.gov/v2.1/authservice/}UserClearanceInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSecurityAttributesResponseType", propOrder = {
    "clearanceInfo"
})
public class GetSecurityAttributesResponseType {

    @XmlElement(name = "ClearanceInfo", required = true)
    protected UserClearanceInfoType clearanceInfo;

    /**
     * Gets the value of the clearanceInfo property.
     * 
     * @return
     *     possible object is
     *     {@link UserClearanceInfoType }
     *     
     */
    public UserClearanceInfoType getClearanceInfo() {
        return clearanceInfo;
    }

    /**
     * Sets the value of the clearanceInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserClearanceInfoType }
     *     
     */
    public void setClearanceInfo(UserClearanceInfoType value) {
        this.clearanceInfo = value;
    }

}
