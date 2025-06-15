package com.example.opencvedgedetection
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
class EdgeRenderer:GLSurfaceView.Renderer{
    //getting the height and width here of the texture
    @Volatile var edgeBuffer:ByteBuffer?=null
    @Volatile var width=0
    @Volatile var height=0
    @Volatile var effectMode=0
    private var prog=0
    private var tex=0
    private var posLoc=0
    private var uvLoc=0
    private var modeLoc=0
    private val vBuf:FloatBuffer
    //square screen cover vertex and the coordinates
    init{
        val v=floatArrayOf(-1f,-1f,0f,1f,1f,-1f,1f,1f,-1f,1f,0f,0f,1f,1f,1f,0f)
        vBuf=ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(v).apply{position(0)}
    }
    //compiling the shader
    private fun sh(t:Int,s:String)=GLES20.glCreateShader(t).also{
        GLES20.glShaderSource(it,s)
        GLES20.glCompileShader(it)
        val ok=IntArray(1)
        GLES20.glGetShaderiv(it,GLES20.GL_COMPILE_STATUS,ok,0)
        if(ok[0]==0)error(GLES20.glGetShaderInfoLog(it))
    }
    //linking with the helper
    private fun pg(vs:Int,fs:Int)=GLES20.glCreateProgram().also{
        GLES20.glAttachShader(it,vs)
        GLES20.glAttachShader(it,fs)
        GLES20.glLinkProgram(it)
        val ok=IntArray(1)
        GLES20.glGetProgramiv(it,GLES20.GL_LINK_STATUS,ok,0)
        if(ok[0]==0)error(GLES20.glGetProgramInfoLog(it))
    }
    override fun onSurfaceCreated(gl:GL10?,cfg:EGLConfig?){
        //for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0f,0f,0f,0f)
        // creating vertex shader uske alawa fragment shader
        val vs=sh(GLES20.GL_VERTEX_SHADER,"attribute vec2 aPos;attribute vec2 aUV;varying vec2 vUV;void main(){gl_Position=vec4(aPos,0.0,1.0);vUV=aUV;}")
        val fs=sh(GLES20.GL_FRAGMENT_SHADER,"precision mediump float;varying vec2 vUV;uniform sampler2D uTex;uniform int uMode;void main(){float a=texture2D(uTex,vUV).a;vec3 c=uMode==0?vec3(1.0):(uMode==1?vec3(a):vec3(1.0-a));gl_FragColor=vec4(c,a);}")
        //linking and using
        prog=pg(vs,fs)
        GLES20.glUseProgram(prog)
        posLoc=GLES20.glGetAttribLocation(prog,"aPos")
        uvLoc=GLES20.glGetAttribLocation(prog,"aUV")
        modeLoc=GLES20.glGetUniformLocation(prog,"uMode")
        val id=IntArray(1)
        GLES20.glGenTextures(1,id,0)
        tex=id[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,tex)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST)
    }
    override fun onSurfaceChanged(gl:GL10?,w:Int,h:Int){GLES20.glViewport(0,0,w,h)}
    //setting viewport according to new shape
    override fun onDrawFrame(gl:GL10?){
        val buf=edgeBuffer?:return
        if(width==0||height==0)return
        buf.rewind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(prog)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,tex)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_ALPHA,width,height,0,GLES20.GL_ALPHA,GLES20.GL_UNSIGNED_BYTE,buf)
        GLES20.glUniform1i(modeLoc,effectMode)
        //setting the effect mode
        val stride=16
        vBuf.position(0)
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc,2,GLES20.GL_FLOAT,false,stride,vBuf)
        vBuf.position(2)
        GLES20.glEnableVertexAttribArray(uvLoc)
        GLES20.glVertexAttribPointer(uvLoc,2,GLES20.GL_FLOAT,false,stride,vBuf)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
        val err=GLES20.glGetError()
        if(err!=GLES20.GL_NO_ERROR)Log.e("EdgeRenderer","GL error $err")
    }
}
