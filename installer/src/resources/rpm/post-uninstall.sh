if [ "x$1" = "x0" ]; then
   # forced removal of centrifuge user
    userdel --force --remove centrifuge
fi

if [ -f "$RPM_INSTALL_PREFIX" ]; then
    rm -rf $RPM_INSTALL_PREFIX
fi
