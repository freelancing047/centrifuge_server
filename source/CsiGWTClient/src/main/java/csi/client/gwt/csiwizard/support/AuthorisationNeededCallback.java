package csi.client.gwt.csiwizard.support;

import csi.server.common.dto.AuthDO;


public interface AuthorisationNeededCallback {

    
   public abstract void onSubmit(AuthDO authDO);
   public abstract void onCancel();
}
