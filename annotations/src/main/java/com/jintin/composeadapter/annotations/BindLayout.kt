package com.jintin.composeadapter.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class BindLayout(val layout: Int)

