package com.jikexueyuan.mobile.address.ui

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.jikexueyuan.mobile.address.*
import com.jikexueyuan.mobile.address.api.AppService
import kotlinx.android.synthetic.activity_main.fab
import kotlinx.android.synthetic.content_main.recyclerView
import kotlinx.android.synthetic.content_main.progress
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {
    internal var taskList = ArrayList<WeakReference<AsyncTask<*, *, *>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fab.setOnClickListener({ view ->
            snackBar("Replace with your own action")
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AddressBookAdapter()
        progress.visibility = View.VISIBLE

        if (isNetWorkAvailable(this)) {
            enqueue(AppService.login(BuildConfig.USER_NAME, BuildConfig.PASSWORD, { success ->
                if (success) {
                    snackBar("登录成功，正在快马加鞭获取联系人。。。")
                    enqueue(AppService.getAddressBookByPage(1, { data ->
                        if (data.userList == null) {
                            snackBar("网络不给力，找不到联系人")
                        } else {
                            snackBar("可获取${data.totalCount}条联系人数据")
                            (recyclerView.adapter as AddressBookAdapter).addUsers(data.userList)
                            saveUserList2Cache(this, (recyclerView.adapter as AddressBookAdapter).userList)
                            progress.visibility = View.GONE
                        }
                    }))
                } else {
                    snackBar("这位壮士，登录不成功啊")
                    progress.visibility = View.GONE
                }
            }))
        } else {
            getUserListFromCache(this, { data ->
                (recyclerView.adapter as AddressBookAdapter).addUsers(data)
                progress.visibility = View.GONE
            })
        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
