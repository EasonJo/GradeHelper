package com.eason.grade.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.eason.grade.BaseApplication
import com.eason.grade.R
import com.eason.grade.base.BaseFragment
import com.eason.grade.bean.gen.ClassesDao
import com.eason.grade.bean.gen.StudentDao
import com.eason.grade.event.EventOnStudenImport
import com.eason.grade.excel.ExcelUtils
import com.eason.grade.students.Classes
import com.eason.grade.students.Student
import com.eason.grade.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_setting.*


/**
 * @author Eason
 */
class SettingFragment : BaseFragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private val TAG = SettingFragment.javaClass.name

    private var classesDao: ClassesDao = BaseApplication.application.daoSession.classesDao
    private var studentDao: StudentDao = BaseApplication.application.daoSession.studentDao

    private var listener: OnFragmentInteractionListener? = null
    private var allClasses: List<Classes> = mutableListOf()
    private lateinit var classAdapter: ArrayAdapter<String>

    override fun getLayout(): Int = R.layout.fragment_setting

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        confirm.setOnClickListener(this)
        import_data.setOnClickListener(this)
        classes_setting_spinner.setOnItemSelectedListener(this)


        allClasses = classesDao.loadAll().distinct()
        if (allClasses.isNotEmpty()) {
            classAdapter =
                    ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, allClasses.map { it.name })
            class_name.setAdapter(classAdapter)
            classes_setting_spinner.attachDataSource(allClasses.map { it.name })

            //classes_setting_spinner.text = ""
        }
    }

    override fun onClick(v: View) {
        when (v) {
            confirm -> {
                val className = class_name.text
                if (TextUtils.isEmpty(className)) {
                    context?.showToast("班级名不能为空!")
                } else {
                    saveClass(className = class_name.text.toString())
                }
            }
            import_data -> {
                if (allClasses.isEmpty() || classes_setting_spinner.selectedIndex >= allClasses.size) {
                    context?.showToast("必须先选择班级,否则不能导入学生信息")
                } else {
                    chooseFile()
                }
            }
        }
    }


    private fun saveClass(className: String) {
        val classes = classesDao.queryBuilder().where(ClassesDao.Properties.Name.eq(className)).build().unique()
        if (classes != null) {
            Toast.makeText(context, "班级已经存在,请勿重复录入", Toast.LENGTH_SHORT).show()
        } else {

            Classes().apply {
                name = className
                classesDao.insertOrReplace(this)
            }

            updateAllClassData()
            context?.showToast("班级录入成功")
            import_data.isEnabled = true
        }
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/*"//设置类型
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            Log.e(TAG, "选择的文件Uri = " + data.toString());
            //通过Uri获取真实路径
            val excelPath = FileUtils.getFilePathByUri(context, data.data)
            Log.e(TAG, "excelPath = " + excelPath)//    /storage/emulated/0/test.xls
            if (excelPath.contains(".xls") || excelPath.contains(".xlsx")) {
                context?.showToast("正在加载Excel中...")
                //载入excel
                parseAllStudentsFromExcel(excelPath)
            } else {
                context?.showToast("此文件不是excel格式")
            }
        }

    }

    private fun parseAllStudentsFromExcel(filePath: String) {
//        val classes = Classes().apply {
//            name = File(filePath).name.substringBefore(".")
//        }
        val disposable = Observable.fromCallable {
            val list = ExcelUtils.readExcel(filePath, 0).filter { t ->
                t.size >= 2 && t[0].isNumber() && !TextUtils.isEmpty(t[1])
            }
                .map { strings ->
                    Student().apply {
                        sid = strings[0].toLong()
                        name = strings[1]
                        //setClasses(classes)
                    }
                }
            list

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                info.append("Excel 解析成功\n")
                context?.showToast("Excel 解析成功")

                //classesDao.insertOrReplace(classes)

                //classes.students = it
                val chooseClass = allClasses[classes_setting_spinner.selectedIndex]

                val fromHtml =
                    Html.fromHtml(
                        "此学生信息将录入以下班级<br><font color='#ff0000'><big><big>${chooseClass.name}</big></big></font><br>录入请点确认,将插入数据库." +
                                "请勿选错班级,否则会导致学生班级信息全部变更"
                    )

                MaterialDialog.Builder(context!!).title("学生信息导入成功")
                    .content(fromHtml)
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive { _, _ ->
                        it.forEach { t: Student -> t.cid = chooseClass.id }
                        studentDao.insertOrReplaceInTx(it)
                        info.text = emptyString
                        info.append("用户数据导入成功\n")
                        context?.showToast("用户数据导入成功")
                        RxBus.get().post(EventOnStudenImport())
                    }.onNegative { _, _ ->
                        //do nothing
                    }.show()


            }, {
                info.append("Excel 解析失败\n")
                context?.showToast("Excel 解析失败")
                it.printStackTrace()
            })
    }


    private fun updateAllClassData() {
        allClasses = classesDao.loadAll().distinct()
        classAdapter =
                ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, allClasses.map { it.name })
        class_name.setAdapter(classAdapter)
        classes_setting_spinner.attachDataSource(allClasses.map { it.name })
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        import_data.isEnabled = true
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingFragment()
    }
}
