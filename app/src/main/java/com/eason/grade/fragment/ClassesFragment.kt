package com.eason.grade.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.afollestad.materialdialogs.MaterialDialog
import com.eason.grade.BaseApplication
import com.eason.grade.R
import com.eason.grade.base.BaseFragment
import com.eason.grade.bean.gen.ClassesDao
import com.eason.grade.bean.gen.GradeDao
import com.eason.grade.excel.ExcelUtils
import com.eason.grade.students.Classes
import com.eason.grade.students.ExcelBean
import com.eason.grade.students.Grade
import com.eason.grade.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_classes_list.*
import java.io.File
import java.util.*


class ClassesFragment : BaseFragment(), MyClassesRecyclerViewAdapter.OnListFragmentInteractionListener,
    View.OnClickListener, AdapterView.OnItemSelectedListener {


    private val TAG = ClassesFragment.javaClass.name
    private lateinit var classesDao: ClassesDao
    private var gradeDao: GradeDao = BaseApplication.application.daoSession.gradeDao
    private var clas = mutableListOf<Grade>()
    private lateinit var myadapter: MyClassesRecyclerViewAdapter
    private val calendar = Calendar.getInstance()!!
    private lateinit var dialog: DatePickerDialog
    private var allClasses: List<Classes> = mutableListOf()

    override fun getLayout(): Int = R.layout.fragment_classes_list


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_classes_list, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        chooseData.setOnClickListener(this)
        export.setOnClickListener(this)
        openDir.setOnClickListener(this)

        choose_class_spinner.setOnItemSelectedListener(this)

        with(list) {
            layoutManager = LinearLayoutManager(context)
            myadapter = MyClassesRecyclerViewAdapter(clas, this@ClassesFragment)
            adapter = myadapter
        }

        classesDao = BaseApplication.application.daoSession.classesDao
        data_info.text =
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        dialog = DatePickerDialog(
            this@ClassesFragment.context,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                data_info.text =
                        "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                updateGradeList()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        //获取班级信息
        getAllClassInfo()
        updateGradeList()
    }

    private fun getAllClassInfo() {
        allClasses = classesDao.loadAll()

        if (allClasses.isNotEmpty()) {
            choose_class_spinner.attachDataSource(allClasses.map { it.name })
        }
    }

    private fun updateGradeList() {
        //获取成绩信息
        if (allClasses.isNotEmpty() && choose_class_spinner.selectedIndex < allClasses.size) {
            val allGrades = getGradeOfClasses(allClasses[choose_class_spinner.selectedIndex])
            myadapter.addGrades(allGrades)
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.i(TAG, "${ClassesFragment.javaClass.name} is Hide -> $hidden")

        if (!hidden) {
            getAllClassInfo()
            updateGradeList()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v) {
            chooseData -> {
//                data_info.text =
//                        "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                dialog.show()
            }
            export -> {
                if (allClasses.isEmpty() || choose_class_spinner.selectedIndex >= allClasses.size) {
                    context?.showToast("无成绩数据,不能导出")
                    return
                }

                val classes = allClasses[choose_class_spinner.selectedIndex]
                val str =
                    Html.fromHtml(
                        "确认导出 <font color='#ff0000'><big><big>${classes.name} ${data_info.text}</big></big></font> 的成绩单?"
                    )
                MaterialDialog.Builder(context!!).title("导出成绩")
                    .content(str)
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive { _, _ ->
                        exportGrade2ExcelFile()
                    }.onNegative { _, _ ->
                        //do nothing
                    }.show()
            }
            openDir -> {
                FileUtils.openAssignFolder(context, FileUtils.getAppStorageDir())
                //OpenFileUtils.openFile(context!!, File(FileUtils.getAppStorageDir()))
            }
        }
    }


    override fun onListFragmentInteraction(item: Grade?) {
        item?.let {
            context?.showToast(it.toString())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        updateGradeList()
    }

    private fun getGradeOfClasses(classes: Classes): List<Grade> {
        val students = classes.students
        return gradeDao.queryBuilder()
            .where(
                GradeDao.Properties.GradeName.eq(data_info.text),
                GradeDao.Properties.Sid.`in`(students.map { it.sid })
            )
            .build().list()
    }

    @SuppressLint("CheckResult")
    private fun exportGrade2ExcelFile() {
        Observable.fromCallable {
            val columns = arrayOf("学号", "姓名", "作业题", "朗读")
            val sheetName = data_info.text.toString()
            val gradeOfClasses = getGradeOfClasses(allClasses[choose_class_spinner.selectedIndex])
            FileUtils.createDir(FileUtils.getAppStorageDir())
            val fileName =
                "${FileUtils.getAppStorageDir()}/${allClasses[choose_class_spinner.selectedIndex].name}_${data_info.text}.xls"

            val initExcel = ExcelUtils.initExcel(fileName, columns, arrayOf(sheetName))

            if (!initExcel) {
                context?.showToast("初始化Excel文件失败,请检查读写存储卡权限是否正确.")
            }

            val objList = gradeOfClasses.map {
                ExcelBean(
                    it.student.sid,
                    it.student.name,
                    if (it.isRight) "正确" else "错误",
                    if (it.isRead) "已朗读" else "未朗读"
                )
            }.sortedBy { excelBean -> excelBean.sid }

            println(objList)

            val r_bitmap =
                generateBitmap(objList, "${allClasses[choose_class_spinner.selectedIndex].name} ${data_info.text} 成绩单")
            Log.i(TAG, "图片生成: $r_bitmap")

            val r = ExcelUtils.writeObjListToExcel(objList, fileName, context, 0)
            r && r_bitmap
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                val str = if (it) "导出成功,目录:${FileUtils.getAppStorageDir()}" else "导出失败"
                info.text = str
                context?.showLongToast(str)
            }, {
                it.printStackTrace()
            })
    }

    private fun exportAllGrade2ExcelFile() {
        val allGrades = gradeDao.loadAll().groupBy { it.student.classes }
            .map { entry -> mapOf(Pair(entry.key, entry.value.groupBy { it.gradeName })) }
        allGrades.forEach {
            for ((classes, grades) in it) {
                //classes 班级
                //grades,所有日期的成绩
                generateFileOfClasses(classes, grades)
                println(classes.toString())
                println(grades.toString())
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
            ExcelUtils.writeObjListToExcel(g, fileName, context, index)
        }
    }

    private fun generateBitmap(objList: List<ExcelBean>, title: String): Boolean {
        val list = objList.map { StringBitmapParameter("${it.sid}  ${it.name} ${it.isRight} ${it.isRead}") }
                as ArrayList<StringBitmapParameter>
        list.add(0, StringBitmapParameter(title))
        list.add(1,StringBitmapParameter("学号  姓名  作业题  朗读"))
        val bitmap = BitmapUtil.stringListtoBitmap(context, list)
        val fileName = "${allClasses[choose_class_spinner.selectedIndex].name}_${data_info.text}"
        return BitmapUtil.saveImageToGallery(context, fileName, bitmap)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ClassesFragment()
    }
}
