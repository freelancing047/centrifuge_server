package csi.client.gwt.i18n;

public class ParametersImpl implements Parameters{
    private Parameter[] params;

    public ParametersImpl(Parameter[] params) {
      this.params = params;
    }

    public int getCount() {
      return params.length;
    }

    public Parameter getParameter(int i) {
      if (i < 0 || i >= params.length) {
        return null;
      }
      return params[i];
    }

    public Parameter getParameter(String name) {
      return getParameter(getParameterIndex(name));
    }

    public int getParameterIndex(String name) {
      for (int i = 0; i < params.length; ++i) {
        if (params[i].getName().equals(name)) {
          return i;
        }
      }
      return -1;
    }
  }