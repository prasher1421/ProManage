package ar.prasher.promanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.bumptech.glide.Glide

open class MemberListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<User>
) : RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = list[position]

        Glide
            .with(context)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.user_grey_24)
            .into(holder.ivImage!!)

        holder.tvName?.text = user.name
        holder.tvEmail?.text = user.email

        if (user.selected){
            holder.ivSelectedMember?.visibility = View.VISIBLE
        }else{
            holder.ivSelectedMember?.visibility = View.GONE
        }

        //single item click listener
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                if (user.selected){
                    onClickListener!!.onClick(position, user,Constants.UN_SELECT)
                }else{
                    onClickListener!!.onClick(position, user,Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onClickListener : OnClickListener? = null

    interface OnClickListener{
        fun onClick(position : Int, user : User, action : String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage : ImageView? = view.findViewById(R.id.iv_member_image)
        val ivSelectedMember : ImageView? = view.findViewById(R.id.iv_selected_member)
        val tvName : TextView? = view.findViewById(R.id.tv_member_name)
        val tvEmail : TextView? = view.findViewById(R.id.tv_member_email)
    }
}