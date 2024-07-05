package csi.client.gwt.mapper.menus;

import java.util.ArrayList;
import java.util.List;

import com.sencha.gxt.data.shared.ListStore;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 3/22/2016.
 */
public class MappingSupport<S extends MappingItem, T extends MappingItem> {

    public List<ValuePair<S, T>> mapByExactName(ListStore<S> listOneIn, ListStore<T> listTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();
        List<T> myListTwo = copyList(listTwoIn);

        if ((null != listOneIn) && (null != myListTwo) && (0 < listOneIn.size()) && (0 < myListTwo.size())) {

            for (int i = 0; listOneIn.size() > i; i++) {

                S myItemOne = listOneIn.get(i);

                if (null != myItemOne) {

                    String myNameOne = myItemOne.getMappingName();
                    if (null != myNameOne) {

                        for (int j = 0; myListTwo.size() > j; j++) {

                            T myItemTwo = myListTwo.get(j);

                            if ((null != myItemOne) && myNameOne.equals(myItemTwo.getMappingName())) {

                                myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                                myListTwo.remove(j);
                                break;
                            }
                        }
                    }
                }
                if (0 == myListTwo.size()) {

                    break;
                }
            }
        }

        return myList;
    }

    public List<ValuePair<S, T>> mapByCaselessName(ListStore<S> listOneIn, ListStore<T> listTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();
        List<T> myListTwo = copyList(listTwoIn);

        if ((null != listOneIn) && (null != myListTwo) && (0 < listOneIn.size()) && (0 < myListTwo.size())) {

            for (int i = 0; listOneIn.size() > i; i++) {

                S myItemOne = listOneIn.get(i);

                if (null != myItemOne) {

                    String myNameOne = myItemOne.getMappingName();
                    if (null != myNameOne) {

                        for (int j = 0; myListTwo.size() > j; j++) {

                            T myItemTwo = myListTwo.get(j);

                            if ((null != myItemOne) & myNameOne.equalsIgnoreCase(myItemTwo.getMappingName())) {

                                myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                                myListTwo.remove(j);
                                break;
                            }
                        }
                    }
                }
                if (0 == myListTwo.size()) {

                    break;
                }
            }
        }

        return myList;
    }

    public List<ValuePair<S, T>> mapByType(ListStore<S> listOneIn, ListStore<T> listTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();
        List<T> myListTwo = copyList(listTwoIn);

        if ((null != listOneIn) && (null != myListTwo) && (0 < listOneIn.size()) && (0 < myListTwo.size())) {

            for (int i = 0; listOneIn.size() > i; i++) {

                S myItemOne = listOneIn.get(i);

                if (null != myItemOne) {

                    CsiDataType myTypeOne = myItemOne.getType();
                    if (null != myTypeOne) {

                        for (int j = 0; myListTwo.size() > j; j++) {

                            T myItemTwo = myListTwo.get(j);

                            if ((null != myItemOne) && myTypeOne.equals(myItemTwo.getType())) {

                                myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                                myListTwo.remove(j);
                                break;
                            }
                        }
                    }
                }
                if (0 == myListTwo.size()) {

                    break;
                }
            }
        }

        return myList;
    }

    public List<ValuePair<S, T>> mapByExactMatch(ListStore<S> listOneIn ,ListStore<T> listTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();
        List<T> myListTwo = copyList(listTwoIn);

        if ((null != listOneIn) && (null != myListTwo) && (0 < listOneIn.size()) && (0 < myListTwo.size())) {

            for (int i = 0; listOneIn.size() > i; i++) {

                S myItemOne = listOneIn.get(i);

                if (null != myItemOne) {

                    String myNameOne = myItemOne.getMappingName();
                    CsiDataType myTypeOne = myItemOne.getType();
                    if (null != myNameOne) {

                        for (int j = 0; myListTwo.size() > j; j++) {

                            T myItemTwo = myListTwo.get(j);

                            if ((null != myItemOne) && myNameOne.equals(myItemTwo.getMappingName())
                                    && myTypeOne.equals(myItemTwo.getType())) {

                                myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                                myListTwo.remove(j);
                                break;
                            }
                        }
                    }
                }
                if (0 == myListTwo.size()) {

                    break;
                }
            }
        }

        return myList;
    }

    public List<ValuePair<S, T>> mapByNearMatch(ListStore<S> listOneIn ,ListStore<T> listTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();
        List<T> myListTwo = copyList(listTwoIn);

        if ((null != listOneIn) && (null != myListTwo) && (0 < listOneIn.size()) && (0 < myListTwo.size())) {

            for (int i = 0; listOneIn.size() > i; i++) {

                S myItemOne = listOneIn.get(i);

                if (null != myItemOne) {

                    String myNameOne = myItemOne.getMappingName();
                    CsiDataType myTypeOne = myItemOne.getType();
                    if (null != myNameOne) {

                        for (int j = 0; myListTwo.size() > j; j++) {

                            T myItemTwo = myListTwo.get(j);

                            if ((null != myItemTwo) && myNameOne.equalsIgnoreCase(myItemTwo.getMappingName())
                                    && myTypeOne.equals(myItemTwo.getType())) {

                                myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                                myListTwo.remove(j);
                                break;
                            }
                        }
                    }
                }
                if (0 == myListTwo.size()) {

                    break;
                }
            }
        }

        return myList;
    }

    public List<ValuePair<S, T>> mapByPosition(ListStore<S> listOneIn, ListStore<T> listTwoIn) {

        return mapByRelativePosition(listOneIn, listTwoIn, 0, 0);
    }

    public List<ValuePair<S, T>> mapByRelativePosition(ListStore<S> listOneIn, ListStore<T> listTwoIn,
                                                       int indexOneIn, int indexTwoIn) {

        List<ValuePair<S, T>> myList = new ArrayList<ValuePair<S, T>>();

        if ((null != listOneIn) && (null != listTwoIn) && (0 < listOneIn.size()) && (0 < listTwoIn.size())) {

            int myLimitOne = listOneIn.size();
            int myLimitTwo = listTwoIn.size();

            for( int i = indexOneIn, j = indexTwoIn; (myLimitOne > i) && (myLimitTwo > j); i++, j++){

                S myItemOne = listOneIn.get(i);
                if (null != myItemOne){

                    T myItemTwo = listTwoIn.get(j);
                    if (null != myItemTwo) {

                        myList.add(new ValuePair<S, T>(myItemOne, myItemTwo));
                    }
                }
            }
        }

        return myList;
    }

    private <R> List<R> copyList(ListStore<R> listIn) {

        List<R> myList = new ArrayList<R>(listIn.size());

        for (int i = 0; listIn.size() > i; i++) {

            myList.add(listIn.get(i));
        }
        return myList;
    }
}
