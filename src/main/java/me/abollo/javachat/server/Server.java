package me.abollo.javachat.server;

import me.abollo.javachat.client.HiloCliente;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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


    //Se rellena una lista con todos los clientes conectados, statica porque la lista es
    //compartida con todos los hilos de clientes

    static List<PrintWriter> clientes = new ArrayList<>();

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
        for (PrintWriter saida : clientes) {
            saida.println(msg);
        }
    }

    //Añade un cliente a la lista (llamado desde HiloCliente al conectar)
    public static synchronized void agregarCliente(PrintWriter saida) {
        clientes.add(saida);
    }


    // Quita un cliente de la lista (lo llamamos desde el HiloCliente al desconectar)
    public static synchronized void borrarCliente(PrintWriter saida) {
        clientes.remove(saida);
    }


}
