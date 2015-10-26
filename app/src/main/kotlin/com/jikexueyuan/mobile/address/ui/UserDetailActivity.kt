package com.jikexueyuan.mobile.address.ui

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import android.widget.Toast
import com.jikexueyuan.mobile.address.R
import com.jikexueyuan.mobile.address.json2User
import kotlinx.android.synthetic.content_user_detail.*

class UserDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.title = ""

        toolbar.setNavigationOnClickListener { finish() }
        (findViewById(R.id.fab) as FloatingActionButton).setOnClickListener({ view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        })
        val user = json2User(intent.getStringExtra("key_user"))
        user?.let {
            user_name.text = user.username
            email.text = user.email
            phone.text = user.phone
            qq.text = user.qq
            wechat.text = user.wechat
        }
    }

    fun setCopyListener(textView: TextView) {
        textView.setOnLongClickListener({ v ->
            Toast.makeText(v?.context, "Long click:$v", Toast.LENGTH_SHORT).show()
            true
        })
    }

}
