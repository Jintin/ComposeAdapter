package com.jintin.composeadapter.compiler

import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier


class AdapterBuilder(
    private val filer: Filer,
    private val input: Map<ClassName, List<ViewHolderInfo>>
) {

    fun generate() {
        input.forEach { (className, infoList) ->
            val packageName = className.packageName()
            val name = getAdapterName(className)

            val typeSpec = generateClassBuilder(name)
            infoList.forEachIndexed { index, info ->
                typeSpec.addField(generateViewType(info.viewType, index))
            }
            val methodSpec = generateOnCreateViewHolder(infoList)
            typeSpec.addMethod(methodSpec.build())
            val javaFile = JavaFile.builder(packageName, typeSpec.build())
                .build()
            try {
                javaFile.writeTo(filer)
            } catch (_: IOException) {

            }
        }
    }

    private fun generateClassBuilder(name: String) =
        TypeSpec.classBuilder(name)
            .superclass(ParameterizedTypeName.get(ADAPTER, VIEW_HOLDER))
            .addModifiers(Modifier.ABSTRACT)
            .addModifiers(Modifier.PUBLIC)

    private fun generateViewType(viewType: String, index: Int) =
        FieldSpec.builder(
            ClassName.INT,
            viewType,
            Modifier.FINAL,
            Modifier.STATIC,
            Modifier.PROTECTED
        ).initializer(index.toString()).build()

    private fun MethodSpec.Builder.generateViewHolderBlock(info: ViewHolderInfo) {
        val viewType = info.viewType
        beginControlFlow("case $viewType:")
            .addCode(
                "\$T view = \$T.from(parent.getContext()).inflate(\$L, parent, false);\n",
                VIEW,
                LAYOUT_INFLATER,
                info.layoutId
            )
            .addCode("return new \$T(view);\n", info.className)
            .endControlFlow()
    }

    private fun generateOnCreateViewHolder(infoList: List<ViewHolderInfo>): MethodSpec.Builder {
        val methodSpec = MethodSpec.methodBuilder("onCreateViewHolder")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .addParameter(ParameterSpec.builder(VIEW_GROUP, "parent").build())
            .addParameter(ParameterSpec.builder(ClassName.INT, "viewType").build())
            .returns(VIEW_HOLDER)
            .beginControlFlow("switch (viewType)")
        infoList.forEach { info ->
            methodSpec.generateViewHolderBlock(info)
        }
        methodSpec.addCode("default: throw new RuntimeException(\"Not support type\" + viewType);\n")
        methodSpec.endControlFlow()

        return methodSpec
    }

    private fun getAdapterName(className: ClassName) =
        className.simpleName() + "Helper"

    companion object {
        private val VIEW_GROUP: ClassName = ClassName.get("android.view", "ViewGroup")
        private val ADAPTER: ClassName = ClassName.get("androidx.recyclerview.widget", "RecyclerView", "Adapter")
        private val VIEW_HOLDER: ClassName = ClassName.get("androidx.recyclerview.widget", "RecyclerView", "ViewHolder")
        private val LAYOUT_INFLATER: ClassName = ClassName.get("android.view", "LayoutInflater")
        private val VIEW: ClassName = ClassName.get("android.view", "View")
    }
}
