package com.fishhawk.driftinglibraryandroid.explore

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Source
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.launch

class GlobalSearchFragment : Fragment() {
    private val viewModel: GlobalSearchViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        ExploreViewModelFactory("", remoteLibraryRepository)
    }
    private lateinit var binding: GlobalSearchFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GlobalSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.keywords = arguments?.getString("keywords")!!
        viewModel.sourceList.observe(viewLifecycleOwner, Observer { result ->
            if (result is Result.Success) search()
        })
    }

    private fun search() {
        val keywords = viewModel.keywords
        val sourceList = (viewModel.sourceList.value as? Result.Success)?.data ?: return
        val adapter = GlobalSearchGroupListAdapter(requireActivity())
        binding.list.adapter = adapter

        for (source in sourceList) {
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.addResultGroup(source.name)
                val result = viewModel.search(source.name, keywords)
                adapter.updateResultFromSource(source.name, result)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_global_search, menu)

        val searchView = menu.findItem(R.id.action_search_global).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_global_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.keywords = query ?: ""
                search()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean = true
        })
    }
}