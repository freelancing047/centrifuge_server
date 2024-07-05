package csi.client.rebind;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class CentrifugeConstantsGenerator extends Generator {
	public static final String STUB_SUFFIX = "_StubImpl";

    class HookupLogic {

        private static final int MAGIC_MAX = 50000;

        private int inputSize;
        private List<String> inputList;
        private List<List<String>> listOfLists;

        public HookupLogic() {

            inputSize = 0;
            listOfLists = new ArrayList<List<String>>();
            inputList = new ArrayList<String>();
            listOfLists.add(inputList);
        }

        public int getInitializationCount() {

            return listOfLists.size();
        }

        public void add(String lineIn) {

            if (MAGIC_MAX < inputSize) {

                inputSize = 0;
                inputList = new ArrayList<String>();
                listOfLists.add(inputList);
            }
            inputList.add(lineIn);
            inputSize += lineIn.length();
        }

        public void addPair(String lineOneIn, String lineTwoIn) {

            if (MAGIC_MAX < inputSize) {

                inputSize = 0;
                inputList = new ArrayList<String>();
                listOfLists.add(inputList);
            }
            inputList.add(lineOneIn);
            inputSize += lineOneIn.length();
            inputList.add(lineTwoIn);
            inputSize += lineTwoIn.length();
        }

        public List<String> getList(int indexIn) {

            return listOfLists.get(indexIn);
        }
    }

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		try {
        	TypeOracle typeOracle = context.getTypeOracle();
        	JClassType classType = typeOracle.findType(typeName);
            String packageName = classType.getPackage().getName();
            String className = classType.getSimpleSourceName() + STUB_SUFFIX;
            logger.log(Type.INFO, "Generating CentrifugeConstants impl " + packageName + "." + className);

            generateClass(logger, context, packageName, className, classType);

            return packageName + "." + className;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	private void generateClass(TreeLogger logger, GeneratorContext context, String packageName, String className,
            JClassType interfaceType) {
        PrintWriter printWriter = context.tryCreate(logger, packageName, className);

        if (printWriter == null) {
            // We've already created this class.
            return;
        }

        ClassSourceFileComposerFactory composer = null;
        composer = new ClassSourceFileComposerFactory(packageName, className);
        composer.addImplementedInterface(interfaceType.getName());
        composer.addImport(java.util.Map.class.getName());

        SourceWriter sourceWriter = null;
        sourceWriter = composer.createSourceWriter(context, printWriter);

        Set<JMethod> methods = RebindUtils.getAllMethodsOfInterface(interfaceType);

        HookupLogic hookupLogic = new HookupLogic();
        // For each method, implement it!
        for (JMethod method : methods) {
        	if (!method.getName().equals("initialize")) sourceWriter.println(implementMethod(interfaceType, method, hookupLogic));
        }
        buildInitializationLogic(sourceWriter, hookupLogic);
        sourceWriter.println("}"); // end of class.
        context.commit(logger, printWriter);
    }

    private void buildInitializationLogic(SourceWriter sourceWriterIn, HookupLogic hookupLogicIn) {

        int myMethodCount = hookupLogicIn.getInitializationCount();

        // Generate top level initialization routine
        sourceWriterIn.println("public void initialize(Map<String, String> properties) {");
        for (int i = 1; myMethodCount >= i; i++) {

            sourceWriterIn.println("  initialize_" + Integer.toString(i) + "(properties);");
        }
        sourceWriterIn.println("}" );

        // Generate lower level initialization routines
        for (int i = 1; myMethodCount >= i; i++) {

            List<String> myList = hookupLogicIn.getList(i - 1);

            sourceWriterIn.println("public void initialize_" + Integer.toString(i) + "(Map<String, String> properties) {");

            for (String myLine : myList) {

                sourceWriterIn.println(myLine);
            }
            sourceWriterIn.println("}" );
        }
    }

    private String implementMethod(JClassType interfaceType, JMethod method, HookupLogic hookupLogic) {
        boolean voidMethod = method.getReturnType().equals(JPrimitiveType.VOID);

        // Create signature
        StringBuilder methodDeclaration = new StringBuilder();
        methodDeclaration.append("public String ").append(method.getName()).append("(");

        // Add formal parameters to declaration. While we do this, also accumulate parameters into signature for
        // passing to server and to the stub-body's assignment array.
        StringBuilder paramList = new StringBuilder();
        StringBuilder paramInfo = new StringBuilder();
        int count = 0;
        for (JParameter param : method.getParameters()) {
        	if (count == 0) {
        		paramInfo.append("Parameter[] "+method.getName()+"_P = new Parameter[" + method.getParameters().length + "];");
        	} else if (count > 0) {
                methodDeclaration.append(", ");
                paramList.append(", ");
            }
            methodDeclaration.append(param.getType().getQualifiedSourceName()).append(" ").append(param.getName());
            paramList.append(param.getName());
            paramInfo.append(method.getName()+"_P["+ count +"] = new Parameter(\""+param.getName()+"\", \""+param.getType().getQualifiedSourceName()+"\");");
            // Add to assignment string
            count++;
        }
        methodDeclaration.append(") {");

        if (!voidMethod) {
            if (method.getReturnType().isPrimitive() != null) {
                methodDeclaration.append("return ").append(method.getReturnType().isPrimitive().getUninitializedFieldExpression()).append(";");
            } else {
            	if (method.getParameters().length == 0) {
                	methodDeclaration.append("return "+method.getName()+"_internal_functional.run();");
                	hookupLogic.add("set"+method.getName()+"_internal_functional(InternationalizationFactory.createConstantFunction(\"" + method.getName() + "\", properties.get(\"" + method.getName() + "\")));");
                } else {
                	methodDeclaration.append("return "+method.getName()+"_internal_functional.run("+ paramList.toString() +");");
                	hookupLogic.addPair(paramInfo.toString(),
                	"set"+method.getName()+"_internal_functional(InternationalizationFactory.createMessagesFunction(\"" + method.getName() + "\", properties.get(\"" + method.getName() + "\"),"+method.getName()+"_P));");
                }
            }
        }

        methodDeclaration.append("}\n\n");
        if (method.getParameters().length == 0) {
        	methodDeclaration.append("private ConstantFunction "+method.getName()+"_internal_functional;");
        	methodDeclaration.append("private void set"+method.getName()+"_internal_functional(ConstantFunction f){"+method.getName()+"_internal_functional = f;"+"}");
        } else {
        	methodDeclaration.append("private MessagesFunction "+method.getName()+"_internal_functional;");
        	methodDeclaration.append("private void set"+method.getName()+"_internal_functional(MessagesFunction f){"+method.getName()+"_internal_functional = f;"+"}");
        }
        return methodDeclaration.toString();
    }

    private void logAndGenLine(SourceWriter sourceWriterIn, String lineIn) {

        System.out.println(lineIn);
        sourceWriterIn.println(lineIn);
    }
}
