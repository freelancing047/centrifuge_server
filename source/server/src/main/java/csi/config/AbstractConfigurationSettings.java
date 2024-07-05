package csi.config;

import csi.server.common.exception.CentrifugeException;

public abstract class AbstractConfigurationSettings implements ConfigurationSettings {
   public void normalize() {
   }

   public void validate() throws ConfigurationException, CentrifugeException {
   }
}
