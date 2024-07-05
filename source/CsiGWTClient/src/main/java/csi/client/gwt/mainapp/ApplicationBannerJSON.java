package csi.client.gwt.mainapp;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import csi.security.jaas.spi.CallBackId;
import csi.security.jaas.spi.callback.LDAPCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.taskdefs.Java;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class ApplicationBannerJSON {
    private static final Logger LOG = LogManager.getLogger(ApplicationBannerJSON.class);

    class IdentityJson extends JavaScriptObject {

        protected IdentityJson() { }

        public final native String getAcess() /*-{ return this.Access; }-*/;
        public final native String getMessage()  /*-{ return this.Message;  }-*/;
    }

    public static String getJSONFromIdentity(Callback[] callBacksIn)  throws LoginException {

        String query = "http://localhost:4000/userauth";

        LDAPCallback myLdapCallback = (LDAPCallback) callBacksIn[CallBackId.LDAP_PASSWORD.getOrdinal()];
        String username = ((NameCallback)callBacksIn[CallBackId.USERNAME.getOrdinal()]).getName();
        String myRawPassword = myLdapCallback.getPassCode();

        String json = "{ \"username\": " + "\"" + username + "\"" +  ", " + "\"password\": " + "\"" + myRawPassword + "\"" + ", " + "\"product\": " + "\"Centrifuge\"" + "}";
        String returnJSON = "";
        try{
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            String jsonResponseString = "";

            try (InputStream inputStream = conn.getInputStream()) {
                jsonResponseString = readInputStream(inputStream);
            }

            conn.disconnect();
            IdentityJson scriptObject;
            if(jsonResponseString.contains("days")) {
                scriptObject = parseJson(jsonResponseString);
                returnJSON = scriptObject.getMessage();
            }
        }catch (Exception myException) {
            LOG.info("           - caught exception:" + display(myException));
        }

        return returnJSON;
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    private static String display(Throwable exceptionIn)
    {
        if ((null != exceptionIn) && (null != exceptionIn.getMessage())
                && (0 < exceptionIn.getMessage().length()))
        {
            return "\n" + exceptionIn.getMessage();
        }
        else
        {
            return "<null>";
        }
    }

    private static <T extends JavaScriptObject> T parseJson(String jsonStr)
    {
        return JsonUtils.safeEval(jsonStr);
    }

}
