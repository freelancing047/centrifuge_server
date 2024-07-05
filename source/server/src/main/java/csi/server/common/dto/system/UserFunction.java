package csi.server.common.dto.system;

import csi.server.common.enumerations.SqlToken;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;

/**
 * Created by centrifuge on 9/26/2017.
 */
@Entity
public class UserFunction implements Serializable {

    private static List<UserFunction> _functionList = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int group;
    private int result;
    private String displayName;
    private String functionName;
    private String arguments;

    public static List<UserFunction> getUserFunctions() {

        return _functionList;
    }

    public static void loadUserFunctions(List<UserFunction> functionListIn) {

        _functionList = functionListIn;

        for (UserFunction myFunction : functionListIn) {

            myFunction.installFunction();
        }
    }

    public UserFunction() {

    }

    public UserFunction(int groupIn, int resultIn, String displayNameIn, String functionNameIn, String argumentsIn) {

        group = groupIn;
        result = resultIn;
        displayName = displayNameIn;
        functionName = functionNameIn;
        arguments = argumentsIn;
    }

    public UserFunction(long idIn, int groupIn, int resultIn,
                        String displayNameIn, String functionNameIn, String argumentsIn) {

        id = idIn;
        group = groupIn;
        result = resultIn;
        displayName = displayNameIn;
        functionName = functionNameIn;
        arguments = argumentsIn;
    }

    public void installFunction() {

        SqlToken.defineFunction(this);
    }

    public void setDisplayName(String displayNameIn) {

        displayName = displayNameIn;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setId(long idIn) {

        id = idIn;
    }

    public long getId() {

        return id;
    }

    public void setGroup(int groupIn) {

        group = groupIn;
    }

    public int getGroup() {

        return group;
    }

    public void setFunctionName(String functionNameIn) {

        functionName = functionNameIn;
    }

    public String getFunctionName() {

        return functionName;
    }

    public void setResult(int resultIn) {

        result = resultIn;
    }

    public int getResult() {

        return result;
    }

    public void setArguments(String argumentsIn) {

        arguments = argumentsIn;
    }

    public String getArguments() {

        return arguments;
    }
}
