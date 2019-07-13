package com.jintin.composeadapter.compiler

import com.squareup.javapoet.ClassName


class BindHolderInfo(
    val className: ClassName,
    val viewType: String,
    var layoutId: Int
)