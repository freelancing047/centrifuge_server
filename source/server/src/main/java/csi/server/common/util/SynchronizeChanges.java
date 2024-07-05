package csi.server.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;

/**
 * Created by centrifuge on 1/5/2015.
 */
public class SynchronizeChanges {
   private static boolean fieldNameIsCaseless = false;
   private static boolean resourceNameIsCaseless = true;

    public static DataViewDef getFieldDefProxy(DataViewDef metaDataIn) {
        DataViewDef myMetaProxy = new DataViewDef(ReleaseInfo.version);
        DataModelDef myModelProxy = new DataModelDef();
        FieldListAccess myFieldAccess = myModelProxy.getFieldListAccess();
        myMetaProxy.setModelDef(myModelProxy);
        if (null != metaDataIn) {
            DataModelDef myModel = metaDataIn.getModelDef();
            if (null != myModel) {
                List<FieldDef> myFieldList = myModel.getFieldDefs();
                if ((null != myFieldList) && !myFieldList.isEmpty()) {
                    for (FieldDef mySource : myFieldList) {
                        FieldDef myTarget = mySource.fullClone();
                        myFieldAccess.addFieldDef(myTarget);
                    }
                }
            }
            List<QueryParameterDef> myParameterList = metaDataIn.getDataSetParameters();
            if ((null != myParameterList) && !myParameterList.isEmpty()) {
                for (QueryParameterDef mySource : myParameterList) {
                    QueryParameterDef myTarget = mySource.fullClone();
                    myMetaProxy.addParameter(myTarget);
                }
            }
        }
        return myMetaProxy;
    }

   public static Exception updateFieldList(DataViewDef metaDataIn, List<FieldDef> listIn) {
      try {
         FieldListAccess model = metaDataIn.getModelDef().getFieldListAccess();
         List<FieldDef> myList = Update.updateListInPlace(model.getFieldDefList(), listIn);
         Map<Integer,FieldDef> myMap = new TreeMap<Integer,FieldDef>();

         for (FieldDef myField : myList) {
             myMap.put(Integer.valueOf(myField.getOrdinal()), myField);
         }
         model.setFieldDefList(new ArrayList<FieldDef>(myMap.values()));
         model.clearDirtyFlags();
         model.resetMaps();
         return null;
      } catch (Exception myException) {
         return myException;
      }
   }

    private static Exception addToFieldList(DataViewDef metaDataIn, FieldDef fieldIn) {
        try {
            if (null != fieldIn) {
                metaDataIn.getModelDef().getFieldListAccess().addFieldDef(fieldIn);
            }
            return null;
        } catch (Exception myException) {
            return myException;
        }
    }

    private static Exception removeFromFieldList(DataViewDef metaDataIn, FieldDef fieldIn) {
        try {
            if (null != fieldIn) {
                metaDataIn.getModelDef().getFieldListAccess().removeFieldDef(fieldIn);
            }
            return null;
        } catch (Exception myException) {
            return myException;
        }
    }

    public static Exception addToLinkupList(DataViewDef metaDataIn, LinkupMapDef linkupIn, List<FieldDef> newFieldsIn) {
        try {
            if (null != newFieldsIn) {
                for (FieldDef fieldDef : newFieldsIn) {
                    addToFieldList(metaDataIn, fieldDef);
                }
            }
            List<LinkupMapDef> myMergedList = metaDataIn.getLinkupDefinitions();
            linkupIn.setOrdinal(myMergedList.size());
            metaDataIn.addLinkup(linkupIn);
            return null;
        } catch (Exception myException) {
            return myException;
        }
    }

    public static Exception removeFromLinkupList(DataViewDef metaDataIn, LinkupMapDef linkupIn, List<FieldDef> discardedFieldsIn) {
        try {
            List<LinkupMapDef> myMergedList = metaDataIn.getLinkupDefinitions();
            myMergedList.remove(linkupIn);
            removeLinkupDiscardedFields(metaDataIn, discardedFieldsIn);
            int howMany = myMergedList.size();

            for (int i = 0; i < howMany; i++) {
                myMergedList.get(i).setOrdinal(i);
            }
            return null;
        } catch (Exception myException) {
            return myException;
        }
    }

    public static Exception removeLinkup(DataViewDef metaDataIn, LinkupMapDef linkupIn) {
        try {
            List<LinkupMapDef> myMergedList = metaDataIn.getLinkupDefinitions();
            myMergedList.remove(linkupIn);
            int howMany = myMergedList.size();

            for (int i = 0; i < howMany; i++) {
                myMergedList.get(i).setOrdinal(i);
            }
            return null;
        } catch (Exception myException) {
            return myException;
        }
    }

    public static void removeLinkupDiscardedFields(DataViewDef metaDataIn, List<FieldDef> discardedFieldsIn) {
        if (null != discardedFieldsIn) {
            for (FieldDef fieldDef : discardedFieldsIn) {
                removeFromFieldList(metaDataIn, fieldDef);
            }
        }
    }

    public static String guaranteeResourceName(String nameIn, List<String> rejectListIn) {
        return guaranteeResourceName(nameIn, createResourceNameMap(rejectListIn));
    }

    public static String guaranteeResourceName(String nameIn, Map<String, ?> rejectMapIn) {
        String myName = null;
        String myPattern = "<unnamed {:0}>";
        if (null != nameIn) {
            String myTrimmedName = nameIn.trim();
            if ((null != rejectMapIn) && !rejectMapIn.isEmpty() && rejectMapIn.containsKey(myTrimmedName.toLowerCase())) {
                myPattern = myTrimmedName + " {:0}";
            } else {
                myName = myTrimmedName;
            }
        }
        return (null != myName) ? myName : generateName(myPattern, rejectMapIn, resourceNameIsCaseless);
    }

    public static String fieldName(String nameIn, List<String> rejectListIn) {
        return uniqueVariation(((null != nameIn) && (0 < nameIn.length())) ? nameIn : "extra_field", rejectListIn, fieldNameIsCaseless);
    }

    public static String fieldName(String nameIn, Map<String, ?> rejectMapIn) {
        return uniqueVariation(((null != nameIn) && (0 < nameIn.length())) ? nameIn : "extra_field", rejectMapIn, fieldNameIsCaseless);
    }

    private static String uniqueVariation(String nameIn, List<String> rejectListIn, boolean caselessCheckIn) {
        String myName = (null != nameIn) ? nameIn : "_";
        String myTest = (caselessCheckIn) ? myName.toLowerCase() : myName;
        Map<String, Object> myRejectMap = createStringMap(rejectListIn, caselessCheckIn);
        // Only generate a unique name if the name is a conflict
        if ((myRejectMap != null) && (myRejectMap.containsKey(myTest))) {
            myName = generateName(myName + "_{:0}", myRejectMap, caselessCheckIn);
        }
        return myName;
    }

    private static String uniqueVariation(String nameIn, Map<String, ?> rejectMapIn, boolean caselessCheckIn) {
        String myName = (null != nameIn) ? nameIn : "_";
        String myTest = (caselessCheckIn) ? myName.toLowerCase() : myName;
        // Only generate a unique name if the name is a conflict
        if (rejectMapIn.containsKey(myTest)) {
            myName = generateName(myName + "_{:0}", rejectMapIn, caselessCheckIn);
        }
        return myName;
    }

    public static String generateName(String formatIn, List<String> rejectListIn, boolean caselessCheckIn) {
        return generateName(formatIn, createStringMap(rejectListIn, caselessCheckIn), caselessCheckIn);
    }

    public static String generateName(String formatIn, Map<String, ?> rejectMapIn, boolean caselessCheckIn) {
        String myName;
        if ((null != rejectMapIn) && !rejectMapIn.isEmpty()) {
            if (caselessCheckIn) {
//                String myFormat = formatIn.toLowerCase();
                String myTest;
                for (int i = 1; ; i++) {
                    myTest = formatIn.replace("{:0}", Integer.toString(i)).toLowerCase();
                    if (!rejectMapIn.containsKey(myTest)) {
                        myName = formatIn.replace("{:0}", Integer.toString(i));
                        break;
                    }
                }
            } else {
                for (int i = 1; ; i++) {
                    myName = formatIn.replace("{:0}", Integer.toString(i));
                    if (!rejectMapIn.containsKey(myName)) {
                        break;
                    }
                }
            }
        } else {
            myName = formatIn.replace("{:0}", Integer.toString(0));
        }
        return myName;
    }

    public static Map<String, Object> createResourceNameMap(List<String> listIn) {
        return createStringMap(listIn, resourceNameIsCaseless);
    }

    public static Map<String, Object> createFieldDefNameMap(List<String> listIn) {
        return createStringMap(listIn, fieldNameIsCaseless);
    }

    private static Map<String, Object> createStringMap(List<String> listIn, boolean caselessCheckIn) {
        Map<String, Object> myMap = null;
        if ((null != listIn) && !listIn.isEmpty()) {
            myMap = new HashMap<>();
            if (caselessCheckIn) {
                for (String myItem : listIn) {
                    if (myItem != null) {
                        myMap.put(myItem.toLowerCase(), null);
                    }
                }
            } else {
                for (String myItem : listIn) {
                    if (myItem != null) {
                        myMap.put(myItem, null);
                    }
                }
            }
        }
        return myMap;
    }

    public static void updateSecurity(Resource resourceIn, CapcoInfo capcoIn, SecurityTagsInfo tagsIn) {
        if (null != resourceIn) {
            CapcoInfo myCapco;
            SecurityTagsInfo myTags;
            if (resourceIn instanceof DataView) {
                myCapco = ((DataView) resourceIn).getMeta().getCapcoInfo();
                myTags = ((DataView) resourceIn).getMeta().getSecurityTagsInfo();
                if ((null != myCapco) && (null != capcoIn)) {
                    myCapco.updateInPlace(capcoIn);
                } else {
                    ((DataView) resourceIn).getMeta().setCapcoInfo(capcoIn);
                }
                if ((null != myTags) && (null != tagsIn)) {
                    myTags.updateInPlace(tagsIn);
                } else {
                    ((DataView) resourceIn).getMeta().setSecurityTagsInfo(tagsIn);
                }
            } else {
                myCapco = resourceIn.getCapcoInfo();
                myTags = resourceIn.getSecurityTagsInfo();
                setCapcoInfo(resourceIn, capcoIn, tagsIn, myCapco, myTags);
            }
        }
    }

    public static void setCapcoInfo(Resource resourceIn, CapcoInfo capcoIn, SecurityTagsInfo tagsIn, CapcoInfo myCapco, SecurityTagsInfo myTags) {
        if ((null != myCapco) && (null != capcoIn)) {
            myCapco.updateInPlace(capcoIn);
        } else {
            resourceIn.setCapcoInfo(capcoIn);
        }
        if ((null != myTags) && (null != tagsIn)) {
            myTags.updateInPlace(tagsIn);
        } else {
            resourceIn.setSecurityTagsInfo(tagsIn);
        }
    }
}
