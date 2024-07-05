package csi.server.common.dto.SelectionListData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.security.AccessControlEntry;
import csi.security.jaas.JAASRole;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.DisplayMode;

/**
 * Created by centrifuge on 9/14/2016.
 */
public class SharingContext implements IsSerializable {




    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    List<StringEntry> _allUserList = null;
    List<StringEntry> _allGroupList = null;
    List<StringEntry> _readUserList = null;
    List<StringEntry> _readGroupList = null;
    List<StringEntry> _editUserList = null;
    List<StringEntry> _editGroupList = null;
    List<StringEntry> _deleteUserList = null;
    List<StringEntry> _deleteGroupList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SharingContext() {

    }

    public SharingContext(List<String> userListIn, List<String> groupListIn) {

        initializeComponents();
        buildDefaultContext(userListIn, groupListIn, null, null);
    }

    public SharingContext(List<String> userListIn, List<String> groupListIn, String ownerIn, String adminGroupIn) {

        initializeComponents();
        buildDefaultContext(userListIn, groupListIn, ownerIn, adminGroupIn);
    }

    public SharingContext(List<String> userListIn, List<String> groupListIn,
                          List<AccessControlEntry> aclListIn, int resourceCountIn) {

        initializeComponents();
        buildContext(userListIn, groupListIn, aclListIn, resourceCountIn);
    }

    public void setAllUserList(List<StringEntry> listIn) {

        _allUserList = listIn;
    }

    public List<StringEntry> getAllUserList() {

        return _allUserList;
    }

    public void setAllGroupList(List<StringEntry> listIn) {

        _allGroupList = listIn;
    }

    public List<StringEntry> getAllGroupList() {

        return _allGroupList;
    }

    public void setReadUserList(List<StringEntry> listIn) {

        _readUserList = listIn;
    }

    public List<StringEntry> getReadUserList() {

        return _readUserList;
    }

    public void setReadGroupList(List<StringEntry> listIn) {

        _readGroupList = listIn;
    }

    public List<StringEntry> getReadGroupList() {

        return _readGroupList;
    }

    public void setEditUserList(List<StringEntry> listIn) {

        _editUserList = listIn;
    }

    public List<StringEntry> getEditUserList() {

        return _editUserList;
    }

    public void setEditGroupList(List<StringEntry> listIn) {

        _editGroupList = listIn;
    }

    public List<StringEntry> getEditGroupList() {

        return _editGroupList;
    }

    public void setDeleteUserList(List<StringEntry> listIn) {

        _deleteUserList = listIn;
    }

    public List<StringEntry> getDeleteUserList() {

        return _deleteUserList;
    }

    public void setDeleteGroupList(List<StringEntry> listIn) {

        _deleteGroupList = listIn;
    }

    public List<StringEntry> getDeleteGroupList() {

        return _deleteGroupList;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeComponents() {

        _allUserList = new ArrayList<StringEntry>();
        _allGroupList = new ArrayList<StringEntry>();
        _readUserList = new ArrayList<StringEntry>();
        _readGroupList = new ArrayList<StringEntry>();
        _editUserList = new ArrayList<StringEntry>();
        _editGroupList = new ArrayList<StringEntry>();
        _deleteUserList = new ArrayList<StringEntry>();
        _deleteGroupList = new ArrayList<StringEntry>();
    }

   private void buildDefaultContext(List<String> userListIn, List<String> groupListIn,
                                    String ownerIn, String adminGroupIn) {
      if (userListIn != null) {
         for (String myUser : userListIn) {
            _allUserList.add(new StringEntry(myUser, DisplayMode.NORMAL));
         }
      }
      if (groupListIn != null) {
         for (String myGroup : groupListIn) {
            _allGroupList.add(new StringEntry(myGroup, DisplayMode.NORMAL));
         }
      }
      if (ownerIn != null) {
         _readUserList.add(new StringEntry(ownerIn, DisplayMode.DISABLED));
         _editUserList.add(new StringEntry(ownerIn, DisplayMode.DISABLED));
         _deleteUserList.add(new StringEntry(ownerIn, DisplayMode.NORMAL));
      }
      if (adminGroupIn != null) {
         _deleteGroupList.add(new StringEntry(adminGroupIn, DisplayMode.DISABLED));
      }
   }

    private void buildContext(List<String> userListIn, List<String> groupListIn,
                              List<AccessControlEntry> aclListIn, final int resourceCountIn) {

        Map<String, String> myUserDisplayMap = new TreeMap<String, String>();
        Map<String, String> myGroupDisplayMap = new TreeMap<String, String>();
        Map<String, Map<AclControlType, Integer>> myUserMap = new TreeMap<String, Map<AclControlType, Integer>>();
        Map<String, Map<AclControlType, Integer>> myGroupMap = new TreeMap<String, Map<AclControlType, Integer>>();

        if (userListIn != null) {

            for (String myUserDisplay : userListIn) {

                String myUserKey = myUserDisplay.toLowerCase();

                myUserDisplayMap.put(myUserKey, myUserDisplay);
                myUserMap.put(myUserKey, new TreeMap<AclControlType, Integer>());
            }
        }
        if (groupListIn != null) {

            for (String myGroupDisplay : groupListIn) {

                String myGroupKey = myGroupDisplay.toLowerCase();

                myGroupDisplayMap.put(myGroupKey, myGroupDisplay);
                myGroupMap.put(myGroupKey, new TreeMap<AclControlType, Integer>());
            }
        }
        if (aclListIn != null) {

           for (AccessControlEntry myEntry : aclListIn) {

                Map<AclControlType, Integer> myMap = myUserMap.get(myEntry.getRoleName());

                if (null == myMap) {

                    myMap = myGroupMap.get(myEntry.getRoleName());
                }
                if (null != myMap) {

                    AclControlType myType = myEntry.getAccessType();
                    Integer myCount = myMap.get(myType);

                    if (null != myCount) {

                        myMap.put(myType, myCount + 1);

                    } else {

                        myMap.put(myType, 1);
                    }
                }
            }
        }
        for (Map.Entry<String, Map<AclControlType, Integer>> myEntry : myUserMap.entrySet()) {

                String myKey = myEntry.getKey();
                String myDisplay = myUserDisplayMap.get(myKey);
                Map<AclControlType, Integer> myMap = myEntry.getValue();
                Integer myReadCount = myMap.get(AclControlType.READ);
                Integer myEditCount = myMap.get(AclControlType.EDIT);
                Integer myDeleteCount = myMap.get(AclControlType.DELETE);

                if (null != myReadCount) {

                    _readUserList.add(new StringEntry(myDisplay, (resourceCountIn > myReadCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                if (null != myEditCount) {

                    _editUserList.add(new StringEntry(myDisplay, (resourceCountIn > myEditCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                if (null != myDeleteCount) {

                    _deleteUserList.add(new StringEntry(myDisplay, (resourceCountIn > myDeleteCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                _allUserList.add(new StringEntry(myDisplay, DisplayMode.NORMAL));
            }
        for (Map.Entry<String, Map<AclControlType, Integer>> myEntry : myGroupMap.entrySet()) {

                String myKey = myEntry.getKey();
                String myDisplay = myGroupDisplayMap.get(myKey);
                Map<AclControlType, Integer> myMap = myEntry.getValue();
                Integer myReadCount = myMap.get(AclControlType.READ);
                Integer myEditCount = myMap.get(AclControlType.EDIT);
                Integer myDeleteCount = myMap.get(AclControlType.DELETE);

                if (null != myReadCount) {

                    _readGroupList.add(new StringEntry(myDisplay, (resourceCountIn > myReadCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                if (null != myEditCount) {

                    _editGroupList.add(new StringEntry(myDisplay, (resourceCountIn > myEditCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                if (null != myDeleteCount) {

                    _deleteGroupList.add(new StringEntry(myDisplay, (resourceCountIn > myDeleteCount)
                            ? DisplayMode.SPECIAL : DisplayMode.NORMAL));
                }
                _allGroupList.add(new StringEntry(myDisplay, DisplayMode.NORMAL));
            }
        _deleteGroupList.add(new StringEntry(JAASRole.ADMIN_GROUP_NAME, DisplayMode.DISABLED));
    }
}
