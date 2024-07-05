
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents the affiliation of the user.
 * 
 * <p>Java class for getAffiliationResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getAffiliationResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Affiliation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAffiliationResponseType", propOrder = {
    "affiliation"
})
public class GetAffiliationResponseType {

    @XmlElement(name = "Affiliation", required = true)
    protected String affiliation;

    /**
     * Gets the value of the affiliation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * Sets the value of the affiliation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAffiliation(String value) {
        this.affiliation = value;
    }

}
