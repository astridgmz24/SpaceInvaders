import java.util.HashSet;
import java.util.Set;

public class Objetos {
    //Singletone
    public static final Objetos Instancia = new Objetos();  
    private Set<ObjetoMovil> objetosmov=new HashSet<ObjetoMovil>();
    
    //Constructor privado
    private Objetos(){
        objetosmov=new HashSet<ObjetoMovil>();
    }
    
    public void añadir(ObjetoMovil o){
        objetosmov.add(o);
    }
    public void eliminar (ObjetoMovil o){
        objetosmov.remove(o);
    }
    
    public void MoverDibujarTodo(Ventana v){
        ObjetoMovil[]copia=getArray();
        for(ObjetoMovil o : getArray()){
            o.MueveDibuja(v);
        }
    }
    
    //Copia en arreglo
    public ObjetoMovil[] getArray(){
        ObjetoMovil[] arr = new ObjetoMovil[objetosmov.size()];
        return objetosmov.toArray(arr);
        
    }
}

