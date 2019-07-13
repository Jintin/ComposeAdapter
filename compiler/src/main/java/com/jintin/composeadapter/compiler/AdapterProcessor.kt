package com.jintin.composeadapter.compiler

import com.google.auto.service.AutoService
import com.jintin.composeadapter.annotations.HolderLayout
import com.jintin.composeadapter.annotations.BindHolder
import com.jintin.composeadapter.annotations.BindHolders
import com.squareup.javapoet.ClassName
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
        val result = HashMap<ClassName, MutableList<BindHolderInfo>>()
        val layoutMap = HashMap<ClassName, Int>()
        for (annotatedElement in roundEnvironment.getElementsAnnotatedWith(HolderLayout::class.java)) {
            if (annotatedElement.kind != ElementKind.CLASS) {
                error(ERROR_HOLDER_LAYOUT, annotatedElement)
                return true
            }
            val className = getClassName(annotatedElement)
            val layout = annotatedElement.getAnnotation(HolderLayout::class.java)
            layoutMap[className] = layout.layout
        }
        for (annotatedElement in roundEnvironment.getElementsAnnotatedWith(BindHolders::class.java)) {
            if (annotatedElement.kind != ElementKind.CLASS) {
                error(ERROR_VIEW_HOLDERS, annotatedElement)
                return true
            }
            val className = getClassName(annotatedElement)
            val holders = annotatedElement.getAnnotation(BindHolders::class.java)
            for (holder in holders.value.reversed()) {
                if (!parseViewHolder(className, holder, layoutMap, result)) {
                    error(ERROR_HOLDER_CREATE, annotatedElement)
                    return true
                }
            }
        }
        for (annotatedElement in roundEnvironment.getElementsAnnotatedWith(BindHolder::class.java)) {
            if (annotatedElement.kind != ElementKind.CLASS) {
                error(ERROR_VIEW_HOLDER, annotatedElement)
                return true
            }
            val className = getClassName(annotatedElement)
            val holder = annotatedElement.getAnnotation(BindHolder::class.java)
            if (!parseViewHolder(className, holder, layoutMap, result)) {
                error(ERROR_HOLDER_CREATE, annotatedElement)
                return true
            }
        }

        AdapterBuilder(filer, result).generate()
        return true
    }

    private fun parseViewHolder(
        className: ClassName,
        holder: BindHolder,
        layoutMap: Map<ClassName, Int>,
        result: HashMap<ClassName, MutableList<BindHolderInfo>>
    ): Boolean {
        val infoList = result.getOrDefault(className, mutableListOf())
        transform(holder, layoutMap)?.let { info ->
            infoList.add(info)
        } ?: run {
            return false
        }
        result[className] = infoList
        return true
    }

    override fun getSupportedAnnotationTypes() = setOf(
        HolderLayout::class.java.canonicalName,
        BindHolder::class.java.canonicalName,
        BindHolders::class.java.canonicalName
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    private fun error(message: String, element: Element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    private fun transform(holder: BindHolder, layoutMap: Map<ClassName, Int>): BindHolderInfo? {
        try {
            holder.model
        } catch (e: MirroredTypeException) {
            val className = getClassName(e.typeMirror)
            val viewType = className.simpleName().toConstFormat()
            val layoutId = if (holder.layout == -1) {
                layoutMap[className] ?: return null
            } else {
                holder.layout
            }
            return BindHolderInfo(className, viewType, layoutId)
        }
        return null
    }

    private fun String.toConstFormat(): String {
        val regex = "([a-z])([A-Z]+)"
        val replacement = "$1_$2"
        return "TYPE_" + this.replace(regex.toRegex(), replacement).toUpperCase()
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

    companion object {
        private const val ERROR_HOLDER_LAYOUT = "Only class can be annotated with HolderLayout"
        private const val ERROR_VIEW_HOLDERS = "Only class can be annotated with BindHolders"
        private const val ERROR_VIEW_HOLDER = "Only class can be annotated with BindHolder"
        private const val ERROR_HOLDER_CREATE = "Can't generate BindHolder"
    }
}
