package csi.config;

import csi.server.common.exception.CentrifugeException;

public interface ConfigurationSettings
{
    void normalize();

    void validate()
        throws ConfigurationException, CentrifugeException;

}
