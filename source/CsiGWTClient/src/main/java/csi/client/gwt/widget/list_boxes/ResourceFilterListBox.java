package csi.client.gwt.widget.list_boxes;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;

/**
 * Created by centrifuge on 3/2/2016.
 */
public class ResourceFilterListBox extends CsiStringListBox {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _none = i18n.noResourceFilter();
    private static final String _adHoc = i18n.adHocResourceFilter();
    private static final String _noneRemarks = i18n.noResourceFilterRemarks();
    private static final String _adHocRemarks = i18n.adHocResourceFilterRemarks();

    private ClickHandler _updateCallback = null;
    private List<OptionBasics> _filterList;
    private String _chosenFilter = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceFilterListBox() {

        WebMain.injector.getMainPresenter().getResourceFilterDisplayList(filterListCallBack);
        initializeDisplay();
    }

    public void updatetList(ResourceFilter filterIn, ClickHandler callbackIn) {

        _chosenFilter = filterIn.getName();
        _updateCallback = callbackIn;
        WebMain.injector.getMainPresenter().addReplaceResourceFilter(filterIn, newFilterCallBack);
    }

    public List<OptionBasics> getFilterList() {

        return _filterList;
    }

    public ResourceFilter getSelectedItem() {

        int mySelection = getSelectedIndex();


        if (1 == mySelection) {

            return new ResourceFilter(true);

        } else if ((1 < mySelection) && ((_filterList.size() + 2) > mySelection)) {

            return WebMain.injector.getMainPresenter().getResourceFilter(_filterList.get(mySelection - 2));

        } else {

            return null;
        }
    }

    private Callback<List<OptionBasics>> filterListCallBack = new Callback<List<OptionBasics>>() {

        @Override
        public void onSuccess(List<OptionBasics> filterListIn) {

            try {

                loadFilters(filterListIn, null);

            } catch (Exception myException) {

                Dialog.showException("MainPresenter", myException);
            }
        }
    };

    private Callback<List<OptionBasics>> newFilterCallBack = new Callback<List<OptionBasics>>() {

        @Override
        public void onSuccess(List<OptionBasics> filterListIn) {

            try {

                loadFilters(filterListIn, _chosenFilter);
                _chosenFilter = null;
                _updateCallback.onClick(null);

            } catch (Exception myException) {

                Dialog.showException("MainPresenter", myException);
            }
        }
    };

    private void loadFilters(List<OptionBasics> listIn, String filterIn) {

        int myIndex = 2;
        int mySelectedIndex = 0;

        _filterList = (null != listIn) ? listIn : new ArrayList<OptionBasics>();
        initializeDisplay();
        for (OptionBasics myFilter : _filterList) {

            String myRemarks = myFilter.getRemarks();

            if ((null != myRemarks) && (0 < myRemarks.length())) {

                addTitledItem(myFilter.getDisplayName() + "\n" + myRemarks, myFilter.getDisplayName());

            } else {

                addTitledItem(myFilter.getDisplayName(), myFilter.getDisplayName());
            }
            if ((null != filterIn) && filterIn.equals(myFilter.getName())) {

                mySelectedIndex = myIndex;
            }
            if ((0 == mySelectedIndex) && (myFilter.getDefaultOption())) {

                mySelectedIndex = myIndex;
            }
            myIndex++;
        }
        if (0 < mySelectedIndex) {

            setSelectedIndex(mySelectedIndex);
        }
    }

    private void initializeDisplay() {

        clear();
        addTitledItem(_noneRemarks, _none);
        addTitledItem(_adHocRemarks, _adHoc);
    }

    public String getAdhocFilter(){
        return _adHoc;
    }
}
