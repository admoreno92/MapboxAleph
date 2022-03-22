package com.family.mapboxaleph.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.family.mapboxaleph.R
import com.family.mapboxaleph.adapter.AnnotationAdapter
import com.family.mapboxaleph.databinding.FragmentAnnotationsBinding
import com.family.mapboxaleph.databinding.FragmentMapBinding
import com.family.mapboxaleph.models.Annotations
import com.family.mapboxaleph.ui.viewmodel.MapViewModel

class AnnotationsFragment : Fragment(R.layout.fragment_annotations) {

    //View binding
    lateinit var binding: FragmentAnnotationsBinding

    //Map Viewmodel
    lateinit var mapViewModel : MapViewModel

    val annotationAdapter = AnnotationAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnotationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        binding.rvAnnotaitonsStaticImg.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.HORIZONTAL))
            annotationAdapter.RecyclerAdapter(mapViewModel.getAnnotationList(),context)
            adapter = annotationAdapter
        }

    }

}