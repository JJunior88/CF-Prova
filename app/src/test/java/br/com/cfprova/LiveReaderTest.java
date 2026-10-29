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
    private byte[] place(byte[] card,int sourceWidth,int sourceHeight,int width,int height,double scale,int left,int top){
        byte[] page=new byte[width*height];Arrays.fill(page,(byte)250);
        for(int y=0;y<sourceHeight*scale;y++)for(int x=0;x<sourceWidth*scale;x++){
            int dx=x+left,dy=y+top;if(dx>=0&&dx<width&&dy>=0&&dy<height)page[dy*width+dx]=card[Math.min(sourceHeight-1,(int)(y/scale))*sourceWidth+Math.min(sourceWidth-1,(int)(x/scale))];
        }return page;
    }
    @Test public void readsQuarterPageCardInUpperLeftOfWholeA4Photo(){
        int[] a={0,1,2,3,4};byte[] page=place(card(5,a,0,true,250,20),800,1100,1200,1700,.75,0,0);
        LiveReader.Reading r=new LiveReader().read(page,1200,1700,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);
    }
    @Test public void readsOffCenterCard(){
        int[] a={4,3,2,1,0};byte[] page=place(card(5,a,0,true,250,20),800,1100,1500,1800,.85,700,700);
        LiveReader.Reading r=new LiveReader().read(page,1500,1800,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);
    }
    private byte[] rotated(byte[] pixels,int width,int height,int turns){
        if(turns==0)return pixels;
        byte[] next=new byte[pixels.length];for(int y=0;y<height;y++)for(int x=0;x<width;x++)next[x*height+height-1-y]=pixels[y*width+x];
        return rotated(next,height,width,turns-1);
    }
    @Test public void readsUpsideDownAndLandscapeCards(){int[] a={0,1,2,3,4};byte[] p=card(5,a,0,true,250,20);for(int turns=1;turns<=3;turns++){LiveReader.Reading r=new LiveReader().read(rotated(p,800,1100,turns),turns%2==1?1100:800,turns%2==1?800:1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}}
    @Test public void readsCardWithUnevenLighting(){int[] a={0,1,2,3,4};byte[] p=card(5,a,0,true,250,20);for(int y=0;y<1100;y++)for(int x=0;x<800;x++)p[y*800+x]=(byte)(int)((p[y*800+x]&255)*(.5+.5*x/800.0));LiveReader.Reading r=new LiveReader().read(p,800,1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void rejectsTwoCardsRatherThanChoosingAnArbitraryStudent(){int[] a={0,1,2,3,4};byte[] card=card(5,a,0,true,250,20),page=place(card,800,1100,2400,1700,.75,0,0),right=place(card,800,1100,2400,1700,.75,1200,0);for(int i=0;i<page.length;i++)page[i]=(byte)Math.min(page[i]&255,right[i]&255);LiveReader.Reading r=new LiveReader().read(page,2400,1700,5);assertNull(r.answers);assertTrue(r.hint.contains("mais de um"));}
    @Test public void missingMarkerCannotProduceGrade(){int[] a={0,1,2,3,4};byte[] p=card(5,a,0,true,250,20);for(int y=945;y<=975;y++)for(int x=645;x<=675;x++)p[y*800+x]=(byte)250;assertNull(new LiveReader().read(p,800,1100,5).answers);}
    @Test public void capturedPhotoAutomaticallyCountsCorrectAnswers(){
        int[] key={0,1,2,3,4};
        LiveReader.Reading reading=new LiveReader().read(card(5,new int[]{0,1,2,3,0},0,true,250,20),800,1100,5);
        assertNotNull(reading.hint,reading.answers);
        assertEquals(4,Grading.correct(key,reading.answers));
        assertEquals(8.0,Grading.grade(key,reading.answers),.001);
        reading.answers[4]=4; // The teacher corrects this answer while checking the photo.
        assertEquals(5,Grading.correct(key,reading.answers));
        assertEquals(10.0,Grading.grade(key,reading.answers),.001);
    }
    @Test public void readsPrintedLayoutIncludingBlanks(){int[] a={0,1,-1,3,4};LiveReader.Reading r=new LiveReader().read(card(5,a,0,true,250,20),800,1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void readsSkewedFortyQuestionCard(){int[] a=new int[40];for(int i=0;i<40;i++)a[i]=i%5;LiveReader.Reading r=new LiveReader().read(card(40,a,.06,true,220,25),800,1100,40);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void readsPerspectiveDistortion(){int[] a={4,3,2,1,0};LiveReader.Reading r=new LiveReader().read(card(5,a,.04,true,250,20,.0001),800,1100,5);assertNotNull(r.hint,r.answers);assertArrayEquals(a,r.answers);}
    @Test public void rejectsWrongQuestionCount(){int[] a={0,1,2,3,4};assertNull(new LiveReader().read(card(5,a,0,true,250,20),800,1100,10).answers);}
    @Test public void rejectsLegacyCardWithoutSignature(){int[] a={0,1,2,3,4};assertNull(new LiveReader().read(card(5,a,0,false,250,20),800,1100,5).answers);}
    @Test public void doubleMarkDoesNotProduceGrade(){int[] a={0,1,-2,3,4};LiveReader.Reading r=new LiveReader().read(card(5,a,0,true,250,20),800,1100,5);assertNull(r.answers);assertTrue(r.hint.contains("questão 3"));}
    @Test public void missingSheetDoesNotProduceGrade(){byte[] p=new byte[800*1100];Arrays.fill(p,(byte)250);assertNull(new LiveReader().read(p,800,1100,5).answers);}
    @Test public void darkFrameDoesNotProduceGrade(){assertNull(new LiveReader().read(new byte[800*1100],800,1100,5).answers);}
}
