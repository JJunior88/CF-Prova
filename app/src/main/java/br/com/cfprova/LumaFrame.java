package br.com.cfprova;

import java.nio.ByteBuffer;

/** Reads camera Y planes including row padding, nonzero buffer positions and device rotation. */
public final class LumaFrame {
    public final byte[] pixels;
    public final int width,height;
    private LumaFrame(byte[] pixels,int width,int height){this.pixels=pixels;this.width=width;this.height=height;}
    public static LumaFrame from(ByteBuffer data,int width,int height,int rowStride,int pixelStride,int rotation,int maxEdge){
        if(width<=0||height<=0||rowStride<=0||pixelStride<=0||maxEdge<=0||(rotation!=0&&rotation!=90&&rotation!=180&&rotation!=270))throw new IllegalArgumentException();
        double scale=Math.min(1,maxEdge/(double)Math.max(width,height));int sw=Math.max(1,(int)(width*scale)),sh=Math.max(1,(int)(height*scale));
        int outW=rotation==90||rotation==270?sh:sw,outH=rotation==90||rotation==270?sw:sh;byte[] result=new byte[outW*outH];int base=data.position();
        for(int y=0;y<sh;y++)for(int x=0;x<sw;x++){
            int sourceX=(int)(x/(double)sw*width),sourceY=(int)(y/(double)sh*height),dx=x,dy=y;
            if(rotation==90){dx=sh-1-y;dy=x;}else if(rotation==180){dx=sw-1-x;dy=sh-1-y;}else if(rotation==270){dx=y;dy=sw-1-x;}
            result[dy*outW+dx]=data.get(base+sourceY*rowStride+sourceX*pixelStride);
        }
        return new LumaFrame(result,outW,outH);
    }
}
