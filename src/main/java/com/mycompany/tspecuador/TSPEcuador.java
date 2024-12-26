import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TSPEcuador extends JFrame {

    private BufferedImage originalMapImage;
    private BufferedImage scaledMapImage;
    private ArrayList<Point> selectedProvinces = new ArrayList<>();
    private ArrayList<String> selectedProvinceNames = new ArrayList<>();
    private ArrayList<String> targetProvinces = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();
    private int currentLevel = 1;
    private int lives = 3;
    private JLabel infoLabel;
    private JLabel routeLabel;
    private JLabel distanceLabel;

    private final String[] provinces = {
            "Carchi", "Esmeraldas", "Imbabura", "Pichincha", "Cotopaxi", "Los Ríos", 
            "Guayas", "Manabí", "El Oro", "Azuay", "Loja", "Zamora Chinchipe", 
            "Morona Santiago", "Chimborazo", "Cañar", "Bolívar", "Tungurahua", 
            "Pastaza", "Orellana", "Napo", "Sucumbíos", "Santo Domingo", "Santa Elena", "Galápagos"
    };

    private final Point[] originalProvinceCoords = {
            new Point(843, 240),   // Carchi
            new Point(520, 245),  // Esmeraldas
            new Point(742, 300),  // Imbabura
            new Point(700, 437),  // Pichincha
            new Point(620, 611),  // Cotopaxi
            new Point(457, 726),  // Los Ríos
            new Point(358, 859),  // Guayas
            new Point(300, 600),  // Manabí
            new Point(395, 1221),  // El Oro
            new Point(555, 1111),  // Azuay
            new Point(438, 1366),  // Loja
            new Point(627, 1331),  // Zamora Chinchipe
            new Point(825, 1019),  // Morona Santiago
            new Point(658, 833),  // Chimborazo
            new Point(600, 990),  // Cañar
            new Point(572, 754),  // Bolívar
            new Point(705, 702),  // Tungurahua
            new Point(1075, 807),  // Pastaza
            new Point(1260, 600),  // Orellana
            new Point(840, 555),  // Napo
            new Point(1177, 709),  // Sucumbíos
            new Point(535, 467),  // Santo Domingo de los Tsáchilas
            new Point(220, 905),  // Santa Elena
            new Point(1290, 1340)   // Galápagos 
    };

    
    private final double[][] distanceMatrix = {
    { 0, 220, 150, 200, 260, 300, 350, 310, 450, 470 },
    { 220, 0, 170, 180, 240, 160, 280, 220, 400, 420 },
    { 150, 170, 0, 80, 140, 190, 240, 230, 380, 400 },
    { 200, 180, 80, 0, 60, 110, 170, 160, 310, 330 },
    { 260, 240, 140, 60, 0, 100, 160, 150, 300, 320 },
    { 300, 160, 190, 110, 100, 0, 120, 100, 250, 270 },
    { 350, 280, 240, 170, 160, 120, 0, 120, 200, 230 },
    { 310, 220, 230, 160, 150, 100, 120, 0, 180, 200 },
    { 450, 400, 380, 310, 300, 250, 200, 180, 0, 80 },
    { 470, 420, 400, 330, 320, 270, 230, 200, 80, 0 }
    };

    private Point[] scaledProvinceCoords;

    public TSPEcuador() {
        setTitle("Agente Viajero en Ecuador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mostrarInstrucciones();

        try {
            originalMapImage = ImageIO.read(new File("5815650c03e5f32d9f8ddf90ced36a45.png"));
            scaledMapImage = originalMapImage;

            scaledProvinceCoords = new Point[originalProvinceCoords.length];
            for (int i = 0; i < originalProvinceCoords.length; i++) {
                scaledProvinceCoords[i] = new Point(originalProvinceCoords[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (scaledMapImage != null) {
                    g.drawImage(scaledMapImage, 0, 0, getWidth(), getHeight(), this);
                }
                g.setColor(Color.RED);
                for (Point p : selectedProvinces) {
                    g.fillOval(p.x - 8, p.y - 8, 16, 16);
                }

                g.setColor(Color.BLUE);
                for (int i = 0; i < selectedProvinces.size() - 1; i++) {
                    Point p1 = selectedProvinces.get(i);
                    Point p2 = selectedProvinces.get(i + 1);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);

                    int midX = (p1.x + p2.x) / 2;
                    int midY = (p1.y + p2.y) / 2;
                    double distance = calculateDistanceBetweenProvinces(selectedProvinceNames.get(i), selectedProvinceNames.get(i + 1));
                    g.drawString(String.format("%.2f", distance), midX, midY);
                }
            }
        };

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeMapAndCoordinates(mapPanel.getWidth(), mapPanel.getHeight());
                mapPanel.repaint();
            }
        });

        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickedPoint = e.getPoint();
                for (int i = 0; i < scaledProvinceCoords.length; i++) {
                    Point provincePoint = scaledProvinceCoords[i];
                    if (clickedPoint.distance(provincePoint) < 50) {
                        selectedProvinces.add(provincePoint);
                        selectedProvinceNames.add(provinces[i]);
                        mostrarMensaje("Provincia seleccionada: " + provinces[i]);
                        repaint();
                        break;
                    }
                }
            }
        });

        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        routeLabel = new JLabel();
        routeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        distanceLabel = new JLabel("Distancia: 0");
        distanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        actualizarBarraDeInformacion();

        JButton calcularButton = new JButton("Calcular");
        calcularButton.addActionListener(e -> verificarRespuesta());

        JButton pistaButton = new JButton("Pista");
        pistaButton.addActionListener(e -> mostrarPista());

        JButton borrarButton = new JButton("Borrar Puntos");
        borrarButton.addActionListener(e -> borrarPuntos());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(pistaButton);
        buttonsPanel.add(borrarButton);
        buttonsPanel.add(calcularButton);

        controlPanel.add(infoLabel, BorderLayout.NORTH);
        controlPanel.add(routeLabel, BorderLayout.CENTER);
        controlPanel.add(distanceLabel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        iniciarNivel();
    }

    private void mostrarInstrucciones() {
        String instrucciones = "¡Bienvenido al juego del Agente Viajero en Ecuador!\n\n" +
                               "Instrucciones:\n" +
                               "1. El juego consta de 3 niveles. En cada nivel debes seleccionar el camino más corto entre las provincias.\n" +
                               "2. Comenzarás con 3 vidas. Pierdes una vida si seleccionas el camino incorrecto.\n" +
                               "3. Debes hacer clic en las provincias en el orden correcto. Si completas el nivel correctamente, avanzarás al siguiente.\n" +
                               "4. Si pierdes todas tus vidas, el juego termina.\n" +
                               "5. Después de seleccionar las provincias, haz clic en 'Calcular' para verificar si has seleccionado correctamente.\n\n" +
                               "¡Buena suerte!";
        JOptionPane.showMessageDialog(this, instrucciones, "Instrucciones", JOptionPane.INFORMATION_MESSAGE);
    }

    private void iniciarNivel() {
        selectedProvinces.clear();
        selectedProvinceNames.clear();
        targetProvinces.clear();
        distances.clear();

        if (currentLevel > 3) {
            mostrarMensaje("¡Felicidades! Has completado todos los niveles.");
            return;
        }

        mostrarMensaje("Nivel " + currentLevel + " - Selecciona el camino más corto.");

        calcularRutaMasCorta();

        actualizarBarraDeInformacion();
    }

    private void calcularRutaMasCorta() {
        Set<Integer> visitados = new HashSet<>();
        Random rand = new Random();
        int[] selectedProvinceIndexes = new int[5];  

        // Selecciona 5 provincias aleatoriamente dentro del rango permitido de la matriz de distancia.
        for (int i = 0; i < 5; i++) {
            int index;
            do {
                index = rand.nextInt(10);  
            } while (visitados.contains(index));
            visitados.add(index);
            selectedProvinceIndexes[i] = index;
        }

        
        int current = selectedProvinceIndexes[0];
        targetProvinces.add(provinces[current]);

        // Algoritmo para calcular la ruta más corta
        while (targetProvinces.size() < 5) {
            int nearest = -1;
            double minDistance = Double.MAX_VALUE;
            for (int i : selectedProvinceIndexes) {
                if (!targetProvinces.contains(provinces[i])) {
                    double distance = distanceMatrix[current][i];
                    if (distance < minDistance) {
                        nearest = i;
                        minDistance = distance;
                    }
                }
            }
            if (nearest != -1) {
                current = nearest;
                targetProvinces.add(provinces[current]);
            }
        }

        
        targetProvinces.add(targetProvinces.get(0));

        mostrarRutaEnPantalla();
    }

    private void mostrarRutaEnPantalla() {
        String ruta = "Ruta: " + String.join(" -> ", targetProvinces);
        routeLabel.setText(ruta);  // Mostrar la ruta en el JLabel debajo del mapa
    }

    private void mostrarPista() {
        if (!targetProvinces.isEmpty() && selectedProvinces.size() < targetProvinces.size()) {
            String nextProvince = targetProvinces.get(selectedProvinces.size());
            mostrarMensaje("Siguiente provincia: " + nextProvince);
        }
    }

    private void borrarPuntos() {
        selectedProvinces.clear();
        selectedProvinceNames.clear();
        repaint();
        mostrarMensaje("Puntos borrados, intenta de nuevo.");
    }

    private double calculateDistanceBetweenProvinces(String province1, String province2) {
        int index1 = java.util.Arrays.asList(provinces).indexOf(province1);
        int index2 = java.util.Arrays.asList(provinces).indexOf(province2);
        if (index1 >= 0 && index2 >= 0) {
            return distanceMatrix[index1][index2];
        }
        return 0;
    }

    private void verificarRespuesta() {
        if (selectedProvinces.size() != targetProvinces.size()) {
            mostrarMensaje("Debes seleccionar exactamente " + targetProvinces.size() + " provincias.");
            return;
        }

        double totalDistance = 0;
        for (int i = 0; i < selectedProvinceNames.size() - 1; i++) {
            double distance = calculateDistanceBetweenProvinces(selectedProvinceNames.get(i), selectedProvinceNames.get(i + 1));
            distances.add(distance);
            totalDistance += distance;
        }

        double correctDistance = 0;
        for (int i = 0; i < targetProvinces.size() - 1; i++) {
            correctDistance += calculateDistanceBetweenProvinces(targetProvinces.get(i), targetProvinces.get(i + 1));
        }

        if (totalDistance == correctDistance) {
            mostrarRetroalimentacionVisual("¡Correcto! Has completado el nivel.");
            currentLevel++;
            iniciarNivel();
        } else {
            lives--;
            mostrarMensaje("Ruta incorrecta. Te quedan " + lives + " vidas.");
            if (lives <= 0) {
                mostrarMensaje("Has perdido todas tus vidas. ¡Juego terminado!");
                System.exit(0);
            }
            iniciarNivel();
        }

        actualizarBarraDeInformacion();
    }

    private void mostrarRetroalimentacionVisual(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "¡Bien hecho!", JOptionPane.INFORMATION_MESSAGE);

        Timer timer = new Timer(100, e -> {
            for (int i = 0; i < selectedProvinces.size(); i++) {
                Point p = selectedProvinces.get(i);
                Graphics g = getGraphics();
                g.setColor(Color.GREEN);
                g.fillOval(p.x - 8, p.y - 8, 16, 16);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void actualizarBarraDeInformacion() {
        infoLabel.setText("Nivel: " + currentLevel + " | Vidas: " + lives);
    }

    private void resizeMapAndCoordinates(int newWidth, int newHeight) {
        scaledMapImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledMapImage.createGraphics();
        g2d.drawImage(originalMapImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        double xScale = (double) newWidth / originalMapImage.getWidth();
        double yScale = (double) newHeight / originalMapImage.getHeight();
        for (int i = 0; i < originalProvinceCoords.length; i++) {
            int newX = (int) (originalProvinceCoords[i].x * xScale);
            int newY = (int) (originalProvinceCoords[i].y * yScale);
            scaledProvinceCoords[i] = new Point(newX, newY);
        }
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TSPEcuador tspEcuador = new TSPEcuador();
            tspEcuador.setExtendedState(JFrame.MAXIMIZED_BOTH);
            tspEcuador.setVisible(true);
        });
    }
}
