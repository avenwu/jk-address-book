package com.jikexueyuan.mobile.address.ui

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jikexueyuan.mobile.address.R
import com.jikexueyuan.mobile.address.bean.User
import java.util.*

/**
 * Created by aven on 10/23/15.
 */
class AddressBookAdapter : RecyclerView.Adapter<AddressBookAdapter.ViewHolder>() {
    var userList: MutableList<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val `in` = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = `in`.inflate(R.layout.address_cell_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getUser(position)
        holder.username.text = user.username
        holder.shorthand.text = "A"
        holder.itemView.setTag(R.id.user_name, user)
    }

    private fun getUser(position: Int): User {
        return userList[position]
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun addUsers(list: List<User>) {
        userList.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        userList.clear()
        notifyDataSetChanged()
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shorthand: TextView
        var username: TextView
        var avatar: ImageView

        init {
            shorthand = itemView.findViewById(R.id.shorthand) as TextView
            username = itemView.findViewById(R.id.user_name) as TextView
            avatar = itemView.findViewById(R.id.avatar) as ImageView
            itemView.setOnClickListener({ v ->
                val data = v.getTag(R.id.user_name)
                if (data is User) {
                    val intent = Intent(v.context, UserDetailActivity::class.java)
                    intent.putExtra("key_user", data.json())
                    v.context.startActivity(intent)
                }
            })
        }
    }

}
