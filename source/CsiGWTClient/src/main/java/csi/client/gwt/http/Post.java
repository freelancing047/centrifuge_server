package csi.client.gwt.http;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

public class Post {

    public static Request logout(RequestCallback callbackIn) {
        
        Request myRequest = null;

        try {
            RequestBuilder myBuilder = new RequestBuilder(RequestBuilder.POST, "/Centrifuge/api/logout/post");
            myBuilder.setCallback(callbackIn);
            myRequest = myBuilder.send();
            
        } catch (RequestException myException) {
            
            Dialog.showException(CentrifugeConstantsLocator.get().post_title(), myException);
        }
        return myRequest;
    }

    public static Request logout() {
        
        return logout(logoutCallback);
    }
    
    private static RequestCallback logoutCallback = new RequestCallback() {
        
        public void onResponseReceived(Request request, Response response) {
            
            //Window.Location.reload();
            if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
                Window.Location.replace("/Centrifuge/admin1/logout2.jsp");
            } else {
                Window.Location.replace("/Centrifuge/admin1/logout.jsp");
            }
        }
        
        public void onError(Request request, java.lang.Throwable exception) {
            
        }
    };
}
