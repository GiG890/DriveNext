package com.example.drivenext.presentation.search

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivenext.R
import com.example.drivenext.presentation.common.AppViewModelFactory
import com.example.drivenext.presentation.common.LoadState
import com.example.drivenext.presentation.common.showLoadError
import com.example.drivenext.presentation.common.withBase
import com.example.drivenext.presentation.home.CarAdapter
import com.example.drivenext.presentation.main.MainNavigator

/** «Результаты поиска»: загрузка, список найденных автомобилей или ошибка с кнопкой «Повторить». */
class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {

    private val viewModel: SearchResultsViewModel by viewModels { AppViewModelFactory(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigator = requireActivity() as MainNavigator
        val query = requireArguments().getString(ARG_QUERY).orEmpty()

        view.findViewById<TextView>(R.id.tvTitle).setText(R.string.search_results_title)
        view.findViewById<View>(R.id.btnBack).setOnClickListener { navigator.goBack() }

        val rvCars = view.findViewById<RecyclerView>(R.id.rvCars)
        val loadingView = view.findViewById<View>(R.id.loadingView)
        val tvEmpty = view.findViewById<View>(R.id.tvEmpty)
        val errorView = view.findViewById<View>(R.id.errorView)
        val tvError = view.findViewById<TextView>(R.id.tvError)

        val adapter = CarAdapter(
            onBook = { navigator.openPlaceholder(R.string.booking_title) },
            onDetails = { navigator.openPlaceholder(R.string.details_title) }
        )
        rvCars.layoutManager = LinearLayoutManager(requireContext())
        rvCars.adapter = adapter

        view.findViewById<View>(R.id.btnRetry).setOnClickListener { viewModel.retry() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val isSuccess = state is LoadState.Success
            loadingView.visibility = if (state is LoadState.Loading) View.VISIBLE else View.GONE
            errorView.visibility = if (state is LoadState.Error) View.VISIBLE else View.GONE
            rvCars.visibility = if (isSuccess && state.data.isNotEmpty()) View.VISIBLE else View.GONE
            tvEmpty.visibility = if (isSuccess && state.data.isEmpty()) View.VISIBLE else View.GONE

            when (state) {
                is LoadState.Success -> adapter.submitList(state.data)
                is LoadState.Error ->
                    tvError.text = state.error.withBase(requireContext(), R.string.error_search)
                is LoadState.Loading -> Unit
            }
        }

        viewModel.errors.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { showLoadError(it) }
        }

        // Запрос выполняется при открытии экрана (и повторно, если приложение было перезапущено)
        viewModel.search(query)
    }

    companion object {
        private const val ARG_QUERY = "query"

        fun newInstance(query: String): SearchResultsFragment =
            SearchResultsFragment().apply { arguments = bundleOf(ARG_QUERY to query) }
    }
}
