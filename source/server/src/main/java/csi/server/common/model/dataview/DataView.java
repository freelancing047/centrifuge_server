package csi.server.common.model.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.Resource;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(ResourceACLMonitor.class)
public class DataView extends Resource implements DataWrapper {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;
    private static final long firstLinkupRowId = 0x40000000;

    protected String version = null;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected DataViewDef meta;

    @Enumerated(EnumType.STRING)
    protected DataViewType type = DataViewType.BASIC;

    protected boolean spinoff = false;

    protected Boolean needsSource = Boolean.FALSE;

    protected Boolean needsRefresh = Boolean.FALSE;
    protected boolean topView = false;

    @Column(columnDefinition = "TEXT")
    protected String linkups = null;            // List of Linkup Tables
    @Column(columnDefinition = "TEXT")
    protected String tables = null;             // Base table name
    @Column(columnDefinition = "TEXT")
    protected String views = null;              // top view name
    @Column(columnDefinition = "TEXT")
    protected String installedTables = null;    // installed table name
    @Column(columnDefinition = "TEXT")
    protected String dataKeys = null;           // list of data source keys from linkups

    protected int nextLinkupId = 1;
    protected long nextLinkupRowId = firstLinkupRowId;
    protected boolean rowLevelCapco = false;
    protected boolean rowLevelTags = false;

    public DataView() {
        super(AclResourceType.DATAVIEW);
        version = ReleaseInfo.version;
    }

    public DataView(String versionIn) {
        this();
        version = versionIn;
    }

    /**
     * DataView Constructor
     *
     * @param metaDataIn
     * @param nameIn
     * @param remarksIn
     * @throws CentrifugeException Creates a dataview from a clone of the DataViewDef object passed in, if it is indeed a template.
     *                             Or creates a dataview incorporating the DataViewDef object directly if it is not a template.
     */
    public DataView(DataViewDef metaDataIn, String nameIn, String remarksIn) throws CentrifugeException {
        super(AclResourceType.DATAVIEW, nameIn, remarksIn);

        version = ReleaseInfo.version;
        if (null != metaDataIn) {

            if (metaDataIn.isTemplate()) {

                setMeta(genComponent(metaDataIn));

            } else {

                setMeta(metaDataIn);
            }

        } else {

            throw new CentrifugeException("Attempting to create dataview from null template.");
        }
    }

    // The range is defined by first id  and last id.
    // IE: value 1 through value 2, not value 1 to value 2.
    // So size of the range dfined by the ValuePair is "getValue2() - getValue1() + 1".
    public List<ValuePair<Long, Long>> getInternalIdRanges() {

        List<ValuePair<Long, Long>> myList = new ArrayList<ValuePair<Long, Long>>();
        long myLinkupSize = nextLinkupRowId - firstLinkupRowId;

        myList.add(new ValuePair<Long, Long>(1L, getSize() - myLinkupSize));
        if (0L < myLinkupSize) {

            myList.add(new ValuePair<Long, Long>(firstLinkupRowId, nextLinkupRowId-1));
        }
        return myList;
    }

    public List<ValuePair<FieldDef, FieldDef>> getPhysicalPairs() {

        return meta.getPhysicalPairs();
    }

    public List<String> getCapcoColumnNames() {

        return meta.getCapcoColumnNames();
    }

    public List<String> getTagColumnNames() {

        return meta.getTagColumnNames();
    }

    public Map<String, FieldDef> getCapcoColumnMap() {

        return meta.getCapcoColumnMap();
    }

    public Map<String, FieldDef> getTagColumnMap() {

        return meta.getTagColumnMap();
    }

    public DataDefinition getDataDefinition() {

        return meta;
    }

    @Override
    public SecurityAccess getSecurityAccess() {

        return meta;
    }

    public List<FieldDef> getFieldList() {

        return (null != meta) ? meta.getFieldList() : null;
    }

    public List<QueryParameterDef> getParameterList() {

        return (null != meta) ? meta.getParameterList() : null;
    }

    public List<DataSourceDef> getDataSources() {

        return (null != meta) ? meta.getDataSources() : null;
    }

    public DataViewDef getMeta() {
        return meta;
    }

    public void setMeta(DataViewDef metaIn) {
        meta = metaIn;
        meta.setResourceType(null);
        meta.setTemplate(false);
        type = DataViewType.BASIC;
        setResourceType(AclResourceType.DATAVIEW);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DataViewType getType() {
        return type;
    }

    public void setType(DataViewType type) {
        this.type = type;
    }

    public boolean isSpinoff() {
        return spinoff;
    }

    public void setSpinoff(boolean spinoff) {
        this.spinoff = spinoff;
    }

    public boolean getNeedsRefresh() {
        if (needsRefresh == null) {
            needsRefresh = false;
        }
        return needsRefresh;
    }

    public void setNeedsRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    public boolean getNeedsSource() {
        if (needsSource == null) {
            needsSource = false;
        }
        return needsSource;
    }

    public void setNeedsSource(boolean needsSource) {
        this.needsSource = needsSource;
    }

    public boolean isTopView() {
        return topView;
    }

    public void setTopView(boolean topViewIn) {
        topView = topViewIn;
    }

    @Override
    public Set<String> getDataSourceKeySet() {

        Set<String> myKeySet = getMeta().getDataSourceKeySet();
        if (null != dataKeys) {

            String[] myKeySets = StringUtil.split(dataKeys, '\n');

            for (int i = 0; myKeySets.length > i; i++) {

                String[] myKeys = StringUtil.split(myKeySets[i], '\t');

                myKeySet.addAll(Arrays.asList(myKeys));
            }
        }
        return myKeySet;
    }

    public void setDataKeys(String dataKeysIn) {

        dataKeys = dataKeysIn;
    }

    public String getDataKeys() {

        return dataKeys;
    }

    public void discardDataKeys() {

        dataKeys = null;
    }

    public void addDataKeys(Collection<String> keySetIn) {

        if (null != keySetIn) {

            String myNewKeys = StringUtil.concatUniqueInput(keySetIn, '\t');

            if (null == dataKeys) {

                dataKeys = myNewKeys;

            } else {

                dataKeys = dataKeys + "\n" + myNewKeys;
            }
        }
    }

    public void lockDataKeys() {

        meta.lockDataKeys(getDataSourceKeySet());
        discardDataKeys();
    }

    public String[] clearInstalledTables() {

        String[] myTables = new String[0];

        if (null != installedTables) {

            myTables = StringUtil.split(installedTables, '|');
        }

        installedTables = null;

        return myTables;
    }

    public void removeInstalledTable(String tableIn, int revisionIn) {

        if ((null != tableIn) && (0 < tableIn.length())) {

            String myTable = tableIn + ":" + Integer.toString(revisionIn);
            String[] myTables = clearTables();

            for (int i = 0; myTables.length > i; i++) {

                if (!myTable.equals(myTables[i])) {

                    addInstalledTable(myTables[i]);
                }
            }
        }
    }

    public void removeInstalledTable(String tableIn) {

        if ((null != tableIn) && (0 < tableIn.length())) {

            String[] myTables = clearTables();

            for (int i = 0; myTables.length > i; i++) {

                if (!tableIn.equals(myTables[i])) {

                    addInstalledTable(myTables[i]);
                }
            }
        }
    }

    public void removeInstalledTable(int indexIn) {

        String[] myTables = clearInstalledTables();

        for (int i = 0; myTables.length > i; i++) {

            if (indexIn != i) {

                addInstalledTable(myTables[i]);
            }
        }
    }

    public void addInstalledTable(String tableIn, int revisionIn) {

        addInstalledTable(tableIn + ":" + Integer.toString(revisionIn));
    }

    public void addInstalledTable(String tableIn) {

        if (null == installedTables) {

            installedTables = tableIn;

        } else {

            installedTables = installedTables + "|" + tableIn;
        }
    }

    public String[] getInstalledTableList() {

        return (null != installedTables) ? StringUtil.split(installedTables, '|') : null;
    }

    public void setInstalledTables(String tablesIn) {

        installedTables = tablesIn;
    }

    public void setInstalledTables(String[] tablesIn) {

        installedTables = StringUtil.concatInput(tablesIn);
    }

    public String getInstalledTables() {

        return installedTables;
    }

    public String[] clearTables() {

        String[] myTables = new String[0];

        if (null != tables) {

            myTables = StringUtil.split(tables, '|');
        }

        tables = null;

        return myTables;
    }

    public void removeTable(String tableIn) {

        if ((null != tableIn) && (0 < tableIn.length())) {

            String[] myTables = clearTables();

            for (int i = 0; myTables.length > i; i++) {

                if (!tableIn.equals(myTables[i])) {

                    addTable(myTables[i]);
                }
            }
        }
    }

    public void addTable(String tableIn) {

        if (null == tables) {

            tables = tableIn;

        } else {

            tables = tables + "|" + tableIn;
        }
    }

    public String[] getTableList() {

        return (null != tables) ? StringUtil.split(tables, '|') : null;
    }

    public String getTables() {

        return tables;
    }

    public void setTables(String tablesIn) {

        tables = tablesIn;
    }

    public String[] clearViews() {

        String[] myViews = new String[0];

        if (null != views) {

            myViews = StringUtil.split(views, '|');
        }

        views = null;

        return myViews;
    }

    public void removeView(String viewIn) {

        if ((null != viewIn) && (0 < viewIn.length())) {

            String[] myViews = clearViews();

            for (int i = 0; myViews.length > i; i++) {

                if (!viewIn.equals(myViews[i])) {

                    addView(myViews[i]);
                }
            }
        }
    }

    public void addView(String viewIn) {

        if (null == views) {

            views = viewIn;

        } else {

            views = views + "|" + viewIn;
        }
    }

    public String[] getViewList() {

        return (null != views) ? StringUtil.split(views, '|') : null;
    }

    public String getViews() {

        return views;
    }

    public void setViews(String viewsIn) {

        views = viewsIn;
    }

    public String[] clearLinkups() {

        String[] myLinkups = new String[0];

        if (null != linkups) {

            myLinkups = StringUtil.split(linkups, '|');
        }

        linkups = null;

        return myLinkups;
    }

    public void removeLinkup(String linkupIn) {

        if ((null != linkupIn) && (0 < linkupIn.length())) {

            String[] myLinkups = clearLinkups();

            for (int i = 0; myLinkups.length > i; i++) {

                if (!linkupIn.equals(myLinkups[i])) {

                    addLinkup(myLinkups[i]);
                }
            }
        }
    }

    public void addLinkup(String linkupIn) {

        if (null == linkups) {

            linkups = linkupIn;

        } else {

            linkups = linkups + "|" + linkupIn;
        }
    }

    public String[] getLinkupList() {

        return (null != linkups) ? StringUtil.split(linkups, '|') : null;
    }

    public String getLinkups() {

        return linkups;
    }

    public void setLinkups(String linkupsIn) {

        linkups = linkupsIn;
    }

    public void setNextLinkupId(int nextLinkupIdIn) {

        nextLinkupId = nextLinkupIdIn;
    }

    public int getNextLinkupId() {

        return nextLinkupId;
    }

    public void incrementNextLinkupId() {

        nextLinkupId++;
    }

    public void resetNextLinkupId() {

        nextLinkupId = 1;
    }

    public void setNextLinkupRowId(long idIn) {

        nextLinkupRowId = idIn;
    }

    public long getNextLinkupRowId() {

        return nextLinkupRowId;
    }

    public void incrementNextLinkupRowId(long deltaIn) {

        if (0 < deltaIn) {

            setSize(getSize() + deltaIn);
            nextLinkupRowId += deltaIn;
        }
    }

    public void resetNextLinkupRowId() {

        nextLinkupRowId = firstLinkupRowId;
    }

    public Resource getResource() {

        return this;
    }

    public void setRowLevelCapco(boolean rowLevelCapcoIn) {

        rowLevelCapco = rowLevelCapcoIn;
    }

    public boolean getRowLevelCapco() {

        return rowLevelCapco;
    }

    public void setRowLevelTags(boolean rowLevelTagsIn) {

        rowLevelTags = rowLevelTagsIn;
    }

    public boolean getRowLevelTags() {

        return rowLevelTags;
    }

    @Override
    public void resetSecurity() {

        super.resetSecurity();
        if (null != meta) {

            meta.resetSecurity();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("spinoff", isSpinoff()) //
                .add("needsRefresh", getNeedsRefresh()) //
                .add("meta (DataViewDef)", getMeta()) //
                .toString();
    }

    @Override
    public DataView clone() {

        DataView myClone = new DataView();

        super.cloneComponents(myClone);

        myClone.setVersion(getVersion());
        if (null != getMeta()) {
            myClone.setMeta(getMeta().clone());
        }
        myClone.setType(type);
        myClone.setSpinoff(false);
        myClone.setNeedsRefresh(true);

        return myClone;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, spinoff, indentIn, "spinoff");
        debugObject(bufferIn, needsRefresh, indentIn, "needsRefresh");
        doDebug(meta, bufferIn, indentIn, "meta");
    }
}
