package ar.prasher.promanage.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R

open class LabelColorListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<String>,
    private var mSelectedColor : String
) : RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val color = list[position]

        holder.viewMain?.setCardBackgroundColor(Color.parseColor(color))
//        holder.viewMain?.setBackgroundColor(Color.parseColor(color))

        if (color == mSelectedColor){
            holder.ivSelectedColor?.visibility = View.VISIBLE
        }else{
            holder.ivSelectedColor?.visibility = View.GONE
        }

        //single item click listener
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick(position, color)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onClickListener : OnClickListener? = null

    interface OnClickListener{
        fun onClick(position : Int, color : String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewMain : CardView? = view.findViewById(R.id.view_main)
        val ivSelectedColor : ImageView? = view.findViewById(R.id.iv_selected_color)
    }
}