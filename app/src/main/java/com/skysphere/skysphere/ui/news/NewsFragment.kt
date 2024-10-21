package com.skysphere.skysphere.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.R
import com.skysphere.skysphere.ui.adapters.NewsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {

    // UI components
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var globalNewsButton: Button
    private lateinit var localNewsButton: Button

    // Repository to fetch news data
    private val newsRepository = NewsRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)

        // Initialize UI components
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        globalNewsButton = view.findViewById(R.id.globalNewsButton)
        localNewsButton = view.findViewById(R.id.localNewsButton)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        updateNews(isLocal = true)
    }

    // Set up the RecyclerView with its adapter
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        newsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }
    }

    // Set up click listeners for the buttons
    private fun setupButtons() {
        globalNewsButton.setOnClickListener { updateNews(isLocal = false) }
        localNewsButton.setOnClickListener { updateNews(isLocal = true) }
    }

    // Fetch and display news
    private fun updateNews(isLocal: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch news articles
                val news = if (isLocal) {
                    newsRepository.fetchLocalWeatherNews()
                } else {
                    newsRepository.fetchGlobalWeatherNews()
                }
                Log.d("NewsFragment", "Fetched ${news.size} articles")

                // Update the RecyclerView with the new articles
                newsAdapter.submitList(news)

                // Show a message if no news is available
                if (news.isEmpty()) {
                    Toast.makeText(context, "No news available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Log any errors and show a toast message
                Log.e("NewsFragment", "Error updating news", e)
                Toast.makeText(context, "Error fetching news", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

