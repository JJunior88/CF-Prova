package br.com.cfprova;

import java.util.function.Consumer;

/** In-memory ownership only. Invalidates delayed captures when a screen is left. */
public final class TransientPhoto<T> {
    private final Consumer<T> release;
    private T value;
    private int generation;
    public TransientPhoto(Consumer<T> release){this.release=release;}
    public int begin(){clear();return generation;}
    public boolean isCurrent(int token){return token==generation;}
    public boolean accept(int token,T next){
        if(!isCurrent(token)){release.accept(next);return false;}
        if(value!=null)release.accept(value);
        value=next;return true;
    }
    public T get(){return value;}
    public void clear(){generation++;if(value!=null){release.accept(value);value=null;}}
}
