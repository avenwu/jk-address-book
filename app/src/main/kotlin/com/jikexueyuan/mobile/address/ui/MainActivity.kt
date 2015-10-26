package com.jikexueyuan.mobile.address.ui

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.jikexueyuan.mobile.address.*
import com.jikexueyuan.mobile.address.api.AppService
import com.jikexueyuan.mobile.address.widget.TintProgressBar
import kotlinx.android.synthetic.activity_main.fab
import kotlinx.android.synthetic.content_main.progress
import kotlinx.android.synthetic.content_main.recyclerView
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {
    internal var taskList = ArrayList<WeakReference<AsyncTask<*, *, *>>>()
    var refreshBtn: TintProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayShowCustomEnabled(true)
        var customLayout = View.inflate(this, R.layout.home_action_layout, null)
        var layoutParams = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        supportActionBar.setCustomView(customLayout, layoutParams)
        refreshBtn = customLayout.findViewById(R.id.btn_refresh) as TintProgressBar
        refreshBtn?.setRefreshing(false)
        refreshBtn?.setOnClickListener({
            refreshBtn?.setRefreshing(true)
            progress.visibility = View.VISIBLE
            (recyclerView.adapter as AddressBookAdapter).clear()
            saveUserList2Cache(this, emptyList())
            updateAddressBook(1)
        })
        fab.setOnClickListener({ view ->
            snackBar("Replace with your own action")
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AddressBookAdapter()
        progress.setRefreshing(true)
        progress.visibility = View.VISIBLE
        getUserListFromCache(this, { data ->
            (recyclerView.adapter as AddressBookAdapter).addUsers(data)
            if (data.size > 0) {
                progress.setRefreshing(false)
                progress.visibility = View.GONE
            }
            if (isNetWorkAvailable(this)) {
                enqueue(AppService.login(BuildConfig.USER_NAME, BuildConfig.PASSWORD, { success ->
                    if (success) {
                        snackBar("登录成功，正在快马加鞭获取联系人。。。")
                        updateAddressBook(1)
                    } else {
                        snackBar("这位壮士，登录不成功啊")
                        progress.setRefreshing(false)
                        progress.visibility = View.GONE
                    }
                }))
            }
        })
    }

    /**
     * 连续获取通讯录信息，直到取完为止
     */
    fun updateAddressBook(p: Int) {
        Log.w("API", "current page=$p")
        enqueue(AppService.getAddressBookByPage(p, { data ->
            if (data.userList == null) {
                snackBar("网络不给力，找不到联系人")
                saveUserList2Cache(this, (recyclerView.adapter as AddressBookAdapter).userList)
                refreshBtn?.setRefreshing(false)
            } else {
                (recyclerView.adapter as AddressBookAdapter).addUsers(data.userList)
                if (data.userList.size >= AppService.PAGE_COUNT) {
                    updateAddressBook(data.pageIndex + 1)
                } else {
                    saveUserList2Cache(this, (recyclerView.adapter as AddressBookAdapter).userList)
                    snackBar("更新了${data.totalCount}条联系人信息")
                    refreshBtn?.setRefreshing(false)
                    progress.visibility = View.GONE
                }
            }
        }))
    }

    fun snackBar(text: CharSequence, listener: View.OnClickListener? = null) {
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).setAction("Action", listener).show()
    }

    fun toast(text: CharSequence) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun enqueue(task: AsyncTask<*, *, *>) {
        taskList.add(WeakReference(task))
    }

    fun dequeue() {
        for (task in taskList) {
            task.get()?.cancel(true)
        }
        taskList.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        dequeue()
    }
}
