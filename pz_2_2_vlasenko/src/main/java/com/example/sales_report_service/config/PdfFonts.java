package com.example.sales_report_service.config;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Готові до використання кириличні PDF-шрифти (DejaVu Sans, embedded, IDENTITY_H).
 * Стандартні шрифти OpenPDF (Helvetica тощо) кирилицю НЕ малюють — тому підключаємо TTF.
 * Статичний утилітний клас: шрифти незмінні на весь час роботи застосунку.
 */
public final class PdfFonts {

    private static final BaseFont BASE_REGULAR = loadBaseFont("/fonts/DejaVuSans.ttf");
    private static final BaseFont BASE_BOLD = loadBaseFont("/fonts/DejaVuSans-Bold.ttf");

    public static final Font TITLE = new Font(BASE_BOLD, 18, Font.BOLD);
    public static final Font HEADER = new Font(BASE_BOLD, 11, Font.BOLD);
    public static final Font NORMAL = new Font(BASE_REGULAR, 10, Font.NORMAL);
    public static final Font BOLD = new Font(BASE_BOLD, 10, Font.BOLD);

    // java.awt.Font — для растрових зображень (JFreeChart), не для тексту в самому PDF.
    // Той самий кириличний TTF, той самий принцип: системні AWT-шрифти можуть не мати кирилиці.
    public static final java.awt.Font AWT_REGULAR = loadAwtFont("/fonts/DejaVuSans.ttf");
    public static final java.awt.Font AWT_BOLD = loadAwtFont("/fonts/DejaVuSans-Bold.ttf");

    private PdfFonts() {
    }

    private static BaseFont loadBaseFont(String classpathLocation) {
        try (InputStream fontStream = PdfFonts.class.getResourceAsStream(classpathLocation)) {
            if (fontStream == null) {
                throw new IllegalStateException("Шрифт не знайдено на класпаті: " + classpathLocation);
            }
            byte[] fontBytes = fontStream.readAllBytes();
            return BaseFont.createFont(classpathLocation, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Не вдалося завантажити шрифт " + classpathLocation, e);
        }
    }

    private static java.awt.Font loadAwtFont(String classpathLocation) {
        try (InputStream fontStream = PdfFonts.class.getResourceAsStream(classpathLocation)) {
            if (fontStream == null) {
                throw new IllegalStateException("Шрифт не знайдено на класпаті: " + classpathLocation);
            }
            return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream);
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException("Не вдалося завантажити AWT-шрифт " + classpathLocation, e);
        }
    }
}
