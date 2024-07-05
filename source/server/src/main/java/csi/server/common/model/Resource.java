package csi.server.common.model;

import static javax.persistence.CascadeType.ALL;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;

@Entity(name = "ModelResource")
@Inheritance(strategy = InheritanceType.JOINED)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Resource extends ModelObject {

    protected String name;
    protected String remarks;
    protected Date createDate;
    protected Date lastOpenDate;
    protected Date lastUpdateDate;
    protected boolean template = false;
    protected AclResourceType priorType;
    protected AclResourceType resourceType;
    protected long size;
    protected int useCount;
    protected String owner;
    protected Long aclId;
    protected int flags;

    @OneToOne(cascade = ALL, orphanRemoval = true)
    protected CapcoInfo capcoInfo = null;
    @OneToOne(cascade = ALL, orphanRemoval = true)
    protected SecurityTagsInfo securityTagsInfo = null;

    public Resource() {
        super();
    }

    public Resource(AclResourceType typeIn) {
        this();
        resourceType = typeIn;
    }

    public Resource(AclResourceType typeIn, String nameIn, String remarksIn) {
        this(typeIn);
        setName(nameIn);
        setRemarks(remarksIn);
    }

    public boolean getTemplate() {
        return template;
    }

    public boolean getComponent() {
        return !template;
    }

    public void setTemplate(boolean templateIn) {
        template = templateIn;
    }

    public void setComponent(boolean componentIn) {
        template = !componentIn;
    }

    public boolean isTemplate() {
        return getTemplate();
    }

    public boolean isComponent() {
        return getComponent();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDateIn) {
        createDate = createDateIn;
    }

    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDateIn) {
        lastOpenDate = lastOpenDateIn;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDateIn) {
        lastUpdateDate = lastUpdateDateIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String ownerIn) {
        owner = ownerIn;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclIdIn) {
        aclId = aclIdIn;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarksIn) {
        remarks = remarksIn;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long sizeIn) {

        size = sizeIn;
    }

    public int getUseCount() {

        return useCount;
    }

    public void setUseCount(int useCountIn) {

        useCount = useCountIn;
    }

    public AclResourceType getResourceType() {

        return resourceType;
    }

    public void setFlags(int flagsIn) {

        flags = flagsIn;
    }

    public int getFlags() {

        return flags;
    }

    public void setResourceType(AclResourceType resourceTypeIn) {

        resourceType = resourceTypeIn;
    }

    public AclResourceType getPriorType() {

        return priorType;
    }

    public void setPriorType(AclResourceType priorTypeIn) {

        priorType = priorTypeIn;
    }

    public void markForDelete() {

        priorType = resourceType;
        resourceType = AclResourceType.DISCARDED;
    }

    public SecurityTagsInfo getSecurityTagsInfo() {

        return securityTagsInfo;
    }

    public void setSecurityTagsInfo(SecurityTagsInfo securityTagsInfoIn) {

        securityTagsInfo = securityTagsInfoIn;
    }

    public CapcoInfo getCapcoInfo() {

        return capcoInfo;
    }

    public void setCapcoInfo(CapcoInfo capcoInfoIn) {

        capcoInfo = capcoInfoIn;
    }

    public String getSecurityBanner(String bannerPrefix, String bannerDelimiter, String bannerSubDelimiter,
                                    String bannerSuffix, String tagItemPrefix) {

        return ((null != securityTagsInfo) && securityTagsInfo.hasTags())
                ? securityTagsInfo.getBanner(bannerPrefix, bannerDelimiter,
                bannerSubDelimiter, bannerSuffix, tagItemPrefix)
                : null;
    }

    public String getSecurityBanner(String defaultBannerIn) {

        String myBanner = (null != capcoInfo) ? capcoInfo.getBanner() : null;
        return (null != myBanner) ? myBanner : defaultBannerIn;
    }

    public String getSecurityBannerAbr(String defaultBannerIn) {

        String myBanner = (null != capcoInfo) ? capcoInfo.getAbbreviation() : null;
        return (null != myBanner) ? myBanner : defaultBannerIn;
    }

    public String getSecurityBanner(String defaultBannerIn, String tagBannerIn) {

        return CapcoInfo.appendDissemination(getSecurityBanner(defaultBannerIn), tagBannerIn);
    }

    public String getSecurityBannerAbr(String defaultBannerIn, String tagBannerIn) {

        return CapcoInfo.appendDissemination(getSecurityBannerAbr(defaultBannerIn), tagBannerIn);
    }

    public String getSecurityPortion() {

        return ((null != capcoInfo) && (CapcoSource.USE_DEFAULT != capcoInfo.getMode()))
                ? capcoInfo.getPortion() : null;
    }

    public Set<String> getDataSourceKeySet() {

        return new TreeSet<String>();
    }

    public void resetDates() {

        createDate = null;
        lastUpdateDate = null;
        lastOpenDate = null;

        guaranteeCreateDate();
    }

    public void updateLastUpdate() {

        Date myDate = guaranteeCreateDate();

        setLastUpdateDate(myDate);
        setLastOpenDate(myDate);
    }

    public void updateLastDataChange() {

        setLastUpdateDate(guaranteeCreateDate());
    }

    public void updateLastOpen() {

        setLastOpenDate(guaranteeCreateDate());
    }

    private Date guaranteeCreateDate() {

        Date myDate = new Date();

        if (null == createDate) {

            createDate = new Date(myDate.getTime());
        }
        return myDate;
    }

    public void resetSecurity() {

        if (null != capcoInfo) {

            capcoInfo.reset();
        }
        if (null != securityTagsInfo) {

            securityTagsInfo.reset();
        }
    }

    public void updateInPlace(Resource sourceIn) {

        sourceIn.cloneValues(this);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T clone(T resourceIn) throws CentrifugeException {

        try {

            return (T) resourceIn.clone();

        } catch (Exception myException) {

            throw new CentrifugeException("", myException);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T genTemplate(T resourceIn, String nameIn, String remarksIn) throws CentrifugeException {

        try {

            T myTemplate = (T)resourceIn.clone();

            if (null != nameIn) {

                myTemplate.setName(nameIn);
            }

            if (null != remarksIn) {

                myTemplate.setRemarks(remarksIn);
            }
            myTemplate.setTemplate(true);

            return myTemplate;

        } catch (Exception myException) {

            throw new CentrifugeException("", myException);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> T genComponent(T resourceIn) throws CentrifugeException {

        try {

            T myComponent = (T)resourceIn.clone();

            myComponent.setComponent(true);

            return myComponent;

        } catch (Exception myException) {

            throw new CentrifugeException("", myException);
        }
    }

    @Override
    public Resource clone() {

        Resource myClone = new Resource(getResourceType());

        cloneComponents(myClone);

        return myClone;
    }

    @Override
    public Resource fullClone() {

        Resource myClone = new Resource(getResourceType());

        fullCloneComponents(myClone);

        return myClone;
    }

    protected void cloneComponents(Resource cloneIn) {

        super.cloneComponents(cloneIn);
        cloneValues(cloneIn);
    }

    protected void copyComponents(Resource cloneIn) {

        super.copyComponents(cloneIn);
        cloneValues(cloneIn);
    }

    protected void fullCloneComponents(Resource cloneIn) {

        super.fullCloneComponents(cloneIn);
        cloneValues(cloneIn);
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, remarks, indentIn, "remarks");
        debugObject(bufferIn, createDate, indentIn, "createDate");
        debugObject(bufferIn, lastOpenDate, indentIn, "lastOpenDate");
        debugObject(bufferIn, lastUpdateDate, indentIn, "lastUpdateDate");
        debugObject(bufferIn, template, indentIn, "template");
    }

    public void cloneValues(Resource cloneIn) {

        cloneIn.setName(name);
        cloneIn.setRemarks(remarks);
        cloneIn.setCreateDate(createDate);
        cloneIn.setLastOpenDate(lastOpenDate);
        cloneIn.setLastUpdateDate(lastUpdateDate);
        cloneIn.setTemplate(template);
        cloneIn.setPriorType(priorType);
        cloneIn.setResourceType(resourceType);
        cloneIn.setSize(size);
        cloneIn.setUseCount(useCount);
        cloneIn.setOwner(owner);
        cloneIn.setAclId(aclId);
    }
}
