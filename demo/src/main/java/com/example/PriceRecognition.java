package com.example;

import java.awt.image.BufferedImage;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class PriceRecognition {
    
    public static String recognizePrice(BufferedImage image) {
        try {
            ITesseract tess = new Tesseract();
            tess.setDatapath("src/main/resources/tessdata");  // Caminho para os dados do Tesseract

            String result = tess.doOCR(image);
            return result.replaceAll("[^\\d.]", "");  // Retira caracteres não numéricos
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
