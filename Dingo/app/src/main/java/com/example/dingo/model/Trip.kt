package com.example.dingo.model
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import com.google.gson.*
import java.lang.reflect.Type

data class Trip(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("locations") @set:PropertyName("locations") var locations: List<LatLng> = emptyList(),
    @get:PropertyName("discoveredEntries") @set:PropertyName("discoveredEntries") var discoveredEntries: List<String> = emptyList(),
) {
    companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Trip::class.java, TripDeserializer())
            .create()
    }
}

class TripDeserializer : JsonDeserializer<Trip> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Trip {
        val jsonObject = json?.asJsonObject
        val id = jsonObject?.get("id")?.asString ?: ""
        val userId = jsonObject?.get("userId")?.asString ?: ""
        val username = jsonObject?.get("username")?.asString ?: ""
        val discoveredEntries = jsonObject?.get("discoveredEntries")?.asJsonArray?.map { it.asString } ?: emptyList()

        val locations = jsonObject?.get("locations")?.asJsonArray?.map {
            val geoPoint = it.asJsonObject
            val latitude = geoPoint.get("latitude").asDouble
            val longitude = geoPoint.get("longitude").asDouble
            LatLng(latitude, longitude)
        } ?: emptyList()

        return Trip(id, userId, username, locations, discoveredEntries)
    }
}
