package csi.server.common.service.api;

import java.util.List;

import csi.server.common.dto.*;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.SelectionListData.SharingContext;
import csi.server.common.dto.system.FilteredUserRequest;
import csi.server.common.dto.user.UserPreferences;
import csi.server.common.dto.user.preferences.DialogPreference;
import csi.server.common.dto.user.preferences.GeneralPreference;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.GroupType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ValuePair;
import csi.shared.gwt.vortex.VortexService;


public interface UserAdministrationServiceProtocol extends VortexService {


    public StartUpDownload getStartupInfo() throws CentrifugeException;


    public UserPreferences getUserPreferences(Long keyIn);


    public Response<Long, List<ResourceFilter>>  getResourceFilterList(Long keyIn);


    public Response<Long, List<ResourceFilter>> addReplaceResourceFilter(Long keyIn, ResourceFilter filterIn);


    public Response<Long, List<ResourceFilter>> deleteResourceFilter(Long keyIn, Long idIn);


    public Response<Long, List<ResourceFilter>> deleteResourceFilters(Long keyIn, List<Long> idListIn);


    public Response<Long, DialogPreference> addReplaceDialogPreference(Long keyIn, DialogPreference preferenceIn);


    public void deleteDialogPreference(Long keyIn, Long idIn);


    public Response<Long, GeneralPreference> addReplaceGeneralPreference(Long keyIn, GeneralPreference preferenceIn);


    public void deleteGeneralPreference(Long keyIn, Long idIn);


    public Response<String, List<List<String>>> identifyRestrictedStrings();


    public Response<String, List<UserDisplay>> searchUsers(String searchStringIn, List<Boolean> userFlagsIn);


    public Response<String, List<UserDisplay>> searchGroupUsers(String searchStringIn, String groupNameIn,
                                                                String classificationIn, List<Boolean> userFlagsIn);

    
    public Response<String, List<UserDisplay>> retrieveUsers(List<String> listIn);
    
    
    public Response<GroupType, List<GroupDisplay>> retrieveGroups(GroupType typeIn, List<String> listIn);

    public Response<String, List<ReportsDisplay>> retrieveReports();

    public Response<String, List<EventsDisplay>> retrieveEvents();

    public Response<String, Integer> retrieveMaxConcurrencyInformation();

    public Response<GroupType, List<GroupDisplay>> searchGroups(GroupType typeIn, String searchStringIn);
    

    public Response<GroupType, List<GroupDisplay>> searchGroupGroups(GroupType typeIn, String groupNameIn,
                                                                     String searchStringIn);
    

    public Response<String, List<String>> getUserNames();


    public Response<GroupType, List<String>> getGroupNames(GroupType typeIn);


    public Response<String, ValuePair<List<String>, List<String>>> listAllUsersAndGroups(String keyIn);


    public Response<String, ValuePair<List<String>, List<String>>> listActiveUsersAndGroups(String keyIn);

    
    public Response<String, UserDisplay> createUser(UserDisplay userIn);
    
    
    public Response<String, UserDisplay> updateUser(UserDisplay userIn);
    
    
    public Response<String, GroupDisplay> createGroup(GroupDisplay groupIn);
    
    
    public Response<String, GroupDisplay> updateGroup(GroupDisplay groupIn);
    
    
    public Response<String, List<String>> deleteGroups(GroupType typeIn, List<String> groupListIn);

    
    public Response<String, List<String>> addGroupsToGroup(String groupNameIn, List<String> groupListIn);
    
    
    public Response<String, List<String>> removeGroupsFromGroup(String groupNameIn, List<String> groupListIn);


    public Response<String, List<String>> activateUsers(List<String> userListIn);


    public Response<String, List<String>> deactivateUsers(List<String> userListIn);

    
    public Response<String, List<String>> deleteUsers(List<String> userListIn);
    
    
    public Response<String, List<String>> addUsersToGroup(String groupIn, List<String> userListIn);
    
    
    public Response<String, List<String>> removeUsersFromGroup(String groupIn, List<String> userListIn);
    

    public void setPassword(String passwordIn) throws CentrifugeException;


    public Response<String, List<String>> getActiveUserNames();


    public Response<String, List<String>> getAllUserNames();


    public Response<String, List<String>> getActiveGroupNames();


    public Response<String, List<SharingDisplay>> share(String keyIn, List<String> resourceListIn,
                                                        SharingRequest sharingRequestIn);

    public Response<String, List<SharingDisplay>> getAclInfo(List<String> resourceListIn);


    public Response<String, List<SharingDisplay>> getSharingNames(AclResourceType resourceTypeIn,
                                                                  ResourceFilter filterIn, String patternIn,
                                                                  String ownerIn);


    public Response<String, SharingDisplay> getSingleSharingName(AclResourceType resourceTypeIn,
                                                                 String nameIn, String ownerIn);


    public Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>
    defineSharing(AclResourceType resourcetypeIn, List<String> resourcesIn, String roleIn,
                  List<AclControlType> permissionsIn, boolean setOwnerIn);


    public Response<String, SharingContext> retrieveSharingContext(String keyIn, List<String> resourceIdListIn);

    public Response<Integer, List<StringEntry>> retrieveFilteredUserList(Integer keyIn, FilteredUserRequest filterIn);
}
