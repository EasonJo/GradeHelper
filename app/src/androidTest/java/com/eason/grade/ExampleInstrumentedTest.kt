package com.eason.grade

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.eason.grade.excel.ExcelUtils
import com.eason.grade.students.Classes
import com.eason.grade.students.Grade
import com.eason.grade.utils.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.eason.grade", appContext.packageName)
    }

    @Test
    fun textGradeExport() {
        val gradeDao = BaseApplication.application.daoSession.gradeDao
        val allGrades = gradeDao.loadAll().groupBy { it.student.classes }
            .map { entry -> mapOf(Pair(entry.key, entry.value.groupBy { it.gradeName })) }
        allGrades.forEach {
            for ((classes, grades) in it) {
                //cid 班级

                //grades,所有日期的成绩

                println(classes.toString())
                println(grades)
                generateFileOfClasses(classes, grades)
            }
        }
    }

    /**
     * 生成一个班级的所有成绩
     */
    private fun generateFileOfClasses(cls: Classes, grades: Map<String, List<Grade>>) {
        val classesDirName = FileUtils.getAppStorageDir() + File.separator + cls.name
        val file = File(classesDirName)
        if (!file.exists()) {
            file.mkdirs()
        }

        val fileName = "$classesDirName/${cls.name}.xls"
        val columns = arrayOf("学号", "姓名", "作业题", "朗读")

        val sheetName = grades.map { it.key }.toTypedArray()
        Arrays.sort(sheetName)

        ExcelUtils.initExcel(fileName, columns, sheetName)

        for ((index, value) in sheetName.withIndex()) {
            val g = grades[value]
            ExcelUtils.writeObjListToExcel(g, fileName, InstrumentationRegistry.getTargetContext(), index)
        }
    }
}
