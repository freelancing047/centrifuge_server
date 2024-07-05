package csi.server.common.exception;


/**
 * "use me if graph has too many types" - PL
 */
public class GraphTooManyTypesException extends CentrifugeException {

    public GraphTooManyTypesException(String type_limit_exceeded) {
        super(type_limit_exceeded, null);
    }

    public GraphTooManyTypesException() {
        super("TOO_MANY_TYPES", null);
    }
}
