package br.com.cfprova;

import java.util.*;

/** Pure Java OMR for the version 2 printed card. Coordinates match the PDF. */
public final class LiveReader {
    public static final int SIGNATURE=0xD3;
    public static final class Reading {
        public final int[] answers;
        public final String hint;
        Reading(int[] answers,String hint){this.answers=answers;this.hint=hint;}
    }
    private static final class Marker {
        double x,y,area;
        Marker(double x,double y,double area){this.x=x;this.y=y;this.area=area;}
    }
    public static double rowY(int i,int count){return 195+i*610.0/(count-1);}
    public static boolean codeBit(int index,int count){return index<8?((SIGNATURE>>(7-index))&1)!=0:((count>>(13-index))&1)!=0;}
    public Reading read(byte[] pixels,int width,int height,int count){
        if(count<5||count>40||width<100||height<100||pixels.length!=width*height)throw new IllegalArgumentException();
        int[] hist=new int[256];for(byte p:pixels)hist[p&255]++;
        int sum=0,white=255;for(int i=0;i<256;i++){sum+=hist[i];if(sum>=pixels.length*.85){white=i;break;}}
        if(white<100)return new Reading(null,"Mais luz: evite sombras sobre o cartão");
        int threshold=(int)(white*.58);
        boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];
        List<Marker> markers=new ArrayList<>();int[] offsets={-1,1,-width,width};
        for(int y=1;y<height-1;y++)for(int x=1;x<width-1;x++){
            int start=y*width+x;if(seen[start]||(pixels[start]&255)>=threshold)continue;
            int head=0,tail=1;queue[0]=start;seen[start]=true;
            int minX=x,maxX=x,minY=y,maxY=y;long sx=0,sy=0;
            while(head<tail){int k=queue[head++],px=k%width,py=k/width;sx+=px;sy+=py;minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);
                for(int offset:offsets){int n=k+offset;if(n>=0&&n<pixels.length&&!seen[n]&&Math.abs(n%width-px)+Math.abs(n/width-py)==1&&(pixels[n]&255)<threshold){seen[n]=true;queue[tail++]=n;}}
            }
            int w=maxX-minX+1,h=maxY-minY+1;
            // Allow modest perspective/rotation; geometry and printed code validate the candidates.
            if(w>=7&&h>=7&&w<width*.13&&h<height*.13&&w/(double)h>.65&&w/(double)h<1.55&&tail/(double)(w*h)>.60)
                markers.add(new Marker(sx/(double)tail,sy/(double)tail,tail));
        }
        Marker[] corners=new Marker[4];
        for(Marker m:markers){int slot=m.y<height*.5?(m.x<width*.5?0:1):(m.x<width*.5?3:2);if(corners[slot]==null||m.area>corners[slot].area)corners[slot]=m;}
        for(Marker m:corners)if(m==null)return new Reading(null,"Enquadre os quatro quadrados do cartão v2");
        double area=0;for(int i=0;i<4;i++){Marker a=corners[i],b=corners[(i+1)%4];area+=a.x*b.y-b.x*a.y;}
        if(area<width*height*.25)return new Reading(null,"Aproxime a câmera e mantenha o cartão inteiro visível");
        double min=Double.MAX_VALUE,max=0;for(int i=1;i<4;i++){min=Math.min(min,corners[i].area);max=Math.max(max,corners[i].area);}
        if(max/min>2.2||corners[0].area<max*1.35||corners[0].area>min*3.2)return new Reading(null,"Mantenha o cartão v2 na vertical, com o quadrado maior no alto à esquerda");
        double[] transform=homography(corners);if(transform==null)return new Reading(null,"Mantenha o cartão plano e de frente para a câmera");
        // Verify printed signature AND question count to avoid grading unrelated or mismatched cards.
        for(int i=0;i<14;i++){
            double d=darkness(pixels,width,height,transform,100+24*i,45,2,threshold);
            if(d<0|| (codeBit(i,count)?d<.85:d>.15))return new Reading(null,"Use o cartão v2 desta prova ("+count+" questões), sem sombras");
        }
        int[] answers=new int[count];int rings=0;
        for(int i=0;i<count;i++){
            double[] dark=new double[5];
            for(int j=0;j<5;j++){
                double cx=220+j*65,cy=rowY(i,count);
                dark[j]=darkness(pixels,width,height,transform,cx,cy,3,threshold);
                if(dark[j]<0)return new Reading(null,"Enquadre a folha inteira");
                int border=0;for(int k=0;k<16;k++){double angle=k*Math.PI/8;int p=sample(pixels,width,height,transform,cx+6*Math.cos(angle),cy+6*Math.sin(angle));if(p>=0&&p<threshold)border++;}
                if(border>=7)rings++;
            }
            answers[i]=Grading.classify(dark);
            if(answers[i]==-2)return new Reading(null,"Marcação fraca ou dupla na questão "+(i+1)+". Corrija ou melhore o enquadramento");
        }
        if(rings<count*5*.8)return new Reading(null,"Aproxime e aguarde o foco: as bolhas precisam ficar nítidas");
        return new Reading(answers,"Lendo respostas… mantenha o celular parado");
    }
    private static int sample(byte[] p,int w,int h,double[] m,double x,double y){
        double z=m[6]*x+m[7]*y+1;if(Math.abs(z)<1e-8)return -1;
        int xx=(int)Math.round((m[0]*x+m[1]*y+m[2])/z),yy=(int)Math.round((m[3]*x+m[4]*y+m[5])/z);
        return xx<0||xx>=w||yy<0||yy>=h?-1:p[yy*w+xx]&255;
    }
    private static double darkness(byte[] p,int w,int h,double[] m,double x,double y,int radius,int threshold){
        int total=0,dark=0;for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++)if(dx*dx+dy*dy<=radius*radius){int v=sample(p,w,h,m,x+dx,y+dy);if(v<0)return -1;total++;if(v<threshold)dark++;}return dark/(double)total;
    }
    private static double[] homography(Marker[] corners){
        double[][] a=new double[8][9];double[] xs={40,560,560,40},ys={40,40,860,860};
        for(int i=0;i<4;i++){double x=xs[i],y=ys[i],u=corners[i].x,v=corners[i].y;a[2*i]=new double[]{x,y,1,0,0,0,-u*x,-u*y,u};a[2*i+1]=new double[]{0,0,0,x,y,1,-v*x,-v*y,v};}
        for(int c=0;c<8;c++){int pivot=c;for(int r=c+1;r<8;r++)if(Math.abs(a[r][c])>Math.abs(a[pivot][c]))pivot=r;double[] tmp=a[c];a[c]=a[pivot];a[pivot]=tmp;if(Math.abs(a[c][c])<1e-9)return null;double d=a[c][c];for(int j=c;j<9;j++)a[c][j]/=d;for(int r=0;r<8;r++)if(r!=c){d=a[r][c];for(int j=c;j<9;j++)a[r][j]-=d*a[c][j];}}
        double[] m=new double[8];for(int i=0;i<8;i++)m[i]=a[i][8];return m;
    }
}
