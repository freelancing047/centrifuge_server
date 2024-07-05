package csi.server.common.linkup;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.DataOperation;
import csi.server.common.model.FieldDef;
import csi.server.common.model.linkup.LinkupMapDef;


public class LinkupDataTransfer implements IsSerializable {

    public String dataViewUuid;
    public LinkupMapDef objectOfInterest;
    public List<FieldDef> newFields;
    public List<FieldDef> deletedFields;
    public DataOperation request = DataOperation.UNSPECIFIED;

    public LinkupDataTransfer() {
        objectOfInterest = null;
        request = DataOperation.UNSPECIFIED;
    }

    public LinkupDataTransfer(String dataViewUuidIn, LinkupMapDef objectOfInterestIn) {
        dataViewUuid = dataViewUuidIn;
        objectOfInterest = objectOfInterestIn;
        request = DataOperation.UNSPECIFIED;
    }

    public LinkupDataTransfer(String dataViewUuidIn, LinkupMapDef objectOfInterestIn, DataOperation requestIn) {
        dataViewUuid = dataViewUuidIn;
        objectOfInterest = objectOfInterestIn;
        request = requestIn;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    public void setDataViewUuid(String dataViewUuidIn) {
        dataViewUuid = dataViewUuidIn;
    }

    public LinkupMapDef getObjectOfInterest() {
        return objectOfInterest;
    }

    public void setObjectOfInterest(LinkupMapDef objectOfInterestIn) {
        objectOfInterest = objectOfInterestIn;
    }

    public List<FieldDef> getNewFields() {
        return newFields;
    }
    
    public void setNewFields(List<FieldDef> newFieldsIn) {
        newFields = newFieldsIn;
    }
    
    public List<FieldDef> getDeletedFields() {
        return deletedFields;
    }
    
    public void setDeletedFields(List<FieldDef> deletedFieldsIn) {
        deletedFields = deletedFieldsIn;
    }
    
    public DataOperation getRequest() {
        return request;
    }
    
    public void setRequest(DataOperation requestIn) {
        request = requestIn;
    }

    @Override
    public String toString() {

        if ((null != objectOfInterest) && (null != objectOfInterest.getLinkupName())) {

            return objectOfInterest.getLinkupName();

        } else {

            return "???";
        }
    }
}
