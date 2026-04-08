package org.valeneisa.core;

import org.valeneisa.network.DatabaseClient;
import org.valeneisa.network.UdpManager;
import org.valeneisa.ui.GameWindow;
import javax.swing.JOptionPane;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        // 1. Pedir nombre (Requisito 2.8)
        String nombre = JOptionPane.showInputDialog("¡Bienvenida a Celestial Fury!\nIngresa tu nombre:");

        // Si cancela o deja vacío, le damos un nombre por defecto
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Player_Valen";
        }

        try {
            // 2. Obtener IP local
            String miIp = InetAddress.getLocalHost().getHostAddress();

            // 3. Registrar en la Base de Datos (PHP + MariaDB)
            DatabaseClient db = new DatabaseClient();
            db.registrarJugador(nombre, miIp);
            System.out.println("🚀 Registrando a: " + nombre + " con IP: " + miIp);

            // 4. Búsqueda de Rival (Matchmaking)
            System.out.println("🔎 Buscando rival en la base de datos...");
            String ipRival = "0.0.0.0";
            int intentos = 0;

            while (ipRival.equals("0.0.0.0") && intentos < 10) {
                ipRival = db.obtenerIpEnemigo(nombre);

                if (ipRival.equals("0.0.0.0")) {
                    Thread.sleep(2000);
                    System.out.println("... sigo buscando rival (" + (intentos + 1) + "/10) ...");
                    intentos++;
                }
            }

            // 5. Configurar la red y el estado inicial de la batalla
            boolean empiezoYo = false;
            if (!ipRival.equals("0.0.0.0") && !ipRival.equals("no_hay_rivales")) {
                System.out.println("⚔️ ¡Rival encontrado! Su IP es: " + ipRival);
                UdpManager.getInstance().setIpDestino(ipRival);
                empiezoYo = true; // El que encuentra rival suele iniciar el ataque
            } else {
                System.out.println(" No hay nadie más conectado. Esperando retador...");
            }

            // 6. Inicializar el Controlador de Batalla (Cerebro del juego)
            BattleController battle = new BattleController(empiezoYo);

            // 7. Conectar la escucha UDP directamente al BattleController
            UdpManager.getInstance().iniciarEscucha(mensaje -> {
                System.out.println(" Red -> Recibido: " + mensaje);
                battle.procesarEntradaRival(mensaje);
            });

            // 8. Lanzar la interfaz gráfica pasando el controlador
            // NOTA: Asegúrate de que el constructor de GameWindow acepte (String, BattleController)
            new GameWindow(nombre, battle);

            System.out.println(" Todo el sistema de backend está corriendo. ¡Listo para la interfaz!");

        } catch (Exception e) {
            System.err.println(" Error crítico en el inicio:");
            e.printStackTrace();
        }
    }
}