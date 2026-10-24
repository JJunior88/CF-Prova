package br.com.cfprova;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

/** Standard Word OOXML: editable institutional headers, fixed-size optical artwork. */
public final class CardDocx {
    public static final int PAGE_WIDTH=11906,PAGE_HEIGHT=16838,MARGIN=283,CELL_WIDTH=5670,ROW_HEIGHT=8100;
    public static final long IMAGE_WIDTH_EMU=3132000,IMAGE_HEIGHT_EMU=4698000; // 87 x 130.5 mm
    private CardDocx() {}
    private static String xml(String s){
        StringBuilder out=new StringBuilder();
        for(int i=0;i<s.length();i++){char c=s.charAt(i);if(c>=32&&c!=0xfffe&&c!=0xffff)out.append(c);}
        return out.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    private static void entry(ZipOutputStream zip,String name,String data)throws IOException{entry(zip,name,data.getBytes(StandardCharsets.UTF_8));}
    private static void entry(ZipOutputStream zip,String name,byte[] data)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(data);zip.closeEntry();}
    private static String line(String text){return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"180\" w:lineRule=\"exact\"/><w:ind w:left=\"350\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:sz w:val=\"15\"/></w:rPr><w:t xml:space=\"preserve\">"+xml(text)+"</w:t></w:r></w:p>";}
    public static void write(OutputStream output,byte[] cardPng,String exam)throws IOException{
        if(cardPng==null||cardPng.length==0)throw new IllegalArgumentException("Imagem do cartão necessária");
        StringBuilder body=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><w:body><w:tbl><w:tblPr><w:tblW w:w=\"5670\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr><w:tblGrid><w:gridCol w:w=\"5670\"/></w:tblGrid>");
            body.append("<w:tr><w:trPr><w:cantSplit/><w:trHeight w:val=\"8100\" w:hRule=\"exact\"/></w:trPr>");
                int id=1;
                body.append("<w:tc><w:tcPr><w:tcW w:w=\"5670\" w:type=\"dxa\"/><w:vAlign w:val=\"top\"/></w:tcPr>");
                body.append(line("Instituição: __________________________________"));
                body.append(line("Professor(a): _________________________________"));
                body.append(line("Avaliação: "+(exam==null?"________________________":exam)));
                body.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"0\" w:after=\"0\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"2\"/></w:rPr><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"3132000\" cy=\"4698000\"/><wp:docPr id=\""+id+"\" name=\"Cartão "+id+"\" descr=\"Cartão de respostas v2: não alterar marcadores ou proporções\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"cartao.png\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"rIdCard\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3132000\" cy=\"4698000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p></w:tc>");
        body.append("</w:tr>");
        body.append("</w:tbl><w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"20\" w:lineRule=\"exact\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"2\"/></w:rPr><w:t/></w:r></w:p><w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"283\" w:right=\"283\" w:bottom=\"283\" w:left=\"283\" w:header=\"0\" w:footer=\"0\"/></w:sectPr></w:body></w:document>");
        try(ZipOutputStream zip=new ZipOutputStream(output)){
            entry(zip,"[Content_Types].xml","<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Default Extension=\"png\" ContentType=\"image/png\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            entry(zip,"_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            entry(zip,"word/document.xml",body.toString());
            entry(zip,"word/_rels/document.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rIdCard\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/cartao.png\"/></Relationships>");
            entry(zip,"word/media/cartao.png",cardPng);
        }
    }
}
