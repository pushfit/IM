package buzz.pushfit.im.mvp.view.interfaces

import buzz.pushfit.im.base.IBaseView

/**
 * Created by yuequan on 2017/10/28.
 */
interface IFriendsView :IBaseView {

    fun onLoadFriendsSuccess()
    fun onLoadFriendsFailed()
}