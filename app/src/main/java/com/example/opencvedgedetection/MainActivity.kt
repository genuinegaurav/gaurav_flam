
package com.example.opencvedgedetection
import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencvedgedetection.ui.CameraPreviewView
import com.example.opencvedgedetection.ui.EdgeGLView
import com.example.opencvedgedetection.ui.theme.OpenCVEdgeDetectionTheme
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class MainActivity:ComponentActivity(){
    //  native function that will process the img thru c++
    external fun processFrame(gray:ByteArray,w:Int,h:Int,mode:Int):ByteArray
    companion object{
        init{System.loadLibrary("opencv_java4");System.loadLibrary("native-lib")}
        private const val TAG="EdgeDetect"
        private const val REQ_CAMERA=1001
    }
    private lateinit var previewView:PreviewView
    private lateinit var cameraExecutor:ExecutorService
    private val edgeRenderer=EdgeRenderer()
    private lateinit var glView:GLSurfaceView
    private var cameraStarted by mutableStateOf(false)
    private var showEdges by mutableStateOf(true)
    private var fpsText by mutableStateOf("FPS: --")
    private var effectLabel by mutableStateOf("Canny")
    private var lastTimestamp=0L
    private var effectMode by mutableStateOf(0)
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor=Executors.newSingleThreadExecutor()
        setContent{
            OpenCVEdgeDetectionTheme{
                Scaffold(Modifier.fillMaxSize()){innerPadding->
                    Box(Modifier.fillMaxSize().padding(innerPadding)){
                        //camera preview composable bana rhe

                        CameraPreviewView{pv->
                            previewView=pv
                            if(!cameraStarted&&hasCamPermission()){startCamera();cameraStarted=true}else if(!hasCamPermission()){requestCamPermission()}
                        }
                        if(showEdges){
                            EdgeGLView(modifier=Modifier.fillMaxSize(),renderer=edgeRenderer){gv->glView=gv}
                        }
                        // displaying fps and they typeof effect
                        Text(text="$fpsText   |   $effectLabel",style=MaterialTheme.typography.bodyLarge,modifier=Modifier.align(Alignment.TopStart).padding(8.dp))
                        FloatingActionButton(onClick={
                            showEdges=!showEdges
                            if(::glView.isInitialized){glView.visibility=if(showEdges)View.VISIBLE else View.GONE}
                        },modifier=Modifier.align(Alignment.BottomEnd).padding(16.dp)){
                            Icon(imageVector=if(showEdges)Icons.Filled.VisibilityOff else Icons.Filled.Visibility,contentDescription=if(showEdges)"Hide edges" else "Show edges")
                        }
                        // on off button banaya hai to switch bw
                        FloatingActionButton(onClick={
                            effectMode=(effectMode+1)%3
                            edgeRenderer.effectMode=effectMode
                            effectLabel=when(effectMode){0->"Canny";1->"Threshold";else->"Invert"}
                        },modifier=Modifier.align(Alignment.BottomStart).padding(16.dp)){
                            Icon(imageVector=Icons.Filled.FilterAlt,contentDescription="Change filter")
                        }
                    }
                }
            }
        }
    }
    // checking camera permisison , request or handle the request
    override fun onDestroy(){super.onDestroy();cameraExecutor.shutdown()}
    private fun hasCamPermission()=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
    private fun requestCamPermission()=ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),REQ_CAMERA)
    override fun onRequestPermissionsResult(code:Int,perms:Array<out String>,res:IntArray){
        super.onRequestPermissionsResult(code,perms,res)
        if(code==REQ_CAMERA&&hasCamPermission()&&!cameraStarted){startCamera();cameraStarted=true}else if(!hasCamPermission())finish()
    }
    // starting logic
    private fun startCamera(){
        val providerFuture=ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider=providerFuture.get()
            val preview=Preview.Builder().build().also{it.setSurfaceProvider(previewView.surfaceProvider)}
            val analysis=ImageAnalysis.Builder().setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST).build().also{it.setAnalyzer(cameraExecutor,analyzer)}
            provider.unbindAll()
            provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,preview,analysis)
        },ContextCompat.getMainExecutor(this))
    }
    // analyzing the image thru analyzer
    private val analyzer=ImageAnalysis.Analyzer{proxy->
        val yBuf=proxy.planes[0].buffer
        val gray=ByteArray(yBuf.remaining()).also{yBuf.get(it)}
        val processed=processFrame(gray,proxy.width,proxy.height,effectMode)
        edgeRenderer.apply{
            width=proxy.width
            height=proxy.height
            edgeBuffer=ByteBuffer.wrap(processed).apply{rewind()}
        }
        if(showEdges&&::glView.isInitialized){runOnUiThread{glView.requestRender()}}
        val now=System.currentTimeMillis()
        if(lastTimestamp!=0L){
            val fps=1000.0/(now-lastTimestamp)
            fpsText="FPS: ${"%.1f".format(fps)}"
        }
        lastTimestamp=now
        Log.d(TAG,"effect=$effectMode  $fpsText")
        proxy.close()
    }
}
