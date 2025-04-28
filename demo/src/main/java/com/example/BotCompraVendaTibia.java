package com.example;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class BotCompraVendaTibia extends JFrame {

    private JTextField compraField, vendaField;
    private JLabel statusLabel, valorCompraLabel, valorVendaLabel;
    private JButton iniciarButton, pararButton;

    private volatile boolean rodando = false;

    private Rectangle regiaoCompra = new Rectangle(100, 200, 150, 50); // Ajuste essas regiões
    private Rectangle regiaoVenda = new Rectangle(300, 200, 150, 50);  // Ajuste essas regiões
    private Point botaoCompra = new Point(500, 600);
    private Point botaoVenda = new Point(700, 600);

    public BotCompraVendaTibia() {
        super("Bot de Compra e Venda - Tibia");

        setLayout(new GridLayout(6, 2, 10, 10));

        compraField = new JTextField("100.0");
        vendaField = new JTextField("150.0");

        valorCompraLabel = new JLabel("Valor Compra: --");
        valorVendaLabel = new JLabel("Valor Venda: --");
        statusLabel = new JLabel("Status: Parado");

        iniciarButton = new JButton("Iniciar");
        pararButton = new JButton("Parar");

        add(new JLabel("Valor Compra Alvo:"));
        add(compraField);
        add(new JLabel("Valor Venda Alvo:"));
        add(vendaField);
        add(valorCompraLabel);
        add(valorVendaLabel);
        add(statusLabel);
        add(new JLabel()); // espaço vazio
        add(iniciarButton);
        add(pararButton);

        iniciarButton.addActionListener((ActionEvent e) -> {
            rodando = true;
            new Thread(this::rodarSistema).start();
        });

        pararButton.addActionListener((ActionEvent e) -> {
            rodando = false;
            statusLabel.setText("Status: Parado");
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void rodarSistema() {
        try {
            Robot robot = new Robot();
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("D:\\ts\\tessdata"); // ajuste seu caminho do tessdata
            tesseract.setLanguage("eng");

            while (rodando) {
                double alvoCompra = Double.parseDouble(compraField.getText());
                double alvoVenda = Double.parseDouble(vendaField.getText());

                double valorCompra = capturarValor(robot, tesseract, regiaoCompra, "compra");
                double valorVenda = capturarValor(robot, tesseract, regiaoVenda, "venda");

                valorCompraLabel.setText("Valor Compra: " + valorCompra);
                valorVendaLabel.setText("Valor Venda: " + valorVenda);

                if (valorCompra <= alvoCompra) {
                    statusLabel.setText("Status: Comprando!");
                    clicar(robot, botaoCompra);
                } else if (valorVenda >= alvoVenda) {
                    statusLabel.setText("Status: Vendendo!");
                    clicar(robot, botaoVenda);
                } else {
                    statusLabel.setText("Status: Aguardando...");
                }

                Thread.sleep(2000); // aguarda 2 segundos
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double capturarValor(Robot robot, Tesseract tesseract, Rectangle regiao, String tipo) throws TesseractException {
        try {
            // Captura a imagem da tela
            var imagem = robot.createScreenCapture(regiao);

            // Salva a imagem capturada para debug
            ImageIO.write(imagem, "png", new File("captura-" + tipo + ".png"));
            System.out.println("Imagem salva como: captura-" + tipo + ".png");

            // Realiza o OCR na imagem capturada
            String texto = tesseract.doOCR(imagem);
            texto = texto.replaceAll("[^0-9.]", ""); // Filtra para deixar só números e ponto

            if (texto.isEmpty() || texto.equals(".")) {
                throw new IllegalArgumentException("Não capturou número válido.");
            }

            return Double.parseDouble(texto); // Converte para double e retorna
        } catch (Exception e) {
            throw new TesseractException("Erro no OCR: " + e.getMessage());
        }
    }

    private void clicar(Robot robot, Point ponto) {
        robot.mouseMove(ponto.x, ponto.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BotCompraVendaTibia::new);
    }
}
