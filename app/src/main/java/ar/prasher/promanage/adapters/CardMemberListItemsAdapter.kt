package ar.prasher.promanage.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.models.SelectedMembers
import com.bumptech.glide.Glide

open class CardMemberListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val assignMembers : Boolean
) : RecyclerView.Adapter<CardMemberListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card_selected_member,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (position ==  list.size-1 && assignMembers){
            holder.cvAddMember?.visibility = View.VISIBLE
            holder.cvSelectedMemberImage?.visibility = View.GONE
        }else{
            holder.cvAddMember?.visibility = View.GONE
            holder.cvSelectedMemberImage?.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.user_grey_24)
                .into(holder.ivSelectedMemberImage!!)

        }

        //single item click listener
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onClickListener : OnClickListener? = null

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick()
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSelectedMemberImage : ImageView? = view.findViewById(R.id.iv_selected_member_image)
        val cvAddMember : CardView? = view.findViewById(R.id.cv_add_member)
        val cvSelectedMemberImage : CardView? = view.findViewById(R.id.cv_selected_member_image)
    }
}