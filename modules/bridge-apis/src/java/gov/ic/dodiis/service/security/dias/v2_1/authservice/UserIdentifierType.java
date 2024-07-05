
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents the unique identifier that can be used to query user attributes.
 * 
 * <p>Java class for UserIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserIdentifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserIdentifierType", propOrder = {
    "dn"
})
public class UserIdentifierType {

    @XmlElement(name = "DN", required = true)
    protected String dn;

    /**
     * Gets the value of the dn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDN() {
        return dn;
    }

    /**
     * Sets the value of the dn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDN(String value) {
        this.dn = value;
    }

}
