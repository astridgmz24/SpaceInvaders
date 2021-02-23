import java.awt.Color;
import java.util.Iterator;

public class Disparo extends Colisionable implements ObjetoMovil{
    //Atributos
    private double ox, oy;
    private final double disp_radio=0.3;
    private final static double disp_vel=0.5;//Velocidad disparo VY
    private static final double disp_maxdist = 10;
    
    //Constructor
    public Disparo (double ox, double oy){
        this.ox = ox;
        this.oy = oy;
    }
    
    public void MueveDibuja(Ventana v){
        oy+=disp_vel;
        v.dibujaCirculo(ox, oy, disp_radio, Color.RED);
        
        for(ObjetoMovil om : Objetos.Instancia.getArray()) {
            if(om instanceof Marcianito) {
                if(colisiona((Colisionable)om)) {
                    Objetos.Instancia.eliminar(this);
                    Objetos.Instancia.eliminar(om);
                    Objetos.Instancia.añadir(new Explosion(((Colisionable)om).getOx(),((Colisionable)om).getOy()));
                }
            }
        }
        
        if(oy > disp_maxdist) {
            Objetos.Instancia.eliminar(this);
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
        return disp_radio;
    }
    
}