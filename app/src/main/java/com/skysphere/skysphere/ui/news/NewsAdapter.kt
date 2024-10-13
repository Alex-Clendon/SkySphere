package com.skysphere.skysphere.ui.news

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.API.Article
import com.skysphere.skysphere.R

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    // List to hold the news articles
    private var newsList: List<Article> = emptyList()

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    // Return the size of dataset
    override fun getItemCount() = newsList.size

    // Update list of articles and refresh the view
    fun submitList(news: List<Article>) {
        newsList = news
        notifyDataSetChanged()
    }

    // ViewHolder for news items
    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.newsTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.newsDescriptionTextView)

        // Bind article data to the view
        fun bind(article: Article) {
            titleTextView.text = article.title
            descriptionTextView.text = article.description

            // Set click listener to open article URL
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                itemView.context.startActivity(intent)
            }
        }
    }
}