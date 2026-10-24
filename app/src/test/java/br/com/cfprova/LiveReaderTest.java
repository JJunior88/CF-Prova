package br.com.cfprova;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class LiveReaderTest {
    private byte[] card(int count,int[] answers,double shear,boolean signature,int paper,int ink){
        return card(count,answers,shear,signature,paper,ink,0);
    }
    private byte[] card(int count,int[] answers,double shear,boolean signature,int paper,int ink,double perspective){
        int width=800,height=1100;byte[] pixels=new byte[width*height];Arrays.fill(pixels,(byte)paper);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            double yy=(y+.5-100)/(1-perspective*(y+.5-100)),xx=(x+.5-100)*(1+perspective*yy)-shear*yy;boolean black=false;
            for(int mx:new int[]{40,560})for(int my:new int[]{40,860}){int r=mx==40&&my==40?14:10;if(Math.abs(xx-mx)<=r&&Math.abs(yy-my)<=r)black=true;}
            if(signature)for(int i=0;i<14;i++)if(LiveReader.codeBit(i,count)&&Math.abs(xx-(100+24*i))<=3&&Math.abs(yy-45)<=3)black=true;
            for(int i=0;i<count;i++){
                double dy=yy-LiveReader.rowY(i,count);if(Math.abs(dy)>7)continue;
                for(int j=0;j<5;j++){double dx=xx-(220+65*j),r=Math.sqrt(dx*dx+dy*dy);if(Math.abs(r-6)<=.55||answers[i]==j&&r<=5.5||answers[i]==-2&&j<2&&r<=5.5)black=true;}
            }
            if(black)pixels[y*width+x]=(byte)ink;
        }return pixels;
    }
    @Test public void liveCameraPipelineAutomaticallyCountsCorrectAnswers(){
        int[] key={0,1,2,3,4};
        LiveReader.Reading reading=new LiveReader().read(card(5,new int[]{0,1,2,3,0},0,true,250,20),800,1100,5);
        assertNotNull(reading.hint,reading.answers);
        StableReading stable=new StableReading();
        assertNull(stable.accept(reading.answers,0));
        assertNull(stable.accept(reading.answers,300));
        assertNull(stable.accept(reading.answers,600));
        int[] result=stable.accept(reading.answers,900);
        assertNotNull(result);
        assertEquals(4,Grading.correct(key,result));
        assertEquals(8.0,Grading.grade(key,result),.001);
    }
    @Test public void readsPrintedLayoutIncludingBlanks(){int[] a={0,1,-1,3,4};LiveReader.Reading r=new LiveReader().read(card(5,a,0,true,250,20),800,1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void readsSkewedFortyQuestionCard(){int[] a=new int[40];for(int i=0;i<40;i++)a[i]=i%5;LiveReader.Reading r=new LiveReader().read(card(40,a,.06,true,220,25),800,1100,40);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void readsPerspectiveDistortion(){int[] a={4,3,2,1,0};LiveReader.Reading r=new LiveReader().read(card(5,a,.04,true,250,20,.0001),800,1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void rejectsWrongQuestionCount(){int[] a={0,1,2,3,4};assertNull(new LiveReader().read(card(5,a,0,true,250,20),800,1100,10).answers);}
    @Test public void rejectsLegacyCardWithoutSignature(){int[] a={0,1,2,3,4};assertNull(new LiveReader().read(card(5,a,0,false,250,20),800,1100,5).answers);}
    @Test public void doubleMarkDoesNotProduceGrade(){int[] a={0,1,-2,3,4};LiveReader.Reading r=new LiveReader().read(card(5,a,0,true,250,20),800,1100,5);assertNull(r.answers);assertTrue(r.hint.contains("questão 3"));}
    @Test public void missingSheetDoesNotProduceGrade(){byte[] p=new byte[800*1100];Arrays.fill(p,(byte)250);assertNull(new LiveReader().read(p,800,1100,5).answers);}
    @Test public void darkFrameDoesNotProduceGrade(){assertNull(new LiveReader().read(new byte[800*1100],800,1100,5).answers);}
    @Test public void requiresTimeAndRepeatedAgreement(){StableReading s=new StableReading();int[] a={0,1,2};assertNull(s.accept(a,0));assertNull(s.accept(a,300));assertNull(s.accept(a,600));assertArrayEquals(a,s.accept(a,900));}
    @Test public void disagreementAndMissingFrameResetConsensus(){StableReading s=new StableReading();int[] a={0,1,2};s.accept(a,0);s.accept(a,300);s.accept(a,600);assertNull(s.accept(new int[]{1,1,2},900));assertNull(s.accept(a,1200));s.accept(null,1500);assertNull(s.accept(a,1800));}
    @Test public void pauseCannotReusePreviousConsensus(){StableReading s=new StableReading();int[] a={0,1,2};s.accept(a,0);s.accept(a,300);s.accept(a,600);assertNull(s.accept(a,2000));}
}
