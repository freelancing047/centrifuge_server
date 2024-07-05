/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.rebind;

import java.io.PrintWriter;
import java.util.Set;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import csi.client.gwt.vortex.impl.AbstractVortexEnabledStub;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * Generates the stub implementation for each of the RpcEnable derivative interfaces.
 * 
 * @author Centrifuge Systems, Inc.
 */
public class VortexServiceStubGenerator {

    public static final String STUB_SUFFIX = "_StubImpl";


    public String generate(TreeLogger logger, GeneratorContext context, JClassType classType)
            throws UnableToCompleteException {
        try {
            String packageName = classType.getPackage().getName();
            String className = classType.getSimpleSourceName() + STUB_SUFFIX;
            logger.log(Type.INFO, "Generating VortexService stub impl " + packageName + "." + className);

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
        composer.setSuperclass(AbstractVortexEnabledStub.class.getName());
        composer.addImplementedInterface(interfaceType.getName());
        composer.addImport(SerializableValueImpl.class.getName());

        SourceWriter sourceWriter = null;
        sourceWriter = composer.createSourceWriter(context, printWriter);

        Set<JMethod> methods = RebindUtils.getAllMethodsOfInterface(interfaceType);

        // For each method, implement it!
        for (JMethod method : methods) {
            sourceWriter.println(implementMethod(interfaceType, method));
        }

        sourceWriter.println("}"); // end of class.
        context.commit(logger, printWriter);
    }


    private String implementMethod(JClassType interfaceType, JMethod method) {
        boolean voidMethod = method.getReturnType().equals(JPrimitiveType.VOID);

        // Create signature
        StringBuilder methodDeclaration = new StringBuilder();
        methodDeclaration.append("public ");
        if (voidMethod) {
            methodDeclaration.append("void");
        } else {
            methodDeclaration.append(method.getReturnType().getQualifiedSourceName());
        }
        methodDeclaration.append(" ").append(method.getName()).append("(");

        // Add formal parameters to declaration. While we do this, also accumulate parameters into signature for
        // passing to server and to the stub-body's assignment array.
        StringBuilder parameterSignature = new StringBuilder();
        StringBuilder parameterAssignments = new StringBuilder();
        int count = 0;
        for (JParameter param : method.getParameters()) {
            if (count > 0) {
                methodDeclaration.append(", ");
            }
            methodDeclaration.append(param.getType().getQualifiedSourceName()).append(" ").append(param.getName());
            // Add to assignment string
            parameterAssignments.append("SerializableValueImpl value" + count + " = new SerializableValueImpl();\n");
            parameterAssignments.append("params[").append(count).append("] = value" + count + ";\n");
            parameterAssignments.append("value" + count + ".setValue(" + param.getName() + ");\n");
            count++;
            parameterSignature.append(",").append(param.getType().getQualifiedSourceName());
        }
        methodDeclaration.append(") {");
        methodDeclaration.append("SerializableValueImpl[] params = new SerializableValueImpl[").append(count).append("];");
        methodDeclaration.append(parameterAssignments);

        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(interfaceType.getQualifiedSourceName()).append(".").append(method.getName());

        methodSignature.append("(");
        methodSignature.append(parameterSignature.length() > 0 ? parameterSignature.substring(1) : "");
        methodSignature.append(")");

        methodDeclaration.append("dispatchRequestToSender(\"").append(methodSignature).append("\",").append("params);");

        if (!voidMethod) {
            if (method.getReturnType().isPrimitive() != null) {
                methodDeclaration.append("return ")
                        .append(method.getReturnType().isPrimitive().getUninitializedFieldExpression()).append(";");
            } else {
                methodDeclaration.append("return null;");
            }
        }
        methodDeclaration.append("}\n\n");

        return methodDeclaration.toString();
    }
}
