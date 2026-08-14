package com.example.sales_report_service.service;

import com.example.sales_report_service.config.PdfFonts;
import com.example.sales_report_service.model.Region;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Генерація стовпчикової діаграми "продажі по регіонах" у вигляді PNG (для вставки в PDF).
 */
@Service
public class ChartService {

    private static final int CHART_WIDTH = 500;
    private static final int CHART_HEIGHT = 300;

    public byte[] generateRegionBarChartPng(Map<Region, BigDecimal> totalsByRegion) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Region, BigDecimal> entry : totalsByRegion.entrySet()) {
            dataset.addValue(entry.getValue(), "Сума продажів", entry.getKey().getDisplayName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Продажі по регіонах",
                "Регіон",
                "Сума, ₴",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        applyCyrillicFonts(chart);

        BufferedImage image = chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося згенерувати діаграму", e);
        }
    }

    // Стандартні AWT-шрифти можуть не мати кирилиці — та сама пастка, що й у PDF-тексті,
    // тому підключаємо той самий DejaVu Sans (через PdfFonts.AWT_*).
    private void applyCyrillicFonts(JFreeChart chart) {
        chart.getTitle().setFont(PdfFonts.AWT_BOLD.deriveFont(16f));

        CategoryPlot plot = chart.getCategoryPlot();

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(PdfFonts.AWT_REGULAR.deriveFont(12f));
        domainAxis.setTickLabelFont(PdfFonts.AWT_REGULAR.deriveFont(11f));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(PdfFonts.AWT_REGULAR.deriveFont(12f));
        rangeAxis.setTickLabelFont(PdfFonts.AWT_REGULAR.deriveFont(11f));
    }
}
