package csi.startup;


/**
 * Component initializer -- executed by product startup and shutdown.
 * <p>
 * Currently just given the XML configuration document; so each component needs
 * to locate respective elements in the document and process accordingly.
 * <p>
 * This provides  little flexibility in how each component determines the
 * configuration properties---e.g. does not require everything to be contained in
 * a single element hierarchy.
 * <p>
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface IComponentInitializer {

    void initialize() throws InitializationException;

    void shutdown();

}
