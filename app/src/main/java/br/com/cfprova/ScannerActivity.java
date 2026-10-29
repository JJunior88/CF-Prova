package br.com.cfprova;

import android.Manifest;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
    private TextView hint,summary;
    private Button shutter,retryCamera,flashButton;
    private Switch immediate;
    private Camera camera;
    private final ReadingConsensus consensus=new ReadingConsensus();
    private volatile boolean immediateEnabled=true;
    private volatile int previewCycle;
    private long lastAnalysis;
    private boolean lightOn;
    private ImageView reviewImage;
    private Dialog zoom;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final TransientPhoto<Bitmap> photo=new TransientPhoto<>(Bitmap::recycle);
    private ProcessCameraProvider provider;
    private ImageCapture capture;
    private volatile boolean active,busy,reviewing;
    private boolean starting;
    private int[] key,answers;
    private String exam;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        // Prevent the OS from storing a photo thumbnail in the recent-apps screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        key=getIntent().getIntArrayExtra("key");exam=getIntent().getStringExtra("exam");
        if(key==null||key.length<5||key.length>40){finish();return;}
        cameraScreen();permission();
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size){return AppUi.text(this,value,size,reviewing?AppUi.INK:Color.WHITE,false);}
    private LinearLayout screen(){
        LinearLayout root=AppUi.column(this);root.setBackgroundColor(reviewing?AppUi.BG:AppUi.NAVY);
        root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(16),i.getSystemWindowInsetTop()+dp(12),dp(16),i.getSystemWindowInsetBottom()+dp(8));return i;});
        getWindow().getDecorView().setSystemUiVisibility(reviewing?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR:0);
        getWindow().setStatusBarColor(reviewing?AppUi.BG:AppUi.NAVY);getWindow().setNavigationBarColor(reviewing?AppUi.BG:AppUi.NAVY);
        setContentView(root);root.requestApplyInsets();return root;
    }
    private Button button(LinearLayout root,int label,Runnable action){
        boolean primary=label==R.string.take_photo||label==R.string.finish_review;String icon=label==R.string.take_photo?"camera":label==R.string.finish_review?"check":null;
        Button b=AppUi.button(this,getString(label),primary,icon,action);root.addView(b,AppUi.block(this,8));return b;
    }
    private void cameraScreen(){
        discardPhoto();stopLight();consensus.reset();previewCycle++;reviewing=false;answers=null;capture=null;summary=null;
        LinearLayout root=screen();LinearLayout top=AppUi.row(this);top.addView(AppUi.badge(this,"02 / LEITURA",0xff9ee3d1,0xff244269),new LinearLayout.LayoutParams(0,-2,1));top.addView(AppUi.button(this,getString(R.string.back),false,"back",this::finish));root.addView(top);AppUi.gap(root,16);
        root.addView(AppUi.text(this,getString(R.string.camera_title),28,Color.WHITE,true));AppUi.gap(root,8);
        hint=AppUi.text(this,getString(R.string.camera_guide),14,0xffd9e3f4,false);root.addView(hint);AppUi.gap(root,16);
        FrameLayout stage=new FrameLayout(this);stage.setBackground(AppUi.shape(this,Color.BLACK,24));stage.setClipToOutline(true);LinearLayout.LayoutParams stageSize=new LinearLayout.LayoutParams(-1,0,1);stageSize.bottomMargin=dp(12);root.addView(stage,stageSize);
        preview=new PreviewView(this);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);stage.addView(preview,new FrameLayout.LayoutParams(-1,-1));stage.addView(new AppUi.CaptureGuide(this),new FrameLayout.LayoutParams(-1,-1));
        TextView guide=AppUi.badge(this,getString(R.string.frame_label),Color.WHITE,0xc0142b4d);FrameLayout.LayoutParams guideSize=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);guideSize.topMargin=dp(12);stage.addView(guide,guideSize);
        TextView privacy=AppUi.text(this,getString(R.string.camera_privacy),12,0xffc6d8ef,false);privacy.setGravity(Gravity.CENTER);root.addView(privacy);AppUi.gap(root,8);
        LinearLayout controls=AppUi.row(this);
        immediate=new Switch(this);immediate.setText(R.string.immediate_mode);immediate.setTextColor(Color.WHITE);immediate.setTextSize(13);immediate.setMinHeight(dp(48));immediate.setChecked(immediateEnabled);immediate.setContentDescription(getString(R.string.immediate_description));
        immediate.setOnCheckedChangeListener((v,enabled)->{immediateEnabled=enabled;previewCycle++;consensus.reset();hint.setText(enabled?R.string.immediate_guide:R.string.camera_guide);});
        controls.addView(immediate,new LinearLayout.LayoutParams(0,-2,1));
        flashButton=AppUi.button(this,getString(R.string.flash_off),false,null,this::toggleLight);flashButton.setEnabled(false);controls.addView(flashButton);root.addView(controls);AppUi.gap(root,8);
        shutter=button(root,R.string.take_photo,this::takePhoto);shutter.setMinHeight(dp(60));shutter.setEnabled(false);
        retryCamera=button(root,R.string.enable_camera,this::permission);
        if(provider!=null)bindCamera();
    }
    private void permission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){openCamera();return;}
        requestPermissions(new String[]{Manifest.permission.CAMERA},10);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==10){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)openCamera();else hint.setText(R.string.camera_permission);}
    }
    private void openCamera(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED||reviewing)return;
        if(provider!=null){bindCamera();return;}if(starting)return;starting=true;
        ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            starting=false;if(isFinishing()||isDestroyed()||reviewing)return;
            try{provider=future.get();bindCamera();}catch(Exception e){cameraError();}
        },ContextCompat.getMainExecutor(this));
    }
    private void bindCamera(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try{
            stopLight();consensus.reset();previewCycle++;provider.unbindAll();
            Preview view=new Preview.Builder().build();view.setSurfaceProvider(preview.getSurfaceProvider());
            capture=new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).setTargetResolution(new Size(1920,1440)).setJpegQuality(95).setFlashMode(ImageCapture.FLASH_MODE_OFF).build();
            ImageAnalysis analysis=new ImageAnalysis.Builder().setTargetResolution(new Size(1280,960)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            analysis.setAnalyzer(worker,this::analyzePreview);
            try{camera=provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,view,capture,analysis);}
            catch(IllegalArgumentException unsupported){
                // Some older camera implementations cannot run three use cases simultaneously.
                provider.unbindAll();analysis.clearAnalyzer();camera=provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,view,capture);
                immediateEnabled=false;immediate.setChecked(false);immediate.setEnabled(false);hint.setText(R.string.immediate_unavailable);
            }
            flashButton.setEnabled(camera.getCameraInfo().hasFlashUnit());flashButton.setText(camera.getCameraInfo().hasFlashUnit()?R.string.flash_off:R.string.flash_unavailable);
            preview.setOnTouchListener((v,event)->{if(event.getAction()==MotionEvent.ACTION_UP){v.performClick();try{camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(preview.getMeteringPointFactory().createPoint(event.getX(),event.getY())).build());}catch(IllegalArgumentException ignored){/* Camera is stopping. */}}return true;});
            shutter.setEnabled(true);retryCamera.setVisibility(View.GONE);if(immediateEnabled)hint.setText(R.string.immediate_guide);
        }catch(Exception e){cameraError();}
    }
    private void toggleLight(){
        if(camera==null||busy||!camera.getCameraInfo().hasFlashUnit())return;
        boolean requested=!lightOn;lightOn=requested;int cycle=++previewCycle;consensus.reset();flashButton.setText(requested?R.string.flash_on:R.string.flash_off);
        ListenableFuture<Void> change=camera.getCameraControl().enableTorch(requested);
        change.addListener(()->{if(cycle!=previewCycle||isFinishing())return;try{change.get();}catch(Exception e){lightOn=false;flashButton.setText(R.string.flash_off);hint.setText(R.string.flash_error);}},ContextCompat.getMainExecutor(this));
    }
    private void stopLight(){
        lightOn=false;if(camera!=null&&camera.getCameraInfo().hasFlashUnit())camera.getCameraControl().enableTorch(false);
        if(flashButton!=null)flashButton.setText(R.string.flash_off);
    }
    private void analyzePreview(ImageProxy image){
        try{
            long now=SystemClock.elapsedRealtime();if(!active||busy||reviewing||!immediateEnabled||now-lastAnalysis<300)return;lastAnalysis=now;int cycle=previewCycle;
            ImageProxy.PlaneProxy plane=image.getPlanes()[0];LumaFrame frame=LumaFrame.from(plane.getBuffer(),image.getWidth(),image.getHeight(),plane.getRowStride(),plane.getPixelStride(),image.getImageInfo().getRotationDegrees(),1280);
            LiveReader.Reading reading=new LiveReader().read(frame.pixels,frame.width,frame.height,key.length);int[] stable=consensus.accept(reading.answers,now);
            runOnUiThread(()->{
                if(!active||busy||reviewing||!immediateEnabled||cycle!=previewCycle||isFinishing())return;
                if(reading.answers==null)hint.setText(reading.hint);
                else hint.setText(getString(R.string.immediate_result,Grading.correct(key,reading.answers),key.length,Grading.grade(key,reading.answers)));
                if(stable!=null)takePhoto();
            });
        }catch(RuntimeException e){consensus.reset();}
        finally{image.close();}
    }
    private void cameraError(){stopLight();consensus.reset();previewCycle++;if(provider!=null)provider.unbindAll();camera=null;capture=null;shutter.setEnabled(false);flashButton.setEnabled(false);retryCamera.setVisibility(View.VISIBLE);hint.setText(R.string.camera_error);}
    private void takePhoto(){
        if(!active||busy||capture==null)return;
        busy=true;previewCycle++;consensus.reset();shutter.setEnabled(false);flashButton.setEnabled(false);hint.setText(R.string.processing_photo);final int token=photo.begin();
        // The in-memory callback has no OutputFileOptions, MediaStore or file URI.
        capture.takePicture(worker,new ImageCapture.OnImageCapturedCallback(){
            @Override public void onCaptureSuccess(ImageProxy image){
                Bitmap decoded=null;
                try{
                    decoded=decodePhoto(image);
                    LiveReader.Reading reading=readPhoto(decoded);Bitmap ready=decoded;decoded=null;
                    runOnUiThread(()->{
                        if(!active||isFinishing()||isDestroyed()){ready.recycle();return;}
                        if(!photo.accept(token,ready))return;
                        busy=false;answers=reading.answers==null?null:reading.answers.clone();reviewing=true;
                        stopLight();if(provider!=null)provider.unbindAll();reviewScreen(reading.hint);
                    });
                }catch(RuntimeException e){if(decoded!=null)decoded.recycle();captureFailed(token);}
                finally{image.close();}
            }
            @Override public void onError(ImageCaptureException e){captureFailed(token);}
        });
    }
    private Bitmap decodePhoto(ImageProxy image){
        if(image.getFormat()==android.graphics.ImageFormat.YUV_420_888){
            ImageProxy.PlaneProxy plane=image.getPlanes()[0];LumaFrame frame=LumaFrame.from(plane.getBuffer(),image.getWidth(),image.getHeight(),plane.getRowStride(),plane.getPixelStride(),image.getImageInfo().getRotationDegrees(),2000);
            int[] colors=new int[frame.pixels.length];for(int i=0;i<colors.length;i++){int y=frame.pixels[i]&255;colors[i]=Color.rgb(y,y,y);}return Bitmap.createBitmap(colors,frame.width,frame.height,Bitmap.Config.ARGB_8888);
        }
        ByteBuffer buffer=image.getPlanes()[0].getBuffer();byte[] jpeg=new byte[buffer.remaining()];buffer.get(jpeg);
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(jpeg,0,jpeg.length,options);
        options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>2400)options.inSampleSize*=2;
        options.inJustDecodeBounds=false;Bitmap decoded=BitmapFactory.decodeByteArray(jpeg,0,jpeg.length,options);if(decoded==null)throw new IllegalArgumentException("Invalid camera JPEG");
        int rotation=image.getImageInfo().getRotationDegrees();if(rotation!=0){Matrix m=new Matrix();m.setRotate(rotation);Bitmap upright=Bitmap.createBitmap(decoded,0,0,decoded.getWidth(),decoded.getHeight(),m,true);if(upright!=decoded)decoded.recycle();decoded=upright;}return decoded;
    }
    private LiveReader.Reading readPhoto(Bitmap image){
        double scale=Math.min(1,1800.0/Math.max(image.getWidth(),image.getHeight()));
        Bitmap small=Bitmap.createScaledBitmap(image,Math.max(1,(int)(image.getWidth()*scale)),Math.max(1,(int)(image.getHeight()*scale)),true);
        try{
            int w=small.getWidth(),h=small.getHeight();int[] colors=new int[w*h];small.getPixels(colors,0,w,0,0,w,h);byte[] luma=new byte[colors.length];
            for(int i=0;i<colors.length;i++){int c=colors[i];luma[i]=(byte)(int)(.299*Color.red(c)+.587*Color.green(c)+.114*Color.blue(c));}
            return new LiveReader().read(luma,w,h,key.length);
        }finally{if(small!=image)small.recycle();}
    }
    private void captureFailed(int token){runOnUiThread(()->{if(!active||!photo.isCurrent(token)||isFinishing())return;busy=false;consensus.reset();immediateEnabled=false;immediate.setChecked(false);shutter.setEnabled(capture!=null);flashButton.setEnabled(camera!=null&&camera.getCameraInfo().hasFlashUnit());hint.setText(R.string.photo_error);});}
    private void reviewScreen(String readingHint){
        LinearLayout root=screen();root.addView(AppUi.badge(this,"03 / CONFERÊNCIA",AppUi.BLUE,0xffe8eeff));AppUi.gap(root,12);
        root.addView(AppUi.text(this,getString(R.string.review_title),27,AppUi.INK,true));if(exam!=null){AppUi.gap(root,4);root.addView(AppUi.text(this,exam,14,AppUi.MUTED,false));}AppUi.gap(root,14);
        ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout content=AppUi.column(this);scroll.addView(content);
        LinearLayout result=AppUi.hero(this);result.addView(AppUi.text(this,getString(R.string.review_summary),10,0xffa9e8d5,true));AppUi.gap(result,8);summary=AppUi.text(this,"",27,Color.WHITE,true);result.addView(summary);content.addView(result,AppUi.block(this,14));updateSummary();
        LinearLayout imageCard=AppUi.card(this);LinearLayout caption=AppUi.row(this);caption.addView(AppUi.text(this,getString(R.string.review_photo),17,AppUi.INK,true),new LinearLayout.LayoutParams(0,-2,1));caption.addView(AppUi.badge(this,getString(R.string.zoom_label),AppUi.BLUE,0xffedf1ff));caption.setOnClickListener(v->zoomPhoto());caption.setContentDescription(getString(R.string.photo_description));imageCard.addView(caption);AppUi.gap(imageCard,12);
        reviewImage=new ImageView(this);reviewImage.setImageBitmap(photo.get());reviewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);reviewImage.setContentDescription(getString(R.string.photo_description));reviewImage.setBackground(AppUi.shape(this,0xffeaf0f7,14));reviewImage.setClipToOutline(true);imageCard.addView(reviewImage,new LinearLayout.LayoutParams(-1,dp(320)));reviewImage.setOnClickListener(v->zoomPhoto());AppUi.gap(imageCard,10);imageCard.addView(AppUi.text(this,getString(R.string.zoom_hint),12,AppUi.MUTED,false));content.addView(imageCard,AppUi.block(this,14));
        if(answers==null){LinearLayout warning=AppUi.card(this);warning.setBackground(AppUi.shape(this,0xfffff2dc,20));warning.addView(AppUi.text(this,getString(R.string.photo_unreadable,readingHint),15,0xff78521f,false));content.addView(warning,AppUi.block(this,12));}
        else{
            content.addView(AppUi.text(this,getString(R.string.answers_title),20,AppUi.INK,true));AppUi.gap(content,6);content.addView(AppUi.text(this,getString(R.string.review_answers),13,AppUi.MUTED,false));AppUi.gap(content,14);
            String[] choices={"A","B","C","D","E",getString(R.string.blank)};
            for(int i=0;i<answers.length;i++){
                final int index=i;LinearLayout card=AppUi.card(this);card.setPadding(dp(12),dp(12),dp(12),dp(12));LinearLayout row=AppUi.row(this);LinearLayout label=AppUi.column(this);label.addView(AppUi.text(this,getString(R.string.question_key,i+1,String.valueOf((char)('A'+key[i]))),15,AppUi.INK,true));AppUi.gap(label,6);TextView state=AppUi.badge(this,"",AppUi.TEAL,0xffe8f5ee);answerState(state,index);label.addView(state);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
                Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));spinner.setSelection(answers[i]<0?5:answers[i]);spinner.setBackground(AppUi.outline(this,0xfff4f7fc,12,AppUi.LINE));spinner.setContentDescription(getString(R.string.answer_description,i+1));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){if(answers!=null){answers[index]=pos==5?-1:pos;updateSummary();answerState(state,index);}}});
                row.addView(spinner,new LinearLayout.LayoutParams(dp(114),dp(52)));card.addView(row);content.addView(card,AppUi.block(this,8));
            }
        }
        AppUi.gap(content,10);content.addView(AppUi.text(this,getString(R.string.photo_memory),12,AppUi.MUTED,false));AppUi.gap(content,16);
        LinearLayout dock=AppUi.column(this);dock.setPadding(0,dp(8),0,0);root.addView(dock);
        if(answers!=null)button(dock,R.string.finish_review,()->{int[] resultAnswers=answers.clone();discardPhoto();setResult(RESULT_OK,new Intent().putExtra("answers",resultAnswers));finish();});
        LinearLayout secondary=AppUi.row(this);Button retake=AppUi.button(this,getString(R.string.retake),false,"camera",this::cameraScreen);Button exit=AppUi.button(this,getString(R.string.discard_exit),false,null,this::finish);LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,-2,1);half.rightMargin=dp(8);secondary.addView(retake,half);secondary.addView(exit,new LinearLayout.LayoutParams(0,-2,1));dock.addView(secondary);
    }
    private void answerState(TextView view,int i){boolean correct=answers[i]==key[i];view.setText(getString(correct?R.string.answer_correct:answers[i]<0?R.string.blank:R.string.answer_wrong));view.setTextColor(correct?AppUi.TEAL:AppUi.MUTED);view.setBackground(AppUi.shape(this,correct?0xffe7f5ee:0xffeef1f7,10));}
    private void updateSummary(){if(summary==null)return;if(answers==null)summary.setText(R.string.no_result);else summary.setText(getString(R.string.photo_result,Grading.correct(key,answers),key.length,Grading.grade(key,answers)));}
    private void zoomPhoto(){
        if(photo.get()==null)return;zoom=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);zoom.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setBackgroundColor(Color.BLACK);
        PhotoZoom image=new PhotoZoom(this,photo.get());box.addView(image,new LinearLayout.LayoutParams(-1,0,1));button(box,R.string.close_photo,()->{zoom.dismiss();zoom=null;});box.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(12),i.getSystemWindowInsetTop(),dp(12),i.getSystemWindowInsetBottom());return i;});zoom.setContentView(box);zoom.show();box.requestApplyInsets();
    }
    private void discardPhoto(){if(zoom!=null){zoom.dismiss();zoom=null;}if(reviewImage!=null){reviewImage.setImageDrawable(null);reviewImage=null;}photo.clear();}
    @Override protected void onStart(){super.onStart();active=true;}
    @Override protected void onStop(){active=false;previewCycle++;consensus.reset();stopLight();boolean discardAndExit=busy||reviewing;busy=false;discardPhoto();if(discardAndExit)finish();super.onStop();}
    @Override protected void onDestroy(){stopLight();discardPhoto();if(provider!=null)provider.unbindAll();worker.shutdown();super.onDestroy();}
    private static final class PhotoZoom extends View{
        private final Bitmap bitmap;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private final ScaleGestureDetector pinch;private float scale=1,panX,panY,lastX,lastY;
        PhotoZoom(Context context,Bitmap bitmap){super(context);this.bitmap=bitmap;setContentDescription(context.getString(R.string.photo_description));pinch=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector d){scale=Math.max(1,Math.min(6,scale*d.getScaleFactor()));invalidate();return true;}});}
        @Override protected void onDraw(Canvas c){if(bitmap.isRecycled())return;float fit=Math.min(getWidth()/(float)bitmap.getWidth(),getHeight()/(float)bitmap.getHeight());c.save();c.translate(getWidth()/2f+panX,getHeight()/2f+panY);c.scale(fit*scale,fit*scale);c.drawBitmap(bitmap,-bitmap.getWidth()/2f,-bitmap.getHeight()/2f,paint);c.restore();}
        @Override public boolean onTouchEvent(MotionEvent e){pinch.onTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();}else if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&e.getPointerCount()==1&&!pinch.isInProgress()){panX+=e.getX()-lastX;panY+=e.getY()-lastY;lastX=e.getX();lastY=e.getY();invalidate();}else if(e.getActionMasked()==MotionEvent.ACTION_UP)performClick();return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
