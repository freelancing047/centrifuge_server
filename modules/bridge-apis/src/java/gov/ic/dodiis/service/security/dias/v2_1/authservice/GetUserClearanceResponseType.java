
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Type representing the user's clearance.
 * 
 * <p>Java class for getUserClearanceResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getUserClearanceResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Clearance" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUserClearanceResponseType", propOrder = {
    "clearance"
})
public class GetUserClearanceResponseType {

    @XmlElement(name = "Clearance", required = true)
    protected String clearance;

    /**
     * Gets the value of the clearance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClearance() {
        return clearance;
    }

    /**
     * Sets the value of the clearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClearance(String value) {
        this.clearance = value;
    }

}
