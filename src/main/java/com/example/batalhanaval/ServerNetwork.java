package com.example.batalhanaval;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNetwork implements Runnable {
    private String ip = "localhost";
    private int errors;
    private int port = 22222;

    private Thread thread;
    private Socket socket;
    private DataOutputStream enviar;
    private DataInputStream entrada;
    private ServerSocket serverSocket;
    private boolean running = true;
    private boolean accepted = false;
    private boolean unableToCommunicateWithOpponent = false;

    private ControllerBatalhaNaval controllerBatalhaNaval;
    private String nomeUsuario;

    public boolean iniciarNetwork(String ip, int porta) {
        this.ip = ip;
        this.port = porta;

        if (!iniciarServidor()) {
            return false;
        }

        thread = new Thread(this, "ServerNetwork");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            tick();

            if (!accepted) {
                escutarPorPedidosDeServidor();
            }
        }
    }

    private void tick() {
        if (errors >= 10) {
            unableToCommunicateWithOpponent = true;
        }

        if (!unableToCommunicateWithOpponent) {
            try {
                if (entrada != null) {
                    String mensagem = receberMensagem();
                    System.out.println("Mensagem recebida: " + mensagem);
                    if(mensagem.startsWith("oponente é: ")){
                         controllerBatalhaNaval.atualizar(mensagem);
                         enviarMensagem(nomeUsuario);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errors++;
            }
        }
    }

    private void escutarPorPedidosDeServidor() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                socket = serverSocket.accept();
                enviar = new DataOutputStream(socket.getOutputStream());
                entrada = new DataInputStream(socket.getInputStream());
                accepted = true;
                System.out.println("O cliente solicitou entrada e foi aceito.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean iniciarServidor() {
        try {
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Você criou uma sala com sucesso.");
        return true;
    }

    public void close() {
        running = false; // Sinaliza para encerrar a execução da thread
        accepted = false;

        try {
            if (serverSocket != null) {
                serverSocket.close(); // Fecha o servidor Socket
            }

            if (socket != null) {
                socket.close(); // Fecha o cliente Socket
            }

            if (enviar != null) {
                enviar.close(); // Fecha o DataOutputStream
            }

            if (entrada != null) {
                entrada.close(); // Fecha o DataInputStream
            }
            System.out.println("A thread foi encerrada com sucesso");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensagem(String mensagem) {
        try {
            enviar.writeUTF(mensagem);
            enviar.flush();
        } catch (IOException e) {
            e.printStackTrace();
            errors++;
        }
    }

    public String receberMensagem() {
        try {
            if (entrada != null) {
                return entrada.readUTF();
            } else {
                System.out.println("O objeto DataInputStream é nulo.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            errors++;
            return null;
        }
    }

    // Método para definir a instância do ControllerBatalhaNaval
    public void setControllerBatalhaNaval(ControllerBatalhaNaval controller, String nomeUsuario) {
        this.controllerBatalhaNaval = controller;
        this.nomeUsuario = nomeUsuario;
    }

}
