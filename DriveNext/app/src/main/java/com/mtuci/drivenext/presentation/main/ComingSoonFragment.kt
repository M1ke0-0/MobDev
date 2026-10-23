package com.mtuci.drivenext.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.mtuci.drivenext.databinding.FragmentComingSoonBinding

/** Заглушка для экранов, которых ещё нет в ТЗ (например, «Подключить свой автомобиль»). */
class ComingSoonFragment : Fragment() {

    private var _binding: FragmentComingSoonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComingSoonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        binding.toolbar.setTitle(args.getInt(ARG_TITLE))
        if (args.getBoolean(ARG_SHOW_BACK)) {
            binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        } else {
            binding.toolbar.navigationIcon = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SHOW_BACK = "show_back"

        fun newInstance(@StringRes titleRes: Int, showBack: Boolean = true) = ComingSoonFragment().apply {
            arguments = bundleOf(ARG_TITLE to titleRes, ARG_SHOW_BACK to showBack)
        }
    }
}
