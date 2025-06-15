package com.example.opencvedgedetection.ui
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.opencvedgedetection.EdgeRenderer
@Composable
fun EdgeGLView(modifier:Modifier=Modifier,renderer:EdgeRenderer,onViewReady:(GLSurfaceView)->Unit={}){
    //when view is ready then callback
    AndroidView(factory={c->
        GLSurfaceView(c).apply{
            //setting opengl es2
            setEGLContextClientVersion(2)
            // buffers for color and depth
            setEGLConfigChooser(8,8,8,8,16,0)
            setZOrderOnTop(true)
            holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
            setRenderer(renderer)
            //whenevr we render requestRender() gets called this is imp
            renderMode=GLSurfaceView.RENDERMODE_WHEN_DIRTY
            onViewReady(this)
        }
    },modifier=modifier)
}
