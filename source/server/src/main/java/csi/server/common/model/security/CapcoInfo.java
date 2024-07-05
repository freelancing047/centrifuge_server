package csi.server.common.model.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CapcoSource;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 5/11/2015.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CapcoInfo extends ModelObject {
/*
    private static String _nofornTag1 = "NOFORN";
    private static String _nofornTag2 = "NOT RELEASABLE TO FOREIGN NATIONALS";
*/
    private String banner = null;
    private String abbreviation = null;
    private String portion = null;
    private String dataPortion = null;
    private String userPortion = null;
    private String fieldString = null;
    private CapcoSource mode = CapcoSource.USE_DEFAULT;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CapcoInfo next = null;

    public static CapcoInfo genFallBack(String defaultIn) {

        return new CapcoInfo(CapcoSource.USER_ONLY, defaultIn, null);
    }

    public static String appendDissemination(String bannerIn, String tagDataIn) {

        String myBanner = bannerIn;

        if (null != bannerIn) {

            if (null != tagDataIn) {

                myBanner = myBanner + "//" + tagDataIn;
            }

        } else if (null != tagDataIn) {

            myBanner = tagDataIn;
        }
        return myBanner;
    }

    public CapcoInfo() {

    }

    public CapcoInfo(CapcoSource modeIn) {

        mode = modeIn;
    }

    public CapcoInfo(CapcoSource modeIn, String userPortionIn, String fieldStringIn) {

        mode = modeIn;
        userPortion = userPortionIn;
        fieldString = fieldStringIn;
    }

    public void reset() {

        reset(false);
    }

    public void reset(boolean clearDataPortion) {

        banner = null;
        portion = null;
        abbreviation = null;
        if (clearDataPortion) {

            dataPortion = null;
        }
        if (null != next) {

            next.reset(clearDataPortion);
        }
    }

    public String getBanner() {

        return banner;
    }

    public void setBanner(String bannerIn) {

        banner = bannerIn;
    }

    public String getPortion() {

        return portion;
    }

    public void setPortion(String portionIn) {

        portion = portionIn;
    }

    public String getUserPortion() {

        return userPortion;
    }

    public void setUserPortion(String userPortionIn) {

        userPortion = userPortionIn;
    }

    public String getDataPortion() {

        return dataPortion;
    }

    public void setDataPortion(String dataPortionIn) {

        dataPortion = dataPortionIn;
    }

    public String getAbbreviation() {

        return abbreviation;
    }

    public void setAbbreviation(String abbreviationIn) {

        abbreviation = abbreviationIn;
    }

    public String getFieldString() {

        return fieldString;
    }

    public void setFieldString(String fieldStringIn) {

        fieldString = fieldStringIn;
    }

    public CapcoSource getMode() {

        return mode;
    }

    public void setMode(CapcoSource modeIn) {

        mode = modeIn;
    }

    public CapcoInfo getNext() {

        return next;
    }

    public void setNext(CapcoInfo nextIn) {

        next = nextIn;
    }

   public String[] getSecurityFieldArray() {
      List<String> myList = getSecurityFields();

      return ((myList != null) && !myList.isEmpty()) ? myList.toArray(new String[0]) : null;
   }

    public List<String> getSecurityFields() {

        List<String> myList = null;

        if ((null != fieldString) && (0 < fieldString.length())) {

            myList = new ArrayList<String>();

            String[] myArray = StringUtil.split(fieldString);

            for (int i = 0; myArray.length > i; i++) {

                String myEntry = myArray[i];

                if (0 < myEntry.length()) {

                    myList.add(myEntry);
                }
            }
        }
        return myList;
    }

    public boolean isCapcoField(FieldDef fieldIn) {

        Set<String> mySet = getSecuritySet();

        if ((null != fieldIn) && (null != mySet) && !mySet.isEmpty()) {

            String myKey = fieldIn.getColumnLocalId();

            if ((null != myKey) && (mySet.contains(myKey))) {

                return true;

            } else {

                myKey = fieldIn.getLocalId();

                if ((null != myKey) && (mySet.contains(myKey))) {

                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getSecuritySet() {

        Set<String> myList = null;

        if ((null != fieldString) && (0 < fieldString.length())) {

            myList = new TreeSet<>();

            String[] myArray = StringUtil.split(fieldString);

            for (int i = 0; myArray.length > i; i++) {

                String myEntry = myArray[i];

                if (0 < myEntry.length()) {

                    myList.add(myEntry);
                }
            }
        }
        return myList;
    }

   public void setSecurityFields(List<String> listIn) {
      fieldString = null;

      if ((listIn != null) && !listIn.isEmpty()) {
         fieldString = listIn.stream().filter(Objects::nonNull).collect(Collectors.joining("|"));
      }
   }

   public void setSecurityFields(Set<String> listIn) {
      fieldString = null;

      if ((listIn != null) && !listIn.isEmpty()) {
         fieldString = listIn.stream().filter(Objects::nonNull).collect(Collectors.joining("|"));
      }
   }

    public CapcoInfo clone() {

        CapcoInfo myClone = new CapcoInfo();

        cloneComponents(myClone);

        return myClone;
    }

    public CapcoInfo fullClone() {

        CapcoInfo myClone = new CapcoInfo();

        fullCloneComponents(myClone);

        return myClone;
    }

    protected void fullCloneComponents(CapcoInfo cloneIn) {

        super.fullCloneComponents(cloneIn);
        cloneLocalComponents(cloneIn);
    }

    protected void cloneComponents(CapcoInfo cloneIn) {

        super.cloneComponents(cloneIn);
        cloneLocalComponents(cloneIn);
    }

    protected void cloneLocalComponents(CapcoInfo cloneIn) {

        cloneIn.setBanner(getBanner());
        cloneIn.setAbbreviation(getAbbreviation());
        cloneIn.setPortion(getPortion());
        cloneIn.setDataPortion(getDataPortion());
        cloneIn.setUserPortion(getUserPortion());
        cloneIn.setFieldString(getFieldString());
        cloneIn.setMode(getMode());
        if (null != next) {

            cloneIn.setNext(next.clone());
        }
    }

    public void updateInPlace(CapcoInfo sourceIn) {

        sourceIn.fullCloneComponents(this);
    }

    public void removeAllLinkupInfo() {

        if (null != next) {

            next.removeAllLinkupInfo();
            next = null;
        }
    }

    public CapcoInfo addLinkupInfo(CapcoInfo capcoIn) {

        if (null != next) {

            return next.addLinkupInfo(capcoIn);

        } else {

            next = capcoIn;
        }
        return next;
    }

    public CapcoInfo genForLinkup(List<String> fieldListIn) {

        CapcoInfo myClone = clone();

        myClone.reset();
        myClone.setSecurityFields(fieldListIn);
        return myClone;
    }

    public void lockResults() {

        mode = CapcoSource.USER_ONLY;
        dataPortion = null;
        userPortion = portion;
        fieldString = null;
        next = null;
    }
}
