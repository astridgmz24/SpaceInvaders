import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Ventana {
    /**
     * Indica el número de fotogramas por segundo. Es decir, el máximo de veces
     * que se puede mostrar el lienzo por pantalla en un segundo.
     */
    private static final long FOTOGRAMAS_SEGUNDO = 30;

    private static final float INTERLINEADO = 1.1f;

    //dos lienzos para el double buffer
    private Image lienzo ;

    private Graphics2D fg;

    private JFrame marcoVentana = null;

    private boolean up = false, down = false, left = false, right = false, space = false, escPulsado;
    private AffineTransform camara;

    private double camX, camY, campoVisionH, campoVisionV;
    private boolean dibujaCoordenadas = false;

    private double lienzoAnchura, lienzoAltura;
    private double aspectRatioH, aspectRatioV;

    private double ratonX, ratonY;
    private boolean ratonPulsado;
    private Color colorFondo = Color.black;

    private Map<String, BufferedImage> cacheImagenes = new HashMap<String, BufferedImage>();

    // Substitutivo para mostrar cuando una imagen no se ha encontrado
    private BufferedImage imagenNoEncontrada;

    /**
     * <p>Crea una nueva ventana.</p>
     * <p><b>NOTA</b>: Si se cierra la ventana con el ratón, el programa acabará.</p>
     * @param titulo El texto que aparecerá en la barra de título de la ventana.
     */
    public Ventana(String titulo) {
        this(titulo,640,480);
    }
    /**
     * <p>Crea una nueva ventana.</p>
     * <p><b>NOTA</b>: Si se cierra la ventana con el ratón, el programa acabará.</p>
     * @param titulo El texto que aparecerá en la barra de título de la ventana.
     * @param ancho Anchura de la ventana en píxels
     * @param alto Altura de la ventana en píxels
     */
    public Ventana(String titulo, int ancho, int alto) {
        final JPanel pantalla = new JPanel();
        marcoVentana = new JFrame(titulo) {
               
                public void paint(Graphics g) {
                    //super.paint(g);
                    pantalla.getGraphics().drawImage(lienzo, 0, 0, null);
                }
            };
        LectorRaton lr = new LectorRaton();
        pantalla.addMouseListener(lr);
        pantalla.addMouseMotionListener(lr);
        marcoVentana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marcoVentana.addWindowListener(new ControladorVentana());
        marcoVentana.setSize(ancho, alto);
        marcoVentana.setResizable(false);
        marcoVentana.setContentPane(pantalla);
        marcoVentana.setVisible(true);

        Rectangle bounds = pantalla.getBounds();
        lienzoAnchura = bounds.width;
        lienzoAltura = bounds.height;
        lienzo = new BufferedImage((int)lienzoAnchura, (int)lienzoAltura, BufferedImage.TYPE_INT_RGB);
        fg = (Graphics2D)lienzo.getGraphics();

        camara = new AffineTransform(0,0,1,0,0,1);
        setCamara(0, 0, 20);
        fg.setTransform(camara);

        setSuavizado(false);

        marcoVentana.addKeyListener(new LectorTeclas());

        crearImagenNoEncontrada();
        iniciaFontRenderContext();

    }

    /**
     * <p>Activa o desactiva el suavizado de los gráficos en pantalla. Esta función también puede
     * activarse o desactivarse pulsando la tecla F2 cuando la ventana está abierta</p>
     *
     * <p>NOTA: activar el suavizado consume más recursos de tu ordenador, por lo que tenerlo activado
     * en un ordenador antiguo y/o poco pontente puede hacer que algunos programas vayan más lentos
     * (o más bruscos)</p>
     *
     * @param suavizado
     */
    public void setSuavizado(boolean suavizado) {
        if(suavizado) {
            fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
    }

    private void crearImagenNoEncontrada() {
        final int red = 0xff0000, white = 0xffffff, blue = 0xff, gray = 0x888888, black = 0;
        char[][] img = {
                "bb   ,,,,,,     ".toCharArray(),
                "b    , ,,,,     ".toCharArray(),
                "     , ,,,,     ".toCharArray(),
                "     ,,,,,,     ".toCharArray(),
                "   x x    x x   ".toCharArray(),
                "    x      x    ".toCharArray(),
                "   x x    x x   ".toCharArray(),
                "    ...x....    ".toCharArray(),
                "   ....xx....   ".toCharArray(),
                "   ..........   ".toCharArray(),
                "   ...xxxx...   ".toCharArray(),
                "   ..x.xx.x..   ".toCharArray(),
                " b .x......x.   ".toCharArray(),
                " b ..........   ".toCharArray(),
                "   ..........   ".toCharArray(),
        };

        imagenNoEncontrada = new BufferedImage(img[0].length, img.length, BufferedImage.TYPE_INT_RGB);
        Graphics g = imagenNoEncontrada.getGraphics();
        for(int y = 0 ; y < img.length ; y++) {
            for (int x = 0; x < img[y].length; x++) {
                int col;
                switch (img[y][x]) {
                    case ',':
                        col = gray;
                        break;
                    case ' ':
                        col = blue;
                        break;
                    case 'x':
                        col = red;
                        break;
                    case '.':
                        col = white;
                        break;
                    case 'b':
                    default:
                        col = black;
                }
                g.setColor(new Color(col));
                g.drawRect(x,y,1,1);
            }
        }


    }

    private Image imagen(String archivo) {
        BufferedImage img = cacheImagenes.get(archivo);
        if(img == null) {
            try {
                img = ImageIO.read(getClass().getResourceAsStream(archivo));
            } catch (Exception e) {
                try {
                    img = ImageIO.read(new FileInputStream(archivo));
                } catch (Exception e1) {
                    System.err.println("No se ha podido leer la imagen " + archivo);
                    System.err.println(e.getMessage());
                    System.err.println(e1.getMessage());
                    img = imagenNoEncontrada;
                }
            }
            cacheImagenes.put(archivo, img);
        }
        return img;
    }

    /**
     * Dibuja una imagen animada a partir de un archivo de imagen en el cual los
     * diferentes fotogramas están dibujados en horizontal, siendo todos del
     * mismo tamaño.
     * 
     * @param archivoImagen Ruta y nombre del archivo que contiene la imagen a mostrar
     * @param izquierda Coordenada del lado más a la izquierda del rectángulo
     *                  sobre el cual se mostrará la imagen
     * @param arriba Coordenada del lado superior del rectángulo sobre el cual
     *               se mostrará la imagen
     * @param ancho Anchura de la imagen mostrada
     * @param alto Altura de la imagen mostrada
     * @param fotogramas Número de fotogramas que tiene la animación
     * @param msFotograma Milisegundos durante los cuales cada fotograma se dibujará,
     *                    antes de mostrarse el siguiente fotograma.
     */
    public void dibujaAnimacion(String archivoImagen, double izquierda, double arriba, double ancho,
                                    double alto, int fotogramas, long msFotograma) {
        BufferedImage img = (BufferedImage) imagen(archivoImagen);

        AffineTransform posicion = new AffineTransform();

        long fotogramaActual = (lastFrameTimeNanos % (fotogramas * msFotograma * 1000000)) / (msFotograma * 1000000);
        int anchoFotograma = img.getWidth(null) / fotogramas;

        double correccionVert  = - alto / img.getHeight(null);

        double transX = izquierda - fotogramaActual * ancho;
        double transY = arriba + correccionVert;
        posicion.translate(transX, transY);
        double scaleX = ancho * fotogramas / img.getWidth(null);
        double scaleY = correccionVert;
        posicion.scale(scaleX, scaleY);

        Shape clipAnterior = fg.getClip();

        fg.setColor(Color.RED);
        Rectangle2D.Double nuevoClip = new Rectangle2D.Double(izquierda, arriba - alto, ancho, alto);
//        fg.draw(nuevoClip);
        fg.setClip(nuevoClip);

        fg.drawImage(img, posicion, null);
        fg.setClip(clipAnterior);

    }

    /**
     * Dibuja en pantalla una imagen de un archivo. La mostrará ajustada al
     * rectángulo delimitado por según las coordenadas de una esquina superior izquierda,
     * anchura y altura.
     *
     * @param archivo Ruta y nombre del archivo que contiene la imagen a mostrar
     * @param izquierda Coordenada del lado más a la izquierda del rectángulo
     *                  sobre el cual se mostrará la imagen
     * @param arriba Coordenada del lado superior del rectángulo sobre el cual
     *               se mostrará la imagen
     * @param ancho Anchura de la imagen mostrada
     * @param alto Altura de la imagen mostrada
     */
    public void dibujaImagen(String archivo, double izquierda, double arriba, double ancho, double alto) {
        Image img = imagen(archivo);

        AffineTransform posicion = new AffineTransform();

        double correccionAlto  = - alto / img.getHeight(null);
        posicion.translate(izquierda, arriba + correccionAlto);
        posicion.scale(ancho / img.getWidth(null), + correccionAlto);
        fg.drawImage(img, posicion, null);
    }


    /**
     * Cambia las coordenadas y el campo de visi&oacute;n de la c&aacute;mara.
     * @param centroX Posici&oacute;n X donde apunta el centro de la c&aacute;mara
     * @param centroY Posici&oacute;n Y donde apunta el centro de la c&aacute;mara
     * @param tamanyoCampoVision El tama&ntilde; del eje menor que se ver&aacute; en pantalla (hay que contar que al no ser la ventana completamente cuadrada, se ver&aacute; m&aacute;s parte de un eje que del otro)
     */
    public final void setCamara(double centroX, double centroY, double tamanyoCampoVision) {
        camX = centroX; camY = centroY;

        if(lienzoAnchura > lienzoAltura) {
            aspectRatioH = lienzoAnchura / lienzoAltura;
            aspectRatioV = 1;
        } else {
            aspectRatioH = 1;
            aspectRatioV = lienzoAltura / lienzoAnchura;
        }
        campoVisionH = (tamanyoCampoVision * aspectRatioH);
        campoVisionV = (tamanyoCampoVision * aspectRatioV);

        double min = Math.min(lienzoAnchura / tamanyoCampoVision, lienzoAltura / tamanyoCampoVision);
        camara.setToIdentity();
        camara.translate(lienzoAnchura/2, lienzoAltura/2);
        camara.scale(min, -min);
        camara.translate(-centroX, -centroY);

        fg.setTransform(camara);

    }

    /**
     * Comprueba si la flecha "Arriba" del cursor está pulsada o no.
     * @return true si está pulsada. false en caso contrario.
     */
    public boolean isPulsadoArriba() {
        return up;
    }
    /**
     * Comprueba si la flecha "Abajo" del cursor está pulsada o no.
     * @return true si está pulsada. false en caso contrario.
     */
    public boolean isPulsadoAbajo() {
        return down;
    }
    /**
     * Comprueba si la flecha "Izquierda" del cursor está pulsada o no.
     * @return true si está pulsada. false en caso contrario.
     */
    public boolean isPulsadoIzquierda() {
        return left;
    }
    /**
     * Comprueba si la flecha "Derecha" del cursor está pulsada o no.
     * @return true si está pulsada. false en caso contrario.
     */
    public boolean isPulsadoDerecha() {
        return right;
    }

    /**
     * Comprueba si la tecla "Escape" está pulsada o no.
     * @return  true si Escape está pulsado. False en caso contrario.
     */
    public boolean isPulsadoEscape() {
        boolean esc = escPulsado;
        escPulsado = false;
        return esc;
    }

    /**
     * Comprueba si la barra espaciadora está pulsada o no.
     * <b>NOTA:</b> a diferencia de los cursores, la barra espaciadora debe
     * soltarse y volver a pulsarse para que la función devuelva "true" dos veces.
     * @return true si está pulsada. false en caso contrario.
     */
    public boolean isPulsadoEspacio() {
        if(space) {
            space = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Muestra una cuadrícula con las coordenadas de la escena. Esta funcionalidad
     * puede ser útil en la etapa de desarrollo del programa, para ayudarnos
     * a colocar los diferentes objetos en pantalla.
     *
     * <p>También puedes habilitar o deshabilitar la cuadrícula pulsando la tecla
     * F1 cuando la ventana está abierta y actualizándose.
     *
     * @param dibujaCoordenadas true si se quiere mostrar la cuadricula; false en caso contrario
     */
    public void setDibujaCoordenadas(boolean dibujaCoordenadas) {
        this.dibujaCoordenadas = dibujaCoordenadas;
    }

    private void dibujaCoordenadas() {
        synchronized (fg) {
            Stroke last = fg.getStroke();

            float cv = (float) Math.min(campoVisionH, campoVisionV);
            Stroke flojo = new BasicStroke((float) (0.5 / cv), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{1.0f / cv, 5.0f / cv}, 0.0f);
            Stroke medio = new BasicStroke((float) (1 / cv), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4.0f / cv, 5.0f / cv}, 0.0f);
            Stroke gordo = new BasicStroke((float) (1.3 / cv), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6f / cv, 2f / cv}, 0.0f);
            fg.setColor(Color.white);
            Line2D.Double l = new Line2D.Double();
            for (int x = (int) (camX - campoVisionH / 2); x <= (int) (camX + campoVisionH / 2); x++) {
                if (x % 10 == 0) {
                    fg.setStroke(gordo);
                    escribeTexto(String.valueOf(x), x + 0.1, camY + campoVisionV / 2 - 1.1, 1, Color.white);

                } else if (x % 5 == 0) {
                    fg.setStroke(medio);
                } else {
                    fg.setStroke(flojo);
                }
                l.setLine(x, camY - campoVisionV / 2, x, camY + campoVisionV / 2);
                fg.draw(l);
                //fg.drawLine((int)x, (int)( camY - campoVisionV / 2), (int)x, (int)(camY + campoVisionV / 2));
            }
            for (int y = (int) (camY - campoVisionV / 2); y <= (int) (camY + campoVisionV / 2); y++) {
                if (y % 10 == 0) {
                    fg.setStroke(gordo);
                    escribeTexto(String.valueOf(y), camX - campoVisionH / 2 + 0.1, y + 0.1, 1, Color.white);
                } else if (y % 5 == 0) {
                    fg.setStroke(medio);
                } else {
                    fg.setStroke(flojo);
                }
                l.setLine(camX - campoVisionH / 2, y, camX + campoVisionH / 2, y);
                fg.draw(l);
                //fg.drawLine((int)x, (int)( camY - campoVisionV / 2), (int)x, (int)(camY + campoVisionV / 2));
            }
            fg.setStroke(last);
        }
    }

    /**
     * Cierra la ventana.
     */
    public void cerrar() {
        marcoVentana.dispose();
    }

    private static final int TAMAÑO_FUENTE_BASE = 28;
    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, TAMAÑO_FUENTE_BASE);
    private FontRenderContext fontRenderContext = null;
    private void iniciaFontRenderContext() {
        fontRenderContext = marcoVentana.getFontMetrics(font).getFontRenderContext();
    }

    /**
     * Escribe un texto por pantalla.
     * @param texto El texto a escribir.
     * @param x Coordenada izquierda del inicio del texto.
     * @param y Coordenada superior del inicio del texto.
     * @param altoFuente Alto de la fuente
     * @param color Color del texto.
     */
    public void escribeTexto(String texto, double x, double y, double altoFuente, Color color) {
        String[] lineas = texto.split("\n");
        fg.setColor(color);

        for(int i = 0 ; i < lineas.length ; i++) {
            Shape txt = font.createGlyphVector(fontRenderContext, lineas[i]).getOutline();
            AffineTransform posicion = (AffineTransform) camara.clone();
            posicion.translate(x, y - i * INTERLINEADO);
            posicion.scale(altoFuente/TAMAÑO_FUENTE_BASE, -altoFuente/TAMAÑO_FUENTE_BASE);
            fg.setTransform(posicion);
            fg.fill(txt);
        }
        fg.setTransform(camara);
    }

    /**
     * Dibuja un triángulo, dadas tres coordenadas en píxeles y un color.
     * @param x1,y1 Coordenadas x,y del primer punto.
     * @param x2,y2 Coordenadas x,y del segundo punto.
     * @param x3,y3 Coordenadas x,y del tercer punto.
     * @param color Color del triángulo.
     */
    public void dibujaTriangulo(double x1, double y1, double x2, double y2, double x3, double y3, Color color){
        fg.setColor(color);
        Path2D.Double t = new Path2D.Double();

        t.moveTo(x1, y1);
        t.lineTo(x2, y2);
        t.lineTo(x3, y3);
        t.lineTo(x1, y1);
        fg.fill(t);
    }

    /**
     * Dibuja un segmento de línea entre los puntos (x1,y2) y (x2, y2), con un grosor y un color dados.
     * @param x1,y1 Coordenadas x,y del primer punto
     * @param x2,y2 Coordenadas x,y del segundo punto
     * @param grosor Grosor de la línea
     * @param color Color de la línea
     */
    public void dibujaLinea(double x1, double y1, double x2, double y2, double grosor, Color color) {
        Stroke last = fg.getStroke();
        fg.setColor(color);
        fg.setStroke(new BasicStroke((float)grosor,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
        Line2D.Double l = new Line2D.Double();
        l.setLine(x1, y1, x2, y2);
        fg.draw(l);
        fg.setStroke(last);
    }
    /**
     * Dibuja un rectángulo en pantalla, dadas las coordenadas de su esquina superior izquierda,
     * su anchura y su altura.
     *
     * @param izquierda Coordenada del lado más a la izquierda del rectángulo.
     * @param arriba Coordenada del lado superior del rectángulo.
     * @param ancho Anchura del rectángulo.
     * @param alto Altura del rectángulo.
     * @param color Color del rectángulo.
     */
    public void dibujaRectangulo(double izquierda, double arriba, double ancho, double alto, Color color) {
        fg.setColor(color);
        fg.fill(new Rectangle2D.Double(izquierda, arriba - alto, ancho, alto));
    }

    /**
     * Dibuja un círculo por pantalla.
     * @param centroX Coordenada X del centro del círculo
     * @param centroY Coordenada Y del centro del círculo
     * @param radio Radio del círculo
     * @param color Color del círculo
     */
    public void dibujaCirculo(double centroX, double centroY, double radio, Color color) {
        fg.setColor(color);
        fg.fill(new Ellipse2D.Double((centroX - radio), (centroY - radio), (radio*2),(radio*2)));
    }

    /**
     * Muestra el fotograma actual por pantalla. Además, crea un nuevo fotograma oculto
     * sobre el que se irá dibujando, y que se mostrará en la siguiente llamada al método
     * actualizaFotograma().
     */
    public void actualizaFotograma() {
        //dibuja cuadricula
        if(dibujaCoordenadas) dibujaCoordenadas();

        // muestra el buffer
        marcoVentana.repaint();

        espera();

        //borra el buffer
        fg.setTransform(identity);
        fg.setColor(colorFondo);
        fg.fillRect(0, 0, (int)lienzoAnchura, (int)lienzoAltura);
        fg.setTransform(camara);



    }

    /**
     * Cambia el color del fondo de la ventana.
     *
     * @param colorFondo Color del fondo de la ventana.
     */
    public void setColorFondo(Color colorFondo) {
        this.colorFondo = colorFondo;
    }



    private AffineTransform identity = new AffineTransform();

    private long lastFrameTimeNanos = 0;

    private void espera() {
        long now = System.nanoTime();
        try {
            long sleepTime = (1000000000 / FOTOGRAMAS_SEGUNDO) - (now - lastFrameTimeNanos);
            if(sleepTime <= 0) {
                Thread.yield();
            } else {
                Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
        }
        lastFrameTimeNanos = System.nanoTime();
    }

    private class ControladorVentana implements WindowListener {
        public void windowClosed(WindowEvent e) {
        }
        public void windowActivated(WindowEvent e) {}
        public void windowClosing(WindowEvent e) {
            cerrar();
        }
        public void windowDeactivated(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowOpened(WindowEvent e) {}
    }

    private class LectorTeclas implements KeyListener {
        private boolean spaceReleased = true;

        public void keyTyped(KeyEvent e) {

        }

        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    up = true;
                    break;
                case KeyEvent.VK_DOWN:
                    down = true;
                    break;
                case KeyEvent.VK_LEFT:
                    left = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    right = true;
                    break;
                case KeyEvent.VK_SPACE:
                    if (spaceReleased) {
                        space = true;
                    }
                    spaceReleased = false;
                    break;
                case KeyEvent.VK_ESCAPE:
                    escPulsado = true;
                    break;
                case KeyEvent.VK_F1:
                    dibujaCoordenadas = !dibujaCoordenadas;
                    break;
                case KeyEvent.VK_F2:
                    synchronized (fg) {
                        if (fg.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON) {
                            setSuavizado(false);
                        } else {
                            setSuavizado(true);
                        }
                    }
            }
        }

        public void keyReleased(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    up = false;
                    break;
                case KeyEvent.VK_DOWN:
                    down = false;
                    break;
                case KeyEvent.VK_LEFT:
                    left = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    right = false;
                    break;
                case KeyEvent.VK_SPACE:
                    spaceReleased = true;
                    space = false;
                    break;
            }
        }

    }

    /**
     * Devuelve true si el bot&oacute;n izquierdo del rat&oacute;n est&aacute; pulsado.
     * @return true si el bot&oacute;n izquierdo del rat&oacute;n est&aacute; pulsado.
     */
    public boolean isRatonPulsado() {
        if(ratonPulsado) {
            ratonPulsado = false;
            return true;
        }
        return false;
    }

    /**
     * Devuelve la coordenada X del rat&oacute;n
     * @return la coordenada X del rat&oacute;n
     */
    public double getRatonX() {
        return mouseEventPos == null ? 0 :
                ((double)mouseEventPos.getX() * campoVisionH) / lienzoAnchura - campoVisionH / 2 + camX;
    }

    /**
     * Devuelve la coordenada Y del rat&oacute;n
     * @return la coordenada Y del rat&oacute;n
     */
    public double getRatonY() {
        return mouseEventPos == null ? 0 :
                - ((double)mouseEventPos.getY() * campoVisionV) / lienzoAltura + campoVisionV / 2 + camY;
    }


    private MouseEvent mouseEventPos = null;
    private class LectorRaton implements MouseListener, MouseMotionListener {
        @Override
        public void mousePressed(MouseEvent me) {
            ratonPulsado = true;
        }
        @Override
        public void mouseReleased(MouseEvent me) {
            ratonPulsado = false;
        }
        @Override
        public void mouseMoved(MouseEvent me) {
            mouseEventPos = me;
        }
        @Override public void mouseDragged(MouseEvent me) {
            mouseEventPos = me;
        }

        @Override public void mouseEntered(MouseEvent me) { }
        @Override public void mouseExited(MouseEvent me) { }
        @Override public void mouseClicked(MouseEvent me) {}
    }
}