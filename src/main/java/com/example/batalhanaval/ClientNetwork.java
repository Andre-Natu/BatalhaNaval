package com.example.batalhanaval;

import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ClientNetwork implements Runnable {
    // Configurações de rede
    private String ip = "localhost";
    private int errors;
    private int port = 22222;

    // Variáveis de conexão
    private Thread thread;
    private Socket socket;
    private ObjectOutputStream enviarObjeto;
    private ObjectInputStream entradaObjeto;
    private boolean running = true;
    private boolean accepted = false;
    private boolean incapazDeComunicarComOponente = false;

    // Referências para outros objetos
    private ControllerBatalhaNaval controllerBatalhaNaval;
    private BatalhaNavalCliente batalhaNavalCliente;

    public boolean iniciarNetwork(String ip, int porta) {
        this.ip = ip;
        this.port = porta;

        // Conectar ao servidor
        if (!conectar()) {
            return false;
        }

        // Iniciar a thread para a comunicação de rede
        thread = new Thread(this, "ClientNetwork");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Atualizar o estado da conexão
            tick();
        }
    }

    private void tick() {
        if (errors >= 5) {
            close();
            return;
        }
            try {
                if (entradaObjeto != null) {
                    Dados dados = receberDados();
                    switch (dados.operacao){
                        case 1:
                            System.out.println("Mensagem recebida: " + dados.mensagem);
                            // Atualizar o controlador com a mensagem recebida
                            controllerBatalhaNaval.atualizar(dados.mensagem);
                            break;
                        case 2:
                            controllerBatalhaNaval.iniciarTabuleiro();
                            break;
                        case 3:
                            System.out.println(dados.naviosParaColocarOponente);
                            System.out.println(dados.isVertical);
                            System.out.println(dados.x);
                            System.out.println(dados.y);
                            batalhaNavalCliente.receberNavioOponente(dados.naviosParaColocarOponente,
                                    dados.isVertical, dados.x, dados.y);
                            break;
                        case 4:
                            batalhaNavalCliente.receberTiroDoOponente(dados.x, dados.y);
                            break;
                        case 5:
                            System.out.println("Derrota recebida com sucesso.");
                            batalhaNavalCliente.receberDerrota();

                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                errors++;
            }
    }

    private boolean conectar() {
        try {
            // Conectar ao servidor
            socket = new Socket(ip, port);
            enviarObjeto = new ObjectOutputStream(socket.getOutputStream());
            entradaObjeto = new ObjectInputStream(socket.getInputStream());
            accepted = true;
        } catch (IOException e) {
            System.out.println("Não foi possível conectar com o servidor: " + ip + ":" + port);
            return false;
        }
        System.out.println("Você foi conectado ao servidor com sucesso.");
        return true;
    }

    public void close() {
        running = false; // Sinaliza para encerrar a execução da thread
        accepted = false;

        try {
            // Fechar os recursos de comunicação
            if (socket != null) {
                socket.close(); // Fecha o Socket do cliente
            }

            if (enviarObjeto != null) {
                enviarObjeto.close(); // Fecha o ObjectOutputStream
            }

            if (entradaObjeto != null) {
                entradaObjeto.close(); // Fecha o ObjectInputStream
            }

            System.out.println("O cliente foi encerrado com sucesso");
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
            System.out.println("Não foi possível mandar a informação, tentando novamente...");
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
            System.out.println("Não foi possível receber a informação, tentando novamente...");
            errors++;
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Método para definir a instância do ControllerBatalhaNaval
    public void setControllerBatalhaNaval(ControllerBatalhaNaval controller) {
        this.controllerBatalhaNaval = controller;
    }

    public void setBatalhaNavalCliente(BatalhaNavalCliente batalhaNaval) {
        this.batalhaNavalCliente = batalhaNaval;
        System.out.println("O cliente recebeu a BatalhaNaval com sucesso");
    }
}
