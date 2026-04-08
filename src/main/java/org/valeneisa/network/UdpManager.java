package org.valeneisa.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;

public class UdpManager {
    private static UdpManager instance;
    private String ipDestino;
    private final int PUERTO = 9876;

    private UdpManager() {}

    public static UdpManager getInstance() {
        if (instance == null) {
            instance = new UdpManager();
        }
        return instance;
    }

    public void setIpDestino(String ip) {
        this.ipDestino = ip;
        System.out.println(" Objetivo fijado en: " + ip);
    }

    public void enviarMensaje(String mensaje) {
        if (ipDestino == null || ipDestino.isEmpty()) {
            System.err.println("❌ Error: No hay IP de destino.");
            return;
        }

        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] buffer = mensaje.getBytes();
                InetAddress address = InetAddress.getByName(ipDestino);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PUERTO);
                socket.send(packet);
                System.out.println(" Ataque enviado: " + mensaje);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void iniciarEscucha(Consumer<String> callback) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(PUERTO)) {
                byte[] buffer = new byte[1024];
                System.out.println("👂 Escuchando ataques en el puerto " + PUERTO + "...");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String recibido = new String(packet.getData(), 0, packet.getLength());
                    callback.accept(recibido);
                }
            } catch (Exception e) {
                System.err.println("❌ Error en escucha UDP: " + e.getMessage());
            }
        }).start();
    }
}