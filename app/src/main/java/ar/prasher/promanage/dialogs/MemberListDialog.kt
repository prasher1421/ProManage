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
import ar.prasher.promanage.adapters.MemberListItemsAdapter
import ar.prasher.promanage.models.User

abstract class MemberListDialog(
    context : Context,
    private var list : ArrayList<User>,
    private val title : String = ""
) : Dialog(context){

    private var adapter: MemberListItemsAdapter? = null

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

        if(list.size > 0){

            rvList?.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context,list)
            rvList?.adapter = adapter

            adapter!!.setOnClickListener(
                object : MemberListItemsAdapter.OnClickListener{
                    override fun onClick(position: Int, user: User, action: String) {
                        dismiss()
                        onItemSelected(user, action)
                    }
                }
            )
        }
    }

    protected abstract fun onItemSelected(user : User,action : String)

}