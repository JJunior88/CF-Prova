package br.com.cfprova;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class CardDocxTest {
    private Map<String,byte[]> parts(String title)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] png={1,2,3,4};CardDocx.write(out,png,title);
        Map<String,byte[]> files=new HashMap<>();
        try(ZipInputStream in=new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))){ZipEntry e;while((e=in.getNextEntry())!=null){ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] buffer=new byte[1024];int n;while((n=in.read(buffer))!=-1)data.write(buffer,0,n);files.put(e.getName(),data.toByteArray());}}
        return files;
    }
    @Test public void wordHasOneBottomRightCardWithoutIdentification()throws Exception{
        Map<String,byte[]> files=parts("Prova A & B <2026>\u0001");
        assertArrayEquals(new byte[]{1,2,3,4},files.get("word/media/cartao.png"));
        DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);
        for(Map.Entry<String,byte[]> e:files.entrySet())if(e.getKey().endsWith(".xml")||e.getKey().endsWith(".rels"))f.newDocumentBuilder().parse(new ByteArrayInputStream(e.getValue()));
        Document doc=f.newDocumentBuilder().parse(new ByteArrayInputStream(files.get("word/document.xml")));
        String w="http://schemas.openxmlformats.org/wordprocessingml/2006/main";
        assertEquals(0,doc.getElementsByTagNameNS(w,"tc").getLength());
        assertEquals(0,doc.getElementsByTagNameNS(w,"tr").getLength());
        Element page=(Element)doc.getElementsByTagNameNS(w,"pgSz").item(0);
        assertEquals("11906",page.getAttributeNS(w,"w"));assertEquals("16838",page.getAttributeNS(w,"h"));
        assertFalse(doc.getDocumentElement().getTextContent().contains("Prova A"));
        assertEquals(0,doc.getElementsByTagNameNS(w,"t").getLength());
        String wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
        assertEquals(1,doc.getElementsByTagNameNS(wp,"anchor").getLength());
        assertEquals(0,doc.getElementsByTagNameNS(wp,"inline").getLength());
        NodeList offsets=doc.getElementsByTagNameNS(wp,"posOffset");
        assertEquals(CardDocx.LEFT_EMU,Long.parseLong(offsets.item(0).getTextContent()));
        assertEquals(CardDocx.TOP_EMU,Long.parseLong(offsets.item(1).getTextContent()));
        assertTrue(CardDocx.LEFT_EMU>=CardDocx.PAGE_WIDTH*635L/2);
        assertTrue(CardDocx.TOP_EMU>=CardDocx.PAGE_HEIGHT*635L/2);
        assertEquals((CardDocx.PAGE_WIDTH-CardDocx.MARGIN)*635L,CardDocx.LEFT_EMU+CardDocx.IMAGE_WIDTH_EMU);
        assertEquals((CardDocx.PAGE_HEIGHT-CardDocx.MARGIN)*635L,CardDocx.TOP_EMU+CardDocx.IMAGE_HEIGHT_EMU);
        assertEquals(1.5,CardDocx.IMAGE_HEIGHT_EMU/(double)CardDocx.IMAGE_WIDTH_EMU,.0001);
    }
    @Test(expected=IllegalArgumentException.class)public void missingCardImageIsRejected()throws Exception{CardDocx.write(new ByteArrayOutputStream(),new byte[0],"Prova");}
}
