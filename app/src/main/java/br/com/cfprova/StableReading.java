package br.com.cfprova;
import java.util.Arrays;
/** Never reuse a consensus across interruptions or a different answer sheet. */
public final class StableReading {
    private int[] previous;
    private int frames;
    private long since,last;
    public int[] accept(int[] answers,long now){
        if(answers==null){reset();return null;}
        if(previous==null||!Arrays.equals(previous,answers)||now-last>700||now<last){previous=answers.clone();frames=1;since=now;}else frames++;
        last=now;
        return frames>=4&&now-since>=900?previous.clone():null;
    }
    public void reset(){previous=null;frames=0;since=0;last=0;}
}
