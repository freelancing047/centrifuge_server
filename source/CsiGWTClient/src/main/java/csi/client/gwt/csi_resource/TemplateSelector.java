package csi.client.gwt.csi_resource;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.enumerations.ResourceChoiceCriteria;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.ValuePair;

import java.util.*;

/**
 * Created by centrifuge on 4/30/2019.
 */
public class TemplateSelector extends ResourceSelectorDialog {

    private MainPresenter _mainPresenter = null;
    private CanBeShownParent _parent;
    private Map<String, DataViewDef> _templateMap = new TreeMap<String, DataViewDef>();
    private DataViewDef _selection = null;

    public TemplateSelector(CanBeShownParent parentIn, List<ValuePair<DataViewDef, Integer>> listIn,
                            DataViewDef templateIn, Integer criteriaIn) {

        super(ResourceSelectorPanel.SelectorMode.READ_ONLY);
        _parent = parentIn;

        List<ResourceBasics> myList = new ArrayList<ResourceBasics>();
        String myUser = getMainPresenter().getUserName();
        String mySelectedId = (null != templateIn) ? templateIn.getUuid() : null;

        if ((null != listIn) && (0 < listIn.size())) {

            for (ValuePair<DataViewDef, Integer> myPair : listIn) {

                DataViewDef myTemplate = myPair.getValue1();
                Integer myCriteria = myPair.getValue2();
                String myUuid = myTemplate.getUuid();
                String myName = myTemplate.getName();
                String myRemarks = ((null != myCriteria) && (0 != myCriteria))
                        ? ResourceChoiceCriteria.expand(myCriteria)
                        : myTemplate.getRemarks();

                _templateMap.put(myUuid, myTemplate);
                myList.add(new ResourceBasics(myUuid, myName, myRemarks, myTemplate.getLastOpenDate(),
                                                myTemplate.getOwner(), myTemplate.getSize(), myUser));

                if (myUuid.equals(mySelectedId)) {

                    mySelectedId = null;
                }
            }
        }
        if (null != templateIn) {

            DataViewDef myTemplate = templateIn;
            Integer myCriteria = criteriaIn;
            String myUuid = myTemplate.getUuid();
            String myName = myTemplate.getName();
            String myRemarks = ((null != myCriteria) && (0 != myCriteria))
                    ? ResourceChoiceCriteria.expand(myCriteria)
                    : myTemplate.getRemarks();

            if (null != mySelectedId) {

                _templateMap.put(myUuid, myTemplate);
                myList.add(new ResourceBasics(myTemplate.getUuid(), myTemplate.getName(), myRemarks,
                        myTemplate.getLastOpenDate(), myTemplate.getOwner(), myTemplate.getSize(), myUser));
                setSingleList(myList);
                myList = null;
            }
            setDefaultChoice(myName);
        }
        if ((null != myList) && (0 < myList.size())) {

            setSingleList(myList);
        }
    }

    public DataViewDef getTemplate() {

        return _selection;
    }

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverWriteIn) {

        String mySelectionKey = (null != selectionIn) ? selectionIn.getUuid() : null;

        _selection = (null != mySelectionKey) ? _templateMap.get(mySelectionKey) : null;
        _parent.showWithResults((KnowsParent)null);
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}
