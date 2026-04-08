package org.valeneisa.core;
import org.valeneisa.network.UdpManager;

public class BattleController {
    private int miVida = 100;
    private int vidaRival = 100;
    private boolean miTurno;

    public BattleController(boolean empiezoYo) {
        this.miTurno = empiezoYo;
    }

    // Lógica para cuando el usuario presiona un botón de la interfaz
    public void realizarAtaque(int danio, String nombreHabilidad) {
        if (miTurno) {
            // Enviamos el paquete por red
            UdpManager.getInstance().enviarMensaje("ATAQUE:" + danio + ":" + nombreHabilidad);
            miTurno = false; // Cambiamos el turno localmente
            System.out.println("Atacaste con " + nombreHabilidad + ". Ahora espera al rival.");
        } else {
            System.out.println("No es tu turno, ¡espera!");
        }
    }

    // Lógica para procesar lo que llega por red (UDP)
    public void procesarEntradaRival(String mensaje) {
        if (mensaje.startsWith("ATAQUE:")) {
            String[] partes = mensaje.split(":");
            int danio = Integer.parseInt(partes[1]);
            String habilidad = partes[2];

            this.miVida -= danio;
            this.miTurno = true; // El rival ya atacó, ahora es mi turno
            System.out.println("Recibiste " + danio + " de daño por " + habilidad);
            System.out.println(" Vida restante: " + miVida);
        }
    }

    // Getters para que la interfaz sepa qué números mostrar
    public int getMiVida() { return miVida; }
    public boolean isMiTurno() { return miTurno; }
}
