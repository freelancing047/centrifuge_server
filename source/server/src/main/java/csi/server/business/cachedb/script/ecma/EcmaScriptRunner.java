package csi.server.business.cachedb.script.ecma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.FeatureToggleConfiguration;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.IDataRow;
import csi.server.common.data.LRUCache;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.FieldDef;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.util.CsiTypeUtil;

public class EcmaScriptRunner implements CsiScriptRunner {
   private static final Logger LOG = LogManager.getLogger(EcmaScriptRunner.class);

   private static final Pattern REFERENCE_PATTERN = Pattern.compile("([\\.\\\\\\?\\*\\+\\&\\:\\{\\}\\[\\]\\(\\)\\^\\$])");

    class DummyBindings implements Bindings {

        @Override
        public Object put(String name, Object value) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> toMerge) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<String> keySet() {
            return null;
        }

        @Override
        public Collection<Object> values() {
            return null;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }
    }
    private static String FIELD_REF_SEARCH_FORMAT = "(?i)csiRow\\.get\\s*\\(\\s*['\"]{1}\\s*%1$s\\s*['\"]{1}\\s*\\)";
    private static String FIELD_REF_FORMAT = "csiRow.get('%1$s')";

    private static String ECMA_ENGINE_NAME = "ECMAScript";
    private static ScriptEngineManager manager = null;
    private ScriptEngine scriptEngine;
    private transient LRUCache<String, CompiledScript> scriptCache;
    private static Class nativeObjectClass = null;
    private Bindings libraryBindings = null;
    private static Method nativeUnwrapMethod = null;
    private static Class undefinedClass = null;
    static {
        try {
            nativeObjectClass = Class.forName("sun.org.mozilla.javascript.internal.NativeJavaObject");
            nativeUnwrapMethod = nativeObjectClass.getMethod("unwrap");
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }

        try {
            undefinedClass = Class.forName("sun.org.mozilla.javascript.internal.Undefined");
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }
    private static Boolean _doScripting = null;

    public EcmaScriptRunner() {
        if (doScripting()) {
            if (manager == null) {
                manager = new ScriptEngineManager();
            }
            this.scriptEngine = manager.getEngineByName(ECMA_ENGINE_NAME);
            this.scriptCache = new LRUCache<String, CompiledScript>(1000);
        }
    }

    @Override
   public Bindings createBindings() {
        if (doScripting()) {
            Bindings bindings = scriptEngine.createBindings();
            // Add JS library files
            loadGlobalScripts(scriptEngine, bindings);
            return bindings;
        }
        return new DummyBindings();
    }

    @Override
   public Bindings getGlobalBindings() {
        if (doScripting()) {
            return this.scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        }
        return new DummyBindings();
    }

   public boolean evalConditional(ConditionalExpression cond, Bindings bindings) throws CentrifugeException {
      return doScripting() && ((Boolean) evalExpression(cond.getUuid(), cond.getExpression(), bindings)).booleanValue();
   }

   @Override
   public String beautifyScript(String script) throws CentrifugeException {
      String result = null;

      if (doScripting()) {
         // indent_size, indent_character, indent_level
         result = (String) invokeGlobalFunction("js_beautify", script, Integer.valueOf(1), "    ", Integer.valueOf(0));
      }
      return result;
   }

    public Object invokeGlobalFunction(String function, Object... args) throws CentrifugeException {
        if (doScripting()) {
            String eMsg;
            Exception ex;

            try {
                return ((Invocable) scriptEngine).invokeFunction(function, args);
            } catch (ScriptException e) {
                eMsg = "Failed to execute function '" + function + "'";
                ex = e;
            } catch (NoSuchMethodException e) {
                eMsg = "Function '" + function + "' not found";
                ex = e;
            }
            // log.warn( eMsg, ex);
            //CentrifugeException ce = new CentrifugeException(eMsg, ex);
            // ce.setLogged( true );
            throw new CentrifugeException(eMsg, ex);
        }
        return null;
    }

    @Override
   public Object invokeMethod(Object object, String method, Object... args) throws CentrifugeException {
        if (doScripting()) {
            String eMsg;
            Exception ex;
            try {
                return ((Invocable) scriptEngine).invokeMethod(object, method, args);
            } catch (ScriptException e) {
                eMsg = "Failed to execute function '" + method + "'";
                ex = e;
            } catch (NoSuchMethodException e) {
                eMsg = "Function '" + method + "' not found";
                ex = e;
            }

            throw new CentrifugeException(eMsg, ex);
        }
        return null;
    }

    public Object evalExpression(String expr) throws CentrifugeException {
        if (doScripting()) {
            return evalExpression(null, expr, null);
        }
        return null;
    }

    @Override
   public Object evalExpression(String expr, Bindings bindings) throws CentrifugeException {
        if (doScripting()) {
            return evalExpression(null, expr, bindings);
        }
        return null;
    }

    public Object evalExpression(String uuid, String expr, Bindings bindingsIn) throws CentrifugeException {

        if (doScripting()) {

            if (Configuration.getInstance().getFeatureToggleConfig().isScriptingEnabled()) {

                if (uuid != null) {
                    CompiledScript script = scriptCache.get(uuid);
                    if (script == null) {
                        script = compileScript(expr);
                        scriptCache.put(uuid, script);
                    }

                    try {
                        return script.eval(bindingsIn);

                    } catch (ScriptException e) {
                        throw new CentrifugeException("Script execution failed", e);
                    }
                } else {

                    try {
                        return scriptEngine.eval(expr, bindingsIn);
                    } catch (ScriptException e) {
                        throw new CentrifugeException("Script execution failed", e);
                    }
                }
            }
        }

        return null;
    }

    public CompiledScript compileScript(String expr) throws CentrifugeException {
        CompiledScript compiled = null;

        if (doScripting()) {
            try {
                compiled = ((Compilable) scriptEngine).compile(expr);
            } catch (ScriptException e) {
                throw new CentrifugeException(String.format("compileScript: error compiling '%s'", expr), e);
            }
        }
        return compiled;
    }

    @Override
    public Object evalScriptedField(FieldListAccess modelIn, FieldDef f, IDataRow rowSet) throws CentrifugeException {

        if (doScripting()) {
            Object myResult = null;
            String script = f.getScriptText();
            if (f.isRawScript()) {
                script = f.getScriptText();
            } else {
                List<ScriptFunction> funcs = f.getFunctions();
                if ((funcs != null) && !funcs.isEmpty()) {
                    script = funcs.get(0).generateScript(modelIn);
                }
            }

            if ((script != null) && (0 < script.trim().length())) {

                Bindings bindings = createLibraryContext(rowSet);
                evalExpression(f.getUuid(), script, bindings);
                Object result = bindings.get("csiResult");
                if (result != null) {
                    result = unwrap(result);
                    myResult = CsiTypeUtil.coerceType(result, f.getValueType(), f.getDisplayFormat());
                }
            }
            return ((null == myResult) && !f.isNullable() && (CsiDataType.String == f.getValueType())) ? "" : myResult;
        }
        return null;
    }

    @Override
    public String updateFieldReferences(String scriptText, String curName, String newName) throws CentrifugeException {
        if (doScripting()) {
            if (scriptText == null) {
                return null;
            }

            String search = String.format(FIELD_REF_SEARCH_FORMAT, curName);
            String replace = String.format(FIELD_REF_FORMAT, newName);
            return scriptText.replaceAll(search, replace);
        }
        return null;
    }

   @Override
   public boolean referencesField(String scriptText, String fieldName) {
      boolean result = false;

      if (doScripting() && (scriptText != null) && !scriptText.trim().isEmpty()) {
         String escaped = REFERENCE_PATTERN.matcher(fieldName).replaceAll("\\$1");
         String search = String.format(FIELD_REF_SEARCH_FORMAT, escaped);
         result = Pattern.compile(search).matcher(scriptText).find();
      }
      return result;
   }

    private Bindings createLibraryContext(IDataRow rowSetIn) {

        if (null == libraryBindings) {
            libraryBindings = this.createBindings();
            libraryBindings.put("csiRow", rowSetIn);
        }
        return libraryBindings;
    }

   private String loadFile(String filePathIn) throws FileNotFoundException, IOException {
      String result = null;

      try (FileInputStream fis = new FileInputStream(new File(filePathIn))) {
         result = IOUtils.toString(fis, "UTF-8");
      }
      return result;
   }

   private void loadGlobalScripts(ScriptEngine engineIn, Bindings bindingsIn) {
      List<String> globalScripts = new ArrayList<String>();

      globalScripts.add("beautify.js");
      globalScripts.add("csifunctions.js");

      for (String script : globalScripts) {
         try (InputStream stream = EcmaScriptRunner.class.getResourceAsStream(script);
              InputStreamReader reader = new InputStreamReader(stream)) {
            engineIn.eval(reader, bindingsIn);
         } catch (ScriptException se) {
            LOG.warn("Failed to load script file: " + script);
            throw new RuntimeException("Failed to initialize script engine: " + se.toString(), se);
         } catch (IOException ioe) {
            LOG.warn("Failed to load script file: " + script);
            throw new RuntimeException("Failed to initialize script engine: " + ioe.toString(), ioe);
         }
      }
   }

    private Object unwrap(Object value) {
        // TODO: We have to use reflection for now
        // till we figure out how to get ant to
        // compile classes that have reference to the
        // sun.org.mozilla.javascript.internal
        // package in java.home/jre/lib/rt.jar.
        //
        // Otherwise we could simply do the following:
        //
        // value = (value instanceof NativeJavaObject) ?
        // ((NativeJavaObject)value).unwrap() : value;
        // return !(value instanceof Undefined) ? value : null;
        //

        if ((nativeObjectClass != null) && (nativeObjectClass == value.getClass()) && (nativeUnwrapMethod != null)) {
            try {
                value = nativeUnwrapMethod.invoke(value);
            } catch (Exception e) {
                // ignore
            }
        }

        if ((undefinedClass != null) && (undefinedClass == value.getClass())) {
            return null;
        } else {
            return value;
        }
    }

    private boolean doScripting() {

        if (null == _doScripting) {
            FeatureToggleConfiguration myFeatureConfig = Configuration.getInstance().getFeatureToggleConfig();

            _doScripting = myFeatureConfig.isScriptingEnabled();
        }
        return _doScripting;
    }
}
