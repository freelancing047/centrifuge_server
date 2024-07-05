/**
 * 
 */
package csi.tools.migrate;

import java.io.File;
import java.io.FilenameFilter;

public class FileExtensionFilter implements FilenameFilter {

    private String[] extensions;

    public FileExtensionFilter(String[] extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean accept(File dir, String name) {
        String lower = name.toLowerCase();
        for (String ext : extensions) {
            if (lower.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}