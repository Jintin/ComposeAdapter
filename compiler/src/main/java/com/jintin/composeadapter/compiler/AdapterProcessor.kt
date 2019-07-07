package com.jintin.composeadapter.compiler

import com.google.auto.service.AutoService
import com.jintin.composeadapter.annotations.ViewHolder
import com.jintin.composeadapter.annotations.ViewHolders
import com.squareup.javapoet.ClassName
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic


@AutoService(Processor::class)
class AdapterProcessor : AbstractProcessor() {
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        val result = HashMap<ClassName, MutableList<ViewHolderInfo>>()
        for (annotatedElement in roundEnvironment.getElementsAnnotatedWith(ViewHolders::class.java)) {
            if (annotatedElement.kind != ElementKind.CLASS) {
                error("Only class can be annotated with ViewHolder", annotatedElement)
                return false
            }
            val className = getClassName(annotatedElement)
            val infoList = result.getOrDefault(className, mutableListOf())
            val holders = annotatedElement.getAnnotation(ViewHolders::class.java)
            for (holder in holders.value.reversed()) {
                transform(holder)?.let { info ->
                    infoList.add(info)
                }
            }
            result[className] = infoList
        }
        for (annotatedElement in roundEnvironment.getElementsAnnotatedWith(ViewHolder::class.java)) {
            if (annotatedElement.kind != ElementKind.CLASS) {
                error("Only class can be annotated with ViewHolder", annotatedElement)
                return false
            }
            val className = getClassName(annotatedElement)
            val infoList = result.getOrDefault(className, mutableListOf())
            val holder = annotatedElement.getAnnotation(ViewHolder::class.java)
            transform(holder)?.let { info ->
                infoList.add(info)
            }
            result[className] = infoList
        }

        AdapterBuilder(filer, result).generate()
        return true
    }

    override fun getSupportedAnnotationTypes() = setOf(
        ViewHolder::class.java.canonicalName,
        ViewHolders::class.java.canonicalName
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    private fun error(message: String, element: Element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    private fun transform(holder: ViewHolder): ViewHolderInfo? {
        try {
            holder.model
        } catch (e: MirroredTypeException) {
            val className = getClassName(e.typeMirror)
            val viewType = "TYPE_" + className.simpleName().toUpperCase()
            return ViewHolderInfo(className, viewType, holder.layout)
        }
        return null
    }

    private fun getClassName(typeMirror: TypeMirror): ClassName {
        val rawString = typeMirror.toString()
        val dotPosition = rawString.lastIndexOf(".")
        val packageName = rawString.substring(0, dotPosition)
        val className = rawString.substring(dotPosition + 1)
        return ClassName.get(packageName, className)
    }

    private fun getClassName(annotatedElement: Element): ClassName {
        val typeElement = annotatedElement as TypeElement
        val simpleName = typeElement.simpleName.toString()
        return ClassName.get(
            typeElement.qualifiedName.toString().replace(".$simpleName", ""),
            simpleName
        )
    }
}
