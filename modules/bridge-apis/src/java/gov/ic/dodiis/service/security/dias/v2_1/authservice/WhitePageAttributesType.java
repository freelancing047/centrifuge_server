
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a response to a "white page" request.  Contains informational attributes about a user.
 * 
 * <p>Java class for WhitePageAttributesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WhitePageAttributesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="firstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="surName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="uid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="companyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephoneNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="icEMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="siprnetEmail " type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="niprnetEmail " type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WhitePageAttributesType", propOrder = {
    "firstName",
    "surName",
    "uid",
    "companyName",
    "telephoneNumber",
    "icEMail",
    "siprnetEmail0020",
    "niprnetEmail0020"
})
public class WhitePageAttributesType {

    protected String firstName;
    protected String surName;
    protected String uid;
    protected String companyName;
    protected String telephoneNumber;
    protected String icEMail;
    @XmlElement(name = "siprnetEmail ")
    protected String siprnetEmail0020;
    @XmlElement(name = "niprnetEmail ")
    protected String niprnetEmail0020;

    /**
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the surName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurName() {
        return surName;
    }

    /**
     * Sets the value of the surName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSurName(String value) {
        this.surName = value;
    }

    /**
     * Gets the value of the uid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the value of the uid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUid(String value) {
        this.uid = value;
    }

    /**
     * Gets the value of the companyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the value of the companyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyName(String value) {
        this.companyName = value;
    }

    /**
     * Gets the value of the telephoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /**
     * Sets the value of the telephoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephoneNumber(String value) {
        this.telephoneNumber = value;
    }

    /**
     * Gets the value of the icEMail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIcEMail() {
        return icEMail;
    }

    /**
     * Sets the value of the icEMail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIcEMail(String value) {
        this.icEMail = value;
    }

    /**
     * Gets the value of the siprnetEmail0020 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSiprnetEmail_0020() {
        return siprnetEmail0020;
    }

    /**
     * Sets the value of the siprnetEmail0020 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSiprnetEmail_0020(String value) {
        this.siprnetEmail0020 = value;
    }

    /**
     * Gets the value of the niprnetEmail0020 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNiprnetEmail_0020() {
        return niprnetEmail0020;
    }

    /**
     * Sets the value of the niprnetEmail0020 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNiprnetEmail_0020(String value) {
        this.niprnetEmail0020 = value;
    }

}
