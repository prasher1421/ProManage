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
import ar.prasher.promanage.models.Card
import com.bumptech.glide.Glide

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
        fun onClick(position : Int, model : Card)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCardName : TextView? = view.findViewById(R.id.tv_card_name)
    }
}