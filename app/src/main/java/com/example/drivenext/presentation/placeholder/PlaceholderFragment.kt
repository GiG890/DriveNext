package com.example.drivenext.presentation.placeholder

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.drivenext.R
import com.example.drivenext.presentation.main.MainNavigator

/** Временный экран для разделов, макеты которых будут в следующих работах. */
class PlaceholderFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleRes = requireArguments().getInt(ARG_TITLE)
        val showBack = requireArguments().getBoolean(ARG_SHOW_BACK)

        view.findViewById<TextView>(R.id.tvTitle).setText(titleRes)

        val back = view.findViewById<View>(R.id.btnBack)
        if (showBack) {
            back.setOnClickListener { (requireActivity() as MainNavigator).goBack() }
        } else {
            back.visibility = View.INVISIBLE
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SHOW_BACK = "show_back"

        fun newInstance(@StringRes titleRes: Int, showBack: Boolean): PlaceholderFragment =
            PlaceholderFragment().apply {
                arguments = bundleOf(ARG_TITLE to titleRes, ARG_SHOW_BACK to showBack)
            }
    }
}
