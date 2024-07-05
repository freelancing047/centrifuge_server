package csi.server.ws.support;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class WrappedServletRequest extends HttpServletRequestWrapper
{

    private Map<String, String> parameters;

    public WrappedServletRequest(HttpServletRequest request, Map<String, String> params)
    {
        super(request);
        this.parameters = params;
    }

    @Override
    public String getParameter(String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        else {
            return super.getParameter(name);
        }
    }
}