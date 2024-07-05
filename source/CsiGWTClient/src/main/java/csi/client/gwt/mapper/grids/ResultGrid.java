package csi.client.gwt.mapper.grids;

import java.util.List;
import java.util.Map;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.widget.gxt.drag_n_drop.IntegratedGrid;

/**
 * Created by centrifuge on 3/28/2016.
 */
public class ResultGrid<T1 extends SelectionDataAccess<?>, T2 extends SelectionDataAccess<?>>
        extends IntegratedGrid<SelectionPair<T1, T2>> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<String, T1> _leftSelected;
    protected Map<String, T2> _rightSelected;
    protected ModelBuilder<SelectionPair<T1, T2>> _modelBuilder;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResultGrid(ModelBuilder<SelectionPair<T1, T2>> modelBuilderIn, GridView<SelectionPair<T1, T2>> gridViewIn) {

        super(modelBuilderIn.genStore(), modelBuilderIn.genModel(), gridViewIn);
        _modelBuilder = modelBuilderIn;
    }

    public ResultGrid(ModelBuilder<SelectionPair<T1, T2>> modelBuilderIn) {

        super(modelBuilderIn.genStore(), modelBuilderIn.genModel());
        _modelBuilder = modelBuilderIn;
    }

    public ResultGrid<T1, T2> initializeGrid(Map<String, T1> leftSelectionIn, Map<String, T2> rightSelectionIn, List<? extends SelectionPair<T1, T2>> listIn) {

        ListStore<SelectionPair<T1, T2>> myGridStore = getStore();

        _leftSelected = leftSelectionIn;
        _rightSelected = rightSelectionIn;

        if ((null != _leftSelected) && (null != _rightSelected) && (null != listIn) && (0 < listIn.size())) {

            for (SelectionPair<T1, T2> myPair : listIn) {

                _leftSelected.put(myPair.getLeftKey(), myPair.getLeftData());
                _rightSelected.put(myPair.getRightKey(), myPair.getRightData());
                myGridStore.add(myPair);
            }
        }

        return this;
    }

    public ModelBuilder<SelectionPair<T1, T2>> getModelBuilder() {

        return _modelBuilder;
    }
}
