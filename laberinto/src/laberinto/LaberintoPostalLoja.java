package laberinto;

import java.util.Random;

/**
 * Clase principal con la lógica del laberinto
 */
public class LaberintoPostalLoja {

    private static final Random random = new Random();

    /**
     * Clase que representa un punto del laberinto
     */
    static class Punto {
        int fila;
        int columna;
        Punto padre;

        public Punto(int fila, int columna, Punto padre) {
            this.fila = fila;
            this.columna = columna;
            this.padre = padre;
        }

        public Punto opuesto() {
            if (this.padre == null) return null;

            if (this.fila != padre.fila) {
                return new Punto(this.fila + (this.fila > padre.fila ? 1 : -1), this.columna, this);
            }
            if (this.columna != padre.columna) {
                return new Punto(this.fila, this.columna + (this.columna > padre.columna ? 1 : -1), this);
            }
            return null;
        }
    }

    /**
     * Lista dinámica de puntos
     */
    static class ListaPuntos {
        private Punto[] puntos;
        private int tamaño;

        public ListaPuntos(int capacidad) {
            puntos = new Punto[capacidad];
            tamaño = 0;
        }

        public void agregar(Punto p) {
            if (tamaño == puntos.length) {
                Punto[] nuevo = new Punto[puntos.length * 2];
                System.arraycopy(puntos, 0, nuevo, 0, puntos.length);
                puntos = nuevo;
            }
            puntos[tamaño++] = p;
        }

        public Punto eliminarAleatorio() {
            int indice = random.nextInt(tamaño);
            Punto p = puntos[indice];
            System.arraycopy(puntos, indice + 1, puntos, indice, tamaño - indice - 1);
            tamaño--;
            return p;
        }

        public boolean estaVacia() {
            return tamaño == 0;
        }
    }

    /**
     * Cola simple para el recorrido tipo Dijkstra
     */
    static class ColaNodos {
        private Punto[] elementos;
        private int frente;
        private int fin;

        public ColaNodos(int capacidad) {
            elementos = new Punto[capacidad];
            frente = 0;
            fin = 0;
        }

        public void encolar(Punto p) {
            elementos[fin++] = p;
        }

        public Punto desencolar() {
            return elementos[frente++];
        }

        public boolean estaVacia() {
            return frente == fin;
        }
    }

    /**
     * Genera un laberinto aleatorio marcando paredes ('0') y caminos ('1').
     * 'S' es el inicio, 'E' el final.
     */
    public static char[][] generarLaberinto(int filas, int columnas) {
        char[][] laberinto = new char[filas][columnas];

        // Inicializar con paredes
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                laberinto[i][j] = '0';
            }
        }

        // Punto inicial aleatorio
        Punto inicio = new Punto(random.nextInt(filas), random.nextInt(columnas), null);
        laberinto[inicio.fila][inicio.columna] = 'S';

        ListaPuntos frontera = new ListaPuntos(filas * columnas);

        // Vecinos iniciales
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int x = inicio.fila + dx[i];
            int y = inicio.columna + dy[i];
            if (x >= 0 && x < filas && y >= 0 && y < columnas) {
                frontera.agregar(new Punto(x, y, inicio));
            }
        }

        Punto ultimo = null;
        while (!frontera.estaVacia()) {
            Punto actual = frontera.eliminarAleatorio();
            Punto opuesto = actual.opuesto();

            if (opuesto == null) continue;

            try {
                if (laberinto[actual.fila][actual.columna] == '0' &&
                    laberinto[opuesto.fila][opuesto.columna] == '0') {

                    laberinto[actual.fila][actual.columna] = '1';
                    laberinto[opuesto.fila][opuesto.columna] = '1';
                    ultimo = opuesto;

                    for (int i = 0; i < 4; i++) {
                        int x = opuesto.fila + dx[i];
                        int y = opuesto.columna + dy[i];
                        if (x >= 0 && x < filas && y >= 0 && y < columnas && laberinto[x][y] == '0') {
                            frontera.agregar(new Punto(x, y, opuesto));
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar errores fuera de límites
            }
        }

        if (ultimo != null) {
            laberinto[ultimo.fila][ultimo.columna] = 'E';
        } else {
            laberinto[filas - 1][columnas - 1] = 'E';
        }

        return laberinto;
    }

    /**
     * Resuelve el laberinto utilizando una variante de Dijkstra
     */
    public static void resolverLaberinto(char[][] laberinto) {
        int filas = laberinto.length;
        int columnas = laberinto[0].length;

        int inicioFila = 0, inicioCol = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (laberinto[i][j] == 'S') {
                    inicioFila = i;
                    inicioCol = j;
                    break;
                }
            }
        }

        int[][] distancias = new int[filas][columnas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                distancias[i][j] = Integer.MAX_VALUE;
            }
        }
        distancias[inicioFila][inicioCol] = 0;

        ColaNodos cola = new ColaNodos(filas * columnas);
        cola.encolar(new Punto(inicioFila, inicioCol, null));

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Punto fin = null;

        while (!cola.estaVacia()) {
            Punto actual = cola.desencolar();

            if (laberinto[actual.fila][actual.columna] == 'E') {
                fin = actual;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int x = actual.fila + dx[i];
                int y = actual.columna + dy[i];

                if (x >= 0 && x < filas && y >= 0 && y < columnas &&
                    laberinto[x][y] != '0') {

                    int nuevaDist = distancias[actual.fila][actual.columna] + 1;

                    if (nuevaDist < distancias[x][y]) {
                        distancias[x][y] = nuevaDist;
                        cola.encolar(new Punto(x, y, actual));
                    }
                }
            }
        }

        // Marcar camino con '.'
        if (fin != null) {
            Punto actual = fin.padre;
            while (actual != null && actual.padre != null) {
                laberinto[actual.fila][actual.columna] = '.';
                actual = actual.padre;
            }
        }
    }
}
