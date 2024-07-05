package csi.security.queries;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.license.model.AbstractLicense;
import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.server.common.dto.system.FilteredUserRequest;
import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.GroupType;
import csi.server.common.enumerations.UserSortMode;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.CapcoGroup;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.dao.DataAccessException;
import csi.server.util.SqlUtil;
import csi.startup.Product;

class NamePair {
    private String _display = null;
    private String _name = null;
    private boolean _valid = false;

    NamePair(String displayIn, String nameIn) {

        _name = (null != nameIn) ? nameIn.trim() : null;
        _display = (null != displayIn) ? displayIn.trim() : null;

        if ((null != _name) && (0 < _name.length())) {

            if ((null == _display) || (0 == _display.length())) {

                _display = _name;
                _name = _name.toLowerCase();
                _valid = true;

            } else {

                _name = _name.toLowerCase();
                _valid = _name.equalsIgnoreCase(_display);
            }

        } else if ((null != _display) && (0 < _display.length())) {

            _name = _display.toLowerCase();
            _valid = true;
        }
    }

    boolean isValid() {

        return _valid;
    }

    String getDisplay() {

        return _display;
    }

    String getName() {

        return _name;
    }
}

public class Users {
   private static final Logger LOG = LogManager.getLogger(Users.class);

   private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1L);

   private static final Set<String> ADMIN_ROLES = new HashSet<String>(Arrays.asList(JAASRole.ADMIN_USER_NAME, JAASRole.SECURITY_USER_NAME));
   private static final String RESTRICTED_USER_STRING =
      ADMIN_ROLES.stream().map(SqlUtil::singleQuoteWithEscape).collect(Collectors.joining(",", "(", ")"));

   private static final Set<String> PERMANENT_GROUP = new HashSet<String>(Arrays.asList(JAASRole.ADMIN_ROLE_NAME, JAASRole.SECURITY_ROLE_NAME, JAASRole.EVERYONE_ROLE_NAME));
   private static final String PERMANENT_GROUP_STRING =
      PERMANENT_GROUP.stream().map(SqlUtil::singleQuoteWithEscape).collect(Collectors.joining(",", "(", ")"));

    private static final List<String> _securityGroups = Arrays.asList(JAASRole.SECURITY_ROLE_NAME);
    private static final List<String> _adminGroups = Arrays.asList(JAASRole.ADMIN_ROLE_NAME);
    private static final List<String> _specialGroups = Arrays.asList(JAASRole.ADMIN_ROLE_NAME, JAASRole.SECURITY_ROLE_NAME);

    private static final String USER_EXCLUSION = " (u.name NOT IN " + RESTRICTED_USER_STRING + ")";
    private static final String _simpleUserBeginning = "SELECT u FROM User u WHERE" + USER_EXCLUSION;
    private static final String _groupListGroupBeginning = "SELECT DISTINCT g FROM Group g WHERE (g.name IN (:s2))";
    private static final String _groupListUserBeginning = "SELECT DISTINCT u FROM User u JOIN u.groups g WHERE (g.name IN (:s2)) AND" + USER_EXCLUSION;
    private static final String _singleGroupUserBeginning = "SELECT u FROM User u JOIN u.groups g WHERE (g.name = :s2) AND" + USER_EXCLUSION;
    private static final String _doubleGroupUserBeginning = "SELECT u FROM User u JOIN u.groups g1 JOIN u.groups g2"
                                                            + " WHERE (g1.name = :s2) AND (g2.name = :s3) AND" + USER_EXCLUSION;
    private static final String _groupSearchMatchingRequest = " AND (g.name LIKE lower(:s1) ESCAPE '\')";
    private static final String _userSearchMatchingRequest = " AND ((u.name LIKE lower(:s1) ESCAPE '\') OR"
                                                            + " (u.firstName LIKE :s1 ESCAPE '\') OR"
                                                            + " (u.lastName LIKE :s1 ESCAPE '\'))";
    private static final String _simpleGroupSearchEnding = " ORDER BY g.name";
    private static final String _simpleUserSearchEnding = " ORDER BY u.name";
    private static final String[] userConditions = new String[] {

            " (u.perpetual = true)",
            " ((u.perpetual = false) AND (u.disabled = false) AND (u.suspended = false))",
            " (u.disabled = true)",
            " (u.suspended = true)",
    };

   private static Long nextExpirationCheck = Long.valueOf(0L);
   private static Long userCount = null;

    public static void enforceExpirationPolicy() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();
            boolean myExpireUsersFlag = policyConfig.getEnableUserAccountExpiration();
            boolean myExpireUsersAfterTime = (myExpireUsersFlag && policyConfig.isExpireUsersByDefault());
            boolean myExpireIdleUsers = (myExpireUsersFlag && policyConfig.getExpireIdleUsers());
            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

            if (myExpireUsersAfterTime || myExpireIdleUsers) {
                Long myCurrentDateTime = System.currentTimeMillis();
                Long myLastMidnight = myCurrentDateTime - (myCurrentDateTime % DAY_IN_MILLIS);
                String updateRequest = "update User u set u.suspended=true where ((u.disabled = false)"
                                        + " AND (u.suspended=false) AND (u.perpetual = false) AND ";

                if (myExpireIdleUsers) {

                    String myIdleLimit = ((Integer) policyConfig.getIdleDaysUntilExpiration()).toString();

                    if (myExpireUsersAfterTime) {

                        updateRequest += "(((u.expirationDate is not null) AND (u.expirationDate < CURRENT_TIMESTAMP()))"
                                        + " OR (((day(current_date()) - day(u.activateDate)) > " + myIdleLimit
                                        +") AND ((u.lastLogin is null) OR ((day(current_date()) - day(u.lastLogin)) > "
                                        + myIdleLimit + "))))";

                    } else {

                        updateRequest += "((day(current_date()) - day(u.activateDate)) > " + myIdleLimit +
                                        ") AND ((u.lastLogin is null) OR ((day(current_date()) - day(u.lastLogin)) > "
                                        + myIdleLimit + ")))";
                    }

                } else /* therefore myExpireUsersAfterTime is true */ {

                    updateRequest += "((u.expirationDate is not null) AND (u.expirationDate < CURRENT_TIMESTAMP())))";
                }
                Query query = manager.createQuery(updateRequest);
                int updateCount = query.executeUpdate();
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
                nextExpirationCheck = myLastMidnight + DAY_IN_MILLIS;
                if (LOG.isInfoEnabled()) {
                    LOG.info("User account expiration updated, {} user accounts are now suspended.", () -> Integer.valueOf(updateCount));
                }
            }

        } catch(Exception myException) {

            LOG.error("Caught exception enforcing Expiration Policy", myException);
            CsiPersistenceManager.rollback();

        } finally {

            userCount = null;
            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void recordLogon(String usernameIn) {

        if (null != usernameIn) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                String myUser = usernameIn.trim().toLowerCase();
                EntityManager myEntityManager = CsiPersistenceManager.getMetaEntityManager();

                String myUpdateRequest = "update User u set u.lastLogin = CURRENT_TIMESTAMP() where u.name = '" + myUser + "'";
                Query query = myEntityManager.createQuery(myUpdateRequest);

                CsiPersistenceManager.rollback();
                CsiPersistenceManager.begin();
                query.executeUpdate();
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();

            } catch(Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception recording logon for user " + Format.value(usernameIn), myException);

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
    }

   public static String validateUser() throws CentrifugeException {
      return validateUser(true);
   }

   public static String validateUser(boolean enforceUserLimit) throws CentrifugeException {
      String user = CsiSecurityManager.getUserName().trim().toLowerCase();

      if (enforceUserLimit) {
         AbstractLicense license = Product.getLicense();
         int persistentUserCount = (int) Users.getUserCount();

         if (license.persistedUsersWithinLimit(persistentUserCount)) {
            SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();

            if (policyConfig.getAutoSuspendWhenOverLimit().booleanValue()) {
               reduceActiveUserCount(persistentUserCount - license.getUserCount());
            }
         }
         if (!license.persistedUsersWithinLimit((int) Users.getUserCount())) {
            throw new CentrifugeException("Number of active users exceeds authorized limit!");
         }
      }
      EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
      String jpaQuery = "select count(u) from User u where u.name = '" + user + "' and u.disabled=false and u.suspended=false";
      Query query = manager.createQuery(jpaQuery);
      Long count = (Long) query.getSingleResult();

      if ((count == null) || (count.longValue() == 0L)) {
         throw new CentrifugeException("Username not found in database, or has been disabled or suspended!");
      }
      return user;
   }

    public static User add(User userIn) throws CentrifugeException {

        if (null != userIn) {

            NamePair myNamePair = new NamePair(userIn.getDisplay(), userIn.getName());

            if (myNamePair.isValid()) {

                userIn.setName(myNamePair.getName());
                userIn.setDisplay(myNamePair.getDisplay());

                guaranteeExpirationInformation(userIn);

                if (null != getUserByName(myNamePair)) {
                    CentrifugeException ce = new CentrifugeException(String.format("Error occurred adding user '%s'; user already exists", userIn.getDisplay()));
                    LOG.info(ce.getMessage());
                    ce.setLogged(true);
                    throw ce;
                }

                boolean myActiveOnEntry = CsiPersistenceManager.isActive();

                try {

                    EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                    myManager.persist(userIn);

                    addUserToGroup(myNamePair.getName(), JAASRole.EVERYONE_GROUP_NAME);
                    myManager.merge(userIn);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();

                    if (userIn.isDisabled()) {

                        LOG.info( "Added disabled user: {}", () -> Format.value(userIn.getName()));

                    } else {

                        LOG.info( "Added user: {}", () -> Format.value(userIn.getName()));
                    }

                    return userIn;

                } catch(Exception myException) {

                    CsiPersistenceManager.rollback();
                    LOG.error("Caught exception adding user " + Format.value(userIn.getDisplay()), myException);
                    throw new CentrifugeException("Caught exception adding user " + Format.value(userIn.getDisplay()), myException);

                } finally {

                    userCount = null;
                    if (!myActiveOnEntry) {

                        CsiPersistenceManager.close();
                    }
                }

            } else {

                throw new CentrifugeException("Corrupted user information encountered.");
            }

        } else {

            throw new CentrifugeException("Corrupted user information encountered.");
        }
    }

   public static User update(User userIn) throws CentrifugeException {
      User result = null;

      if (userIn != null) {
         boolean myActiveOnEntry = CsiPersistenceManager.isActive();
         User existingUser = Users.getUserByName(userIn.getName());

         if (existingUser != null) {
            String formattedUserInName = Format.value(userIn.getName());
            boolean wasDisabled = existingUser.isDisabled();
            boolean wasSuspended = existingUser.isSuspended();

            existingUser.updateFrom(userIn);
            guaranteeExpirationInformation(existingUser);

            try {
               EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

               myManager.merge(existingUser);
               CsiPersistenceManager.commit();
               CsiPersistenceManager.begin();

               if (userIn.isDisabled() && !wasDisabled) {
                  LOG.info("Disabled user: {}", () -> formattedUserInName);
               } else if (!userIn.isDisabled() && wasDisabled) {
                  LOG.info("Enabled user: {}", () -> formattedUserInName);
               } else {
                  LOG.info("Updated information for user: {}", () -> formattedUserInName);
               }

                if (userIn.isSuspended() && !wasSuspended) {
                    LOG.info("Suspended user: {}", () -> formattedUserInName);
                } else if (!userIn.isSuspended() && wasSuspended) {
                    LOG.info("Enabled user: {}", () -> formattedUserInName);
                } else {
                    LOG.info("Updated information for user: {}", () -> formattedUserInName);
                }
               result = existingUser;
            } catch(Exception myException) {
               CsiPersistenceManager.rollback();
               LOG.error("Caught exception updating user " + Format.value(userIn.getDisplay()), myException);
               throw new CentrifugeException("Caught exception updating user " + Format.value(userIn.getDisplay()), myException);
            } finally {
               userCount = null;

               if (!myActiveOnEntry) {
                  CsiPersistenceManager.close();
               }
            }
         } else {
            throw new CentrifugeException("Could not find user \"" + userIn.getDisplay() + "\" to update!");
         }
      } else {
         throw new CentrifugeException("Corrupted user information encountered.");
      }
      return result;
   }

    public static void deleteByName(String name) throws CentrifugeException {
        User user = getUserByName(name);
        if (user != null) {
            delete(user);
        }

    }

    public static void delete(User user) throws CentrifugeException {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();

            /*
             * NB: If a user is deleted immediately after creation, then Hibernate will not have had a chance to create
             * an empty "persistent bag" of groups, so we need to check for null.
             */
            List<Group> groups = user.getGroups();
            if (groups != null) {
                for (Group g : user.getGroups()) {
                    g.removeMember(user);
                    manager.merge(g);
                }
            }

            manager.remove(user);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception removing user " + Format.value(user.getDisplay()), myException);
            throw new CentrifugeException("Caught exception removing user "
                    + Format.value(user.getDisplay()), myException);

        } finally {

            userCount = null;
            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return;
    }

    public static void deactivateByName(String userNameIn) throws CentrifugeException {
        User myUser = getUserByName(userNameIn);
        if (myUser != null) {
            deactivate(myUser);
        }
    }

    public static void activateByName(String userNameIn) throws CentrifugeException {
        User myUser = getUserByName(userNameIn);
        if (myUser != null) {
            activate(myUser);
        }
    }

    public static void deactivate(User userIn) throws CentrifugeException {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            userIn.setDisabled(true);
            myManager.merge(userIn);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception deactivating user " + Format.value(userIn.getDisplay()), myException);

        } finally {

            userCount = null;
            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void suspend(User userIn) throws CentrifugeException {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            userIn.setSuspended(true);
            myManager.merge(userIn);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception deactivating user " + Format.value(userIn.getDisplay()), myException);

        } finally {

            userCount = null;
            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

   public static void activate(User userIn) throws CentrifugeException {
      boolean myActiveOnEntry = CsiPersistenceManager.isActive();

      try {
         EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

         userIn.setSuspended(Boolean.FALSE);
         userIn.setDisabled(Boolean.FALSE);
         userIn.setSuspended(Boolean.FALSE);
         userIn.setActivateDateTime(LocalDateTime.now());
         myManager.merge(userIn);
         CsiPersistenceManager.commit();
         CsiPersistenceManager.begin();
      } catch (Exception myException) {
         CsiPersistenceManager.rollback();
         LOG.error("Caught exception activating user " + Format.value(userIn.getDisplay()), myException);
      } finally {
         userCount = null;

         if (!myActiveOnEntry) {
            CsiPersistenceManager.close();
         }
      }
   }

    public static Group createSharingGroup(String groupNameIn, String remarksIn) throws CentrifugeException {

        Group myGroup = null;
        NamePair myNamePair = new NamePair(groupNameIn, null);

        if (myNamePair.isValid()) {

            if (groupExists(myNamePair.getName())) {
                CentrifugeException myException = new CentrifugeException(String.format("Error occurred adding group '%s'; group already exists", groupNameIn));
                LOG.info(myException.getMessage());
                myException.setLogged(true);
                throw myException;
            }

            myGroup = new Group();

            myGroup.setDisplay(myNamePair.getDisplay());
            myGroup.setType(GroupType.SHARING);
            myGroup.setName(myNamePair.getName());
            myGroup.setRemark(remarksIn);

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
                manager.persist(myGroup);
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();

                return myGroup;

            } catch(Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception creating sharing group " + Format.value(groupNameIn), myException);

                return null;

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
        return myGroup;
    }

    public static CapcoGroup createSecurityGroup(String groupNameIn, String remarksIn,
                                                 CapcoSection sectionIn, String portionTextIn)
            throws CentrifugeException {

        CapcoGroup myGroup = null;
        NamePair myNamePair = new NamePair(groupNameIn, null);

        if (myNamePair.isValid()) {

            if (groupExists(myNamePair.getName())) {
                CentrifugeException myException = new CentrifugeException(String.format("Error occurred adding group '%s'; group already exists", groupNameIn));
                LOG.info(myException.getMessage());
                myException.setLogged(true);
                throw myException;
            }

            myGroup = new CapcoGroup();

            myGroup.setDisplay(myNamePair.getDisplay());
            myGroup.setType(GroupType.SECURITY);
            myGroup.setName(myNamePair.getName());
            myGroup.setRemark(remarksIn);
            myGroup.setSection(sectionIn);
            myGroup.setPortion(portionTextIn);
            myGroup.setReject(false);

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
                manager.persist(myGroup);
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();

                return myGroup;

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception creating security role " + Format.value(groupNameIn), myException);

                return null;

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
        return myGroup;
    }

    public static boolean deleteGroupByName(String nameIn) throws DataAccessException {

        boolean myCapcoFlag = false;
        Group myGroup = getGroupByName(nameIn);

        if (null != myGroup) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                EntityManager myEntityManager = CsiPersistenceManager.getMetaEntityManager();

                if (PERMANENT_GROUP.contains(myGroup.getName())) {
                    throw new DataAccessException(String.format("Cannot delete group %s", myGroup.getName()));
                }
                for (Group myParentGroup : myGroup.getGroups()) {
                    myParentGroup.removeMember(myGroup);
                    myEntityManager.merge(myParentGroup);
                }
                myEntityManager.remove(myGroup);

                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();

                if (myGroup instanceof CapcoGroup) {

                    myCapcoFlag = (null != ((CapcoGroup)myGroup).getPortion());
                }

            } catch(Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception removing group " + Format.value(nameIn), myException);

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
        return myCapcoFlag;
    }

    public static Group updateSharingGroup(String groupNameIn, String remarksIn, boolean externalIn)
            throws CentrifugeException {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            Group myGroup = getSharingGroupByName(groupNameIn);

            if (myGroup != null) {
               myGroup.setRemark(remarksIn);
               myGroup.setExternal(externalIn);
               myManager.merge(myGroup);

               CsiPersistenceManager.commit();
               CsiPersistenceManager.begin();
            }
            return myGroup;

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception updating sharing group " + Format.value(groupNameIn), myException);

            return null;

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

   public static Group updateSecurityGroup(String groupNameIn, String remarksIn, boolean externalIn,
                                           CapcoSection sectionIn, String portionTextIn)
         throws CentrifugeException {
      Group result  = null;
      boolean activeOnEntry = CsiPersistenceManager.isActive();

      try {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         CapcoGroup group = getSecurityGroupByName(groupNameIn);

         if (group != null) {
            group.setRemark(remarksIn);
            group.setExternal(externalIn);
            group.setSection(sectionIn);
            group.setPortion(portionTextIn);
            manager.merge(group);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
         }
         result = group;
      } catch (Exception exception) {
         CsiPersistenceManager.rollback();
         LOG.error("Caught exception updating security group " + Format.value(groupNameIn), exception);
      } finally {
         if (!activeOnEntry) {
            CsiPersistenceManager.close();
         }
      }
      return result;
   }

    public static void removeUserFromGroup(String userNameIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            Group myGroup = getGroupByName(groupNameIn);
            User myUser = getUserByName(userNameIn);

            if ((null != myGroup) && (null != myUser)) {

                myGroup.removeMember(myUser);
                myManager.merge(myGroup);

                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception removing user " + Format.value(userNameIn) + " from group " + Format.value(groupNameIn), myException);

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void addUserToGroup(String userNameIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            Group myGroup = getGroupByName(groupNameIn);
            User myUser = getUserByName(userNameIn);

            if ((null != myGroup) && (null != myUser)) {

                myGroup.addMember(myUser);
                myManager.merge(myGroup);

                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception adding user " + Format.value(userNameIn) + " to group " + Format.value(groupNameIn), myException);

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void removeGroupFromGroup(String memberNameIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            Group myGroup1 = getGroupByName(groupNameIn);
            Group myGroup2 = getGroupByName(memberNameIn);

            if ((null != myGroup1) && (null != myGroup2)) {

                myGroup1.removeMember(myGroup2);
                myManager.merge(myGroup1);

                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception removing group " + Format.value(memberNameIn) + " from group " + Format.value(groupNameIn), myException);

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void addGroupToGroup(String memberNameIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            Group myGroup1 = getGroupByName(groupNameIn);
            Group myGroup2 = getGroupByName(memberNameIn);

            if ((null != myGroup1) && (null != myGroup2)) {

                myGroup1.addMember(myGroup2);
                myManager.merge(myGroup1);

                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
            }

        } catch(Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("Caught exception adding group " + Format.value(memberNameIn) + " to group " + Format.value(groupNameIn), myException);

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static void checkExpiration(boolean forceIn) {

        if (forceIn || (System.currentTimeMillis() > nextExpirationCheck)) {

            enforceExpirationPolicy();
        }
    }

   public static long getUserCount(boolean force) {
      if (force) {
         userCount = null;
      }
      return getUserCount();
   }

   public static long getUserCount() {
      Long count = userCount;

      if (count == null) {
         boolean myActiveOnEntry = CsiPersistenceManager.isActive();

         try {
            if (System.currentTimeMillis() > nextExpirationCheck) {
               enforceExpirationPolicy();
            }
            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            String jpaQuery = "select count(u) from User u where u.name not in " + RESTRICTED_USER_STRING +
                              " and u.disabled=false and u.suspended=false";
            Query query = manager.createQuery(jpaQuery);
            count = (Long) query.getSingleResult();
            userCount = count;
         } finally {
            if (!myActiveOnEntry) {
               CsiPersistenceManager.close();
            }
         }
      }
      return (count == null) ? null : count.longValue();
   }

    public static long reduceActiveUserCount(long countIn) {

        long myCount = countIn;

        if (0 < myCount) {

            myCount = suspendUsers(getNormalUsers(), myCount);
        }
        if (0 < myCount) {

            myCount = suspendUsers(getSecurityUsers(), myCount);
        }
        if (0 < myCount) {

            suspendUsers(getAdminUsers(), myCount);
        }
        return getUserCount(true);
    }

    private static List<User> getNormalUsers() {

        return getListForSuspension(getFilteredActiveUsers(_specialGroups, null, null));
    }

    private static List<User> getAdminUsers() {

        return getListForSuspension(getFilteredActiveUsers(null, _adminGroups, null), 1);
    }

    private static List<User> getSecurityUsers() {

        return getListForSuspension(getFilteredActiveUsers(null, _securityGroups, null), 1);
    }

    private static List<User> getListForSuspension(List<User> userListIn, int retentionCountIn) {

        List<User> myList = getListForSuspension(userListIn);

        if (!myList.isEmpty()) {

            for (int myRetentionCount = retentionCountIn; 0 < myRetentionCount; myRetentionCount--) {

                int myIndex = myList.size() - 1;

                if (0 <= myIndex) {

                    myList.remove(myIndex);

                } else {

                    break;
                }
            }
        }
        return myList;
    }

   private static List<User> getListForSuspension(List<User> userListIn) {
      List<User> suspendableUsers = new ArrayList<User>();

      if ((userListIn != null) && !userListIn.isEmpty()) {
         LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

         for (User user : userListIn) {
             LocalDateTime lastLoginDateTime = user.getLastLoginDateTime();
             if (lastLoginDateTime == null || lastLoginDateTime.isBefore(yesterday)) {
                 suspendableUsers.add(user);
             }
         }
      }
      return suspendableUsers;
   }

   @SuppressWarnings("unchecked")
   public static List<String> getFilteredUserNames(Collection<String> blockedGroupsIn,
                                                   Collection<String> restrictedGroupsIn,
                                                   FilteredUserRequest filterIn) {
      List<String> result = null;
      boolean myActiveOnEntry = CsiPersistenceManager.isActive();

      try {
         if (System.currentTimeMillis() > nextExpirationCheck) {
            enforceExpirationPolicy();
         }
         Query myQuery = buildFilteredUserRequest(blockedGroupsIn, restrictedGroupsIn, filterIn, true);
         result = myQuery.getResultList();
      } finally {
         if (!myActiveOnEntry) {
            CsiPersistenceManager.close();
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public static List<String> getFilteredActiveUserNames(Collection<String> blockedGroupsIn,
                                                         Collection<String> restrictedGroupsIn,
                                                         FilteredUserRequest filterIn) {
      List<String> result = null;
      boolean myActiveOnEntry = CsiPersistenceManager.isActive();

      try {
         if (System.currentTimeMillis() > nextExpirationCheck) {
            enforceExpirationPolicy();
         }
         Query myQuery = buildFilteredActiveUserRequest(blockedGroupsIn, restrictedGroupsIn, filterIn, true);
         result = myQuery.getResultList();
      } finally {
         if (!myActiveOnEntry) {
            CsiPersistenceManager.close();
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public static List<User> getFilteredActiveUsers(Collection<String> blockedGroupsIn,
                                                   Collection<String> restrictedGroupsIn,
                                                   FilteredUserRequest filterIn) {
      List<User> result = null;
      boolean myActiveOnEntry = CsiPersistenceManager.isActive();

      try {
         if (System.currentTimeMillis() > nextExpirationCheck) {
            enforceExpirationPolicy();
         }
         Query myQuery = buildFilteredActiveUserRequest(blockedGroupsIn, restrictedGroupsIn, filterIn, false);
         result = myQuery.getResultList();
      } finally {
         if (!myActiveOnEntry) {
            CsiPersistenceManager.close();
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public static List<String> getGroupNames(Collection<String> selectedGroupsIn) {
      List<String> result = null;

      if (selectedGroupsIn == null) {
         result = getActiveGroupNames();
      } else {
         if (!selectedGroupsIn.isEmpty()) {
            boolean activeOnEntry = CsiPersistenceManager.isActive();

            try {
               if (System.currentTimeMillis() > nextExpirationCheck.longValue()) {
                  enforceExpirationPolicy();
               }
               EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
               Query query =
                  manager.createQuery(new StringBuilder("select g.display from Group g where (g.type = :s1) and (g.name in ")
                                                .append(buildSelectList(selectedGroupsIn))
                                                .append(") order by lower(g.display)").toString());

               query.setParameter("s1", GroupType.SHARING);
               query.setHint("toplink.refresh", Boolean.TRUE);

               result = query.getResultList();
            } finally {
               if (!activeOnEntry) {
                  CsiPersistenceManager.close();
               }
            }
         }
      }
      return result;
   }

    // Do not return "admin" or "cso"
    @SuppressWarnings("unchecked")
    public static List<String> getAllUserNames() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            if (System.currentTimeMillis() > nextExpirationCheck) {

                enforceExpirationPolicy();
            }

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            String myQueryString = "select u.display from User u where u.name not in "
                    + RESTRICTED_USER_STRING + " order by lower(u.display)";
            Query myQuery = manager.createQuery(myQueryString);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            List<String> resultList = myQuery.getResultList();

            return resultList;

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    // Do not return "admin" or "cso"
    @SuppressWarnings("unchecked")
    public static List<String> getActiveUserNames() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            if (System.currentTimeMillis() > nextExpirationCheck) {

                enforceExpirationPolicy();
            }

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            String myQueryString = "select u.display from User u where u.name not in "
                                    + RESTRICTED_USER_STRING + " and u.disabled=false"
                                    + " and u.suspended=false order by lower(u.display)";
            Query myQuery = manager.createQuery(myQueryString);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            List<String> resultList = myQuery.getResultList();

            return resultList;

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getActiveGroupNames() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            if (System.currentTimeMillis() > nextExpirationCheck) {

                enforceExpirationPolicy();
            }

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            String myQueryString = "select g.display from Group g where g.type = :s1 order by lower(g.display)";
            Query myQuery = manager.createQuery(myQueryString);
            myQuery.setParameter("s1", GroupType.SHARING);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    // Do not return "admin" or "cso"
    @SuppressWarnings("unchecked")
    public static List<User>searchAllUsers(String searchStringIn, List<Boolean> userFlagsIn) {

        List<User> myResultList = new ArrayList<>();

        if ((null != searchStringIn) && (0 < searchStringIn.length())) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                Query myQuery = null;
                EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_simpleUserBeginning);
                appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                myQuery = manager.createQuery(myBuffer.toString());
                myQuery.setHint("toplink.refresh", Boolean.TRUE);
                myResultList = myQuery.getResultList();

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }

        } else {

            myResultList = getAllUsers(userFlagsIn);
        }
        return myResultList;
    }

    // Do not return "admin" or "cso"
    @SuppressWarnings("unchecked")
    public static List<User> getAllUsers(List<Boolean> userFlagsIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            Query myQuery = null;
            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            StringBuilder myBuffer = new StringBuilder();

            myBuffer.append(_simpleUserBeginning);
            appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
            myQuery = manager.createQuery(myBuffer.toString());
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllRoleNames() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            Query query = manager.createQuery("select r.name from Role r order by r.name");
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllCapcoPortions() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            Query query = manager.createQuery("select c.portion from CapcoGroup c order by c.portion");
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<CapcoGroup> getAllCapcoMappedRoles() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            Query query = manager.createQuery("select c from CapcoGroup c where c.portion is not null");
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllUserDisplayNames() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
            Query query = manager.createQuery("select u.display from User u order by lower(u.display)");
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> listUsers() {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query query = myManager.createQuery("select u.display from User u order by lower(u.display)");
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> listGroups(GroupType typeIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query query = myManager.createQuery("select g.display from Group g where g.type = :s1 order by lower(g.display)");
            query.setParameter("s1", typeIn);
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static GroupType getGroupType(String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query query = myManager.createQuery("select g.type from Group g where g.name = :s1");
            query.setParameter("s1", groupNameIn.trim().toLowerCase());
            query.setHint("toplink.refresh", Boolean.TRUE);
            GroupType myResult = null;
            try {

                myResult = (GroupType)query.getSingleResult();

            } catch(NoResultException myException) {

                // Ignore exception!
            }

            return myResult;

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

   @SuppressWarnings("unchecked")
   public static List<User> getUsers(List<String> listIn) throws CentrifugeException {
      List<User> result = null;

      if (listIn == null) {
         result = new ArrayList<User>();
      } else {
         if (!listIn.isEmpty()) {
            boolean activeOnEntry = CsiPersistenceManager.isActive();

            try {
               EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
               Query query =
                  manager.createQuery(new StringBuilder("select u from User u where u.name in ")
                                                .append(buildSelectList(listIn))
                                                .append(" order by u.name").toString());

               query.setHint("toplink.refresh", Boolean.TRUE);

               result = query.getResultList();
            } finally {
               if (!activeOnEntry) {
                  CsiPersistenceManager.close();
               }
            }
         }
      }
      return result;
   }

    // Do not return "admin" or "cso"
    @SuppressWarnings("unchecked")
    public static List<User> searchUsers(String searchStringIn, List<Boolean> userFlagsIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            Query myQuery = null;
            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            StringBuilder myBuffer = new StringBuilder();

            myBuffer.append(_simpleUserBeginning);
            if ((null != searchStringIn) && (0 < searchStringIn.length())) {

                myBuffer.append(_userSearchMatchingRequest);
            }
            appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
            myQuery = myManager.createQuery(myBuffer.toString());
            myQuery.setParameter("s1", searchStringIn);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> searchUsersInGroup(String group1NameIn, String group2NameIn,
                                                String searchStringIn, List<Boolean> userFlagsIn) {

        String mySearch = (null != searchStringIn) ? searchStringIn.trim() : null;

        if (null != mySearch) {

            String myGroup1Name = (null != group1NameIn) ? group1NameIn.trim().toLowerCase() : null;
            String myGroup2Name = (null != group2NameIn) ? group2NameIn.trim().toLowerCase() : null;

            if (null != myGroup1Name) {

                if (null != myGroup2Name) {

                    boolean myActiveOnEntry = CsiPersistenceManager.isActive();

                    try {

                        Query myQuery = null;
                        EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                        StringBuilder myBuffer = new StringBuilder();

                        myBuffer.append(_doubleGroupUserBeginning);

                        if (mySearch.length() > 0) {
                            myBuffer.append(_userSearchMatchingRequest);
                        }
                        appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                        myQuery = myManager.createQuery(myBuffer.toString());
                        myQuery.setParameter("s1", searchStringIn);
                        myQuery.setParameter("s2", myGroup1Name);
                        myQuery.setParameter("s3", myGroup2Name);
                        myQuery.setHint("toplink.refresh", Boolean.TRUE);
                        return myQuery.getResultList();

                    } finally {

                        if (!myActiveOnEntry) {

                            CsiPersistenceManager.close();
                        }
                    }

                } else {

                    return searchUsersInGroup(myGroup1Name, mySearch, userFlagsIn);
                }

            } else {

                return searchUsersInGroup(myGroup2Name, mySearch, userFlagsIn);
            }

        } else {

            return getAllUsersInGroup(group1NameIn, group2NameIn, userFlagsIn);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> searchUsersInGroup(String groupNameIn, String searchStringIn, List<Boolean> userFlagsIn) {

        String mySearch = (null != searchStringIn) ? searchStringIn.trim() : null;

        if ((null != mySearch) && (0 < mySearch.length())) {

            String myGroupName = (null != groupNameIn) ? groupNameIn.trim().toLowerCase() : null;

            if (null != myGroupName) {

                boolean myActiveOnEntry = CsiPersistenceManager.isActive();

                try {

                    Query myQuery = null;
                    EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                    StringBuilder myBuffer = new StringBuilder();

                    myBuffer.append(_singleGroupUserBeginning);
                    myBuffer.append(_userSearchMatchingRequest);
                    appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                    myQuery = myManager.createQuery(myBuffer.toString());
                    myQuery.setParameter("s1", mySearch);
                    myQuery.setParameter("s2", myGroupName);
                    myQuery.setHint("toplink.refresh", Boolean.TRUE);
                    return myQuery.getResultList();

                } finally {

                    if (!myActiveOnEntry) {

                        CsiPersistenceManager.close();
                    }
                }

            } else {

                return searchAllUsers(searchStringIn, userFlagsIn);
            }

        } else {

            return getAllUsersInGroup(groupNameIn, userFlagsIn);
        }
    }

   @SuppressWarnings("unchecked")
   public static List<Group> getGroups(GroupType typeIn, List<String> listIn) throws CentrifugeException {
      List<Group> result = null;

      if (listIn == null) {
         result = new ArrayList<Group>();
      } else {
         if (!listIn.isEmpty()) {
            boolean activeOnEntry = CsiPersistenceManager.isActive();

            try {
               EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
               Query query =
                  manager.createQuery(new StringBuilder("select g from Group g where g.type = :s2 and g.name in ")
                                                .append(buildSelectList(listIn))
                                                .append(" order by g.name").toString());

               query.setHint("toplink.refresh", Boolean.TRUE);

               result = query.getResultList();
            } finally {
               if (!activeOnEntry) {
                  CsiPersistenceManager.close();
               }
            }
         }
      }
      return result;
   }

    // Do not return "Administrators", "SecurityOfficers" or "Everyone"
    @SuppressWarnings("unchecked")
    public static List<Group> searchGroups(GroupType typeIn, String searchStringIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery("select g from Group g where (g.name  not in "
                                                    + PERMANENT_GROUP_STRING + ") and (g.type = :s2) and (g.name LIKE lower(:s1) ESCAPE '\\') order by g.name");
            myQuery.setParameter("s1", searchStringIn);
            myQuery.setParameter("s2", typeIn);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    // Do not return "Administrators", "SecurityOfficers" or "Everyone"
    @SuppressWarnings("unchecked")
    public static List<Group> getAllGroups(GroupType typeIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            Query myQuery = myManager.createQuery("select g from Group g where (g.name  not in "
                    + PERMANENT_GROUP_STRING + ") and (type = :s1) order by g.name");
            myQuery.setParameter("s1", typeIn);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Group> getAllGroupsInGroup(GroupType typeIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            Query query = null;
            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            query = myManager.createQuery("select g1 from Group g1 join g1.groups g2 where (g1.type = :s2) and (g2.name = :s1) order by g1.name");
            query.setParameter("s2", typeIn);
            query.setParameter("s1", myGroupName);
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllGroupNamesInGroup(GroupType typeIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            Query query = myManager.createQuery("select g1.name from Group g1 join g1.groups g2 where (g1.type = :s2) and (g2.name = :s1)");
            query.setParameter("s1", myGroupName);
            query.setParameter("s2", typeIn);
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllGroupNamesGroupIsIn(GroupType typeIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            Query query = myManager.createQuery("select g2.name from Group g1 join g1.groups g2 where (g2.type = :s2) and (g1.name = :s1)");
            query.setParameter("s1", myGroupName);
            query.setParameter("s2", typeIn);
            query.setHint("toplink.refresh", Boolean.TRUE);
            return query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Group> searchGroupsInGroup(GroupType typeIn, String searchStringIn, String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            Query myQuery = myManager.createQuery("select g1 from Group g1 join g1.groups g2 where (g1.type = :s2) and (g2.name = :s3) and (g1.name LIKE lower(:s1) ESCAPE '\\') order by g1.name");
            myQuery.setParameter("s1", searchStringIn);
            myQuery.setParameter("s2", typeIn);
            myQuery.setParameter("s3", myGroupName);
            myQuery.setHint("toplink.refresh", Boolean.TRUE);
            return myQuery.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    public static boolean groupExists(String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            Query query = myManager.createQuery("select count(*) from Group g where g.name = :s1");
            query.setParameter("s1", myGroupName);

            return (0L < (Long)query.getSingleResult());

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
    }

    //TODO:


    //TODO:


    //TODO:


    //TODO:


    //TODO:


    //TODO:


    //TODO:


    //TODO:

    @SuppressWarnings("unchecked")
    public static List<User> getAllUsersInGroup(String group1NameIn, String group2NameIn, List<Boolean> userFlagsIn) {

        String myGroup1Name = (null != group1NameIn) ? group1NameIn.trim() : "";
        String myGroup2Name = (null != group2NameIn) ? group2NameIn.trim() : "";

        if (0 < myGroup1Name.length()) {

            if (0 < myGroup2Name.length()) {

                boolean myActiveOnEntry = CsiPersistenceManager.isActive();

                try {

                    Query myQuery = null;
                    EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                    StringBuilder myBuffer = new StringBuilder();

                    myBuffer.append(_doubleGroupUserBeginning);
                    appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                    myQuery = myManager.createQuery(myBuffer.toString());
                    myQuery.setParameter("s1", myGroup1Name);
                    myQuery.setParameter("s2", myGroup2Name);
                    myQuery.setHint("toplink.refresh", Boolean.TRUE);
                    return myQuery.getResultList();

                } finally {

                    if (!myActiveOnEntry) {

                        CsiPersistenceManager.close();
                    }
                }

            } else {

                return getAllUsersInGroup(myGroup1Name, userFlagsIn);
            }

        } else {

            return getAllUsersInGroup(myGroup2Name, userFlagsIn);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> getAllUsersInGroup(String groupNameIn, List<Boolean> userFlagsIn) {

        String myGroupName = (null != groupNameIn) ? groupNameIn.trim().toLowerCase() : null;

        if (null != myGroupName) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                Query myQuery = null;
                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_singleGroupUserBeginning);
                appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                myQuery = myManager.createQuery(myBuffer.toString());
                myQuery.setParameter("s1", myGroupName);
                myQuery.setHint("toplink.refresh", Boolean.TRUE);
                return myQuery.getResultList();

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }

        } else {

            return getAllUsers(userFlagsIn);
        }
    }

    public static User getUserByName(User userIn) {

        return getUserByName(new NamePair(userIn.getDisplay(), userIn.getName()));
    }

    public static User getUserByName(String userNameIn) {

        return getUserByName(new NamePair(userNameIn, userNameIn));
    }

    public static User getUserByName(NamePair namePairIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();
        User myUser = null;

        try {

            if ((null != namePairIn) && namePairIn.isValid()) {

                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

                Query myQuery = myManager.createQuery("select u1 from User u1 where u1.name = :s1");
                myQuery.setParameter("s1", namePairIn.getName());
                try {

                    myUser = (User) myQuery.getSingleResult();

                } catch(NoResultException myException) {

                    // Ignore exception!
                }
            }

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return myUser;
    }

    public static Group getGroupByName(Group groupIn) {

        return getGroupByName(new NamePair(groupIn.getDisplay(), groupIn.getName()));
    }

    public static Group getGroupByName(String groupNameIn) {

        return getGroupByName(new NamePair(groupNameIn, groupNameIn));
    }

    public static Group getGroupByName(NamePair namePairIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();
        Group myGroup = null;

        try {

            if ((null != namePairIn) && namePairIn.isValid()) {

                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

                Query myQuery = myManager.createQuery("select g1 from Group g1 where g1.name = :s1");
                myQuery.setParameter("s1", namePairIn.getName());
                try {

                    myGroup = (Group) myQuery.getSingleResult();

                } catch(NoResultException myException) {

                    // Ignore exception!
                }
            }

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return myGroup;
    }

    public static Group getSharingGroupByName(Group groupIn) {

        return getSharingGroupByName(new NamePair(groupIn.getDisplay(), groupIn.getName()));
    }

    public static Group getSharingGroupByName(String groupNameIn) {

        return getSharingGroupByName(new NamePair(groupNameIn, null));
    }

    public static Group getSharingGroupByName(NamePair namePairIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();
        Group myGroup = null;

        try {

            if ((null != namePairIn) && namePairIn.isValid()) {

                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

                Query myQuery = myManager.createQuery("select g1 from Group g1 where g1.name = :s1 and g1.type = :s2");
                myQuery.setParameter("s1", namePairIn.getName());
                myQuery.setParameter("s2", GroupType.SHARING);
                try {

                    myGroup = (Group) myQuery.getSingleResult();

                } catch(NoResultException myException) {

                    // Ignore exception!
                }
            }

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return myGroup;
    }

    public static CapcoGroup getSecurityGroupByName(Group groupIn) {

        return getSecurityGroupByName(new NamePair(groupIn.getDisplay(), groupIn.getName()));
    }

    public static CapcoGroup getSecurityGroupByName(String groupNameIn) {

        return getSecurityGroupByName(new NamePair(groupNameIn, groupNameIn));
    }

    public static CapcoGroup getSecurityGroupByName(NamePair namePairIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();
        CapcoGroup myGroup = null;

        try {

            if ((null != namePairIn) && namePairIn.isValid()) {

                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

                Query myQuery = myManager.createQuery("select g1 from CapcoGroup g1 where g1.name = :s1");
                myQuery.setParameter("s1", namePairIn.getName());
                try {

                    myGroup = (CapcoGroup) myQuery.getSingleResult();

                } catch(NoResultException myException) {

                    // Ignore exception!
                }
            }

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return myGroup;
    }

    public static List<User> getExhaustiveUsersInGroup(String groupNameOneIn, String groupNameTwoIn, String searchStringIn, List<Boolean> userFlagsIn) {

        List<User> myResults = new ArrayList<User>();
        String myGroupNameOne = (null != groupNameOneIn) ? groupNameOneIn.trim().toLowerCase() : null;
        String myGroupNameTwo = (null != groupNameTwoIn) ? groupNameTwoIn.trim().toLowerCase() : null;

        if ((null != myGroupNameOne) && (0 < myGroupNameOne.length())) {

            if ((null != myGroupNameTwo) && (0 < myGroupNameTwo.length())) {

                List<User> myListOne = getUsersInGroups(getExhaustiveGroupsInGroup(myGroupNameOne), searchStringIn, userFlagsIn);
                List<User> myListTwo = getUsersInGroups(getExhaustiveGroupsInGroup(myGroupNameTwo), searchStringIn, userFlagsIn);
                Map<String, User> myMap = new TreeMap<String, User>();

                for (User myUser : myListOne) {

                    myMap.put(myUser.getName(), myUser);
                }
                for (User myUser : myListTwo) {

                    if (myMap.containsKey(myUser.getName())) {

                        myResults.add(myUser);
                    }
                }

            } else {

                myResults = getUsersInGroups(getExhaustiveGroupsInGroup(myGroupNameOne), searchStringIn, userFlagsIn);
            }

        } else if ((null != myGroupNameTwo) && (0 < myGroupNameTwo.length())) {

            myResults = getUsersInGroups(getExhaustiveGroupsInGroup(myGroupNameTwo), searchStringIn, userFlagsIn);

        } else {

            myResults = searchAllUsers(searchStringIn, userFlagsIn);
        }
        return myResults;
    }

    public static List<Group> getExhaustiveGroupsInGroup(String groupNameIn, String searchStringIn, GroupType typeIn) {

        List<Group> myResults = new ArrayList<Group>();
        String myGroupName = (null != groupNameIn) ? groupNameIn.trim().toLowerCase() : null;

        if ((null != myGroupName) && (0 < myGroupName.length())) {

            myResults = searchGroupList(getExhaustiveGroupsInGroup(myGroupName), searchStringIn);

        } else {

            searchGroups(typeIn, searchStringIn);
        }

        return myResults;
    }

    private static List<User> getUsersInGroups(Collection<String> groupNamesIn, String searchStringIn, List<Boolean> userFlagsIn) {

        List<User> myUserList = new ArrayList<User>();

        if ((null != groupNamesIn) && !groupNamesIn.isEmpty()) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();
            String mySearch = (null != searchStringIn) ? searchStringIn.trim() : null;

            try {

                Query myQuery = null;
                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                StringBuilder myBuffer = new StringBuilder();
                String myUserMatchRequest = ((null != mySearch) && (0 < mySearch.length())) ? _userSearchMatchingRequest : "";

                myBuffer.append(_groupListUserBeginning);
                myBuffer.append(myUserMatchRequest);
                appendUserFlags(myBuffer, userFlagsIn).append(_simpleUserSearchEnding);
                myQuery = myManager.createQuery(myBuffer.toString());
                if (0 < myUserMatchRequest.length()) {

                    myQuery.setParameter("s1", mySearch);
                }
                myQuery.setParameter("s2", groupNamesIn);
                myQuery.setHint("toplink.refresh", Boolean.TRUE);

                myUserList = myQuery.getResultList();

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
        return myUserList;
    }

    private static Set<String> getExhaustiveGroupsInGroup(String groupNameIn) {

        Set<String> myResults = new TreeSet<String>();
        List<Group> myGroups = new ArrayList<Group>();
        String myGroupName = groupNameIn;

        while (null != myGroupName) {

            myResults.add(myGroupName);
            myGroups.addAll(getAllGroupsInGroup(myGroupName));
            myGroupName = null;

            for (int i = myGroups.size() - 1; 0 <= i; i = myGroups.size() - 1) {

                Group myGroup = myGroups.remove(i);

                myGroupName = myGroup.getName();

                if (!myResults.contains(myGroupName)) {

                    break;
                }
                myGroupName = null;
            }
        }
        return myResults;
    }

   private static String buildSelectList(Collection<String> listIn) {
      return listIn.stream().map(i -> i.trim().toLowerCase()).collect(Collectors.joining("','", "('", "')"));
   }

    private static List<Group> searchGroupList(Collection<String> groupNamesIn, String searchStringIn) {

        List<Group> myGroupList = new ArrayList<Group>();
        String mySearch = (null != searchStringIn) ? searchStringIn.trim().toLowerCase() : null;

        if ((null != groupNamesIn) && !groupNamesIn.isEmpty()) {

            boolean myActiveOnEntry = CsiPersistenceManager.isActive();

            try {

                Query myQuery = null;
                EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
                StringBuilder myBuffer = new StringBuilder();
                String myGroupMatchRequest = ((null != mySearch) && (0 < mySearch.length())) ? _groupSearchMatchingRequest : "";

                myBuffer.append(_groupListGroupBeginning);
                myBuffer.append(myGroupMatchRequest);
                myBuffer.append(_simpleGroupSearchEnding);
                myQuery = myManager.createQuery(myBuffer.toString());
                if ((null != mySearch) && (0 < mySearch.length())) {

                    myQuery.setParameter("s1", mySearch);
                }
                myQuery.setParameter("s2", groupNamesIn);
                myQuery.setHint("toplink.refresh", Boolean.TRUE);

                myGroupList = myQuery.getResultList();

            } finally {

                if (!myActiveOnEntry) {

                    CsiPersistenceManager.close();
                }
            }
        }
        return myGroupList;
    }

   private static void guaranteeExpirationInformation(User newData) {
      if (newData != null) {
         if (newData.getCreationDateTime() == null) {
            newData.setCreationDateTime(LocalDateTime.now());
         }
         if (!newData.isPerpetual().booleanValue() && (newData.getExpirationDateTime() == null)) {
            SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();

            newData.setExpirationDateTime(LocalDate.now().plusDays(1 + policyConfig.getDaysUntilExpiration()).atStartOfDay());
         }
      }
   }

    private static Query buildFilteredUserRequest(Collection<String> blockedGroupsIn,
                                                  Collection<String> restrictedGroupsIn,
                                                  FilteredUserRequest filterIn, boolean namesOnlyIn) {

        return buildUserRequest(blockedGroupsIn, restrictedGroupsIn, filterIn, false, namesOnlyIn);
    }

    private static Query buildFilteredActiveUserRequest(Collection<String> blockedGroupsIn,
                                                        Collection<String> restrictedGroupsIn,
                                                        FilteredUserRequest filterIn, boolean namesOnlyIn) {

        return buildUserRequest(blockedGroupsIn, restrictedGroupsIn, filterIn, true, namesOnlyIn);
    }

   private static Query buildUserRequest(Collection<String> blockedGroupsIn, Collection<String> restrictedGroupsIn,
                                         FilteredUserRequest extendedFilterIn, boolean activeOnlyIn, boolean namesOnlyIn) {
      Query query = null;
      StringBuilder buffer =
         new StringBuilder(namesOnlyIn ? "SELECT u.display" : "SELECT u")
                   .append(" FROM User u WHERE u.name NOT IN ")
                   .append(RESTRICTED_USER_STRING);

      if (activeOnlyIn) {
         buffer.append(" AND u.disabled=false AND u.suspended=false ");
      }
      buffer = basicFilteredUserCondition(buffer, blockedGroupsIn, restrictedGroupsIn);

      if (extendedFilterIn != null) {
         buffer = applyExtendedFilter(buffer, extendedFilterIn);
      }
      buffer.append(" ORDER BY LOWER(u.display)");

      EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
      query = manager.createQuery(buffer.toString());

      if ((restrictedGroupsIn != null) && !restrictedGroupsIn.isEmpty()) {
         query.setParameter("s1", GroupType.SHARING);
      }
      query.setHint("toplink.refresh", Boolean.TRUE);
      return query;
   }

   private static StringBuilder basicFilteredUserCondition(StringBuilder bufferIn, Collection<String> blockedGroupsIn,
                                                           Collection<String> restrictedGroupsIn) {
      if ((restrictedGroupsIn != null) && !restrictedGroupsIn.isEmpty()) {
         bufferIn.append(" AND EXISTS (SELECT g1 FROM u.groups g1 WHERE (g1.type = :s1) AND (g1.name IN ")
                 .append(buildSelectList(restrictedGroupsIn))
                 .append("))");
      }
      if ((blockedGroupsIn != null) && !blockedGroupsIn.isEmpty()) {
         bufferIn.append(" AND NOT EXISTS (SELECT g2 FROM u.groups g2 WHERE g2.name IN ")
                 .append(buildSelectList(blockedGroupsIn))
                 .append(")");
      }
      return bufferIn;
   }

    private static StringBuilder applyExtendedFilter(StringBuilder bufferIn, FilteredUserRequest filterIn) {

        String[][] myPatterns = filterIn.getPatterns();
        int[] myMasks = filterIn.getMasks();
        UserSortMode[] mySorts = filterIn.getSorts();
        int myPatternsSize = (null != myPatterns) ? myPatterns.length : 0;
        int myMasksSize = (null != myMasks) ? myMasks.length : 0;
        int myLimit = Math.min(myPatternsSize, myMasksSize);
        int myFallBack = bufferIn.length();
        boolean myChange = false;

        if (0 < myLimit) {

            String myPrefix = "";

            bufferIn.append(" AND (");
            for (int i = 0; myLimit > i; i++) {

                String[] myPatternSet = myPatterns[i];
                List<String> myFieldSet = unmaskFieldNames(UserSortMode.getColumns(), myMasks[i]);
                int myFieldSetSize = (null != myFieldSet) ? myFieldSet.size() : 0;
                int myPatternSetLength = (null != myPatternSet) ? myPatternSet.length : 0;

                if ((0 < myFieldSetSize) && (0 < myPatternSetLength)) {

                    String myMatch = ((null != myPatternSet[0]) && (0 < myPatternSet[0].length()))
                            ? myPatternSet[0].toLowerCase() : null;
                    String myReject = ((1 < myPatternSetLength) && (null != myPatternSet[1]) && (0 < myPatternSet[1].length()))
                            ? myPatternSet[1].toLowerCase() : null;

                    for (int j = 0; myFieldSetSize > j; j++) {

                        String myFieldName = myFieldSet.get(j);

                        if ("%".equals(myReject)) {

                            bufferIn.append(myPrefix);
                            bufferIn.append("((u.");
                            bufferIn.append(myFieldName);
                            bufferIn.append(" IS NULL) OR (u.");
                            bufferIn.append(myFieldName);
                            bufferIn.append(" = '')) ");
                            myChange = true;

                        } else if (null != myMatch) {

                            if (null != myReject) {

                                // Process both match and reject
                                bufferIn.append(myPrefix);
                                bufferIn.append("(u.");
                                bufferIn.append(myFieldName);
                                bufferIn.append(" LIKE '");
                                bufferIn.append(myMatch);
                                bufferIn.append("') AND (u.");
                                bufferIn.append(myFieldName);
                                bufferIn.append(" NOT LIKE '");
                                bufferIn.append(myReject);
                                bufferIn.append("') ");

                            } else {

                                // Process match only
                                bufferIn.append(myPrefix);
                                bufferIn.append("(u.");
                                bufferIn.append(myFieldName);
                                bufferIn.append(" LIKE '");
                                bufferIn.append(myMatch);
                                bufferIn.append("') ");
                            }
                            myChange = true;

                        } else if (null != myReject) {

                            // Process reject only
                            bufferIn.append(myPrefix);
                            bufferIn.append("(u.");
                            bufferIn.append(myFieldName);
                            bufferIn.append(" NOT LIKE '");
                            bufferIn.append(myReject);
                            bufferIn.append("') ");
                            myChange = true;
                        }
                        myPrefix = " OR ";
                    }
                }
            }
            bufferIn.append(") ");
        }
        if (!myChange) {

            bufferIn.setLength(myFallBack);
        }
        if ((null != mySorts) && (0 < mySorts.length)) {

            String myPrefix = " ORDER BY u.";

            for (int i = 0; mySorts.length > i; i++) {

                UserSortMode mySort = mySorts[i];

                if (null != mySort) {

                    bufferIn.append(myPrefix);
                    bufferIn.append(mySort.getColumn());
                    bufferIn.append(' ');
                    bufferIn.append(mySort.getDirection());
                    myPrefix = ", u.";
                }
            }
        }
        return bufferIn;
    }

    private static List<String> unmaskFieldNames(String[] fieldNamesIn, int maskIn) {

        List<String> myList = new ArrayList<String>();
        int myLimit = fieldNamesIn.length;
        int myMask = maskIn;

        for (int i = 0; myLimit > i; i++) {

            if (0 == myMask) {

                break;
            }
            if (0 < (1 & myMask)) {

                myList.add(fieldNamesIn[i]);
            }
            myMask >>= 1;
        }
        return myList.isEmpty() ? null : myList;
    }

    private static long suspendUsers(List<User> userListIn, long countIn) {

        long myCount = countIn;

        for (User myUser : userListIn) {

            if (0 == myCount) {

                break;
            }
            try {

                suspend(myUser);
                myCount--;

            } catch (Exception myException) {

            }
        }
        userCount = null;
        return myCount;
    }

   private static StringBuilder appendUserFlags(StringBuilder bufferIn, List<Boolean> userFlagsIn) {
      StringBuilder buffer = (bufferIn == null) ? new StringBuilder() : bufferIn;

      if (userFlagsIn != null) {
         boolean initializedFlag = false;
         int howMany = userFlagsIn.size();

         for (int i = 0; (i < howMany) && (i < userConditions.length); i++) {
            if ((userFlagsIn.get(i) != null) && userFlagsIn.get(i).booleanValue()) {
               if (initializedFlag) {
                  buffer.append(" OR");
               } else {
                  initializedFlag = true;
                  buffer.append(" AND (");
               }
               buffer.append(userConditions[i]);
            }
         }
         if (initializedFlag) {
            buffer.append(")");
         }
      }
      return buffer;
   }

    @SuppressWarnings("unchecked")
    private static List<Group> getAllGroupsInGroup(String groupNameIn) {

        boolean myActiveOnEntry = CsiPersistenceManager.isActive();
        List<Group> resultList = new ArrayList<Group>();

        try {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();
            String myGroupName = groupNameIn.trim().toLowerCase();
            Query query = myManager.createQuery("select g1 from Group g1 join g1.groups g2 where (g2.name = :s1)");
            query.setParameter("s1", myGroupName);
            query.setHint("toplink.refresh", Boolean.TRUE);
            resultList = query.getResultList();

        } finally {

            if (!myActiveOnEntry) {

                CsiPersistenceManager.close();
            }
        }
        return resultList;
    }
}

