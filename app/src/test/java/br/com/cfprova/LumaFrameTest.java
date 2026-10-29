package br.com.cfprova;
import org.junit.Test;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;

public class LumaFrameTest {
    @Test public void respectsBufferPositionRowPaddingAndPixelStride(){ByteBuffer b=ByteBuffer.wrap(new byte[]{99,99,1,0,2,0,3,0,88,88,4,0,5,0,6});b.position(2);LumaFrame f=LumaFrame.from(b,3,2,8,2,0,1280);assertEquals(3,f.width);assertEquals(2,f.height);assertArrayEquals(new byte[]{1,2,3,4,5,6},f.pixels);assertEquals(2,b.position());}
    @Test public void rotatesAllPhoneOrientations(){byte[] p={1,2,3,4,5,6};assertArrayEquals(new byte[]{4,1,5,2,6,3},LumaFrame.from(ByteBuffer.wrap(p),3,2,3,1,90,1280).pixels);assertArrayEquals(new byte[]{6,5,4,3,2,1},LumaFrame.from(ByteBuffer.wrap(p),3,2,3,1,180,1280).pixels);assertArrayEquals(new byte[]{3,6,2,5,1,4},LumaFrame.from(ByteBuffer.wrap(p),3,2,3,1,270,1280).pixels);}
    @Test public void capsAnalysisSizeWithoutUnnecessaryHalving(){LumaFrame f=LumaFrame.from(ByteBuffer.allocate(1920*1080),1920,1080,1920,1,90,1280);assertEquals(720,f.width);assertEquals(1280,f.height);}
    @Test(expected=IllegalArgumentException.class)public void rejectsUnsupportedRotation(){LumaFrame.from(ByteBuffer.allocate(100),10,10,10,1,45,1280);}
}
