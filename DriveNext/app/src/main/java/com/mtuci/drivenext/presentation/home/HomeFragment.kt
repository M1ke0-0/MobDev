package com.mtuci.drivenext.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mtuci.drivenext.databinding.FragmentHomeBinding
import com.mtuci.drivenext.presentation.cars.CarAdapter
import com.mtuci.drivenext.presentation.cars.handleCarListEvent
import com.mtuci.drivenext.presentation.cars.render
import com.mtuci.drivenext.presentation.cars.setupCarList
import com.mtuci.drivenext.presentation.common.collectWhenStarted

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = CarAdapter(onBookClick = viewModel::onBookClick, onDetailsClick = viewModel::onDetailsClick)
        binding.rvCars.setupCarList(adapter)
        binding.listState.btnRetry.setOnClickListener { viewModel.onRetryClick() }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Пользователь нажал «Найти» — ищем по окончательному запросу
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchView.clearFocus()
                viewModel.onSearchSubmit(query)
                return true
            }

            // На главной по каждой букве ничего не делаем — список обновляется на экране результатов
            override fun onQueryTextChange(newText: String): Boolean = false
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
}
