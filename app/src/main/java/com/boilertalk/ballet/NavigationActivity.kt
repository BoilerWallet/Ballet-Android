package com.boilertalk.ballet

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import com.boilertalk.ballet.toolbox.bind
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    private var contentView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Bind Views
        contentView = bind(R.id.navigation_content_view)

        // Set listeners
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment?

        when (item.itemId) {
            R.id.navigation_wallet -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_send -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_receive -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
