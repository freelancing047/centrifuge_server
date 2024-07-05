
README for upgrading gwt-bootstrap library 

0. If upgrading bootstrap:
	Check if the modal issue is fixed (see WebMain.initialize()) and remove the fix if bootstrap has fixed it.

1. In bootstrap.less
	 Replace sprites.less with ../font-awesome/font-awesome.less
	 
2. Copy the font-awesome.less file from the font-awesome distribution.
     Edit the file and change paths to the less files distributed as part of font-awesome to font-awesome subdirectory.
     Copy the extra font-awesome less files to the font-awesome subdirectory.
