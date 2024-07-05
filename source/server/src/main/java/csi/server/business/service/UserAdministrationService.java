package csi.server.business.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.ClientConfig;
import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.AccessControlEntry;
import csi.security.CsiSecurityManager;
import csi.security.Passwords;
import csi.security.jaas.JAASRole;
import csi.security.loginevent.EventReasons;
import csi.security.loginevent.LoginEvent;
import csi.security.loginevent.LoginEventService;
import csi.security.loginevent.PeriodLoginEvents;
import csi.security.loginevent.RepositoryException;
import csi.security.loginevent.YearEventPeriods;
import csi.security.monitors.CapcoRollup;
import csi.security.queries.AclRequest;
import csi.security.queries.DirectMetaDB;
import csi.security.queries.UserEnvironment;
import csi.security.queries.Users;
import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.SecurityHelper;
import csi.server.business.service.annotation.Operation;
import csi.server.common.dto.DataPair;
import csi.server.common.dto.EventsDisplay;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.dto.ReportsDisplay;
import csi.server.common.dto.Response;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.StartUpDownload;
import csi.server.common.dto.UserDisplay;
import csi.server.common.dto.SelectionListData.SharingContext;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.system.FilteredUserRequest;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.dto.system.UserFunction;
import csi.server.common.dto.user.UserPreferences;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.dto.user.preferences.DialogPreference;
import csi.server.common.dto.user.preferences.GeneralPreference;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.enumerations.GroupType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.CapcoGroup;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.SynchronizeChanges;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.Product;

public class UserAdministrationService extends AbstractService implements UserAdministrationServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(UserAdministrationService.class);

   private static final String META_VIEWER_GROUP = JAASRole.META_VIEWER_GROUP_NAME;
   private static final String ORIGINATOR_GROUP = JAASRole.ORIGINATOR_GROUP_NAME;
   private static final String ADMINISTRATOR_GROUP = JAASRole.ADMIN_GROUP_NAME;
   private static final String ADMINISTRATOR_USER = JAASRole.ADMIN_USER_NAME;
   private static final String SECURITY_GROUP = JAASRole.SECURITY_GROUP_NAME;
   private static final String SECURITY_USER = JAASRole.SECURITY_USER_NAME;
   private static final String EVERYONE_GROUP = JAASRole.EVERYONE_GROUP_NAME;

   private static final String GROUP_CONJUNCTION = ", ";
   private static final Pattern GROUP_CONJUNCTION_PATTERN = Pattern.compile(GROUP_CONJUNCTION);

    private static SecurityPolicyConfig securityInfo = null;

    public static SecurityPolicyConfig getSecurityInfo() {

        if (securityInfo == null) {

            securityInfo = Configuration.getInstance().getSecurityPolicyConfig();
        }
        return securityInfo;
    }

    private boolean isAdminGroup(String nameIn) {

        return (!nameIn.equals(ORIGINATOR_GROUP)) && (!nameIn.equals(SECURITY_GROUP));
    }

    private boolean isAdminOnlyGroup(String nameIn) {

        return (isSecurityGroup(nameIn));
    }

    private boolean isSecurityGroup(String nameIn) {

        return nameIn.equals(META_VIEWER_GROUP) || nameIn.equals(ORIGINATOR_GROUP) || nameIn.equals(SECURITY_GROUP);
    }

    private boolean isSecurityOnlyGroup(String nameIn) {

        return nameIn.equals(ORIGINATOR_GROUP) || nameIn.equals(SECURITY_GROUP);
    }

    @Override
   @Operation
    public StartUpDownload getStartupInfo() throws CentrifugeException {

        String myUsername = CsiSecurityManager.getUserName();

        if ((null != myUsername) && (0 < myUsername.length())) {

            Configuration myConfig = Configuration.getInstance();
            ClientConfig myClientConfig = myConfig.getClientConfig();
            UserSecurityInfo mySecurity = new UserSecurityInfo();
            UserEnvironment myUserEnvironment = UserEnvironment.getUserEnvironment();
            UserPreferences myPreferences = myUserEnvironment.getUserPreferences();
            List<UserFunction> myFunctions = UserFunction.getUserFunctions();
            boolean myCaselessNames = (null != System.getenv()) && (null != System.getenv().get("OS"))
                    && (System.getenv().get("OS").toLowerCase().contains("windows"));
            boolean myProvideSourceName = myConfig.getClientConfig().getProvideSourceName();
            boolean myBracketDefault = myConfig.getClientConfig().getBracketDefault();
            boolean myIncrementImmediately = myConfig.getClientConfig().getIncrementImmediately();
            Set<String> myRoles = CsiSecurityManager.getUserRoles();
            String myReleaseVersion = ReleaseInfo.version;
            int mySatisfiedCount = myConfig.getPopupConfig().getSatisfiedCount();
            int myMilliSecondWait = myConfig.getPopupConfig().getMillisecondWait();
            String myBuildNumber = ReleaseInfo.build;

            mySecurity.setAdminGroup(JAASRole.ADMIN_GROUP_NAME);
            mySecurity.setCsoGroup(JAASRole.SECURITY_GROUP_NAME);
            mySecurity.setViewerGroup(JAASRole.META_VIEWER_GROUP_NAME);
            mySecurity.setOriginatorGroup(JAASRole.ORIGINATOR_GROUP_NAME);
            mySecurity.setEveryoneGroup(JAASRole.EVERYONE_GROUP_NAME);
            mySecurity.setAdminUser(JAASRole.ADMIN_USER_NAME);
            mySecurity.setCsoUser(JAASRole.SECURITY_USER_NAME);
            mySecurity.setName(myUsername);
            mySecurity.setAdmin(CsiSecurityManager.isAdmin());
            mySecurity.setSecurity(CsiSecurityManager.isSecurity());
            mySecurity.setRestricted(CsiSecurityManager.isRestricted());
            mySecurity.setCanSetSecurity(CsiSecurityManager.canChangeSecurity());
            mySecurity.setDoCapco(SecurityHelper.doCapco());
            mySecurity.setDoTags(SecurityHelper.doTags());
            mySecurity.setIconAdmin(CsiSecurityManager.isIconAdmin());
            mySecurity.setFieldListAdmin(CsiSecurityManager.isFieldListAdmin());

            for (String myRole : myRoles) {

                mySecurity.addRole(myRole);
            }
            if (null == myFunctions) {

                myFunctions = AclRequest.getUserFunctionList();
                UserFunction.loadUserFunctions(myFunctions);
            }
            return new StartUpDownload(mySecurity, myPreferences, myFunctions, myClientConfig, myProvideSourceName,
                                        myBracketDefault, myIncrementImmediately, myCaselessNames,
                                        myReleaseVersion, myBuildNumber, mySatisfiedCount, myMilliSecondWait);
        } else {

            throw new CentrifugeException("Unable to retrieve username!");
        }
    }

    @Override
   @Operation
    public UserPreferences getUserPreferences(Long keyIn) {

        return UserEnvironment.getUserEnvironment().getUserPreferences();
    }

    @Override
   @Operation
    public Response<Long, List<ResourceFilter>>  getResourceFilterList(Long keyIn) {

        return new Response<Long, List<ResourceFilter>>(keyIn, UserEnvironment.getUserEnvironment().getResourceFilters());
    }

    @Override
   @Operation
    public Response<Long, List<ResourceFilter>> addReplaceResourceFilter(Long keyIn, ResourceFilter filterIn) {

        try {

            if (null != filterIn.getName()) {

                UserEnvironment.getUserEnvironment().addReplaceResourceFilter(filterIn);

                return new Response<Long, List<ResourceFilter>>(keyIn, UserEnvironment.getUserEnvironment().getResourceFilters());
            }
            return new Response<Long, List<ResourceFilter>>(keyIn, ServerMessage.MISSING_DATA_KEY);

        } catch (Exception myException) {

            return new Response<Long, List<ResourceFilter>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, myException.getMessage());
        }
    }

    @Override
   @Operation
    public Response<Long, List<ResourceFilter>> deleteResourceFilter(Long keyIn, Long idIn) {

        try {

            UserEnvironment.getUserEnvironment().deleteResourceFilter(idIn);

        } catch (Exception ignore) {
        }
        return new Response<Long, List<ResourceFilter>>(keyIn, UserEnvironment.getUserEnvironment().getResourceFilters());
    }

    @Override
   @Operation
    public Response<Long, List<ResourceFilter>> deleteResourceFilters(Long keyIn, List<Long> idListIn) {

        for (Long myId : idListIn) {

            try {

                UserEnvironment.getUserEnvironment().deleteResourceFilter(myId);
                CsiPersistenceManager.commit();

            } catch (Exception ignore) {

                CsiPersistenceManager.rollback();
            }
            CsiPersistenceManager.begin();
        }
        return new Response<Long, List<ResourceFilter>>(keyIn, UserEnvironment.getUserEnvironment().getResourceFilters());
    }

    @Override
   @Operation
    public Response<Long, DialogPreference> addReplaceDialogPreference(Long keyIn, DialogPreference preferenceIn) {

        try {

            if (null != preferenceIn.getDialogKey()) {

                if (null != preferenceIn.getDataKey()) {

                    return new Response<Long, DialogPreference>(UserEnvironment.getUserEnvironment().addReplaceDialogPreference(preferenceIn));
                }
                return new Response<Long, DialogPreference>(keyIn, ServerMessage.MISSING_DATA_KEY);
            }
            return new Response<Long, DialogPreference>(keyIn, ServerMessage.MISSING_DIALOG_KEY);

        } catch (Exception myException) {

            return new Response<Long, DialogPreference>(keyIn, ServerMessage.CAUGHT_EXCEPTION, myException.getMessage());
        }
    }

    @Override
   @Operation
    public void deleteDialogPreference(Long keyIn, Long idIn) {

        try {

            UserEnvironment.getUserEnvironment().deleteDialogPreference(idIn);

        } catch (Exception ignore) {
        }
    }

    @Override
   @Operation
    public Response<Long, GeneralPreference> addReplaceGeneralPreference(Long keyIn, GeneralPreference preferenceIn) {

        try {

            if (null != preferenceIn.getDataKey()) {

                return new Response<Long, GeneralPreference>(keyIn, UserEnvironment.getUserEnvironment().addReplaceGeneralPreference(preferenceIn));
            }
            return new Response<Long, GeneralPreference>(keyIn, ServerMessage.MISSING_DATA_KEY);

        } catch (Exception myException) {

            return new Response<Long, GeneralPreference>(keyIn, ServerMessage.CAUGHT_EXCEPTION, myException.getMessage());
        }
    }

    @Override
   @Operation
    public void deleteGeneralPreference(Long keyIn, Long idIn) {

        try {

            UserEnvironment.getUserEnvironment().deleteGeneralPreference(idIn);

        } catch (Exception ignore) {
        }
    }

   @Override
   @Operation
   public Response<String,List<List<String>>> identifyRestrictedStrings() {
      Response<String,List<List<String>>> results = null;

      if (CsiSecurityManager.isAdmin() || CsiSecurityManager.isSecurity()) {
         List<List<String>> resultingList =
            new ArrayList<List<String>>(Arrays.asList(Users.getAllRoleNames(), Users.getAllCapcoPortions()));
         results = new Response<String,List<List<String>>>(resultingList, checkUserLimit());
      } else {
         results = new Response<String,List<List<String>>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<UserDisplay>> searchGroupUsers(String searchString, String groupName,
                                                              String classification, List<Boolean> userFlags) {
      Response<String,List<UserDisplay>> results = null;

      if (CsiSecurityManager.isAdmin() || CsiSecurityManager.isSecurity()) {
         List<User> users =
            Users.getExhaustiveUsersInGroup(groupName, classification, StringUtil.patternToSql(searchString), userFlags);
         results = new Response<String,List<UserDisplay>>(createUserDisplay(users), checkUserLimit());
      } else {
         results = new Response<String,List<UserDisplay>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<UserDisplay>> searchUsers(String searchString, List<Boolean> userFlags) {
      Response<String,List<UserDisplay>> results = null;

      if (CsiSecurityManager.isAdmin() || CsiSecurityManager.isSecurity()) {
         List<User> users =
            (StringUtils.isEmpty(searchString) || searchString.equals("*"))
               ? Users.getAllUsers(userFlags)
               : Users.searchUsers(StringUtil.patternToSql(searchString), userFlags);
         results = new Response<String,List<UserDisplay>>(createUserDisplay(users), checkUserLimit());
      } else {
         results = new Response<String,List<UserDisplay>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<UserDisplay>> retrieveUsers(List<String> list) {
      Response<String,List<UserDisplay>> results = null;

      if (CsiSecurityManager.isAdmin() || CsiSecurityManager.isSecurity()) {
         try {
            results = new Response<String,List<UserDisplay>>(createUserDisplay(Users.getUsers(list)), checkUserLimit());
         } catch (Exception myException) {
            results = new Response<String,List<UserDisplay>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
         }
      } else {
         results = new Response<String,List<UserDisplay>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<GroupType,List<GroupDisplay>> retrieveGroups(GroupType type, List<String> list) {
      Response<GroupType,List<GroupDisplay>> results = null;

      if (((GroupType.SHARING == type) && CsiSecurityManager.isAdmin()) ||
          ((GroupType.SECURITY == type) && CsiSecurityManager.isSecurity())) {
         try {
            results = new Response<GroupType,List<GroupDisplay>>(type, formatGroups(Users.getGroups(type, list)), checkUserLimit());
         } catch (Exception exception) {
            results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
         }
      } else if (GroupType.SHARING == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.USER_NOT_AUTHORIZED);
      } else if (GroupType.SECURITY == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CSO_REQUIRED);
      } else {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<GroupType,List<GroupDisplay>> searchGroups(GroupType type, String searchString) {
      Response<GroupType,List<GroupDisplay>> results = null;

      if (((GroupType.SHARING == type) && CsiSecurityManager.isAdmin()) ||
          ((GroupType.SECURITY == type) && CsiSecurityManager.isSecurity())) {
         List<Group> groups =
            (StringUtils.isEmpty(searchString) || searchString.equals("*"))
               ? Users.getAllGroups(type)
               : Users.searchGroups(type, StringUtil.patternToSql(searchString));

         try {
            results = new Response<GroupType,List<GroupDisplay>>(type, formatGroups(groups), checkUserLimit());
         } catch (Exception exception) {
            results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
         }
      } else if (GroupType.SHARING == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.ADMIN_REQUIRED);
      } else if (GroupType.SECURITY == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CSO_REQUIRED);
      } else {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<GroupType,List<GroupDisplay>> searchGroupGroups(GroupType type, String groupName, String searchString) {
      Response<GroupType,List<GroupDisplay>> results = null;

      if (((GroupType.SHARING == type) && CsiSecurityManager.isAdmin()) ||
          ((GroupType.SECURITY == type) && CsiSecurityManager.isSecurity())) {
         try {
            List<Group> groups = Users.getExhaustiveGroupsInGroup(groupName, StringUtil.patternToSql(searchString), type);
            results = new Response<GroupType,List<GroupDisplay>>(type, formatGroups(groups), checkUserLimit());
         } catch (Exception myException) {
            results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
         }
      } else if (GroupType.SHARING == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.ADMIN_REQUIRED);
      } else if (GroupType.SECURITY == type) {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.CSO_REQUIRED);
      } else {
         results = new Response<GroupType,List<GroupDisplay>>(type, ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> getUserNames() {
      return new Response<String,List<String>>(Users.listUsers(), checkUserLimit());
   }

   @Override
   @Operation
   public Response<GroupType,List<String>> getGroupNames(GroupType type) {
      Response<GroupType,List<String>> results = null;

      if ((GroupType.SHARING == type) || ((GroupType.SECURITY == type) && CsiSecurityManager.isSecurity())) {
         results = new Response<GroupType,List<String>>(type, Users.listGroups(type), checkUserLimit());
      } else if (GroupType.SECURITY == type) {
         // Limit the response to users security groups
         results = new Response<GroupType,List<String>>(type, Users.listGroups(type), checkUserLimit());
      } else {
         results = new Response<GroupType,List<String>>(type, ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   public Response<String,ValuePair<List<String>,List<String>>> listAllUsersAndGroups(String key) {
      List<String> users = Users.getAllUserNames();
      List<String> groups = Users.getActiveGroupNames();

      return new Response<String,ValuePair<List<String>,List<String>>>(key, new ValuePair<List<String>,List<String>>(users, groups));
   }

   @Override
   public Response<String,ValuePair<List<String>,List<String>>> listActiveUsersAndGroups(String key) {
      List<String> users = Users.getActiveUserNames();
      List<String> groups = Users.getActiveGroupNames();

      return new Response<String,ValuePair<List<String>,List<String>>>(key, new ValuePair<List<String>,List<String>>(users, groups));
   }

   @Override
   @Operation
   public Response<String,UserDisplay> createUser(UserDisplay userDisplay) {
      Response<String,UserDisplay> results = null;

      if (CsiSecurityManager.isAdmin()) {
         if ((userDisplay != null) && (userDisplay.getName() != null)) {
            if (Product.getLicense().addingUserAllowed((int) Users.getUserCount())) {
               try {
                  Users.validateUser();

                  User user = extractUserData(null, userDisplay);
                  String display = user.getDisplay();
                  boolean disabled = user.isDisabled();

                  LOG.info("Add {}username {}", () -> disabled ? "disabled " : "", () -> Format.value(display));

                  user = Users.add(user);

                  addGroupMemberships(userDisplay.getName(), userDisplay.getGroups(), user);
                  addGroupMemberships(userDisplay.getName(), userDisplay.getClearance(), user);

                  user = Users.update(user);
                  /*
                   * Done within Users library CsiPersistenceManager.commit();
                   * CsiPersistenceManager.begin();
                   */
                  CsiPersistenceManager.refreshObject(user);

                  results = new Response<String,UserDisplay>(display, createUserDisplay(user), checkUserLimit(), null);
               } catch (Exception exception) {
                  results = new Response<String,UserDisplay>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
               }
            } else {
               results = new Response<String,UserDisplay>(userDisplay.getName(), ServerMessage.LICENSE_EXCEEDED);
            }
         } else {
            results = new Response<String,UserDisplay>("<null>", ServerMessage.CORRUPT_USER_DATA);
         }
      } else {
         results = new Response<String,UserDisplay>(Format.value(userDisplay.getName()), ServerMessage.ADMIN_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String, UserDisplay> updateUser(UserDisplay userDisplay) {
      Response<String, UserDisplay> results = null;

      if (CsiSecurityManager.isAdmin() || CsiSecurityManager.isSecurity()) {
         if ((userDisplay != null) && (userDisplay.getName() != null)) {
            try {
               Users.validateUser((!userDisplay.getDisabled().booleanValue()) && (!userDisplay.getSuspended().booleanValue()));

               User oldUser = Users.getUserByName(userDisplay.getName());
               User user = oldUser;
               String formattedUserDisplay = Format.value(user.getDisplay());
               ServerMessage message = null;

               if (user.isDisabled() && !userDisplay.getDisabled().booleanValue()) {
                  if (Product.getLicense().addingUserAllowed((int) Users.getUserCount())) {
                     LOG.info("Activate username {}", () -> formattedUserDisplay);
                  } else {
                     userDisplay.setDisabled(Boolean.TRUE);
                     userDisplay.setPerpetual(Boolean.FALSE);
                     message = ServerMessage.LICENSE_USER_DISABLED;
                  }
               } else if (!user.isDisabled() && userDisplay.getDisabled().booleanValue()) {
                  LOG.info("Deactivate username {}", () -> formattedUserDisplay);
               }

               if (user.isSuspended() && !userDisplay.getSuspended().booleanValue()) {
                   LOG.info("Un-suspend username {}", () -> formattedUserDisplay);
               } else if (!user.isSuspended() && userDisplay.getDisabled().booleanValue()) {
                   LOG.info("Suspend username {}", () -> formattedUserDisplay);
               }

               if (CsiSecurityManager.isAdmin()) {
                  user = extractUserData(oldUser, userDisplay);
               }
               cancelGroupMemberships(user, userDisplay);
               addGroupMemberships(userDisplay.getName(), userDisplay.getGroups(), user);

               if (CsiSecurityManager.isSecurity()) {
                  cancelClearances(user, userDisplay);
                  addGroupMemberships(userDisplay.getName(), userDisplay.getClearance(), user);
               }
               user = Users.update(user);
               /* Done within Users library
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                */
               CsiPersistenceManager.refreshObject(user);

               results = new Response<String, UserDisplay>(user.getDisplay(), createUserDisplay(user), checkUserLimit(), message);
            } catch (Exception exception) {
               results = new Response<String, UserDisplay>(userDisplay.getName(), ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         } else {
            results = new Response<String, UserDisplay>("<null>", ServerMessage.CORRUPT_USER_DATA);
         }
      } else {
         results = new Response<String, UserDisplay>(Format.value(userDisplay.getName()), ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String, List<String>> activateUsers(List<String> users) {
      Response<String, List<String>> results = null;

      if (CsiSecurityManager.isAdmin()) {
         List<String> activatedUsers = new ArrayList<String>();

         if ((users != null) && !users.isEmpty()) {
            try {
               Users.validateUser();

               int availableUserSlots = Product.getLicense().availableLicenses((int) Users.getUserCount());

               if (availableUserSlots > 0) {
                  if (availableUserSlots >= users.size()) {
                     for (String user : users) {
                        LOG.info("Activate username {}", () -> Format.value(user));
                        Users.activateByName(user);
                        activatedUsers.add(user);
                     }
                     results = new Response<String, List<String>>(activatedUsers, checkUserLimit());
                  } else {
                     int howMany = users.size() - availableUserSlots;

                     for (int i = 0; i < howMany; i++) {
                        String user = users.get(i);

                        LOG.info("Activate username {}", () -> Format.value(user));
                        Users.activateByName(user);
                        activatedUsers.add(user);
                     }
                     results = new Response<String, List<String>>(activatedUsers, availableUserSlots, ServerMessage.LICENSE_LIMITATION);
                  }
               } else {
                  results = new Response<String, List<String>>(activatedUsers, availableUserSlots, ServerMessage.LICENSE_LIMITATION);
               }
            } catch (Exception exception) {
               results = new Response<String, List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         } else {
            results = new Response<String, List<String>>(activatedUsers, checkUserLimit());
         }
      } else {
         results = new Response<String, List<String>>(ServerMessage.ADMIN_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> deactivateUsers(List<String> users) {
      Response<String,List<String>> results = null;

      if (CsiSecurityManager.isAdmin()) {
         if ((users != null) && !users.isEmpty()) {
            try {
               Users.validateUser(false);

               String self = CsiSecurityManager.getUserName();

               for (String user : users) {
                  if (!user.equalsIgnoreCase(ADMINISTRATOR_USER) && !user.equalsIgnoreCase(SECURITY_USER) &&
                      !user.equalsIgnoreCase(self)) {
                     LOG.info("Deactivate username {}", () -> Format.value(user));
                     Users.deactivateByName(user);
                  }
               }
            } catch (Exception exception) {
               results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         }
         if (results == null) {
            results = new Response<String,List<String>>(users, checkUserLimit());
         }
      } else {
         results = new Response<String,List<String>>(ServerMessage.ADMIN_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> deleteUsers(List<String> users) {
      Response<String,List<String>> results = null;

      if (CsiSecurityManager.isAdmin()) {
         if ((users != null) && !users.isEmpty()) {
            try {
               Users.validateUser(false);

               String self = CsiSecurityManager.getUserName();

               for (String user : users) {
                  if (!user.equalsIgnoreCase(ADMINISTRATOR_USER) && !user.equalsIgnoreCase(SECURITY_USER) &&
                      !user.equalsIgnoreCase(self)) {
                     LOG.info("Delete username {}", () -> Format.value(user));
                     Users.deleteByName(user);
                     AclRequest.removeRoleFromAllACLs(user);
                     AclRequest.replaceAllOwnership(user, ADMINISTRATOR_USER);
                  }
               }
            } catch (Exception exception) {
               results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         }
         if (results == null) {
            results = new Response<String,List<String>>(Users.getAllRoleNames(), checkUserLimit());  //TODO: users?
         }
      } else {
         results = new Response<String,List<String>>(ServerMessage.ADMIN_REQUIRED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> addUsersToGroup(String groupName, List<String> users) {
      Response<String,List<String>> results = null;

      try {
         Users.validateUser();

         GroupType groupType = Users.getGroupType(groupName);
// TODO:
         if (((GroupType.SHARING == groupType) &&
              ((isAdminGroup(groupName) && CsiSecurityManager.isAdmin()) ||
               (isSecurityGroup(groupName) && CsiSecurityManager.isSecurity()))) ||
             ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
            if ((users != null) && !users.isEmpty()) {
               String formattedGroupName = Format.value(groupName);

               for (String user : users) {
                  LOG.info("Add username {} to group {}", () -> Format.value(user), () -> formattedGroupName);
                  Users.addUserToGroup(user, groupName);
               }
            }
            results = new Response<String,List<String>>(groupName, users, checkUserLimit());
         } else if (GroupType.SHARING == groupType) {
            if (isSecurityOnlyGroup(groupName)) {
               results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
            } else if (isAdminOnlyGroup(groupName)) {
               results = new Response<String,List<String>>(ServerMessage.ADMIN_REQUIRED);
            } else {
               results = new Response<String,List<String>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
            }
         } else if (GroupType.SECURITY == groupType) {
            results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
         } else {
            results = new Response<String,List<String>>(ServerMessage.USER_NOT_AUTHORIZED);
         }
      } catch (Exception exception) {
         results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> removeUsersFromGroup(String groupName, List<String> users) {
      Response<String,List<String>> results = null;

      try {
         Users.validateUser();

         GroupType groupType = Users.getGroupType(groupName);

         if (((GroupType.SHARING == groupType) &&
              ((isAdminGroup(groupName) && CsiSecurityManager.isAdmin()) ||
               (isSecurityGroup(groupName) && CsiSecurityManager.isSecurity()))) ||
             ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
            if ((users != null) && !users.isEmpty()) {
               if (StringUtils.isNotEmpty(groupName)) {
                  if (groupName.equalsIgnoreCase(ADMINISTRATOR_GROUP)) {
                     String self = CsiSecurityManager.getUserName();
                     String formattedGroupName = Format.value(groupName);

                     for (String user : users) {
                        if (!user.equalsIgnoreCase(ADMINISTRATOR_USER) && !user.equalsIgnoreCase(SECURITY_USER) &&
                            !user.equalsIgnoreCase(self)) {
                           LOG.info("Remove username {} from group {}", () -> Format.value(user), () -> formattedGroupName);
                           Users.removeUserFromGroup(user, groupName);
                        }
                     }
                  } else {
                     for (String user : users) {
                        LOG.info("Remove username {} from group {}", () -> Format.value(user), () -> Format.value(groupName));
                        Users.removeUserFromGroup(user, groupName);
                     }
                  }
               }
            }
            results = new Response<String,List<String>>(groupName, users, checkUserLimit());
         } else if (GroupType.SHARING == groupType) {
            if (isSecurityOnlyGroup(groupName)) {
               results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
            } else if (isAdminOnlyGroup(groupName)) {
               results = new Response<String,List<String>>(ServerMessage.ADMIN_REQUIRED);
            } else {
               results = new Response<String,List<String>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
            }
         } else if (GroupType.SECURITY == groupType) {
            results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
         } else {
            results = new Response<String,List<String>>(ServerMessage.USER_NOT_AUTHORIZED);
         }
      } catch (Exception exception) {
         results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
      }
      return results;
   }

   @Operation
   public Response<String,List<String>> getGroupParents(String groupName) {
      Response<String,List<String>> results = null;
      GroupType groupType = Users.getGroupType(groupName);

      if (((GroupType.SHARING == groupType) &&
           ((isAdminGroup(groupName) && CsiSecurityManager.isAdmin()) ||
            (isSecurityGroup(groupName) && CsiSecurityManager.isSecurity()))) ||
          ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {

         List<String> parents = new ArrayList<String>();
         Group groupData = Users.getGroupByName(groupName);

         if (groupData != null) {
            // Add direct membership groups
            for (Group group : groupData.getGroups()) {
               parents.add(group.getName());
            }
         }
         results = new Response<String,List<String>>(parents, checkUserLimit());
      } else if (GroupType.SHARING == groupType) {
         if (isSecurityOnlyGroup(groupName)) {
            results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
         } else if (isAdminOnlyGroup(groupName)) {
            results = new Response<String,List<String>>(ServerMessage.ADMIN_REQUIRED);
         } else {
            results = new Response<String,List<String>>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
         }
      } else if (GroupType.SECURITY == groupType) {
         results = new Response<String,List<String>>(ServerMessage.CSO_REQUIRED);
      } else {
         results = new Response<String,List<String>>(ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,GroupDisplay> createGroup(GroupDisplay groupDisplay) {
      Response<String,GroupDisplay> results = null;

      if ((groupDisplay != null) && (groupDisplay.getName() != null)) {
         GroupType groupType = groupDisplay.getType();

         if (((GroupType.SHARING == groupType) && CsiSecurityManager.isAdmin()) ||
             ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
            String groupName = groupDisplay.getName();
            String remarks = groupDisplay.getRemarks();
            boolean external = groupDisplay.getExternal().booleanValue();

            try {
               Group group = null;

               Users.validateUser();
               LOG.info("Create group {}", () -> Format.value(groupName));

               if (GroupType.SHARING == groupType) {
                  group = Users.createSharingGroup(groupName, remarks);
               } else {
                  CapcoSection section = groupDisplay.getSection();
                  String portionText = groupDisplay.getPortionText();

                  group = Users.createSecurityGroup(groupName, remarks, section, portionText);
               }
               if (group != null) {
                  String newGroups = groupDisplay.getParentGroups();
                  String[] newGroupArray = (newGroups == null) ? null : GROUP_CONJUNCTION_PATTERN.split(newGroups);

                  group.setExternal(external);

                  if ((newGroupArray != null) && (newGroupArray.length > 0)) {
                     String formattedGroupDisplay = Format.value(group.getDisplay());

                     for (String parentName : newGroupArray) {
                        if (StringUtils.isNotEmpty(parentName)) {
                           LOG.info("Add group {} to group {}.", () -> formattedGroupDisplay, () -> Format.value(parentName));
                           Users.addGroupToGroup(groupName, parentName);
                        }
                     }
                  }
                  CsiPersistenceManager.commit();
                  CsiPersistenceManager.begin();
                  CsiPersistenceManager.refreshObject(group);

                  if (GroupType.SECURITY == groupType) {
                     if (((CapcoGroup) group).getPortion() != null) {
                        handleSecurityChange();
                     }
                  } else {
                     Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
                  }
                  results = new Response<String,GroupDisplay>(groupName, formatGroup(group), checkUserLimit());
               } else {
                  results = new Response<String,GroupDisplay>("<null>", ServerMessage.GROUP_CREATE_FAILED);
               }
            } catch (Exception exception) {
               results = new Response<String,GroupDisplay>(Format.value(groupDisplay.getName()),
                                                           ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         } else if (GroupType.SHARING == groupType) {
            results = new Response<String,GroupDisplay>(ServerMessage.ADMIN_REQUIRED);
         } else if (GroupType.SECURITY == groupType) {
            results = new Response<String,GroupDisplay>(ServerMessage.CSO_REQUIRED);
         } else {
            results = new Response<String,GroupDisplay>(ServerMessage.USER_NOT_AUTHORIZED);
         }
      } else {
         results = new Response<String,GroupDisplay>("<null>", ServerMessage.CORRUPT_GROUP);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,GroupDisplay> updateGroup(GroupDisplay groupDisplay) {
      Response<String,GroupDisplay> results = null;
      GroupType groupType = Users.getGroupType(groupDisplay.getName());
      String groupDisplayName = groupDisplay.getName();

      if (((GroupType.SHARING == groupType) &&
           ((isAdminGroup(groupDisplayName) && CsiSecurityManager.isAdmin()) ||
            (isSecurityGroup(groupDisplayName) && CsiSecurityManager.isSecurity()))) ||
          ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
         Map<String,Integer> newMap = new HashMap<String,Integer>();
         String groupName = groupDisplay.getName().toLowerCase();
         String remarks = groupDisplay.getRemarks();
         boolean external = groupDisplay.getExternal().booleanValue();
         Group group = null;
         boolean wasCapco = false;

         if (GroupType.SHARING == groupType) {
            group = Users.getSharingGroupByName(groupName);
         } else {
            group = Users.getSecurityGroupByName(groupName);
            wasCapco = (((CapcoGroup) group).getPortion() != null);
         }
         if (group != null) {
            List<Group> oldGroups = group.getGroups();
            String newGroups = groupDisplay.getParentGroups();
            String[] newGroupArray = (newGroups == null) ? null : GROUP_CONJUNCTION_PATTERN.split(newGroups);
            String formattedGroupDisplay = Format.value(group.getDisplay());

            try {
               Users.validateUser();
               LOG.info("Update group {}", () -> formattedGroupDisplay);

               if (GroupType.SHARING == groupType) {
                  Users.updateSharingGroup(groupName, remarks, external);
               } else {
                  CapcoSection section = groupDisplay.getSection();
                  String portionText = groupDisplay.getPortionText();

                  Users.updateSecurityGroup(groupName, remarks, external, section, portionText);
               }
               if ((newGroupArray != null) && (newGroupArray.length > 0)) {
                  for (String newGroup : newGroupArray) {
                     if (StringUtils.isNotEmpty(newGroup)) {
                        newMap.put(newGroup, Integer.valueOf(0));
                     }
                  }
               }
               if ((oldGroups != null) && !oldGroups.isEmpty()) {
                  for (Group oldGroup : oldGroups) {
                     String parentName = oldGroup.getName();

                     if (newMap.containsKey(parentName)) {
                        newMap.remove(parentName);
                     } else {
                        LOG.info("Remove group {} to group {}.",
                                 () -> formattedGroupDisplay, () -> Format.value(oldGroup.getDisplay()));
                        Users.removeGroupFromGroup(groupName, parentName);
                     }
                  }
               }
               if (!newMap.isEmpty()) {
                  for (String parentName : newMap.keySet()) {
                     LOG.info("Add group {} to group {}.",
                              () -> formattedGroupDisplay, () -> Format.value(parentName));
                     Users.addGroupToGroup(groupName, parentName);
                  }
               }
               CsiPersistenceManager.commit();
               CsiPersistenceManager.begin();
               CsiPersistenceManager.refreshObject(group);

               if (GroupType.SECURITY == groupType) {
                  if (wasCapco || (null != ((CapcoGroup) group).getPortion())) {
                     handleSecurityChange();
                  }
               } else {
                  Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
               }
               results = new Response<String,GroupDisplay>(groupName, formatGroup(group), checkUserLimit());
            } catch (Exception exception) {
               results = new Response<String,GroupDisplay>(Format.value(groupName), ServerMessage.CAUGHT_EXCEPTION,
                                                           Format.value(exception));
            }
         } else {
            results = new Response<String,GroupDisplay>("<null>", ServerMessage.CORRUPT_GROUP);
         }
      } else if (GroupType.SHARING == groupType) {
         if (isSecurityOnlyGroup(groupDisplayName)) {
            results = new Response<String,GroupDisplay>(ServerMessage.CSO_REQUIRED);
         } else if (isAdminOnlyGroup(groupDisplayName)) {
            results = new Response<String,GroupDisplay>(ServerMessage.ADMIN_REQUIRED);
         } else {
            results = new Response<String,GroupDisplay>(ServerMessage.ADMIN_OR_CSO_REQUIRED);
         }
      } else if (GroupType.SECURITY == groupType) {
         results = new Response<String,GroupDisplay>(ServerMessage.CSO_REQUIRED);
      } else {
         results = new Response<String,GroupDisplay>(ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> deleteGroups(GroupType groupType, List<String> groups) {
      Response<String,List<String>> results = null;

      if (((GroupType.SHARING == groupType) && CsiSecurityManager.isAdmin()) ||
          ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
         boolean capcoFlag = false;

         if ((groups != null) && !groups.isEmpty()) {
            try {
               Users.validateUser();

               for (String group : groups) {
                  if (!group.equalsIgnoreCase(ADMINISTRATOR_GROUP) && !group.equalsIgnoreCase(SECURITY_GROUP) &&
                      !group.equalsIgnoreCase(EVERYONE_GROUP) && !group.equalsIgnoreCase(ORIGINATOR_GROUP) &&
                      !group.equalsIgnoreCase(META_VIEWER_GROUP)) {
                     LOG.info("Delete group {}.", () -> Format.value(group));

                     if (Users.deleteGroupByName(group)) {
                        if (GroupType.SHARING == groupType) {
                           AclRequest.removeRoleFromAllACLs(group);
                        }
                        capcoFlag = true;
                     }
                  }
               }
            } catch (Exception exception) {
               results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
            }
         }
         if (results == null) {
            if (GroupType.SECURITY == groupType) {
               if (capcoFlag) {
                  handleSecurityChange();
               }
            } else {
               Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
            }
            results = new Response<String,List<String>>(Users.getAllRoleNames(), checkUserLimit());
         }
      } else {
         results = new Response<String,List<String>>(ServerMessage.USER_NOT_AUTHORIZED);
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> addGroupsToGroup(String groupName, List<String> groups) {
      Response<String,List<String>> results = null;

      try {
         Users.validateUser();

         GroupType groupType = Users.getGroupType(groupName);

         if (((GroupType.SHARING == groupType) &&
              ((isAdminGroup(groupName) && CsiSecurityManager.isAdmin()) ||
               (isSecurityGroup(groupName) && CsiSecurityManager.isSecurity()))) ||
             ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
            if ((groupName != null) && (groups != null) && !groups.isEmpty()) {
               for (String group : groups) {
                  LOG.info("Add group {} to group {}.", () -> Format.value(group), () -> Format.value(groupName));
                  Users.addGroupToGroup(group, groupName);
               }
            }
            if (GroupType.SHARING == groupType) {
               Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
            }
            results = new Response<String,List<String>>(groupName, groups, checkUserLimit());
         } else {
            results = new Response<String,List<String>>(groupName, ServerMessage.USER_NOT_AUTHORIZED);
         }
      } catch (Exception myException) {
         results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
      }
      return results;
   }

   @Override
   @Operation
   public Response<String,List<String>> removeGroupsFromGroup(String groupName, List<String> groups) {
      Response<String,List<String>> results = null;

      try {
         Users.validateUser();

         GroupType groupType = Users.getGroupType(groupName);

         if (((GroupType.SHARING == groupType) &&
              ((isAdminGroup(groupName) && CsiSecurityManager.isAdmin()) ||
               (isSecurityGroup(groupName) && CsiSecurityManager.isSecurity()))) ||
             ((GroupType.SECURITY == groupType) && CsiSecurityManager.isSecurity())) {
            if ((groupName != null) && (groups != null) && !groups.isEmpty()) {
               for (String group : groups) {
                  LOG.info("Remove group {} from group {}.", () -> Format.value(group), () -> Format.value(groupName));
                  Users.removeGroupFromGroup(group, groupName);
               }
            }
            if (GroupType.SHARING == groupType) {
               Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
            }
            results = new Response<String,List<String>>(groupName, groups, checkUserLimit());
         } else {
            results = new Response<String,List<String>>(groupName, ServerMessage.USER_NOT_AUTHORIZED);
         }
      } catch (Exception exception) {
         results = new Response<String,List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
      }
      return results;
   }

   @Override
   @Operation
   public void setPassword(String password) throws CentrifugeException {
      User user = Users.getUserByName(CsiSecurityManager.getUserName());

      if (!user.isDisabled()) {
         user.setPassword(Passwords.Digest(password, "SHA", "UTF-8"));
         Users.update(user);
      }
   }

    @Override
   @Operation
    public Response<String, List<String>> getAllUserNames() {

        try {

            return new Response<String, List<String>>(allUserNames());

        } catch(Exception myException) {

            return new Response<String, List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   @Operation
    public Response<String, List<String>> getActiveUserNames() {

        try {

            return new Response<String, List<String>>(activeUserNames());

        } catch(Exception myException) {

            return new Response<String, List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   @Operation
    public Response<String, List<String>> getActiveGroupNames() {

        try {

            return new Response<String, List<String>>(activeGroupNames());

        } catch(Exception myException) {

            return new Response<String, List<String>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   public Response<String, List<SharingDisplay>> share(String keyIn, List<String> resourceListIn,
                                                        SharingRequest sharingRequestIn) {

        return ModelHelper.defineSharing(keyIn, resourceListIn, sharingRequestIn);
    }

    @Override
   @Operation
    public Response<String, List<SharingDisplay>> getAclInfo(List<String> resourceListIn) {

        try {

            return new Response<String, List<SharingDisplay>>(DirectMetaDB.getAclInfoAvoidingSecurity(resourceListIn));

        } catch(Exception myException) {

            return new Response<String, List<SharingDisplay>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   @Operation
    public Response<String, List<SharingDisplay>> getSharingNames(AclResourceType resourceTypeIn,
                                                                  ResourceFilter filterIn, String patternIn,
                                                                  String ownerIn) {

        try {

            return new Response<String, List<SharingDisplay>>(AclRequest.getSharingNamesAvoidingSecurity(resourceTypeIn,
                                                                                        filterIn, patternIn, ownerIn));

        } catch(Exception myException) {

            return new Response<String, List<SharingDisplay>>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   @Operation
    public Response<String, SharingDisplay> getSingleSharingName(AclResourceType resourceTypeIn, String nameIn, String ownerIn) {

        try {

            return new Response<String, SharingDisplay>(AclRequest.getSingleSharingNameAvoidingSecurity(resourceTypeIn,
                    nameIn, ownerIn));

        } catch(Exception myException) {

            return new Response<String, SharingDisplay>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Override
   @Operation
    public Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>
    defineSharing(AclResourceType resourcetypeIn, List<String> resourcesIn, String roleIn,
                  List<AclControlType> permissionsIn, boolean setOwnerIn) {

        try {

            Users.validateUser();

            List<DataPair<String, String>> myChangeList = null;

            if ((null != resourcesIn) && !resourcesIn.isEmpty()) {

                if (CsiSecurityManager.isAdmin() || !setOwnerIn) {

                    if ((null != permissionsIn) && !permissionsIn.isEmpty()) {

                        AclRequest.setRolePermissions(resourcesIn, roleIn, permissionsIn);

                    } else {

                        AclRequest.removeRolePermissions(resourcesIn, roleIn);
                    }
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();

                    if (setOwnerIn) {

                        Map<String, Object> myNameMap = null;
                        List<String> myNameList = AclRequest.listUserResourceNames(resourcetypeIn, roleIn);

                        if ((null != myNameList) && !myNameList.isEmpty()) {

                            myNameMap = SynchronizeChanges.createResourceNameMap(myNameList);
                        }
                        myChangeList = AclRequest.setResourceOwner(resourcesIn, roleIn, myNameMap);
                    }
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();

                } else {

                    return new Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>
                            (ServerMessage.ADMIN_REQUIRED);
                }
            }
            return new Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>
                    (new DataPair<>(AclRequest.getSharingNamesAvoidingSecurity(resourcesIn), myChangeList));

        } catch(Exception myException) {

            return new Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>
                    (ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

   @Override
   public Response<Integer,List<StringEntry>> retrieveFilteredUserList(Integer keyIn, FilteredUserRequest filterIn) {
      List<StringEntry> results = new ArrayList<>();
      List<String> myList = filterIn.getRestricted() ? activeUserNames(filterIn) : allUserNames(filterIn);

      if ((myList != null) && !myList.isEmpty()) {
         int howMany = myList.size();

         for (int i = 0; i < howMany; i++) {
            results.add(new StringEntry(myList.get(i), DisplayMode.NORMAL, i));
         }
      }
      return new Response<Integer,List<StringEntry>>(keyIn, results);
   }

    @Override
    public Response<String, Integer> retrieveMaxConcurrencyInformation() {
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusDays(360);
        Integer maxConcurrency = 0;

        try {
            Map<Integer, YearEventPeriods> eventPeriodsMap = LoginEventService.fetchLoginEvents(start, end);
            handleFindMaxConcurrency(eventPeriodsMap, maxConcurrency);
        } catch (RepositoryException re) {
            LOG.error(re);
        }
        return new Response<>(new String("Max Concurrency"), maxConcurrency);
    }

    @Override
    public Response<String, List<EventsDisplay>> retrieveEvents() {
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusDays(30);
        List<EventsDisplay> eventsDisplays = new ArrayList<EventsDisplay>();

        try {
            Map<Integer, YearEventPeriods> eventPeriodsMap = LoginEventService.fetchLoginEvents(start, end);
            handleBuildEventDisplays(eventPeriodsMap, eventsDisplays);
        } catch (RepositoryException re) {
            LOG.error(re);
        }
        Collections.reverse(eventsDisplays);
        return new Response<>(new Date().toString(), eventsDisplays);
    }

    @Override
    public Response<String, List<ReportsDisplay>> retrieveReports() {
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusDays(30);
        List<ReportsDisplay> reportsDisplays = new ArrayList<ReportsDisplay>();

        try {
            Map<Integer, YearEventPeriods> eventPeriodsMap = LoginEventService.fetchLoginEvents(start, end);
            handleBuildReportDisplays(eventPeriodsMap, reportsDisplays);
        } catch (RepositoryException re) {
           LOG.error(re);
        }
        Collections.reverse(reportsDisplays);
        return  new Response<>(new Date().toString(), reportsDisplays);
    }

    private void handleBuildEventDisplays(Map<Integer, YearEventPeriods> eventPeriodsMap, List<EventsDisplay> eventsDisplays) {
        for(Map.Entry<Integer, YearEventPeriods> entry : eventPeriodsMap.entrySet()) {
           PeriodLoginEvents yearlyEvents = entry.getValue().getYearlyEvents();
           for(LoginEvent event : yearlyEvents.getPeriodEvents()) {
               EventsDisplay display = new EventsDisplay();
               display.setId(event.getId());
               display.setUserId(event.getUserName());
               display.setEvent(event.getReason().toString());
               display.setTimestamp(event.getEventDateTime().toString());

               eventsDisplays.add(display);
           }
        }
    }

    private void handleFindMaxConcurrency(Map<Integer, YearEventPeriods> eventPeriodsMap, Integer maxConcurrency) {
        for(Map.Entry<Integer, YearEventPeriods> entry : eventPeriodsMap.entrySet()) {
            YearEventPeriods yearEventPeriods = entry.getValue();
            PeriodLoginEvents events = yearEventPeriods.getYearlyEvents();
            for(LoginEvent loginEvent : events.getPeriodEvents()) {
                if(loginEvent.getActiveUsersAtEventTime() > maxConcurrency) {
                    maxConcurrency = loginEvent.getActiveUsersAtEventTime() + 1;
                }

            }
        }
    }

    private void handleBuildReportDisplays(Map<Integer, YearEventPeriods> eventPeriodsMap, List<ReportsDisplay> reportsDisplays) {
        List<EventReasons> successReasons = new ArrayList<EventReasons>();
        successReasons.add(EventReasons.LOGIN_SUCCESS);
        successReasons.add(EventReasons.LOGOUT_SUCCESS);

        for(Map.Entry<Integer, YearEventPeriods> entry : eventPeriodsMap.entrySet()) {
            YearEventPeriods yearEventPeriods = entry.getValue();
            Map<LocalDate, PeriodLoginEvents> dailyEvents = yearEventPeriods.getDailyEvents();
            for (LocalDate date : dailyEvents.keySet()) {
                List<String> loggedInNames = new ArrayList<String>();
                ReportsDisplay display = new ReportsDisplay();
                display.setId(new Random().nextLong());
                display.setDate(java.sql.Date.valueOf(date));
                display.setMaxLoginFailed(0);
                display.setActiveUsers(0);
                display.setConcurrentUsers(0);
                for (LoginEvent event : dailyEvents.get(date).getPeriodEvents()) {
                    if (!loggedInNames.contains(event.getUserName())) {
                        loggedInNames.add(event.getUserName());
                        display.setActiveUsers(display.getActiveUsers() + 1);
                    }
                    if (event.getActiveUsersAtEventTime() > display.getConcurrentUsers()) {
                        display.setConcurrentUsers(event.getActiveUsersAtEventTime());
                    }
                    if (!successReasons.contains(event.getReason())) {
                        display.setMaxLoginFailed(display.getMaxLoginFailed() + 1);
                    }
                }
                if(dailyEvents.get(date).getPeriodEvents().size() != 0) {
                    reportsDisplays.add(display);
                }
            }
        }

    }

    @Override
   public Response<String, SharingContext> retrieveSharingContext(String keyIn, List<String> resourceIdListIn) {

        List<String> myUserList = activeUserNames();
        List<String> myGroupList = activeGroupNames();
        List<AccessControlEntry> myAclEntries = AclRequest.getResourceUsersAvoidingSecurity(resourceIdListIn);

        return new Response<String, SharingContext>(keyIn, new SharingContext(myUserList, myGroupList,
                                                                                myAclEntries, resourceIdListIn.size()));
    }

    private GroupDisplay formatGroup(Group group) {
        GroupDisplay myItem = null;

        if (group != null) {

            myItem = new GroupDisplay();
            myItem.setId(group.getId());
            myItem.setType(group.getType());
            myItem.setName(group.getDisplay());
            myItem.setRemarks(group.getRemark());
            myItem.setExternal(Boolean.valueOf(group.getExternal()));

            if (group instanceof CapcoGroup) {

                myItem.setSection(((CapcoGroup) group).getSection());
                myItem.setPortionText(((CapcoGroup) group).getPortion());
            }

            List<Group> myParentGroups = group.getGroups();
            if ((null != myParentGroups) && !myParentGroups.isEmpty()) {
                StringBuilder myGroupBuffer = new StringBuilder();
                for (Group myParentGroup : myParentGroups) {
                    myGroupBuffer.append(GROUP_CONJUNCTION + myParentGroup.getDisplay());
                }
                myItem.setParentGroups(myGroupBuffer.toString().substring(2));
            }
            else {
                myItem.setParentGroups("");
            }

            myItem.setMemberGroups("");
        }

        return myItem;
    }

    private List<GroupDisplay> formatGroups(List<Group> groupListIn) throws CentrifugeException {

        List<GroupDisplay> myGroupList = new ArrayList<GroupDisplay>();

        if (groupListIn != null) {

            // Add direct membership groups to each user while selecting fields to display
            for (Group myGroup : groupListIn) {
                myGroupList.add(formatGroup(myGroup));
            }
        }

        return myGroupList;
    }

    private void cancelGroupMemberships(User oldUserIn, UserDisplay userIn) {

        List<Group> myOldGroupList = oldUserIn.getGroups();

        if ((null != myOldGroupList) && !myOldGroupList.isEmpty()) {

            String myGroups = userIn.getGroups();
            String myUsername = userIn.getName();
            boolean myCheckForAdmin = (myUsername.equalsIgnoreCase(ADMINISTRATOR_USER)
                                        || myUsername.equalsIgnoreCase(SECURITY_USER)
                                        || myUsername.equalsIgnoreCase(CsiSecurityManager.getUserName()));

            if ((null != myGroups) && (0 < myGroups.length())) {

                Map<String, Integer> myGroupMap = new HashMap<String, Integer>();
                String[] myGroupNames = GROUP_CONJUNCTION_PATTERN.split(myGroups);

                for (String myGroupName : myGroupNames) {

                    if (0 < myGroupName.length()) {

                        myGroupMap.put(myGroupName, 0);
                    }
                }
                myGroupMap.put(EVERYONE_GROUP, 0);

                for (Group myGroup : myOldGroupList) {

                    if (GroupType.SHARING == myGroup.getType()) {

                        String myParentName = myGroup.getName();

                        if ((!myCheckForAdmin || !myParentName.equalsIgnoreCase(ADMINISTRATOR_GROUP))) {

                            if (! myGroupMap.containsKey(myParentName)) {

                                LOG.info("Remove username {} from group {}.",
                                         () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroup.getDisplay()));
                                Users.removeUserFromGroup(myUsername, myParentName);
                            }
                        }
                    }
                }
            }
            else {

                for (Group myGroup : myOldGroupList) {

                    if (GroupType.SHARING == myGroup.getType()) {

                        String myParentName = myGroup.getName();

                        if ((!myParentName.equals(EVERYONE_GROUP))
                            &&((!myCheckForAdmin || !myParentName.equalsIgnoreCase(ADMINISTRATOR_GROUP)))) {

                            LOG.info("Remove username {} from group {}.",
                                     () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroup.getDisplay()));
                            Users.removeUserFromGroup(myUsername, myParentName);
                        }
                    }
                }
            }
        }
    }

    private void cancelClearances(User oldUserIn, UserDisplay userIn) {

        List<Group> myOldGroupList = oldUserIn.getGroups();

        if ((null != myOldGroupList) && !myOldGroupList.isEmpty()) {

            String myGroups = userIn.getGroups();
            String myUsername = userIn.getName();

            if ((null != myGroups) && (0 < myGroups.length())) {

                Map<String, Integer> myGroupMap = new HashMap<String, Integer>();
                String[] myGroupNames = GROUP_CONJUNCTION_PATTERN.split(myGroups);

                for (String myGroupName : myGroupNames) {

                    if (0 < myGroupName.length()) {

                        myGroupMap.put(myGroupName, 0);
                    }
                }

                for (Group myGroup : myOldGroupList) {

                   if (GroupType.SECURITY == myGroup.getType()) {

                       String myParentName = myGroup.getName();

                       if (! myGroupMap.containsKey(myParentName)) {

                           LOG.info("Remove username {} from group {}.",
                                    () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroup.getDisplay()));
                           Users.removeUserFromGroup(myUsername, myParentName);
                       }
                   }
                }
            }
            else {

                for (Group myGroup : myOldGroupList) {

                    if (GroupType.SECURITY == myGroup.getType()) {

                        String myParentName = myGroup.getName();

                        LOG.info("Remove username {} from group {}.",
                                 () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroup.getDisplay()));
                        Users.removeUserFromGroup(myUsername, myParentName);
                    }
                }
            }
        }
    }

   private void addGroupMemberships(String usernameIn, String groupsIn, User oldUserIn) {
      String[] myGroupNames = (groupsIn == null) ? new String[0] : GROUP_CONJUNCTION_PATTERN.split(groupsIn);
      List<Group> myOldGroupList = oldUserIn.getGroups();
      Map<String, Integer> myGroupMap = new HashMap<String, Integer>();

      LOG.info("Add to {} username {} groups: {}.",
               () -> (oldUserIn.isDisabled() ? "disabled " : ""), () -> Format.value(usernameIn), () -> groupsIn);

        if ((null != myOldGroupList) && !myOldGroupList.isEmpty()) {

            for (Group myGroup : myOldGroupList) {

                myGroupMap.put(myGroup.getName(), Integer.valueOf(0));
            }

            for (String myGroupName : myGroupNames) {

                if (0 < myGroupName.length()) {

                    if (! myGroupMap.containsKey(myGroupName)) {

                        myGroupMap.put(myGroupName, Integer.valueOf(0));
                        LOG.info("Add username {} to group {}.",
                                 () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroupName));
                        Users.addUserToGroup(usernameIn, myGroupName);
                    }
                }
            }
        }
        else {

            for (String myGroupName : myGroupNames) {

                if (0 < myGroupName.length()) {

                    myGroupMap.put(myGroupName, Integer.valueOf(0));
                    LOG.info("Add username {} to group {}.",
                             () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(myGroupName));
                    Users.addUserToGroup(usernameIn, myGroupName);
                }
            }
        }
        if (! myGroupMap.containsKey(EVERYONE_GROUP)) {

            LOG.info("Add username {} to group {}.",
                     () -> Format.value(oldUserIn.getDisplay()), () -> Format.value(EVERYONE_GROUP));
            Users.addUserToGroup(usernameIn, EVERYONE_GROUP);
        }
    }

    private static User extractUserData(User oldUserIn, UserDisplay newUserIn) {

        User myUser = oldUserIn;

        if (null != newUserIn) {

            String myPassword = newUserIn.getPassword();

            if (null == myUser) {

                myUser = new User();
//                myUser.setId(null);
            }

            myUser.setName(newUserIn.getName().toLowerCase());
            myUser.setDisplay(newUserIn.getName());
            myUser.setFirstName(newUserIn.getFirstName());
            myUser.setLastName(newUserIn.getLastName());
            myUser.setEmail(newUserIn.getEmail());
            myUser.setRemark(newUserIn.getRemarks());
            myUser.setDisabled(newUserIn.getDisabled());
            myUser.setSuspended(newUserIn.getSuspended());
            myUser.setPerpetual(newUserIn.getPerpetual());
            myUser.setExpirationDate(newUserIn.getExpirationDate());

            if ((null != myPassword) && (0 < myPassword.length())) {

                myUser.setPassword(Passwords.Digest(myPassword, "SHA", "UTF-8"));
            }

        }
        return myUser;
    }

    private List<UserDisplay> createUserDisplay(List<User> userDataIn) {

        List<UserDisplay> myUserList = new ArrayList<UserDisplay>();

        if (userDataIn != null) {

            // Add direct membership groups to each user while selecting fields to display
            for (User myUser : userDataIn) {

                myUserList.add(createUserDisplay(myUser));
            }
        }

        return myUserList;
    }

   private UserDisplay createUserDisplay(User user) {
      UserDisplay userDisplay = null;

      if (user != null) {
         userDisplay = new UserDisplay();

         userDisplay.setId(user.getId());
         userDisplay.setName(user.getDisplay());
         userDisplay.setFirstName(user.getFirstName());
         userDisplay.setLastName(user.getLastName());
         userDisplay.setEmail(user.getEmail());
         userDisplay.setRemarks(user.getRemark());
         userDisplay.setLastLogin(user.getLastLogin());
         userDisplay.setCreationDate(user.getCreationDate());
         userDisplay.setExpirationDate(user.getExpirationDate());
         userDisplay.setPerpetual(user.isPerpetual());
         userDisplay.setDisabled(Boolean.valueOf(user.isDisabled()));
         userDisplay.setSuspended(user.isSuspended());

         List<Group> groups = user.getGroups();

         if (groups == null) {
            userDisplay.setGroups("");
            userDisplay.setClearance("");
         } else {
            for (Group group : groups) {
               if (GroupType.SHARING == group.getType()) {
                  userDisplay.setGroups(groups.stream().map(g -> g.getDisplay()).collect(Collectors.joining(GROUP_CONJUNCTION)));
               } else if (GroupType.SECURITY == group.getType()) {
                  userDisplay.setClearance(groups.stream().map(g -> g.getDisplay()).collect(Collectors.joining(", ")));
               }
            }
         }
      }
      return userDisplay;
   }

   private static long checkUserLimit() {
      return Product.getLicense().availableLicenses((int) Users.getUserCount(true));
   }

   private static void handleSecurityChange() {
      Configuration.getInstance().getSecurityPolicyConfig().resetGroupMaps();
      CapcoRollup.refreshRoleMap();
   }

    private Map<String, String> getBlockedGroups() {

        Map<String, String> myMap = null;

        if ((!isSpecialUser()) && getSecurityInfo().getRestrictRoleVisibility().booleanValue()) {

            if (getSecurityInfo().isHiddenFromGroups()) {

                myMap = getSecurityInfo().getHideFromGroupMap();
            }
        }
        return myMap;
    }

    private Map<String, String> getRestrictedGroups(Map<String, String> blockedGroupsIn) {

        Map<String, String> myMap = new HashMap<String, String>();
        Collection<String> myRoles = ((!isSpecialUser()) && getSecurityInfo().getRestrictRoleVisibility().booleanValue())
                ? getSecurityInfo().getSharingGroups()
                : Users.getActiveGroupNames();
        String myEveryoneRole = EVERYONE_GROUP.toLowerCase();

        if (getSecurityInfo().isRestrictedToGroups()) {

            if ((null != blockedGroupsIn) && !blockedGroupsIn.isEmpty()) {

                for (String myRole : myRoles) {

                    if ((!blockedGroupsIn.containsKey(myRole))
                            && getSecurityInfo().isRestrictedTo(myRole)) {

                        myMap.put(myRole, myRole);
                    }
                }

            } else {

                for (String myRole : myRoles) {

                    if (getSecurityInfo().isRestrictedTo(myRole)) {

                        myMap.put(myRole, myRole);
                    }
                }
            }

        } else {

            if ((null != blockedGroupsIn) && !blockedGroupsIn.isEmpty()) {

                for (String myRole : myRoles) {

                    if ((!myEveryoneRole.equals(myRole) && !blockedGroupsIn.containsKey(myRole))) {

                        myMap.put(myRole, myRole);
                    }
                }

            } else {

                for (String myRole : myRoles) {

                    if (!myEveryoneRole.equals(myRole)) {

                        myMap.put(myRole, myRole);
                    }
                }
            }
        }
        return myMap;
    }

    private boolean isSpecialUser() {

        return (ADMINISTRATOR_USER.equals(CsiSecurityManager.getUserName())
                || SECURITY_USER.equals(CsiSecurityManager.getUserName())
                /*
                || ((!getSecurityInfo().getRestrictAdminVisibility) && ADMINISTRATOR_GROUP.equals(CsiSecurityManager.getUserName()))
                || ((!getSecurityInfo().getRestrictCsoVisibility) && SECURITY_GROUP.equals(CsiSecurityManager.getUserName()))
                */
                );
    }

    private List<String> activeGroupNames() {

        Map<String, String> myRestrictedGroupMap = getRestrictedGroups(getBlockedGroups());
        Collection<String> myRestrictedGroups = (null != myRestrictedGroupMap) ? myRestrictedGroupMap.values() : null;

        return Users.getGroupNames(myRestrictedGroups);
    }

    private List<String> activeUserNames() {

        return activeUserNames(null);
    }

    private List<String> allUserNames() {

        return activeUserNames(null);
    }

    private List<String> activeUserNames(FilteredUserRequest filterIn) {

        boolean myRestrictionFlag = ((!isSpecialUser()) && getSecurityInfo().getRestrictRoleVisibility().booleanValue());
        Map<String, String> myBlockedGroupMap = myRestrictionFlag ? getBlockedGroups() : null;
        Map<String, String> myRestrictedGroupMap = myRestrictionFlag ? getRestrictedGroups(myBlockedGroupMap) : null;
        Collection<String> myBlockedGroups = (null != myBlockedGroupMap) ? myBlockedGroupMap.values() : null;
        Collection<String> myRestrictedGroups = (null != myRestrictedGroupMap) ? myRestrictedGroupMap.values() : null;

        return Users.getFilteredActiveUserNames(myBlockedGroups, myRestrictedGroups, filterIn);
    }

    private List<String> allUserNames(FilteredUserRequest filterIn) {

        boolean myRestrictionFlag = ((!isSpecialUser()) && getSecurityInfo().getRestrictRoleVisibility().booleanValue());
        Map<String, String> myBlockedGroupMap = myRestrictionFlag ? getBlockedGroups() : null;
        Map<String, String> myRestrictedGroupMap = myRestrictionFlag ? getRestrictedGroups(myBlockedGroupMap) : null;
        Collection<String> myBlockedGroups = (null != myBlockedGroupMap) ? myBlockedGroupMap.values() : null;
        Collection<String> myRestrictedGroups = (null != myRestrictedGroupMap) ? myRestrictedGroupMap.values() : null;

        return Users.getFilteredUserNames(myBlockedGroups, myRestrictedGroups, filterIn);
    }
}
