package csi.security.jaas.spi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SSLTestingWorkaround extends SSLSocketFactory {
   private static final Logger LOG = LogManager.getLogger(SSLTestingWorkaround.class);

   private SSLSocketFactory socketFactory;

   public SSLTestingWorkaround() {
      LOG.info(">> >> >>  SSLTestingWorkaround::SSLTestingWorkaround()");
      LOG.info(
            "\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  WARNING LDAP SERVER IS NOT BEING AUTHENTICATED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n");
      LOG.info("Remove \"ldapTrustServer.DEBUG_ONLY=true\" from the \"jaas.config\" file.\n\n\n");

      try {
         SSLContext ctx = SSLContext.getInstance("TLS");
         ctx.init(null, new TrustManager[] { new DummyTrustmanager() }, new SecureRandom());
         socketFactory = ctx.getSocketFactory();
      } catch (Exception ex) {
         ex.printStackTrace(System.err);
         /* handle exception */ }
      LOG.info("<< << <<  SSLTestingWorkaround::SSLTestingWorkaround()");
   }

   public static SocketFactory getDefault() {
      LOG.info("<> <> <>  SSLTestingWorkaround::getDefault()");
      return new SSLTestingWorkaround();
   }

   @Override
   public String[] getDefaultCipherSuites() {
      return socketFactory.getDefaultCipherSuites();
   }

   @Override
   public String[] getSupportedCipherSuites() {
      return socketFactory.getSupportedCipherSuites();
   }

   @Override
   public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
      return socketFactory.createSocket(socket, string, i, bln);
   }

   @Override
   public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
      return socketFactory.createSocket(string, i);
   }

   @Override
   public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
      return socketFactory.createSocket(string, i, ia, i1);
   }

   @Override
   public Socket createSocket(InetAddress ia, int i) throws IOException {
      return socketFactory.createSocket(ia, i);
   }

   @Override
   public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
      return socketFactory.createSocket(ia, i, ia1, i1);
   }
}
