package csi.dias;

import gov.ic.dodiis.service.security.dias.v2_1.authservice.UserIdentifierType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.WhitePageAttributesType;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import dias.DIASMessageReceiver;
import dias.DIASMessageReceiverService;
import dias.Error;

public class DIASTest
{
    public static void main( String[] args )
            throws Error, Throwable
    {

        String property = System.getProperty( "java.class.path" );
        String sep = System.getProperty( "path.separator" );

        String[] split = property.split( sep );
        URL[] jars = new URL[ split.length ];
        for( int i = 0; i < jars.length; i++ ) {
            jars[ i ] = new File( split[ i] ).toURL();
        }

        LocalClassLoader classLoader = new LocalClassLoader();

        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader( classLoader );

        DIASMessageReceiverService service = new DIASMessageReceiverService();

        DIASMessageReceiver dias = service.getDIAS();

        UserIdentifierType request = new UserIdentifierType();
        String dn = "EMAILADDRESS=eduardo@centrifugesystems.com, CN=Eduardo.Saltelli, O=Centrifuge Systems Inc, ST=Virginia, C=US";

        request.setDN( dn );
        WhitePageAttributesType response = dias.getUserInfoAttributes( request );

        dumpResponse( response );
    }

    private static void dumpResponse( WhitePageAttributesType response )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( response.getFirstName() ).append( " " ).append( response.getSurName() ).append( "\n" );
        buf.append( response.getCompanyName() ).append( "\n" );
        buf.append( response.getUid() );

        System.out.println( buf.toString() );
    }

    static class LocalClassLoader
            extends URLClassLoader
    {

        public LocalClassLoader()
        {
            super( new URL[ 0 ] );
        }

        @Override
        public URL findResource( String name )
        {
            if( name.startsWith( "/META-INF" ) ) {
                super.findResource( name );

            }
            return super.findResource( name );
        }

    }

}
