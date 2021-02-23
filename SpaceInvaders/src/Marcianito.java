import java.awt.Color;

public class Marcianito extends Colisionable implements ObjetoMovil{
    //Atributos
    private double ox, oy;
    private double mar_radio=1;
    private double velocidad;//velocidad actual
    private Nave nave;
    
    private static final double mar_Velocidad_inic=0.4;//Velocidad inicial
    private static final double mar_Salto_y=-0.5;
    private static final int frec_Disparo=50;
    
    //Constructor
    public Marcianito(double ox, double oy, Nave nave){
        this.ox=ox;
        this.oy=oy;
        this.nave=nave;
        if(SpaceMain.Rand.nextBoolean()) {
            velocidad = mar_Velocidad_inic;
        } else {
            velocidad = -mar_Velocidad_inic;
        }
    }
    
    public void MueveDibuja(Ventana v){
        //Mover
        ox+=velocidad;
        if(ox>SpaceMain.Margen || ox<-SpaceMain.Margen){
            velocidad=-velocidad;
            oy+=mar_Salto_y;
        }
        
        //Dibujar
        v.dibujaRectangulo(ox-0.5, oy+1.5, 0.1, 0.7, Color.GREEN);
        v.dibujaRectangulo(ox+0.5, oy+1.5, 0.1, 0.7, Color.GREEN);
        v.dibujaCirculo(ox, oy, 1, Color.GREEN);
        v.dibujaCirculo(ox-0.5, oy+0.3, 0.3, Color.BLACK);
        v.dibujaCirculo(ox+0.5, oy+0.3, 0.3, Color.BLACK);
        
        //Disparar
        if(SpaceMain.Rand.nextInt(frec_Disparo)==0){
            Objetos.Instancia.añadir(new DisparoEnemigo(ox,oy, nave));
        }
       
    }

    //Getter
    public double getOx() {
        return ox;
    }

    public double getOy() {
        return oy;
    }

    public double getR() {
        return mar_radio;
    }
    
}//Class Marcianito

