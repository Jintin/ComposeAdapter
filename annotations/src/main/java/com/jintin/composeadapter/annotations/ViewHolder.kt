package com.jintin.composeadapter.annotations

import kotlin.reflect.KClass

@Suppress("DEPRECATED_JAVA_ANNOTATION")
@java.lang.annotation.Repeatable(ViewHolders::class)
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ViewHolder(val layout: Int = -1, val model: KClass<out Any>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ViewHolders(vararg val value: ViewHolder)
