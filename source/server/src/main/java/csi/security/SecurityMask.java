package csi.security;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.server.common.enumerations.AclSecurityMode;
import csi.server.common.exception.CsiSecurityException;

/**
 * Created by centrifuge on 9/29/2016.
 */
/*
AclSecurityMode
    CONFIGURED_ACCESS, CONFIGURED_SOURCE_EDIT, CONFIGURED_EXPORT, CAPCO, GENERIC

 */
public class SecurityMask {

    private static final int _configuredAccessMask = (1 << AclSecurityMode.CONFIGURED_ACCESS.ordinal());
    private static final int _configuredSourceEditMask = (1 << AclSecurityMode.CONFIGURED_SOURCE_EDIT.ordinal());
    private static final int _configuredExportMask = (1 << AclSecurityMode.CONFIGURED_EXPORT.ordinal());
    private static final int _capcoMask = (1 << AclSecurityMode.CAPCO.ordinal());
    private static final int _genericMask = (1 << AclSecurityMode.GENERIC.ordinal());

    private static Integer _master = null;

    private int _mask = 0;


    public static SecurityMask getDefaultSecurityMask() throws CsiSecurityException {

        return new SecurityMask(_capcoMask | _genericMask | _configuredAccessMask);
    }

    public static SecurityMask getSourceEditSecurityMask() throws CsiSecurityException {

        return new SecurityMask(_capcoMask | _genericMask | _configuredSourceEditMask);
    }

    public static SecurityMask getExportSecurityMask() throws CsiSecurityException {

        return new SecurityMask(_capcoMask | _genericMask | _configuredExportMask);
    }

    public static SecurityMask getNoSecurityMask() throws CsiSecurityException {

        return new SecurityMask(0);
    }

    public static SecurityMask getTotalSecurityMask() throws CsiSecurityException {

        return new SecurityMask(-1);
    }

    SecurityMask() { }

    public SecurityMask(boolean useDefaultIn) throws CsiSecurityException {

        if (useDefaultIn) {

            if (0 != getMaster()) {

                _mask = getMaster() & (_capcoMask | _genericMask | _configuredAccessMask);
            }
        }
    }

    public SecurityMask(int maskIn) throws CsiSecurityException {

        _mask = maskIn & getMaster();
    }

    public SecurityMask(AclSecurityMode[] modeArrayIn) throws CsiSecurityException {

        if (null != modeArrayIn) {

            if (0 != getMaster()) {

                for (AclSecurityMode myMode : modeArrayIn) {

                    _mask |= ((1 << myMode.ordinal()) & getMaster());
                }
            }
        }
    }

    public void setMask(Integer maskIn) throws CsiSecurityException {

        _mask = (maskIn & getMaster());
    }

    public Integer getMask() {

        return _mask;
    }

    public boolean hasSecurity()  {

        return (0 != _mask);
    }

    public boolean hasCapcoRestrictions() {

        return (0 != (_capcoMask & _mask));
    }

    public boolean hasGenericRestrictions() {

        return (0 != (_genericMask & _mask));
    }

    public boolean hasConfiguredAccessRestrictions() {

        return (0 != (_configuredAccessMask & _mask));
    }

    public boolean hasConfiguredSourceEditRestrictions() {

        return (0 != (_configuredSourceEditMask & _mask));
    }

    public boolean hasConfiguredExportRestrictions() {

        return (0 != (_configuredExportMask & _mask));
    }

    public void addCapcoRestrictions() throws CsiSecurityException {

        _mask |= (_capcoMask & getMaster());
    }

    public void addGenericRestrictions() throws CsiSecurityException {

        _mask |= (_genericMask & getMaster());
    }

    public void addConfiguredAccessRestrictions() throws CsiSecurityException {

        _mask |= (_configuredAccessMask & getMaster());
    }

    public void addConfiguredSourceEditRestrictions() throws CsiSecurityException {

        _mask |= (_configuredSourceEditMask & getMaster());
    }

    public void addConfiguredExportRestrictions() throws CsiSecurityException {

        _mask |= (_configuredExportMask & getMaster());
    }

    private Integer getMaster() throws CsiSecurityException {

        if (null == _master) {

            SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();

            _master = AclTagRepository.getConfiguredSecurityMask();

            if (policyConfig.getEnforceAccessRestrictions().booleanValue()) {

                _master |= _capcoMask;
            }
            if (policyConfig.getEnforceDataSecurityTags().booleanValue()) {

                _master |= _genericMask;
            }
        }
        return _master;
    }
}
