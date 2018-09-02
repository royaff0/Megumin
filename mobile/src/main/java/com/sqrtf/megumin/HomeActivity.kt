package com.sqrtf.megumin

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.sqrtf.common.MeguminApplocation
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.cache.MeguminPreferences
import com.sqrtf.common.cache.PreferencesUtil
import retrofit2.HttpException

class HomeActivity : BaseThemeActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun themeWhite(): Int {
        return R.style.AppThemeWhite_NoStateBar
    }

    override fun themeStand(): Int {
        return R.style.AppTheme_NoStateBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isWhiteTheme) {
            val v = findViewById(android.R.id.content)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                v.systemUiVisibility = v.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
        }

        setContentView(R.layout.activity_home)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        findViewById(R.id.fab_search).setOnClickListener { search() }

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        val navHeaderT1 = navigationView.getHeaderView(0).findViewById(R.id.textView1) as TextView
        val navHeaderT2 = navigationView.getHeaderView(0).findViewById(R.id.textView2) as TextView
        navigationView.setNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

        ApiClient.getInstance().getUserInfo()
                .withLifecycle()
                .subscribe({
                    navHeaderT1.text = it.getData().name
                    navHeaderT2.text = it.getData().email
                }, {
                    toastErrors().accept(it)

                    if (it is HttpException && it.code() == 401) {
                        logout()
                    }
                })
    }

    private fun search() {
        startActivity(SearchActivity.intent(this))
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_favorite -> {
                startActivity(FavoriteActivity.intent(this))
            }
            R.id.nav_bangmuni -> {
                startActivity(AllBangumiActivity.intent(this))
            }
            R.id.nav_setting -> {
                themeChanged()
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return false
    }

    private fun logout() {
        MeguminApplocation.logout(this)
        finishAffinity()
    }

}
