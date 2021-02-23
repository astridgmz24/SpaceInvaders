import java.awt.Color;
import java.util.Random;

public class DisparoEnemigo extends Colisionable implements ObjetoMovil{
    //Atributos
    private double ox, oy;
    private double dispen_velx, dispen_vely;
    public static final double dispen_maxvelocidad = 0.6;
    public static final double dispen_minvelocidad = 0.2;
    private static final Random rnd = new Random();
    private static final double dispen_maxdistancia = -12;
    private Nave nave;
    
    //Constructor
    public DisparoEnemigo (double ox, double oy, Nave nave){
        this.nave=nave;
        this.ox = ox;
        this.oy = oy;
        
        double angle = Math.atan((nave.getOy()-oy)/(nave.getOx()-ox));
        double velocidadBase = dispen_minvelocidad + rnd.nextDouble() * (dispen_maxvelocidad - dispen_minvelocidad);
        dispen_velx =  velocidadBase * Math.cos(angle);
        dispen_vely = velocidadBase * Math.sin(angle);  
        if((nave.getOx()-ox) < 0) {
            dispen_velx = -dispen_velx;
            dispen_vely = -dispen_vely;
        }
    }
    
    public void MueveDibuja(Ventana v){
        ox+=dispen_velx;
        oy+=dispen_vely;
        v.dibujaCirculo(ox, oy, 0.3, Color.YELLOW);
        
        if(oy < dispen_maxdistancia) {
            Objetos.Instancia.eliminar(this);        
        }
        if(colisiona(nave)) {
            nave.setDestruida(true);
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
        return 0.3;
    }
}
