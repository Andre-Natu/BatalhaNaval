package com.example.batalhanaval;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Network implements Runnable {
    private String ip = "localhost";
    private int errors;
    private boolean circle;
    private int port = 22222;

    private Thread thread;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ServerSocket serverSocket;
    private boolean running = true;

    private boolean yourTurn = false;
    private boolean accepted = false;
    private boolean unableToCommunicateWithOpponent = false;


    public boolean iniciarNetwork(String ip, int porta, boolean isServer) {
        this.ip = ip;
        this.port = porta;

        if(isServer == true) {
             if(!iniciarServidor()) return false;
        }else {
            if(!conectar()) return false;
        }

        thread = new Thread(this, "Network");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            tick();

            if (!circle && !accepted) {
                escutarPorPedidosDeServidor();
            }
        }
    }

    private void tick() {
        if (errors >= 10) unableToCommunicateWithOpponent = true;

        if (!yourTurn && !unableToCommunicateWithOpponent) {
            try {
                if (dis != null) {

                    String mensagem = receberMensagem();
                    System.out.println("Mensagem recebida: " + mensagem);
                    yourTurn = true;


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
                Socket socket = serverSocket.accept();
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                accepted = true;
                System.out.println("O cliente solicitou entrada e foi aceito.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean conectar() {

        try {
            socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
        } catch (IOException e) {
            System.out.println("Não foi possível conectar com o servidor: " + ip + ":" + port );
            return false;
        }
        System.out.println("Você foi conectado ao servidor com sucesso.");
        return true;
    }

    protected boolean iniciarServidor() {

        try {
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Você criou uma sala com sucesso.");
        yourTurn = true;
        circle = false;
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

            if (dos != null) {
                dos.close(); // Fecha o DataOutputStream
            }

            if (dis != null) {
                dis.close(); // Fecha o DataInputStream
            }
            System.out.println("A thread foi encerrada com sucesso");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensagem(String mensagem) {
        try {
            dos.writeUTF(mensagem);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            errors++;
        }
    }

    public String receberMensagem() {
        try {
            if (dis != null) {
                return dis.readUTF();
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


}
