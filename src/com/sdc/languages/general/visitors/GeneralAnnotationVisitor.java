package com.sdc.languages.general.visitors;

import com.sdc.languages.general.languageParts.Annotation;
import org.objectweb.asm.AnnotationVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

public class GeneralAnnotationVisitor extends AnnotationVisitor {
    protected Annotation myAnnotation;

    public GeneralAnnotationVisitor(final Annotation annotation) {
        super(ASM4);
        this.myAnnotation = annotation;
    }

    @Override
    public void visit(final String name, final Object value) {
        myAnnotation.addProperty(name, value);
        super.visit(name, value);
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        super.visitEnum(name, desc, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        return super.visitAnnotation(name, desc);
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        return super.visitArray(name);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
