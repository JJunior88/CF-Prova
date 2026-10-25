package br.com.cfprova;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.Locale;

/** Shared visual language. Optical card drawing and camera/photo ownership stay separate. */
public final class AppUi {
    public static final int BG=0xfff4f6fb,INK=0xff152943,MUTED=0xff5b6b82,BLUE=0xff345ce9,NAVY=0xff142b4d,LINE=0xffe1e7f0,TEAL=0xff087f68;
    private AppUi(){}
    public static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    public static TextView text(Context c,String value,int size,int color,boolean bold){TextView t=new TextView(c);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");t.setTypeface(android.graphics.Typeface.create(bold?"sans-serif-medium":"sans-serif",bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL));t.setLineSpacing(dp(c,3),1);return t;}
    public static GradientDrawable shape(Context c,int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d;}
    public static GradientDrawable outline(Context c,int color,int radius,int stroke){GradientDrawable d=shape(c,color,radius);d.setStroke(dp(c,1),stroke);return d;}
    public static LinearLayout column(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    public static LinearLayout row(Context c){LinearLayout v=new LinearLayout(c);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    public static LinearLayout card(Context c){LinearLayout v=column(c);v.setPadding(dp(c,16),dp(c,16),dp(c,16),dp(c,16));v.setBackground(outline(c,Color.WHITE,22,LINE));return v;}
    public static LinearLayout.LayoutParams block(Context c,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(c,bottom);return p;}
    public static void gap(LinearLayout parent,int size){View s=new View(parent.getContext());parent.addView(s,new LinearLayout.LayoutParams(1,dp(parent.getContext(),size)));}
    public static TextView badge(Context c,String value,int color,int background){TextView t=text(c,value,11,color,true);t.setPadding(dp(c,10),dp(c,6),dp(c,10),dp(c,6));t.setBackground(shape(c,background,10));return t;}
    public static LinearLayout hero(Context c){LinearLayout v=column(c);GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,0xff244d98});d.setCornerRadius(dp(c,26));v.setBackground(d);v.setPadding(dp(c,22),dp(c,22),dp(c,22),dp(c,22));return v;}
    public static Button button(Context c,String label,boolean primary,String icon,Runnable action){
        Button b=new Button(c);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.BOLD));b.setMinHeight(dp(c,52));b.setMinimumHeight(dp(c,52));b.setMinWidth(0);b.setMinimumWidth(0);b.setGravity(Gravity.CENTER);b.setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10));
        int enabled=primary?Color.WHITE:BLUE;
        b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{0xff8b99af,enabled}));
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{-android.R.attr.state_enabled},shape(c,0xffe5eaf2,16));states.addState(new int[]{},primary?shape(c,BLUE,16):outline(c,Color.WHITE,16,LINE));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(primary?0x45ffffff:0x18345ce9),states,null));b.setStateListAnimator(null);
        if(icon!=null){Glyph g=new Glyph(c,icon,enabled);b.setCompoundDrawablesRelativeWithIntrinsicBounds(g,null,null,null);b.setCompoundDrawablePadding(dp(c,8));}
        b.setOnClickListener(v->action.run());return b;
    }
    public static void section(LinearLayout parent,String number,String title,String subtitle){Context c=parent.getContext();LinearLayout row=row(c);TextView n=badge(c,number,BLUE,0xffedf1ff);row.addView(n);TextView h=text(c,title,18,INK,true);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,-2,1);hp.leftMargin=dp(c,10);row.addView(h,hp);parent.addView(row);if(subtitle!=null){gap(parent,8);parent.addView(text(c,subtitle,13,MUTED,false));}gap(parent,16);}
    public static void decorateChoice(Button b,boolean selected){Context c=b.getContext();b.setSelected(selected);b.setTextColor(selected?Color.WHITE:INK);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30345ce9),selected?shape(c,BLUE,12):outline(c,0xfff5f7fc,12,LINE),null));}
    public static final class Glyph extends Drawable {
        private final Paint p=new Paint(3);private final String name;private final int size;
        public Glyph(Context c,String name,int color){this.name=name;size=dp(c,22);p.setColor(color);p.setStrokeWidth(1.8f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
        @Override public int getIntrinsicWidth(){return size;}@Override public int getIntrinsicHeight(){return size;}
        @Override public void draw(Canvas c){c.save();Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/24f,b.height()/24f);
            if("camera".equals(name)){c.drawRoundRect(3,6,21,20,3,3,p);c.drawCircle(12,13,4,p);c.drawLine(8,6,9,3,p);c.drawLine(9,3,15,3,p);c.drawLine(15,3,16,6,p);}
            else if("check".equals(name)){c.drawLine(5,12,10,17,p);c.drawLine(10,17,20,6,p);}
            else if("back".equals(name)){c.drawLine(19,12,5,12,p);c.drawLine(5,12,11,6,p);c.drawLine(5,12,11,18,p);}
            else if("doc".equals(name)){c.drawRoundRect(5,3,19,21,2,2,p);c.drawLine(8,9,16,9,p);c.drawLine(8,13,16,13,p);c.drawLine(8,17,13,17,p);}
            else if("grid".equals(name)){for(int x:new int[]{4,14})for(int y:new int[]{4,14})c.drawRoundRect(x,y,x+6,y+6,1,1,p);}
            else{c.drawLine(5,12,19,12,p);c.drawLine(14,7,19,12,p);c.drawLine(14,17,19,12,p);}c.restore();}
        @Override public void setAlpha(int a){p.setAlpha(a);}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    public static final class GradeRing extends View {
        private final float grade;private final Paint paint=new Paint(3);private final String gradeLabel;private final Typeface font=Typeface.create("sans-serif-medium",Typeface.BOLD);
        public GradeRing(Context c){this(c,0);}
        public GradeRing(Context c,double grade){super(c);this.grade=(float)grade;this.gradeLabel=String.format(new Locale("pt","BR"),"%.1f",grade);setContentDescription(String.format(new Locale("pt","BR"),"Nota %.1f de 10",grade));setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override protected void onDraw(Canvas c){float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.min(getWidth(),getHeight())/2f-dp(getContext(),10);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(getContext(),10));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(0xffe8edf8);c.drawCircle(cx,cy,r,paint);paint.setColor(BLUE);c.drawArc(cx-r,cy-r,cx+r,cy+r,-90,grade*36,false,paint);paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTypeface(font);paint.setTextSize(dp(getContext(),42));paint.setColor(INK);c.drawText(gradeLabel,cx,cy+dp(getContext(),4),paint);paint.setTextSize(dp(getContext(),10));paint.setColor(MUTED);c.drawText("NOTA / 10",cx,cy+dp(getContext(),25),paint);}
    }
    public static final class CaptureGuide extends View {
        private final Paint p=new Paint(3);
        public CaptureGuide(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setClickable(false);}
        @Override protected void onDraw(Canvas c){float h=Math.min(getHeight()*.84f,getWidth()*1.22f),w=h*2/3,left=(getWidth()-w)/2,top=(getHeight()-h)/2,k=dp(getContext(),24);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(getContext(),3));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xff8ee5cf);for(int i=0;i<4;i++){float x=i%2==0?left:left+w,y=i<2?top:top+h;c.drawLine(x,y,x+(i%2==0?k:-k),y,p);c.drawLine(x,y,x,y+(i<2?k:-k),p);}}
    }
}
