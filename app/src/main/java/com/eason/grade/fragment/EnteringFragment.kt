package com.eason.grade.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import com.eason.grade.BaseApplication
import com.eason.grade.R
import com.eason.grade.base.BaseFragment
import com.eason.grade.bean.gen.ClassesDao
import com.eason.grade.bean.gen.GradeDao
import com.eason.grade.bean.gen.StudentDao
import com.eason.grade.event.EventOnStudenImport
import com.eason.grade.students.Classes
import com.eason.grade.students.Grade
import com.eason.grade.students.Student
import com.eason.grade.utils.RxBus
import com.eason.grade.utils.showToast
import io.reactivex.Observable
import kotlinx.android.synthetic.main.entering_fragment.*
import java.util.*


class EnteringFragment : BaseFragment(), RadioGroup.OnCheckedChangeListener, View.OnClickListener,
    AdapterView.OnItemSelectedListener {
    private val TAG = EnteringFragment.javaClass.name
    override fun getLayout(): Int = R.layout.entering_fragment
    private var grade = Grade()
    private lateinit var gradeDao: GradeDao
    private lateinit var classesDao: ClassesDao
    private val studentDao: StudentDao = BaseApplication.application.daoSession.studentDao
    private var allClasses: List<Classes> = mutableListOf()
    private var students: List<Student> = mutableListOf()
    private lateinit var studentImportObserver: Observable<EventOnStudenImport>


    companion object {
        fun newInstance() = EnteringFragment()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkIn.setOnClickListener(this)
        rg1.setOnCheckedChangeListener(this)
        readgroup.setOnCheckedChangeListener(this)
        choose_date.setOnClickListener(this)
        gradeDao = BaseApplication.application.daoSession.gradeDao
        classesDao = BaseApplication.application.daoSession.classesDao

        classes_spinner.setOnItemSelectedListener(this)
        stus_spinner.setOnItemSelectedListener(this)


        allClasses = classesDao.loadAll().distinct()
        allClasses.let {
            if (it.isNotEmpty()) {
                classes_spinner.attachDataSource(allClasses.map { classes -> classes.name })
            }
        }

        date_tip.text =
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        dialog = DatePickerDialog(
            this@EnteringFragment.context,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date_tip.text =
                        "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )


        //val list = classesDao.loadAll().map { it.name }.distinct()

        if (allClasses.isNotEmpty()) {
            students = allClasses[classes_spinner.selectedIndex].students
            students.let {
                if (it.isNotEmpty()) {
                    stus_spinner.attachDataSource(it.map { it1 -> it1.name })
                }
            }
        }


        studentImportObserver = RxBus.get().register(EventOnStudenImport::class.java)
        val disposable = studentImportObserver.subscribe {
            allClasses = classesDao.loadAll().distinct()
            allClasses.let {
                if (it.isNotEmpty()) {
                    classes_spinner.attachDataSource(allClasses.map { classes -> classes.name })
                }

                if (allClasses.isNotEmpty()) {
                    students = allClasses[classes_spinner.selectedIndex].students
                    students.let {
                        if (it.isNotEmpty()) {
                            stus_spinner.attachDataSource(it.map { it1 -> it1.name })
                        }
                    }
                }
            }
        }
    }


    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.right -> {
                grade.isRight = true
            }
            R.id.wrong -> {
                grade.isRight = false
            }
            R.id.read -> {
                grade.isRead = true
            }
            R.id.not_read -> {
                grade.isRead = false
            }
        }
    }

    private val calendar = Calendar.getInstance()!!
    private lateinit var dialog: DatePickerDialog

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v) {
            checkIn -> {
                //Insert to DB

                val name = stus_spinner.text.toString()

                if (TextUtils.isEmpty(name)) {
                    context?.showToast("学生姓名不能为空")
                    return
                }

                val student = students.find { it.name == name }

                if (student == null) {
                    context?.showToast("此学生不存在,不能录入成绩")
                    return
                }


                if (rg1.checkedRadioButtonId == -1 || readgroup.checkedRadioButtonId == -1) {
                    context?.showToast("必须选择答案")
                    return
                }


                grade.apply {
                    gradeName = date_tip.text.toString()
                    sid = student.sid

                    val gid = gradeDao.insertOrReplace(grade)
                    context?.showToast("成绩录入成功,成绩 ID 为: $gid")
                    //reset()
                }

            }

            choose_date -> {
                date_tip.text =
                        "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                dialog.show()
            }
        }
    }

    private fun loadAllStudents(className: String) {
        checkIn.text = "确认录入成绩"
        //reset Grade Object
        grade = Grade()
        //当前 班级 的所有学生
        students = allClasses.find { it.name == className }?.students as List<Student>
        students.let {
            if (it.isNotEmpty()) {
                stus_spinner.isEnabled = true
                stus_spinner.attachDataSource(it.map { student -> student.name })
            } else {
                stus_spinner.text = ""
                stus_spinner.isEnabled = false
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        RxBus.get().unregister(EventOnStudenImport::class.java, studentImportObserver)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            R.id.classes_spinner -> loadAllStudents(allClasses[position].name)
            R.id.stus_spinner -> {
                checkIn.text = "确认录入成绩"
                grade = Grade()
                if (students.isEmpty()) {
                    return
                }

                rg1.clearCheck()
                readgroup.clearCheck()

                val student = students[position]

                Log.i(TAG, "select student: $student")

                //grade.id = -1
                student.let {
                    val grade1 = gradeDao.queryBuilder()
                        .where(
                            GradeDao.Properties.Sid.eq(student.sid),
                            GradeDao.Properties.GradeName.eq(date_tip.text)
                        ).build().unique()
                    grade1?.let {
                        val rgCheckedID = if (grade.isRight) R.id.right else R.id.wrong
                        val checkedId = if (grade.isRead) R.id.read else R.id.not_read
                        rg1.check(rgCheckedID)
                        readgroup.check(checkedId)
                        //if grade is exist,update grade
                        grade = grade1
//                        info.text="当前用户已经录入成绩,再次录入为修改"
                        checkIn.text = "确认修改成绩"
                    }
                }
            }
        }
    }


    private fun reset() {
        stus_spinner.setText("")
        rg1.clearCheck()
        readgroup.clearCheck()
    }
}