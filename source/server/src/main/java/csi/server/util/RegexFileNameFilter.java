/**
 * 
 */
package csi.server.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class RegexFileNameFilter implements FilenameFilter {

    private Pattern regex;
    private boolean acceptDirs;

    public RegexFileNameFilter(String regex, boolean acceptDirs) {
        this.regex = Pattern.compile(regex);
        this.acceptDirs = acceptDirs;
    }

    @Override
    public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        if (f.isDirectory()) {
            return acceptDirs;
        }

        return regex.matcher(name).matches();
    }
}