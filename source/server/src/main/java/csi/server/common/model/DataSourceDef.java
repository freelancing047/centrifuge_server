package csi.server.common.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;

import csi.server.common.enumerations.AclResourceType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataSourceDef extends Resource implements InPlaceUpdate<DataSourceDef> {

    protected int ordinal;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected ConnectionDef connection;
    protected String localId;
    private boolean inPlace = false;
    private boolean singleTable = false;
    private boolean simpleLoader = false;

    @Transient
    private Map<String, DataSourceDef> _cloneDataSourceMap;

    @Transient
    private int childCount = 0;

    public DataSourceDef() {
        super(AclResourceType.DATA_SOURCE);
    }

    public DataSourceDef(ConnectionDef connectionIn, boolean inPlaceIn, boolean singleTableIn, boolean simpleLoaderIn) {

        super(AclResourceType.DATA_SOURCE);
        localId = UUID.randomUUID();
        connection = connectionIn;
        inPlace = inPlaceIn;
        singleTable = singleTableIn;
        simpleLoader = simpleLoaderIn;
    }

    public DataSourceDef redactAll() {

        DataSourceDef myClone = new DataSourceDef();

        super.cloneComponents(myClone);

        if (null != getConnection()) {
            myClone.setConnection(getConnection().redactAll());
        }
        return cloneValues(myClone);
    }

    public String getLocalId() {
        // if (localId == null) {
        // localId = UUID.randomUUID().toString();
        // }
        return localId;
    }

    public void setLocalId(String uuid) {
        this.localId = uuid;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public ConnectionDef getConnection() {
        return connection;
    }

    public void setConnection(ConnectionDef connectionDefIn) {

        connection = connectionDefIn;
        inPlace = "installedtabledriver".equals(connection.getType());
    }

    public boolean isInPlace() {

        return inPlace;
    }

    public void setInPlace(boolean inPlaceIn) {

        inPlace = inPlaceIn;
    }

    public boolean isSingleTable() {

        return singleTable;
    }

    public void setSingleTable(boolean singleTableIn) {

        singleTable = singleTableIn;
    }

    public boolean isSimpleLoader() {

        return simpleLoader;
    }

    public void setSimpleLoader(boolean simpleLoaderIn) {

        simpleLoader = simpleLoaderIn;
    }

    public String getDataSourceKey() {

        return (null != connection) ? connection.getType() : null;
    }

    public Set<String> getDataSourceKeySet() {

        return (null != connection) ? connection.getDataSourceKeySet() : new TreeSet<String>();
    }

    public boolean hasChildren() {

        return (0 < childCount);
    }

    public void zeroChildCount() {
        childCount = 0;
    }

    public void incrementChildCount() {
        childCount++;
    }

    public void decrementChildCount() {
        if (0 < childCount) {
            childCount--;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("ordinal", getOrdinal()) //
                .add("connection", getConnection()) //
                .toString();
    }

    public void updateInPlace(DataSourceDef sourceIn) {

        super.updateInPlace(sourceIn);
        sourceIn.cloneValues(this);
        connection.updateInPlace(sourceIn.getConnection());
    }

    public DataSourceDef cloneValues(DataSourceDef cloneIn) {

        cloneIn.setOrdinal(getOrdinal());
        cloneIn.setLocalId(getLocalId());
        cloneIn.setInPlace(isInPlace());
        cloneIn.setSingleTable(isSingleTable());

        return cloneIn;
    }

    @Override
    public DataSourceDef fullClone() {

        DataSourceDef myClone = new DataSourceDef();

        super.fullCloneComponents(myClone);
        myClone.setConnection(connection.fullClone());

        return cloneValues(myClone);
    }

    @Override
    public DataSourceDef clone() {

        _cloneDataSourceMap = new HashMap<String, DataSourceDef>();

        return clone(_cloneDataSourceMap, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> DataSourceDef clone(Map<String, T> sourceMapIn, Map<String, S> tableMapIn, Map<String, R> fieldMapIn) {

        DataSourceDef myClone = new DataSourceDef();

        super.cloneComponents(myClone);

        // Insert into map now, since children need to reference the clone
        sourceMapIn.put(getUuid(), (T)myClone);

        if (null != getConnection()) {
            myClone.setConnection(getConnection().clone());
        }
        return cloneValues(myClone);
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, ordinal, indentIn, "ordinal");
        debugObject(bufferIn, localId, indentIn, "localId");
        doDebug(connection, bufferIn, indentIn, "connection", "ConnectionDef");
    }
}
