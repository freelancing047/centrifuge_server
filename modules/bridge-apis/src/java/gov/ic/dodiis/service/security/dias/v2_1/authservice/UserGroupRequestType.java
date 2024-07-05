
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a request for user groups
 * 
 * <p>Java class for UserGroupRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserGroupRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userDN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="projectName" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserGroupRequestType", propOrder = {
    "userDN",
    "projectName"
})
public class UserGroupRequestType {

    @XmlElement(required = true)
    protected String userDN;
    @XmlElement(required = true)
    protected List<String> projectName;

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
     * Gets the value of the projectName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the projectName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProjectName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getProjectName() {
        if (projectName == null) {
            projectName = new ArrayList<String>();
        }
        return this.projectName;
    }

}
