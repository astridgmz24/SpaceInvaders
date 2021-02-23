import java.awt.Color;

public class Explosion implements ObjetoMovil {
    //Atributos
    private double x, y;
    private double exp_radio;
    private final static double exp_radiomax = 1.5;
    private final static double exp_velocidad = 0.3;

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        exp_radio = 0;
    }
    

    public void MueveDibuja(Ventana v) {
        exp_radio += exp_velocidad;
        v.dibujaCirculo(x, y, exp_radio, Color.GREEN);
        if(exp_radio > exp_radiomax) {
            Objetos.Instancia.eliminar(this);
        }
    }
        
}
