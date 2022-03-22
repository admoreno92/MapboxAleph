package com.family.mapboxaleph.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.family.mapboxaleph.R
import com.family.mapboxaleph.databinding.FragmentMapBinding
import com.family.mapboxaleph.models.Annotations
import com.family.mapboxaleph.ui.viewmodel.MapViewModel
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.utils.internal.logD
import java.io.Serializable
import java.lang.Exception
import java.util.ArrayList

class MapFragment : Fragment(R.layout.fragment_map), LocationListener {

    //User last know location
    lateinit var userLocationManager : LocationManager
    lateinit var location : Location

    //Mapbox map
    lateinit var mapboxMap : MapboxMap

    //Annotation
    lateinit var annotationManager : CircleAnnotationManager
    var annotationList : MutableList<Annotations> = mutableListOf()

    //Ask for permission
    lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    var fineLocationPermission : Boolean = false
    var coarseLocationPermission : Boolean = false

    //View binding
    lateinit var binding: FragmentMapBinding

    //Map Viewmodel
    lateinit var mapViewModel : MapViewModel

    //Dialog
    lateinit var loadingDialog : AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Get viewmodel
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        //Clear viewmodel annotation list
        if(mapViewModel.annotationListSize() > 0) {
            mapViewModel.clearAnnotationList()
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true && result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                Snackbar.make(binding.root,"Permiso otorgado", Snackbar.LENGTH_SHORT).show()
                getUserLocation()
            } else {
                Snackbar.make(binding.root,"Ubicacion Reynosa", Snackbar.LENGTH_SHORT).show()
                setupMap(1)
            }
        }

        mapboxMap = binding.mainMap.getMapboxMap().apply {
            loadStyleUri(
                Style.MAPBOX_STREETS
            ) {
                binding.mainMap.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                    locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_mylocation)
                    )
                }
            }
            addOnMapClickListener { point ->
                addAnnotationToMap(point)
                true
            }
        }
        val annotation = binding.mainMap.annotations
        annotationManager = annotation.createCircleAnnotationManager()
        askUserAboutLocation()

        binding.btnClick.setOnClickListener {
            annotationManager.annotations.forEach {
                val item = Annotations(
                    mapboxId = it.featureIdentifier,
                    mapboxLongitude = it.point.longitude(),
                    mapboxLatitude = it.point.latitude()
                )
                mapViewModel.addAnnotationItem(item)
            }
            findNavController().navigate(R.id.action_mapFragment_to_annotationsFragment)
        }
    }

    override fun onLocationChanged(loc: Location) {
        location = loc
        setupMap(2)
        userLocationManager.removeUpdates(this)
    }

    private fun askUserAboutLocation() {
        fineLocationPermission = ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        coarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineLocationPermission && coarseLocationPermission) {
            getUserLocation()
        } else {
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.apply {
                setTitle(getString(R.string.map_userpermission_title))
                setMessage(getString(R.string.map_userpermission_message))
                setPositiveButton(getString(R.string.map_userpermission_devicelocation)) { dialogInterface, i ->
                    val neededPermissions : MutableList<String> = mutableListOf()
                    if (!fineLocationPermission) {
                        neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if (!coarseLocationPermission) {
                        neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    if (neededPermissions.isNotEmpty()) {
                        permissionLauncher.launch(neededPermissions.toTypedArray())
                    }
                }
                setNegativeButton(getString(R.string.map_userpermission_reynosalocation)) { dialogInterface, i -> setupMap(1) }
            }.create().show()
        }
    }

    private fun getUserLocation() {
        val finePerm = ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarsePerm = ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        userLocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (finePerm && coarsePerm) {
            try {
                location = userLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
                setupMap(2)
            } catch (expection : Exception) {
                userLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,this)
            }
        }
    }
    private fun addAnnotationToMap(point : Point) {
        val pointOpt : CircleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(),point.latitude()))
            .withCircleRadius(8.0)
            .withCircleColor(ContextCompat.getColor(requireContext(),R.color.circleColor))
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor(ContextCompat.getColor(requireContext(),R.color.circleStroke))
        annotationManager.create(pointOpt)
    }
    private fun setupMap(option : Int) {
        when(option) {
            1 -> locateOnMap(-98.28835,26.08061)
            2 -> locateOnMap(location.longitude,location.latitude)
        }
    }
    private fun locateOnMap(lng : Double, lat : Double) {
        mapboxMap.flyTo(
            cameraOptions {
                center(Point.fromLngLat(lng,lat))
                zoom(15.0)
            },
            MapAnimationOptions.mapAnimationOptions {
                duration(10000)
            })
    }
}