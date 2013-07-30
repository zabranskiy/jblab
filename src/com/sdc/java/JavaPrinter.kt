package JavaPrinter

import pretty.*

import com.sdc.java.JavaClass
import com.sdc.java.JavaClassField
import com.sdc.java.JavaMethod
import com.sdc.java.JavaAnnotation

import com.sdc.abstractLanguage.AbstractClass
import com.sdc.abstractLanguage.AbstractMethod
import com.sdc.abstractLanguage.AbstractClassField
import com.sdc.abstractLanguage.AbstractAnnotation
import com.sdc.abstractLanguage.AbstractPrinter


class JavaPrinter: AbstractPrinter() {
    override fun printAnnotationIdentifier(): PrimeDoc = text("@")

    override fun printClass(decompiledClass: AbstractClass): PrimeDoc {
        val javaClass: JavaClass = decompiledClass as JavaClass

        val packageCode = text("package " + javaClass.getPackage() + ";")
        var imports = group(nil())
        for (importName in javaClass.getImports()!!.toArray())
            imports = group(imports / text("import " + importName + ";"))

        var declaration : PrimeDoc = group(printAnnotations(javaClass.getAnnotations()!!.toList()) + text(javaClass.getModifier() + javaClass.getType() + javaClass.getName()))

        val genericsCode = printGenerics(javaClass.getGenericDeclaration())
        declaration = declaration + genericsCode

        val superClass = javaClass.getSuperClass()
        if (!superClass!!.isEmpty())
            declaration = group(declaration + nest(javaClass.getNestSize(), line() + text("extends " + superClass)))

        val implementedInterfaces = javaClass.getImplementedInterfaces()!!.toArray()
        if (!implementedInterfaces.isEmpty())
            declaration = group(
                    declaration
                    + nest(2 * javaClass.getNestSize(), line() + text("implements " + implementedInterfaces.get(0)))
            )
        for (interface in implementedInterfaces.drop(1)) {
            declaration = group(
                    (declaration + text(","))
                    + nest(2 * javaClass.getNestSize(), line() + text(interface as String))
            )
        }

        var javaClassCode = group(packageCode + imports / declaration + text(" {"))

        for (classField in javaClass.getFields()!!.toArray())
            javaClassCode = group(
                    javaClassCode
                    + nest(javaClass.getNestSize(), line() + printField(classField as JavaClassField))
            )

        for (classMethod in javaClass.getMethods()!!.toArray())
            javaClassCode = group(
                    javaClassCode
                    + nest(javaClass.getNestSize(), line() + printMethod(classMethod as JavaMethod))
            )

        return group(javaClassCode / text("}"))
    }

    override fun printMethod(decompiledMethod: AbstractMethod): PrimeDoc {
        val classMethod: JavaMethod = decompiledMethod as JavaMethod

        var declaration : PrimeDoc = group(printAnnotations(classMethod.getAnnotations()!!.toList()) + text(classMethod.getModifier()))

        val genericsCode = printGenerics(classMethod.getGenericDeclaration())

        declaration = group(declaration + genericsCode + text(classMethod.getReturnType() + classMethod.getName() + "("))

        var throwsExceptions = group(nil())
        val exceptions = classMethod.getExceptions()
        if (!exceptions!!.isEmpty()) {
            throwsExceptions = group(text("throws " + exceptions.get(0)))
            for (exception in exceptions.drop(1)) {
                throwsExceptions = group((throwsExceptions + text(",")) / text(exception))
            }
            throwsExceptions = group(nest(2 * classMethod.getNestSize(), line() + throwsExceptions))
        }

        var arguments: PrimeDoc = printMethodParameters(classMethod)

        val body = nest(
                       classMethod.getNestSize(),
                       printStatements(classMethod.getBody(), classMethod.getNestSize())
                   ) / text("}")

        return group(declaration + arguments + text(")") + throwsExceptions + text(" {")) + body
    }

    override fun printField(decompiledField: AbstractClassField): PrimeDoc {
        val classField: JavaClassField = decompiledField as JavaClassField

        var fieldCode : PrimeDoc = text(classField.getModifier() + classField.getType() + classField.getName())
        if (classField.hasInitializer())
            fieldCode = fieldCode + text(" = ") + printExpression(classField.getInitializer(), classField.getNestSize())
        return fieldCode + text(";")
    }
}