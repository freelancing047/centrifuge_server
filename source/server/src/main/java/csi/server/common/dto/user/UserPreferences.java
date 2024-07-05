package csi.server.common.dto.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Transient;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.user.preferences.DialogPreference;
import csi.server.common.dto.user.preferences.GeneralPreference;
import csi.server.common.dto.user.preferences.ResourceFilter;

/**
 * Created by centrifuge on 3/1/2016.
 */
public class UserPreferences implements IsSerializable {

    private List<ResourceFilter> _resourceFilterList;
    private List<DialogPreference> _dialogPreferenceList;
    private List<GeneralPreference> _generalPreferenceList;

    @Transient
    private Map<Long, ResourceFilter> _resourceFilterMap;
    @Transient
    private Map<String, OptionBasics> _resourceFilterSort;
    @Transient
    private Map<String, Map<String, DialogPreference>> _dialogPreferenceMap;
    @Transient
    private Map<String, GeneralPreference> _generalPreferenceMap;

    public UserPreferences() {

    }

    public void setResourceFilterList(List<ResourceFilter> resourceFilterListIn) {

        _resourceFilterList = resourceFilterListIn;
        _resourceFilterMap = null;
        _resourceFilterSort = null;
    }

    public List<ResourceFilter> getResourceFilterList() {

        return  _resourceFilterList;
    }

    public List<OptionBasics> getResourceFilterDisplayList() {

        return  new ArrayList(getResourceFilterSort().values());
    }

    public void setDialogPreferenceList(List<DialogPreference> dialogPreferenceListIn) {

        _dialogPreferenceList = dialogPreferenceListIn;
        _dialogPreferenceMap = null;
    }

    public List<DialogPreference> getDialogPreferenceList() {

        return _dialogPreferenceList;
    }

    public Map<String, DialogPreference> getDialogPreferenceMap(String dialogKeyIn) {

        return getDialogPreferenceMap().get(dialogKeyIn);
    }

    public List<DialogPreference> getDialogPreferenceList(String dialogKeyIn) {

        Map<String, DialogPreference> myMap = getDialogPreferenceMap().get(dialogKeyIn);

        return (null != myMap) ? new ArrayList<DialogPreference>(myMap.values()) : null;
    }

    public void setGeneralPreferenceList(List<GeneralPreference> generalPreferenceListIn) {

        _generalPreferenceList = generalPreferenceListIn;
        _generalPreferenceMap = null;
    }

    public List<GeneralPreference> getGeneralPreferenceList() {

        return _generalPreferenceList;
    }

    public boolean filterExists(String nameIn) {

        return getResourceFilterSort().containsKey(nameIn);
    }

    public Long getFilterId(String nameIn) {

        OptionBasics myFilter = (null != nameIn) ? getResourceFilterSort().get(nameIn.toLowerCase()) : null;

        return (null != myFilter) ? Long.parseLong(myFilter.getKey()) : null;
    }

    public boolean dialogPreferenceExists(String dialogKeyIn, String dataKeyIn) {

        Map<String, DialogPreference> myMap = getDialogPreferenceMap().get(dialogKeyIn);

        return (myMap != null) && myMap.containsKey(dataKeyIn);
    }

    public Long getDialogPreferenceId(String dialogKeyIn, String dataKeyIn) {

        Map<String, DialogPreference> myMap = getDialogPreferenceMap().get(dialogKeyIn);
        DialogPreference myPreference = (null != myMap) ? myMap.get(dataKeyIn) : null;

        return (null != myPreference) ? myPreference.getId() : null;
    }

    public boolean generalPreferenceExists(String dataKeyIn) {

        return getGeneralPreferenceMap().containsKey(dataKeyIn);
    }

    public Long getGeneralPreferenceId(String dataKeyIn) {

        GeneralPreference myPreference = getGeneralPreferenceMap().get(dataKeyIn);

        return (null != myPreference) ? myPreference.getId() : null;
    }

    public void addReplaceFilter(ResourceFilter filterIn) {

        Long myId = (null != filterIn) ? filterIn.getId() : null;

        if (null != myId) {

            if (getResourceFilterMap().containsKey(myId)) {

                ResourceFilter myOldFilter = getResourceFilterMap().get(myId);

                if (null != myOldFilter) {

                    String myKey = myOldFilter.getSortingName();

                    if ((null != myKey) && getResourceFilterSort().containsKey(myKey)) {

                        getResourceFilterSort().remove(myKey);
                    }
                }
                _resourceFilterMap.remove(myId);
            }
            getResourceFilterSort().put(filterIn.getSortingName(), filterIn.getOptionBasics());
            getResourceFilterMap().put(myId, filterIn);
        }
    }

    public DialogPreference addReplaceDialogPreference(DialogPreference preferenceIn) {

        Map<String, DialogPreference> myMap = getDialogPreferenceMap().get(preferenceIn.getDialogKey());
        DialogPreference myPreference = (null != myMap) ? myMap.get(preferenceIn.getDataKey()) : null;

        if (null != myPreference) {

            myPreference.setDataValue(preferenceIn.getDataValue());

        } else {


            if (null == myMap) {

                myMap = new TreeMap<String, DialogPreference>();
                getDialogPreferenceMap().put(preferenceIn.getDialogKey(), myMap);
            }
            myMap.put(preferenceIn.getDataKey(), preferenceIn);
        }
        return (null != myPreference) ? myPreference : preferenceIn;
    }

    public GeneralPreference addReplaceGeneralPreference(GeneralPreference preferenceIn) {

        GeneralPreference myPreference = getGeneralPreferenceMap().get(preferenceIn.getDataKey());

        if (null != myPreference) {

            myPreference.setDataValue(preferenceIn.getDataValue());

        } else {

            getGeneralPreferenceMap().put(preferenceIn.getDataKey(), preferenceIn);
        }
        return (null != myPreference) ? myPreference : preferenceIn;
    }

    public void deleteFilter(OptionBasics selectionIn) {

        deleteFilter((null != selectionIn) ? selectionIn.getKey() : null);
    }

    public void deleteFilter(String idIn) {

        try {
            deleteFilter(Long.parseLong(idIn));
        } catch (Exception ignore) {}
    }

    public void deleteFilter(Long idIn) {

        ResourceFilter myOldFilter = (null != idIn) ? getResourceFilter(idIn) : null;

        if ((null != myOldFilter) && getResourceFilterMap().containsKey(idIn)) {

            String mySortKey = myOldFilter.getSortingName();

            if ((null != mySortKey) && getResourceFilterSort().containsKey(mySortKey)) {

                getResourceFilterSort().remove(mySortKey);
            }
            getResourceFilterMap().remove(idIn);
        }
    }

    public Long deleteDialogPreference(String dialogKeyIn, String dataKeyIn) {

        Map<String, DialogPreference> myOldMap = getDialogPreferenceMap().get(dialogKeyIn);
        DialogPreference myOldPreference = (null != myOldMap) ? myOldMap.get(dataKeyIn) : null;
        Long myRecordKey = null;

        if (null != myOldPreference) {

            myRecordKey = myOldPreference.getId();
            myOldMap.remove(dataKeyIn);
        }

        return myRecordKey;
    }

    public Long deleteGeneralPreference(String dataKeyIn) {

        GeneralPreference myOldPreference = getGeneralPreferenceMap().get(dataKeyIn);
        Long myRecordKey = null;

        if (null != myOldPreference) {

            myRecordKey = myOldPreference.getId();
            getGeneralPreferenceMap().remove(dataKeyIn);
        }

        return myRecordKey;
    }

    public ResourceFilter getResourceFilter(OptionBasics selectionIn) {

        return getResourceFilter((null != selectionIn) ? selectionIn.getKey() : null);
    }

    public ResourceFilter getResourceFilter(String idIn) {

        try {

            return getResourceFilter((null != idIn) ? Long.parseLong(idIn) : null);

        } catch (Exception ignore) {

            return null;
        }
    }

    public ResourceFilter getResourceFilter(Long idIn) {

        return (null != idIn) ? getResourceFilterMap().get(idIn) : null;
    }

    public DialogPreference getDialogPreference(String dialogKeyIn, String dataKeyIn) {

        DialogPreference myPreference = null;

        if ((null != dialogKeyIn) && (null != dataKeyIn)) {

            Map<String, DialogPreference> myMap = getDialogPreferenceMap().get(dialogKeyIn);

            if (null != myMap) {

                myPreference = myMap.get(dataKeyIn);
            }
        }
        return myPreference;
    }

    public GeneralPreference getGeneralPreference(String dataKeyIn) {

        return getGeneralPreferenceMap().get(dataKeyIn);
    }

    private Map<String, OptionBasics> getResourceFilterSort() {

        if (null == _resourceFilterSort) {

            _resourceFilterSort = new TreeMap<String, OptionBasics>();

            if (_resourceFilterList != null) {

                for (ResourceFilter myFilter : _resourceFilterList) {

                    _resourceFilterSort.put(myFilter.getSortingName(), myFilter.getOptionBasics());
                }
            }
        }
        return _resourceFilterSort;
    }

    private Map<Long, ResourceFilter> getResourceFilterMap() {

        if (null == _resourceFilterMap) {

            _resourceFilterMap = new TreeMap<Long, ResourceFilter>();

            if (_resourceFilterList != null) {

                for (ResourceFilter myFilter : _resourceFilterList) {

                    _resourceFilterMap.put(myFilter.getId(), myFilter);
                }
            }
        }
        return _resourceFilterMap;
    }

    private Map<String, Map<String, DialogPreference>> getDialogPreferenceMap() {

        if (null == _dialogPreferenceMap) {

            _dialogPreferenceMap = new TreeMap<String, Map<String, DialogPreference>>();

            if (_dialogPreferenceList != null) {

                for (DialogPreference myPreference : _dialogPreferenceList) {

                    String myPrimaryKey = myPreference.getDialogKey();
                    String mySecondaryKey = myPreference.getDataKey();
                    Map<String, DialogPreference> myMap = _dialogPreferenceMap.get(myPrimaryKey);

                    if (null == myMap) {

                        myMap = new TreeMap<String, DialogPreference>();
                        _dialogPreferenceMap.put(myPrimaryKey, myMap);
                    }
                    myMap.put(mySecondaryKey, myPreference);
                }
            }
        }
        return _dialogPreferenceMap;
    }

    private Map<String, GeneralPreference> getGeneralPreferenceMap() {

        if (null == _generalPreferenceMap) {

            _generalPreferenceMap = new TreeMap<String, GeneralPreference>();

            if (_generalPreferenceList != null) {

                for (GeneralPreference myPreference : _generalPreferenceList) {

                    _generalPreferenceMap.put(myPreference.getDataKey(), myPreference);
                }
            }
        }
        return _generalPreferenceMap;
    }
}
