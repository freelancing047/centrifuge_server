package csi.security.monitors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import csi.config.Configuration;
import csi.security.queries.Users;
import csi.server.business.helper.QueryHelper;
import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.CapcoGroup;
import csi.server.common.model.security.CapcoInfo;
import csi.server.security.AbstractCapcoTagProcessing;
import csi.server.security.CapcoClassTags;
import csi.server.security.CapcoFgiTags;
import csi.server.util.SqlUtil;

/**
 * Created by centrifuge on 4/10/2015.
 */
public class CapcoRollup {
   private static final String UNCLASSIFIED_PORTION = "U";
   private static final String UNCLASSIFIED_BANNER = "UNCLASSIFIED";

   private static List<Map<String, String>> _aclMapList;

   static {
      refreshRoleMap();
   }

   private String[] _portionSplit;
   private String[] _activeSplit;
   private Map<String,?> _portionMap;
   private List<AbstractCapcoTagProcessing> _rollupMap;
   private Set<String> _unrecognizedTags;

   private CapcoInfo _info;
   private String _basePortion;
   private boolean _acceptUnrecognizedSci;

   public CapcoRollup(CapcoInfo infoIn) {
      _info = infoIn;
      reset();
   }

   public CapcoRollup(CapcoInfo infoIn, String basePortionIn) {
      this(infoIn);
      _basePortion = basePortionIn;
   }

   public CapcoRollup(CapcoInfo infoIn, boolean acceptUnrecognizedSciIn) {
      this(infoIn);
      _acceptUnrecognizedSci = acceptUnrecognizedSciIn;
   }

   public CapcoRollup(CapcoInfo infoIn, String basePortionIn, boolean acceptUnrecognizedSciIn) {
      this(infoIn, basePortionIn);
      _acceptUnrecognizedSci = acceptUnrecognizedSciIn;
   }

    public static CapcoRollup rollupDefault() {

        return (new CapcoRollup(new CapcoInfo(CapcoSource.USER_ONLY,
                Configuration.getInstance().getSecurityPolicyConfig().getDefaultPortion(), null))).finalizeRollup();
    }

    public static CapcoRollup rollupFailure() {

        return (new CapcoRollup(new CapcoInfo(CapcoSource.USER_ONLY,
                Configuration.getInstance().getSecurityPolicyConfig().getFailOverPortion(), null))).finalizeRollup();
    }

    public static CapcoRollup rollupPortions(Map<String, ?> mapIn) {

        return (new CapcoRollup(new CapcoInfo(CapcoSource.DATA_ONLY))).updateRollup(mapIn);
    }

    public static void refreshRoleMap() {

        _aclMapList = null;
    }

    public void setAccepUnrecognizedSci(boolean acceptUnrecognizedSciIn) {

        _acceptUnrecognizedSci = acceptUnrecognizedSciIn;
    }

    public boolean getAcceptUnrecognizedSci() {

        return _acceptUnrecognizedSci;
    }

    public CapcoInfo getCapcoInfo() {

        return _info;
    }

    public CapcoRollup updateRollup() {

        addPortion(_basePortion);
        createRollup();
        return this;
    }

    public CapcoRollup updateRollup(Map<String, ?> mapIn) {

        String myCurrentPortion = (null != _info) ? _info.getDataPortion() : null;
        _portionMap = mapIn;
        createRollup(myCurrentPortion);
        return this;
    }

    public boolean createRollup(String portionIn) {

        addPortion(portionIn);
        return createRollup();
    }

   public boolean createRollup() {
      boolean created = false;

      if ((_portionMap != null) && !_portionMap.isEmpty()) {
         resetRollup();
         rollupPortions();

         String[] capcoStrings = AbstractCapcoTagProcessing.genFullLabelSet(_rollupMap, _unrecognizedTags);

         _info.setDataPortion(capcoStrings[0]);
         _info.setBanner(capcoStrings[1]);
         _info.setAbbreviation(capcoStrings[2]);
         created = true;
      }
      return created;
   }

    public CapcoRollup finalizeRollup() {

        return finalizeRollup(false);
    }

    public CapcoRollup finalizeRollup(boolean forceDataRollup) {

        if (null != _info) {

            String[] myCapcoStrings;

            _portionMap = null;

            switch (_info.getMode()) {

                case USER_AND_DATA:

                    if (!forceDataRollup) {

                        addPortion(_info.getDataPortion());
                    }

                case USER_ONLY:

                    if (forceDataRollup) {

                        addPortion(_info.getDataPortion());
                    }
                    addPortion(_info.getUserPortion());
                    _rollupMap = null;
                    rollupPortions();
                    myCapcoStrings = AbstractCapcoTagProcessing.genFullLabelSet(_rollupMap, _unrecognizedTags);
                    _info.setPortion(myCapcoStrings[0]);
                    _info.setBanner(myCapcoStrings[1]);
                    _info.setAbbreviation(myCapcoStrings[2]);
                    break;

                case DATA_ONLY:

                    _info.setPortion(_info.getDataPortion());
                    break;

                case USE_DEFAULT:

                    if (forceDataRollup) {

                        addPortion(_info.getDataPortion());
                    }
                    addPortion(Configuration.getInstance().getSecurityPolicyConfig().getDefaultPortion());
                    _rollupMap = null;
                    rollupPortions();
                    myCapcoStrings = AbstractCapcoTagProcessing.genFullLabelSet(_rollupMap, _unrecognizedTags);
                    _info.setPortion(myCapcoStrings[0]);
                    _info.setBanner(myCapcoStrings[1]);
                    _info.setAbbreviation(myCapcoStrings[2]);
                    break;

                default:

                    break;
            }

            if (null == _info.getPortion()) {

                _info.setPortion(UNCLASSIFIED_PORTION);
                _info.setBanner(UNCLASSIFIED_BANNER);
                _info.setAbbreviation(UNCLASSIFIED_BANNER);
            }
        }
        return this;
    }

   public Set<String> getPositiveAclList() {
      Set<String> myList = new TreeSet<String>();

      if ((_info != null) && (_rollupMap != null)) {
         for (AbstractCapcoTagProcessing section : _rollupMap) {
            if ((CapcoSection.FGI != section.getEnum()) && (CapcoSection.DISM != section.getEnum())) {
               Collection<String> portionList = section.getPortionList();

               if ((portionList != null) && !portionList.isEmpty()) {
                  Map<String,String> map = getAclPortionMapping(section.getOrdinal());

                  for (String portion : portionList) {
                     String aclToken = map.get(portion);

                     if ((aclToken != null) && (aclToken.length() > 0)) {
                        myList.add(aclToken);
                     }
                  }
               }
            }
         }
      }
      return myList;
   }

   public Set<String> getNegativeAclList() {
      Set<String> myList = new TreeSet<String>();

      if ((_info != null) && (_rollupMap != null)) {
         myList.addAll(_rollupMap.get(CapcoSection.DISM.ordinal()).getPortionList());
      }
      return myList;
   }

    public void reset() {

        _portionMap = null;
        resetRollup();
    }

    public void captureMultipleCapcoColumns(Connection cacheConnectionIn, String queryTextIn, String portionIn) {

        ResultSet myResults = null;

        try {

            throw new CentrifugeException("Multiple CAPCO columns not supported.");

        } catch(Exception myException) {

            handleCaptureError();

        } finally {

            SqlUtil.quietCloseResulSet(myResults);
        }
    }

    public void captureCapcoColumn(Connection cacheConnectionIn, String queryTextIn, String portionIn) {

        ResultSet myResults = null;

        try {

            addPortion(portionIn);
            if ((null != queryTextIn) && (0 < queryTextIn.length())) {

                myResults = QueryHelper.executeSingleQuery(cacheConnectionIn, queryTextIn, null);

                while (SqlUtil.hasMoreRows(myResults)) {

                    String myPortionText = myResults.getString(1);

                    if (null != myPortionText) {

                        addPortion(myPortionText);
                    }
                }
            }

        } catch(Exception myException) {

            handleCaptureError();

        } finally {

            SqlUtil.quietCloseResulSet(myResults);
        }
    }

    public void setPortionMap(HashMap<String, Integer> portionMapIn) {

        _portionMap = portionMapIn;
    }

   private void handleCaptureError() {
      if (_info != null) {
         switch (_info.getMode()) {
            case USE_DEFAULT:
               String portion = Configuration.getInstance().getSecurityPolicyConfig().getDefaultPortion();

               if ((portion != null) && (portion.length() > 0)) {
                   addPortion(portion);
               }
               break;
            case USER_ONLY:
            case USER_AND_DATA:
               String userPortion = _info.getUserPortion();

               if (userPortion != null) {
                   addPortion(userPortion);
               }
               break;
            case DATA_ONLY:
               String dataPortion = _info.getDataPortion();

               if (dataPortion != null) {
                   addPortion(dataPortion);
               }
               break;
         }
      }
   }

    private void resetRollup() {

        if (null != _info) {

            _info.reset();

        } else {

            _info = new CapcoInfo();
        }
        _rollupMap = null;
    }

    private Map<String, ?> getPortionMap() {

        if (null == _portionMap) {

            _portionMap = new HashMap<String, Integer>();
        }
        return _portionMap;
    }

    private CapcoRollup rollupPortions() {

        _rollupMap = AbstractCapcoTagProcessing.getProcessingList();
        _unrecognizedTags = new TreeSet<String>();

        CapcoClassTags myClassExtractor = (CapcoClassTags)_rollupMap.get(CapcoSection.CLASS.ordinal());
        CapcoFgiTags myFgiRecorder = (CapcoFgiTags)_rollupMap.get(CapcoSection.FGI.ordinal());

        for (String myPortionString : getPortionMap().keySet()) {

            int myNext = 0;
            int myLastMatch = 0;

            _activeSplit = null;
            _portionSplit = myClassExtractor.extractClassificationTag(myPortionString, myFgiRecorder);
            boolean myClassifiedFlag = myClassExtractor.isClassified();
            String myBadClassification = myClassExtractor.getBadClassification();

            if (null != myBadClassification) {

                _unrecognizedTags.add(myBadClassification);
            }

            if (_portionSplit.length > myNext) {

                _activeSplit = _portionSplit[myNext].split("/");

                while ((null != _portionSplit) && (_portionSplit.length > myNext) && ((_rollupMap.size() - 1) > myLastMatch)) {

                    for (int i = myLastMatch + 1; (_rollupMap.size() > i) && (_portionSplit.length > myNext); i++) {

                        if (_rollupMap.get(i).parsePortionTag(_activeSplit, myClassifiedFlag, false)) {

                            myLastMatch = i;
                            myNext++;
                            if (_portionSplit.length > myNext) {

                                _activeSplit = _portionSplit[myNext].split("/");

                            } else {

                                _activeSplit = null;
                            }
                        }
                    }

                    if (_portionSplit.length > myNext) {

                        myLastMatch = forceLoad(myLastMatch, myNext++, myClassifiedFlag);

                        if (_portionSplit.length > myNext) {

                            _activeSplit = _portionSplit[myNext].split("/");
                        }
                    }
                }
            }
            AbstractCapcoTagProcessing.finalizeParsing(_rollupMap, myLastMatch, myClassifiedFlag);
        }
        return this;
    }

    private int forceLoad(int lastMatchIn, int nextIn, boolean isClassifiedIn) {

        int myFoundIndex = lastMatchIn;

        if (_acceptUnrecognizedSci && (0 == lastMatchIn)) {

            String[] mySplit = _portionSplit[nextIn].split("/");

            if (_rollupMap.get(1).parsePortionTag(mySplit, isClassifiedIn, true)) {

                myFoundIndex = 1;

            } else {

                _unrecognizedTags.add(_portionSplit[nextIn]);
            }

        } else {

            _unrecognizedTags.add(_portionSplit[nextIn]);
        }
        return myFoundIndex;
    }

    private void addPortion(String portionIn) {

        String myPortion = null;

        if ((null != portionIn) && (0 < portionIn.length())) {

            int myStart = portionIn.indexOf('(');
            int myStop = portionIn.indexOf(')');

            if (0 <= myStart) {

                if (0 <= myStop) {

                    myPortion = portionIn.substring(myStart + 1, myStop).trim();

                } else {

                    myPortion = portionIn.substring(myStart + 1).trim();
                }

            } else {

                myPortion = portionIn.trim();
            }
        }
        if ((null != myPortion) && (0 < myPortion.length())) {

            getPortionMap().put(myPortion, null);
        }
    }

    private static Map<String, String> getAclPortionMapping(int ordinalIn) {

        if ((0 <= ordinalIn) && ((CapcoSection.values().length - 1) > ordinalIn)) {

            return loadPortionMaps().get(ordinalIn);

        } else {

            return new HashMap<String, String>();
        }
    }

    private static List<Map<String, String>> loadPortionMaps() {

        if (null == _aclMapList) {

            _aclMapList = initializePortionMaps();

        }
        return _aclMapList;
    }

    private static List<Map<String, String>> initializePortionMaps() {

        if (null == _aclMapList) {

            List<CapcoGroup> myList = Users.getAllCapcoMappedRoles();

            _aclMapList = new ArrayList<Map<String, String>>();

            for (int i = 0; (CapcoSection.values().length - 1) > i; i++) {

                _aclMapList.add(new HashMap<String, String>());
            }

            if (null != myList) {

                for (CapcoGroup myRole : myList) {

                    int mySection = myRole.getSection().ordinal();

                    if ((0 <= mySection) && (_aclMapList.size() > mySection)) {

                        _aclMapList.get(mySection).put(myRole.getPortion(), myRole.getName());
                    }
                }
            }
        }
        return _aclMapList;
    }
}
