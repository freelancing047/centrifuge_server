package csi.container.tomcat;

/**
 * Created by centrifuge on 6/15/2016.
 */
public class ConfigResponse {
   private String _authenticationOrder = null;
   private String _userHeaderKey = null;
   private String _kerberosKeyTab = null;
   private String _kerberosPrincipal = null;
   private boolean _kerberosUseDomainName = false;
   private boolean _doDebug = false;

   public ConfigResponse(String authenticationOrderIn, String userHeaderKeyIn, String kerberosKeyTabIn,
                         String kerberosPrincipalIn, boolean kerberosUseDomainNameIn, boolean doDebugIn) {
      _authenticationOrder = authenticationOrderIn;
      _userHeaderKey = userHeaderKeyIn;
      _kerberosKeyTab = kerberosKeyTabIn;
      _kerberosPrincipal = kerberosPrincipalIn;
      _kerberosUseDomainName = kerberosUseDomainNameIn;
      _doDebug = doDebugIn;
   }

   public String getAuthenticationOrder() {
      return _authenticationOrder;
   }

   public void setAuthenticationOrder(String authenticationOrderIn) {
      _authenticationOrder = authenticationOrderIn;
   }

   public String getUserHeaderKey() {
      return _userHeaderKey;
   }

   public void setUserHeaderKey(String userHeaderKeyIn) {
      _userHeaderKey = userHeaderKeyIn;
   }

   public String getKerberosKeyTab() {
      return _kerberosKeyTab;
   }

   public void setKerberosKeyTab(String kerberosKeyTabIn) {
      _kerberosKeyTab = kerberosKeyTabIn;
   }

   public String getKerberosPrincipal() {
      return _kerberosPrincipal;
   }

   public void setKerberosPrincipal(String kerberosPrincipalIn) {
      _kerberosPrincipal = kerberosPrincipalIn;
   }

   public boolean getKerberosUseDomainName() {
      return _kerberosUseDomainName;
   }

   public void setKerberosUseDomainName(boolean kerberosUseDomainNameIn) {
      _kerberosUseDomainName = kerberosUseDomainNameIn;
   }

   public boolean getDoDebug() {
      return _doDebug;
   }

   public void setDoDebug(boolean doDebugIn) {
      _doDebug = doDebugIn;
   }
}
