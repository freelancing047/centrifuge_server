package csi.AntTasks;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

public class CsiSPDiff extends Task {
	private String installdir = null;
	private String builddir = null;
	private String destdir = null;
	private Vector<FileSet> filesets = new Vector<FileSet>();
	private int filecount = 0;
	private Boolean chksize = new Boolean(true);
	private Boolean chktime = new Boolean(false);
	private Boolean chkmissing = new Boolean(true);
	private Boolean chkdigest = new Boolean(true);
	private String difftype = null;

	public void setInstalldir(String installdir) {
		this.installdir = installdir;
	}
	
	public void setBuilddir(String builddir) {
		this.builddir = builddir;
	}
	
	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}
	
	public void setChksize(Boolean chksize) {
		this.chksize = chksize;
	}

	public void setChktime(Boolean chktime) {
		this.chktime = chktime;
	}

	public void setChkmissing(Boolean chkmissing) {
		this.chkmissing = chkmissing;
	}

	public void setChkdigest(Boolean chkdigest) {
		this.chkdigest = chkdigest;
	}

	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}
	
	public CsiSPDiff() {
		super();
	}
	
	private void validate() throws BuildException {
		System.out.println("installdir="+installdir);
		System.out.println("builddir="+builddir);
		System.out.println("DestDir="+destdir);
		if (installdir == null) {
			throw new BuildException("CsiSPDiff: missing install directory");
		}
		if (builddir == null) {
			throw new BuildException("CsiSPDiff: missing build directory");
		}
		if (filesets == null || filesets.size() < 1) {
			throw new BuildException("CsiSPDiff: missing Fileset");
		}
		if (destdir == null) {
			throw new BuildException("CsiSPDiff: missing destination directory");
		}
		if (!(new File(installdir).exists())) {
			throw new BuildException("CsiSPDiff: install directory does not exist");
		}
		if (!(new File(builddir).exists())) {
			throw new BuildException("CsiSPDiff: build directory does not exist");
		}
		if (!(new File(destdir).exists())) {
			throw new BuildException("CsiSPDiff: destination directory does not exist");
		}
	}
	public void execute() throws BuildException {
		validate();

        for(Iterator itFSets = filesets.iterator(); itFSets.hasNext(); ) {  
            FileSet fs = (FileSet)itFSets.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());         
            String[] includedFiles = ds.getIncludedFiles();
            for(int i=0; i<includedFiles.length; i++) {
                String filename = includedFiles[i].replace('\\','/');
                File installfile = new File(installdir + "/" + filename);
                File buildfile = new File(builddir + "/" + filename);
                if (fileChanged(installfile, buildfile)) {
                	copyToDest(destdir, filename, buildfile);
                }
            }
        }
        System.out.println("CsiSPDiff: "+filecount+" Service Pack files copied.");
	}
	
	private void copyToDest(String destdir, String filename, File buildfile) throws BuildException {
		File destfile = new File(destdir + "/" + filename);
		try {
			FileUtils.copyFile(buildfile, destfile);
			filecount++;
		}
		catch(Exception e) {
			throw new BuildException("CsiSPDiff: copy failed: "+ e);
		}
		System.out.println("CsiSPDiff: copied file "+difftype+": " + destfile.getAbsolutePath());
	}
	
	private boolean fileChanged(File installfile, File buildfile) throws BuildException {
		if (!installfile.exists()) {
			difftype = "(missing)";
            return chkmissing.booleanValue();
		}
		
		if (chksize.booleanValue() && (buildfile.length() != installfile.length())) {
			difftype = "(size)";
			return true;
		}
		
		if (chktime.booleanValue() && (buildfile.lastModified() > installfile.lastModified())) {
			difftype = "(time)";
			return true;
		}
		
		if (chkdigest.booleanValue()) {
			return compareMD5(installfile, buildfile);
		}
		return false;
	}
	
	private boolean compareMD5(File installfile, File buildfile) throws BuildException {
		byte[] installdigest = getFileDigest(installfile);
		byte[] builddigest = getFileDigest(buildfile);
		boolean rc = !(Arrays.equals(installdigest, builddigest));
		if (rc) {
			difftype = "(digest)";
			if (buildfile.getName().lastIndexOf("jar") > -1) {
				rc = compareJars(installfile, buildfile);
			}
		}
		return rc;
	}
	
	private boolean compareJars(File installfile, File buildfile) throws BuildException {
		JarFile installjar = null;
		JarFile buildjar = null;
		HashMap<String, JarEntry> classes = new HashMap<String, JarEntry>();
		JarEntry je = null;
		
		try {
			installjar = new JarFile(installfile);
			buildjar = new JarFile(buildfile);
		}
		catch(Exception e) {
			throw new BuildException("CsiSPDiff: jar error: "+e);
		}
		
		Enumeration<JarEntry> jenum = buildjar.entries();
		while (jenum.hasMoreElements()) {
			je = (JarEntry)jenum.nextElement();
			classes.put(je.getName(), je);
		}
		jenum = installjar.entries();
		while (jenum.hasMoreElements()) {
			je = (JarEntry)jenum.nextElement();
			JarEntry bldje = classes.get(je.getName());
			if (bldje == null) {
				difftype = "jar: missing class";
				return true;
			}
			if ((bldje.hashCode() != je.hashCode()) || (bldje.getSize() != je.getSize())) {
				difftype = "jar: class";
				return true;
			}
		}
		classes.clear();
		return false;  // jars match
	}
	
	private byte[] getFileDigest(File file) throws BuildException {
		MessageDigest digest = null;
		int rc = 0;
		int size = 1024 * 1024;
		byte[] buf = new byte[size];
		
		try {
			digest = MessageDigest.getInstance("MD5");
			FileInputStream in = new FileInputStream(file);
			do {
				rc = in.read(buf, 0, size);
				if (rc > 0) {
					digest.update(buf, 0, rc);
				}
			} while(rc > -1);
			return digest.digest();
		}
		catch (Exception e) {
			throw new BuildException("CsiSPDiff: create digest error: "+ e);
		}
		
	}
}
