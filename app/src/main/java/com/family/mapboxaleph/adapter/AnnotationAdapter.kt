package com.family.mapboxaleph.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.family.mapboxaleph.R
import com.family.mapboxaleph.api.RetrofitClient
import com.family.mapboxaleph.databinding.ItemAnnotationBinding
import com.family.mapboxaleph.models.Annotations
import com.family.mapboxaleph.util.Constants.Companion.TOKEN
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnotationAdapter : RecyclerView.Adapter<AnnotationAdapter.AnnotationViewHolder>() {
    class AnnotationViewHolder(val binding: ItemAnnotationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item : Annotations, context: Context) {
            val call : Call<ResponseBody> = RetrofitClient.getClient.downloadMapboxStaticImg(
                longitude = item.mapboxLongitude.toString(),
                latitude = item.mapboxLatitude.toString(),
                token = TOKEN
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val bytes: ByteArray = response.body()!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.apply {
                        binding.txtLongitude.text = String.format(context.getString(R.string.placeholder_lng),item.mapboxLongitude)
                        binding.txtLatitude.text = String.format(context.getString(R.string.placeholder_lat),item.mapboxLatitude)
                        binding.pbLoading.visibility = View.GONE
                        binding.ivStaticMapImage.apply {
                            visibility = View.VISIBLE
                            setImageBitmap(bitmap)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    binding.ivStaticMapImage.apply {
                        visibility = View.VISIBLE
                        setImageResource(R.drawable.ic_baseline_image)
                    }
                }

            })
        }
    }
    lateinit var data : MutableList<Annotations>
    lateinit var context: Context

    fun RecyclerAdapter(annotations : MutableList<Annotations>, context: Context) {
        this.data = annotations
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnotationViewHolder {
        val binding = ItemAnnotationBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return AnnotationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnotationViewHolder, position: Int) {
        val annotation = data[position]
        holder.bind(annotation, context)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}