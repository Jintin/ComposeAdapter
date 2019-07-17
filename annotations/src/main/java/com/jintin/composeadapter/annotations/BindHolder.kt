package com.jintin.composeadapter.annotations

import kotlin.reflect.KClass

@Suppress("DEPRECATED_JAVA_ANNOTATION")
@java.lang.annotation.Repeatable(BindHolders::class)
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class BindHolder(val model: KClass<out Any>, val layout: Int = -1)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class BindHolders(vararg val value: BindHolder)
