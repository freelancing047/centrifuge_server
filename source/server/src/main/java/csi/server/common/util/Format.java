package csi.server.common.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.column.InstalledColumn;

public class Format {
   private static final char[] STRINGBUILDER =
      new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private static final char[] HEX_CHAR =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
   private static final int DEPTH_LIMIT = 3;
   private static final String NULL = "<null>";

    public interface OutPut {

        public void putLine(String displayLineIn);
    }

    public static String identifyNull(String valueIn){

        return (null != valueIn) ? valueIn : NULL;
    }

    public static String value(Enum valueIn){

        return (null != valueIn) ? value(valueIn.name()) : NULL;
    }

    public static String value(Enum valueIn, boolean doQuotesIn){

        return (null != valueIn) ? value(valueIn.name()) : NULL;
    }

    public static String value(String stringIn) {
        return (null != stringIn) ? value(stringIn, true) : NULL;
    }

    public static String rawValue(String stringIn) {
        return (null != stringIn) ? value(stringIn, false) : NULL;
    }

    public static String lower(String stringIn, boolean doQuotesIn) {
        return (null != stringIn) ? value(stringIn.toLowerCase(), doQuotesIn) : NULL;
    }

    public static String upper(String stringIn, boolean doQuotesIn) {
        return (null != stringIn) ? value(stringIn.toUpperCase(), doQuotesIn) : NULL;
    }

    public static String lower(String stringIn) {
        return (null != stringIn) ? value(stringIn.toLowerCase()) : NULL;
    }

    public static String upper(String stringIn) {
        return (null != stringIn) ? value(stringIn.toUpperCase()) : NULL;
    }

    public static String value(Boolean valueIn) {  //TODO: not used
        return (null != valueIn) ? Boolean.toString(valueIn.booleanValue()) : NULL;
    }

    public static String value(Double valueIn) {
        return (null != valueIn) ? value(valueIn.doubleValue()) : NULL;
    }

    public static String value(Integer valueIn) {
        return (null != valueIn) ? value(valueIn.intValue()) : NULL;
    }

    public static String value(Double valueIn, boolean doQuotesIn) {
        String myValue = (null != valueIn) ? valueIn.toString() : null;
        return value(myValue, doQuotesIn);
    }

    public static String value(Integer valueIn, boolean doQuotesIn) {
        String myValue = (null != valueIn) ? valueIn.toString() : null;
        return value(myValue, doQuotesIn);
    }

    public static String value(InstalledColumn valueIn) {
        return (null != valueIn)
                ? ("Field = " + Format.value(valueIn.getFieldName())
                    + ", Column = " + Format.value(valueIn.getColumnName())
                    + ", Id = " + Format.value(valueIn.getLocalId()))
                : NULL;
    }

    public static String value(Resource valueIn) {
        String myName = valueIn.getName();
        if (null != myName) {
            return (null != valueIn) ? valueIn.getClass().getSimpleName() + ", name = " + Format.value(myName) : NULL;
        } else{
            return (null != valueIn) ? valueIn.getClass().getSimpleName() + ", id = " + Format.value(valueIn.getUuid()) : NULL;
        }
    }

    public static String value(SelectorBasics valueIn) {
        return (null != valueIn) ? "{name = " + Format.value(valueIn.getName()) + ", id = " + Format.value(valueIn.getKey()) + "}" : NULL;
    }

    public static String value(ModelObject valueIn) {
        return (null != valueIn) ? valueIn.getClass().getSimpleName() + ", id = " + Format.value(valueIn.getUuid()) : NULL;
    }

    public static String value(Object valueIn) {
        return (null != valueIn) ? valueIn.toString() : NULL;
    }

    public static String value(CsiFileType valueIn) {
        return (null != valueIn) ? valueIn.getExtension() : NULL;
    }

    public static String stringValue(byte[] valueIn)
    {
        return (null != valueIn) ? value(new String(valueIn)) : value((String)null);
    }

    public static String value(byte[] valueIn)
    {
        if (null != valueIn)
        {
            if (0 < valueIn.length)
            {
                return "[]";
            }
            else
            {
                StringBuilder myBuffer = new StringBuilder(2 * (valueIn.length + 1));
                byte myDelimeter = '[';

                for (int i = 0; valueIn.length > i; i++) {

                    myBuffer.append(myDelimeter);
                    myBuffer.append(STRINGBUILDER[(valueIn[i]/16) & 15]);
                    myBuffer.append(STRINGBUILDER[valueIn[i]%16]);
                    myDelimeter = ' ';
                }
                myBuffer.append(']');
                return myBuffer.toString();
            }
        }
        else
        {
            return NULL;
        }
    }

    public static String value(byte[] valueIn, int entriesPerRowIn)
    {
        if (null != valueIn)
        {
            if (0 < valueIn.length)
            {
                return "[]";
            }
            else if (0 < entriesPerRowIn)
            {
                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append('[');

                for (int j = 0, k = entriesPerRowIn + j; valueIn.length > j; j = k)
                {
                    for (int i = j; k > i; i++) {

                        if (j == i)
                        {
                            myBuffer.append("\n");
                        }
                        else
                        {
                            myBuffer.append(" ");
                        }
                        myBuffer.append(STRINGBUILDER[(valueIn[i]/16) & 15]);
                        myBuffer.append(STRINGBUILDER[valueIn[i]%16]);
                    }
                }

                myBuffer.append(']');
                return myBuffer.toString();
            }
            else
            {
                return value(valueIn);
            }
        }
        else
        {
            return NULL;
        }
    }

    public static String value(String stringIn, String replacementIn)
    {
        if ((null != stringIn) && (0 < stringIn.length()))
        {
            if (null != replacementIn)
            {
                return replacementIn;
            }
            else
            {
                return "\"" + stringIn + "\"";
            }
        }
        else
        {
            return NULL;
        }
    }

    public static String value(String stringIn, boolean doQuotesIn)
    {
        if (null != stringIn)
        {
            if (doQuotesIn) {

                return "\"" + stringIn + "\"";

            } else {

                return stringIn;
            }
        }
        else
        {
            return NULL;
        }
    }

    public static String value(Throwable exceptionIn)
    {
        String myMessage = "Unknown Exception";

        if (null != exceptionIn) {

            myMessage = subValue(exceptionIn, 0);
        }
        return myMessage;
    }

    private static String subValue(Throwable exceptionIn, int levelIn)
    {
        String myMessage = "";

        if ((null != exceptionIn) && (DEPTH_LIMIT > levelIn)) {

            Throwable mySourceException = exceptionIn.getCause();
            String mySubMessage = exceptionIn.getMessage();

            myMessage = exceptionIn.getClass().getSimpleName();

            if ((null != mySubMessage) && (0 < mySubMessage.length())) {

                myMessage = myMessage + ": " + mySubMessage;
            }
            if (null != mySourceException) {

                myMessage += " [ " + subValue(mySourceException, levelIn + 1) + " ] ";
            }
        }
        return myMessage;
    }

    public static String value(double valueIn)
    {
        return Double.toString(valueIn);
    }

    public static String value(long valueIn)
    {
        return Long.toString(valueIn);
    }

    public static String value(int valueIn)
    {
        return Integer.toString(valueIn);
    }

    public static String display(byte[] arrayIn)
    {
        if (null != arrayIn)
        {
            int mySize = (arrayIn.length * 3) - 1;
            char[] myChar = new char[mySize];

            for (int i = 0, j = 0; mySize > i; j++)
            {
                int myUpperNibble = ((arrayIn[j]) & 0x000000ff) / 16;
                int myLowerNibble = ((arrayIn[j]) & 0x000000ff) % 16;

                if (0 < i)
                {
                    myChar[i++] = ' ';
                }
                myChar[i++] = HEX_CHAR[myUpperNibble];
                myChar[i++] = HEX_CHAR[myLowerNibble];
            }
            return new String(myChar);
        }
        else
        {
            return NULL;
        }
    }

    public static String value(Date dateTimeIn)
    {
        String myValue = NULL;

        if (null != dateTimeIn)
        {
            myValue = value(dateTimeIn.toString());
        }
        return myValue;
    }

    public static String value(FieldDef fieldDefIn)
    {
        String myName = NULL;

        if (null != fieldDefIn)
        {
            String mFieldName = fieldDefIn.getFieldName();

            if (null != mFieldName)
            {
                myName = "FieldDef:" + mFieldName;
            }
            else
            {
                FieldType myType = fieldDefIn.getFieldType();

                if (FieldType.STATIC == myType)
                {
                    String myText = fieldDefIn.getStaticText();

                    if (null != myText)
                    {
                        myName = "FieldDef:" + myType.toString() + ":\"" + myText + "\"";
                    }
                    else
                    {
                        myName = "FieldDef:" + myType.toString() + ":<null>";
                    }
                }
                else
                {
                    myName = "FieldDef:" + myType.toString() + ":<?>";
                }
            }
        }
        return myName;
    }

    public static String value(Set<Object> setIn) {

        StringBuilder myBuffer = new StringBuilder();
        Iterator<Object> myIterator = setIn.iterator();

        if (null != setIn) {

            if (!setIn.isEmpty()) {

                myBuffer.append(value(myIterator.next()));

                while (myIterator.hasNext()) {

                    myBuffer.append(", ");
                    myBuffer.append(value(myIterator.next()));
                }

            } else {

                myBuffer.append("<empty set>");
            }

        } else {

            myBuffer.append(NULL);
        }
        return myBuffer.toString();
    }

    public static String value(List<Object> listIn) {

        StringBuilder myBuffer = new StringBuilder();
        Iterator<Object> myIterator = listIn.iterator();

        if (null != listIn) {

            if (!listIn.isEmpty()) {

                myBuffer.append(value(myIterator.next()));

                while (myIterator.hasNext()) {

                    myBuffer.append(", ");
                    myBuffer.append(value(myIterator.next()));
                }

            } else {

                myBuffer.append("<empty list>");
            }

        } else {

            myBuffer.append(NULL);
        }
        return myBuffer.toString();
    }

    public static String value(Object[] arrayIn) {

        StringBuilder myBuffer = new StringBuilder();

        if (null != arrayIn) {

            if (0 < arrayIn.length) {

                myBuffer.append(value(arrayIn[0]));

                for (int i = 1; arrayIn.length > i; i++) {

                    myBuffer.append(", ");
                    myBuffer.append(value(arrayIn[i]));
                }

            } else {

                myBuffer.append("<empty array>");
            }

        } else {

            myBuffer.append(NULL);
        }
        return myBuffer.toString();
    }

    public static String value(String[] stringsIn, String delimeterIn)
    {

        if ((null != stringsIn) && (0 < stringsIn.length)) {

            StringBuilder myInstructions = new StringBuilder();

            for (int i = 0; stringsIn.length > i; i++) {

                if (0 != i) {

                    myInstructions.append(delimeterIn);
                }
                myInstructions.append(stringsIn[i]);
            }
            return myInstructions.toString();

        } else {

            return null;
        }
    }

    public static String stripQuotes(String valueIn) {

        if (null != valueIn) {

            int myLength = valueIn.length();

            if ((1 < myLength) && (valueIn.charAt(0) == '"') && (valueIn.charAt(myLength - 1) == '"')) {

                return valueIn.substring(1, myLength - 1);

            } else if (NULL.equals(valueIn)) {

                return null;
            }
            return valueIn;
        }
        return null;
    }

    public static String normalizePath(String pathIn) {

        if (null != pathIn) {

            StringBuilder myPath = new StringBuilder();

            for (int i = 0; pathIn.length() > i; i++){

                char myChar = pathIn.charAt(i);

                if ('\\' == myChar) {

                    myPath.append('/');

                } else {

                    myPath.append(myChar);
                }
            }
            return myPath.toString();

        } else {

            return null;
        }
    }

    public static <S,T> void debugDumpMap(OutPut logIn, String titleIn, Map<S,T> mapIn) {

        if (null != titleIn) {

            logIn.putLine("---- " + titleIn + "-------------------------------------------");

        } else {

            logIn.putLine("--------------------------------------------------------------------------------------");
        }

        for (Entry<S,T> myEntry : mapIn.entrySet()) {

            logIn.putLine("     -- " + value(myEntry.getKey() + " :: " + myEntry.getValue()));
        }
        logIn.putLine("--------------------------------------------------------------------------------------");
    }
}
