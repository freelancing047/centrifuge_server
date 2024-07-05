
package dias;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.ErrorType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the dias package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Error_QNAME = new QName("urn:dias", "error");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: dias
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:dias", name = "error")
    public JAXBElement<ErrorType> createError(ErrorType value) {
        return new JAXBElement<ErrorType>(_Error_QNAME, ErrorType.class, null, value);
    }

}
