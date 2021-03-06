package buzz.pushfit.im.mvp.presenter.impl

import buzz.pushfit.im.mvp.data.FriendsListItem
import buzz.pushfit.im.mvp.data.db.Contact
import buzz.pushfit.im.mvp.data.db.IMDataBase
import buzz.pushfit.im.mvp.presenter.interfacces.IFriendsPresenter
import buzz.pushfit.im.mvp.view.interfaces.IFriendsView
import com.hyphenate.chat.EMClient
import com.hyphenate.exceptions.HyphenateException
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by yuequan on 2017/10/28.
 */
class FriendsPresenter(val view: IFriendsView) : IFriendsPresenter {
    val friendListItems = mutableListOf<FriendsListItem>()

    override fun onLoadFriendsData() {
        doAsync {
            friendListItems.clear()//清空集合
            IMDataBase.instance.deleteAllContact()//清除所有联系人
            try {
                val usernames = EMClient.getInstance().contactManager().allContactsFromServer
                usernames.sortBy { it[0] }
                usernames.forEachIndexed { index, s ->

                    val showFirstLatter = index == 0 || s[0] != usernames[index - 1][0]

                    val friendsListItem = FriendsListItem(s, s[0].toUpperCase(),showFirstLatter )

                    friendListItems.add(friendsListItem)

                    val  contact=Contact(mutableMapOf("name" to s))
                    IMDataBase.instance.saveContact(contact)
                }

                uiThread { view.onLoadFriendsSuccess() }//切换主线程
            } catch (e: HyphenateException) {
                uiThread { view.onLoadFriendsFailed() }//切换主线程
            }
        }
    }
}