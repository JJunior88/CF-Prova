package br.com.cfprova;

import java.util.Arrays;

/** Live readings never trigger a capture until independent frames agree over time. */
public final class ReadingConsensus {
    private int[] previous;
    private int frames;
    private long first,last;
    public synchronized int[] accept(int[] answers,long time){
        if(answers==null){reset();return null;}
        if(previous==null||!Arrays.equals(previous,answers)||time<last||time-last>1500){previous=answers.clone();frames=1;first=time;}else frames++;
        last=time;return frames>=3&&time-first>=700?previous.clone():null;
    }
    public synchronized void reset(){previous=null;frames=0;first=0;last=0;}
}
