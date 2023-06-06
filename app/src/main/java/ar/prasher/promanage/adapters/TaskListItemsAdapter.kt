package ar.prasher.promanage.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.activities.TaskListActivity
import ar.prasher.promanage.models.Card
import ar.prasher.promanage.models.Task

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

        holder.tvTaskListTitle?.text = model.title
        holder.tvAddTaskList?.setOnClickListener {
            holder.tvAddTaskList.visibility =View.GONE
            holder.cvAddTaskListName?.visibility = View.VISIBLE
        }

        holder.ibCloseListName?.setOnClickListener {
            holder.tvAddTaskList?.visibility =View.VISIBLE
            holder.cvAddTaskListName?.visibility = View.GONE
        }

        holder.ibDoneListName?.setOnClickListener {
            val listName = holder.etTaskListName?.text.toString()

            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.createTaskList(listName)
                }else{
                    Toast.makeText(
                        context,
                        "Please Enter List Name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        holder.ibEditListName?.setOnClickListener {
            holder.etEditTaskListName?.setText(model.title)
            holder.llTitleView?.visibility = View.GONE
            holder.cvEditTaskListName?.visibility = View.VISIBLE
        }

        holder.ibCloseEditableView?.setOnClickListener {
            holder.llTitleView?.visibility =View.VISIBLE
            holder.cvEditTaskListName?.visibility = View.GONE
        }

        holder.ibDoneEditListName?.setOnClickListener {
            val listName = holder.etEditTaskListName?.text.toString()

            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.updateTaskList(position, listName, model)
                }else{
                    Toast.makeText(
                        context,
                        "Please Enter List Name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        holder.ibDeleteList?.setOnClickListener {
            alertDialogForDeleteList(position,model.title)
        }

        holder.tvAddCard?.setOnClickListener {
            holder.tvAddCard.visibility = View.GONE
            holder.cvAddCard?.visibility = View.VISIBLE
        }
        holder.ibCloseCardName?.setOnClickListener {
            holder.tvAddCard?.visibility = View.VISIBLE
            holder.cvAddCard?.visibility = View.GONE
        }

        holder.ibDoneCardName?.setOnClickListener {
            val cardName = holder.etCardName?.text.toString()

            if (cardName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.addCardToTaskList(position, cardName)
                }else{
                    Toast.makeText(
                        context,
                        "Please Enter Card Name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        holder.rvCardList?.layoutManager = LinearLayoutManager(context)
        holder.rvCardList?.setHasFixedSize(true)
        val adapter = CardListItemsAdapter(context,model.cards)
        holder.rvCardList?.adapter = adapter

        adapter.setOnClickListener(
            object : CardListItemsAdapter.OnClickListener{
                override fun onClick(cardPosition: Int, model: Card) {
                    if (context is TaskListActivity){
                        context.cardDetails(position, cardPosition)
                    }
                }
            }
        )

    }




    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llTaskItem : LinearLayout? = view.findViewById(R.id.ll_task_item)
        val llTitleView : LinearLayout? = view.findViewById(R.id.ll_title_view)
        val tvAddTaskList : TextView? = view.findViewById(R.id.tv_add_task_list)
        val tvAddCard : TextView? = view.findViewById(R.id.tv_add_card)
        val tvTaskListTitle : TextView? = view.findViewById(R.id.tv_task_list_title)
        val cvAddTaskListName : CardView? = view.findViewById(R.id.cv_add_task_list_name)
        val cvEditTaskListName : CardView? = view.findViewById(R.id.cv_edit_task_list_name)
        val cvAddCard : CardView? = view.findViewById(R.id.cv_add_card)
        val ibCloseListName : ImageButton? = view.findViewById(R.id.ib_close_list_name)
        val ibDoneListName : ImageButton? = view.findViewById(R.id.ib_done_list_name)
        val ibDoneCardName : ImageButton? = view.findViewById(R.id.ib_done_card_name)
        val ibDoneEditListName : ImageButton? = view.findViewById(R.id.ib_done_edit_list_name)
        val ibCloseEditableView : ImageButton? = view.findViewById(R.id.ib_close_editable_view)
        val ibEditListName : ImageButton? = view.findViewById(R.id.ib_edit_list_name)
        val ibDeleteList : ImageButton? = view.findViewById(R.id.ib_delete_list)
        val ibCloseCardName : ImageButton? = view.findViewById(R.id.ib_close_card_name)
        val etTaskListName : EditText? = view.findViewById(R.id.et_task_list_name)
        val etCardName : EditText? = view.findViewById(R.id.et_card_name)
        val etEditTaskListName : EditText? = view.findViewById(R.id.et_edit_task_list_name)
        val rvCardList : RecyclerView? = view.findViewById(R.id.rv_card_list)
    }

    private fun alertDialogForDeleteList(position: Int,title: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title")
        builder.setIcon(R.drawable.ic_delete)
        builder.setPositiveButton("Yes"){
                dialogInterface,which ->
            dialogInterface.dismiss()
            if (context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No"){
                dialogInterface,which ->
            dialogInterface.dismiss()
        }
        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun Int.toDp() : Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx() : Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}