package com.example.jaschates.filter

interface AddRemove {
    fun add(vararg texts: String?)
    fun add(texts: List<String?>?)
    fun add(texts: Set<String?>?)
    fun remove(vararg texts: String?)
    fun remove(texts: List<String?>?)
    fun remove(texts: Set<String?>?)
}