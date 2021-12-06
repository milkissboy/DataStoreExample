package com.hyk.datastoreexample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DataStoreUtil(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "myData")

    fun first(activity: AppCompatActivity?) {
        activity?.apply {
            lifecycleScope.launch {
                context.dataStore.data.first()
                // You should also handle IOExceptions here.
            }
        }
    }

    fun <T> doSave(value: T) =
        GlobalScope.launch(Dispatchers.IO) {
            when(value) {
                is String -> {
                    putValueToDataStore(keyString, value)
                }
                is Int -> {
                    putValueToDataStore(keyInt, value)
                }
                is Float -> {
                    putValueToDataStore(keyFloat, value)
                }
                is Boolean -> {
                    putValueToDataStore(keyBoolean, value)
                }
                else -> {}
            }
        }

    private suspend fun <T> putValueToDataStore(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    fun getValueInDataStore(key: Preferences.Key<*>) = context.dataStore.data.map {
        it[key]
        /*when(it[key]) {
            is String -> {
                it[key] ?: ""
            }
            is Int, Float -> {
                it[key] ?: 0
            }
            else -> {}
        }*/
    }

    companion object {
        val keyString = stringPreferencesKey("KEY_STRING")
        val keyInt = intPreferencesKey("KEY_INT")
        val keyFloat = floatPreferencesKey("KEY_FLOAT")
        val keyBoolean = booleanPreferencesKey("KEY_BOOLEAN")
    }
}