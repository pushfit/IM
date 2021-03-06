package buzz.pushfit.im.mvp.view.activity


import android.annotation.SuppressLint
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import buzz.pushfit.im.R
import buzz.pushfit.im.adapter.EMMessageListenerAdapter
import buzz.pushfit.im.base.BaseActivity
import buzz.pushfit.im.factory.FragmentFactory
import buzz.pushfit.im.mvp.presenter.impl.MainPresenter
import buzz.pushfit.im.mvp.view.interfaces.IMainView
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMError
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.roughike.bottombar.OnTabSelectListener
import kotlinx.android.synthetic.main.abc_activity_main.*
import kotlinx.android.synthetic.main.layout_content_main.*
import kotlinx.android.synthetic.main.layout_drawer_bottom.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class MainActivity : BaseActivity(), IMainView, NavigationView.OnNavigationItemSelectedListener {
    val presenter = MainPresenter(this)
    @SuppressLint("StringFormatInvalid")
    private val titles = arrayOf("消息", "联系人", "动态")


    private val mOnTabSelectListener= OnTabSelectListener { itemId->
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.replace(R.id.fragment, FragmentFactory.instance.getFragment(itemId))
        beginTransaction.commit()
        when (itemId) {
            R.id.navigation_message -> {
                between.text = titles[0]
                right.visibility = View.GONE
            }
            R.id.navigation_friends -> {
                between.text = titles[1]
                right.visibility = View.VISIBLE
                right.setBackgroundResource(R.mipmap.ic_add_48)
                right.setOnClickListener { startActivity<AddFriendActivity>() }
            }
            R.id.navigation_space -> {
                between.text = titles[2]
                right.visibility = View.GONE
            }
        }
    }
    private val mEmConnectionListener =object :EMConnectionListener{
        override fun onConnected() {
        }
        override fun onDisconnected(p0: Int) {
            if (p0==EMError.USER_LOGIN_ANOTHER_DEVICE){
                //发送多设备登录 跳转到登录界面
                startActivity<LoginActivity>()
                finish()
            }
        }
    }
    private val mMessageListener =object : EMMessageListenerAdapter() {
        override fun onMessageDelivered(message: List<EMMessage>) {
            runOnUiThread {
                updateBottomBarUnReadMsgCount()
            }
        }
    }

    override fun getLayoutResId(): Int = R.layout.abc_activity_main
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    override fun init() {
        super.init()
        between.text = getString(R.string.title_message)
        initDrawer()
        navigation.setOnTabSelectListener(mOnTabSelectListener)
        left.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }//打开侧滑菜单
        logout.setOnClickListener { logout() }//退出登录

        EMClient.getInstance().addConnectionListener(mEmConnectionListener)

        EMClient.getInstance().chatManager().addMessageListener(mMessageListener)
    }
    private fun logout() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.hint))
                .setMessage(getString(R.string.logout_message))
                .setNegativeButton(getString(R.string.cancel_logout), null)
                .setPositiveButton(getString(R.string.agree_logout)) { _, _ -> presenter.onLogout() }
                .show()
        drawerLayout.closeDrawer(GravityCompat.START)
    }
    private fun initDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    override fun onLogoutSuccess() {
        toast(getString(R.string.logout_success))
        startActivity<LoginActivity>()
        finish()
    }

    override fun onLogoutError() = toast(getString(R.string.logout_error))

    override fun onResume() {
        updateBottomBarUnReadMsgCount()
        super.onResume()
    }

    private fun updateBottomBarUnReadMsgCount() {
        val tabWithId = navigation.getTabWithId(R.id.navigation_message)
        tabWithId.setBadgeCount(EMClient.getInstance().chatManager().unreadMessageCount)
    }

    override fun onDestroy() {
        super.onDestroy()
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener)
    }
}
