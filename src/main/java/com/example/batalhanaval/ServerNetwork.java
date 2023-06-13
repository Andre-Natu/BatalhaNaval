package com.example.batalhanaval;

import javafx.stage.Stage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNetwork implements Runnable {
    // Configurações de rede
    private String ip = "localhost";
    private int errors;
    private int port = 22222;

    // Variáveis de conexão
    private Thread thread;
    private Socket socket;
    private ObjectOutputStream enviarObjeto;
    private ObjectInputStream entradaObjeto;
    private ServerSocket serverSocket;
    private boolean running = true;
    private boolean accepted = false;
    private boolean incapazDeComunicarComOponente = false;

    // Referências para outros objetos
    private ControllerBatalhaNaval controllerBatalhaNaval;
    private BatalhaNavalServidor batalhaNavalServidor;
    private String nomeUsuario;

    public boolean iniciarNetwork(String ip, int porta) {
        this.ip = ip;
        this.port = porta;

        // Inicializar o servidor
        if (!iniciarServidor()) {
            return false;
        }

        // Iniciar a thread para a comunicação de rede
        thread = new Thread(this, "ServerNetwork");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Atualizar o estado da conexão
            tick();

            if (!accepted) {
                // Esperar por pedidos de conexão
                escutarPorPedidosDeServidor();
            }
        }
    }

    private void tick() {
        if (errors >= 10) {
            incapazDeComunicarComOponente = true;
        }

        if (!incapazDeComunicarComOponente) {
            try {
                if (entradaObjeto != null) {
                    Dados dados = receberDados();
                    switch (dados.operacao) {
                        case 1:
                            System.out.println("Mensagem recebida: " + dados.mensagem);
                            // Atualizar o controlador com a mensagem recebida
                            controllerBatalhaNaval.atualizar(dados.mensagem);
                            Dados t = new Dados(1,nomeUsuario);
                            enviarDados(t);
                            controllerBatalhaNaval.mostrarBotao();
                            break;
                        case 2:
                            System.out.println("Não existe comando para essa operação.");
                            break;
                        case 3:
                            System.out.println(dados.naviosParaColocarOponente);
                            System.out.println(dados.isVertical);
                            System.out.println(dados.x);
                            System.out.println(dados.y);
                            batalhaNavalServidor.receberNavioOponente(dados.naviosParaColocarOponente,
                                    dados.isVertical, dados.x, dados.y);
                            break;
                        case 4:
                            batalhaNavalServidor.receberTiroDoOponente(dados.x, dados.y);
                            break;
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
                // Aceitar a conexão do cliente
                socket = serverSocket.accept();
                entradaObjeto = new ObjectInputStream(socket.getInputStream());
                enviarObjeto = new ObjectOutputStream(socket.getOutputStream());
                accepted = true;
                System.out.println("O cliente solicitou entrada e foi aceito.");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    protected boolean iniciarServidor() {
        try {
            // Iniciar o socket do servidor
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        System.out.println("Você criou uma sala com sucesso.");
        return true;
    }

    public void close() {
        running = false; // Sinaliza para encerrar a execução da thread
        accepted = false;

        try {
            // Fechar os recursos de comunicação
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (socket != null) {
                socket.close();
            }

            if (enviarObjeto != null) {
                enviarObjeto.close();
            }

            if (entradaObjeto != null) {
                entradaObjeto.close();
            }
            System.out.println("A thread foi encerrada com sucesso");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void enviarDados(Dados dados) {
        try {
            // Enviar objeto de dados
            enviarObjeto.reset();
            enviarObjeto.writeObject(dados);
            enviarObjeto.flush();
            System.out.println("Tamanho navio:" + dados.naviosParaColocarOponente + "Posição x:"
                    + dados.x + "Posição y:" + dados.y + "É vertical:" + dados.isVertical);
        } catch (IOException e) {
            System.out.println(e);
            errors++;
        }
    }

    public Dados receberDados() {
        try {
            System.out.println("dado recebido pela network com sucesso!");
            if (entradaObjeto != null) {
                // Receber objeto de dados
                return (Dados) entradaObjeto.readObject();
            } else {
                System.out.println("O Dado é nulo.");
                return null;
            }
        } catch (IOException e) {
            System.out.println(e);
            errors++;
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // recebe o ControllerBatalhaNaval
    public void setControllerBatalhaNaval(ControllerBatalhaNaval controller, String nomeUsuario) {
        this.controllerBatalhaNaval = controller;
        this.nomeUsuario = nomeUsuario;
    }

    public void setBatalhaNavalServidor(BatalhaNavalServidor batalhaNaval) {
        this.batalhaNavalServidor = batalhaNaval;
        System.out.println("O servidor recebeu a BatalhaNaval com sucesso");
    }
}