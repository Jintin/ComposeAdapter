package com.jintin.composeadapter.app

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jintin.composeadapter.annotations.BindLayout
import com.jintin.composeadapter.annotations.BindHolder
import kotlinx.android.synthetic.main.item_holder1.view.*

@BindHolder(model = ViewHolder1::class)
@BindHolder(layout = R.layout.item_holder2, model = ViewHolder2::class)
class SampleAdapter(private val list: List<String>) : SampleAdapterHelper() {

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            TYPE_VIEW_HOLDER1
        } else {
            TYPE_VIEW_HOLDER2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        if (holder is StringHolder) {
            holder.onBind(data)
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

}

interface StringHolder {
    fun onBind(string: String)
}


@BindLayout(R.layout.item_holder1)
class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView), StringHolder {
    override fun onBind(string: String) {
        itemView.textView.text = string
    }
}

class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView), StringHolder {
    override fun onBind(string: String) {
        itemView.textView.text = string
    }
}