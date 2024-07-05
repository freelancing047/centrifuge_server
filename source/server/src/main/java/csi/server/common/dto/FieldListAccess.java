package csi.server.common.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.FieldDefSource;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 6/27/2017.
 */
public class FieldListAccess implements IsSerializable {

    private FieldDefSource parent = null;

    private List<FieldDef> fieldDefList = null;
    private int _fieldOrdinal = 0;

    private Map<String, FieldDef> fieldDefMapByColumnKey = null;
    private Map<String, FieldDef> fieldDefMapByLocalId = null;
    private Map<String, FieldDef> fieldDefMapByUuid = null;
    private Map<String, FieldDef> fieldDefMapByName = null;
    private Map<String, String> localIdNameMap = null;
    private Map<String, String> staticFieldMap = null;
    private Map<String, String> columnMap = null;
    private Map<Integer, FieldDef> orderedFieldDefMap = null;


    public FieldListAccess() {

    }

    public FieldListAccess(FieldDefSource parentIn, List<FieldDef> fieldDefListIn) {

        resetList(parentIn, fieldDefListIn);
    }

    public FieldListAccess resetList(FieldDefSource parentIn, List<FieldDef> fieldDefListIn) {

        parent = parentIn;
        fieldDefList = (null != fieldDefListIn) ? fieldDefListIn : new ArrayList<FieldDef>();
        return resetMaps();
    }

    public FieldListAccess resetMaps() {

        _fieldOrdinal = 0;

        fieldDefMapByColumnKey = null;
        fieldDefMapByLocalId = null;
        fieldDefMapByUuid = null;
        fieldDefMapByName = null;
        staticFieldMap = null;
        columnMap = null;
        localIdNameMap = null;
        orderedFieldDefMap = null;

        return this;
    }

    public List<String> getCoreFieldIds() {

        List<String> myList = new ArrayList<>();
        Set<Map.Entry<String, FieldDef>> myEntrySet = getFieldMapByUuid().entrySet();

        for (Map.Entry<String, FieldDef> myEntry : myEntrySet) {

            String myKey = myEntry.getKey();
            FieldDef myField = myEntry.getValue();

            if (FieldType.COLUMN_REF == myField.getFieldType()) {

                myList.add(myKey);
            }
        }
        return myList;
    }

    public List<String> getFieldIds() {

        return new ArrayList<String>(getFieldMapByUuid().keySet());
    }

    public List<FieldDef> getFieldDefList() {

        return (null != fieldDefList) ? fieldDefList : new ArrayList<FieldDef>();
    }

    public void setFieldDefList(List<FieldDef> listIn) {

        parent.setFieldDefs(listIn);
    }

    public List<FieldDef> getOrderedFieldDefList() {

        return new ArrayList<FieldDef>(getOrderedFieldDefMap().values());
    }

    public Map<Integer, FieldDef> getOrderedFieldDefMap() {

        if (null == orderedFieldDefMap) {

            Integer myOrdinal = 0;
            orderedFieldDefMap = new TreeMap<Integer, FieldDef>();
            for (FieldDef myField : getFieldDefList()) {

                orderedFieldDefMap.put(myOrdinal++, myField);
            }
        }
        return orderedFieldDefMap;
    }

    public FieldDef getFieldDefByAnyKey(String keyIn) {

        FieldDef myField = getFieldDefByLocalId(keyIn);

        if (null == myField) {

            myField = getFieldDefByColumnKey(keyIn);
        }
        if (null == myField) {

            myField = getFieldDefByUuid(keyIn);
        }
        return myField;
    }

    public Map<String, FieldDef> getFieldDefMapByColumnKey() {

        if (null == fieldDefMapByColumnKey) {

            fieldDefMapByColumnKey = new HashMap<String, FieldDef>();
            for (FieldDef f : getFieldDefList()) {
                String myKey = f.getColumnKey();
                if (null != myKey) {
                    fieldDefMapByColumnKey.put(myKey, f);
                }
            }
        }
        return fieldDefMapByColumnKey;
    }

    public Map<String, FieldDef> getFieldMapByLocalId() {

        if (null == fieldDefMapByLocalId) {

            fieldDefMapByLocalId = new HashMap<String, FieldDef>();
            for (FieldDef f : getFieldDefList()) {
                fieldDefMapByLocalId.put(f.getLocalId(), f);
            }
        }
        return fieldDefMapByLocalId;
    }

    public Map<String, FieldDef> getFieldMapByUuid() {

        if (null == fieldDefMapByUuid) {

            _fieldOrdinal = 0;
            fieldDefMapByUuid = new HashMap<String, FieldDef>();
            for (FieldDef f : getFieldDefList()) {
                f.setOrdinal(_fieldOrdinal++);
                fieldDefMapByUuid.put(f.getUuid(), f);
            }
        }
        return fieldDefMapByUuid;
    }

    public Map<String, FieldDef> getFieldMapByName() {

        if (null == fieldDefMapByName) {

            fieldDefMapByName = new TreeMap<String, FieldDef>();
            localIdNameMap = new TreeMap<String, String>();

            for (FieldDef f : getFieldDefList()) {
                String myKey = f.mapKey();
                if (null != myKey) {

                    fieldDefMapByName.put(myKey, f);
                    localIdNameMap.put(f.getLocalId(), myKey);
                }
            }
        }
        return fieldDefMapByName;
    }

    public List<FieldDef> getAlphaOrderedFieldSet() {

        Collection<FieldDef> myListIn = getFieldMapByName().values();
        List<FieldDef> myListOut = new ArrayList<FieldDef>();

        for (FieldDef myFieldDef : myListIn) {

            myListOut.add(myFieldDef);
        }
        return myListOut;
    }

    public List<FieldDef> getAlphaOrderedFieldSet(CsiDataType dataTypeIn) {

        List<FieldDef> myListOut = new ArrayList<FieldDef>();

        if (null != dataTypeIn) {

            Collection<FieldDef> myListIn = getFieldMapByName().values();

            for (FieldDef myFieldDef : myListIn) {

                if (dataTypeIn == myFieldDef.getValueType()) {

                    myListOut.add(myFieldDef);
                }
            }
        }
        return myListOut;
    }

    public List<FieldDef> getOrderedFieldSet() {

        Collection<FieldDef> myListIn = getOrderedFieldDefMap().values();
        List<FieldDef> myListOut = new ArrayList<FieldDef>();

        for (FieldDef myFieldDef : myListIn) {

            myListOut.add(myFieldDef);
        }
        return myListOut;
    }

    public List<FieldDef> getOrderedFieldSet(CsiDataType dataTypeIn) {

        List<FieldDef> myListOut = new ArrayList<FieldDef>();

        if (null != dataTypeIn) {

            Collection<FieldDef> myListIn = getOrderedFieldDefMap().values();

            for (FieldDef myFieldDef : myListIn) {

                if (dataTypeIn == myFieldDef.getValueType()) {

                    myListOut.add(myFieldDef);
                }
            }
        }
        return myListOut;
    }

    public List<String> getAlphaOrderedFieldNames() {

        Collection<String> myListIn = getFieldMapByName().keySet();
        List<String> myListOut = new ArrayList<String>();

        for (String myFieldName : myListIn) {

            myListOut.add(myFieldName);
        }
        return myListOut;
    }

    public List<String> getFieldDefNames() {
        List<String> names = new ArrayList<String>();
        for (FieldDef f : getFieldDefList()) {
            if (f.getFieldName() != null) {
                names.add(f.getFieldName());
            }
        }
        return names;
    }

    public Set<String> getColumnKeys() {

        return getFieldDefMapByColumnKey().keySet();
    }

    public List<FieldDef> getColumnReferenceFieldDefs(List<ColumnDef> columnListIn, Map<String, ?> mapIn) {
        List<FieldDef> list = new ArrayList<FieldDef>();
        Map<String, FieldDef> myMap = getFieldDefMapByColumnKey();
        for (ColumnDef myColumn : columnListIn) {

            FieldDef myFieldDef = myMap.get(myColumn.getColumnKey());

            if ((null != myFieldDef)
                    && ((null == mapIn) || (mapIn.containsKey(myFieldDef.getLocalId())))) {

                CsiDataType myType = myFieldDef.tryStorageType();

                if ((null != myType) && (myType != myColumn.getCsiType())) {

                    myColumn =  myColumn.clone();
                    myColumn.setCsiType(myType);
                }
                list.add(myFieldDef);
            }
        }
        return list;
    }

    public FieldDef getFieldDefByColumnKey(String localKeyIn) {

        return (null != localKeyIn) ? getFieldDefMapByColumnKey().get(localKeyIn) : null;
    }

    public FieldDef getFieldDefByLocalId(String localIdIn) {

        return (null != localIdIn) ? getFieldMapByLocalId().get(localIdIn) : null;
    }

    public FieldDef getFieldDefByUuid(String uuidIn) {

        return (null != uuidIn) ? getFieldMapByUuid().get(uuidIn) : null;
    }

    public FieldDef getFieldDefByName(String nameIn) {

        return (null != nameIn) ? getFieldMapByName().get(FieldDef.makeKey(nameIn)) : null;
    }

    public boolean containsFieldName(String nameIn) {

        return (null != getFieldDefByName(nameIn));
    }

    public boolean containsFieldLocalId(String localIdIn) {

        return (null != getFieldDefByLocalId(localIdIn));
    }

    public void addFieldDef(FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            if (!getFieldMapByUuid().containsKey(fieldDefIn.getUuid())) {

                String myLocalId = fieldDefIn.getLocalId();

                fieldDefIn.setOrdinal(_fieldOrdinal++);

                getFieldDefList().add(fieldDefIn);
                fieldDefMapByUuid.put(fieldDefIn.getUuid(), fieldDefIn);

                if (null != fieldDefMapByColumnKey) {
                    fieldDefMapByColumnKey.put(myLocalId, fieldDefIn);
                }
                if (null != fieldDefMapByLocalId) {
                    fieldDefMapByLocalId.put(myLocalId, fieldDefIn);
                }
                if (null != fieldDefMapByName) {
                    String myKey = fieldDefIn.mapKey();
                    if (null != myKey) {

                        fieldDefMapByName.put(myKey, fieldDefIn);
                        localIdNameMap.put(myLocalId, myKey);
                    }
                }
                if ((null != staticFieldMap) && (FieldType.STATIC == fieldDefIn.getFieldType())) {
                    staticFieldMap.put(myLocalId, fieldDefIn.getStaticText());
                }
                if ((null != columnMap) && (FieldType.COLUMN_REF == fieldDefIn.getFieldType())) {
                    columnMap.put(myLocalId, myLocalId);
                }
                if (null != orderedFieldDefMap) {
                    orderedFieldDefMap.put(fieldDefIn.getOrdinal(), fieldDefIn);
                }
            }
        }
    }

    public void reportChange(FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            if (getFieldMapByUuid().containsKey(fieldDefIn.getUuid())) {

                String myLocalId = fieldDefIn.getLocalId();

                if (null != fieldDefMapByName) {
                    String myNewKey = fieldDefIn.mapKey();
                    if (null != myNewKey) {

                        String myOldKey = localIdNameMap.get(myLocalId);

                        if (!myNewKey.equals(myOldKey)) {

                            if (null != myOldKey) {

                                localIdNameMap.remove(myLocalId);
                            }
                            fieldDefMapByName.remove(myOldKey);
                            fieldDefMapByName.put(myNewKey, fieldDefIn);
                            localIdNameMap.put(myLocalId, myNewKey);
                        }
                    }
                }
                if (null != staticFieldMap) {
                    if ((FieldType.STATIC != fieldDefIn.getFieldType()) && staticFieldMap.containsKey(myLocalId)) {

                        staticFieldMap.remove(myLocalId);

                    } else if ((FieldType.STATIC == fieldDefIn.getFieldType()) && !staticFieldMap.containsKey(myLocalId)) {

                        staticFieldMap.put(myLocalId, fieldDefIn.getStaticText());
                    }
                }
                if (null != columnMap) {
                    if ((FieldType.COLUMN_REF != fieldDefIn.getFieldType()) && columnMap.containsKey(myLocalId)) {

                        columnMap.remove(myLocalId);

                    } else if ((FieldType.COLUMN_REF == fieldDefIn.getFieldType()) && !columnMap.containsKey(myLocalId)) {

                        columnMap.put(myLocalId, fieldDefIn.getStaticText());
                    }
                }

            } else {

                addFieldDef(fieldDefIn);
            }
        }
    }

    public void removeFieldDefByUuid(String uuidIn) {

        removeFieldDef(getFieldDefByUuid(uuidIn));
    }

    public void removeFieldDef(FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            if (getFieldMapByUuid().containsKey(fieldDefIn.getUuid())) {

                String myLocalId = fieldDefIn.getLocalId();

                getFieldDefList().remove(fieldDefIn);
                fieldDefMapByUuid.remove(fieldDefIn.getUuid());

                if (null != fieldDefMapByColumnKey) {
                    fieldDefMapByColumnKey.remove(myLocalId);
                }
                if (null != fieldDefMapByLocalId) {
                    fieldDefMapByLocalId.remove(myLocalId);
                }
                if (null != fieldDefMapByName) {
                    String myKey = fieldDefIn.mapKey();
                    if (null != myKey) {

                        fieldDefMapByName.remove(myKey);
                        localIdNameMap.remove(myLocalId);
                    }
                }
                if ((null != staticFieldMap) && staticFieldMap.containsKey(myLocalId)) {
                    staticFieldMap.remove(myLocalId);
                }
                if ((null != columnMap) && columnMap.containsKey(myLocalId)) {
                    columnMap.remove(myLocalId);
                }
                if (null != orderedFieldDefMap) {
                    orderedFieldDefMap.remove(fieldDefIn.getOrdinal());
                }
            }
        }
    }

    public Map<String, String> getColumnMap() {
        if (null == columnMap) {
            columnMap = new TreeMap<String, String>();
            for (FieldDef f : getFieldDefList()) {
                if ((FieldType.COLUMN_REF == f.getFieldType())
                        || (FieldType.STATIC == f.getFieldType())) {
                    columnMap.put(f.getLocalId(), f.getLocalId());
                }
            }
        }

        return columnMap;
    }

    public List<FieldDef> getDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {
            if (f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }
    public List<FieldDef> getNonDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {
            if (!f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getReferenceFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {
            if (f.getFieldType().isReference()) {
                list.add(f);
            }
        }

        return list;
    }

    public boolean hasPrecalculatedFields() {

        boolean mySuccess = false;

        for (FieldDef myField : getFieldDefList()) {

            if (myField.isPreCalculated()) {

                mySuccess = true;
                break;
            }
        }
        return mySuccess;
    }

    public List<FieldDef> addPrecalculatedFieldDefs(List<FieldDef> listIn) {

        List<FieldDef> myList;

        if (null != listIn) {

            myList = listIn;

        } else {

            myList = new ArrayList<FieldDef>();
        }
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (f.isPreCalculated()) {
                myList.add(f);
            }
        }
        return myList;
    }

    public List<FieldDef> getPhysicalFieldDefs() {

        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {

            if (f.isPreCalculated()
                    || (FieldType.COLUMN_REF == f.getFieldType())
                    || (FieldType.LINKUP_REF == f.getFieldType())) {

                list.add(f);
            }
        }

        return list;
    }

    public List<ValuePair<FieldDef, FieldDef>> getPhysicalPairs() {

        List<ValuePair<FieldDef, FieldDef>> list = new ArrayList<ValuePair<FieldDef, FieldDef>>();
        for (FieldDef f : getFieldDefList()) {

            if (f.isPreCalculated()
                    || (FieldType.COLUMN_REF == f.getFieldType())
                    || (FieldType.LINKUP_REF == f.getFieldType())) {

                list.add(new ValuePair<FieldDef, FieldDef>(f, f));
            }
        }

        return list;
    }

    public Map<String, String> getStaticFieldMap() {
        if (null == staticFieldMap) {
            staticFieldMap = new TreeMap<String, String>();
            for (FieldDef f : getFieldDefList()) {
                if (FieldType.STATIC == f.getFieldType()) {
                    staticFieldMap.put(f.getLocalId(), f.getStaticText());
                }
            }
        }

        return staticFieldMap;
    }

    public List<FieldDef> getStaticFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {
            if (FieldType.STATIC == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getNonStaticFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getFieldDefList()) {
            if (FieldType.STATIC != f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedActiveFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if ((FieldType.DERIVED == f.getFieldType()) && !f.isPreCalculated()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedActiveFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if ((FieldType.DERIVED == f.getFieldType()) && !f.isPreCalculated()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedNonDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (!f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedNonDependentFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (!f.getFieldType().isDependent()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedNonDependentFieldDefs(CsiDataType dataTypeIn) {
        List<FieldDef> list = new ArrayList<FieldDef>();
        if (null != dataTypeIn) {

            for (FieldDef myField : getAlphaOrderedFieldSet()) {

                if ((!myField.getFieldType().isDependent()) && (dataTypeIn == myField.getValueType())) {

                    list.add(myField);
                }
            }
        }
        return list;
    }

    public List<FieldDef> getOrderedNonDependentFieldDefs(CsiDataType dataTypeIn) {
        List<FieldDef> list = new ArrayList<FieldDef>();
        if (null != dataTypeIn) {

            for (FieldDef myField : getOrderedFieldSet()) {

                if ((!myField.getFieldType().isDependent()) && (dataTypeIn == myField.getValueType())) {

                    list.add(myField);
                }
            }
        }
        return list;
    }

    public List<FieldDef> getAlphaOrderedCaculatedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (FieldType.DERIVED == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedCaculatedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (FieldType.DERIVED == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedPreCaculatedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (f.isPreCalculated()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedPreCaculatedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (f.isPreCalculated() && (f.getFieldType() != FieldType.COLUMN_REF)) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedScriptedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (FieldType.SCRIPTED == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedScriptedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (FieldType.SCRIPTED == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedDirtyRetypedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if ((FieldType.DERIVED != f.getFieldType())
                    && (FieldType.SCRIPTED != f.getFieldType())
                    && f.isPreCalculated() && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedDirtyRetypedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if ((FieldType.DERIVED != f.getFieldType())
                    && (FieldType.SCRIPTED != f.getFieldType())
                    && f.isPreCalculated() && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedDirtyRestrictedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if ((FieldType.DERIVED == f.getFieldType()) && f.isPreCalculated() && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedDirtyRestrictedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if ((FieldType.DERIVED == f.getFieldType()) && f.isPreCalculated() && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedDirtyUnrestrictedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if ((FieldType.SCRIPTED == f.getFieldType()) && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedDirtyUnrestrictedFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if ((FieldType.SCRIPTED == f.getFieldType()) && f.isDirty()) {
                list.add(f);
            }
        }

        return list;
    }

    public Map<String, CsiDataType> getPreCaculatedColumnMap() {
        Map<String, CsiDataType> myMap = new TreeMap<String, CsiDataType>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (f.isPreCalculated()
                    && ((FieldType.DERIVED == f.getFieldType())
                    || (FieldType.SCRIPTED == f.getFieldType()))) {
                myMap.put(f.getLocalId(), f.getValueType());
            }
        }

        return myMap;
    }

    public List<FieldDef> getAlphaOrderedReferenceFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (f.getFieldType().isReference()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedReferenceFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (f.getFieldType().isReference()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getAlphaOrderedColumnFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getAlphaOrderedFieldSet()) {
            if (FieldType.COLUMN_REF == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getOrderedColumnFieldDefs() {
        List<FieldDef> list = new ArrayList<FieldDef>();
        for (FieldDef f : getOrderedFieldSet()) {
            if (FieldType.COLUMN_REF == f.getFieldType()) {
                list.add(f);
            }
        }

        return list;
    }

    public List<FieldDef> getSafeFieldList() {

        return new ArrayList<FieldDef>(fieldDefList);
    }

    public FieldDef findFieldDefByName(String name) {
        return getFieldDefByName(name);
    }

    public FieldDef findFieldDefByUuid(String uuid) {
        return getFieldDefByUuid(uuid);
    }

    public void setDirtyFlags() {

        for (FieldDef myField : fieldDefList) {

            myField.setDirty(true);
        }
    }

    public void clearDirtyFlags() {

        for (FieldDef myField : fieldDefList) {

            myField.setDirty(false);
        }
    }
}
