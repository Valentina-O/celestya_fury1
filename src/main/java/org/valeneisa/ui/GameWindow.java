package org.valeneisa.ui;

import org.valeneisa.core.BattleController;
import org.valeneisa.core.Habilidad;
import org.valeneisa.core.HabilidadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal de la interfaz gráfica de Celestial Fury.
 *
 * <p>Muestra el estado de la batalla y permite al jugador activar
 * habilidades mediante botones o clic directo en el área de juego.</p>
 *
 * <p>Principios SOLID aplicados:</p>
 * <ul>
 *   <li><b>S</b> — Solo gestiona la presentación visual;
 *       toda la lógica de combate vive en {@link BattleController}.</li>
 *   <li><b>O</b> — Nuevas habilidades se agregan en {@link HabilidadFactory}
 *       sin modificar esta clase.</li>
 *   <li><b>D</b> — Depende de {@link BattleController} como abstracción
 *       de la lógica de batalla.</li>
 * </ul>
 *
 * @author Celestial Fury Team
 * @version 1.0
 */
public class GameWindow extends JFrame {

    /** Controlador de batalla que gestiona la lógica del juego. */
    private final BattleController battle;

    /** Etiqueta superior que muestra vida y turno actuales. */
    private JLabel labelStatus;

    /** Panel central donde ocurre la acción visual del juego. */
    private JPanel panelJuego;

    /** Mapa de nombre de habilidad → botón correspondiente en la UI. */
    private final Map<String, JButton> botonesHabilidad = new HashMap<>();

    /** Habilidad de acceso rápido al hacer clic en el panel central. */
    private static final String HABILIDAD_RAPIDA = "Golpe_Basico";

    /**
     * Construye y muestra la ventana principal del juego.
     *
     * @param nombreLocal nombre del jugador local (se muestra en el título)
     * @param battle      controlador de batalla ya inicializado
     */
    public GameWindow(String nombreLocal, BattleController battle) {
        this.battle = battle;
        construirVentana(nombreLocal);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    //  Construcción de la ventana
    // ─────────────────────────────────────────────────────────

    /**
     * Inicializa y compone todos los paneles de la ventana.
     *
     * @param nombre nombre del jugador local
     */
    private void construirVentana(String nombre) {
        setTitle("Celestial Fury - Jugador: " + nombre);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(construirPanelEstado(), BorderLayout.NORTH);
        add(construirPanelJuego(),  BorderLayout.CENTER);
        add(construirPanelHabilidades(), BorderLayout.SOUTH);
    }

    /**
     * Construye el panel superior con la etiqueta de estado (vida y turno).
     *
     * @return panel configurado listo para añadir al frame
     */
    private JPanel construirPanelEstado() {
        labelStatus = new JLabel(obtenerTextoEstado(), SwingConstants.CENTER);
        labelStatus.setFont(new Font("Consolas", Font.BOLD, 18));
        labelStatus.setForeground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.add(labelStatus);
        return panel;
    }

    /**
     * Construye el panel central (área de juego) con el listener de mouse.
     *
     * @return panel configurado con evento de clic
     */
    private JPanel construirPanelJuego() {
        panelJuego = new JPanel();
        panelJuego.setBackground(Color.DARK_GRAY);
        panelJuego.addMouseListener(new MouseAdapter() {
            /**
             * Al hacer clic en el área de juego se activa la habilidad rápida.
             *
             * @param e evento de mouse recibido
             */
            @Override
            public void mousePressed(MouseEvent e) {
                ejecutarHabilidadRapida();
            }
        });
        return panelJuego;
    }

    /**
     * Construye el panel inferior con los botones de habilidades,
     * generados dinámicamente desde {@link HabilidadFactory}.
     *
     * @return panel de habilidades configurado
     */
    private JPanel construirPanelHabilidades() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.BLACK);

        List<Habilidad> habilidades = HabilidadFactory.crearHabilidades();
        for (Habilidad h : habilidades) {
            JButton btn = construirBotonHabilidad(h);
            botonesHabilidad.put(h.getNombre(), btn);
            panel.add(btn);
        }

        return panel;
    }

    // ─────────────────────────────────────────────────────────
    //  Botones de habilidad
    // ─────────────────────────────────────────────────────────

    /**
     * Construye un botón visual para una habilidad específica,
     * incluyendo su lógica de cooldown y activación.
     *
     * @param habilidad datos de la habilidad a representar
     * @return botón configurado y listo para usar
     */
    private JButton construirBotonHabilidad(Habilidad habilidad) {
        JButton btn = new JButton(habilidad.getEtiquetaUI());
        btn.setFont(new Font("Consolas", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(resolverColorHabilidad(habilidad.getNombre()));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 50));

        btn.addActionListener(e -> manejarClicHabilidad(habilidad, btn));

        return btn;
    }

    /**
     * Maneja el evento de clic sobre un botón de habilidad.
     *
     * <p>Valida el turno y el cooldown antes de ejecutar el ataque.</p>
     *
     * @param habilidad habilidad asociada al botón presionado
     * @param btn       botón que fue presionado
     */
    private void manejarClicHabilidad(Habilidad habilidad, JButton btn) {
        if (!battle.isMiTurno()) {
            JOptionPane.showMessageDialog(this, "¡Espera tu turno!");
            return;
        }

        if (!btn.isEnabled()) {
            return; // Está en cooldown, el timer lo maneja
        }

        battle.realizarAtaque(habilidad.getDanio(), habilidad.getNombre());
        actualizarInterfaz();

        if (habilidad.getCooldownSegundos() > 0) {
            activarCooldown(btn, habilidad);
        }
    }

    /**
     * Inicia la cuenta regresiva de cooldown para un botón de habilidad.
     *
     * <p>El botón queda deshabilitado y muestra los segundos restantes
     * hasta que el cooldown termina.</p>
     *
     * @param btn       botón a bloquear durante el cooldown
     * @param habilidad habilidad cuyo cooldown se activa
     */
    private void activarCooldown(JButton btn, Habilidad habilidad) {
        btn.setEnabled(false);
        final int[] restantes = {habilidad.getCooldownSegundos()};
        final String etiquetaOriginal = habilidad.getEtiquetaUI();

        Timer timer = new Timer(1000, null);
        timer.addActionListener(tick -> {
            restantes[0]--;
            if (restantes[0] <= 0) {
                btn.setText(etiquetaOriginal);
                btn.setEnabled(true);
                timer.stop();
            } else {
                btn.setText(etiquetaOriginal + " (" + restantes[0] + "s)");
            }
        });

        timer.start();
    }

    // ─────────────────────────────────────────────────────────
    //  Acciones
    // ─────────────────────────────────────────────────────────

    /**
     * Ejecuta la habilidad de acceso rápido (clic en el área de juego).
     *
     * <p>Muestra un mensaje si no es el turno del jugador.</p>
     */
    private void ejecutarHabilidadRapida() {
        if (!battle.isMiTurno()) {
            JOptionPane.showMessageDialog(this, "¡Espera tu turno!");
            return;
        }

        HabilidadFactory.crearHabilidades().stream()
                .filter(h -> h.getNombre().equals(HABILIDAD_RAPIDA))
                .findFirst()
                .ifPresent(h -> {
                    battle.realizarAtaque(h.getDanio(), h.getNombre());
                    actualizarInterfaz();
                });
    }

    // ─────────────────────────────────────────────────────────
    //  Actualización de UI
    // ─────────────────────────────────────────────────────────

    /**
     * Refresca la etiqueta de estado con la vida y turno actuales.
     *
     * <p>Si el jugador ha perdido, muestra un mensaje de derrota
     * y deshabilita todos los botones de habilidad.</p>
     *
     * <p>Debe llamarse desde cualquier hilo; usa
     * {@link SwingUtilities#invokeLater} internamente.</p>
     */
    public void actualizarInterfaz() {
        SwingUtilities.invokeLater(() -> {
            if (battle.getMiVida() <= 0) {
                labelStatus.setText("☠️ HAS PERDIDO ☠️");
                labelStatus.setForeground(Color.RED);
                deshabilitarTodosLosBotones();
            } else {
                labelStatus.setText(obtenerTextoEstado());
            }
        });
    }

    /**
     * Deshabilita todos los botones de habilidad (usado al perder).
     */
    private void deshabilitarTodosLosBotones() {
        botonesHabilidad.values().forEach(btn -> btn.setEnabled(false));
    }

    // ─────────────────────────────────────────────────────────
    //  Utilidades
    // ─────────────────────────────────────────────────────────

    /**
     * Genera el texto de estado actual: vida y turno del jugador.
     *
     * @return cadena formateada para mostrar en {@code labelStatus}
     */
    private String obtenerTextoEstado() {
        String turno = battle.isMiTurno() ? "Tu Turno" : "Turno Enemigo";
        return "Vida: " + battle.getMiVida() + " | " + turno;
    }

    /**
     * Resuelve el color del botón según el nombre de la habilidad.
     *
     * @param nombreHabilidad nombre de la habilidad
     * @return color asignado para la habilidad
     */
    private Color resolverColorHabilidad(String nombreHabilidad) {
        return switch (nombreHabilidad) {
            case "Llama" -> new Color(200, 80, 0);
            case "Rayo"  -> new Color(50, 50, 200);
            default      -> Color.GRAY;
        };
    }
}