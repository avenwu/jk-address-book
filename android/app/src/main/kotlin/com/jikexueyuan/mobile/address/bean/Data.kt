package com.jikexueyuan.mobile.address.bean

import java.io.Serializable

/**
 * Created by aven on 10/22/15.
 */
data class User(val username: String = "", val email: String = "", val phone: String = "",
                val qq: String = "", val wechat: String = "", val pinying: String = "") : Serializable {
    fun json(): String {
        return "{username:\"$username\", email:\"$email\", phone:\"$phone\",qq:\"$qq\",wechat:\"$wechat\", pinying:\"$pinying\"}"
    }
}

data class AddressBook(val userList: List<User>?, val totalCount: Int = 0, val pageIndex: Int = 1)

