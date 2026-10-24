package br.com.cfprova;

public final class Grading {
    private Grading() {}
    // -1 = blank, -2 = ambiguous. Only a single sufficiently dark bubble is accepted.
    public static int classify(double[] darkness) {
        if (darkness.length != 5) throw new IllegalArgumentException("Cinco alternativas necessárias");
        int selected = -1;
        for (int i = 0; i < 5; i++) {
            if (darkness[i] >= .42) { if (selected >= 0) return -2; selected = i; }
        }
        if (selected >= 0) for (int i=0;i<5;i++) if(i!=selected && darkness[i]>.24) return -2;
        if (selected == -1) for(double d:darkness) if(d>.18) return -2;
        return selected;
    }
    public static int correct(int[] key, int[] answers) {
        if (key.length == 0 || key.length != answers.length) throw new IllegalArgumentException("Quantidade inválida");
        int hits=0;
        for(int i=0;i<key.length;i++) {
            if(key[i]<0 || key[i]>4 || answers[i]<-2 || answers[i]>4) throw new IllegalArgumentException("Resposta inválida");
            if(key[i]==answers[i]) hits++;
        }
        return hits;
    }
    public static double grade(int[] key,int[] answers) { return 10.0*correct(key,answers)/key.length; }
}
