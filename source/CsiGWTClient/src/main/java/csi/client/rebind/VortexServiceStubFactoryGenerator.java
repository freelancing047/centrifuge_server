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
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import csi.shared.gwt.vortex.VortexService;

/**
 * Creates VortexService derivative interface stubs and creates a factory to instantiate them.
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortexServiceStubFactoryGenerator extends Generator {

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
            throws UnableToCompleteException {
        // First generate all client-side rpc stub interfaces.
        implementStubs(logger, context);

        // next generate the instantiator.
        return implementInstantiator(logger, context, typeName);
    }


    private String implementInstantiator(TreeLogger logger, GeneratorContext context, String typeName) {
        TypeOracle typeOracle = context.getTypeOracle();

        try {
            JClassType classType = typeOracle.getType(typeName);
            String packageName = classType.getPackage().getName();
            String className = classType.getSimpleSourceName() + "Impl";

            implement(logger, context, packageName, className, classType);

            return packageName + "." + className;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void implement(TreeLogger logger, GeneratorContext context, String packageName, String className,
            JClassType classType) {

        PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        if (printWriter == null) {
            // Already generated. Return.
            return;
        }

        ClassSourceFileComposerFactory composer = null;
        composer = new ClassSourceFileComposerFactory(packageName, className);
        composer.addImplementedInterface(classType.getName());
        composer.addImport(VortexService.class.getName());

        SourceWriter sourceWriter = null;
        sourceWriter = composer.createSourceWriter(context, printWriter);

        sourceWriter.println("public <V extends VortexService> V create(Class<V> clz)");
        sourceWriter.println("{");
        sourceWriter.println("String clzName = clz.getName();");

        TypeOracle typeOracle = context.getTypeOracle();
        JClassType vortexServiceType = typeOracle.findType(VortexService.class.getName());

        JClassType[] allTypes = typeOracle.getTypes();
        Set<JClassType> vortexServiceTypes = new HashSet<JClassType>();

        for (JClassType type : allTypes) {
            if (type.isInterface() != null && type.isAssignableTo(vortexServiceType)
                    && type.equals(vortexServiceType) == false && type.isGenericType() == null) {
                // See note in implementStubs method.
                vortexServiceTypes.add(type);
            }
        }

        for (JClassType aType : vortexServiceTypes) {
            sourceWriter.println("if (\"" + aType.getQualifiedSourceName() + "\".equals(clzName)) { return (V)new "
                    + aType.getQualifiedSourceName() + VortexServiceStubGenerator.STUB_SUFFIX + "(); }\n");
        }

        sourceWriter.println("throw new RuntimeException(\"RPC failure: "
                + "Could not find stub implementation for \" + clzName);");
        sourceWriter.println("}"); // end of method.

        sourceWriter.println("}"); // end of class.
        // commit generated class
        context.commit(logger, printWriter);
    }


    private void implementStubs(TreeLogger logger, GeneratorContext context) throws UnableToCompleteException {
        // Find all interfaces of type VortexService. Create stub implementations for each of those.
        TypeOracle typeOracle = context.getTypeOracle();
        JClassType vortexServiceType = typeOracle.findType(VortexService.class.getName());

        JClassType[] allTypes = typeOracle.getTypes();
        Set<JClassType> vortexServiceTypes = new HashSet<JClassType>();

        for (JClassType type : allTypes) {
            if (type.isInterface() != null && type.isAssignableTo(vortexServiceType)
                    && type.equals(vortexServiceType) == false && type.isGenericType() == null) {
                // Note: the isGenericType == null (i.e., the interface is not generic) check is to make sure
                // generified interfaces are always extended to a specific typed-interface before implementing them.
                // This is because the type-matches for a generic interface is potentially unbounded and that cannot
                // be properly handled as yet.
                vortexServiceTypes.add(type);
            }
        }

        for (JClassType jClassType : vortexServiceTypes) {
            VortexServiceStubGenerator generator = new VortexServiceStubGenerator();
            generator.generate(logger, context, jClassType);
        }
    }
}
