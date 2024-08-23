package com.skysphere.skysphere.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavedLocationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Saved Locations Fragment"
    }
    val text: LiveData<String> = _text
}