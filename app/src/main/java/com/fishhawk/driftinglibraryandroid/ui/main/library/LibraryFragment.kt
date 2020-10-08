package com.fishhawk.driftinglibraryandroid.ui.main.library

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.LibraryFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.extension.changeMangaListDisplayMode
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter

class LibraryFragment : Fragment() {
    private lateinit var binding: LibraryFragmentBinding
    private val viewModel: LibraryViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LibraryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MangaListAdapter(requireActivity(), null)
        adapter.onCardLongClicked = { outline ->
            AlertDialog.Builder(requireActivity())
                .setTitle("Confirm to delete manga?")
                .setPositiveButton("OK") { _, _ -> viewModel.deleteManga(outline.id) }
                .setNegativeButton("cancel") { _, _ -> }
                .show()
        }
        binding.mangaList.list.adapter = adapter

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.list.changeMangaListDisplayMode(adapter)
        })

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            adapter
        )

        val filter: String? = arguments?.getString("filter")
        viewModel.reloadIfNeed(filter ?: "")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_library, menu)

        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.maxWidth = Int.MAX_VALUE
        if (viewModel.filter != "") searchView.setQuery(viewModel.filter, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.reload(query)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return true
            }
        })

        val item = menu.findItem(R.id.action_display_mode)
        item.setIcon(getDisplayModeIcon())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                SettingsHelper.displayMode.setNextValue()
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}