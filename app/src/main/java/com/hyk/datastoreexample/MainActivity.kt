package com.hyk.datastoreexample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.core.Preferences
import com.hyk.datastoreexample.DataStoreUtil.Companion.keyBoolean
import com.hyk.datastoreexample.DataStoreUtil.Companion.keyFloat
import com.hyk.datastoreexample.DataStoreUtil.Companion.keyInt
import com.hyk.datastoreexample.DataStoreUtil.Companion.keyString
import com.hyk.datastoreexample.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var dataStore: DataStoreUtil

    private val resultList:ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        d("init")
        resultList.add("")
        resultList.add("")
        resultList.add("")
        resultList.add("")

        initDataStore()

        setupLayout()
    }

    private fun initDataStore() {
        dataStore = DataStoreUtil(baseContext)
        dataStore.first(this)
        // UI 스레드에서 동기 I/O 작업을 실행하면 ANR 또는 UI 버벅거림이 발생할 수 있습니다
        // onCreate 에서 실행
    }

    @SuppressLint("SetTextI18n")
    private fun setupLayout() = binding.apply {
        btApply.setOnClickListener {
            val string = layString.editText?.text?.toString() ?: ""
            if(string.isNotEmpty()) {
                dataStore.doSave(string)
                doLoad(keyString)
            }

            val integer = layInt.editText?.text?.toString() ?: ""
            if(integer.isNotEmpty()) {
                if(integer.isIntNumber()) {
                    dataStore.doSave(integer.toInt())
                    doLoad(keyInt)
                }
            }

            val float = layFloat.editText?.text?.toString() ?: ""
            if(float.isNotEmpty()) {
                if(float.isFloatNumber()) {
                    dataStore.doSave(float.toFloat())
                    doLoad(keyFloat)
                }
            }

            dataStore.doSave(cb.isChecked)
            doLoad(keyBoolean)
        }
    }

    private fun result() {
        val text = StringBuilder()
        resultList.filter { s ->
            s.isNotEmpty()
        }.mapIndexed { index, s ->
            //i("aaaa $s , index $index")
            text.append(when(index) {
                0 -> "string : "
                1 -> "int : "
                2 -> "float : "
                3 -> "check : "
                else -> ""
            })
            text.append(s)
            if(index != 3) {
                text.append("\n\n")
            }
        }

        binding.tvResult.text = text.toString()
    }

    private fun doLoad(key: Preferences.Key<*>) {
        GlobalScope.launch(Dispatchers.IO) {
            dataStore.getValueInDataStore(key).catch { e ->
                e("$e")
            }.conflate().collect { data ->      // conflate() 중간값은 skip 처리
                d("data $data")
                withContext(Dispatchers.Main) {
                    when(data) {
                        is String -> {
                            d("data result string : $data")
                            resultList[0] = data.toString()
                        }
                        is Int -> {
                            d("data result int : $data")
                            resultList[1] = data.toString()
                        }
                        is Float -> {
                            d("data result float : $data")
                            resultList[2] = data.toString()
                        }
                        is Boolean -> {
                            d("data result boolean : $data")
                            resultList[3] = data.toString()
                        }
                    }
                    result()
                }
            }
        }

        /** collect -> collectLatest, 중간에 새로운 값이 들어오면, 값 취소하고 다시 불러옴
         * conflate() : 중간 값 무시 */
        runBlocking {
            /*getEmitTest().conflate().collectLatest { value ->
                i("value $value")
            }*/
            /*val test = getEmitTest().conflate().onEach {
            }.toList()
            i("test $test")*/
        }
    }

    private fun getEmitTest() : Flow<Int> = flow {
        val list: List<Int> = arrayListOf(0, 1, 2)
        list.forEach { i ->
            //delay(500)    // conflate 랑은 같이 사용할수 없네!
            emit(i)//emit(1)
        }
    }
}