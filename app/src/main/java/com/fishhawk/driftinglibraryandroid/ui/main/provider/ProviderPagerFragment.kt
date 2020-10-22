package com.fishhawk.driftinglibraryandroid.ui.main.provider

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderPagerFragmentBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon

class ProviderPagerFragment : Fragment() {
    private lateinit var binding: ProviderPagerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProviderPagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val providerId = requireArguments().getString("providerId")!!
        val providerName = requireArguments().getString("providerName")!!

        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.title = providerName
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = ProviderPagerAdapter(
            requireContext(), childFragmentManager, providerId
        )
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun setupMenu(menu: Menu) {
        val providerId = requireArguments().getString("providerId")!!
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_hint)
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    setQuery("", false)
                    findNavController().navigate(
                        R.id.action_to_search,
                        bundleOf(
                            "providerId" to providerId,
                            "keywords" to query
                        )
                    )
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean = true
            })
        }

        menu.findItem(R.id.action_display_mode).setIcon(getDisplayModeIcon())
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                SettingsHelper.displayMode.setNextValue()
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> false
        }
    }
}