package csi.server.business.service.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import csi.config.ClientConfig;
import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.server.business.service.AbstractService;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.dto.FileValidation;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.FileActionsServiceProtocol;
import csi.server.gwt.vortex.HttpAccessor;
import csi.shared.gwt.exception.FileNotFoundException;
import csi.shared.gwt.exception.SecurityException;

@Service(path = "/actions/file")
public class FileActionsService extends AbstractService implements FileActionsServiceProtocol {
   private static final Pattern NON_FILE_NAME_CHARACTERS_PATTERN = Pattern.compile("[\\/|*?:<>%\"]");

    private String[] authorizedRoles = new String[] { };
    private List<String> folderVisibility = new ArrayList<String>();

    @Autowired
    ServletContext servletContext;

    @Inject
    private HttpAccessor httpAccessor;

    public HttpAccessor getHttpAccessor() {
        return httpAccessor;
    }

    public void setHttpAccessor(HttpAccessor httpAccessor) {
        this.httpAccessor = httpAccessor;
    }

    // TODO: Setup a spring external properties file to populate this.
    public void setAuthorizedRoles(String authorizedRoles) {
        this.authorizedRoles = Iterables.toArray(
                Splitter.on(',').omitEmptyStrings().trimResults().split(authorizedRoles), String.class);
    }

    public void setFolderVisibility(String folders) {
        folderVisibility = Lists.newArrayList(Splitter.on(',').omitEmptyStrings().trimResults().split(folders));
    }

    /*
     * Validate the provided filename to ensure that it doesn't contain illegal characters and the file doesn't already
     * exist.
     */
   @Operation
   @Override
   public FileValidation validateName(@PayloadParam String filename) throws CentrifugeException {
      if (filename == null) {
         throw new CentrifugeException("Missing filename for validateName");
      }
      FileValidation results = new FileValidation();
      String userName = CsiSecurityManager.getUserName();

      if (NON_FILE_NAME_CHARACTERS_PATTERN.matcher(filename).find() || ".".equals(filename) ||
            "..".equals(filename) || filename.contains(File.separator)) {
         results.invalidChars = true;
      }
      File subject = new File("userfiles" + File.separator + userName + File.separator + "datafiles" + File.separator + filename);
      results.nameInUse = subject.exists();
      return results;
   }

   private String getResourcePath(String resourceName) {
      if (resourceName.startsWith(File.separator)) {
         return "/resources" + resourceName;
      } else {
         return "/resources" + File.separator + resourceName;
      }
   }

    @Override
    public String getApplicationResource(String resourceName) {
        if (!CsiSecurityManager.hasAnyRole(authorizedRoles)) {
            Throwables.propagate(new SecurityException("Authorization failed, access denied."));
        }

        boolean folderVisible = folderVisibility.isEmpty();
        for (String visibleFolder : folderVisibility) {
            if (resourceName.startsWith(visibleFolder)) {
                folderVisible = true;
                break;
            }
        }
        if (!folderVisible) {
            Throwables.propagate(new SecurityException("Authorization failed, access denied."));
        }

        InputStream is = null;
        try {
            is = servletContext.getResourceAsStream(getResourcePath(resourceName));
            if (is == null) {
                throw Throwables.propagate(new FileNotFoundException(resourceName));
            }
            return IOUtils.toString(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public List<String> getApplicationResourceDirectories(String resourceName) {
        return getDirectoryInfo(resourceName, true);
    }

    @Override
    public List<String> getApplicationResourceFiles(String resourceName) {
        return getDirectoryInfo(resourceName, false);
    }

    @Override
    public Long getMaximumClientBufferSize() {

        ClientConfig myConfig = Configuration.getInstance().getClientConfig();

        return myConfig.getMaxClientBufferSize();
    }

    /**
     * @param resourceName
     * @param foldersOnly if true only folders are returned. Otherwise only files are returned.
     * @return
     */
    private List<String> getDirectoryInfo(String resourceName, boolean foldersOnly) {
        if (!CsiSecurityManager.hasAnyRole(authorizedRoles)) {
            Throwables.propagate(new SecurityException("Authorization failed, access denied."));
        }

        boolean folderVisible = folderVisibility.isEmpty();
        for (String visibleFolder : folderVisibility) {
            if (resourceName.startsWith(visibleFolder)) {
                folderVisible = true;
                break;
            }
        }
        if (!folderVisible) {
            Throwables.propagate(new SecurityException("Authorization failed, access denied."));
        }

        Set<String> paths = servletContext.getResourcePaths(getResourcePath(resourceName));

        // Note: the paths returned is an immutable set that will fail to go past vortex transformers.
        List<String> list = new ArrayList<String>();
        for (String path : paths) {
            if (foldersOnly && path.endsWith(File.separator)) {
                String str = path.substring((getResourcePath(resourceName) + File.separator).length());
                list.add(str.substring(0, str.length() - 1));
            } else if (!foldersOnly && !path.endsWith(File.separator)) {
                String str = path.substring((getResourcePath(resourceName) + File.separator).length());
                list.add(str.substring(0, str.length() - 1));
            }
        }
        return new ArrayList<String>(paths);
    }
}
