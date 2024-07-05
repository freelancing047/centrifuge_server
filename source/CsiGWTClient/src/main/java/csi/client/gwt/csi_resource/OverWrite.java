package csi.client.gwt.csi_resource;

import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.util.ValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by centrifuge on 4/26/2019.
 */
public class OverWrite {

    private Map<String, Integer> _overWriteMap = null;
    private Map<String, Integer> _rejectMap = null;
    private Map<String, Integer> _localMap = null;

    public static Map<String, List<OverWrite>>
    createOverWriteCollection(List<List<ResourceBasics>> dataViewOverWriteIn,
                              List<List<ResourceBasics>> templateOverWriteIn,
                              List<List<ResourceBasics>> mapOverWriteIn,
                              Map<String, List<ResourceBasics>> adminDataViewOverWriteIn,
                              Map<String, List<ResourceBasics>> adminTemplateOverWriteIn,
                              Map<String, List<ResourceBasics>> adminMapOverWriteIn) {

        Map<String, List<OverWrite>> myMap = new TreeMap<String, List<OverWrite>>();
        List<OverWrite> myList = new ArrayList<OverWrite>();

        myMap.put("", myList);
        myList.add(new OverWrite(dataViewOverWriteIn));
        myList.add(new OverWrite(templateOverWriteIn));
        myList.add(new OverWrite(mapOverWriteIn));

        if ((null != adminDataViewOverWriteIn) && (0 < adminDataViewOverWriteIn.size())) {

            for (Map.Entry<String, List<ResourceBasics>> myEntry : adminDataViewOverWriteIn.entrySet()) {

                String myKey = myEntry.getKey();
                List<ResourceBasics> myConflicts = myEntry.getValue();

                if ((null != myKey) && (0 < myKey.length()) && (null != myConflicts) && (0 < myConflicts.size())) {

                    myList = myMap.get(myKey);
                    if (null == myList) {

                        myList = new ArrayList<OverWrite>();
                        myMap.put("myKey", myList);
                    }
                    myList.add(new OverWrite(null, myConflicts));
                }
            }
        }
        if ((null != adminTemplateOverWriteIn) && (0 < adminTemplateOverWriteIn.size())) {

            for (Map.Entry<String, List<ResourceBasics>> myEntry : adminTemplateOverWriteIn.entrySet()) {

                String myKey = myEntry.getKey();
                List<ResourceBasics> myConflicts = myEntry.getValue();

                if ((null != myKey) && (0 < myKey.length()) && (null != myConflicts) && (0 < myConflicts.size())) {

                    myList = myMap.get(myKey);
                    if (null == myList) {

                        myList = new ArrayList<OverWrite>();
                        myMap.put("myKey", myList);
                        myList.add(new OverWrite(null, null));
                    }
                    myList.add(new OverWrite(null, myConflicts));
                }
            }
        }
        if ((null != adminMapOverWriteIn) && (0 < adminMapOverWriteIn.size())) {

            for (Map.Entry<String, List<ResourceBasics>> myEntry : adminMapOverWriteIn.entrySet()) {

                String myKey = myEntry.getKey();
                List<ResourceBasics> myConflicts = myEntry.getValue();

                if ((null != myKey) && (0 < myKey.length()) && (null != myConflicts) && (0 < myConflicts.size())) {

                    myList = myMap.get(myKey);
                    if (null == myList) {

                        myList = new ArrayList<OverWrite>();
                        myMap.put(myKey, myList);
                        myList.add(new OverWrite(null, null));
                        myList.add(new OverWrite(null, null));
                    }
                    myList.add(new OverWrite(null, myConflicts));
                }
            }
        }
        if (0 < myMap.size()) {

            for (List<OverWrite> myOverWriteList : myMap.values()) {

                switch (myOverWriteList.size()) {

                    case 2:

                        myOverWriteList.add(new OverWrite(null, null));

                    case 1:

                        myOverWriteList.add(new OverWrite(null, null));

                    default:

                        break;
                }
            }
        }
        return myMap;
    }

    public OverWrite(List<List<ResourceBasics>> multiListIn) {

        this((1 < multiListIn.size()) ? multiListIn.get(1) : null,
                (2 < multiListIn.size()) ? multiListIn.get(2) : null);
    }

    private OverWrite(List<ResourceBasics> rejectIn, List<ResourceBasics> overWriteIn) {

        _overWriteMap = new TreeMap<String, Integer>();
        _rejectMap = new TreeMap<String, Integer>();
        _localMap = new TreeMap<String, Integer>();

        if ((null != overWriteIn) && (0 < overWriteIn.size())) {

            for (ResourceBasics myResource : overWriteIn) {

                _overWriteMap.put(myResource.getName(), 1);
            }
        }
        if ((null != rejectIn) && (0 < rejectIn.size())) {

            for (ResourceBasics myResource : rejectIn) {

                _rejectMap.put(myResource.getName(), 1);
            }
        }
    }

    public void clearLocalList() {

        _localMap = new TreeMap<String, Integer>();
    }

    public List<String> getLocalList(String nameIn) {

        List<String> myList = new ArrayList<String>();
        Integer myCount = _localMap.get(nameIn);

        if ((null != myCount) && (1 < myCount)) {

            myList.addAll(_localMap.keySet());

        } else {

            for (String myName : _localMap.keySet()) {

                if (!nameIn.equals(myName)) {

                    myList.add(myName);
                }
            }
        }
        return myList;
    }

    public void addLocal(String keyIn) {

        Integer myCount = _localMap.get(keyIn);

        if (null == myCount) {

            myCount = 0;
        }
        _localMap.put(keyIn, myCount + 1);
    }

    public void removeLocal(String keyIn) {

        Integer myCount = _localMap.get(keyIn);

        if (null != myCount) {

            myCount--;

            if (0 < myCount) {

                _localMap.put(keyIn, myCount);
            } else {

                _localMap.remove(keyIn);
            }
        }

    }

    public String getColor(String stringIn) {

        if (isBlocked(stringIn)) {

            return Dialog.txtErrorColor;

        } else if (isAuthorized(stringIn)) {

            return Dialog.txtWarningColor;

        } else {

            return Dialog.txtLabelColor;
        }
    }

    public boolean isRequired(String keyIn) {

        return isAuthorized(keyIn) || isBlocked(keyIn);
    }

    public boolean isNotRequired(String keyIn) {

        return !isRequired(keyIn);
    }

    public boolean isAuthorized(String keyIn) {

        return _overWriteMap.containsKey(keyIn);
    }

    public boolean isBlocked(String keyIn) {

        if (!_rejectMap.containsKey(keyIn)) {

            Integer myCount = _localMap.get(keyIn);

            if ((null == myCount) || (2 > myCount)) {

                return false;
            }
        }
        return true;
    }
}
