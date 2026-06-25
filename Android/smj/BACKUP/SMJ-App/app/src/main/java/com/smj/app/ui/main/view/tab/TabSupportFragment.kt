package com.smj.app.ui.main.view.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smj.app.databinding.FragmentTabSupportBinding

class TabSupportFragment : Fragment() {

    private lateinit var binding: FragmentTabSupportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabSupportBinding.inflate(layoutInflater)



        return binding.root
    }

    companion object {
    }
}