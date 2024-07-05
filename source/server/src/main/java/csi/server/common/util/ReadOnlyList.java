package csi.server.common.util;

import java.util.AbstractList;
import java.util.List;

/**
 * Created by centrifuge on 11/20/2018.
 */
public class ReadOnlyList<T> extends AbstractList<T> {

    List<T> _baseList;

    public ReadOnlyList(List<T> listIn) {

        _baseList = listIn;
    }

    @Override
    public final T get(int indexIn) {

        return _baseList.get(indexIn);
    }

    @Override
    public int size() {

        return _baseList.size();
    }
}
