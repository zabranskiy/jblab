package com.sdc.languages.kotlin.languageParts;

import KotlinPrinters.KotlinPrinter;
import com.sdc.languages.general.languageParts.Method;
import pretty.PrettyPackage;

import com.sdc.languages.general.languageParts.GeneralClass;
import com.sdc.ast.expressions.Expression;

import java.util.Arrays;
import java.util.List;

public class KotlinClass extends GeneralClass {
    public final static String INHERITANCE_IDENTIFIER = ":";

    private Method myConstructor;
    private Expression mySuperClassConstructor;
    private String mySrcClassName;

    public KotlinClass(final String modifier, final ClassType type, final String name, final String packageName,
                       final List<String> traits, final String superClass,
                       final List<String> genericTypes, final List<String> genericIdentifiers,
                       final int textWidth, final int nestSize)
    {
        super(modifier, type, name, packageName, traits, superClass, genericTypes, genericIdentifiers, textWidth, nestSize);
        this.myDefaultPackages = Arrays.asList(myPackage, "java.lang", "jet", "jet.runtime");
    }

    public void setConstructor(final Method constructor) {
        this.myConstructor = constructor;
    }

    public void setSuperClassConstructor(final Expression superClassConstructor) {
        this.mySuperClassConstructor = superClassConstructor;
    }

    public Method getConstructor() {
        return myConstructor;
    }

    public Expression getSuperClassConstructor() {
        return mySuperClassConstructor;
    }

    protected String getInheritanceIdentifier() {
        return INHERITANCE_IDENTIFIER;
    }

    public void removeMethod(final Method method) {
        myMethods.remove(method);
    }

    public void setSrcClassName(final String srcClassName) {
        this.mySrcClassName = srcClassName;
    }

    public String getSrcClassName() {
        return mySrcClassName;
    }

    public void addAssignmentToField(final String fieldName) {
        ((KotlinClassField) getField(fieldName)).addAssignment();
    }

    @Override
    public void appendImport(final String importName) {
        if (!importName.contains(".src.") && !importName.endsWith("KotlinPackage")) {
            super.appendImport(importName);
        }
    }

    @Override
    public String getTypeToString() {
        switch (myType) {
            case INTERFACE:
                return "trait ";
            default:
                return super.getTypeToString();
        }
    }

    @Override
    public String toString() {
        return PrettyPackage.pretty(myTextWidth, (new KotlinPrinter()).printClass(this));
    }
}
