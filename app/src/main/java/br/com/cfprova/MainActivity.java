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
    private LinearLayout root,body,footer;
    private String currentPage="home";
    private int[] key=new int[20];
    private int[] answers;
    private String exam="Minha prova";
    private EditText title,count;
    private static final int INK=AppUi.INK;
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
        root=AppUi.column(this);root.setBackgroundColor(AppUi.BG);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(16),insets.getSystemWindowInsetTop()+dp(12),dp(16),insets.getSystemWindowInsetBottom()+dp(8));return insets;});
        setContentView(root);root.requestApplyInsets();
        LinearLayout bar=AppUi.row(this);bar.setPadding(0,0,0,dp(12));
        TextView logo=AppUi.badge(this,"CF",Color.WHITE,AppUi.BLUE);logo.setTextSize(19);bar.addView(logo);
        TextView brand=AppUi.text(this,"  CF Prova",19,INK,true);bar.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        if("home".equals(currentPage))bar.addView(AppUi.badge(this,"OFFLINE",AppUi.TEAL,0xffe5f5ef));
        else bar.addView(AppUi.button(this,"Início",false,"back",this::home));
        root.addView(bar);
        ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setFillViewport(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=AppUi.column(this);body.setPadding(0,dp(4),0,dp(12));scroll.addView(body);
        if(!"home".equals(currentPage)){body.addView(AppUi.text(this,heading,28,INK,true));AppUi.gap(body,6);body.addView(AppUi.text(this,sub,14,AppUi.MUTED,false));AppUi.gap(body,20);}
        footer=AppUi.column(this);footer.setPadding(0,dp(8),0,0);root.addView(footer);
    }
    private TextView text(String s,int size){return AppUi.text(this,s,size,INK,false);}
    private void action(LinearLayout parent,String label,boolean primary,String icon,Runnable run){parent.addView(AppUi.button(this,label,primary,icon,run),AppUi.block(this,8));}
    private EditText field(LinearLayout parent,String label,String value,boolean numeric){
        parent.addView(AppUi.text(this,label,12,AppUi.MUTED,true));AppUi.gap(parent,6);
        EditText e=new EditText(this);e.setText(value);e.setHint(label);e.setTextSize(17);e.setTextColor(INK);e.setSingleLine();e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackground(AppUi.outline(this,0xfff7f9fd,12,AppUi.LINE));
        e.setInputType(numeric?android.text.InputType.TYPE_CLASS_NUMBER:android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        e.setContentDescription(label);parent.addView(e,AppUi.block(this,14));return e;
    }
    private void home(){
        currentPage="home";page("Sua próxima correção","");
        LinearLayout hero=AppUi.hero(this);hero.addView(AppUi.text(this,"MENOS PAPELADA. MAIS TEMPO.",10,0xffaddfdc,true));AppUi.gap(hero,12);
        hero.addView(AppUi.text(this,"Sua próxima correção\ncomeça aqui.",29,Color.WHITE,true));AppUi.gap(hero,10);
        hero.addView(AppUi.text(this,"Prepare a prova, leia o cartão-resposta e confira os acertos.",14,0xffdae5f8,false));AppUi.gap(hero,16);
        hero.addView(AppUi.badge(this,"01  Prepare    →    02  Fotografe    →    03  Confira",Color.WHITE,0xff345485));body.addView(hero,AppUi.block(this,18));
        LinearLayout setup=AppUi.card(this);AppUi.section(setup,"01","Sua avaliação","Um gabarito por vez, do seu jeito.");
        title=field(setup,"NOME DA PROVA",exam,false);count=field(setup,"QUESTÕES · DE 5 A 40",""+key.length,true);
        setup.addView(AppUi.badge(this,"A–E  •  Questões com o mesmo peso",AppUi.MUTED,0xfff0f3fa));body.addView(setup,AppUi.block(this,14));
        LinearLayout answer=AppUi.card(this);AppUi.section(answer,"02","Gabarito do professor","Confira a alternativa correta de cada questão antes de começar.");
        action(answer,"Conferir gabarito",false,"check",()->{if(configure())editKey();});body.addView(answer,AppUi.block(this,14));
        LinearLayout print=AppUi.card(this);AppUi.section(print,"03","Cartão-resposta do aluno","Um cartão-resposta compacto de ¼ de A4. Cabeçalho editável para qualquer instituição.");
        LinearLayout exports=AppUi.row(this);Button word=AppUi.button(this,"Word",false,"doc",()->{if(configure())word();});Button pdf=AppUi.button(this,"PDF",false,"doc",()->{if(configure())pdf();});
        LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,-2,1);half.rightMargin=dp(8);exports.addView(word,half);exports.addView(pdf,new LinearLayout.LayoutParams(0,-2,1));print.addView(exports);body.addView(print,AppUi.block(this,14));
        LinearLayout tip=AppUi.card(this);tip.setBackground(AppUi.shape(this,0xffe8f5f0,20));tip.addView(AppUi.text(this,"A foto é só para conferir",15,AppUi.TEAL,true));AppUi.gap(tip,5);tip.addView(AppUi.text(this,"Ela fica apenas na memória e é descartada ao sair da conferência.",13,AppUi.TEAL,false));body.addView(tip,AppUi.block(this,8));
        action(footer,"Ler cartão-resposta",true,"camera",()->{if(configure())capture();});
    }
    private boolean configure(){
        try {int n=Integer.parseInt(count.getText().toString());if(n<5||n>40)throw new NumberFormatException();
            if(n!=key.length){key=Arrays.copyOf(key,n);} exam=title.getText().toString().trim();if(exam.isEmpty())exam="Minha prova";save();return true;
        }catch(NumberFormatException e){message("Informe de 5 a 40 questões.");return false;}
    }
    private void save(){StringBuilder s=new StringBuilder();for(int v:key)s.append((char)('A'+v));getPreferences(0).edit().putString("key",s.toString()).putString("exam",exam).apply();}
    private void editKey(){
        currentPage="key";page("Gabarito da prova",exam+" • Toque na alternativa correta de cada questão.");
        body.addView(AppUi.badge(this,key.length+" QUESTÕES  •  ALTERNATIVAS A–E",AppUi.BLUE,0xffe9efff));AppUi.gap(body,16);
        for(int i=0;i<key.length;i++)answerRow(i,key);
        action(footer,"Salvar gabarito",true,"check",()->{save();home();});
    }
    private void answerRow(int i,int[] values){
        LinearLayout card=AppUi.card(this);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        card.addView(AppUi.text(this,String.format(Locale.ROOT,"QUESTÃO %02d",i+1),11,AppUi.MUTED,true));AppUi.gap(card,10);
        LinearLayout choices=AppUi.row(this);Button[] buttons=new Button[5];
        for(int j=0;j<5;j++){
            final int selected=j;Button b=AppUi.button(this,""+(char)('A'+j),false,null,()->{values[i]=selected;for(int k=0;k<5;k++)AppUi.decorateChoice(buttons[k],k==selected);});
            b.setPadding(dp(4),dp(10),dp(4),dp(10));b.setContentDescription("Questão "+(i+1)+", alternativa "+(char)('A'+j));AppUi.decorateChoice(b,j==values[i]);buttons[j]=b;
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(j<4)lp.rightMargin=dp(4);choices.addView(b,lp);
        }
        card.addView(choices);body.addView(card,AppUi.block(this,10));
    }
    private void share(String file,String type){
        Intent send=new Intent(Intent.ACTION_SEND).setType(type).putExtra(Intent.EXTRA_STREAM,uri(file)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setClipData(ClipData.newRawUri("Cartão-resposta",uri(file)));
        startActivity(Intent.createChooser(send,"Salvar ou compartilhar cartão-resposta"));
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
            {
                float left=5*mm,top=5*mm;
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
        startActivityForResult(new Intent(this,ScannerActivity.class).putExtra("key",key.clone()).putExtra("exam",exam),1);
    }
    @Override protected void onActivityResult(int request,int code,Intent data){
        super.onActivityResult(request,code,data);
        if(request==1 && code==RESULT_OK && data!=null){
            int[] read=data.getIntArrayExtra("answers");
            if(read!=null&&read.length==key.length){answers=read;result();}
        }
    }
    private void result(){
        currentPage="result";int hits=Grading.correct(key,answers),blanks=0;for(int a:answers)if(a<0)blanks++;
        page("Correção concluída",exam+" • Confira o desempenho e as respostas abaixo.");
        LinearLayout score=AppUi.card(this);score.setGravity(Gravity.CENTER_HORIZONTAL);score.addView(AppUi.badge(this,"RESULTADO DA AVALIAÇÃO",AppUi.BLUE,0xffedf1ff));AppUi.gap(score,12);
        score.addView(new AppUi.GradeRing(this,Grading.grade(key,answers)),new LinearLayout.LayoutParams(dp(174),dp(174)));AppUi.gap(score,12);
        score.addView(AppUi.text(this,hits+" de "+key.length+" questões corretas",23,INK,true));AppUi.gap(score,16);
        LinearLayout stats=AppUi.row(this);stat(stats,"Acertos",hits,AppUi.TEAL);stat(stats,"Erros",key.length-hits-blanks,0xffb54b47);stat(stats,"Em branco",blanks,AppUi.MUTED);score.addView(stats);body.addView(score,AppUi.block(this,20));
        body.addView(AppUi.text(this,"Questão por questão",20,INK,true));AppUi.gap(body,12);
        for(int i=0;i<key.length;i++){
            boolean correct=answers[i]==key[i];String response=answers[i]<0?"Em branco":"Resposta "+(char)('A'+answers[i]);
            LinearLayout row=AppUi.card(this);row.setPadding(dp(14),dp(12),dp(14),dp(12));
            LinearLayout top=AppUi.row(this);top.addView(AppUi.text(this,String.format(Locale.ROOT,"%02d",i+1),18,INK,true));
            TextView label=AppUi.text(this,"  "+response,15,INK,false);top.addView(label,new LinearLayout.LayoutParams(0,-2,1));top.addView(AppUi.badge(this,correct?"ACERTO":answers[i]<0?"EM BRANCO":"ERRO",correct?AppUi.TEAL:AppUi.MUTED,correct?0xffe5f5ed:0xfff0f2f7));row.addView(top);AppUi.gap(row,5);row.addView(AppUi.text(this,"Gabarito: "+(char)('A'+key[i]),12,AppUi.MUTED,false));body.addView(row,AppUi.block(this,8));
        }
        AppUi.gap(body,8);body.addView(AppUi.text(this,"Conferência finalizada. A foto foi descartada.",12,AppUi.MUTED,false));
        action(footer,"Corrigir outro aluno",true,"camera",this::capture);action(footer,"Voltar ao início",false,null,this::home);
    }
    private void stat(LinearLayout row,String label,int number,int color){LinearLayout item=AppUi.column(this);item.setGravity(Gravity.CENTER);item.addView(AppUi.text(this,""+number,25,color,true));item.addView(AppUi.text(this,label,11,AppUi.MUTED,false));row.addView(item,new LinearLayout.LayoutParams(0,-2,1));}
    private void message(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
}
