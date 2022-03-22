package com.family.mapboxaleph.models

import java.io.Serializable

data class Annotations(
    val mapboxId : String,
    val mapboxLongitude : Double,
    val mapboxLatitude : Double
) : Serializable
