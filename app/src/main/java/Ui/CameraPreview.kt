package com.example.opencvedgedetection.ui
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
@Composable
fun CameraPreviewView(cb:(PreviewView)->Unit){
    //tryin to compose the preview-view of android through the androidview
    AndroidView(
        factory={c->
            //creating new preview
            val v=PreviewView(c)
            v.layoutParams=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
            cb(v);v
        },
        //access the whole area , for tht a modifier used
        modifier=Modifier.fillMaxSize()
    )
}
