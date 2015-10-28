package com.jikexueyuan.mobile.address.ui

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
    var colorMap = arrayOf(
            0xFFF44336,
            0xFFE91E63,
            0xFF9C27B0,
            0xFF673AB7,
            0xFF3F51B5,
            0xFF2196F3,
            0xFF03A9F4,
            0xFF00BCD4,
            0xFF009688,
            0xFF4CAF50,
            0xFF8BC34A,
            0xFFCDDC39,
            0XFFFFEB3B,
            0xFFFFC107,
            0xFFFF9800,
            0xFFFF5722,
            0xFF795548,
            0xFF9E9E9E,
            0xFF607D8B)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val `in` = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = `in`.inflate(R.layout.address_cell_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getUser(position)
        holder.username.text = user.username
        if (TextUtils.isEmpty(user.pinying)) {
            holder.shorthand.text = ""
        } else {
            holder.shorthand.text = user.pinying.subSequence(0, 1)
        }

        if (position > 0) {
            var pinyin = getUser(position - 1).pinying;
            pinyin.let {
                if (user.pinying.startsWith(it.substring(0, 1))) {
                    holder.shorthand.visibility = View.INVISIBLE
                } else {
                    holder.shorthand.visibility = View.VISIBLE
                }
            }
        } else {
            holder.shorthand.visibility = View.VISIBLE
        }

        holder.avatar.imageTintList = colorMapping(user.pinying, position)
        holder.itemView.setTag(R.id.user_name, user)
    }

    /**
     * 为尽量让列表内用户头像的颜色差异化，对颜色的选用简单做一个映射
     * 颜色索引 = (全拼的首字符 x 全拼的字符数长度 + 在列表内所处位置)% 可用颜色种类
     */
    fun colorMapping(text: String?, p: Int): ColorStateList {
        var index = text?.length ?: 0;
        var char4Color = 'A'
        if (index > 1) {
            char4Color = text?.get(0)!!
        }
        var position = char4Color.toInt() * (text?.length ?: 1) + p;
        return ColorStateList.valueOf(colorMap[position % colorMap.size].toInt())
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
