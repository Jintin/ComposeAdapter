package com.jintin.composeadapter.app

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jintin.composeadapter.annotations.HolderLayout
import com.jintin.composeadapter.annotations.ViewHolder
import kotlinx.android.synthetic.main.item_holder1.view.*

@ViewHolder(model = ViewHolder1::class)
@ViewHolder(layout = R.layout.item_holder2, model = ViewHolder2::class)
class SampleAdapter(private val list: List<String>) : SampleAdapterHelper() {

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            TYPE_VIEWHOLDER1
        } else {
            TYPE_VIEWHOLDER2
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


@HolderLayout(R.layout.item_holder1)
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