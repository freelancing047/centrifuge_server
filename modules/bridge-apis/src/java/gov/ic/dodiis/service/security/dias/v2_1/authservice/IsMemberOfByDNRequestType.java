
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A request to check if a user is a member of a group by group DN
 * 
 * <p>Java class for isMemberOfByDNRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="isMemberOfByDNRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userDN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groupDN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "isMemberOfByDNRequestType", propOrder = {
    "userDN",
    "groupDN"
})
public class IsMemberOfByDNRequestType {

    @XmlElement(required = true)
    protected String userDN;
    @XmlElement(required = true)
    protected String groupDN;

    /**
     * Gets the value of the userDN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserDN() {
        return userDN;
    }

    /**
     * Sets the value of the userDN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserDN(String value) {
        this.userDN = value;
    }

    /**
     * Gets the value of the groupDN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupDN() {
        return groupDN;
    }

    /**
     * Sets the value of the groupDN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupDN(String value) {
        this.groupDN = value;
    }

}
