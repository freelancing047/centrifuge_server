package csi.server.common.model.linkup;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.ParamMapEntry;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LinkupExtender extends ModelObject {

    protected String _vizDefId;
    protected String _nodeDefId;
    protected String _linkDefId;
    protected String _name;
    protected String _description;
    protected Boolean _isDisabled;

    // map of current DV fieldDefs of the current
    // DV to parameter name of the DV being merged in
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<ParamMapEntry> _parameterList;

    public LinkupExtender() {
        super();
    }

    public String getVizDefId() {

        return _vizDefId;
    }

    public void setVizDefId(String idIn) {

        _vizDefId = idIn;
    }

    public String getNodeDefId() {

        return _nodeDefId;
    }

    public void setNodeDefId(String idIn) {

        _nodeDefId = idIn;
    }

    public String getLinkDefId() {

        return _linkDefId;
    }

    public void setLinkDefId(String idIn) {

        _linkDefId = idIn;
    }

    public String getName() {

        return _name;
    }

    public void setName(String nameIn) {

        _name = nameIn;
    }

    public String getDescription() {

        return _description;
    }

    public void setDescription(String descriptionIn) {

        _description = descriptionIn;
    }

    public Boolean getIsDisabled() {

        return _isDisabled;
    }

    public void setIsDisabled(Boolean isDisabledIn) {

        _isDisabled = isDisabledIn;
    }

    public List<ParamMapEntry> getParameterList() {
        if (_parameterList == null) {
            _parameterList = new ArrayList<ParamMapEntry>();
        }
        return _parameterList;
    }

    public List<ParamMapEntry> getFinalParameterList() {
        List<ParamMapEntry> myParameterList = new ArrayList<ParamMapEntry>();

        if ((null != _parameterList) && !_parameterList.isEmpty()) {

            for (ParamMapEntry myEntry : _parameterList) {

                myParameterList.add(myEntry.newCopy());
            }
        }
        return myParameterList;
    }

    public void setParameterList(List<ParamMapEntry> _parameterListIn) {
        _parameterList = _parameterListIn;
    }

    public int size() {

        return (null != _parameterList) ? _parameterList.size() : 0;
    }

   public boolean isReady() {
      boolean myReady = !_parameterList.isEmpty();
      int howMany = _parameterList.size();

      for (int i = 0; i < howMany; i++) {
         if (!myReady) {
            break;
         }
         myReady = _parameterList.get(i).isValid();
      }
      return myReady;
   }
}
