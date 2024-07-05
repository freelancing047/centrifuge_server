
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents all the attributes related to a user's clearance
 * 
 * <p>Java class for UserClearanceInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserClearanceInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Clearance" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FormalAccess" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Citizenship" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserClearanceInfoType", propOrder = {
    "clearance",
    "formalAccess",
    "citizenship"
})
public class UserClearanceInfoType {

    @XmlElement(name = "Clearance", required = true)
    protected String clearance;
    @XmlElement(name = "FormalAccess")
    protected List<String> formalAccess;
    @XmlElement(name = "Citizenship", required = true)
    protected String citizenship;

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

    /**
     * Gets the value of the formalAccess property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formalAccess property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormalAccess().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFormalAccess() {
        if (formalAccess == null) {
            formalAccess = new ArrayList<String>();
        }
        return this.formalAccess;
    }

    /**
     * Gets the value of the citizenship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCitizenship() {
        return citizenship;
    }

    /**
     * Sets the value of the citizenship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCitizenship(String value) {
        this.citizenship = value;
    }

}
