package ar.prasher.promanage.adapters

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.Task
import com.bumptech.glide.Glide

open class TaskListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Task>
) : RecyclerView.Adapter<TaskListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.item_task,
            parent,
            false
        )
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            (15.toDp().toPx()),0,(40.toDp().toPx()),0)

        view.layoutParams = layoutParams

        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]


        if (position == list.size -1){
            holder.tvAddTaskList?.visibility = View.VISIBLE
            holder.llTaskItem?.visibility = View.GONE
        }else{
            holder.tvAddTaskList?.visibility =View.GONE
            holder.llTaskItem?.visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llTaskItem : LinearLayout? = view.findViewById(R.id.ll_task_item)
        val tvAddTaskList : TextView? = view.findViewById(R.id.tv_add_task_list)
        val tvCreatedBy : TextView? = view.findViewById(R.id.tv_created_by)
    }

    private fun Int.toDp() : Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx() : Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}