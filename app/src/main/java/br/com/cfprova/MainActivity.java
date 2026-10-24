package br.com.cfprova;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root,body;
    private int[] key=new int[20];
    private int[] answers;
    private Bitmap photo;
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
        page("Corrigir ficou simples", "Cadastre, fotografe e confira a nota. Tudo no celular, sem enviar fotos à internet.");
        body.addView(text("1  Prepare a prova",20));
        title=field("Nome da prova",exam);count=field("Número de questões (5 a 40)",""+key.length);count.setInputType(2);
        button("Editar gabarito",()->{if(configure())editKey();});
        button("Gerar cartão para impressão",()->{if(configure())pdf();});
        body.addView(text("2  Leia o cartão do aluno",20));
        body.addView(text("Use somente o cartão gerado pelo app. Preencha uma bolha por questão com caneta escura. Fotografe a folha inteira, na posição vertical, com boa luz e sem sombras.",15));
        button("Fotografar cartão",()->{if(configure())capture();});
        body.addView(text("O gabarito fica salvo neste aparelho. A primeira versão trabalha com uma prova por vez, alternativas A–E e questões de mesmo peso.",14));
    }
    private boolean configure(){
        try {int n=Integer.parseInt(count.getText().toString());if(n<5||n>40)throw new NumberFormatException();
            if(n!=key.length){key=Arrays.copyOf(key,n);} exam=title.getText().toString().trim();if(exam.isEmpty())exam="Minha prova";save();return true;
        }catch(NumberFormatException e){message("Informe de 5 a 40 questões.");return false;}
    }
    private void save(){StringBuilder s=new StringBuilder();for(int v:key)s.append((char)('A'+v));getPreferences(0).edit().putString("key",s.toString()).putString("exam",exam).apply();}
    private void editKey(){
        page("Gabarito do professor",exam+" • Todas as questões têm o mesmo peso. Confira as alternativas antes de fotografar.");
        for(int i=0;i<key.length;i++) answerRow(i,key,false);
        button("Salvar gabarito",()->{save();home();});
    }
    private void answerRow(int i,int[] values,boolean reviewing){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(4),0,dp(4));
        String label=String.format(Locale.ROOT,"%02d",i+1);
        if(reviewing) label+="  "+(values[i]==-2?"Revisar":values[i]==-1?"Em branco":values[i]==key[i]?"Acerto":"Erro");
        TextView t=text(label,15);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        Spinner sp=new Spinner(this);String[] choices=reviewing?new String[]{"A","B","C","D","E","Em branco","Revisar"}:new String[]{"A","B","C","D","E"};
        sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));sp.setSelection(values[i]>=0?values[i]:values[i]==-1?5:6);
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int pos,long id){values[i]=pos<5?pos:pos==5?-1:-2;}});
        row.addView(sp,new LinearLayout.LayoutParams(dp(150),dp(52)));body.addView(row);
    }
    private float rowY(int i){return 195+i*(610f/Math.max(1,key.length-1));}
    private void pdf(){
        PdfDocument doc=new PdfDocument();
        try {
            PdfDocument.Page p=doc.startPage(new PdfDocument.PageInfo.Builder(600,900,1).create());Canvas c=p.getCanvas();c.drawColor(Color.WHITE);Paint ink=new Paint(3);ink.setColor(Color.BLACK);
            for(int x:new int[]{40,560})for(int y:new int[]{40,860})c.drawRect(x-10,y-10,x+10,y+10,ink);
            ink.setTextSize(20);c.drawText("CF PROVA • "+exam,70,85,ink);ink.setTextSize(12);
            c.drawText("Aluno: __________________________________________",70,112,ink);
            c.drawText(key.length+" questões • A–E • Uma resposta por questão",70,135,ink);
            c.drawText("Preencha toda a bolha. Não dobre ou escreva sobre os marcadores.",70,153,ink);
            for(int j=0;j<5;j++)c.drawText(""+(char)('A'+j),216+j*65,175,ink);
            for(int i=0;i<key.length;i++){
                float y=rowY(i);ink.setStyle(Paint.Style.FILL);c.drawText(String.format(Locale.ROOT,"%02d",i+1),150,y+4,ink);
                ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(1);for(int j=0;j<5;j++)c.drawCircle(220+j*65,y,6,ink);
            }
            ink.setStyle(Paint.Style.FILL);ink.setTextSize(10);c.drawText("CF Prova v1 • Fotografe na vertical, com os quatro quadrados visíveis.",70,837,ink);
            doc.finishPage(p);try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"cartao.pdf"))){doc.writeTo(out);}
            Intent send=new Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,uri("cartao.pdf")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send,"Imprimir ou compartilhar cartão"));
        }catch(Exception e){message("Não foi possível gerar ou compartilhar o cartão: "+e.getMessage());} finally {doc.close();}
    }
    private void capture(){
        new AlertDialog.Builder(this).setTitle("Gabarito conferido?").setMessage("O gabarito atual tem "+key.length+" respostas. Confira-o antes da correção; novas questões começam com A.").setPositiveButton("Fotografar",(d,w)->{
            try {Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT,uri("capture.jpg")).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);i.setClipData(ClipData.newRawUri("Foto",uri("capture.jpg")));startActivityForResult(i,1);}catch(ActivityNotFoundException e){message("Nenhum aplicativo de câmera disponível.");}
        }).setNegativeButton("Conferir gabarito",(d,w)->editKey()).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==1 && result==RESULT_OK){
        try {BitmapFactory.Options o=new BitmapFactory.Options();File f=new File(getCacheDir(),"capture.jpg");o.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.toString(),o);int sample=1;while(Math.max(o.outWidth,o.outHeight)/sample>2400)sample*=2;o.inJustDecodeBounds=false;o.inSampleSize=sample;photo=BitmapFactory.decodeFile(f.toString(),o);if(photo==null)throw new IOException();
            androidx.exifinterface.media.ExifInterface exif=new androidx.exifinterface.media.ExifInterface(f.toString());
            int orientation=exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,1);
            Matrix rotation=new Matrix();
            switch(orientation){
                case 2: rotation.setScale(-1,1); break;
                case 3: rotation.setRotate(180); break;
                case 4: rotation.setScale(1,-1); break;
                case 5: rotation.setRotate(90);rotation.postScale(-1,1);break;
                case 6: rotation.setRotate(90);break;
                case 7: rotation.setRotate(270);rotation.postScale(-1,1);break;
                case 8: rotation.setRotate(270);break;
            }
            if(!rotation.isIdentity()){Bitmap fixed=Bitmap.createBitmap(photo,0,0,photo.getWidth(),photo.getHeight(),rotation,true);if(fixed!=photo)photo.recycle();photo=fixed;}
            align();}catch(Exception e){message("Não foi possível abrir a foto. Tente novamente.");}
    }}
    private void align(){
        page("Alinhar o cartão", "Toque no centro dos quadrados nesta ordem: superior esquerdo, superior direito, inferior direito e inferior esquerdo do cartão. Siga a orientação do texto impresso.");
        TextView status=text("Marcador 1 de 4",16);body.addView(status);
        Alignment view=new Alignment(status);body.addView(view,new LinearLayout.LayoutParams(-1,dp(430)));
        button("Refazer os quatro pontos",()->{view.points.clear();status.setText(R.string.first_marker);view.invalidate();});
        button("Ler respostas",()->{if(view.points.size()!=4){message("Selecione os quatro marcadores.");return;}read(view.points);});
        button("Tirar outra foto",this::capture);
    }
    private class Alignment extends View {
        ArrayList<PointF> points=new ArrayList<>();Paint p=new Paint(3);RectF bounds=new RectF();TextView status;
        Alignment(TextView s){super(MainActivity.this);status=s;setContentDescription("Foto do cartão. Toque nos quatro marcadores na ordem indicada.");}
        protected void onDraw(Canvas c){float scale=Math.min(getWidth()/(float)photo.getWidth(),getHeight()/(float)photo.getHeight());float w=photo.getWidth()*scale,h=photo.getHeight()*scale;bounds.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);c.drawBitmap(photo,null,bounds,p);p.setColor(TEAL);p.setTextSize(dp(18));for(int i=0;i<points.size();i++){PointF q=points.get(i);float x=bounds.left+q.x*scale,y=bounds.top+q.y*scale;c.drawCircle(x,y,dp(7),p);c.drawText(""+(i+1),x+dp(9),y,p);}}
        public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){performClick();if(points.size()<4 && bounds.contains(e.getX(),e.getY())){points.add(new PointF((e.getX()-bounds.left)*photo.getWidth()/bounds.width(),(e.getY()-bounds.top)*photo.getHeight()/bounds.height()));status.setText(points.size()==4?"Quatro marcadores selecionados":"Marcador "+(points.size()+1)+" de 4");invalidate();}return true;}return true;}
        public boolean performClick(){super.performClick();return true;}
    }
    private void read(ArrayList<PointF> points){
        float[] src=new float[8];for(int i=0;i<4;i++){src[2*i]=points.get(i).x;src[2*i+1]=points.get(i).y;}
        // Reject crossed, collinear, tiny or reversed selections before rectifying.
        for(int i=0;i<4;i++){PointF a=points.get(i),b=points.get((i+1)%4),c=points.get((i+2)%4);if((b.x-a.x)*(c.y-b.y)-(b.y-a.y)*(c.x-b.x)<100){message("Pontos inválidos. Selecione os cantos na ordem indicada.");return;}}
        Matrix m=new Matrix();if(!m.setPolyToPoly(src,0,new float[]{40,40,560,40,560,860,40,860},0,4)){message("Não foi possível alinhar a imagem.");return;}
        Bitmap sheet=Bitmap.createBitmap(600,900,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);c.drawColor(Color.WHITE);c.drawBitmap(photo,m,new Paint(3));answers=new int[key.length];
        for(int i=0;i<key.length;i++){
            double[] dark=new double[5];
            for(int j=0;j<5;j++){
                int hits=0,total=0,cx=220+j*65,cy=Math.round(rowY(i));
                for(int dx=-3;dx<=3;dx++)for(int dy=-3;dy<=3;dy++)if(dx*dx+dy*dy<=9){int color=sheet.getPixel(cx+dx,cy+dy);double l=.299*Color.red(color)+.587*Color.green(color)+.114*Color.blue(color);if(l<140)hits++;total++;}
                dark[j]=hits/(double)total;
            }
            answers[i]=Grading.classify(dark);
        }
        sheet.recycle();review();
    }
    private void review(){
        page("Confira a leitura", "Compare com a foto antes de confirmar. Ajuste respostas incorretas e resolva todas as marcações “Revisar”. Em branco vale zero.");
        ImageView preview=new ImageView(this);preview.setImageBitmap(photo);preview.setAdjustViewBounds(true);body.addView(preview,new LinearLayout.LayoutParams(-1,dp(280)));
        for(int i=0;i<key.length;i++)answerRow(i,answers,true);
        button("Confirmar e calcular nota",()->{for(int a:answers)if(a==-2){message("Resolva as respostas marcadas como Revisar.");return;}result();});
        button("Refazer alinhamento",this::align);
    }
    private void result(){
        int hits=Grading.correct(key,answers);
        page("Resultado",exam);
        body.addView(text(String.format(new Locale("pt","BR"),"%.1f / 10",Grading.grade(key,answers)),48));
        body.addView(text(hits+" acertos em "+key.length+" questões",22));
        for(int i=0;i<key.length;i++)body.addView(text(String.format(Locale.ROOT,"%02d",i+1)+"   "+(answers[i]<0?"Em branco":""+(char)('A'+answers[i]))+"   •   Gabarito: "+(char)('A'+key[i])+"   "+(answers[i]==key[i]?"✓":"✗"),16));
        button("Revisar respostas",this::review);
        button("Corrigir outro aluno",this::capture);
        button("Voltar ao início",this::home);
    }
    private void message(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
}
