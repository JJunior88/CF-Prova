package br.com.cfprova;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Size;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class ScannerActivity extends ComponentActivity {
    private PreviewView preview;
    private TextView hint;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final LiveReader reader=new LiveReader();
    private final StableReading stability=new StableReading();
    private ProcessCameraProvider provider;
    private long lastFrame;
    private volatile boolean completed;
    private volatile boolean active;
    private int questions;
    private boolean starting;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);questions=getIntent().getIntExtra("questions",0);
        if(questions<5||questions>40){finish();return;}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff102b2a);
        root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(20,i.getSystemWindowInsetTop(),20,i.getSystemWindowInsetBottom());return i;});
        TextView title=new TextView(this);title.setText(R.string.scanner_0);title.setTextSize(24);title.setTextColor(-1);title.setPadding(20,16,20,8);root.addView(title);
        hint=new TextView(this);hint.setText(R.string.scanner_1);hint.setTextColor(-1);hint.setTextSize(16);hint.setPadding(20,12,20,20);root.addView(hint);
        preview=new PreviewView(this);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        Button retry=new Button(this);retry.setText(R.string.scanner_2);retry.setOnClickListener(v->permission());root.addView(retry);
        Button back=new Button(this);back.setText(R.string.scanner_3);back.setOnClickListener(v->finish());root.addView(back);
        setContentView(root);root.requestApplyInsets();permission();
    }
    private void permission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){openCamera();return;}
        requestPermissions(new String[]{Manifest.permission.CAMERA},10);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==10){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)openCamera();else hint.setText(R.string.scanner_4);}
    }
    private void openCamera(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        if(starting||provider!=null)return;starting=true;
        ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            starting=false;if(isFinishing()||isDestroyed())return;
            try{
                provider=future.get();
                Preview view=new Preview.Builder().build();view.setSurfaceProvider(preview.getSurfaceProvider());
                ImageAnalysis analysis=new ImageAnalysis.Builder().setTargetResolution(new Size(1280,960)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(worker,this::analyze);
                provider.unbindAll();
                Camera camera=provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,view,analysis);
                preview.setOnTouchListener((v,event)->{if(event.getAction()==MotionEvent.ACTION_UP){v.performClick();camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(preview.getMeteringPointFactory().createPoint(event.getX(),event.getY())).build());}return true;});
            }catch(Exception e){if(provider!=null)provider.unbindAll();provider=null;hint.setText(R.string.scanner_5);}
        },ContextCompat.getMainExecutor(this));
    }
    private void analyze(ImageProxy image){
        try{
            long now=SystemClock.elapsedRealtime();if(!active||completed||now-lastFrame<250)return;lastFrame=now;
            ImageProxy.PlaneProxy plane=image.getPlanes()[0];ByteBuffer buffer=plane.getBuffer();int base=buffer.position();
            int rotation=image.getImageInfo().getRotationDegrees(),iw=image.getWidth(),ih=image.getHeight();
            int step=Math.max(1,(Math.max(iw,ih)+1279)/1280),sw=iw/step,sh=ih/step;
            int w=rotation==90||rotation==270?sh:sw,h=rotation==90||rotation==270?sw:sh;byte[] luma=new byte[w*h];
            for(int y=0;y<sh;y++)for(int x=0;x<sw;x++){
                int dx=x,dy=y;
                if(rotation==90){dx=sh-1-y;dy=x;}else if(rotation==180){dx=sw-1-x;dy=sh-1-y;}else if(rotation==270){dx=y;dy=sw-1-x;}
                luma[dy*w+dx]=buffer.get(base+y*step*plane.getRowStride()+x*step*plane.getPixelStride());
            }
            LiveReader.Reading reading=reader.read(luma,w,h,questions);
            int[] stable=stability.accept(reading.answers,now);
            if(stable!=null){runOnUiThread(()->{if(!active||completed||isFinishing()||isDestroyed())return;completed=true;setResult(RESULT_OK,new Intent().putExtra("answers",stable));finish();});}
            else runOnUiThread(()->{if(!completed&&!isFinishing())hint.setText(reading.hint);});
        }catch(RuntimeException e){stability.reset();runOnUiThread(()->{if(!completed&&!isFinishing())hint.setText(R.string.scanner_6);});}
        finally{image.close();}
    }
    @Override protected void onStart(){super.onStart();active=true;}
    @Override protected void onStop(){active=false;super.onStop();worker.execute(stability::reset);}
    @Override protected void onDestroy(){completed=true;if(provider!=null)provider.unbindAll();worker.shutdown();super.onDestroy();}
}
