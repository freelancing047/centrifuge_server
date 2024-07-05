package csi.server.common.dto;


import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class LaunchRequest implements IsSerializable {

    private String targetUuid;
    private String sourceTemplateName;
    private String targetDvName;
    private String remarks;
    private List<AuthDO> auths;
    private List<LaunchParam> params;
    private boolean forceOverwrite = false;
    private boolean migrateACL = false;

    public LaunchRequest() {
        
    }
    
    public LaunchRequest(String targetUuidIn, String sourceNameIn, String targetNameIn, String remarksIn,
            List<LaunchParam> parametersIn, List<AuthDO> authorizationsIn, boolean forceIn, boolean migrateAclIn) {

        this.targetUuid = targetUuidIn;
        this.sourceTemplateName = sourceNameIn;
        this.targetDvName = targetNameIn;
        this.remarks = remarksIn;
        this.auths = authorizationsIn;
        this.params = parametersIn;
        this.forceOverwrite = forceIn;
        this.migrateACL = migrateAclIn;
    }

    public boolean getMigrateACL() {
        return migrateACL;
    }

    public void setMigrateACL(boolean migrateAclIn) {
        this.migrateACL = migrateAclIn;
    }

    public boolean getForceOverwrite() {
        return forceOverwrite;
    }

    public void setForceOverwrite(boolean forceOverwriteIn) {
        this.forceOverwrite = forceOverwriteIn;
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String uuid) {
        this.targetUuid = uuid;
    }

    public String getName() {
        return sourceTemplateName;
    }

    public void setName(String name) {
        this.sourceTemplateName = name;
    }

    public String getTargetDvName() {
        return targetDvName;
    }

    public void setTargetDvName(String newName) {
        this.targetDvName = newName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarksIn) {
        this.remarks = remarksIn;
    }

    public List<AuthDO> getAuths() {
        return auths;
    }

    public void setAuths(List<AuthDO> auths) {
        this.auths = auths;
    }

    public List<LaunchParam> getParams() {
        return params;
    }

    public void setParams(List<LaunchParam> params) {
        this.params = params;
    }
}
