package com.jikexueyuan.mobile.address.ui

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jikexueyuan.mobile.address.*
import com.jikexueyuan.mobile.address.api.AppService
import com.jikexueyuan.mobile.address.bean.User
import com.jikexueyuan.mobile.address.extention.startActivitySafely
import com.jikexueyuan.mobile.address.extention.toast
import kotlinx.android.synthetic.activity_main.fab
import kotlinx.android.synthetic.content_main.progress
import kotlinx.android.synthetic.content_main.recyclerView
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {
    internal var taskList = ArrayList<WeakReference<AsyncTask<*, *, *>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayShowTitleEnabled(true)
        supportActionBar.setDisplayShowCustomEnabled(true)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AddressBookAdapter()

        progress.setRefreshing(true)
        progress.visibility = View.VISIBLE

        getUserListFromCache(this, {
            (recyclerView.adapter as AddressBookAdapter).addUsers(it)
            if (it.size > 0) {
                progress.setRefreshing(false)
                progress.visibility = View.GONE
            }
            if (isNetWorkAvailable(this)) {
                enqueue(AppService.login(BuildConfig.USER_NAME, BuildConfig.PASSWORD, { success ->
                    if (success) {
                        snackBar(R.string.receive_user_list)
                        updateAddressBook(1)
                    } else {
                        snackBar(R.string.login_failed)
                        progress.setRefreshing(false)
                        progress.visibility = View.GONE
                    }
                }))
            }
        })

        fab.setOnClickListener({
            snackBar(R.string.import_all_user_confirm, getString(R.string.confirm), View.OnClickListener {
                var data = getDataList()
                if (data.size == 0) {
                    snackBar(R.string.no_users_can_not_import)
                } else {
                    export2VCard(this, data, {
                        var intent = Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(it), "text/vcard");
                        startActivitySafely(intent, {
                            toast(it, R.string.no_address_booK_application_found)
                        })
                    })
                }
            })
        })
    }

    var rawUserList: MutableList<User> = ArrayList()

    /**
     * 连续获取通讯录信息，直到取完为止
     */
    fun updateAddressBook(p: Int) {
        Log.w("API", "current page=$p")
        enqueue(AppService.getAddressBookByPage(p, { data ->
            if (data.userList == null) {
                snackBar(R.string.unable_to_find_user_data)
                saveUserList2Cache(this, getDataList(), ::skipSameUserFilter)
                forceUpdate = false
                progress.visibility = View.GONE
            } else {
                rawUserList.addAll(data.userList)
                if (data.userList.size >= AppService.PAGE_COUNT) {
                    updateAddressBook(data.pageIndex + 1)
                } else {
                    sortUserList(rawUserList, {
                        (recyclerView.adapter as AddressBookAdapter).clear()
                        (recyclerView.adapter as AddressBookAdapter).addUsers(it)
                        saveUserList2Cache(this, getDataList(), ::skipSameUserFilter)
                        var text = getString(R.string.got_N_users, it.size)
                        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).show()
                        forceUpdate = false
                        progress.visibility = View.GONE
                    })

                }
            }
        }))
    }

    fun getDataList(): List<User> {
        return (recyclerView.adapter as AddressBookAdapter).userList
    }

    fun snackBar(text: Int, action: String? = null, listener: View.OnClickListener? = null) {
        var snackbar = Snackbar.make(fab, text, Snackbar.LENGTH_LONG)
        action?.let {
            snackbar.setAction(action, listener)
        }
        snackbar.show()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu);
        return true
    }

    var forceUpdate = false
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (forceUpdate || progress.visibility == View.VISIBLE) {
            snackBar(R.string.refreshing)
        } else {
            progress.visibility = View.VISIBLE
            progress.setRefreshing(true)
            updateAddressBook(1)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        dequeue()
    }
}
