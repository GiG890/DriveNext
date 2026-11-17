package com.example.drivenext.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivenext.R
import com.example.drivenext.presentation.common.AppViewModelFactory
import com.example.drivenext.presentation.common.LoadState
import com.example.drivenext.presentation.common.showLoadError
import com.example.drivenext.presentation.common.withBase
import com.example.drivenext.presentation.main.MainNavigator

/** «Главная»: поиск и список доступных автомобилей. */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels { AppViewModelFactory(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigator = requireActivity() as MainNavigator
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val rvCars = view.findViewById<RecyclerView>(R.id.rvCars)
        val progress = view.findViewById<View>(R.id.progress)
        val errorView = view.findViewById<View>(R.id.errorView)
        val tvError = view.findViewById<TextView>(R.id.tvError)

        val adapter = CarAdapter(
            onBook = { navigator.openPlaceholder(R.string.booking_title) },
            onDetails = { navigator.openPlaceholder(R.string.details_title) }
        )
        rvCars.layoutManager = LinearLayoutManager(requireContext())
        rvCars.adapter = adapter

        // Поиск: после подтверждения запроса открываем «Результаты поиска»
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val text = query?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    searchView.setQuery("", false)
                    searchView.clearFocus()
                    navigator.openSearchResults(text)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        view.findViewById<View>(R.id.btnRetry).setOnClickListener { viewModel.loadCars() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            progress.visibility = if (state is LoadState.Loading) View.VISIBLE else View.GONE
            errorView.visibility = if (state is LoadState.Error) View.VISIBLE else View.GONE
            rvCars.visibility = if (state is LoadState.Success) View.VISIBLE else View.GONE

            when (state) {
                is LoadState.Success -> adapter.submitList(state.data)
                is LoadState.Error -> tvError.text = state.error.withBase(requireContext(), R.string.error_load)
                is LoadState.Loading -> Unit
            }
        }

        viewModel.errors.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { showLoadError(it) }
        }
    }
}
