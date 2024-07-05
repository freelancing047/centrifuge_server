/**
 * 
 */
package csi.tools.migrate;

import java.io.File;
import java.io.FilenameFilter;

public class FileNamePrefixFilter implements FilenameFilter {

    private String prefix;

    public FileNamePrefixFilter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return (name.toLowerCase().startsWith(prefix));
    }
}