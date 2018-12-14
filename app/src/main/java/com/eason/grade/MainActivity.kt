package com.eason.grade

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import com.eason.grade.base.BaseActivity
import com.eason.grade.fragment.ClassesFragment
import com.eason.grade.fragment.EnteringFragment
import com.eason.grade.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Eason
 */
class MainActivity : BaseActivity() {
    private lateinit var fragments: Array<Fragment>
    private var lastFragment: Int = 0//用于记录上个选择的Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFragments()
    }

    /**
     * 切换Fragment
     *
     * @param lastIndex 上个显示Fragment的索引
     * @param index     需要显示的Fragment的索引
     */
    private fun switchFragment(lastIndex: Int, index: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(fragments[lastIndex])
        if (!fragments[index].isAdded) {
            transaction.add(R.id.fragment_container, fragments[index])
        }
        transaction.show(fragments[index]).commitAllowingStateLoss()
    }

    private fun initFragments() {
        fragments =
                arrayOf(EnteringFragment.newInstance(), ClassesFragment.newInstance(), SettingFragment.newInstance())
        lastFragment = 0
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragments[0])
            .show(fragments[0])
            .commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                if (lastFragment != 0) {
                    switchFragment(lastFragment, 0)
                    lastFragment = 0
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                if (lastFragment != 1) {
                    switchFragment(lastFragment, 1)
                    lastFragment = 1
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                if (lastFragment != 2) {
                    switchFragment(lastFragment, 2)
                    lastFragment = 2
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


}
