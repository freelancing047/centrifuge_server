package csi.license.persist.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class AbstractLicensePersistenceFactory<T extends AbstractLicensePersistence> {
   public abstract T create(final ByteArrayInputStream bis) throws IOException;
}
