
package csi.server.common.exception;

import java.io.IOException;

/**
 * Created by centrifuge on 8/6/2015.
 */
public class EOBException extends IOException
{
    /**
     * Compatible with JDK 1.0+.
     */
    private static final long serialVersionUID = 6433858223774886977L;

    /**
     * Create an exception without a descriptive error message.
     */
    public EOBException()
    {
    }

    /**
     * Create an exception with a descriptive error message.
     *
     * @param message the descriptive error message
     */
    public EOBException(String message)
    {
        super(message);
    }
} // class EOFException
