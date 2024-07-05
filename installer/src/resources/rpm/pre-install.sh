
#Centrifuge Modification: Do not run as root

if [ $EUID -eq 0 ]; then
  echo "This installation script can not be run using sudo or as the root user."
  exit 1
fi
