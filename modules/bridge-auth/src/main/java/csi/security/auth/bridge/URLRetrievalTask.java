package csi.security.auth.bridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class URLRetrievalTask extends TimerTask {

    public static final long ONE_DAY = 24 * 60 * 60 * 1000;

    protected Logger log = Logger.getLogger(URLRetrievalTask.class);

    protected URL remoteURL;

    protected File localFile;

    public URLRetrievalTask(URL remoteLocation, File localFile) {
        if (remoteLocation == null) {
            throw new NullPointerException("remoteLocation");
        }

        if (localFile == null) {
            throw new NullPointerException("localCopy");
        }

        // HACK: ensure that we're dealing w/ a local file.
        if (localFile.exists()) {
            log.warn("Local copy for " + remoteLocation.toString() + " already exists and will be overwritten.");
        }

        this.remoteURL = remoteLocation;
        this.localFile = localFile;
    }

    @Override
    public void run() {
        InputStream istream = null;
        FileOutputStream ostream = null;
        try {
            URLConnection connection = remoteURL.openConnection();
            istream = connection.getInputStream();

            File tempFile = File.createTempFile("Bridge-IC", "crl");
            ostream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[8096];
            int nRead = -1;
            while ((nRead = istream.read(buffer)) != -1) {
                ostream.write(buffer, 0, nRead);
            }

            ostream.flush();

            tempFile.renameTo(localFile);
        } catch (IOException e) {
            log.warn("Encountered an error retrieving " + remoteURL.toString(), e);
        } catch (Throwable t) {
            log.error("Unexpected error occurred retrieving " + remoteURL.toString(), t);
        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
                if (ostream != null) {
                    ostream.close();
                }
            } catch (IOException e) {

            }
        }

    }

}
