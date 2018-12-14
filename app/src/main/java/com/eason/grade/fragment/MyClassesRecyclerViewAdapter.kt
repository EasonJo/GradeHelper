package com.eason.grade.fragment


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.eason.grade.R
import com.eason.grade.students.Grade
import kotlinx.android.synthetic.main.fragment_classes.view.*

class MyClassesRecyclerViewAdapter(
    private val mValues: MutableList<Grade> = mutableListOf<Grade>(),
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyClassesRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Grade
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_classes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.studentName.text = item.student.name
        holder.isRight.text = when (item.isRight) {
            0 -> "正确"
            1 -> "错误"
            -1 -> "未完成"
            else -> {
                "未完成"
            }
        }
        holder.isRead.text = if (item.isRead) "已朗读" else "未朗读"
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun addGrades(cl: List<Grade>) {
        mValues.clear()
        mValues.addAll(cl)
        notifyDataSetChanged()
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Grade?)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val studentName: TextView = mView.item_name
        val isRight: TextView = mView.item_isRight
        val isRead: TextView = mView.item_isRead
    }
}
