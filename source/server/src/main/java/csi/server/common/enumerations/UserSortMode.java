package csi.server.common.enumerations;

import csi.server.common.interfaces.SortingEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 2/26/2016.
 */
public enum UserSortMode implements SortingEnum<UserSortMode> {

    USERNAME_ASC("username ascending", "name", "asc"),
    USERNAME_DESC("username descending", "name", "desc"),
    FIRST_NAME_ASC("first name ascending", "firstName", "asc"),
    FIRST_NAME_DESC("first name descending", "firstName", "desc"),
    LAST_NAME_ASC("last name ascending", "lastName", "asc"),
    LAST_NAME_DESC("last name descending", "lastName", "desc"),
    EMAIL_ASC("e-mail address ascending", "email", "asc"),
    EMAIL_DESC("e-mail address descending", "email", "desc");

    private static List<UserSortMode> _partnerList = null;
    private static String[] _i18nLabels = null;
    private static String[] _columns = null;

    private String _label;
    private String _column;
    private String _direction;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private UserSortMode(String labelIn, String columnIn, String directionIn) {

        _label = labelIn;
        _column = columnIn;
        _direction = directionIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public static String[] getColumns() {

        if (null == _columns) {

            _columns = new String[values().length / 2];
            for (int i = 0; _columns.length > i; i++) {

                _columns[i] = values()[(2 * i)]._column;
            }
        }
        return _columns;
    }

    public String getColumn() {

        return _column;
    }

    public String getDirection() {

        return _direction;
    }

    public UserSortMode getPartner() {

        return getPartnerList().get(ordinal());
    }

    public static List<UserSortMode> list() {

        List<UserSortMode> myList = new ArrayList<UserSortMode>(UserSortMode.values().length);
        UserSortMode[] myValues = UserSortMode.values();

        for (int i = 0; myValues.length > i; i++) {

            myList.add(myValues[i]);
        }
        return myList;
    }

    private static List<UserSortMode> getPartnerList() {

        if (null == _partnerList) {

            _partnerList = new ArrayList<UserSortMode>(UserSortMode.values().length);

            for (int i = 0; values().length >i; i += 2) {

                _partnerList.add(values()[i + 1]);
                _partnerList.add(values()[i]);
            }
        }

        return _partnerList;
    }
}
