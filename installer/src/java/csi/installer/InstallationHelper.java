package csi.installer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.license.InstallerDigestLicenseReader;
import csi.license.InstallerLicense;

public class InstallationHelper {
   private static final Logger LOG = LogManager.getLogger(InstallationHelper.class);
    
    public static String getClipboardData( JComponent component )
    {
        try {
            Toolkit toolkit = component.getToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            Object o = clipboard.getData( DataFlavor.stringFlavor );
            String value  = ( o == null ) ? null : o.toString();
            return value;
        } catch( Throwable t ) {
            
           LOG.error( "Failed to obtain clipboard value: " + t.getMessage() );
        }
        
        return null;
    }
    
    public static InstallerLicense createLicense( JTextComponent component )
    {
        if( component == null ) {
            return null;
        }
        
        String text = component.getText();
        text = ( text != null ) ? text.trim() : "";
        
        if( text.length() == 0 ) {
            return null;
        }
        
        InstallerDigestLicenseReader reader = new InstallerDigestLicenseReader();
        InstallerLicense license = reader.read( text );
        
        return license;
    }
    
    public static boolean isExpired( InstallerLicense license ) {
        if( license == null ) {
            return true;
        }
        
        Date now = new Date();
        boolean isExpired = license.is_expiring() && now.after(license.get_expiration_date());
        
        return isExpired;
    }

    
    public static void addPasteMenu( JTextComponent component )
    {
        final JTextComponent target = (JTextField)component;
        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem item = new JMenuItem( "Paste     Ctrl-V" );
        item.addActionListener( new ActionListener(){
            
            @Override
            public void actionPerformed( ActionEvent e )
            {
                String value = getClipboardData( target );
                value = (value != null ) ? value.trim() : "";
                if( value.length() > 0 ) {
                    target.setText( value );
                }
            }
        });
        
        popupMenu.add( item );
        
        target.addMouseListener( new MouseAdapter(){

            @Override
            public void mousePressed( MouseEvent e )
            {
                maybeShowMenu( e );
            }

            @Override
            public void mouseReleased( MouseEvent e )
            {
                maybeShowMenu( e );
            }
            
            private void maybeShowMenu( MouseEvent e )
            {
                if( e != null && e.isPopupTrigger() ) {
                    popupMenu.show( e.getComponent(), e.getX(), e.getY() );
                }
            }
            
        });
    }
    
}
