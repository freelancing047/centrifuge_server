/**
 * 
 */
package csi.server.util;

import java.io.File;
import java.io.FilenameFilter;

public class FileExtensionFilter implements FilenameFilter {

    private String[] extensions;
    private boolean acceptDirs;

    public FileExtensionFilter(String[] extensions, boolean acceptDirs) {
        this.extensions = extensions;
        this.acceptDirs = acceptDirs;
    }

    @Override
    public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        if (f.isDirectory()) {
            return acceptDirs;
        }

        String lower = name.toLowerCase();
        for (String ext : extensions) {
            if (lower.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}