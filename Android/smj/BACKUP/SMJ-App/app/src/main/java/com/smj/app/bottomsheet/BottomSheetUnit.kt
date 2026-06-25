package com.smj.app.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smj.app.R
import com.smj.app.databinding.LayoutUnitBinding
import com.smj.app.helper.DateHelper

class BottomSheetUnit(adapterPosition: Int, var title: String) : BottomSheetDialogFragment() {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var bi: LayoutUnitBinding? = null
    var position: Int = adapterPosition

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("DetachAndAttachSameFragment", "ResourceType")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        //inflating layout
        val view: View = View.inflate(context, R.layout.layout_unit, null)

        //binding views to data binding.
        bi = DataBindingUtil.bind(view)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //setting layout with bottom sheet
        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from<View>(view.parent as View)

        //setting Peek at the 16:9 ratio keyline of its parent.
        (bottomSheetBehavior as BottomSheetBehavior<*>).peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        //setting max height of bottom sheet
        bi?.extraSpace?.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 1
        (bottomSheetBehavior as BottomSheetBehavior<*>).removeBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, i: Int) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    showView(bi!!.appBarLayout, actionBarSize)
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                }
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        //aap bar cancel button clicked
        bi?.cancelBtn?.setOnClickListener {
            dismiss()
        }

//        val year = DateHelper().getMonthlyOfYear(12)
//        Log.i("Year", year.toString())
//        Helper().showToast(year.toString(), requireActivity())

        bi?.nameToolbar?.text = title

        return bottomSheet
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideAppBar(view: View) {
        val params: ViewGroup.LayoutParams = view.layoutParams
        params.height = 0
        view.layoutParams = params
    }

    private fun showView(view: View, size: Int) {
        val params: ViewGroup.LayoutParams = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

    private val actionBarSize: Int
        get() {
            val array =
                requireContext().theme.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.actionBarSize))
            return array.getDimension(0, 0f).toInt()
        }
}