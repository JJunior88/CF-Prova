package br.com.cfprova;

import org.junit.Test;
import java.io.InputStream;
import static org.junit.Assert.*;

public class CapturedCardTest {
    @Test public void readsRealExportRenderedOnWholeA4WithJpegCompressionAndLightingGradient()throws Exception{check("/quarter-card-20-jpeg.jpg");}
    @Test public void readsBottomRightCardWithoutIdentification()throws Exception{check("/lower-right-card-20-jpeg.jpg");}
    @Test public void tableLettersAreNotStudentMarks()throws Exception{
        int[] expected=new int[20];java.util.Arrays.fill(expected,-1);assertArrayEquals(expected,readTable("/table-v3-blank.jpg").answers);
    }
    @Test public void tableReadsFilledBubblesAndCountsCorrectAnswers()throws Exception{
        LiveReader.Reading r=readTable("/table-v3-filled.jpg");assertNotNull(r.hint,r.answers);int[] key=new int[20],expected=new int[20];
        for(int i=0;i<20;i++){key[i]=i%5;expected[i]=i%4==0?-1:i%5;}assertArrayEquals(expected,r.answers);assertEquals(15,Grading.correct(key,r.answers));
    }
    @Test public void tableDoubleMarkIsNotGraded()throws Exception{assertNull(readTable("/table-v3-double.jpg").answers);}
    @Test public void readsReducedSoftPhotoThatPreviouslyFailedRingCheck()throws Exception{
        LiveReader.Reading r=readTable("/table-v3-soft-filled.jpg");assertNotNull(r.hint,r.answers);int[] expected=new int[20];for(int i=0;i<20;i++)expected[i]=i%4==0?-1:i%5;assertArrayEquals(expected,r.answers);
    }
    @Test public void blurredLettersAloneRemainBlank()throws Exception{
        int[] expected=new int[20];java.util.Arrays.fill(expected,-1);assertArrayEquals(expected,readTable("/table-v3-soft-blank.jpg").answers);
    }
    @Test public void blurredDoubleMarkStillBlocksGrade()throws Exception{assertNull(readTable("/table-v3-soft-double.jpg").answers);}
    @Test public void printedLettersWithoutCircleOutlinesCannotProduceGrade()throws Exception{assertNull(readTable("/table-v3-no-rings.jpg").answers);}
    @Test public void readsTableUnderUnevenLighting()throws Exception{
        LiveReader.Reading r=readTable("/table-v3-shadow.jpg");assertNotNull(r.hint,r.answers);int[] expected=new int[20];for(int i=0;i<20;i++)expected[i]=i%4==0?-1:i%5;assertArrayEquals(expected,r.answers);
    }
    @Test public void excessiveBlurStillCannotProduceGrade()throws Exception{assertNull(readTable("/table-v3-unreadable.jpg").answers);}
    @Test public void rowBordersRecoverSlightlyCurvedFilledCard()throws Exception{
        LiveReader.Reading r=readTable("/table-v3-curved-filled.jpg");assertNotNull(r.hint,r.answers);int[] expected=new int[20];for(int i=0;i<20;i++)expected[i]=i%4==0?-1:i%5;assertArrayEquals(expected,r.answers);
    }
    @Test public void curvedUnfilledLettersRemainBlank()throws Exception{
        int[] expected=new int[20];java.util.Arrays.fill(expected,-1);assertArrayEquals(expected,readTable("/table-v3-curved-blank.jpg").answers);
    }
    @Test public void curvedDoubleMarkIsStillRejected()throws Exception{assertNull(readTable("/table-v3-curved-double.jpg").answers);}
    private LiveReader.Reading readTable(String resource)throws Exception{
        try(InputStream in=getClass().getResourceAsStream(resource)){
            Object image=Class.forName("javax.imageio.ImageIO").getMethod("read",InputStream.class).invoke(null,in);Class<?> type=Class.forName("java.awt.image.BufferedImage");
            int w=(Integer)type.getMethod("getWidth").invoke(image),h=(Integer)type.getMethod("getHeight").invoke(image);
            int[] colors=(int[])type.getMethod("getRGB",int.class,int.class,int.class,int.class,int[].class,int.class,int.class).invoke(image,0,0,w,h,null,0,w);
            byte[] pixels=new byte[colors.length];for(int i=0;i<colors.length;i++){int c=colors[i];pixels[i]=(byte)(int)(.299*((c>>16)&255)+.587*((c>>8)&255)+.114*(c&255));}
            return new LiveReader().read(pixels,w,h,20);
        }
    }
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
