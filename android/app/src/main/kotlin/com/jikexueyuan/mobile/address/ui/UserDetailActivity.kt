package com.jikexueyuan.mobile.address.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jikexueyuan.mobile.address.*
import com.jikexueyuan.mobile.address.bean.User
import com.jikexueyuan.mobile.address.extention.startActivitySafely
import com.jikexueyuan.mobile.address.extention.toast
import kotlinx.android.synthetic.content_user_detail.*

class UserDetailActivity : AppCompatActivity() {
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.title = ""

        toolbar.setNavigationOnClickListener { finish() }
        (findViewById(R.id.fab) as FloatingActionButton).setOnClickListener({
            user?.let {
                export2VCard(this, listOf(user!!), {
                    var intent = Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(it), "text/vcard");
                    startActivitySafely(intent, {
                        toast(it, R.string.no_address_booK_application_found)
                    })
                })
            }
        })
        user = json2User(intent.getStringExtra("key_user"))
        user?.let {
            user_name.text = user?.username
            email.text = user?.email
            bindAction(email.parent as View, ::sendEmail)

            phone.text = user?.phone
            bindAction(phone.parent as View, ::makeCall)

            qq.text = user?.qq
            bindAction(qq.parent as View, ::openQQ)

            wechat.text = user?.wechat
            bindAction(wechat.parent as View, ::openWeChat)
        }
    }

    fun bindAction(v: View, block: (Context, CharSequence) -> Unit) {
        v.setOnClickListener({
            var label = (it as ViewGroup).getChildAt(1)
            if (label is TextView) {
                block(label.context, label.text)
            }
        })
    }
}
