
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * The response to an isMemberOf Request
 * 
 * <p>Java class for isMemberOfResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="isMemberOfResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="isMemberOf" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "isMemberOfResponseType", propOrder = {
    "isMemberOf"
})
public class IsMemberOfResponseType {

    protected boolean isMemberOf;

    /**
     * Gets the value of the isMemberOf property.
     * 
     */
    public boolean isIsMemberOf() {
        return isMemberOf;
    }

    /**
     * Sets the value of the isMemberOf property.
     * 
     */
    public void setIsMemberOf(boolean value) {
        this.isMemberOf = value;
    }

}
