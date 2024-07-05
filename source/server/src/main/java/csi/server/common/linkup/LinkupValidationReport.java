package csi.server.common.linkup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 5/5/2016.
 */
public class LinkupValidationReport implements IsSerializable {

    String _missingTemplateName = null;
    String _missingTemplateUser = null;
    String _missingTemplateId = null;
    List<ValuePair<String, String>> _missingFieldsList = null;
    List<ValuePair<String, String>> _questionableFieldsList = null;
    List<String> _missingParameterList = null;
    List<String> _missingParmDataList = null;

    public LinkupValidationReport() {

    }

    public LinkupValidationReport(String missingTemplateNameIn,
                                  String missingTemplateUserIn, String missingTemplateIdIn) {

        _missingTemplateName = missingTemplateNameIn;
        _missingTemplateUser = missingTemplateUserIn;
        _missingTemplateId = missingTemplateIdIn;
    }

    public LinkupValidationReport(List<QueryParameterDef> parameterListIn) {

        _missingParmDataList = new ArrayList<String>();

        for (QueryParameterDef myParameter : parameterListIn) {

            _missingParmDataList.add(myParameter.getName());
        }
    }

    public void setMissingTemplate(String missingTemplateNameIn,
                                  String missingTemplateUserIn, String missingTemplateIdIn) {

        _missingTemplateName = missingTemplateNameIn;
        _missingTemplateUser = missingTemplateUserIn;
        _missingTemplateId = missingTemplateIdIn;
    }

    public boolean isOK(){

        return (!isTemplateMissing() && ((null == _missingFieldsList) || _missingFieldsList.isEmpty()) && ((null == _missingParmDataList) || _missingParmDataList.isEmpty())
            && ((null == _missingParameterList) || _missingParameterList.isEmpty()));
    }

    public boolean isTemplateMissing() {

        return (null != _missingTemplateName) && (null != _missingTemplateUser) && (null != _missingTemplateId);
    }

    public void setMissingTemplateName(String missingTemplateNameIn) {

        _missingTemplateName = missingTemplateNameIn;
    }

    public String getMissingTemplateName() {

        return _missingTemplateName;
    }

    public void setMissingTemplateUser(String missingTemplateUserIn) {

        _missingTemplateUser = missingTemplateUserIn;
    }

    public String getMissingTemplateUser() {

        return _missingTemplateUser;
    }

    public void setMissingTemplateId(String missingTemplateIdIn) {

        _missingTemplateId = missingTemplateIdIn;
    }

    public String getMissingTemplateId() {

        return _missingTemplateId;
    }

    public void setMissingFieldList(List<ValuePair<String, String>> missingFieldsListIn) {

        _missingFieldsList = missingFieldsListIn;
    }

    public List<ValuePair<String, String>> getMissingFieldList() {

        return _missingFieldsList;
    }

    public void setQuestionableFieldList(List<ValuePair<String, String>> questionableFieldsListIn) {

        _questionableFieldsList = questionableFieldsListIn;
    }

    public List<ValuePair<String, String>> getQuestionableFieldList() {

        return _questionableFieldsList;
    }

    public void setMissingParmDataList(List<String> missingParmDataListIn) {

        _missingParmDataList = missingParmDataListIn;
    }

    public List<String> getMissingParmDataList() {

        return _missingParmDataList;
    }

    public void setMissingParameterList(List<String> missingParameterListIn) {

        _missingParameterList = missingParameterListIn;
    }

    public List<String> getMissingParameterList() {

        return _missingParameterList;
    }

    public void addMissingField(String nameIn, String localIdIn) {

        if (null == _missingFieldsList) {

            _missingFieldsList = new ArrayList<ValuePair<String, String>>();
        }
        _missingFieldsList.add(new ValuePair<String, String>(nameIn, localIdIn));
    }

    public void addQuestionableField(String nameIn, String localIdIn) {

        if (null == _questionableFieldsList) {

            _questionableFieldsList = new ArrayList<ValuePair<String, String>>();
        }
        _questionableFieldsList.add(new ValuePair<String, String>(nameIn, localIdIn));
    }

    public void addMissingDataParameter(String nameIn) {

        if (null == _missingParmDataList) {

            _missingParmDataList = new ArrayList<String>();
        }
        _missingParmDataList.add(nameIn);
    }

    public void addMissingParameter(String nameIn) {

        if (null == _missingParameterList) {

            _missingParameterList = new ArrayList<String>();
        }
        _missingParameterList.add(nameIn);
    }
}
