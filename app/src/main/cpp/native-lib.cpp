
#include<jni.h>
#include<opencv2/imgproc.hpp>
#include<opencv2/core.hpp>
using namespace cv;
//helper funt to convert byte array into opencv mat
static Mat jb2m(JNIEnv*e,jbyteArray a,int w,int h){
    jbyte*d=e->GetByteArrayElements(a,nullptr);
    Mat m(h,w,CV_8UC1,reinterpret_cast<unsigned char*>(d));
    Mat c=m.clone();
    e->ReleaseByteArrayElements(a,d,JNI_ABORT);
    return c;
}
extern"C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_opencvedgedetection_MainActivity_processFrame(JNIEnv*e,jobject,jbyteArray g,jint w,jint h,jint m){
    //grayscale se mat me badal rhe
    Mat s=jb2m(e,g,w,h);
    Mat d;
    //m ki value dekhkar we will coose the correct operation to apply
    if(m==0){Canny(s,d,80,120);}
    else if(m==1){
        Mat b;
        GaussianBlur(s,b,Size(5,5),0);
        threshold(b,d,90,255,THRESH_BINARY_INV);
    }else if(m==2){bitwise_not(s,d);}
    // kisi se match nhi hua toh return same image
    else d=s;
    // new byte array for output
    jbyteArray o=e->NewByteArray(w*h);
    e->SetByteArrayRegion(o,0,w*h,reinterpret_cast<jbyte*>(d.data));
    return o;
}
