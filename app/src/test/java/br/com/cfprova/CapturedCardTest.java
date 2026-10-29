package br.com.cfprova;

import org.junit.Test;
import java.io.InputStream;
import static org.junit.Assert.*;

public class CapturedCardTest {
    @Test public void readsRealExportRenderedOnWholeA4WithJpegCompressionAndLightingGradient()throws Exception{check("/quarter-card-20-jpeg.jpg");}
    @Test public void readsBottomRightCardWithoutIdentification()throws Exception{check("/lower-right-card-20-jpeg.jpg");}
    private void check(String resource)throws Exception{
        try(InputStream in=getClass().getResourceAsStream(resource)){
            assertNotNull(in);
            // Host-JVM decoder: reflection keeps java.desktop APIs out of Android's compile stubs.
            Object image=Class.forName("javax.imageio.ImageIO").getMethod("read",InputStream.class).invoke(null,in);
            Class<?> type=Class.forName("java.awt.image.BufferedImage");
            int width=(Integer)type.getMethod("getWidth").invoke(image),height=(Integer)type.getMethod("getHeight").invoke(image);
            int[] colors=(int[])type.getMethod("getRGB",int.class,int.class,int.class,int.class,int[].class,int.class,int.class).invoke(image,0,0,width,height,null,0,width);
            byte[] pixels=new byte[colors.length];for(int i=0;i<colors.length;i++){int c=colors[i];pixels[i]=(byte)(int)(.299*((c>>16)&255)+.587*((c>>8)&255)+.114*(c&255));}
            LiveReader.Reading reading=new LiveReader().read(pixels,width,height,20);assertNotNull(reading.hint,reading.answers);
            int[] expected=new int[20],key=new int[20];for(int i=0;i<20;i++){key[i]=i%5;expected[i]=i%4==0?-1:i%5;}
            assertArrayEquals(expected,reading.answers);assertEquals(15,Grading.correct(key,reading.answers));assertEquals(7.5,Grading.grade(key,reading.answers),.001);
        }
    }
}
