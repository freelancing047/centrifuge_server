
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A request to get a list of the members of a group by the groups DN
 * 
 * <p>Java class for getGroupMembersByDNRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getGroupMembersByDNRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
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
@XmlType(name = "getGroupMembersByDNRequestType", propOrder = {
    "groupDN"
})
public class GetGroupMembersByDNRequestType {

    @XmlElement(required = true)
    protected String groupDN;

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
