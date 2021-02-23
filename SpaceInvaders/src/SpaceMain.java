import java.awt.*;
import java.util.Random;

public class SpaceMain {
    //Atributos
    public static final double Eje_vision=10;
    public static final int Ventana_pix=500;
    public static final double Margen=10;
    
    public static final double XNave=0;
    public static final double YNave=-9;
    public static final double yMarcianito=10;
    public static final double ESPACIO_VERTICAL_ENTRE_MARCIANOS = 2;
    public static final int MARCIANOS_INICIALES = 15;
    public static final int Frec_aparicion=60; //60 fotogramas = 2 segundos 

    
    public static final Random Rand = new Random();
    
    
    public static void main(String[] args) {
        Ventana v = new Ventana("Space Battle", Ventana_pix,Ventana_pix);
        
        Nave nave = new Nave(XNave, YNave);
        Objetos.Instancia.añadir(nave);
        
        int fotogramas=0;
        while(!v.isPulsadoEscape() && !nave.isDestruida()) {            
            if (fotogramas%Frec_aparicion==0){
                Objetos.Instancia.añadir(new Marcianito(Rand.nextDouble()*2*Margen-Margen, yMarcianito, nave));
            }
            Objetos.Instancia.MoverDibujarTodo(v);

            fotogramas++;
            v.actualizaFotograma();
        }
        
        while(!v.isPulsadoEscape()) {
            v.escribeTexto("GAME OVER", -5, 0, 2, Color.white );
            v.actualizaFotograma();
        }
    
        v.cerrar();
        
        
        
    }//Main
        

    
}//Clase MarcianitosMain

