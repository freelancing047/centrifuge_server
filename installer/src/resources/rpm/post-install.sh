
grep "centrifuge" /etc/passwd

if [ $? != 0 ]; then
	useradd -m centrifuge
fi

# fix-up .vmoptions files.  shell files do not handle
# \r all that well!
for x in `ls $RPM_INSTALL_PREFIX/bin/*.vmoptions`
do
    sed -i -e "s/\x0d//" $x
done


cd $RPM_INSTALL_PREFIX
tar -xf jre.tar
rm -f jre.tar
cd cachedb
tar -xf pgsql.tar
rm -f pgsql.tar 

chown --recursive centrifuge:centrifuge $RPM_INSTALL_PREFIX
chmod +x $RPM_INSTALL_PREFIX/cachedb/pgsql/bin/*

echo "# Installation completed successfully."  

