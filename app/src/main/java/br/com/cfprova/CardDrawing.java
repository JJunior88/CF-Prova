package br.com.cfprova;

import android.graphics.*;
import java.util.Locale;

/** The optical geometry stays in v2 coordinates, regardless of printed size. */
public final class CardDrawing {
    private CardDrawing() {}
    public static void draw(Canvas c,int count){
        Paint background=new Paint();background.setColor(Color.WHITE);c.drawRect(0,0,600,900,background);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);
        for(int x:new int[]{40,560})for(int y:new int[]{40,860}){int r=x==40&&y==40?14:10;c.drawRect(x-r,y-r,x+r,y+r,p);}
        for(int i=0;i<14;i++)if(LiveReader.codeBit(i,count))c.drawRect(97+i*24,42,103+i*24,48,p);
        p.setTextSize(23);c.drawText("CARTÃO-RESPOSTA",70,90,p);
        p.setTextSize(17);c.drawText("Aluno: ______________________________",70,120,p);
        c.drawText("Turma: __________  Data: ____/____/______",70,145,p);
        p.setTextSize(15);for(int j=0;j<5;j++)c.drawText(""+(char)('A'+j),215+j*65,175,p);
        for(int i=0;i<count;i++){
            float y=(float)LiveReader.rowY(i,count);p.setStyle(Paint.Style.FILL);p.setTextSize(16);c.drawText(String.format(Locale.ROOT,"%02d",i+1),150,y+5,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);for(int j=0;j<5;j++)c.drawCircle(220+j*65,y,6,p);
        }
        p.setStyle(Paint.Style.FILL);p.setTextSize(12);c.drawText(count+" questões • A–E • Preencha uma bolha por questão.",70,832,p);
        p.setTextSize(10);c.drawText("Modelo v2 • Mantenha os quatro quadrados e o código intactos.",70,847,p);
    }
}
