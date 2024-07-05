package csi.server.common.linkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.linkup.LinkupExtender;


public class LinkupHelper implements IsSerializable {

    private String _nodeDefId;
    private String _edgeDefId;
    private List<ParamMapEntry> _parameterList = null;
    private Collection<Integer> _rowList = new TreeSet<Integer>();

    public LinkupHelper() {

    }

    public LinkupHelper(List<ParamMapEntry> parameterListIn) {

        _parameterList = parameterListIn;
    }

    public LinkupHelper(List<ParamMapEntry> parameterListIn, Collection<Integer> rowListIn) {

        _parameterList = parameterListIn;
        initialzeRowList(rowListIn);

    }

    public LinkupHelper(LinkupExtender extenderIn) {

        _nodeDefId = extenderIn.getNodeDefId();
        _edgeDefId = extenderIn.getLinkDefId();
        _parameterList = extenderIn.getFinalParameterList();
    }

    public LinkupHelper(LinkupExtender extenderIn, Collection<Integer> rowListIn) {

        _nodeDefId = extenderIn.getNodeDefId();
        _edgeDefId = extenderIn.getLinkDefId();
        _parameterList = extenderIn.getFinalParameterList();
        initialzeRowList(rowListIn);
    }

    public String getNodeDefId() {

        return _nodeDefId;
    }

    public void setNodeDefId(String idIn) {

        _nodeDefId = idIn;
    }

    public String getEdgeDefId() {

        return _edgeDefId;
    }

    public void setEdgeDefId(String idIn) {

        _edgeDefId = idIn;
    }

    public List<ParamMapEntry> getParameterList() {
        if (_parameterList == null) {
            _parameterList = new ArrayList<ParamMapEntry>();
        }
        return _parameterList;
    }

    public List<ParamMapEntry> finalizeParameterList(FieldListAccess dataModelIn) {
       int howMany = _parameterList.size();

        for (int i = 0; i < howMany; i++) {
            ParamMapEntry myEntry = _parameterList.get(i);
            FieldDef myTargetField = dataModelIn.getFieldDefByLocalId(myEntry.getTargetFieldLocalId());

            if (null != myTargetField) {
                myEntry.setParamName(myTargetField.getFieldName());
            }
            myEntry.setParamOrdinal(i);
        }
        return _parameterList;
    }

    public void setParameterList(List<ParamMapEntry> parameterListIn) {
        _parameterList = parameterListIn;
    }

    public int getRowCount() {
        return _rowList.size();
    }

    public void addRows(Collection<Integer> rowListIn) {
        if ((rowListIn != null) && !rowListIn.isEmpty()) {
            for (Integer myRowId : rowListIn) {
                addRow(myRowId);
            }
        }
    }

   public void addRow(Integer rowIdIn) {
      if ((rowIdIn != null) && !_rowList.contains(rowIdIn)) {
         _rowList.add(rowIdIn);
      }
   }

    public Collection<Integer> getRowList() {
        return _rowList;
    }

    public boolean hasSelection() {
        return !_rowList.isEmpty();
    }

    public String getFormattedIdList() {
        return formatIntegerCollection();
    }

    private void initialzeRowList(Collection<Integer> rowListIn) {
        _rowList = rowListIn;

        if(_rowList == null) {
         _rowList = new TreeSet<Integer>();
      }
    }

    private String formatIntegerCollection() {

        StringBuilder myBuffer = new StringBuilder();

        if (!_rowList.isEmpty()) {

            for (Integer myRowId : _rowList) {

                myBuffer.append(myRowId.toString());
                myBuffer.append(',');
            }
            myBuffer.deleteCharAt(myBuffer.length() - 1);
        }

        return myBuffer.toString();
    }

}
