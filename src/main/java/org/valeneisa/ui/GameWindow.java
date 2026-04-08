package org.valeneisa.ui;

import org.valeneisa.core.BattleController;
import org.valeneisa.core.Player;
import org.valeneisa.network.UdpManager;
import org.valeneisa.patterns.Protocolo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow extends JFrame {
    private Player localPlayer;
    private Player remotePlayer;
    private JLabel labelStatus;
    private JPanel panelJuego;
    private BattleController battle; // El cerebro que conectamos desde el Main

    // Constructor actualizado para recibir el nombre y el controlador
    public GameWindow(String nombreLocal, BattleController battle) {
        this.localPlayer = new Player(nombreLocal, "localhost");
        this.remotePlayer = new Player("Enemigo", "0.0.0.0");
        this.battle = battle; // Guardamos la lógica de batalla

        configurarVentana(nombreLocal);
        agregarEventosMouse();

        setVisible(true);
    }

    private void configurarVentana(String nombre) {
        setTitle("Celestial Fury - Jugador: " + nombre);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Mostramos la vida inicial y de quién es el turno
        String turnoInicial = battle.isMiTurno() ? "Tu Turno" : "Turno Enemigo";
        labelStatus = new JLabel("Vida: " + battle.getMiVida() + " | " + turnoInicial, SwingConstants.CENTER);
        labelStatus.setFont(new Font("Consolas", Font.BOLD, 18));
        labelStatus.setForeground(Color.WHITE);

        JPanel panelNorte = new JPanel();
        panelNorte.setBackground(Color.BLACK);
        panelNorte.add(labelStatus);
        add(panelNorte, BorderLayout.NORTH);

        panelJuego = new JPanel();
        panelJuego.setBackground(Color.DARK_GRAY);
        add(panelJuego, BorderLayout.CENTER);
    }

    private void agregarEventosMouse() {
        panelJuego.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Al hacer clic, ejecutamos un ataque a través del controlador
                if (battle.isMiTurno()) {
                    battle.realizarAtaque(10, "Golpe_Basico");
                    actualizarInterfaz();
                } else {
                    JOptionPane.showMessageDialog(null, "¡Espera tu turno!");
                }
            }
        });
    }

    // Método para refrescar los textos cuando algo cambia
    public void actualizarInterfaz() {
        SwingUtilities.invokeLater(() -> {
            String turno = battle.isMiTurno() ? "Tu Turno" : "Turno Enemigo";
            labelStatus.setText("Vida: " + battle.getMiVida() + " | " + turno);

            if (battle.getMiVida() <= 0) {
                labelStatus.setText("☠️ HAS PERDIDO ☠️");
                labelStatus.setForeground(Color.RED);
            }
        });
    }
}