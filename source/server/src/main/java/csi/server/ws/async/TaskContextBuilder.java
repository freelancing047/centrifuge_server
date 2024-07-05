package csi.server.ws.async;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.CsiSecurityManager;
import csi.security.queries.Users;
import csi.server.business.service.AbstractService;
import csi.server.business.service.BusinessServiceManager;
import csi.server.business.service.ServiceLocator;
import csi.server.business.service.annotation.DefaultValue;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.MultipartParam;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.RequestContextPathParam;
import csi.server.business.service.annotation.RequestReaderParam;
import csi.server.business.service.annotation.RequestStreamParam;
import csi.server.business.service.annotation.RequestURLParam;
import csi.server.business.service.annotation.ResponseStreamParam;
import csi.server.business.service.annotation.ResponseWriterParam;
import csi.server.business.service.annotation.ServletRequestParam;
import csi.server.business.service.annotation.ServletResponseParam;
import csi.server.business.service.annotation.SessionParam;
import csi.server.common.codec.Codec;
import csi.server.common.codec.CodecManager;
import csi.server.common.codec.CodecType;
import csi.server.common.codec.xstream.XStreamCodec;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.exception.AuthorizationException;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.User;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.TaskConstants;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskSession;
import csi.server.task.core.TaskGroupId;
import csi.server.util.AsyncConfiguration;
import csi.startup.Product;

/**
 * Class designated for building the TaskContext and members of it.
 *
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 */
public class TaskContextBuilder {
   private static final Logger LOG = LogManager.getLogger(TaskContextBuilder.class);

   private static final String HANDLER_ID = "TaskContextBuilder";
   private static final Pattern SERVLET_PATH_PATTERN = Pattern.compile("(?i)/v[0-9]+/op");

   public static final String KEY_URL_STRUCT_PATH = "Path";
   public static final String KEY_URL_STRUCT_METHOD = "Method";
   public static final String RETURN_TYPE = "rtnType";
   public static final String KEY_URL_STRUCT_HTTP_PARAMS = "HttpParams";

    private static Set<Class<?>> synchronousAnnotations = new HashSet<Class<?>>();
    static {
        synchronousAnnotations.add(ServletRequestParam.class);
        synchronousAnnotations.add(RequestStreamParam.class);
        synchronousAnnotations.add(RequestReaderParam.class);
        synchronousAnnotations.add(ServletResponseParam.class);
        synchronousAnnotations.add(ResponseStreamParam.class);
        synchronousAnnotations.add(ResponseWriterParam.class);
    }


    /**
     * Used by the GWT code calling in.
     * @param req
     * @param resp
     * @return
     * @throws CentrifugeException
     */
    public static TaskContext buildVortexTaskContext(HttpServletRequest req, HttpServletResponse resp)
            throws CentrifugeException {

        TaskSession taskSession = new HttpTaskSession(req.getSession());

        String contextPath = req.getContextPath();
        String requestURL = req.getRequestURL().toString();
        String remoteUser = req.getRemoteUser();

        String clientId = UUID.randomUUID().toString();
        String taskId = UUID.randomUUID().toString();
        String clientIP = req.getHeader("X-FORWARDED-FOR");

        if (null == clientIP) {
            clientIP = req.getRemoteAddr();
        }

        // Create and set the TaskContext
        TaskContext taskContext = new TaskContext(clientId, taskId, new TaskGroupId(), taskSession);
        taskContext.setAdminTask(false);
        taskContext.setMethod(null);
        taskContext.setInterruptable(false);
        taskContext.setServicePath(null);
        taskContext.setContextPath(contextPath);
        taskContext.setRequestURL(requestURL);
        taskContext.setCodec(null);
        taskContext.setType(TaskConstants.TASK_TYPE_REST);
        taskContext.setGwtService(true);
        taskContext.setRemoteUser(remoteUser);
        taskContext.setClientIP(clientIP);

        return taskContext;
    }


    /**
     * Creates the <code>TaskContext</code> object based on the given HTTP Request Parameters.
     *
     * @param taskId
     * @param req
     *            the HTTP Request
     * @param resp
     * @return the constructed TaskContext required by the Execute Operation
     * @throws ServletException
     *             in case the TaskContext construction fails
     */
    public static TaskContext buildTaskContext(String clientId, String taskId, TaskGroupId taskGroupId,
            HttpServletRequest req, HttpServletResponse resp, boolean gwtService) throws CentrifugeException {
        String servicePath = null;
        String methodName = null;
        String pathInfo = null;
        String servletPath = null;

        if (!gwtService) {
            pathInfo = req.getPathInfo().trim();
            servletPath = req.getServletPath().trim();

            if (SERVLET_PATH_PATTERN.matcher(servletPath).matches()) {
                int lastSlash = pathInfo.lastIndexOf('/');
                servicePath = pathInfo.substring(0, lastSlash);

                if ((lastSlash > -1) && (lastSlash < pathInfo.length())) {
                    methodName = pathInfo.substring(lastSlash + 1);
                }
            } else {
                // old style
                servicePath = servletPath;
                methodName = pathInfo.substring(1);
            }
        }

        if (!gwtService && ((servicePath == null) || (methodName == null) || servicePath.isEmpty() || methodName.isEmpty())) {
            throw new CentrifugeException("Malformed URL: servicePath or operation is missing.");
        }

        Method serviceMethod = null;
        CodecType codecType = null;
        Codec codec = null;
        if (!gwtService) {
            // Find the annotated AbstractService Class corresponding to the
            // AbstractService Path
            Class<?> serviceClass = null;
            try {
                serviceClass = ServiceLocator.getInstance().getServiceClass(servicePath);
            } catch (ClassNotFoundException e) {
                throw new CentrifugeException(e);
            }

            // Find the annotated AbstractService Method corresponding to the
            // AbstractService Class
            serviceMethod = getServiceMethod(serviceClass, methodName);
            assert serviceMethod != null : "Service method is null";

            // look for optional parameter to request to specify codec type.
            // Defaults to FLEX_XML
            String msgFormat = req.getParameter(AsyncConstants.MESSAGE_FORMAT);
            codecType = CodecType.resolveValue(msgFormat);
            String type = req.getParameter(RETURN_TYPE);

            // default to flex xml for backward compatibility
            if (codecType == null) {
                if ((type != null) && type.equalsIgnoreCase("JSON")) {
                    codecType = CodecType.JSON_DEEP;
                } else {
                    codecType = CodecType.FLEX_XML;
                }
            }

            codec = getServiceCodec(serviceClass, codecType);
        }
        // create a task session object to wrap the http session object
        TaskSession taskSession = new HttpTaskSession(req.getSession());

        String contextPath = req.getContextPath();
        String requestURL = req.getRequestURL().toString();

        boolean forceSynchronous = false;
        if ((clientId == null) && (taskId == null)) {
            clientId = UUID.randomUUID().toString();
            taskId = UUID.randomUUID().toString();
            forceSynchronous = true;
        }

        boolean isAdminTask = false;
        boolean isInterruptable = false;

        if (!gwtService) {
            isAdminTask = servicePath.equals("/services/task");
            isInterruptable = serviceMethod.isAnnotationPresent(Interruptable.class);
        }
        // Create and set the TaskContext
        TaskContext taskContext = new TaskContext(clientId, taskId, taskGroupId, taskSession);
        taskContext.setAdminTask(isAdminTask);
        taskContext.setMethod(serviceMethod);
        taskContext.setInterruptable(isInterruptable);
        taskContext.setServicePath(servicePath);
        taskContext.setContextPath(contextPath);
        taskContext.setRequestURL(requestURL);
        taskContext.setCodec(codec);
        taskContext.setType(TaskConstants.TASK_TYPE_REST);
        taskContext.setGwtService(gwtService);

        if (!gwtService) {
            Object[] methodArgs = buildMethodArgs(serviceMethod, req, resp, codec);
            taskContext.setMethodArgs(methodArgs);

            if (forceSynchronous) {
                taskContext.setSynchronous(true);
            } else {
                taskContext.setSynchronous(isSynchronous(serviceMethod));
            }

            if (taskContext.isSynchronous()) {
                taskContext.setSynchronousHttpResponse(resp);
            }
        }
        return taskContext;
    }


    public static Codec getServiceCodec(Class<?> serviceClass, CodecType codecType) {
        Codec codec = null;
        if (CodecType.FLEX_XML == codecType) {
            // this is for backward compatibility. Going forward each service should not
            // need it's own codec variants
            codec = CodecManager.getCodec(serviceClass.getName(), codecType);
            if (codec == null) {
                XStream xstream = XStreamHelper.createLegacyFlexMarshaller();
                AbstractService abstractService = (AbstractService) BusinessServiceManager.getInstance().getComponent(
                        serviceClass);
                abstractService.initMarshaller(xstream);
                codec = new XStreamCodec(codecType, xstream);
                CodecManager.registerCodec(serviceClass.getName(), codec);
            }
        } else {
            codec = CodecManager.getCommonCodec(codecType);
        }

        return codec;
    }


    private static boolean isSynchronous(Method serviceMethod) {
        Class<?>[] methodArgTypes = serviceMethod.getParameterTypes();
        if ((methodArgTypes == null) || (methodArgTypes.length == 0)) {
            return false;
        }

        Annotation[][] paramAnnotations = serviceMethod.getParameterAnnotations();
        for (int i = 0; i < methodArgTypes.length; i++) {
            Annotation[] annotations = paramAnnotations[i];
            for (Annotation annotation : annotations) {
                if (synchronousAnnotations.contains(annotation.getClass())) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Construct the appropriate array of arguments for the given service method based on the method's parameter
     * annotations.
     *
     * @param serviceMethod
     * @param req
     * @param resp
     * @param codec
     * @return
     * @throws CentrifugeException
     * @throws ServletException
     * @throws AuthorizationException
     */
    public static Object[] buildMethodArgs(Method serviceMethod, HttpServletRequest req, HttpServletResponse resp,
            Codec codec) throws CentrifugeException, AuthorizationException {
        Class<?>[] methodArgTypes = serviceMethod.getParameterTypes();
        if ((methodArgTypes == null) || (methodArgTypes.length == 0)) {
            return new Object[0];
        }

        Object[] methodArgs = new Object[methodArgTypes.length];
        Annotation[][] paramAnnotations = serviceMethod.getParameterAnnotations();
        String path = null;
        String paramname = null;
        for (int i = 0; i < methodArgs.length; i++) {
            Class<?> argClass = methodArgTypes[i];

            Annotation[] annotations = paramAnnotations[i];
            Object argValue = null;
            Object defaultValue = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof QueryParam) {
                    argValue = req.getParameter(((QueryParam) annotation).value());
                    LOG.debug("Query param name: " + ((QueryParam) annotation).value() + " = " + argValue);

                } else if (annotation instanceof SessionParam) {
                    argValue = req.getSession().getAttribute(((SessionParam) annotation).value());
                } else if (annotation instanceof DefaultValue) {
                    defaultValue = ((DefaultValue) annotation).value();
                } else if (annotation instanceof MultipartParam) {
                    argValue = prepareMultiPartInfo(req);
                } else if (annotation instanceof PayloadParam) {
                    try {
                        argValue = codec.unmarshal(req.getInputStream());
                        if (LOG.isDebugEnabled()) {
                           LOG.debug(argValue.toString());
                        }
                        if ((argValue != null) && !(argClass.isAssignableFrom(argValue.getClass()))) {
                            throw new CentrifugeException("Argument type mismatch " + argValue.getClass()
                                    + ".  Expected " + argClass.getName() + ".");
                        }
                    } catch (Exception e) {
                        throw new CentrifugeException("Error while reading from request input stream. Reason: "
                                + e.getMessage(), e);
                    }
                } else if (annotation instanceof ServletRequestParam) {
                    argValue = req;
                } else if (annotation instanceof RequestStreamParam) {
                    try {
                        argValue = req.getInputStream();
                    } catch (IOException e) {
                        throw new CentrifugeException("Error retrieving input stream", e);
                    }
                } else if (annotation instanceof RequestReaderParam) {
                    try {
                        argValue = req.getReader();
                    } catch (IOException e) {
                        throw new CentrifugeException("Error retrieving reader", e);
                    }
                } else if (annotation instanceof ServletResponseParam) {
                    argValue = resp;
                } else if (annotation instanceof ResponseStreamParam) {
                    try {
                        argValue = resp.getOutputStream();
                    } catch (IOException e) {
                        throw new CentrifugeException("Error retrieving output stream", e);
                    }
                } else if (annotation instanceof ResponseWriterParam) {
                    try {
                        argValue = resp.getWriter();
                    } catch (IOException e) {
                        throw new CentrifugeException("Error retrieving writer", e);
                    }
                } else if (annotation instanceof RequestContextPathParam) {
                    argValue = req.getContextPath();

                } else if (annotation instanceof RequestURLParam) {
                    argValue = req.getRequestURL().toString();

                } else if (annotation instanceof PathParam) {
                    if (path == null) {
                        path = serviceMethod.getDeclaringClass().getAnnotation(Path.class).value()
                                + serviceMethod.getAnnotation(Path.class).value();
                    }
                    paramname = ((PathParam) annotation).value();
                    String url = req.getRequestURL().toString();
                    String[] pathParts = path.split("/");
                    String[] urlParts = url.split("/");
                    int pathIndx = 0;
                    while ((pathParts[pathIndx] == null) || pathParts[pathIndx].trim().equals("")) {
                        pathIndx++;
                    }
                    int urlIndx = pathIndx;
                    while (!pathParts[pathIndx].equals(urlParts[urlIndx])) {
                        urlIndx++;
                    }
                    for (++pathIndx, ++urlIndx; pathIndx < pathParts.length; pathIndx++, urlIndx++) {
                        if (pathParts[pathIndx].equals("{" + paramname + "}")) {
                            argValue = urlParts[urlIndx];
                            break;
                        }
                    }
                } else {
                    // must be some other annotation
                    // that we don't know about so skip it
                    continue;
                }
            }

            if ((argValue == null) && (defaultValue != null)) {
                argValue = defaultValue;
                if (LOG.isDebugEnabled()) {
                   LOG.debug("ArgValue is null, defaultValue: " + defaultValue);
                }
            }

            if (argValue != null) {
                methodArgs[i] = coerceType(argClass, argValue);
                if (path != null) {
                    int indx = path.indexOf(paramname);
                    if (indx > -1) {
                        path = path.substring(0, indx - 1) + argValue + path.substring(indx + paramname.length() + 1);
                    }
                }
            }
        }

        return methodArgs;
    }


    /**
     * Convert a string to an instance of the target class. The target Class must have one of the following:
     * <p/>
     * 1. A constructor that takes a string 2. The class has a static valueOf(String s) method
     * <p/>
     * TODO: cache the method or constructor in an LRUCache to avoid having to reflect on a targetClz every time.
     *
     * @param targetClz
     * @param arg
     * @return
     * @throws CentrifugeException
     */
    private static Object coerceType(Class<?> targetClz, Object arg) throws CentrifugeException {
        Object obj = null;

        if (arg == null) {
            return null;
        }

        if (targetClz.isAssignableFrom(arg.getClass())) {
            return arg;
        }

        // try looking for a constructor
        try {
            Constructor<?> constructor = targetClz.getConstructor(String.class);
            if (constructor != null) {
                obj = constructor.newInstance(arg.toString());
            }
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (InstantiationException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        }

        // try looking for valueOf method
        try {
            Method method = targetClz.getMethod("valueOf", String.class);
            if (method != null) {
                obj = method.invoke(null, arg.toString());
            }
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        }

        if (obj == null) {
            throw new CentrifugeException("Unable to coerce value '" + arg + "' to type " + targetClz.getSimpleName());
        }

        return obj;

    }


    /**
     * Gets the designated AbstractService class Method instance based on the given <code>methodName</code>. The Method
     * must have 0 or 1 parameter.
     *
     * @param serviceClass
     *            the AbstractService Class instance
     * @param methodName
     *            the name of the method that needs to be checked
     * @return the Method instance
     * @throws CentrifugeException
     *             in case the Class instance contains no such method or the method has more than one parameter
     */
    private static Method getServiceMethod(Class<?> serviceClass, String methodName) throws CentrifugeException {
        Method method = null;
        Method[] methods = serviceClass.getMethods();
        for (int i = 0; (i < methods.length) && (method == null); i++) {
            if (methods[i].getName().equals(methodName) && methods[i].isAnnotationPresent(Operation.class)) {
                method = methods[i];
            }
        }
        if (method == null) {
            CentrifugeException ce = new CentrifugeException(String.format(
                    "Request for non-existent '%s' from service'%s'", methodName, serviceClass.getName()));
            throw ce;
        }
        return method;
    }

    /**
     * Checks and prepares the MultiPartInfo. At the end the authorization is checked and if fails an
     * <code>AuthorizationException</code> will be thrown.
     *
     * @param request
     *            the HttpServletRequest instance
     * @return a Map containing the MultiPartInfo with the Form FileItem filed name/values pair and other information as
     *         follows per keys:
     *         <p>
     *         <ul>
     *         <li>
     *         AsyncConstants.KEY_USER</li>
     *         <li>
     *         AsyncConstants.KEY_AGENT</li>
     *         <li>
     *         AsyncConstants.KEY_PASSWORD</li>
     *         <li>
     *         AsyncConstants.KEY_FILEITEMS</li>
     *         <li>
     *         AsyncConstants.KEY_IS_MULTI_PART</li>
     *         </ul>
     *         </p>
     * @throws AuthorizationException
     *             in case the authorization fails
     * @throws ServletException
     *             in case there is a file upload exception or other unexpected exception
     */
    private static HashMap<String, Object> prepareMultiPartInfo(HttpServletRequest request) throws AuthorizationException, CentrifugeException {
        String username = request.getParameter(AsyncConstants.KEY_USER);
        String agentname = request.getParameter(AsyncConstants.KEY_AGENT);
        String pwhash = request.getParameter(AsyncConstants.KEY_PASSWORD);

        String tempThreshold;
        int intTempThreshold;
        String tempDirectory;
        long longMaxFileSize;
        String maxFileSize;

        tempThreshold = (String) AsyncConfiguration.getAttribute(AsyncConstants.KEY_INIT_CONFIG_TEMP_THRESHOLD);
        if (null == tempThreshold) {
            tempThreshold = "1048576"; // 1MB
        }

        maxFileSize = (String) AsyncConfiguration.getAttribute(AsyncConstants.KEY_INIT_CONFIG_MAX_FILE_SIZE);
        if (null == maxFileSize) {
            maxFileSize = "1073741824"; // 1024*1MB
        }

        // Convert number strings to numbers for later use
        try {
            intTempThreshold = Integer.parseInt(tempThreshold);
            longMaxFileSize = Long.parseLong(maxFileSize);
        }
        // If any exceptions, just set defaults
        catch (NumberFormatException nfx) {
            intTempThreshold = 1048576; // 1MB
            longMaxFileSize = 134217728; // 128MB
        }

        tempDirectory = (String) AsyncConfiguration.getAttribute(AsyncConstants.KEY_INIT_CONFIG_TEMP_DIRECTORY);
        if (null == tempDirectory) {
            tempDirectory = "temp";
        }

        /* Preparing the MultiPartInfo */

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        // if multipart content, we need to look at all the form fields to see
        // if we need any of that information.
        HashMap<String, Object> mpinfo = new HashMap<String, Object>();
        DiskFileItemFactory dfifactory;
        ServletFileUpload upload;
        List<FileItem> /* FileItem */fileItems;
        if (isMultipart) {
            try {
                // Create factory for disk-based file items, set constraints
                dfifactory = new DiskFileItemFactory();
                dfifactory.setSizeThreshold(intTempThreshold);
                dfifactory.setRepository(new File(tempDirectory));

                // Create new file upload handler, set constraints
                upload = new ServletFileUpload(dfifactory);
                upload.setSizeMax(longMaxFileSize);

                fileItems = upload.parseRequest(request);
                FileItem item;
                Iterator<FileItem> iter = fileItems.iterator();
                String fieldname, fieldvalue;

                while (iter.hasNext()) {
                    item = iter.next();
                    if (item.isFormField()) {
                        fieldname = item.getFieldName();
                        fieldvalue = item.getString();
                        // if we've already seen this key, ignore others
                        if (!mpinfo.containsKey(fieldname)) {
                            mpinfo.put(fieldname, fieldvalue);
                        }
                    }
                }
                // if we find agent, user, password values in the multipart content
                // they will override what we might have found in the request parameters
                if (mpinfo.containsKey(AsyncConstants.KEY_USER)) {
                  username = (String) mpinfo.get(AsyncConstants.KEY_USER);
               }
                if (mpinfo.containsKey(AsyncConstants.KEY_AGENT)) {
                  agentname = (String) mpinfo.get(AsyncConstants.KEY_AGENT);
               }
                if (mpinfo.containsKey(AsyncConstants.KEY_PASSWORD)) {
                  pwhash = (String) mpinfo.get(AsyncConstants.KEY_PASSWORD);
               }

                mpinfo.put(AsyncConstants.KEY_FILEITEMS, fileItems);
            } catch (Exception ex) {
                throw new CentrifugeException(ex);
            }
        }
        // end multipart info collection

        /* Performing required authorization */
        authorizeUser(request, agentname, username, pwhash);

        mpinfo.put(AsyncConstants.KEY_IS_MULTI_PART, Boolean.valueOf(isMultipart));
        return mpinfo;
    }

    private static void authorizeUser(HttpServletRequest request, String agentname, String username, String pwhash) throws AuthorizationException {
        boolean authorized = false;
        String errorMsg = null;

        String[] agentroles = (String[]) AsyncConfiguration.getAttribute(AsyncConstants.KEY_INIT_CONFIG_AUTHORIZED_ROLES);
        if (null == agentroles) {
            agentroles = new String[] { "administrators" };
        }

        // see if we should auto register unknown users
        String wrk = (String) AsyncConfiguration.getAttribute(AsyncConstants.KEY_INIT_CONFIG_AUTO_REGISTER_UNKNOWN_USER);
        if (null == wrk) {
            wrk = "false";
        }
        boolean autoregister = wrk.equalsIgnoreCase("true");

        //
        // If there is a Principal name, we can assume that all auth/auth
        // has already occurred. If the Principal name is present, it is
        // used to identify the user's folder in the user file space. If
        // a user parameter is also specified and it does not match the
        // Principal name, we have to check that the Principal is allowed
        // to act on behalf of the user specified in the user parameter and
        // reject the request if the Principal is not authorized.
        //
        String prname = CsiSecurityManager.getUserName();
        if (null != prname) {
            agentname = prname;
            // user can do things in his own user folder
            if ((null == username) || (prname.equals(username))) {
                authorized = true;
                username = prname;
            }
            // otherwise check to see if principal is allowed to do things
            // in the named user's folder
            else {
                //
                // check to see if the username is known to the server. if it is,
                // continue. if not, we have to see if we should auto register
                // the username. this decision is based on the servlet init
                // parameter AutoRegisterUnknownUser. The default is false.
                //
                boolean knownuser = CsiSecurityManager.isRegisteredUser(username);
                if (knownuser) {
                    authorized = CsiSecurityManager.hasAnyRole(agentroles);
                } else {
                    if (autoregister) {
                        authorized = CsiSecurityManager.hasAnyRole(agentroles);
                        if (authorized) {
                            boolean added = addUser(username);
                            if (!added) {
                                errorMsg = " Cannot auto-register unknown user " + username + "; licensed user count exceeded or internal error occurred";
                                throw new AuthorizationException(errorMsg);
                            }
                        }
                    } else {
                        authorized = false;
                    }
                }
            }
        }
        //
        // If there is no Principal name, it means that this servlet has
        // been invoked by way of an unprotected URL. This means that we
        // will examine the invoking parameters for user identification and
        // password credentials.
        //
        else {
            // if both agent and user name are present, see if they're the same.
            // if so, ignore agentname
            if ((null != agentname) && (null != username)) {
                if (agentname.equals(username)) {
                    agentname = null;
                }
            }
            authorized = CsiSecurityManager.userOrAgentHasAnyRole(agentname, username, pwhash, agentroles);
            if ((null != agentname) && (null == username)) {
               username = agentname;
            }
            // if authorized and (agentname != username) we need to see if the username
            // is known to the server. if it is, continue. if not, we have to see if we
            // should auto register the username. this decision is based on the servlet
            // init parameter AutoRegisterUnknownUser. The default is false.
            if ((authorized) && (null != username) && (null != agentname)) {
                if (!username.equals(agentname)) {
                    boolean knownuser = CsiSecurityManager.isRegisteredUser(username);
                    if (!knownuser) {
                        if (autoregister) {
                            boolean added = addUser(username);
                            if (!added) {
                                errorMsg = "Cannot auto-register unknown user " + username + "; licensed user count exceeded or internal error occurred";
                                throw new AuthorizationException(errorMsg);
                            }
                        } else {
                            authorized = false; // disallow since user is not known and autoregister is off
                        }
                    }
                }
            }
        }

        // end this quickly if authorization checks fail.
        if (!authorized) {
           LOG.debug("User " + agentname + " attempted to access files on behalf of user " + username + "; not authorized or user does not exist");
            errorMsg = "Cannot complete requested operation, access denied";
            throw new AuthorizationException(errorMsg);
        }
    }

   private static boolean addUser(String username) {
      boolean rc = false;

      try {
         if (Product.getLicense().addingUserAllowed((int) Users.getUserCount())) {
            User user = new User();

            user.setName(username);
            user.setRemark("Auto-registered by " + HANDLER_ID);

            SecurityPolicyConfig policy = Configuration.getInstance().getSecurityPolicyConfig();

            policy.applyExpirationPolicy(user);
            Users.add(user);

            LOG.debug("Previously unknown user {} auto-registered by {}", () -> username, () -> HANDLER_ID);
            // GroupDAOBean bean = new GroupDAOBean();
            // Group group = bean.findByName( RoleSupport.AUTHENTICATED_ROLE );
            // if( group != null ) {
            // group.getMembers().add( user );
            // }
            // bean.merge( group );
            rc = true;
         } else {
            LOG.debug("Cannot auto-register unknown user {}; licensed user count ({}) exceeded",
                      () -> username, () -> Integer.toString(Product.getLicense().getUserCount()));
         }
      } catch (CentrifugeException cex) {
         CsiPersistenceManager.markForRollback();
         LOG.debug("{} encountered errors auto-registering user {}", () -> HANDLER_ID, () -> username);
         cex.printStackTrace();
      }
      return rc;
   }
}
