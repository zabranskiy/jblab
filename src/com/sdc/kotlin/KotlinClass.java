package com.sdc.kotlin;

import KotlinPrinter.KotlinPrinterPackage;
import com.sdc.ast.expressions.Expression;
import pretty.PrettyPackage;

import java.util.ArrayList;
import java.util.List;

public class KotlinClass {
    private final String myModifier;
    private final String myType;
    private final String myName;
    private final String myPackage;

    private final String mySuperClass;
    private final List<String> myTraits;

    private final List<String> myGenericTypes;
    private final List<String> myGenericIdentifiers;

    private List<KotlinClassField> myFields = new ArrayList<KotlinClassField>();
    private List<KotlinMethod> myMethods = new ArrayList<KotlinMethod>();

    private List<KotlinAnnotation> myAnnotations = new ArrayList<KotlinAnnotation>();

    private KotlinMethod myConstructor;
    private Expression mySuperClassConstructor;

    private List<String> myImports = new ArrayList<String>();

    private boolean myIsNormalClass = true;

    private final int myTextWidth;
    private final int myNestSize;

    public KotlinClass(final String modifier, final String type, final String name, final String packageName,
                       final List<String> traits, final String superClass,
                       final List<String> genericTypes, final List<String> genericIdentifiers,
                       final int textWidth, final int nestSize) {
        this.myModifier = modifier;
        this.myType = type;
        this.myName = name;
        this.myPackage = packageName;
        this.myTraits = traits;
        this.mySuperClass = superClass;
        this.myGenericTypes = genericTypes;
        this.myGenericIdentifiers = genericIdentifiers;
        this.myTextWidth = textWidth;
        this.myNestSize = nestSize;
    }

    public String getModifier() {
        return myModifier;
    }

    public String getType() {
        return myType;
    }

    public String getName() {
        return myName;
    }

    public String getPackage() {
        return myPackage;
    }

    public List<String> getImports() {
        return myImports;
    }

    public List<String> getTraits() {
        return myTraits;
    }

    public String getSuperClass() {
        return mySuperClass;
    }

    public List<KotlinClassField> getFields() {
        return myFields;
    }

    public List<KotlinMethod> getMethods() {
        return myMethods;
    }

    public void setIsNormalClass(boolean isNormalClass) {
        myIsNormalClass = isNormalClass;
    }

    public void setConstructor(final KotlinMethod constructor) {
        this.myConstructor = constructor;
    }

    public void setSuperClassConstructor(final Expression superClassConstructor) {
        this.mySuperClassConstructor = superClassConstructor;
    }

    public KotlinMethod getConstructor() {
        return myConstructor;
    }

    public Expression getSuperClassConstructor() {
        return mySuperClassConstructor;
    }

    public boolean isNormalClass() {
        return myIsNormalClass;
    }

    public int getNestSize() {
        return myNestSize;
    }

    public void appendField(final KotlinClassField field) {
        myFields.add(field);
    }

    public void appendMethod(final KotlinMethod method) {
        myMethods.add(method);
    }

    public void appendImports(final List<String> imports) {
        for (final String importName : imports) {
            appendImport(importName);
        }
    }

    public void appendImport(final String importName) {
        if (!myImports.contains(importName)
                && (importName.indexOf(myPackage) != 0 || importName.lastIndexOf(".") != myPackage.length())
                && (importName.indexOf("jet.") != 0 || importName.lastIndexOf(".") != "jet".length())
                && (importName.indexOf("java.lang.") != 0 || importName.lastIndexOf(".") != "java.lang".length())
                && (importName.indexOf("jet.runtime.") != 0 || importName.lastIndexOf(".") != "jet.runtime".length()))
        {
            myImports.add(importName);
        }
    }

    public void appendAnnotation(final KotlinAnnotation annotation) {
        myAnnotations.add(annotation);
    }

    public List<KotlinAnnotation> getAnnotations() {
        return myAnnotations;
    }

    public boolean isGenericType(final String className) {
        return myGenericTypes.contains(className);
    }

    public String getGenericIdentifier(final String className) {
        return myGenericIdentifiers.get(myGenericTypes.indexOf(className));
    }

    public List<String> getGenericDeclaration() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < myGenericTypes.size(); i++) {
            if (!myGenericTypes.get(i).equals("java/lang/Object")) {
                final String[] classParts = myGenericTypes.get(i).split("/");
                result.add(myGenericIdentifiers.get(i) + " : " + classParts[classParts.length - 1]);
            } else {
                result.add(myGenericIdentifiers.get(i));
            }
        }
        return result;
    }

    public void addInitializerToField(final String fieldName, final Expression initializer) {
        getField(fieldName).setInitializer(initializer);
    }

    public KotlinClassField getField(final String fieldName) {
        for (KotlinClassField field : myFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public boolean hasField(final String fieldName) {
        for (KotlinClassField field : myFields) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return PrettyPackage.pretty(myTextWidth, KotlinPrinterPackage.printKotlinClass(this));
    }
}
