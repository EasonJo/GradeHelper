package com.eason.grade

import com.eason.grade.excel.ExcelUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.math.BigDecimal

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun readFile() {
        val file = File("testfile.xlsx")
        println(file.absoluteFile)
        val list = ExcelUtils.readXLSX(file, 0)
        println(list)
    }


    @Test
    fun parseNumber(){

//        var num = "1.0"
//        val nf = NumberFormat.getInstance()
//        println(nf.format(num.toDouble()))


        //解决接续数据后面有.0的情况,比如 1.0  2.0 等等
        var v = "2.0"
        //val df = DecimalFormat("#")
        val one = BigDecimal(v.toDouble())
        println(v)
        val s = one.toPlainString()
        println(s)


    }

}
