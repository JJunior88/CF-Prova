package br.com.cfprova;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReadingConsensusTest {
    @Test public void requiresThreeMatchingFramesAndTime(){ReadingConsensus c=new ReadingConsensus();int[] a={0,1,2};assertNull(c.accept(a,0));assertNull(c.accept(a,350));assertArrayEquals(a,c.accept(a,700));}
    @Test public void fastFramesCannotTriggerPrematureCapture(){ReadingConsensus c=new ReadingConsensus();int[] a={0,1,2};c.accept(a,0);c.accept(a,30);assertNull(c.accept(a,60));}
    @Test public void failureAndDifferentAnswersRestartConsensus(){ReadingConsensus c=new ReadingConsensus();int[] a={0,1,2};c.accept(a,0);c.accept(a,350);assertNull(c.accept(null,700));assertNull(c.accept(a,1050));assertNull(c.accept(new int[]{1,1,2},1400));assertNull(c.accept(a,1750));}
    @Test public void stopOrPauseCannotReusePreviousFrames(){ReadingConsensus c=new ReadingConsensus();int[] a={0,1,2};c.accept(a,0);c.accept(a,350);c.reset();assertNull(c.accept(a,700));assertNull(c.accept(a,3000));}
    @Test public void storedAnswersCannotBeChangedByCaller(){ReadingConsensus c=new ReadingConsensus();int[] a={0,1,2};c.accept(a,0);a[0]=4;assertNull(c.accept(a,350));assertNull(c.accept(a,700));int[] result=c.accept(a,1050);assertNotNull(result);result[0]=0;assertEquals(4,c.accept(a,1400)[0]);}
}
