package csi.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.security.queries.Users;
import csi.server.common.enumerations.GroupType;
import csi.server.common.identity.User;
import csi.server.common.util.ValuePair;

public class SecurityPolicyConfig extends AbstractConfigurationSettings {
    private static final String EVERYONE_GROUP = JAASRole.EVERYONE_GROUP_NAME;
    private static final int    DEFAULT_EXPIRATION_DAYS             = 30;
    private static final int    DEFAULT_IDLE_DAYS                   = 15;

    private boolean             expireUsersByDefault                = false;
    private int                 daysUntilExpiration                 = DEFAULT_EXPIRATION_DAYS;
    private boolean             expireIdleUsers                     = false;
    private int                 idleDaysUntilExpiration             = DEFAULT_IDLE_DAYS;
    private Boolean             enableUserAccountExpiration         = Boolean.FALSE;
    private Boolean             autoSuspendWhenOverLimit            = Boolean.FALSE;

    private Boolean             enforceCapcoRestrictions            = Boolean.FALSE;
    private Boolean             forceUnrecognizedSciProcessing      = Boolean.FALSE;
    private Boolean             forceSciCompliance                  = Boolean.FALSE;
    private Boolean             forceSapCompliance                  = Boolean.FALSE;
    private Boolean             enableCapcoLabelProcessing          = Boolean.FALSE;
    private Boolean             useAbreviations                     = Boolean.FALSE;
    private String              defaultPortion                      = null;
    private String              failOverPortion                     = null;

    private String              defaultTags                         = null;
    private String              failOverTags                        = null;
    private Boolean             enforceDataSecurityTags             = Boolean.FALSE;
    private Boolean             enableTagLabelProcessing            = Boolean.FALSE;
    private String              tagBannerPrefix                     = null;
    private String              tagBannerSuffix                     = null;
    private String              tagItemPrefix                       = null;
    private String              tagBannerDelimiter                  = null;
    private String              tagBannerSubDelimiter               = null;
    private String              tagInputDelimiter                   = null;

    private String              defaultBanner                       = null;
    private Map<String, String> bannerColors                        = null;

    private Boolean             restrictRoleVisibility              = Boolean.FALSE;
    private String		 	    hideFromGroups			            = null;
    private String		 	    restrictToGroups				    = null;

    private Boolean             ownerSetsSecurity                   = Boolean.FALSE;
    private Boolean             showSharingPanel                    = Boolean.FALSE; // Not currently in use
    private Boolean             enforceNeedToKnow                   = Boolean.FALSE; // Not currently in use

    // Values Generated from configured values above
    private Map<String, String> hideFromGroupMap    		        = null;
    private Map<String, String> restrictToGroupMap			        = null;
    private Map<String, ValuePair<String, String>> bannerControl    = null;

   public Boolean getEnableUserAccountExpiration() {
      return enableUserAccountExpiration;
   }

   public void setEnableUserAccountExpiration(Boolean enableUserExpirationIn) {
      enableUserAccountExpiration = (enableUserExpirationIn == null) ? Boolean.FALSE : enableUserExpirationIn;
   }

   public Boolean getAutoSuspendWhenOverLimit() {
      return autoSuspendWhenOverLimit;
   }

   public void setAutoSuspendWhenOverLimit(Boolean autoSuspendWhenOverLimitIn) {
      autoSuspendWhenOverLimit = (autoSuspendWhenOverLimitIn == null) ? Boolean.FALSE : autoSuspendWhenOverLimitIn;
   }

   public boolean getExpireIdleUsers() {
      return expireIdleUsers;
   }

   public void setExpireIdleUsers(Boolean expireIdleUsersIn) {
      expireIdleUsers = (expireIdleUsersIn == null) ? Boolean.FALSE.booleanValue() : expireIdleUsersIn.booleanValue();
   }

   public Boolean getEnforceCapcoRestrictions() {
      return enforceCapcoRestrictions;
   }

   public void setEnforceCapcoRestrictions(Boolean restrict) {
      enforceCapcoRestrictions = (restrict == null ) ? Boolean.FALSE : restrict;
   }

   public Boolean getEnforceAccessRestrictions() {
      return getEnforceCapcoRestrictions();
   }

   public void setEnforceAccessRestrictions(Boolean restrict) {
      setEnforceCapcoRestrictions(restrict);
   }

   public Boolean getEnforceDataSecurityTags() {
      return enforceDataSecurityTags;
   }

   public void setEnforceDataSecurityTags(Boolean enforceIn) {
      enforceDataSecurityTags = (enforceIn == null ) ? Boolean.FALSE : enforceIn;
   }

   public Boolean getOwnerSetsSecurity() {
      return ownerSetsSecurity;
   }

   public void setOwnerSetsSecurity(Boolean ownerSetsSecurityIn) {
      ownerSetsSecurity = (ownerSetsSecurityIn == null) ? Boolean.FALSE : ownerSetsSecurityIn;
   }

   public Boolean getShowSharingPanel() {
      return showSharingPanel;
   }

   public void setShowSharingPanel(Boolean showSharingPanelIn) {
      showSharingPanel = (showSharingPanelIn == null) ? Boolean.FALSE : showSharingPanelIn;
   }

   public Boolean getEnforceNeedToKnow() {
      return enforceNeedToKnow;
   }

   public void setEnforceNeedToKnow(Boolean enforceIn) {
      enforceNeedToKnow = (enforceIn == null) ? Boolean.FALSE : enforceIn;
   }

   public Boolean getRestrictRoleVisibility() {
      return restrictRoleVisibility;
   }

   public void setRestrictRoleVisibility(Boolean restrict) {
      restrictRoleVisibility = (restrict == null) ? Boolean.FALSE : restrict;
   }

   public Boolean getUseAbreviations() {
      return useAbreviations;
   }

   public void setUseAbreviations(Boolean useAbreviationsIn) {
      useAbreviations = (useAbreviationsIn == null) ? Boolean.FALSE : useAbreviationsIn;
   }

   public Boolean getEnableCapcoLabelProcessing() {
      return enableCapcoLabelProcessing;
   }

   public void setEnableCapcoLabelProcessing(Boolean enableIn) {
      enableCapcoLabelProcessing = (enableIn == null) ? Boolean.FALSE : enableIn;
   }

   public Boolean getEnableTagLabelProcessing() {
      return enableTagLabelProcessing;
   }

   public void setEnableTagLabelProcessing(Boolean enableIn) {
      enableTagLabelProcessing = (enableIn == null) ? Boolean.FALSE : enableIn;
   }

   public Boolean getForceUnrecognizedSciProcessing() {
      return forceUnrecognizedSciProcessing;
   }

   public void setForceUnrecognizedSciProcessing(Boolean enableIn) {
      forceUnrecognizedSciProcessing = (enableIn == null) ? Boolean.FALSE : enableIn;
   }

   public Boolean getForceSciCompliance() {
      return forceSciCompliance;
   }

   public void setForceSciCompliance(Boolean enableIn) {
      forceSciCompliance = (enableIn == null) ? Boolean.FALSE : enableIn;
   }

   public Boolean getForceSapCompliance() {
      return forceSapCompliance;
   }

   public void setForceSapCompliance(Boolean enableIn) {
      forceSapCompliance = (enableIn == null) ? Boolean.FALSE : enableIn;
   }

   public String getHideFromGroups() {
      return hideFromGroups;
   }

   public void setHideFromGroups(String hideFromGroupsIn) {
      hideFromGroups = hideFromGroupsIn;
   }

   public String getRestrictToGroups() {
      return restrictToGroups;
   }

   public void setRestrictToGroups(String restrictToGroupsIn) {
      restrictToGroups = restrictToGroupsIn;
   }

   public String getDefaultBanner() {
      return defaultBanner;
   }

   public void setDefaultBanner(String bannerIn) {
      defaultBanner = bannerIn;
   }

   public String getDefaultPortion() {
      return defaultPortion;
   }

   public void setDefaultPortion(String portionIn) {
      defaultPortion = portionIn;
   }

   public String getFailOverPortion() {
      return failOverPortion;
   }

   public void setFailOverPortion(String portionIn) {
      failOverPortion = portionIn;
   }

   public String getDefaultTags() {
      return defaultTags;
   }

   public void setDefaultTags(String tagsIn) {
      defaultTags = tagsIn;
   }

   public String getFailOverTags() {
      return failOverTags;
   }

   public void setFailOverTags(String tagsIn) {
      failOverTags = tagsIn;
   }

   public String getTagBannerPrefix() {
      return tagBannerPrefix;
   }

   public void setTagBannerPrefix(String tagBannerPrefixIn) {
      tagBannerPrefix = tagBannerPrefixIn;
   }

   public String getTagBannerSuffix() {
      return tagBannerSuffix;
   }

   public void setTagBannerSuffix(String tagBannerSuffixIn) {
      tagBannerSuffix = tagBannerSuffixIn;
   }

   public String getTagItemPrefix() {
      return tagItemPrefix;
   }

   public void setTagItemPrefix(String tagItemPrefixIn) {
      tagItemPrefix = tagItemPrefixIn;
   }

   public String getTagBannerDelimiter() {
      return tagBannerDelimiter;
   }

   public void setTagBannerDelimiter(String tagBannerDelimiterIn) {
      tagBannerDelimiter = tagBannerDelimiterIn;
   }

   public String getTagBannerSubDelimiter() {
      return tagBannerSubDelimiter;
   }

   public void setTagBannerSubDelimiter(String tagBannerSubDelimiterIn) {
      tagBannerSubDelimiter = tagBannerSubDelimiterIn;
   }

   public String getTagInputDelimiter() {
      return tagInputDelimiter;
   }

   public void setTagInputDelimiter(String tagInputDelimiterIn) {
      tagInputDelimiter = tagInputDelimiterIn;
   }

   public Map<String,String> getBannerColors() {
      return bannerColors;
   }

   public void setBannerColors(Map<String,String> bannerColorsIn) {
      bannerColors = bannerColorsIn;
   }

   public Map<String,ValuePair<String,String>> getBannerControl() {
      if (bannerControl == null) {
         bannerControl = new HashMap<String,ValuePair<String,String>>();

         if (bannerColors != null) {
            for (Map.Entry<String,String> entry : bannerColors.entrySet()) {
               String[] myPair = entry.getValue().split(",");

               if (myPair.length > 1) {
                  bannerControl.put(entry.getKey(), new ValuePair<String,String>(myPair[0].trim(), myPair[1].trim()));
               }
            }
         }
      }
      return bannerControl;
   }

   public void setBannerControl(Map<String, ValuePair<String, String>> bannerControlIn) {
      bannerControl = bannerControlIn;
   }

   private LocalDateTime computeExpiration() {
      return LocalDate.now().plusDays(1 + getDaysUntilExpiration()).atStartOfDay();
   }

   public void applyExpirationPolicy(User user) {
      if (user != null) {
         LocalDateTime now = LocalDateTime.now();

         // Calculate and set expiration date if "expireUsersByDefault" is set
         // without regard to whether "enableUserAccountExpiration" is set.
         // Identifying and enforcing expiration dates are separate concerns.
         if (expireUsersByDefault) {
            user.setExpirationDateTime(computeExpiration());
            user.setPerpetual(Boolean.FALSE);
         } else {
            user.setExpirationDateTime(null);
            user.setPerpetual(Boolean.TRUE);
         }
         user.setCreationDateTime(now);
         user.setLastLoginDateTime(now);
         user.setDisabled(Boolean.FALSE);
      }
   }

   public boolean isExpireUsersByDefault() {
      return expireUsersByDefault;
   }

   public void setExpireUsersByDefault(boolean expireUsersByDefault) {
      this.expireUsersByDefault = expireUsersByDefault;
   }

   public int getDaysUntilExpiration() {
      return (daysUntilExpiration > 0) ? daysUntilExpiration : DEFAULT_EXPIRATION_DAYS;
   }

   public void setDaysUntilExpiration(int defaultExpirationPeriod) {
      this.daysUntilExpiration = defaultExpirationPeriod;
   }

   public int getIdleDaysUntilExpiration() {
      return (idleDaysUntilExpiration > 0) ? idleDaysUntilExpiration : DEFAULT_IDLE_DAYS;
   }

   public void setIdleDaysUntilExpiration(int defaultExpirationPeriod) {
      this.idleDaysUntilExpiration = defaultExpirationPeriod;
   }

   public Collection<String> getSharingGroups() {
      return buildGroupMap(CsiSecurityManager.getUserRoles().toArray(new String[0])).keySet();
   }

   public boolean isHiddenFromGroups() {
      return !getHideFromGroupMap().isEmpty();
   }

   public boolean isRestrictedToGroups() {
      return !getRestrictToGroupMap().isEmpty();
   }

   public boolean isHiddenFrom(String groupIn) {
      return getHideFromGroupMap().containsKey(groupIn.toLowerCase());
   }

   public boolean isRestrictedTo(String groupIn) {
      return getRestrictToGroupMap().containsKey(groupIn.toLowerCase());
   }

   public Map<String,String> getHideFromGroupMap() {
      if (hideFromGroupMap == null) {
         if ((hideFromGroups != null) && (hideFromGroups.length() > 0)) {
            String[] myGroups = hideFromGroups.split("\\|");
            hideFromGroupMap = buildGroupMap(myGroups);
         } else {
            hideFromGroupMap = new HashMap<String,String>();
         }
      }
      return hideFromGroupMap;
   }

   public Map<String,String> getRestrictToGroupMap() {
      if (restrictToGroupMap == null) {
         if ((restrictToGroups != null) && (restrictToGroups.length() > 0)) {
            String[] myGroups = restrictToGroups.split("\\|");
            restrictToGroupMap = buildGroupMap(myGroups);
         } else {
            restrictToGroupMap = new HashMap<String,String>();
         }
      }
      return restrictToGroupMap;
   }

   public void resetGroupMaps() {
      hideFromGroupMap = null;
      restrictToGroupMap = null;
   }

   private Map<String,String> buildGroupMap(String[] groupsIn) {
      Map<String,String> myMap = new HashMap<String,String>();
      ArrayDeque<String> pendingList = new ArrayDeque<String>();
      String myEveryoneRole = EVERYONE_GROUP.toLowerCase();

      for (String myGroup : groupsIn) {
         String myRole = myGroup.trim().toLowerCase();

         if (!myEveryoneRole.equals(myRole) && !myMap.containsKey(myRole)) {
            myMap.put(myRole, myRole);
            pendingList.push(myRole);
         }
      }
      while (!pendingList.isEmpty()) {
         String name = pendingList.pop();
         List<String> myList = Users.getAllGroupNamesInGroup(GroupType.SHARING, name);

         for (String myRole : myList) {
            if (!myEveryoneRole.equals(myRole) && !myMap.containsKey(myRole)) {
               myMap.put(myRole, myRole);
               pendingList.push(myRole);
            }
         }
      }
      return myMap;
   }
}
