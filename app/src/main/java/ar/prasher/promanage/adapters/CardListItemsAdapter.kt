package ar.prasher.promanage.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.activities.TaskListActivity
import ar.prasher.promanage.models.Card
import ar.prasher.promanage.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Card>
) : RecyclerView.Adapter<CardListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        holder.tvCardName?.text = model.name

        if (model.labelColor.isNotEmpty()){
            holder.viewLabelColor?.visibility = View.VISIBLE
            holder.viewLabelColor?.setBackgroundColor(Color.parseColor(model.labelColor))
        }

        if ((context as TaskListActivity)
                .mAssignedMemberDetailsList.size > 0){
            val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

            for( i in context.mAssignedMemberDetailsList.indices){
                for (j in model.assignedTo){
                    if (context.mAssignedMemberDetailsList[i].id == j){
                        val selectedMember = SelectedMembers(
                            context.mAssignedMemberDetailsList[i].id,
                            context.mAssignedMemberDetailsList[i].image
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }

            //following the design principle that the creator only as a member mustn't see
            //himself as assigned to card
            if (selectedMembersList.size > 0){
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                    holder.rvCardSelectedMembersList?.visibility = View.GONE
                }else{
                    holder.rvCardSelectedMembersList?.visibility = View.VISIBLE
                    holder.rvCardSelectedMembersList?.layoutManager = GridLayoutManager(
                        context,4
                    )
                    val adapter = CardMemberListItemsAdapter(context,selectedMembersList,false)
                    holder.rvCardSelectedMembersList?.adapter = adapter

                    adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            if (onClickListener != null){
                                onClickListener!!.onClick(position)
                            }
                        }
                    })
                }
            }else{
                holder.rvCardSelectedMembersList?.visibility = View.GONE
            }

        }

        //single item click listener
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onClickListener : OnClickListener? = null
    interface OnClickListener{
        fun onClick(cardPosition : Int)
    }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCardName : TextView? = view.findViewById(R.id.tv_card_name)
        val viewLabelColor : View? = view.findViewById(R.id.view_label_color)
        val rvCardSelectedMembersList : RecyclerView? = view.findViewById(R.id.rv_card_selected_members_list)
    }
}