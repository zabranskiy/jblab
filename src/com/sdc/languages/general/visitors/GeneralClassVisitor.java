package com.sdc.languages.general.visitors;

import com.sdc.ast.Type;
import com.sdc.languages.general.languageParts.*;
import com.sdc.util.DeclarationWorker;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.sdc.languages.general.languageParts.GeneralClass.ClassType.*;
import static org.objectweb.asm.Opcodes.ASM4;

public abstract class GeneralClassVisitor extends ClassVisitor {
    protected GeneralClass myOuterClass;
    protected GeneralClass myDecompiledClass;
    protected final int myTextWidth;
    protected final int myNestSize;

    protected boolean myIsLambdaFunction = false;
    protected boolean myIsNestedClass = false;

    protected String myClassFilesJarPath = "";

    protected LanguagePartFactory myLanguagePartFactory;
    protected GeneralVisitorFactory myVisitorFactory;

    protected DeclarationWorker.SupportedLanguage myLanguage;

    protected Set<String> myVisitedClasses = new HashSet<String>();

    public GeneralClassVisitor(final int textWidth, final int nestSize) {
        super(ASM4);
        this.myTextWidth = textWidth;
        this.myNestSize = nestSize;
    }

    protected abstract String getDefaultImplementedInterface();

    protected abstract String getDefaultExtendedClass();

    protected abstract boolean checkForAutomaticallyGeneratedAnnotation(final String annotationName);

    public String getDecompiledCode() {
        return myDecompiledClass.toString();
    }

    public GeneralClass getDecompiledClass() {
        return myDecompiledClass;
    }

    public void setIsLambdaFunction(final boolean isLambdaFunction) {
        this.myIsLambdaFunction = isLambdaFunction;
    }

    public void setIsNestedClass(final boolean isNestedClass) {
        this.myIsNestedClass = isNestedClass;
    }

    public void setVisitedClasses(final Set<String> visitedClasses) {
        this.myVisitedClasses = visitedClasses;
    }

    public void setClassFilesJarPath(final String classFilesJarPath) {
        this.myClassFilesJarPath = classFilesJarPath;
    }

    public void setOuterClass(final GeneralClass outerClass) {
        this.myOuterClass = outerClass;
    }

    @Override
    public void visit(final int version, final int access, final String name
            , final String signature, final String superName, final String[] interfaces) {
        String modifier = DeclarationWorker.getAccess(access & ~Opcodes.ACC_SUPER, myLanguage);
        GeneralClass.ClassType type = SIMPLE_CLASS;

        if ((access & Opcodes.ACC_ENUM) == 0
                && (access & Opcodes.ACC_INTERFACE) == 0
                && (access & Opcodes.ACC_ANNOTATION) == 0) {
            type = SIMPLE_CLASS;
        } else if ((access & Opcodes.ACC_ENUM)!= 0){
            modifier = modifier.replace("final","");
            modifier = modifier.replace("enum","");
            modifier = modifier.trim() + " ";
            type = ENUM;
        }  else if((access & Opcodes.ACC_INTERFACE) != 0){
            modifier = modifier.replace("abstract ","");
            type = INTERFACE;
        }  else if((access & Opcodes.ACC_ANNOTATION) != 0){
            type = ANNOTATION;
        } else if((access & Opcodes.ACC_ABSTRACT) !=0){
            type = ABSTRACT_CLASS;
        }

        final String className = DeclarationWorker.decompileSimpleClassName(name);
        myVisitedClasses.add(className);

        StringBuilder packageName = new StringBuilder("");
        if (name.contains("/")) {
            final String[] classParts = name.split("/");
            for (int i = 0; i < classParts.length - 2; i++) {
                packageName.append(classParts[i]).append(".");
            }
            packageName.append(classParts[classParts.length - 2]);
        }

        String superClass = "";
        String superClassImport = "";
        if (superName != null && !getDefaultExtendedClass().equals(superName)) {
            superClass = DeclarationWorker.decompileClassNameWithOuterClasses(superName);
            superClassImport = DeclarationWorker.decompileClassNameForImport(superName);
        }

        List<String> implementedInterfaces = new ArrayList<String>();
        List<String> implementedInterfacesImports = new ArrayList<String>();
        if (interfaces != null && interfaces.length > 0) {
            for (final String implInterface : interfaces) {
                if (!implInterface.equals(getDefaultImplementedInterface())) {
                    implementedInterfaces.add(DeclarationWorker.decompileClassNameWithOuterClasses(implInterface));
                    implementedInterfacesImports.add(DeclarationWorker.decompileClassNameForImport(implInterface));
                }
            }
        }

        List<String> genericTypesList = new ArrayList<String>();
        List<String> genericIdentifiersList = new ArrayList<String>();
        List<String> genericTypesImports = new ArrayList<String>();
        DeclarationWorker.parseGenericDeclaration(signature, genericTypesList, genericIdentifiersList, genericTypesImports, myLanguage);

        myDecompiledClass = myLanguagePartFactory.createClass(modifier, type, className, packageName.toString(), implementedInterfaces
                , superClass, genericTypesList, genericIdentifiersList, myTextWidth, myNestSize);

        myDecompiledClass.setIsLambdaFunctionClass(myIsLambdaFunction);
        myDecompiledClass.setIsNestedClass(myIsNestedClass);
        myDecompiledClass.setFullClassName(DeclarationWorker.decompileFullClassName(name));
        myDecompiledClass.setOuterClass(myOuterClass);

        if (!superClassImport.isEmpty()) {
            myDecompiledClass.appendImport(superClassImport);
        }

        myDecompiledClass.appendImports(implementedInterfacesImports);
        myDecompiledClass.appendImports(genericTypesImports);
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        myDecompiledClass.setInnerClassIdentifier(decompileClassNameWithOuterClasses(owner), name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        List<String> annotationImports = new ArrayList<String>();
        final String annotationName = getDescriptor(desc, 0, annotationImports);

        if (!checkForAutomaticallyGeneratedAnnotation(annotationName)) {
            Annotation annotation = myLanguagePartFactory.createAnnotation();
            annotation.setName(annotationName);

            myDecompiledClass.appendAnnotation(annotation);
            myDecompiledClass.appendImports(annotationImports);

            return myVisitorFactory.createAnnotationVisitor(annotation);
        } else {
            return null;
        }
    }

    @Override
    public void visitAttribute(final Attribute attr) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        final String innerClassName = DeclarationWorker.decompileSimpleClassName(name);
        final String outerClassName = outerName == null ? null : DeclarationWorker.decompileSimpleClassName(outerName);

        if (!myVisitedClasses.contains(innerClassName)) {
            try {
                GeneralClassVisitor cv = myVisitorFactory.createClassVisitor(myDecompiledClass.getTextWidth(), myDecompiledClass.getNestSize());
                cv.setVisitedClasses(myVisitedClasses);
                cv.setClassFilesJarPath(myClassFilesJarPath);
                cv.setOuterClass(myDecompiledClass);
                cv.setIsNestedClass(true);

                ClassReader cr = getInnerClassClassReader(myClassFilesJarPath, name);
                cr.accept(cv, 0);

                GeneralClass decompiledClass = cv.getDecompiledClass();

                if (innerName != null) {
                    GeneralClass outerClass = myDecompiledClass.getOuterClass(outerClassName);
                    if (outerClass != null) {
                        outerClass.addInnerClass(innerClassName, decompiledClass);
                        if (outerClassName != null) {
                            decompiledClass.setInnerClassIdentifier(outerClassName, null, null);
                        }
                    }
                } else {
                    myDecompiledClass.addAnonymousClass(innerClassName, decompiledClass);
                }
            } catch (Exception e) {
                myDecompiledClass.addInnerClassError(name, e);
            }
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        List<String> fieldDeclarationImports = new ArrayList<String>();
        final String description = signature != null ? signature : desc;
        final String accessString = DeclarationWorker.getAccess(access, myLanguage);

        if (!accessString.contains("synthetic")) {
            final ClassField cf = myLanguagePartFactory.createClassField(accessString
                    , getDescriptor(description, 0, fieldDeclarationImports)
                    , name, myTextWidth, myNestSize);

            myDecompiledClass.appendField(cf);
            myDecompiledClass.appendImports(fieldDeclarationImports);
        }

        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc
            , final String signature, final String[] exceptions) {
        final String description = signature != null ? signature : desc;
        String modifier = DeclarationWorker.getAccess(access, myLanguage);
        if(myDecompiledClass.getType()==INTERFACE){
            modifier = modifier.replace("abstract ","");
        }

        List<String> throwedExceptions = new ArrayList<String>();
        if (exceptions != null) {
            for (final String exception : exceptions) {
                throwedExceptions.add(decompileClassNameWithOuterClasses(exception));
            }
        }

        List<String> genericTypesList = new ArrayList<String>();
        List<String> genericIdentifiersList = new ArrayList<String>();
        List<String> genericTypesImports = new ArrayList<String>();
        DeclarationWorker.parseGenericDeclaration(description, genericTypesList, genericIdentifiersList, genericTypesImports, myLanguage);

        String returnType;
        String methodName;
        if (name.equals("<init>")) {
            returnType = "";
            methodName = myDecompiledClass.getName();
        } else {
            List<String> methodReturnTypeImports = new ArrayList<String>();
            final int returnTypeIndex = description.indexOf(')') + 1;
            returnType = getDescriptor(description, returnTypeIndex, methodReturnTypeImports);
            methodName = name;
            myDecompiledClass.appendImports(methodReturnTypeImports);
        }

        final Method method = myLanguagePartFactory.createMethod(modifier, returnType, methodName, desc
                , throwedExceptions.toArray(new String[throwedExceptions.size()])
                , myDecompiledClass, genericTypesList, genericIdentifiersList
                , myTextWidth, myNestSize);

        myDecompiledClass.appendImports(genericTypesImports);

        final String parameters = description.substring(description.indexOf('(') + 1, description.indexOf(')'));
        final int startIndex = getStartIndexForParameters(method);

        if (myDecompiledClass.isNormalClass() && !modifier.contains("static")) {
            method.addThisVariable(new Type(getDescriptor("L" + myDecompiledClass.getName() + ";", 0, new ArrayList<String>())));
            method.declareThisVariable();
        }

        DeclarationWorker.addInformationAboutParameters(parameters, method, startIndex, myLanguage);

        myDecompiledClass.appendMethod(method);

        GeneralMethodVisitor methodVisitor = myVisitorFactory.createMethodVisitor(method, myDecompiledClass.getFullClassName(), myDecompiledClass.getSuperClass());
        methodVisitor.setClassFilesJarPath(myClassFilesJarPath);

        return new MethodVisitorStub(methodVisitor);
    }

    @Override
    public void visitEnd() {
        for (final Method method : myDecompiledClass.getMethods()) {
            myDecompiledClass.appendImports(method.getImports());
        }
    }

    protected int getStartIndexForParameters(final Method method) {
        return myDecompiledClass.isNormalClass() && !method.getModifier().contains("static") ? 1 : 0;
    }

    protected String decompileClassNameWithOuterClasses(final String fullClassName) {
        return myDecompiledClass.decompileClassNameWithOuterClasses(fullClassName);
    }

    protected String getDescriptor(final String descriptor, final int pos, List<String> imports) {
        return myDecompiledClass.getDescriptor(descriptor, pos, imports, myLanguage);
    }

    public static ClassReader getInnerClassClassReader(final String jarPath, final String fullClassName) throws IOException {
        if (jarPath.isEmpty()) {
            return new ClassReader(fullClassName);
        } else {
            return new ClassReader(getInnerClassInputStreamFromJarFile(jarPath, fullClassName));
        }
    }

    private static InputStream getInnerClassInputStreamFromJarFile(final String jarPath, final String fullClassName) throws IOException {
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        InputStream is = null;
        while (jarFileEntries.hasMoreElements()) {
            JarEntry file = jarFileEntries.nextElement();
            final String insideJarClassName = file.getName();
            if (insideJarClassName.equals(fullClassName + ".class")) {
                is = jarFile.getInputStream(file);
            }
        }

        return is;
    }
}
