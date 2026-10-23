package com.mtuci.drivenext.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mtuci.drivenext.databinding.FragmentSearchResultsBinding
import com.mtuci.drivenext.presentation.cars.CarAdapter
import com.mtuci.drivenext.presentation.cars.handleCarListEvent
import com.mtuci.drivenext.presentation.cars.render
import com.mtuci.drivenext.presentation.cars.setupCarList
import com.mtuci.drivenext.presentation.common.collectWhenStarted

class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchResultsViewModel by viewModels { SearchResultsViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val adapter = CarAdapter(onBookClick = viewModel::onBookClick, onDetailsClick = viewModel::onDetailsClick)
        binding.rvCars.setupCarList(adapter)
        binding.listState.btnRetry.setOnClickListener { viewModel.onRetryClick() }

        binding.searchView.setQuery(viewModel.currentQuery, false)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Поиск уже запущен из onQueryTextChange — здесь только прячем клавиатуру
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            // Каждое изменение текста отдаём во ViewModel, она сама выждет паузу и обновит список
            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.onQueryChanged(newText)
                return true
            }
        })

        viewLifecycleOwner.collectWhenStarted(viewModel.state) { state ->
            adapter.submitList(state.cars)
            binding.listState.render(state, binding.rvCars)
        }
        viewLifecycleOwner.collectWhenStarted(viewModel.events, ::handleCarListEvent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenStarted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(query: String) = SearchResultsFragment().apply {
            arguments = bundleOf(SearchResultsViewModel.KEY_QUERY to query)
        }
    }
}
