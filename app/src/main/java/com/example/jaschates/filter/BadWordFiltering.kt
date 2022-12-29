package com.example.jaschates.filter

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.function.Consumer
import java.util.stream.Collectors

class BadWordFiltering : BadWords, AddRemove {
    private val set: MutableSet<String?> = HashSet(java.util.List.of(*BadWords.profanity))
    private var substituteValue = "*"

    //대체 문자 지정
    //기본값 : *
    @SuppressLint("NotConstructor")
    open fun BadWordFiltering(substituteValue: String?) {
        this.substituteValue = substituteValue!!
    }

    //특정 문자 추가, 삭제
    override fun add(vararg texts: String?) {
        set.addAll(java.util.List.of(*texts))
    }

    override fun add(texts: List<String?>?) {
        set.addAll(texts!!)
    }

    override fun add(texts: Set<String?>?) {
        set.addAll(texts!!)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun remove(vararg texts: String?) {
        java.util.List.of(*texts).forEach(Consumer { o: String? -> set.remove(o) })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun remove(texts: List<String?>?) {
        texts!!.forEach(Consumer { o: String? -> set.remove(o) })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun remove(texts: Set<String?>?) {
        texts!!.forEach(Consumer { o: String? -> set.remove(o) })
    }

    //비속어 있다면 대체
    @RequiresApi(Build.VERSION_CODES.N)
    fun checkAndChange(text: String): String {
        var text = text
        val s = set.stream()
            .filter { s: String? -> text.contains(s!!) }
            .collect(Collectors.toSet())
        for (v in s) {
            val textLen = v!!.length
            val sub = substituteValue.repeat(textLen)
            text = text.replace(v, sub)
        }
        return text
    }

    //비속어가 1개라도 존재하면 true 반환
    @RequiresApi(Build.VERSION_CODES.N)
    fun check(text: String): Boolean {
        return set.stream()
            .anyMatch { s: String? ->
                text.contains(
                    s!!)
            }
    }

    //공백 없는 상태 체크
    @RequiresApi(Build.VERSION_CODES.N)
    fun blankCheck(text: String): Boolean {
        val cpText = text.replace(" ", "")
        return check(cpText)
    }
}