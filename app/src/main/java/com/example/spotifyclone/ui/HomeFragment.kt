package com.example.spotifyclone.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifyclone.Consts.TAG
import com.example.spotifyclone.R
import com.example.spotifyclone.Status
import com.example.spotifyclone.exoplayer.SongAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setUpRecyclerView()
        subscribeToObserver()
        songAdapter.setClickListener {
            Log.d(TAG, "mainViewmodel media ID ${it.mediaID}")
          mainViewModel.playOrToggleSong(it)
        }

    }
    private fun setUpRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
    }
    private fun subscribeToObserver(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){ result->
        when(result.status){
            Status.SUCCESS ->{
                Log.d("mTag","songs loaded")
                allSongsProgressBar.isVisible = false
                result.data?.let{ songs->
                    songAdapter.songs = songs //triggers submitList
                }
            }
            Status.ERROR ->Unit
            Status.LOADING -> allSongsProgressBar.isVisible = true
        }
        }
    }
}