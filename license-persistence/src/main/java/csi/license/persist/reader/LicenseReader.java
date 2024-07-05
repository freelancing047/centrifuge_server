package csi.license.persist.reader;

import java.io.InputStream;

import csi.license.LicenseException;
import csi.license.persist.persistence.AbstractLicensePersistence;

public interface LicenseReader {
   public AbstractLicensePersistence read(InputStream stream) throws LicenseException;
}
