package com.dutch2019.extension

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.Observable
import com.dutch2019.MarkerOverlay
import com.dutch2019.R
import com.dutch2019.base.BaseViewModel
import com.dutch2019.model.LocationInfo
import com.dutch2019.ui.middle.MiddleLocationViewModel
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import kotlinx.android.synthetic.main.fragment_middle_location.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

lateinit var tMapView : TMapView

@BindingAdapter(value = ["mapsetting"])
fun mapSetting(mapLayout: LinearLayout, locationInfo: LocationInfo) {
    val markerItemPoint = TMapPoint(locationInfo.latitude, locationInfo.longitude)
    val markerImage = BitmapFactory.decodeResource(
        mapLayout.context.resources,
        R.drawable.result_ic_marker_black
    )
    val markerItem = TMapMarkerItem().apply {
        icon = markerImage
        tMapPoint = markerItemPoint
        setPosition(0.5F, 1F)
    }
    val tMapView = TMapView(mapLayout.context).apply {
        setSKTMapApiKey("l7xx75e02f3eccaa4f56b3f269cb4a9f2b43")
        setCenterPoint(locationInfo.longitude, locationInfo.latitude)
        addMarkerItem("markerItem", markerItem)
    }
    mapLayout.addView(tMapView)
}

@BindingAdapter(value = ["mapview"])
fun mapview(layout: LinearLayout, viewModel: BaseViewModel) {

    val viewModel = viewModel as MiddleLocationViewModel
    tMapView = TMapView(layout.context)
    layout.addView(tMapView)

    viewModel.setCenterPoint(viewModel.calculateCenterPoint(viewModel.getLocationList()))

    // centerpoint가 확정되고나서 작업들이 진행되니까 rx로 순차적으로 처리하는 방식을 적용하면 좋을것같다.

    markSearchLoaction(tMapView, layout.context, viewModel.getLocationList())
    markMiddleLocation(tMapView, layout.context, viewModel.getCenterPoint())
    setBallonOverlayClickEvent(tMapView, viewModel)
    CoroutineScope(Dispatchers.IO).launch {
        //  setPolyLine(tMapView, viewModel.getLocationList(), viewModel.getCenterPoint())
        //  -> t map api 일일 할당량이 1000인데 소요시간과 경로탐색을 2번하게되서 더 필요없어보이는 자동차 경로 그리기를 제외.
        viewModel.setLocationAddress(viewModel.getCenterPoint())
        viewModel.setNearSubway(viewModel.getCenterPoint())
    }
    mapAutoZoom(tMapView, viewModel.getLocationList(), viewModel.getCenterPoint())
}

@BindingAdapter(value = ["ratiocheck"])
fun ratioCheck(layout: LinearLayout, ratioPoint: TMapPoint) {
    setMarkRatioLocation(tMapView, layout.context, ratioPoint)
}

fun markSearchLoaction(
    tMapView: TMapView,
    context: Context,
    locationList: ArrayList<LocationInfo>
) {

    if (locationList.isNotEmpty()) {
        for (i in 0 until locationList.size) {
            val markerItemPoint =
                TMapPoint(locationList[i].latitude, locationList[i].longitude)

            val markerImage =
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.result_ic_marker_black
                )

            val marker = MarkerOverlay(
                context,
                tMapView,
                locationList[i].name,
                "marker2$i"
            )
            val strId = locationList[i].name
            marker.id = strId
            marker.icon = markerImage
            marker.setPosition(0.5F, 1F)
            marker.tMapPoint = markerItemPoint

            tMapView.addMarkerItem2(strId, marker)
        }
    }

}

fun markMiddleLocation(tMapView: TMapView, context: Context, centerPoint: TMapPoint) {
    val markerImage =
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.group6
        )
    val marker = MarkerOverlay(context, tMapView, "중간지점", "middlemarkerItem")
    val strId = "중간지점"
    marker.id = strId
    marker.changeTextRedColor(context)
    marker.icon = markerImage
    marker.setPosition(0.5F, 1F)
    marker.tMapPoint = centerPoint
    tMapView.addMarkerItem2(strId, marker)
}


fun mapAutoZoom(tMapView: TMapView, locationList: ArrayList<LocationInfo>, centerPoint: TMapPoint) {
    var leftTopLat = centerPoint.latitude
    var leftTopLon = centerPoint.longitude
    var rightBottomLat = centerPoint.latitude
    var rightBottomLon = centerPoint.longitude
    try {
        for (i in 0 until locationList.size) {
            if (locationList.isNotEmpty()) {
                if (locationList[i].latitude >= leftTopLat) {
                    leftTopLat = locationList[i].latitude
                }
                if (locationList[i].longitude >= leftTopLon) {
                    leftTopLon = locationList[i].longitude
                }
                if (locationList[i].latitude <= rightBottomLat) {
                    rightBottomLat = locationList[i].latitude
                }

                if (locationList[i].longitude <= rightBottomLon) {
                    rightBottomLon = locationList[i].longitude
                }
            }

        }
        val leftTopPoint = TMapPoint(leftTopLat, leftTopLon)
        val rightBottomPoint = TMapPoint(rightBottomLat, rightBottomLon)
        tMapView.setCenterPoint(centerPoint.longitude, centerPoint.latitude)
        tMapView.zoomToTMapPoint(leftTopPoint, rightBottomPoint)
    } catch (e: Exception) {
        e.printStackTrace()
        tMapView.setCenterPoint(centerPoint.longitude, centerPoint.latitude)
    }

}

fun setBallonOverlayClickEvent(tMapView: TMapView, baseViewModel: BaseViewModel) {
    var viewModel = baseViewModel as MiddleLocationViewModel
    tMapView.setOnMarkerClickEvent { _, p1 ->
        val point = p1.tMapPoint
        //   (viewModel as MiddleLocationViewModel).setCenterPoint(point)
        viewModel.setLocationAddress(point)
        viewModel.setNearSubway(point)
        tMapView.rootView.textview_middle_result.text = p1.id
        if (p1.id == "중간지점") {
            tMapView.rootView.textview_middle_result.setTextColor(
                ContextCompat.getColor(tMapView.rootView.context, R.color.orange)
            )
            viewModel.resetRouteTime()
        }
        else if(p1.id =="비율변경지점"){
            tMapView.rootView.textview_middle_result.setTextColor(
                ContextCompat.getColor(tMapView.rootView.context, R.color.blue)
            )
            viewModel.getMiddleRouteTime(viewModel.getCenterPoint(), p1.latitude, p1.longitude)
        }
        else {
            tMapView.rootView.textview_middle_result.setTextColor(
                ContextCompat.getColor(tMapView.rootView.context, R.color.black)
            )
            viewModel.getMiddleRouteTime(viewModel.getCenterPoint(), p1.latitude, p1.longitude)
        }
    }

}


fun setMarkRatioLocation(tMapView: TMapView, context: Context, ratioPoint: TMapPoint) {
    val markerImage =
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.result_ic_maker_blue
        )


    val marker = MarkerOverlay(context, tMapView, "비율변경지점", "ratioMarkerItem")

    val strId = "비율변경지점"
    marker.id = strId
    marker.chagneTextBlueColor(context)
    marker.icon = markerImage
    marker.setPosition(0.5F, 1F)
    marker.tMapPoint = ratioPoint

    tMapView.addMarkerItem2(strId, marker)
    Log.e("MARKERPOINTBLUE", ratioPoint.toString())
   // marker.markerTouch
}

/*
fun setPolyLine(
    tMapView: TMapView,
    locationList: ArrayList<LocationInfo>,
    centerPoint: TMapPoint
) {
    try {
        for (i in 0 until locationList.size) {
            val startPoint =
                TMapPoint(locationList[i].latitude, locationList[i].longitude)
            val tMapPolyLine = TMapData().findPathData(startPoint, centerPoint)
            tMapPolyLine.lineColor = Color.BLACK
            tMapPolyLine.lineWidth = 16F
            tMapView.addTMapPolyLine("Line$i", tMapPolyLine)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}
*/


