package csi.server.common.enumerations;

import csi.server.common.interfaces.SortingEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 2/26/2016.
 */
public enum ResourceSortMode implements SortingEnum<ResourceSortMode> {

    CREATE_DATE_ASC("creation date ascending", "d.createDate", "asc"),
    CREATE_DATE_DESC("creation date descending", "d.createDate", "desc"),
    ACCESS_DATE_ASC("access date ascending", "d.lastOpenDate", "asc"),
    ACCESS_DATE_DESC("access date descending", "d.lastOpenDate", "desc"),
    OWNER_ALPHA_ASC("owner username ascending", "LOWER(a.owner)", "asc"),
    OWNER_ALPHA_DESC("owner username descending", "LOWER(a.owner)", "desc"),
    NAME_ALPHA_ASC("resource name ascending", "LOWER(d.name)", "asc"),
    NAME_ALPHA_DESC("resource name descending", "LOWER(d.name)", "desc");

    private static List<ResourceSortMode> _partnerList = null;
    private static String[] _i18nLabels = null;

    private String _label;
    private String _column;
    private String _direction;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    private ResourceSortMode(String labelIn, String columnIn, String directionIn) {

        _label = labelIn;
        _column = columnIn;
        _direction = directionIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public String getColumn() {

        return _column;
    }

    public String getDirection() {

        return _direction;
    }

    public static int getPartnerOrdinal(int ordinalIn) {

        List<ResourceSortMode> myList = getPartnerList();

        return (myList.size() > ordinalIn) ? myList.get(ordinalIn).ordinal() : ordinalIn;
    }

    public ResourceSortMode getPartner() {

        return getPartnerList().get(ordinal());
    }

    public static List<ResourceSortMode> list() {

        List<ResourceSortMode> myList = new ArrayList<ResourceSortMode>(ResourceSortMode.values().length);
        ResourceSortMode[] myValues = ResourceSortMode.values();

        for (int i = 0; myValues.length > i; i++) {

            myList.add(myValues[i]);
        }
        return myList;
    }

    private static List<ResourceSortMode> getPartnerList() {

        if (null == _partnerList) {

            _partnerList = new ArrayList<ResourceSortMode>(ResourceSortMode.values().length);

            for (int i = 0; values().length >i; i += 2) {

                _partnerList.add(values()[i + 1]);
                _partnerList.add(values()[i]);
            }
        }

        return _partnerList;
    }
}
