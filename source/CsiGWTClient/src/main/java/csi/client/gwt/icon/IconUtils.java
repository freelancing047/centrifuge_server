package csi.client.gwt.icon;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.icons.Icon;
import csi.server.common.service.api.IconActionsServiceProtocol;

public class IconUtils {
    
    public static SafeUri generateIconDownloadUri(String iconId) {
        String hostUrl = DownloadHelper.hostPageURLWithoutH5();
        return UriUtils.fromString(hostUrl + "iconProvider?id=" + iconId);
    }

    public static void getBase64(String iconId, AbstractVortexEventHandler<String> handler) {
        final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(IconActionsServiceProtocol.class).getDataUrlImage(iconId);
        } catch (CentrifugeException e) {
            
        }
        futureTask.addEventHandler(handler);
    }
    
    public static Image createDataUrl(Icon icon) {
        
        //String base64 = Base64Util.toBase64(icon.getImage());
        String base64 = icon.getImage();
        base64 += "data:image/png;base64,";
        return new Image(base64);
    }

}
