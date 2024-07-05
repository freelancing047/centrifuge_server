package csi.security.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 4/2/2018.
 */
public class MetaQuery {
   private static final Logger LOG = LogManager.getLogger(MetaQuery.class);

    public enum ParameterType {

        STRING, DATE_TIME, BIG_INT,
        STRING_LIST, DATE_TIME_LIST, BIG_INT_LIST,
        STRING_ARRAY, DATE_TIME_ARRAY, BIG_INT_ARRAY
    }

    private StringBuilder _queryText;
    private Map<String, ValuePair<Object, ParameterType>> _parameters;


    public MetaQuery() throws CentrifugeException {

        reset();
    }

    public void reset() {

        _queryText = new StringBuilder();
        _parameters = new TreeMap<>();
    }

    public void addQueryText(String queryTextIn) {

        if ((null != queryTextIn) && (0 < queryTextIn.length())) {

            _queryText.append(queryTextIn);
        }
    }

    public void addParameter(String nameIn, Object valueIn, ParameterType typeIn) {

        _parameters.put(nameIn, new ValuePair<Object, ParameterType>(valueIn, typeIn));
    }

    public ResultSet execute() throws CentrifugeException {
       String sql = prepareQuery();

       try (Connection connection = CsiPersistenceManager.getMetaConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
          try {
             return statement.executeQuery();
          } catch (SQLException ignoreFirstFailure) {
             connection.rollback();
             return statement.executeQuery();
          }
       } catch (SQLException myException) {
          throw new CentrifugeException("Failed to execute metaDB query: " + sql, myException);
       } catch (java.util.MissingResourceException myException) {
          LOG.error(myException.toString());
          return null;
       } catch (Exception exception) {
          return null;
       }
    }

    private String prepareQuery() throws CentrifugeException {

        StringBuilder myBuffer = new StringBuilder();
        int myLimit = _queryText.length();
        int myInput = 0;

        while(myLimit > myInput) {

            char myCharacter = _queryText.charAt(myInput++);

            if (':' == myCharacter) {

                StringBuilder myKey = new StringBuilder();
                int myOffset = myInput;

                while (myLimit > myOffset) {

                    myCharacter = _queryText.charAt(myOffset++);
                    if (('0' > myCharacter) || (('9' < myCharacter) && ('A' > myCharacter))
                            || (('Z' < myCharacter) && ('a' > myCharacter)) || ('z' < myCharacter)) {

                        break;
                    }
                    myKey.append(myCharacter);
                }
                if ((myInput + 1) == myOffset) {

                    throw new CentrifugeException("Unexpected colon (:) found while adding parameters to query:\n");
                }
                substituteParameter(myBuffer, myKey.toString());
                myInput = myOffset - 1;

            } else {

                myBuffer.append(myCharacter);
            }
        }
        return myBuffer.toString();
    }

   private void substituteParameter(StringBuilder bufferIn, String keyIn) {
      ValuePair<Object,ParameterType> myPair = _parameters.get(keyIn);
      ParameterType type = myPair.getValue2();
      int howMany = 0;

      switch (type) {
         case STRING:
            formatString(bufferIn, (String) myPair.getValue1());
            break;
         case DATE_TIME:
            formatDateTime(bufferIn, (Date) myPair.getValue1());
            break;
         case BIG_INT:
            formatInteger(bufferIn, (Long) myPair.getValue1());
            break;
         case STRING_LIST:
            List<String> stringList = (List<String>) myPair.getValue1();

            if ((stringList != null) && !stringList.isEmpty()) {
               howMany = stringList.size();

               for (int i = 0; i < howMany; i++) {
                  if (i > 0) {
                     bufferIn.append(", ");
                  }
                  formatString(bufferIn, stringList.get(i));
               }
            }
            break;
         case DATE_TIME_LIST:
            List<Date> dateTimeList = (List<Date>) myPair.getValue1();

            if ((dateTimeList != null) && !dateTimeList.isEmpty()) {
               howMany = dateTimeList.size();

               for (int i = 0; i < howMany; i++) {
                  if (i > 0) {
                     bufferIn.append(", ");
                  }
                  formatDateTime(bufferIn, dateTimeList.get(i));
               }
            }
            break;
         case BIG_INT_LIST:
            List<Long> longList = (List<Long>) myPair.getValue1();

            if ((longList != null) && !longList.isEmpty()) {
               howMany = longList.size();

               for (int i = 0; i < howMany; i++) {
                  if (i > 0) {
                     bufferIn.append(", ");
                  }
                  formatInteger(bufferIn, longList.get(i));
               }
            }
            break;
          case STRING_ARRAY:
             String[] stringArray = (String[]) myPair.getValue1();

             if ((stringArray != null) && (stringArray.length > 0)) {
                for (int i = 0; i < stringArray.length; i++) {
                   if (i > 0) {
                      bufferIn.append(", ");
                   }
                   formatString(bufferIn, stringArray[i]);
                }
             }
             break;
         case DATE_TIME_ARRAY:
            Date[] dateTimeArray = (Date[]) myPair.getValue1();

            if ((dateTimeArray != null) && (dateTimeArray.length > 0)) {
               for (int i = 0; i < dateTimeArray.length; i++) {
                  if (i > 0) {
                     bufferIn.append(", ");
                  }
                  formatDateTime(bufferIn, dateTimeArray[i]);
               }
            }
            break;
         case BIG_INT_ARRAY:
            Long[] longArray = (Long[]) myPair.getValue1();

            if ((longArray != null) && (longArray.length > 0)) {
               for (int i = 0; i < longArray.length; i++) {
                  if (i > 0) {
                     bufferIn.append(", ");
                  }
                  formatInteger(bufferIn, longArray[i]);
               }
            }
            break;
      }
   }

    private void formatString(StringBuilder bufferIn, String valueIn) {

        bufferIn.append('\'');
        for (int i = 0; valueIn.length() > i; i++) {

            char myCharacter = valueIn.charAt(i);
            if ('\'' == myCharacter) {

                bufferIn.append('\'');
            }
            bufferIn.append(myCharacter);
        }
        bufferIn.append('\'');
    }

    private void formatDateTime(StringBuilder bufferIn, Date valueIn) {

        bufferIn.append('\'');
        valueIn.toString();
        bufferIn.append('\'');
    }

    private void formatInteger(StringBuilder bufferIn, Long valueIn) {

        valueIn.toString();
    }
}
