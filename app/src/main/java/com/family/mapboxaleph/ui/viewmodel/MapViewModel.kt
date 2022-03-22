package com.family.mapboxaleph.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.family.mapboxaleph.api.RetrofitClient
import com.family.mapboxaleph.models.Annotations
import com.family.mapboxaleph.util.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel : ViewModel() {

    private var mutableAnnotations : MutableList<Annotations> = mutableListOf()

    //region Annotation List methods
    fun addAnnotationItem(item : Annotations) {
        mutableAnnotations.add(item)
    }
    fun getAnnotationList() : MutableList<Annotations> {
        return mutableAnnotations
    }
    fun annotationListSize() : Int {
        return mutableAnnotations.size
    }
    fun clearAnnotationList() {
        mutableAnnotations.clear()
    }
    //endregion

}