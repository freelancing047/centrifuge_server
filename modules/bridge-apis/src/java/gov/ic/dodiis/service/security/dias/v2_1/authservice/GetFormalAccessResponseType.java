
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents the formal accesses a user has.  There can be one or more values here.
 * 
 * <p>Java class for getFormalAccessResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getFormalAccessResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FormalAccess" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getFormalAccessResponseType", propOrder = {
    "formalAccess"
})
public class GetFormalAccessResponseType {

    @XmlElement(name = "FormalAccess", required = true)
    protected List<String> formalAccess;

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

}
