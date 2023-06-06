package ar.prasher.promanage.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context : Context,
    private var list : ArrayList<String>,
    private val title : String = "",
    private val mSelectedColor : String = "",
) : Dialog(context){

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_list,
            null
        )

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        setupRecyclerView(view)

    }

    private fun setupRecyclerView(view : View){
        val tvTitle: TextView? = view.findViewById(R.id.tvTitle)
        val rvList: RecyclerView? = view.findViewById(R.id.rvList)
        tvTitle?.text = title
        rvList?.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context,list, mSelectedColor)
        rvList?.adapter = adapter
        adapter!!.setOnClickListener(
            object : LabelColorListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, color: String) {
                    dismiss()
                    onItemSelected(color)

                }
            }
        )
    }

    protected abstract fun onItemSelected(color : String)

}