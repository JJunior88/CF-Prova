package br.com.cfprova;

import android.graphics.*;

/** Answer table with letters inside circles and optical alignment markers. */
public final class CardDrawing {
    private CardDrawing() {}
    public static void draw(Canvas c,int count){draw(c,count,5);}
    public static void draw(Canvas c,int count,int alternatives){
        if(count<5||count>40||(alternatives!=4&&alternatives!=5))throw new IllegalArgumentException();
        drawTable(c,count,alternatives);
    }
    private static void drawTable(Canvas c,int count,int alternatives){
        c.drawColor(Color.WHITE);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);
        for(int x:new int[]{40,560})for(int y:new int[]{40,860}){int r=x==40&&y==40?14:10;c.drawRect(x-r,y-r,x+r,y+r,p);}
        for(int i=0;i<14;i++)if(LiveReader.tableCodeBit(i,count,alternatives))c.drawRect(97+i*24,42,103+i*24,48,p);
        float step=(float)(610.0/(count-1)),top=195-step/2,bottom=805+step/2,r=(float)LiveReader.tableRadius(count);
        p.setStrokeWidth(1.2f);c.drawLine(70,top,70,bottom,p);for(int j=0;j<=alternatives;j++){float x=130+j*400f/alternatives;c.drawLine(x,top,x,bottom,p);}
        for(int i=0;i<=count;i++)c.drawLine(70,top+i*step,530,top+i*step,p);
        p.setTextAlign(Paint.Align.CENTER);
        for(int i=0;i<count;i++){
            float y=(float)LiveReader.rowY(i,count);p.setStyle(Paint.Style.FILL);p.setTextSize(Math.min(18,step*.54f));Paint.FontMetrics fm=p.getFontMetrics();c.drawText(String.valueOf(i+1),100,y-(fm.ascent+fm.descent)/2,p);
            for(int j=0;j<alternatives;j++){
                float x=(float)LiveReader.tableX(j,alternatives);p.setStyle(Paint.Style.STROKE);c.drawCircle(x,y,r,p);
                p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(r*.95f);fm=p.getFontMetrics();c.drawText(""+(char)('A'+j),x,y-(fm.ascent+fm.descent)/2,p);
            }
        }
    }

}
