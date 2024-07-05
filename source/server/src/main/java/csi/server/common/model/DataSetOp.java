package csi.server.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.operator.OpJoinType;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataSetOp extends ModelObject {

    public static final String IS_LINKUP_PROP = "isLinkup";
    public static final String LINKUP_COUNT = "linkupCount";

    private String name;
    private String localId;

    @Transient
    private boolean initialized = false;
    @Transient
    private DataSetOp parent;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DataSetOp leftChild;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DataSetOp rightChild;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private SqlTableDef tableDef;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<OpMapItem> mapItems = new ArrayList<OpMapItem>();

    @Enumerated(value = EnumType.STRING)
    private OpMapType mapType = OpMapType.JOIN;

    @Enumerated(value = EnumType.STRING)
    private OpJoinType joinType = OpJoinType.EQUI_JOIN;

    private boolean appendAll = false;
    private boolean forceLocal = false;
    private boolean passOnlyMapped = false;

    @Transient
    private Map<String, String> _biMap = null;
    @Transient
    private Map<String, String> _activeColumnMap = null;

    public DataSetOp() {
        super();
    }

    public DataSetOp(String nameIn, SqlTableDef tableIn) {
        super();

        localId = CsiUUID.randomUUID();
        name = nameIn;
        tableDef = tableIn;
    }

    public OpMapItem addMappingItem(String fromTableLocalIdIn, String fromColumnLocalIdIn,
                                    String toTableLocalIdIn, String toColumnLocalIdIn) {

        OpMapItem myItem = new OpMapItem(this, fromTableLocalIdIn, fromColumnLocalIdIn, toTableLocalIdIn, toColumnLocalIdIn);
        mapItems.add(myItem);

        return myItem;
    }

    @Override
    public void resetTransients() {

        initialized = false;
        parent = null;
        _biMap = null;
    }

    public DataSetOp finalizeForUpload() {

        regenerateUuid();

        initialized = false;
        _biMap = null;
        parent = null;

        if (null != leftChild) {

            leftChild.finalizeForUpload();
        }

        if (null != rightChild) {

            rightChild.finalizeForUpload();
        }

        if (null != tableDef) {

            tableDef.regenerateUuid();
        }

        if (null != mapItems) {

            for (OpMapItem myItem : mapItems) {

                myItem.regenerateUuid();
            }
        }
        return this;
    }

    public Map<String, DataSourceDef> buildSourceMap() {

        Map<String, DataSourceDef> myMap = new TreeMap<>();
        fillSourceMap(myMap);
        return myMap;
    }

    public void fillSourceMap(Map<String, DataSourceDef> mapIn) {

        if (null != leftChild) {

            leftChild.fillSourceMap(mapIn);
        }
        if (null != rightChild) {

            rightChild.fillSourceMap(mapIn);
        }
        if (null != tableDef) {

            DataSourceDef mySource = tableDef.getSource();

            if (null != mySource) {

                String myKey = mySource.getLocalId();

                if (null != myKey) {

                    mapIn.put(myKey, mySource);
                }
            }
        }
    }

    public void deselectUnusedColumns(Set<String> activeSetIn) {

        if (hasLeftChild() && hasRightChild())  {

            getLeftChild().deselectUnusedColumns(activeSetIn);
            getRightChild().deselectUnusedColumns(activeSetIn);

        } else if (null != tableDef) {

            for (ColumnDef myColumn : tableDef.getColumns()) {

                if (myColumn.isSelected()) {

                    String myKey = myColumn.getColumnKey();

                    if ((activeSetIn == null) || (!activeSetIn.contains(myKey))) {

                        DataSetOp myOp;

                        for (myOp = parent; null != myOp; myOp = myOp.getParent()) {

                            Map<String, String> myMap = myOp.getBiMap();

                            if (null != myMap) {

                                String myValue = myMap.get(myKey);

                                if ((null != myValue)
                                        && (((OpMapType.JOIN == mapType)) || ((activeSetIn != null) && activeSetIn.contains(myKey)))) {

                                    break;
                                }
                            }
                        }
                        if (null == myOp) {

                            myColumn.setSelected(false);
                        }
                    }
                }
            }
        }
    }

   public Integer getKeyField() {
      return (tableDef == null) ? null : tableDef.getKeyField();
   }

   public boolean isSingleTable() {
      return (tableDef == null) ? false : tableDef.isSingleTable();
   }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {

        name = nameIn;

        if (null != tableDef) {

            tableDef.setDsoName(nameIn);
        }
    }

    public String createName(int ordinalIn) {

        setName(Integer.toString(ordinalIn / 10) + Integer.toString(ordinalIn % 10) + " " + baseName());

        return name;
    }

    public String updateName() {

        if (null != name) {

            setName(name.substring(0, 3) + baseName());

        } else {

            createName(0);
        }
        return name;
    }

    public Integer extractPrefix() {

        Integer myValue = null;

        if ((null != name) && ((2 < name.length()) && (' ' == name.charAt(2))
                && ('0' <= name.charAt(0)) && ('9' >= name.charAt(0))
                && ('0' <= name.charAt(1)) && ('9' >= name.charAt(1)))) {

            myValue = ((name.charAt(0) - '0') * 10) + (name.charAt(1) - '0');
        }
        return myValue;
    }

    public String getLocalId() {
        // if (localId == null) {
        // localId = UUID.randomUUID().toString();
        // }
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public List<OpMapItem> getMapItems() {
        return mapItems;
    }

    public void setMapItems(List<OpMapItem> mapItems) {
        this.mapItems = mapItems;
    }

    public OpMapType getMapType() {
        return mapType;
    }

    public void setMapType(OpMapType mapType) {
        this.mapType = mapType;
    }

    public OpJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(OpJoinType joinType) {
        this.joinType = joinType;
    }

    public boolean hasMapItems() {
        return (null != mapItems) && !mapItems.isEmpty();
    }

   public boolean isOkTree(List<DataSourceDef> dataSourcesIn) {
      return (tableDef == null)
                ? (hasBothChildren() && leftChild.isOkTree(dataSourcesIn) && rightChild.isOkTree(dataSourcesIn))
                : tableDef.hasDataSource(dataSourcesIn);
   }

    public void setParent(DataSetOp parentIn) {
        parent = parentIn;
    }

    public DataSetOp getParent() {
        return parent;
    }

    public void setLeftChild(DataSetOp childIn) {
        leftChild = childIn;
    }

    public DataSetOp getLeftChild() {
        return leftChild;
    }

    public void setRightChild(DataSetOp childIn) {
        rightChild = childIn;
    }

    public DataSetOp getRightChild() {
        return rightChild;
    }

    public SqlTableDef getTableDef() {
        return tableDef;
    }

    public void setTableDef(SqlTableDef tableDef) {
        this.tableDef = tableDef;
    }

    public boolean getAppendAll() {
        return appendAll;
    }

    public void setAppendAll(boolean appendAll) {
        this.appendAll = appendAll;
    }

    public boolean getForceLocal() {

        return forceLocal;
    }

    public void setForceLocal(boolean forceLocalIn) {
        forceLocal = forceLocalIn;
    }

    public boolean getPassOnlyMapped() {

        return passOnlyMapped;
    }

    public void setPassOnlyMapped(boolean passOnlyMappedIn) {
        passOnlyMapped = passOnlyMappedIn;
    }

    public void displayBiMap(StringBuilder bufferIn) {

        Map<String, String> myBiMap = getBiMap();

        for (Map.Entry<String, String> myItem : myBiMap.entrySet()) {

            bufferIn.append(myItem .getKey());
            bufferIn.append(" :: ");
            bufferIn.append(myItem .getValue());
            bufferIn.append("\n");
        }
    }

    public void displayBiMap(StringBuilder bufferIn, String indentIn) {

        Map<String, String> myBiMap = getBiMap();

        if ((null != indentIn) && (0 < indentIn.length())) {

            for (Map.Entry<String, String> myItem : myBiMap.entrySet()) {

                bufferIn.append(indentIn);
                bufferIn.append(myItem .getKey());
                bufferIn.append(" :: ");
                bufferIn.append(myItem .getValue());
                bufferIn.append("\n");
            }

        } else {

            displayBiMap(bufferIn);
        }
    }

    public void addMappingItem(OpMapItem itemIn) {

        Map<String, String> myBiMap = getBiMap();

        getMapItems().add(itemIn);
        myBiMap.put(itemIn.getRightColumnKey(), itemIn.getLeftColumnKey());
        myBiMap.put(itemIn.getLeftColumnKey(), itemIn.getRightColumnKey());
    }

    public void removeMappingItem(OpMapItem itemIn) {

        Map<String, String> myBiMap = getBiMap();

        getMapItems().remove(itemIn);
        myBiMap.remove(itemIn.getRightColumnKey());
        myBiMap.remove(itemIn.getLeftColumnKey());
    }

    public String getMapResult(String columnKeyIn) {

        return getBiMap().get(columnKeyIn);
    }

    public boolean fixupMappings(DataSetOp dsoIn, SqlTableDef tableIn) {

        boolean myChangeFlag = false;

        if ((null != dsoIn) && (null != tableIn)) {

            DataSetOp myParent = dsoIn.getParent();
            List<OpMapItem> myMapItems = dsoIn.getMapItems();
            String myTableId = tableIn.getLocalId();

            if (null != myParent) {

                myChangeFlag = fixupMappings(myParent, tableIn);
            }
            for (int i = myMapItems.size() - 1; 0 <= i; i--) {

                OpMapItem myItem = myMapItems.get(i);

                if (((myTableId.equals(myItem.getLeftTableLocalId()))
                        && (!tableIn.containsColumn(myItem.getLeftColumnLocalId())))
                        || ((myTableId.equals(myItem.getRightTableLocalId()))
                        && (!tableIn.containsColumn(myItem.getRightColumnLocalId())))) {

                    myMapItems.remove(i);
                    myChangeFlag = true;
                }
            }
        }
        return myChangeFlag;
    }

    public boolean removeMappings(DataSetOp dsoIn, String tableIdIn) {

        boolean myChangeFlag = false;

        if ((null != dsoIn) && (null != tableIdIn)) {

            DataSetOp myParent = dsoIn.getParent();
            List<OpMapItem> myMapItems = dsoIn.getMapItems();

            if (null != myParent) {

                myChangeFlag = removeMappings(myParent, tableIdIn);
            }
            for (int i = myMapItems.size() - 1; 0 <= i; i--) {

                OpMapItem myItem = myMapItems.get(i);

                if (tableIdIn.equals(myItem.getLeftTableLocalId())
                        || tableIdIn.equals(myItem.getRightTableLocalId())) {

                    myMapItems.remove(i);
                    myChangeFlag = true;
                }
            }
        }
        return myChangeFlag;
    }

    public void removeTableMapEntry(String tableIdIn) {

        if (null != mapItems) {

            List<OpMapItem> myList = new ArrayList<OpMapItem>();


            for(OpMapItem myItem : mapItems) {

                String myKey = null;

                if (myItem.getLeftTableLocalId().equals(tableIdIn)) {

                    myKey = myItem.getLeftColumnKey();
                    if (null != _biMap) {
                     _biMap.remove(myKey);
                  }
                }

                if (myItem.getRightTableLocalId().equals(tableIdIn)) {

                    myKey = myItem.getLeftColumnKey();
                    if (null != _biMap) {
                     _biMap.remove(myKey);
                  }
                }
                if (null != myKey) {

                    myList.add(myItem);
                }
            }

            for (OpMapItem myItem : myList) {

                mapItems.remove(myItem);
            }
        }
    }

    public void removeColumnMapEntry(String columnKeyIn) {

        if (null != mapItems) {

            List<OpMapItem> myList = new ArrayList<OpMapItem>();

            if (null != _biMap) {
               _biMap.remove(columnKeyIn);
            }

            for(OpMapItem myItem : mapItems) {

                if (myItem.getLeftColumnKey().equals(columnKeyIn) || myItem.getRightColumnKey().equals(columnKeyIn)) {

                    myList.add(myItem);
                }
            }

            for (OpMapItem myItem : myList) {

                mapItems.remove(myItem);
            }
        }
    }

    public boolean initialize() {

        return initialize(null);
    }

    public boolean initialize(DataSetOp parentIn) {

        boolean myOK = true;

        setParent(parentIn);

        if (null  !=  tableDef) {

            DataSourceDef mySource = tableDef.getSource();

            if (null != mySource) {

                mySource.incrementChildCount();
            }
        }

        if (hasChildren()) {

            if ((null == leftChild) || (!leftChild.initialize(this))) {

                myOK = false;
            }

            if ((null == rightChild) || (!rightChild.initialize(this))) {

                myOK = false;
            }
        }
        initialized = true;

        return myOK;
    }
/*
    public DataSetOp getFirstTableOp() {

        DataSetOp myDso = this;

        if (!initialized) {

            initialize();
        }

        while (myDso.hasLeftChild()) {

            myDso = myDso.getLeftChild();
        }
        return myDso;
    }

    public DataSetOp getNextTableOp() {

        DataSetOp myDso = null;

        if (isTheLeftChild()) {

            myDso = getParent().getRightChild();

            while (myDso.hasLeftChild()) {

                myDso = myDso.getLeftChild();
            }

        } else if (isTheRightChild()) {

            for (myDso = getParent(); (null != myDso) && myDso.isTheRightChild(); myDso = myDso.getParent()) { }

            if ((null != myDso) && myDso.isTheLeftChild()) {

                myDso = myDso.getParent().getRightChild();

                while (myDso.hasLeftChild()) {

                    myDso = myDso.getLeftChild();
                }
            }
        }
        return myDso;
    }
*/
    public DataSetOp getTopOp() {

        DataSetOp myDso;

        for (myDso = this; myDso.hasParent(); myDso = myDso.getParent()) { }

        return myDso;
    }

    public DataSetOp getFirstOp() {

        DataSetOp myDso = this;

        if (!initialized) {

            initialize();
        }

        while (myDso.hasLeftChild()) {

            myDso = myDso.getLeftChild();
        }
        return myDso;
    }

    public DataSetOp getNextOp() {

        DataSetOp myDso = null;

        if (isTheLeftChild()) {

            myDso = getParent().getRightChild();

            while (myDso.hasLeftChild()) {

                myDso = myDso.getLeftChild();
            }

        } else if (isTheRightChild()) {

            myDso = getParent();
        }
        return myDso;
    }

    public DataSetOp getFirstExternalOp(final DataSetOp excludedTreeIn) {

        DataSetOp myDso = this;

        if (!initialized) {

            initialize();
        }

        while ((excludedTreeIn != myDso) && myDso.hasLeftChild()) {

            myDso = myDso.getLeftChild();
        }
        if (excludedTreeIn == myDso) {

            if (myDso.hasParent()) {

                myDso = getParent().getRightChild();

                while (myDso.hasLeftChild()) {

                    myDso = myDso.getLeftChild();
                }

            } else {

                myDso = null;
            }
        }
        return myDso;
    }

    public DataSetOp getNextExternalOp(final DataSetOp excludedTreeIn) {

        DataSetOp myDso = null;

        if (isTheLeftChild()) {

            myDso = getParent().getRightChild();

            if (excludedTreeIn == myDso) {

                myDso = myDso.getParent();

            } else {

                while ((excludedTreeIn != myDso) && myDso.hasLeftChild()) {

                    myDso = myDso.getLeftChild();
                }
                if (excludedTreeIn == myDso) {

                    myDso = myDso.getParent().getRightChild();

                    while (myDso.hasLeftChild()) {

                        myDso = myDso.getLeftChild();
                    }
                }
            }

        } else if (isTheRightChild()) {

            myDso = getParent();
        }
        return myDso;
    }

    public boolean hasParent(DataSetOp dsoIn) {
        return (null != dsoIn) && (!this.equals(dsoIn)) && (null != dsoIn.getParent());
    }

    public boolean hasParent() {
        return (null != getParent());
    }

    public boolean isLeftChild(DataSetOp dsoIn) {
        return hasParent(dsoIn) && dsoIn.getParent().getLeftChild().equals(dsoIn);
    }

    public boolean isTheLeftChild() {
        return hasParent() && getParent().getLeftChild().equals(this);
    }

    public boolean isRightChild(DataSetOp dsoIn) {
        return hasParent(dsoIn) && dsoIn.getParent().getRightChild().equals(dsoIn);
    }

    public boolean isTheRightChild() {
        return hasParent() && getParent().getRightChild().equals(this);
    }

    public boolean hasLeftChild() {
        return (null != leftChild);
    }

    public boolean hasRightChild() {
        return (null != rightChild);
    }

    public boolean hasChildren() {
        return (null != leftChild) || (null != rightChild);
    }

    public boolean hasBothChildren() {
        return (null != leftChild) && (null != rightChild);
    }

    public DataSetOp substitute(DataSetOp sustituteIn) {

        DataSetOp myRoot = sustituteIn;

        if (null != sustituteIn) {

            DataSetOp myRootParent = getParent();

            if (null != myRootParent) {

                myRootParent.replaceChild(this, sustituteIn);
            }
            while(null != myRootParent) {

                myRoot = myRootParent;
                myRootParent = myRoot.getParent();
            }

        } else {

            myRoot = remove();
        }
        return myRoot;
    }

    public List<DataSetOp> substitute(DataSetOp sustituteIn, List<DataSetOp> listIn) {

        if (null != sustituteIn) {

            DataSetOp myParent = getParent();

            if (null != myParent) {

                myParent.replaceChild(this, sustituteIn);

            } else {
               int howMany = listIn.size();

               for (int i = 0; i < howMany; i++) {

                    if (this.equals(listIn.get(i))) {

                        listIn.set(i, sustituteIn);
                    }
                }
            }

        } else {

            remove(listIn);
        }
        return listIn;
    }

    public DataSetOp addSibbling(DataSetOp parentIn, DataSetOp sibblingIn,
                            boolean asLeftIn) throws CentrifugeException {

        DataSetOp myRoot = null;

        if (null != sibblingIn) {

            if (asLeftIn) {

                parentIn.installLeftChild(sibblingIn);
                myRoot = substitute(parentIn);
                parentIn.installRightChild(this);

            } else {

                parentIn.installRightChild(sibblingIn);
                myRoot = substitute(parentIn);
                parentIn.installLeftChild(this);
            }

        } else {

            throw new CentrifugeException("Sibbling must be identified");
        }
        return myRoot;
    }

    public List<DataSetOp> addSibbling(DataSetOp parentIn, DataSetOp sibblingIn,
            boolean asLeftIn, List<DataSetOp> listIn) throws CentrifugeException {

        if (null != sibblingIn) {

            if (asLeftIn) {

                parentIn.installLeftChild(sibblingIn);
                listIn = substitute(parentIn, listIn);
                parentIn.installRightChild(this);

            } else {

                parentIn.installRightChild(sibblingIn);
                listIn = substitute(parentIn, listIn);
                parentIn.installLeftChild(this);
            }

        } else {

            throw new CentrifugeException("Sibbling must be identified");
        }
        return listIn;
    }

    public DataSetOp remove() {

        DataSetOp myRoot = getParent();

        if (null != myRoot) {

            DataSetOp myRootParent = myRoot.getParent();
            DataSetOp mySibbling = (this.equals(myRoot.getLeftChild()))
                                    ? myRoot.getRightChild()
                                    : (this.equals(myRoot.getRightChild()))
                                        ? myRoot.getLeftChild()
                                        : null;
            if (null != mySibbling) {

                if (null != myRootParent) {

                    if (myRoot.equals(myRootParent.getLeftChild())) {

                        myRootParent.replaceLeftChild(mySibbling);

                    } else if (myRoot.equals(myRootParent.getRightChild())) {

                        myRootParent.replaceRightChild(mySibbling);
                    }
                    while(null != myRootParent) {

                        myRoot = myRootParent;
                        myRootParent = myRoot.getParent();
                    }

                } else {

                    myRoot = mySibbling;
                    myRoot.setParent(null);
                }
            }
        }
        setParent(null);
        return myRoot;
    }

    public List<DataSetOp> remove(List<DataSetOp> listIn) {

        DataSetOp myRoot = getParent();

        if (null != myRoot) {

            DataSetOp myRootParent = myRoot.getParent();
            DataSetOp mySibbling = (this.equals(myRoot.getLeftChild()))
                    ? myRoot.getRightChild()
                    : (this.equals(myRoot.getRightChild()))
                        ? myRoot.getLeftChild()
                        : null;

            if (null != mySibbling) {

                if (null != myRootParent) {

                    if (myRoot.equals(myRootParent.getLeftChild())) {

                        myRootParent.replaceLeftChild(mySibbling);

                    } else if (myRoot.equals(myRootParent.getRightChild())) {

                        myRootParent.replaceRightChild(mySibbling);
                    }

                } else {
                   int howMany = listIn.size();

                   for (int i = 0; i < howMany; i++) {

                        if (myRoot.equals(listIn.get(i))) {

                            listIn.set(i, mySibbling);
                        }
                    }
                    myRoot = mySibbling;
                    myRoot.setParent(null);
                }

            } else if (listIn.contains(this)) {

                listIn.remove(this);
            }

        } else if (listIn.contains(this)) {

            listIn.remove(this);
        }
        setParent(null);
        return listIn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid(), getLocalId());
    }

    public DataSetOp clear() {

        if (hasLeftChild()) {

            leftChild.clear();
        }

        if (hasRightChild()) {

            rightChild.clear();
        }

        return null;
    }

    public boolean hasWeekMappings() {

        for (DataSetOp myDso = getTopOp().getFirstOp(); null != myDso; myDso = myDso.getNextOp()) {

            if (myDso.hasChildren() && (OpMapType.APPEND == myDso.getMapType()) && (!myDso.hasMapItems())) {

                return true;
            }
        }
        return false;
    }

    public boolean hasBadMappings() {

        for (DataSetOp myDso = getTopOp().getFirstOp(); null != myDso; myDso = myDso.getNextOp()) {

            if (myDso.hasChildren() && (OpMapType.JOIN == myDso.getMapType()) && (!myDso.hasMapItems())) {

                return true;
            }
        }
        return false;
    }

    public Map<String, CsiDataType> buildUnionCastingMap() {

        return buildUnionCastingMap(null);
    }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof DataSetOp) &&
              Objects.equal(getLocalId(), ((DataSetOp) obj).getLocalId()));
   }

    public DataSetOp getWorkingCopy(Map<String, DataSourceDef> sourceMapIn, Map<String, SqlTableDef> sqlTableMapIn) {

        DataSetOp myClone = new DataSetOp();

        super.fullCloneComponents(myClone);

        myClone.setName(getName());
        myClone.setLocalId(getLocalId());
        if (null != leftChild) {
            myClone.setLeftChild(leftChild.getWorkingCopy(sourceMapIn, sqlTableMapIn));
            myClone.getLeftChild().setParent(myClone);
        }
        if (null != rightChild) {
            myClone.setRightChild(rightChild.getWorkingCopy(sourceMapIn, sqlTableMapIn));
            myClone.getRightChild().setParent(myClone);
        }
        if (null != tableDef) {

            SqlTableDef myCloneTable = cloneFromOrToMap(sqlTableMapIn, getTableDef(), sourceMapIn);

            myCloneTable.setUuid(tableDef.getUuid());
            myClone.setTableDef(myCloneTable);
        }
        myClone.setMapItems(cloneMapItems());
        myClone.setMapType(getMapType());
        myClone.setJoinType(getJoinType());
        myClone.setAppendAll(getAppendAll());
        myClone.setForceLocal(getForceLocal());
        if (null != myClone.getMapItems()) {
            for (OpMapItem myMapItem : myClone.getMapItems()) {
                myMapItem.setParent(myClone);
            }
        }

        return myClone;
    }

    public DataSetOp fullClone(Map<String, DataSourceDef> sourceMapIn, Map<String, SqlTableDef> sqlTableMapIn) {

        DataSetOp myClone = new DataSetOp();

        super.fullCloneComponents(myClone);

        myClone.setName(getName());
        myClone.setLocalId(getLocalId());
        if (null != leftChild) {
            myClone.setLeftChild(leftChild.fullClone(sourceMapIn, sqlTableMapIn));
            myClone.getLeftChild().setParent(myClone);
        }
        if (null != rightChild) {
            myClone.setRightChild(rightChild.fullClone(sourceMapIn, sqlTableMapIn));
            myClone.getRightChild().setParent(myClone);
        }
        if (null != tableDef) {

            SqlTableDef myCloneTable = tableDef.fullClone(sourceMapIn, sqlTableMapIn);

            myCloneTable.setUuid(tableDef.getUuid());
            myClone.setTableDef(myCloneTable);
        }
        myClone.setMapItems(fullCloneMapItems(myClone));
        myClone.setMapType(getMapType());
        myClone.setJoinType(getJoinType());
        myClone.setAppendAll(getAppendAll());
        myClone.setForceLocal(getForceLocal());
        if (null != myClone.getMapItems()) {
            for (OpMapItem myMapItem : myClone.getMapItems()) {
                myMapItem.setParent(myClone);
            }
        }

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject, S extends ModelObject> DataSetOp clone(Map<String, T> sourceMapIn, Map<String, S> sqlTableMapIn) {

        DataSetOp myClone = new DataSetOp();

        super.cloneComponents(myClone);

        myClone.setName(getName());
        myClone.setLocalId(getLocalId());
        if (null != leftChild) {
            myClone.setLeftChild(leftChild.clone(sourceMapIn, sqlTableMapIn));
            myClone.getLeftChild().setParent(myClone);
        }
        if (null != rightChild) {
            myClone.setRightChild(rightChild.clone(sourceMapIn, sqlTableMapIn));
            myClone.getRightChild().setParent(myClone);
        }
        if (null != tableDef) {
            myClone.setTableDef((SqlTableDef) cloneFromOrToMap(sqlTableMapIn, (S) getTableDef(), sourceMapIn));
        }
        // Allow for either custom query tables which are
        // identified within the map, or actual tables which are not
//        if (null != getTableDef()) {
//            SqlTableDef myTableClone = sqlTableMapIn.get(getTableDef().getUuid());
//            myClone.setTableDef((null != myTableClone) ? myTableClone : getTableDef());
//        }
        myClone.setMapItems(cloneMapItems());
        myClone.setMapType(getMapType());
        myClone.setJoinType(getJoinType());
        myClone.setAppendAll(getAppendAll());
        myClone.setForceLocal(getForceLocal());
        if (null != myClone.getMapItems()) {
            for (OpMapItem myMapItem : myClone.getMapItems()) {
                myMapItem.setParent(myClone);
            }
        }

        return myClone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("name", getName()) //
                .add("tableDef", getTableDef()) //
                .add("joinType", getJoinType()) //
                .add("leftChild", leftChild) //
                .add("rightChild", rightChild) //
                .add("mapType", getMapType()) //
                .add("mapItems", getMapItems()) //
                .toString();
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, localId, indentIn, "localId");
        debugObject(bufferIn, mapType, indentIn, "mapType");
        debugObject(bufferIn, joinType, indentIn, "joinType");
        debugObject(bufferIn, appendAll, indentIn, "appendAll");
        doDebug(tableDef, bufferIn, indentIn, "tableDef", "SqlTableDef");
        doDebug(leftChild, bufferIn, indentIn, "leftChild", "DataSetOp");
        doDebug(rightChild, bufferIn, indentIn, "rightChild", "DataSetOp");
        debugList(bufferIn, mapItems, indentIn, "mapItems");
    }

    private Map<String, CsiDataType> buildUnionCastingMap(Map<String, CsiDataType> mapIn) {

        Map<String, CsiDataType> myMap = (null != mapIn) ? mapIn : new HashMap<String, CsiDataType>();

        if (null == tableDef) {

            if (OpMapType.APPEND == mapType) {

                for (OpMapItem myMapItem : mapItems) {

                    CsiDataType myCast = myMapItem.getCastToType();

                    if (null != myCast) {

                        if (null == myMap.get(myMapItem.getLeftColumnKey())) {

                            myMap.put(myMapItem.getLeftColumnKey(), myCast);
                        }
                        if (null == myMap.get(myMapItem.getRightColumnKey())) {

                            myMap.put(myMapItem.getRightColumnKey(), myCast);
                        }
                    }
                }
            }
            if (null != leftChild) {

                leftChild.buildUnionCastingMap(myMap);
            }
            if (null != rightChild) {

                rightChild.buildUnionCastingMap(myMap);
            }
        }
        return myMap;
    }

    private Map<String, String> getBiMap() {

        if (null == _biMap) {

            _biMap = new HashMap<String, String>();

            for (OpMapItem myItem : mapItems) {

                _biMap.put(myItem.getRightColumnKey(), myItem.getLeftColumnKey());
                _biMap.put(myItem.getLeftColumnKey(), myItem.getRightColumnKey());
            }
        }
        return _biMap;
    }

    private DataSetOp installLeftChild(DataSetOp childIn) {

        setLeftChild(childIn);

        if (null != childIn) {

            childIn.setParent(this);
        }
        return this;
    }

    private DataSetOp installRightChild(DataSetOp childIn) {

        setRightChild(childIn);

        if (null != childIn) {

            childIn.setParent(this);
        }
        return this;
    }

    public DataSetOp replaceLeftChild(DataSetOp childIn) {

        DataSetOp myChild = leftChild;

        setLeftChild(childIn);

        if (null != childIn) {

            childIn.setParent(this);
        }

        if (null != myChild) {

            myChild.setParent(null);
        }
        return myChild;
    }

    private DataSetOp replaceRightChild(DataSetOp childIn) {

        DataSetOp myChild = rightChild;

        setRightChild(childIn);

        if (null != childIn) {

            childIn.setParent(this);
        }

        if (null != myChild) {

            myChild.setParent(null);
        }
        return myChild;
    }

    private DataSetOp replaceChild(DataSetOp oldChildIn, DataSetOp newChildIn) {

        DataSetOp myChild = null;

        if (leftChild.equals(oldChildIn)) {

            myChild = replaceLeftChild(newChildIn);

        } else if (rightChild.equals(oldChildIn)) {

            myChild = replaceRightChild(newChildIn);
        }
        return myChild;
    }

    private List<OpMapItem> cloneMapItems() {

        if (null != getMapItems()) {

            List<OpMapItem>  myList = new ArrayList<OpMapItem>();

            for (OpMapItem myItem : getMapItems()) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<OpMapItem> fullCloneMapItems(DataSetOp cloneIn) {

        if (null != getMapItems()) {

            List<OpMapItem>  myList = new ArrayList<OpMapItem>();

            for (OpMapItem myItem : getMapItems()) {

                myList.add(myItem.fullClone(cloneIn));
            }

            return myList;

        } else {

            return null;
        }
    }

   private String baseName() {
      return (tableDef == null)
                ? ((mapType == OpMapType.APPEND) ? (appendAll ? "UNION ALL" : "DISTINCT UNION") : joinType.getSql())
                : tableDef.getDisplayName();
   }
}
