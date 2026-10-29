package br.com.cfprova;

import java.util.*;

/** Locates the app's v2 answer card anywhere in a frame, then rectifies its geometry. */
public final class LiveReader {
    public static final int SIGNATURE=0xD3;
    public static final class Reading {
        public final int[] answers;
        public final String hint;
        Reading(int[] answers,String hint){this.answers=answers;this.hint=hint;}
    }
    private static final class Marker {
        final double x,y,area;
        Marker(double x,double y,double area){this.x=x;this.y=y;this.area=area;}
    }
    private static final class Card {
        final Marker[] points;final double[] map;final int count;
        Card(Marker[] points,double[] map,int count){this.points=points;this.map=map;this.count=count;}
    }
    public static double rowY(int i,int count){return 195+i*610.0/(count-1);}
    public static boolean codeBit(int index,int count){return index<8?((SIGNATURE>>(7-index))&1)!=0:((count>>(13-index))&1)!=0;}
    public Reading read(byte[] pixels,int width,int height,int count){
        if(count<5||count>40||width<100||height<100||pixels.length!=width*height)throw new IllegalArgumentException();
        int[] hist=new int[256];for(byte p:pixels)hist[p&255]++;
        int sum=0,white=255;for(int i=0;i<256;i++){sum+=hist[i];if(sum>=pixels.length*.85){white=i;break;}}
        if(white<65)return new Reading(null,"Pouca luz. Ative o flash/luz e mantenha o cartão-resposta sem sombras.");
        List<Marker> markers=findMarkers(pixels,width,height,white);
        if(markers.size()<4)return new Reading(null,"Mostre os quatro quadrados do cartão-resposta. Aproxime e toque para focar.");
        // Bound the search; unrelated background objects never become a valid card without its code.
        markers.sort((a,b)->Double.compare(b.area,a.area));if(markers.size()>48)markers=new ArrayList<>(markers.subList(0,48));
        List<Card> cards=new ArrayList<>();
        for(Marker a:markers)for(Marker b:markers){
            if(a==b||!relativeSize(a,b))continue;
            double ab=distance(a,b),unit=(Math.sqrt(a.area)+Math.sqrt(b.area))/2;
            if(ab<90||ab/unit<11||ab/unit>36)continue;
            for(Marker d:markers){
                if(d==a||d==b||!relativeSize(a,d)||Math.max(b.area,d.area)/Math.min(b.area,d.area)>2.5)continue;
                double ad=distance(a,d),cross=cross(a,b,d),cos=((b.x-a.x)*(d.x-a.x)+(b.y-a.y)*(d.y-a.y))/(ab*ad);
                if(ad<140||ab/ad<.37||ab/ad>1.02||cross<=0||Math.abs(cos)>.65)continue;
                double predictedX=b.x+d.x-a.x,predictedY=b.y+d.y-a.y;
                for(Marker c:markers){
                    if(c==a||c==b||c==d||!relativeSize(a,c)||Math.max(c.area,b.area)/Math.min(c.area,b.area)>2.5)continue;
                    if(Math.hypot(c.x-predictedX,c.y-predictedY)>Math.hypot(ab,ad)*.28)continue;
                    Marker[] points={a,b,c,d};if(!convex(points)||distance(b,c)/ad<.5||distance(b,c)/ad>1.8||distance(c,d)/ab<.5||distance(c,d)/ab>1.8)continue;
                    double[] map=homography(points);if(map==null)continue;
                    int decoded=decode(pixels,width,height,map);if(decoded<0)continue;
                    boolean duplicate=false;
                    for(Card old:cards)if(distance(old.points[0],a)<Math.sqrt(a.area)*2){duplicate=true;break;}
                    if(!duplicate)cards.add(new Card(points,map,decoded));
                    if(cards.size()>1)return new Reading(null,"Há mais de um cartão-resposta na imagem. Enquadre apenas um por vez.");
                }
            }
        }
        if(cards.isEmpty())return new Reading(null,"Código ou marcadores sem nitidez. Use o cartão-resposta v2 do app, aproxime e toque para focar; ative a luz se necessário.");
        Card card=cards.get(0);
        if(card.count!=count)return new Reading(null,"O cartão-resposta tem "+card.count+" questões, mas a prova está configurada com "+count+". Use o modelo correspondente.");
        int[] answers=new int[count];int rings=0;
        for(int i=0;i<count;i++){
            double[] dark=new double[5];
            for(int j=0;j<5;j++){
                double x=220+j*65,y=rowY(i,count),paper=paper(pixels,width,height,card.map,x,y,10),ink=markerInk(pixels,width,height,card.map);
                if(paper-ink<35)return new Reading(null,"Contraste insuficiente. Ative a luz, evite reflexos e refaça a leitura.");
                dark[j]=darkness(pixels,width,height,card.map,x,y,3,paper,ink);
                if(dark[j]<0)return new Reading(null,"Mantenha os quatro marcadores e todas as respostas visíveis.");
                // The thin printed outline can shift by a pixel after JPEG/resize. Search a narrow radial band.
                int border=0;
                for(int k=0;k<16;k++){
                    double angle=k*Math.PI/8,best=255;
                    for(double radius:new double[]{5.2,6,6.8}){double value=sample(pixels,width,height,card.map,x+radius*Math.cos(angle),y+radius*Math.sin(angle));if(value>=0)best=Math.min(best,value);}
                    if(best<paper-(paper-ink)*.35)border++;
                }
                if(border>=8)rings++;
            }
            answers[i]=Grading.classify(dark);
            if(answers[i]==-2)return new Reading(null,"Confira o preenchimento da questão "+(i+1)+": marca fraca, dupla ou imagem desfocada. Corrija e refaça.");
        }
        if(rings<count*5*.75)return new Reading(null,"Bolhas sem nitidez. Aproxime do cartão-resposta, toque para focar e evite reflexos.");
        return new Reading(answers,"Cartão-resposta reconhecido.");
    }
    private static List<Marker> findMarkers(byte[] pixels,int width,int height,int white){
        // Local thresholds preserve dark print under uneven light instead of turning a whole shadow black.
        int stride=width+1;int[] integral=new int[(width+1)*(height+1)];
        for(int y=0;y<height;y++){int sum=0;for(int x=0;x<width;x++){sum+=pixels[y*width+x]&255;integral[(y+1)*stride+x+1]=integral[y*stride+x+1]+sum;}}
        boolean[] dark=new boolean[pixels.length];int radius=Math.max(12,Math.min(width,height)/35);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            int l=Math.max(0,x-radius),r=Math.min(width,x+radius+1),t=Math.max(0,y-radius),b=Math.min(height,y+radius+1);
            double local=(integral[b*stride+r]-integral[b*stride+l]-integral[t*stride+r]+integral[t*stride+l])/(double)((r-l)*(b-t));
            dark[y*width+x]=(pixels[y*width+x]&255)<Math.min(white*.72,local-18);
        }
        boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];int[] offsets={-1,1,-width,width};List<Marker> markers=new ArrayList<>();
        for(int y=1;y<height-1;y++)for(int x=1;x<width-1;x++){
            int start=y*width+x;if(seen[start]||!dark[start])continue;
            int head=0,tail=1;queue[0]=start;seen[start]=true;int minX=x,maxX=x,minY=y,maxY=y;long sx=0,sy=0;
            while(head<tail){int k=queue[head++],px=k%width,py=k/width;sx+=px;sy+=py;minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);
                for(int offset:offsets){int n=k+offset;if(n>=0&&n<pixels.length&&!seen[n]&&dark[n]&&Math.abs(n%width-px)+Math.abs(n/width-py)==1){seen[n]=true;queue[tail++]=n;}}
            }
            int w=maxX-minX+1,h=maxY-minY+1;
            if(w>=5&&h>=5&&w<width*.2&&h<height*.2&&w/(double)h>.48&&w/(double)h<2.08&&tail/(double)(w*h)>.47&&minX>0&&minY>0&&maxX<width-1&&maxY<height-1)
                markers.add(new Marker(sx/(double)tail,sy/(double)tail,tail));
        }
        return markers;
    }
    private static boolean relativeSize(Marker a,Marker b){return a.area/b.area>=1.22&&a.area/b.area<=4.5;}
    private static double distance(Marker a,Marker b){return Math.hypot(a.x-b.x,a.y-b.y);}
    private static double cross(Marker a,Marker b,Marker c){return (b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x);}
    private static boolean convex(Marker[] p){for(int i=0;i<4;i++)if(cross(p[i],p[(i+1)%4],p[(i+2)%4])<=0)return false;return true;}
    private static int decode(byte[] p,int w,int h,double[] map){
        int signature=0,count=0;double ink=markerInk(p,w,h,map);
        for(int i=0;i<14;i++){
            double x=100+24*i,white=paper(p,w,h,map,x,45,8);if(white-ink<35)return -1;
            double value=darkness(p,w,h,map,x,45,1,white,ink);int bit=value>.65?1:value<.25?0:-1;if(bit<0)return -1;
            if(i<8)signature=(signature<<1)|bit;else count=(count<<1)|bit;
        }
        return signature==SIGNATURE&&count>=5&&count<=40?count:-1;
    }
    private static double markerInk(byte[] p,int w,int h,double[] m){double[] values={sample(p,w,h,m,40,40),sample(p,w,h,m,560,40),sample(p,w,h,m,560,860),sample(p,w,h,m,40,860)};Arrays.sort(values);return (values[1]+values[2])/2;}
    private static double paper(byte[] p,int w,int h,double[] m,double x,double y,double radius){double[] values=new double[16];for(int i=0;i<16;i++){double a=i*Math.PI/8;values[i]=sample(p,w,h,m,x+radius*Math.cos(a),y+radius*Math.sin(a));}Arrays.sort(values);return (values[10]+values[11])/2;}
    private static double sample(byte[] p,int w,int h,double[] m,double x,double y){
        double z=m[6]*x+m[7]*y+1;if(Math.abs(z)<1e-8)return -1;
        double xx=(m[0]*x+m[1]*y+m[2])/z,yy=(m[3]*x+m[4]*y+m[5])/z;
        int ix=(int)Math.floor(xx),iy=(int)Math.floor(yy);if(ix<0||ix>=w-1||iy<0||iy>=h-1)return -1;
        double dx=xx-ix,dy=yy-iy;return (1-dy)*((1-dx)*(p[iy*w+ix]&255)+dx*(p[iy*w+ix+1]&255))+dy*((1-dx)*(p[(iy+1)*w+ix]&255)+dx*(p[(iy+1)*w+ix+1]&255));
    }
    private static double darkness(byte[] p,int w,int h,double[] m,double x,double y,int radius,double paper,double ink){
        double total=0,dark=0;for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++)if(dx*dx+dy*dy<=radius*radius){double value=sample(p,w,h,m,x+dx,y+dy);if(value<0)return -1;total++;dark+=Math.max(0,Math.min(1,(paper-value)/(paper-ink)));}return dark/total;
    }
    private static double[] homography(Marker[] corners){
        double[][] a=new double[8][9];double[] xs={40,560,560,40},ys={40,40,860,860};
        for(int i=0;i<4;i++){double x=xs[i],y=ys[i],u=corners[i].x,v=corners[i].y;a[2*i]=new double[]{x,y,1,0,0,0,-u*x,-u*y,u};a[2*i+1]=new double[]{0,0,0,x,y,1,-v*x,-v*y,v};}
        for(int c=0;c<8;c++){int pivot=c;for(int r=c+1;r<8;r++)if(Math.abs(a[r][c])>Math.abs(a[pivot][c]))pivot=r;double[] tmp=a[c];a[c]=a[pivot];a[pivot]=tmp;if(Math.abs(a[c][c])<1e-9)return null;double d=a[c][c];for(int j=c;j<9;j++)a[c][j]/=d;for(int r=0;r<8;r++)if(r!=c){d=a[r][c];for(int j=c;j<9;j++)a[r][j]-=d*a[c][j];}}
        double[] m=new double[8];for(int i=0;i<8;i++)m[i]=a[i][8];return m;
    }
}
