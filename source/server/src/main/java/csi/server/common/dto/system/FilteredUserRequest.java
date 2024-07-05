package csi.server.common.dto.system;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.UserSortMode;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 3/13/2018.
 */
public class FilteredUserRequest implements IsSerializable {

    public enum Pattern {

        USERNAME,
        FIRST_NAME,
        LAST_NAME,
        EMAIL
    }
    private String[][] _patterns = null;
    private UserSortMode[] _sorts = null;
    private boolean _restricted = false;
    private int[] _masks = null;

    public FilteredUserRequest() {

    }

    public FilteredUserRequest(int[] masksIn, String[][] patternsIn, UserSortMode[] sortsIn, boolean restrictedIn) {

        int myLimit = ((null != masksIn) && (null != patternsIn)) ? Math.min(masksIn.length, patternsIn.length) : 0;

        if (0 < myLimit) {

            _masks = new int[myLimit];
            myLimit = 0;
            for (int i = 0; _masks.length > i; i++) {

                _masks[i] = masksIn[i];
            }
            _patterns = new String[myLimit][];

            for (int i = 0; patternsIn.length > i; i++) {

                String[] myEntry = patternsIn[i];

                if ((null != myEntry) && (0 < myEntry.length)) {

                    _patterns[i] = new String[myEntry.length];
                    for (int j = 0; _patterns[i].length > j; j++) {

                        _patterns[i][j] = (null != myEntry[j]) ? StringUtil.patternToSql(myEntry[j].trim()) : null;
                    }
                }
            }
        }
        if ((null != sortsIn) && (0 < sortsIn.length)) {

            _sorts = new UserSortMode[sortsIn.length];
            for (int i = 0; _sorts.length > i; i++) {

                _sorts[i] = sortsIn[i];
            }
        }
    }

    public void setMasks(int[] masksIn) {

        _masks = masksIn;
    }

    public int[] getMasks() {

        return _masks;
    }

    public void setPatterns(String[][] patternsIn) {

        _patterns = patternsIn;
    }

    public String[][] getPatterns() {

        return _patterns;
    }

   public void setPatternDisplays(String[][] patternsIn) {
      int myOuterLimit = (null != patternsIn) ? patternsIn.length : 0;
      _patterns = (0 < myOuterLimit) ? new String[myOuterLimit][] : null;

      if (_patterns != null) {
         for (int i = 0; myOuterLimit > i; i++) {
            String[] myInput = patternsIn[i];

            if (null != myInput) {
               int myInnerLimit = myInput.length;
               String[] myOutput = new String[myInnerLimit];

               for (int j = 0; myInnerLimit > j; j++) {
                  myOutput[j] = (null != myInput[j]) ? StringUtil.patternToSql(myInput[j].trim()) : null;
               }
               _patterns[i] = myOutput;
            }
         }
      }
   }

   public String[][] getPatternDisplays() {
      int myOuterLimit = (null != _patterns) ? _patterns.length : 0;
      String[][] myDisplays = (0 < myOuterLimit) ? new String[myOuterLimit][] : null;

      if (myDisplays != null) {
         for (int i = 0; myOuterLimit > i; i++) {
            String[] myInput = _patterns[i];

            if (null != myInput) {
               int myInnerLimit = myInput.length;
               String[] myOutput = new String[myInnerLimit];

               for (int j = 0; myInnerLimit > j; j++) {
                  myOutput[j] = StringUtil.patternFromSql( myInput[j]);
               }
               myDisplays[i] = myOutput;
            }
         }
      }
      return myDisplays;
   }

    public void setSorts(UserSortMode[] sortsIn) {

        _sorts = sortsIn;
    }

    public UserSortMode[] getSorts() {

        return _sorts;
    }

    public void setRestricted(boolean restrictedIn) {

        _restricted = restrictedIn;
    }

    public boolean getRestricted() {

        return _restricted;
    }
}
