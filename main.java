import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class GameBot {

    // --- Configurações de Coordenadas (AJUSTE ESTES VALORES!) ---
    private static final int PRICE_AREA_X = 500;       // Coordenada X do canto superior esquerdo da área do preço
    private static final int PRICE_AREA_Y = 300;       // Coordenada Y do canto superior esquerdo da área do preço
    private static final int PRICE_AREA_WIDTH = 100;   // Largura da área do preço
    private static final int PRICE_AREA_HEIGHT = 30;   // Altura da área do preço

    private static final int BUY_BUTTON_X = 650;       // Coordenada X do botão COMPRAR
    private static final int BUY_BUTTON_Y = 350;       // Coordenada Y do botão COMPRAR

    private static final int SELL_BUTTON_X = 750;      // Coordenada X do botão VENDER
    private static final int SELL_BUTTON_Y = 350;      // Coordenada Y do botão VENDER
    // --- Fim das Configurações de Coordenadas ---

    private static final int CHECK_INTERVAL_MS = 3000; // Intervalo entre verificações (3 segundos)
    private static final int CLICK_DELAY_MS = 150;     // Pequeno delay para cliques

    private Robot robot;
    private ITesseract tesseract;
    private Rectangle priceArea;
    private Point buyButtonCoords;
    private Point sellButtonCoords;
    private double maxBuyPrice;
    private double minSellPrice;

    public GameBot(double maxBuyPrice, double minSellPrice) throws AWTException {
        this.robot = new Robot();
        this.tesseract = new Tesseract();
        // IMPORTANTÍSSIMO: Configure o caminho para sua pasta 'tessdata'
        // Se o Tesseract e tessdata estiverem no PATH do sistema, pode não ser necessário.
        // Exemplo: tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Linux
        // Exemplo: tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // Windows
        // tesseract.setDatapath("/path/to/your/tessdata"); // <--- DESCOMENTE E AJUSTE SE NECESSÁRIO

        // Define o idioma para o OCR (português 'por', inglês 'eng')
        // Certifique-se que o arquivo .traineddata correspondente existe na pasta tessdata
        tesseract.setLanguage("eng"); // Ou "por", dependendo do idioma do número no jogo

        // Configura o Tesseract para reconhecer apenas dígitos e ponto decimal (melhora precisão)
        tesseract.setVariable("tessedit_char_whitelist", "0123456789.");

        this.priceArea = new Rectangle(PRICE_AREA_X, PRICE_AREA_Y, PRICE_AREA_WIDTH, PRICE_AREA_HEIGHT);
        this.buyButtonCoords = new Point(BUY_BUTTON_X, BUY_BUTTON_Y);
        this.sellButtonCoords = new Point(SELL_BUTTON_X, SELL_BUTTON_Y);
        this.maxBuyPrice = maxBuyPrice;
        this.minSellPrice = minSellPrice;

        System.out.println("Bot inicializado.");
        System.out.println("Área de Preço: " + priceArea);
        System.out.println("Botão Comprar: " + buyButtonCoords);
        System.out.println("Botão Vender: " + sellButtonCoords);
        System.out.println("Preço Máx. Compra: " + maxBuyPrice);
        System.out.println("Preço Mín. Venda: " + minSellPrice);
        System.out.println("---");
    }

    /**
     * Captura a área definida da tela.
     * @return BufferedImage da área capturada.
     */
    private BufferedImage capturePriceArea() {
        return robot.createScreenCapture(priceArea);
    }

    /**
     * Realiza OCR na imagem fornecida para extrair o preço.
     * @param image Imagem a ser processada.
     * @return O preço como double, ou -1.0 se o OCR falhar ou não for um número válido.
     */
    private double performOCR(BufferedImage image) {
        try {
            String rawText = tesseract.doOCR(image);
            // Limpa o texto obtido (remove espaços, letras, símbolos exceto ponto)
            String cleanedText = rawText.replaceAll("[^\\d.]", "");

            if (cleanedText.isEmpty()) {
                 System.err.println("OCR não retornou texto numérico.");
                 return -1.0; // Indica falha
            }

            // Tenta converter para double
             try {
                 return Double.parseDouble(cleanedText);
             } catch (NumberFormatException nfe) {
                 System.err.println("Erro ao converter texto OCR '" + cleanedText + "' para número: " + nfe.getMessage());
                 return -1.0; // Indica falha
             }

        } catch (TesseractException e) {
            System.err.println("Erro no OCR: " + e.getMessage());
            // Considerar adicionar mais detalhes do erro, como e.getCause()
            return -1.0; // Indica falha
        }
    }

    /**
     * Move o mouse para as coordenadas e simula um clique esquerdo.
     * @param coords Ponto (coordenadas X, Y) onde clicar.
     */
    private void clickAt(Point coords) {
        System.out.println("Movendo mouse para: " + coords.x + ", " + coords.y);
        robot.mouseMove(coords.x, coords.y);
        robot.delay(CLICK_DELAY_MS); // Pequena pausa antes de clicar

        System.out.println("Clicando...");
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY_MS / 2); // Pausa curta entre pressionar e soltar
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY_MS); // Pequena pausa após clicar
         System.out.println("Clique finalizado.");
    }

    /**
     * Inicia o loop principal do bot.
     */
    public void runBotLoop() {
        System.out.println("\nIniciando loop do bot... Pressione CTRL+C para parar.");
        while (true) {
            try {
                System.out.println("\nVerificando preço...");
                // 1. Capturar a imagem da área de preço
                BufferedImage priceImage = capturePriceArea();

                // Opcional: Salvar a imagem para depuração
                // javax.imageio.ImageIO.write(priceImage, "png", new File("price_capture.png"));

                // 2. Fazer OCR na imagem
                double currentPrice = performOCR(priceImage);

                if (currentPrice < 0) {
                    System.out.println("Não foi possível ler o preço nesta verificação.");
                } else {
                    System.out.println("Preço detectado: " + currentPrice);

                    // 3. Comparar preços e agir
                    if (currentPrice <= maxBuyPrice) {
                        System.out.println(">>> PREÇO ABAIXO DO MÁXIMO DE COMPRA (" + maxBuyPrice + "). COMPRANDO!");
                        clickAt(buyButtonCoords);
                        // Adiciona uma pausa extra após a ação para evitar cliques duplos rápidos
                        Thread.sleep(CHECK_INTERVAL_MS / 2);
                    } else if (currentPrice >= minSellPrice) {
                        System.out.println(">>> PREÇO ACIMA DO MÍNIMO DE VENDA (" + minSellPrice + "). VENDENDO!");
                        clickAt(sellButtonCoords);
                        // Adiciona uma pausa extra após a ação
                         Thread.sleep(CHECK_INTERVAL_MS / 2);
                    } else {
                        System.out.println("Preço dentro da faixa definida. Aguardando...");
                    }
                }

                // 4. Aguardar antes da próxima verificação
                System.out.println("Aguardando " + (CHECK_INTERVAL_MS / 1000.0) + " segundos...");
                Thread.sleep(CHECK_INTERVAL_MS);

            } catch (InterruptedException e) {
                System.out.println("Loop interrompido.");
                Thread.currentThread().interrupt(); // Restaura o status de interrupção
                break; // Sai do loop
            } catch (Exception e) {
                // Captura outras exceções inesperadas para não parar o bot abruptamente
                System.err.println("Erro inesperado no loop principal: " + e.getMessage());
                e.printStackTrace();
                // Pausa um pouco antes de tentar novamente
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                     Thread.currentThread().interrupt();
                     break;
                }
            }
        }
         System.out.println("Bot encerrado.");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double maxBuy = -1.0;
        double minSell = -1.0;

        System.out.println("--- Configuração do Bot de Compra/Venda ---");

        // Obter preço máximo de compra
        while (maxBuy < 0) {
            System.out.print("Digite o preço MÁXIMO para COMPRAR o item (ex: 150.50): ");
            try {
                maxBuy = scanner.nextDouble();
                if (maxBuy < 0) {
                    System.out.println("O preço deve ser um valor positivo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, digite um número.");
                scanner.next(); // Consome a entrada inválida
            }
        }

        // Obter preço mínimo de venda
        while (minSell < 0 || minSell <= maxBuy) {
             System.out.print("Digite o preço MÍNIMO para VENDER o item (ex: 200.00): ");
             try {
                 minSell = scanner.nextDouble();
                 if (minSell < 0) {
                      System.out.println("O preço deve ser um valor positivo.");
                 } else if (minSell <= maxBuy) {
                     System.out.println("O preço mínimo de venda deve ser MAIOR que o preço máximo de compra (" + maxBuy + ").");
                 }
             } catch (InputMismatchException e) {
                 System.out.println("Entrada inválida. Por favor, digite um número.");
                 scanner.next(); // Consome a entrada inválida
             }
        }

        scanner.close(); // Fecha o scanner após obter as entradas

        try {
            GameBot bot = new GameBot(maxBuy, minSell);
            bot.runBotLoop();
        } catch (AWTException e) {
            System.err.println("Erro ao inicializar o Robot (necessário para controle de tela/mouse): " + e.getMessage());
            System.err.println("Verifique se o ambiente gráfico está acessível.");
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
             System.err.println("Erro ao carregar bibliotecas nativas do Tesseract/Tess4J.");
             System.err.println("Verifique se o Tesseract está instalado CORRETAMENTE e se as DLLs/SOs estão acessíveis.");
             System.err.println("Verifique também se a dependência Tess4J está configurada no projeto.");
             System.err.println("Mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado na inicialização: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
