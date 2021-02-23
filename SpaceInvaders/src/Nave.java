import java.awt.Color;

public class Nave extends Colisionable implements ObjetoMovil{
    //Atributos
    private double ox, oy;//Coordenadas centro nave
    private static final double Nav_Velocidad=0.3;
    private static final double Nav_Vertices=1;
    private boolean destruida;
    
    //Constructor
    public Nave(double x, double y){
        ox=x;
        oy=y;
        destruida=false;
    }
    
    public void MueveDibuja(Ventana v){
        //Mover
        if(v.isPulsadoDerecha()&& ox<=SpaceMain.Margen){
            ox+=Nav_Velocidad;
        }
        if(v.isPulsadoIzquierda()&& ox>=-SpaceMain.Margen){
            ox-=Nav_Velocidad;
        }
        
        //Disparar
        if(v.isPulsadoEspacio()){
            Objetos.Instancia.añadir(new Disparo(ox,oy));
        }
        
        //Dibujar
        v.dibujaTriangulo(ox-Nav_Vertices, oy-Nav_Vertices, 
                          ox, oy+Nav_Vertices, 
                          ox+Nav_Vertices, oy-Nav_Vertices, Color.BLUE);
        
    }
    public boolean isDestruida(){
        return destruida;
    }
    
    //Setter
    public void setDestruida(boolean destruida){
        this.destruida=destruida;
    }
    //Getter
    public double getOx(){
        return ox;
    }
    public double getOy(){
        return oy;
    }

    public double getR() {
        return 1;
    }
}//clase Nave
