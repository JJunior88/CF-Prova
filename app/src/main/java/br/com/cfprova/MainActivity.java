package br.com.cfprova;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root,body;
    private int[] key=new int[20];
    private int[] answers;
    private String exam="Minha prova";
    private EditText title,count;
    private static final int INK=Color.rgb(24,46,48), TEAL=Color.rgb(8,127,117);
    private Uri uri(String name){return Uri.parse("content://br.com.cfprova.files/"+name);}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        String saved=getPreferences(0).getString("key","");
        if(!saved.isEmpty()) {key=new int[saved.length()]; for(int i=0;i<key.length;i++)key[i]=saved.charAt(i)-'A';}
        exam=getPreferences(0).getString("exam","Minha prova");
        home();
    }
    private void page(String heading,String sub){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(16),dp(20),dp(10));root.setBackgroundColor(Color.rgb(243,247,246));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(20),dp(12)+insets.getSystemWindowInsetTop(),dp(20),dp(10)+insets.getSystemWindowInsetBottom());return insets;});
        setContentView(root); root.requestApplyInsets();
        TextView brand=text("CF / PROVA",13);brand.setTextColor(TEAL);root.addView(brand);
        TextView h=text(heading,28);h.setPadding(0,dp(18),0,dp(8));root.addView(h);
        root.addView(text(sub,15));
        ScrollView scroll=new ScrollView(this); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,dp(18),0,dp(12));scroll.addView(body);
    }
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(INK);t.setPadding(0,dp(5),0,dp(5));return t;}
    private void button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(TEAL);body.addView(b,new LinearLayout.LayoutParams(-1,dp(56)));b.setOnClickListener(v->action.run());}
    private EditText field(String hint,String value){EditText e=new EditText(this);e.setHint(hint);e.setText(value);e.setSingleLine();body.addView(e);return e;}
    private void home(){
        page("Corrigir ficou simples", "Cadastre, aponte a câmera e veja a nota. Leitura ao vivo, sem salvar fotos e sem internet.");
        body.addView(text("1  Prepare a prova",20));
        title=field("Nome da prova",exam);count=field("Número de questões (5 a 40)",""+key.length);count.setInputType(2);
        button("Editar gabarito",()->{if(configure())editKey();});
        button("Word editável • 4 cartões por A4",()->{if(configure())word();});
        button("PDF • 4 cartões por A4",()->{if(configure())pdf();});
        body.addView(text("2  Leia o cartão do aluno",20));
        body.addView(text("Use somente o cartão gerado pelo app. Preencha uma bolha por questão com caneta escura. Aponte para um cartão por vez, com os quatro marcadores visíveis, boa luz e sem sombras. Use o cartão v2, com o quadrado maior no alto à esquerda.",15));
        button("Abrir câmera e corrigir",()->{if(configure())capture();});
        body.addView(text("O gabarito fica salvo neste aparelho. A primeira versão trabalha com uma prova por vez, alternativas A–E e questões de mesmo peso.",14));
    }
    private boolean configure(){
        try {int n=Integer.parseInt(count.getText().toString());if(n<5||n>40)throw new NumberFormatException();
            if(n!=key.length){key=Arrays.copyOf(key,n);} exam=title.getText().toString().trim();if(exam.isEmpty())exam="Minha prova";save();return true;
        }catch(NumberFormatException e){message("Informe de 5 a 40 questões.");return false;}
    }
    private void save(){StringBuilder s=new StringBuilder();for(int v:key)s.append((char)('A'+v));getPreferences(0).edit().putString("key",s.toString()).putString("exam",exam).apply();}
    private void editKey(){
        page("Gabarito do professor",exam+" • Todas as questões têm o mesmo peso. Confira as alternativas antes de abrir a câmera.");
        for(int i=0;i<key.length;i++) answerRow(i,key);
        button("Salvar gabarito",()->{save();home();});
    }
    private void answerRow(int i,int[] values){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(4),0,dp(4));
        String label=String.format(Locale.ROOT,"%02d",i+1);
        TextView t=text(label,15);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        Spinner sp=new Spinner(this);String[] choices=new String[]{"A","B","C","D","E"};
        sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));sp.setSelection(values[i]>=0?values[i]:values[i]==-1?5:6);
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int pos,long id){values[i]=pos<5?pos:pos==5?-1:-2;}});
        row.addView(sp,new LinearLayout.LayoutParams(dp(150),dp(52)));body.addView(row);
    }
    private void share(String file,String type){
        Intent send=new Intent(Intent.ACTION_SEND).setType(type).putExtra(Intent.EXTRA_STREAM,uri(file)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setClipData(ClipData.newRawUri("Cartões de respostas",uri(file)));
        startActivity(Intent.createChooser(send,"Salvar ou compartilhar cartões"));
    }
    private void word(){
        try {
            Bitmap image=Bitmap.createBitmap(1200,1800,Bitmap.Config.ARGB_8888);
            ByteArrayOutputStream png=new ByteArrayOutputStream();
            try {Canvas canvas=new Canvas(image);canvas.scale(2,2);CardDrawing.draw(canvas,key.length);image.compress(Bitmap.CompressFormat.PNG,100,png);}finally{image.recycle();}
            try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"cartoes.docx"))){CardDocx.write(out,png.toByteArray(),exam);}
            share("cartoes.docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }catch(Exception e){message("Não foi possível gerar ou compartilhar o Word: "+e.getMessage());}
    }
    private void pdf(){
        PdfDocument doc=new PdfDocument();
        try {
            PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,1).create());Canvas c=page.getCanvas();c.drawColor(Color.WHITE);
            Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);ink.setColor(Color.BLACK);ink.setTextSize(7.5f);
            float mm=72f/25.4f;
            for(int row=0;row<2;row++)for(int col=0;col<2;col++){
                float left=5*mm+col*100*mm,top=5*mm+row*142.875f*mm;
                c.drawText("Instituição: __________________________________",left+6.2f*mm,top+8,ink);
                c.drawText("Professor(a): _________________________________",left+6.2f*mm,top+17,ink);
                String title="Avaliação: "+exam;int end=ink.breakText(title,true,87*mm,null);
                c.drawText(title.substring(0,end),left+6.2f*mm,top+26,ink);
                c.save();c.translate(left+6.5f*mm,top+28);c.scale(87*mm/600,87*mm/600);CardDrawing.draw(c,key.length);c.restore();
            }
            doc.finishPage(page);try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"cartao.pdf"))){doc.writeTo(out);}
            share("cartao.pdf","application/pdf");
        }catch(Exception e){message("Não foi possível gerar ou compartilhar o PDF: "+e.getMessage());}finally{doc.close();}
    }
    private void capture(){
        startActivityForResult(new Intent(this,ScannerActivity.class).putExtra("questions",key.length),1);
    }
    @Override protected void onActivityResult(int request,int code,Intent data){
        super.onActivityResult(request,code,data);
        if(request==1 && code==RESULT_OK && data!=null){
            int[] read=data.getIntArrayExtra("answers");
            if(read!=null&&read.length==key.length){answers=read;result();}
        }
    }
    private void result(){
        int hits=Grading.correct(key,answers);
        page("Resultado",exam);
        body.addView(text(String.format(new Locale("pt","BR"),"%.1f / 10",Grading.grade(key,answers)),48));
        body.addView(text(hits+" acertos em "+key.length+" questões",22));
        for(int i=0;i<key.length;i++)body.addView(text(String.format(Locale.ROOT,"%02d",i+1)+"   "+(answers[i]<0?"Em branco":""+(char)('A'+answers[i]))+"   •   Gabarito: "+(char)('A'+key[i])+"   "+(answers[i]==key[i]?"✓":"✗"),16));
        body.addView(text("Leitura automática estável. Respostas em branco valem zero. Confira os detalhes abaixo antes de registrar a nota.",14));
        button("Corrigir outro aluno",this::capture);
        button("Voltar ao início",this::home);
    }
    private void message(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
}
