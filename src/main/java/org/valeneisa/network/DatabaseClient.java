package org.valeneisa.network;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Cliente para la comunicación con el backend PHP y MySQL.
 * Se encarga de la persistencia y el descubrimiento de pares (P2P).
 */
public class DatabaseClient {

    // Cambiar a la IP de la máquina que tenga el XAMPP si no es la local HEAD
    private static final String BASE_URL = "http://localhost/celestial_fury/";    /**
=======
    private static final String BASE_URL = "http://192.168.1.7/celestial_fury/";;
    /**
>>>>>>> origin/feature/frontend-css-fixes
     * Registra al jugador actual en la base de datos de forma asíncrona.
     */
    public void registrarJugador(String nombre, String ip) {
        new Thread(() -> {
            try {
                String urlString = BASE_URL + "registrar_jugador.php?nombre=" + nombre + "&ip=" + ip;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner sc = new Scanner(conn.getInputStream());
                if (sc.hasNext()) {
                    System.out.println("DB dice: " + sc.nextLine());
                }
                sc.close();
            } catch (Exception e) {
                System.err.println(" No se pudo registrar en la DB: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Busca en la base de datos la IP de otro jugador conectado.
     * @param miNombre El nombre del jugador actual para no autonecontarse.
     * @return La IP del oponente o "0.0.0.0" si no hay nadie.
     */
    public String obtenerIpEnemigo(String miNombre) {
        try {
            String urlString = BASE_URL + "obtener_rival.php?miNombre=" + miNombre;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Establecemos un tiempo de espera corto para no congelar el juego
            conn.setConnectTimeout(3000);

            Scanner sc = new Scanner(conn.getInputStream());
            if (sc.hasNextLine()) {
                String respuesta = sc.nextLine().trim();
                // Limpiamos cualquier rastro del mensaje "Conectado con éxito" si el PHP lo envía
                String ipEnemigo = respuesta.replace("Conectado con éxito", "").trim();
                return ipEnemigo;
            }
            sc.close();
        } catch (Exception e) {
            System.err.println("❌ No se encontró rival en la red: " + e.getMessage());
        }
        return "0.0.0.0";
    }
}