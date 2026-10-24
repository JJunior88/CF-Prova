package br.com.cfprova;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class TransientPhotoTest {
    @Test public void imageExistsOnlyDuringCurrentReview(){List<String> released=new ArrayList<>();TransientPhoto<String> p=new TransientPhoto<>(released::add);int token=p.begin();assertTrue(p.accept(token,"photo"));assertEquals("photo",p.get());p.clear();assertNull(p.get());assertEquals(Arrays.asList("photo"),released);}
    @Test public void leavingScreenRejectsAndReleasesDelayedCapture(){List<String> released=new ArrayList<>();TransientPhoto<String> p=new TransientPhoto<>(released::add);int token=p.begin();p.clear();assertFalse(p.accept(token,"late-photo"));assertNull(p.get());assertEquals(Arrays.asList("late-photo"),released);}
    @Test public void retakeDiscardsPreviousAndRejectsOlderCallback(){List<String> released=new ArrayList<>();TransientPhoto<String> p=new TransientPhoto<>(released::add);int old=p.begin();p.accept(old,"old-photo");int next=p.begin();assertNull(p.get());assertFalse(p.accept(old,"stale-photo"));assertTrue(p.accept(next,"new-photo"));assertEquals("new-photo",p.get());assertEquals(Arrays.asList("old-photo","stale-photo"),released);}
    @Test public void repeatedCleanupDoesNotReleaseImageTwice(){List<String> released=new ArrayList<>();TransientPhoto<String> p=new TransientPhoto<>(released::add);p.accept(p.begin(),"photo");p.clear();p.clear();assertEquals(1,released.size());}
}
