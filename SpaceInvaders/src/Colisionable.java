//No es interfaz porque si tiene mas codigo

public abstract class Colisionable {
    public abstract double getOx();
    public abstract double getOy();
    public abstract double getR();
    
    public boolean colisiona(Colisionable otro){
        double dx=getOx() -otro.getOx();
        double dy=getOy() -otro.getOy();
        
        return Math.sqrt(dx*dx+dy*dy)<(getR()+otro.getR());
    }

    
}
