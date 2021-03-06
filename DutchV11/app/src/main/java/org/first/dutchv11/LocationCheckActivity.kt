package org.first.dutchv11

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import org.first.dutchv11.Data.LocationData
import org.first.dutchv11.databinding.ActivityLocationCheckBinding

class LocationCheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationCheckBinding
    private lateinit var locationInfo: LocationData
    private lateinit var tMapView: TMapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_check)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_check)
        locationInfo = LocationData(
            intent.getStringExtra("locationName"),
            intent.getStringExtra("locationAddress"),
            intent.getDoubleExtra("latitude", 0.0),
            intent.getDoubleExtra("longitude", 0.0)
        )
        Log.e("LocationInfo", locationInfo.toString())
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey("l7xx75e02f3eccaa4f56b3f269cb4a9f2b43")
        binding.mapview.addView(tMapView)

        infoSetting()

        binding.locationsetbutton.setOnClickListener {

         //   LocationSetData.data.add(locationInfo)

            val intent = Intent()
            intent.putExtra("UserChooseLocationName", locationInfo.locationName)
            intent.putExtra("UserChooseLocationAddress", locationInfo.locationAddress)
            intent.putExtra("UserChooseLocationLatitude", locationInfo.latitude)
            intent.putExtra("UserChooseLocationLongitude", locationInfo.longitude)
            Log.e("push Data", locationInfo.locationName)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    private fun infoSetting() {
        val markerItemPoint = TMapPoint(locationInfo.latitude, locationInfo.longitude)
        val markerImage = BitmapFactory.decodeResource(this.resources, R.drawable.markerblack)
        val markerItem = TMapMarkerItem()
        markerItem.icon = markerImage
        markerItem.tMapPoint = markerItemPoint
        tMapView.setCenterPoint(locationInfo.longitude, locationInfo.latitude)
        tMapView.addMarkerItem("markerItem", markerItem)

        binding.nametextview.text = locationInfo.locationName
        binding.addresstextview.text = locationInfo.locationAddress
    }
}