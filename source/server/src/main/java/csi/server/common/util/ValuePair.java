package csi.server.common.util;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 3/27/2015.
 */
public class ValuePair<S, T> implements IsSerializable {

    private S _value1;
    private T _value2;

    public ValuePair() {

        this(null, null);
    }

    public ValuePair(S value1In, T value2In) {

        _value1 = value1In;
        _value2 = value2In;
    }

    public void setValue1(S value1In) {

        _value1 = value1In;
    }

    public void setValue2(T value2In) {

        _value2 = value2In;
    }

    public void setValuePair(S value1In, T value2In) {

        _value1 = value1In;
        _value2 = value2In;
    }

    public S getValue1() {

        return _value1;
    }

    public T getValue2() {

        return _value2;
    }
}
