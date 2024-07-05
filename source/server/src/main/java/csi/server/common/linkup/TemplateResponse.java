package csi.server.common.linkup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.ResourceChoiceCriteria;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 5/5/2016.
 */
public class TemplateResponse implements IsSerializable {

    private DataViewDef _template;
    private List<ValuePair<DataViewDef, Integer>> _templateList;
    private Integer _criteriaMask; // mask of matching ResourceChoiceCriteria

    public TemplateResponse(DataViewDef templateIn, int criteriaMaskIn, List<ValuePair<DataViewDef, Integer>> templateListIn) {

        _template = templateIn;
        _criteriaMask = criteriaMaskIn;
        _templateList = templateListIn;
    }

    public TemplateResponse(List<ValuePair<DataViewDef, Integer>> templateListIn) {

        this(null, 0, templateListIn);
    }

    public TemplateResponse(DataViewDef templateIn, int criteriaMaskIn) {

        this(templateIn, criteriaMaskIn, null);
    }

    public TemplateResponse() {

        this(null, 0, null);
    }

    public void setTemplate(DataViewDef templateIn) {

        _template = templateIn;
    }

    public DataViewDef getTemplate() {

        return _template;
    }

    public void setTemplateList(List<ValuePair<DataViewDef, Integer>> templateListIn) {

        _templateList = templateListIn;
    }

    public List<ValuePair<DataViewDef, Integer>> getTemplateList() {

        return _templateList;
    }

    public void setCriteriaMask(int criteriaMaskIn) {

        _criteriaMask = criteriaMaskIn;
    }

    public int getCriteriaMask() {

        return _criteriaMask;
    }

    public int getTemplateCount() {

        return (null != _templateList) ? _templateList.size() : 0;
    }

    public boolean isOK(ResourceChoiceCriteria criteriaIn) {

        return ((1 << criteriaIn.ordinal()) <= _criteriaMask) && (null != getTemplate());
    }

    public void setResult(DataViewDef templateIn, Integer criteriaMaskIn) {

        _template = templateIn;
        _criteriaMask = criteriaMaskIn;
    }

    public void addToList(DataViewDef templateIn, Integer criteriaMaskIn) {

        accessTemplateList().add(new ValuePair<DataViewDef, Integer>(templateIn, criteriaMaskIn));
    }

    private List<ValuePair<DataViewDef, Integer>> accessTemplateList() {

        if (null == _templateList) {

            _templateList = new ArrayList<ValuePair<DataViewDef, Integer>>();
        }
        return _templateList;
    }
}
