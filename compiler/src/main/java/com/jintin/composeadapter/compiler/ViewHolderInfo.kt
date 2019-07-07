package com.jintin.composeadapter.compiler

import com.squareup.javapoet.ClassName


class ViewHolderInfo(
    val className: ClassName,
    val viewType: String,
    var layoutId: Int
)