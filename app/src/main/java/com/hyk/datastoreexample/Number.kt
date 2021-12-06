package com.hyk.datastoreexample

fun String.isIntNumber() = toIntOrNull()?.let { true } ?: false

fun String.isFloatNumber() = toFloatOrNull()?.let { true } ?: false

