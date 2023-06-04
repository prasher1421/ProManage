package ar.prasher.promanage.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.models.Board
import com.bumptech.glide.Glide

open class BoardItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Board>
) : RecyclerView.Adapter<BoardItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_board,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.user_grey_24)
            .into(holder.ivBoardImage!!)
        holder.tvName?.text = model.name
        holder.tvCreatedBy?.text = "Created by : " + model.createdBy

        //single item click listener
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick(position, model)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onClickListener : OnClickListener? = null

    interface OnClickListener{
        fun onClick(position : Int, model : Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBoardImage : ImageView? = view.findViewById(R.id.iv_board_image)
        val tvName : TextView? = view.findViewById(R.id.tv_name)
        val tvCreatedBy : TextView? = view.findViewById(R.id.tv_created_by)
    }
}