package com.smj.app.ui.main.view.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smj.app.ui.main.view.tab.MalamFragment
import com.smj.app.ui.main.view.tab.SiangFragment

class TabShiftAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val TAB_COUNT = 2

    override fun getItemCount(): Int {
        return TAB_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> SiangFragment()
            1 -> MalamFragment()
            else -> throw RuntimeException("Invalid position: $position")
        }
    }
}