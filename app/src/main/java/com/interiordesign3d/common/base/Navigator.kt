package com.interiordesign3d.common.base

/** Navigation surface a ViewModel is allowed to touch; the NavHost supplies the implementation. */
interface Navigator {
    fun to(route: String)
    fun back()
}

val NoOpNavigator = object : Navigator {
    override fun to(route: String) = Unit
    override fun back() = Unit
}
