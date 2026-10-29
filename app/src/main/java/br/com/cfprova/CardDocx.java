package br.com.cfprova;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

/** One identification-free optical card, positioned relative to the A4 page. */
public final class CardDocx {
    public static final int PAGE_WIDTH=11906,PAGE_HEIGHT=16838,MARGIN=283;
    public static final long IMAGE_WIDTH_EMU=3132000,IMAGE_HEIGHT_EMU=4698000; // 87 x 130.5 mm
    public static final long LEFT_EMU=(PAGE_WIDTH-MARGIN)*635L-IMAGE_WIDTH_EMU;
    public static final long TOP_EMU=(PAGE_HEIGHT-MARGIN)*635L-IMAGE_HEIGHT_EMU;
    private CardDocx() {}
    private static void entry(ZipOutputStream zip,String name,String data)throws IOException{entry(zip,name,data.getBytes(StandardCharsets.UTF_8));}
    private static void entry(ZipOutputStream zip,String name,byte[] data)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(data);zip.closeEntry();}
    // The old exam argument is intentionally ignored: exported cards contain no identification.
    public static void write(OutputStream output,byte[] cardPng,String unusedExam)throws IOException{
        if(cardPng==null||cardPng.length==0)throw new IllegalArgumentException("Imagem do cartão necessária");
        String document="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            +"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><w:body>"
            +"<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"20\" w:lineRule=\"exact\"/></w:pPr><w:r><w:drawing>"
            +"<wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"0\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
            +"<wp:simplePos x=\"0\" y=\"0\"/><wp:positionH relativeFrom=\"page\"><wp:posOffset>"+LEFT_EMU+"</wp:posOffset></wp:positionH>"
            +"<wp:positionV relativeFrom=\"page\"><wp:posOffset>"+TOP_EMU+"</wp:posOffset></wp:positionV>"
            +"<wp:extent cx=\""+IMAGE_WIDTH_EMU+"\" cy=\""+IMAGE_HEIGHT_EMU+"\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Cartão-resposta\" descr=\"Grade de respostas com marcadores de leitura\"/>"
            +"<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
            +"<pic:pic><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"cartao.png\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"rIdCard\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
            +"<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\""+IMAGE_WIDTH_EMU+"\" cy=\""+IMAGE_HEIGHT_EMU+"\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
            +"</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></w:p>"
            +"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"283\" w:right=\"283\" w:bottom=\"283\" w:left=\"283\" w:header=\"0\" w:footer=\"0\"/></w:sectPr></w:body></w:document>";
        try(ZipOutputStream zip=new ZipOutputStream(output)){
            entry(zip,"[Content_Types].xml","<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Default Extension=\"png\" ContentType=\"image/png\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            entry(zip,"_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            entry(zip,"word/document.xml",document);
            entry(zip,"word/_rels/document.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rIdCard\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/cartao.png\"/></Relationships>");
            entry(zip,"word/media/cartao.png",cardPng);
        }
    }
}
