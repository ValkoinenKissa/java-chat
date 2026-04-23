package me.abollo.javachat.server;

import me.abollo.javachat.client.HiloCliente;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flujo:
 *   1. Abre un ServerSocket en el puerto 5000 y espera conexiones.
 *   2. Por cada cliente que entra, lanza un HiloCliente en un Thread separado.
 *   3. Mantiene una lista de todos los clientes conectados para hacer broadcast.
 * <p>
 *   - ServerSocket.accept() → bloquea hasta que llega un cliente
 *   - Un Thread por cliente → el servidor puede atender varios a la vez
 *   - synchronized → protege la lista compartida de accesos concurrentes
 */
public class Server {

    //Definimos el puerto

    static final int PUERTO = 5000;


    //Se rellena un diccionario con todos los clientes conectados, statica porque la lista es
    //compartida con todos los hilos de clientes

    static Map<String, PrintWriter> clientes = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Servidor iniciado en el puerto: " + PUERTO);


        //Creamos un socket que será el "enchufe" que escuche conexiones entrantes

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {


            while (true) {
                // accept bloquea aqui hasta que conecta alguien
                // devolvemos un socket normal para hablar con el cliente
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());

                //Lanzamos un hilo para este cliente y seguimos esperando mas:

                Thread hilo = new Thread(new HiloCliente(socketCliente));
                hilo.start();
            }

        }
    }

    //Enviamos un mensaje broadcast a todos los clientes conectados,
    // syncronized evita que dos hilos escriban la lista a la vez

    public static synchronized void broadcast(String msg) {
        System.out.println("[BROADCAST]: " + msg);
        for (PrintWriter salida : clientes.values()) {
            salida.println(msg);
        }
    }

    //Añade un cliente a la lista (llamado desde HiloCliente al conectar)
    public static synchronized void agregarCliente(String nombre, PrintWriter salida) {
        clientes.put(nombre, salida);
    }


    // Quita un cliente de la lista (lo llamamos desde el HiloCliente al desconectar)
    public static synchronized void borrarCliente(String nombre, PrintWriter salida) {
        clientes.remove(nombre, salida);
    }

    public static synchronized void enviarListaA(PrintWriter destinatario) {
        for (String nombre : clientes.keySet()) {
            destinatario.println("JOINED:" + nombre);
        }
    }

}
