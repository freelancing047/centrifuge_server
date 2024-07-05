package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.List;

/**
 * Created by centrifuge on 9/11/2015.
 */

public class EscapeTest {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Integer AsciiReturn = 13;
    private static final Integer AsciiNewLine = 10;

    private List<Integer> _characterBlock;
    private int _limit;
    private int _delimeter;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EscapeTest(List<Integer> characterBlockIn, int delimeterIn) {

        _characterBlock = characterBlockIn;
        _limit = characterBlockIn.size();
        _delimeter = delimeterIn;
    }

    public int[] identifyEscape(int[] listIn, int lineCountIn) {

        int myLimit = listIn.length;
        int[] myList = new int[myLimit];

        return myList;
    }
}
