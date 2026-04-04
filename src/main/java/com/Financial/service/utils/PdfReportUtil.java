package com.Financial.service.utils;

import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.ReportResponse;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.lowagie.text.Rectangle;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PdfReportUtil {

    
    private static final Color HEADER_BG    = new Color(33, 97, 140);   
    private static final Color INCOME_COLOR = new Color(39, 174, 96);   
    private static final Color EXPENSE_COLOR= new Color(192, 57, 43);   
    private static final Color ROW_EVEN     = new Color(235, 245, 251); 
    private static final Color ROW_ODD      = Color.WHITE;
    private static final Color SECTION_BG   = new Color(52, 73, 94);    

    
    private static final Font TITLE_FONT      = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);
    private static final Font SECTION_FONT    = new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE);
    private static final Font TABLE_HEAD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font NORMAL_FONT     = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT       = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font INCOME_FONT     = new Font(Font.HELVETICA, 9, Font.BOLD, INCOME_COLOR);
    private static final Font EXPENSE_FONT    = new Font(Font.HELVETICA, 9, Font.BOLD, EXPENSE_COLOR);


   
    @Async
    public CompletableFuture<byte[]> generatePdfAsync(ReportResponse report) {
        try {
            byte[] pdfBytes = buildPdf(report);
            log.info("PDF generated successfully — {} records", report.getTotalRecords());
            return CompletableFuture.completedFuture(pdfBytes);
        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    
    private byte[] buildPdf(ReportResponse report) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ── 1. Title Page Header ──────────────────
        addTitleHeader(document, report);

        // ── 2. Applied Filters Info ───────────────
        addFiltersSection(document, report);

        // ── 3. Overall Summary ────────────────────
        addOverallSummary(document, report);

        // ── 4. Category Wise Totals ───────────────
        addCategoryWiseTotals(document, report);

        // ── 5. Type + Category Breakdown ─────────
        addTypeAndCategoryBreakdown(document, report);

        // ── 6. Monthly Trends ─────────────────────
        addMonthlyTrends(document, report);

        // ── 7. Per User Summary ───────────────────
        if (report.getPerUserSummary() != null && !report.getPerUserSummary().isEmpty()) {
            addPerUserSummary(document, report);
        }

        // ── 8. All Records Table ──────────────────
        addRecordsTable(document, report);

        document.close();
        return baos.toByteArray();
    }

    // ──────────────────────────────────────────────
    // Section 1: Title Header
    // ──────────────────────────────────────────────
    private void addTitleHeader(Document doc, ReportResponse report) throws Exception {

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(HEADER_BG);
        titleCell.setPadding(20);
        titleCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("Financial Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(title);

        Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(189, 215, 238));
        Paragraph sub = new Paragraph("Generated At: " + report.getReportGeneratedAt(), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(sub);

        headerTable.addCell(titleCell);
        doc.add(headerTable);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 2: Applied Filters
    // ──────────────────────────────────────────────
    private void addFiltersSection(Document doc, ReportResponse report) throws Exception {

        doc.add(createSectionHeader("Applied Filters"));

        PdfPTable table = new PdfPTable(new float[]{2, 3});
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addFilterRow(table, "User",       report.getAppliedUserId());
        addFilterRow(table, "Type",       report.getAppliedType());
        addFilterRow(table, "Category",   report.getAppliedCategory());
        addFilterRow(table, "Date Range", report.getAppliedDateRange());

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 3: Overall Summary
    // ──────────────────────────────────────────────
    private void addOverallSummary(Document doc, ReportResponse report) throws Exception {

        doc.add(createSectionHeader("Overall Summary"));

        PdfPTable table = new PdfPTable(new float[]{3, 2, 2, 2, 2});
        table.setWidthPercentage(100);

        // Header row
        addTableHeader(table, "Total Records", "Total Income", "Total Expense",
                "Net Balance", "Status");

        // Data row
        BigDecimal net = report.getNetBalance();
        String status = net.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS";
        Font statusFont = net.compareTo(BigDecimal.ZERO) >= 0 ? INCOME_FONT : EXPENSE_FONT;

        addSummaryCell(table, String.valueOf(report.getTotalRecords()), NORMAL_FONT, ROW_EVEN);
        addSummaryCell(table, "₹ " + report.getTotalIncome(), INCOME_FONT, ROW_EVEN);
        addSummaryCell(table, "₹ " + report.getTotalExpense(), EXPENSE_FONT, ROW_EVEN);
        addSummaryCell(table, "₹ " + net, net.compareTo(BigDecimal.ZERO) >= 0 ? INCOME_FONT : EXPENSE_FONT, ROW_EVEN);
        addSummaryCell(table, status, statusFont, ROW_EVEN);

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 4: Category Wise Totals
    // ──────────────────────────────────────────────
    private void addCategoryWiseTotals(Document doc, ReportResponse report) throws Exception {

        if (report.getCategoryWiseTotals() == null || report.getCategoryWiseTotals().isEmpty())
            return;

        doc.add(createSectionHeader("Category Wise Totals"));

        PdfPTable table = new PdfPTable(new float[]{3, 3});
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTableHeader(table, "Category", "Net Amount");

        boolean isEven = false;
        for (Map.Entry<String, BigDecimal> entry : report.getCategoryWiseTotals().entrySet()) {
            Color bg = isEven ? ROW_EVEN : ROW_ODD;
            BigDecimal val = entry.getValue();
            Font font = val.compareTo(BigDecimal.ZERO) >= 0 ? INCOME_FONT : EXPENSE_FONT;

            addSummaryCell(table, entry.getKey(), BOLD_FONT, bg);
            addSummaryCell(table, "₹ " + val, font, bg);
            isEven = !isEven;
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 5: Type + Category Breakdown
    // ──────────────────────────────────────────────
    private void addTypeAndCategoryBreakdown(Document doc, ReportResponse report) throws Exception {

        if (report.getTypeAndCategoryBreakdown() == null) return;

        doc.add(createSectionHeader("Type & Category Breakdown"));

        for (Map.Entry<String, Map<String, BigDecimal>> typeEntry :
                report.getTypeAndCategoryBreakdown().entrySet()) {

            String type = typeEntry.getKey();
            Font labelFont = type.equals("INCOME") ? INCOME_FONT : EXPENSE_FONT;

            Paragraph typeLabel = new Paragraph("  " + type, labelFont);
            typeLabel.setSpacingBefore(6);
            doc.add(typeLabel);

            PdfPTable table = new PdfPTable(new float[]{3, 3});
            table.setWidthPercentage(55);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            addTableHeader(table, "Category", "Amount");

            boolean isEven = false;
            for (Map.Entry<String, BigDecimal> catEntry : typeEntry.getValue().entrySet()) {
                Color bg = isEven ? ROW_EVEN : ROW_ODD;
                addSummaryCell(table, catEntry.getKey(), NORMAL_FONT, bg);
                addSummaryCell(table, "₹ " + catEntry.getValue(), BOLD_FONT, bg);
                isEven = !isEven;
            }
            doc.add(table);
        }
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 6: Monthly Trends
    // ──────────────────────────────────────────────
    private void addMonthlyTrends(Document doc, ReportResponse report) throws Exception {

        if (report.getMonthlyTrends() == null || report.getMonthlyTrends().isEmpty()) return;

        doc.add(createSectionHeader("Monthly Trends"));

        PdfPTable table = new PdfPTable(new float[]{3, 3});
        table.setWidthPercentage(55);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        addTableHeader(table, "Month", "Net Amount");

        boolean isEven = false;
        for (Map.Entry<String, BigDecimal> entry : report.getMonthlyTrends().entrySet()) {
            Color bg = isEven ? ROW_EVEN : ROW_ODD;
            BigDecimal val = entry.getValue();
            Font font = val.compareTo(BigDecimal.ZERO) >= 0 ? INCOME_FONT : EXPENSE_FONT;
            addSummaryCell(table, entry.getKey(), NORMAL_FONT, bg);
            addSummaryCell(table, "₹ " + val, font, bg);
            isEven = !isEven;
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 7: Per User Summary
    // ──────────────────────────────────────────────
    private void addPerUserSummary(Document doc, ReportResponse report) throws Exception {

        doc.add(createSectionHeader("Per User Summary"));

        PdfPTable table = new PdfPTable(new float[]{2, 3, 2, 3, 3, 3});
        table.setWidthPercentage(100);
        addTableHeader(table, "User ID", "User Name", "Records",
                "Total Income", "Total Expense", "Net Balance");

        boolean isEven = false;
        for (ReportResponse.UserReportSummary user : report.getPerUserSummary().values()) {
            Color bg = isEven ? ROW_EVEN : ROW_ODD;
            BigDecimal net = user.getNetBalance();

            addSummaryCell(table, user.getUserId(),                   NORMAL_FONT, bg);
            addSummaryCell(table, user.getUserName(),                  BOLD_FONT,   bg);
            addSummaryCell(table, String.valueOf(user.getTotalRecords()), NORMAL_FONT, bg);
            addSummaryCell(table, "₹ " + user.getTotalIncome(),       INCOME_FONT, bg);
            addSummaryCell(table, "₹ " + user.getTotalExpense(),      EXPENSE_FONT,bg);
            addSummaryCell(table, "₹ " + net,
                    net.compareTo(BigDecimal.ZERO) >= 0 ? INCOME_FONT : EXPENSE_FONT, bg);
            isEven = !isEven;
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    // ──────────────────────────────────────────────
    // Section 8: All Records Table
    // ──────────────────────────────────────────────
    private void addRecordsTable(Document doc, ReportResponse report) throws Exception {

        if (report.getRecords() == null || report.getRecords().isEmpty()) return;

        doc.add(createSectionHeader("All Records (" + report.getTotalRecords() + ")"));

        PdfPTable table = new PdfPTable(new float[]{2, 2, 2, 2, 2, 3, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "Date", "User", "Type", "Category", "Amount", "Notes", "Created At");

        boolean isEven = false;
        for (FinancialRecordResponse r : report.getRecords()) {
            Color bg = isEven ? ROW_EVEN : ROW_ODD;
            Font typeFont = r.getType().name().equals("INCOME") ? INCOME_FONT : EXPENSE_FONT;

            addSummaryCell(table, r.getDate() != null ? r.getDate().toLocalDate().toString() : "-", NORMAL_FONT, bg);
            addSummaryCell(table, r.getUserName() != null ? r.getUserName() : "-",                   NORMAL_FONT, bg);
            addSummaryCell(table, r.getType().name(),                                                 typeFont,    bg);
            addSummaryCell(table, r.getCategory().name(),                                             NORMAL_FONT, bg);
            addSummaryCell(table, "₹ " + r.getAmount(),                                               BOLD_FONT,   bg);
            addSummaryCell(table, r.getNotes() != null ? r.getNotes() : "-",                         NORMAL_FONT, bg);
            addSummaryCell(table, r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "-", NORMAL_FONT, bg);
            isEven = !isEven;
        }

        doc.add(table);
    }

    // ──────────────────────────────────────────────
    // Helper methods
    // ──────────────────────────────────────────────

    private Paragraph createSectionHeader(String title) {
        PdfPTable sectionTable = new PdfPTable(1);
        // ← just return styled Paragraph instead
        Paragraph p = new Paragraph(title, SECTION_FONT);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        return p;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEAD_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(Color.WHITE);
            table.addCell(cell);
        }
    }

    private void addSummaryCell(PdfPTable table, String value, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new Color(189, 215, 238));
        table.addCell(cell);
    }

    private void addFilterRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(ROW_EVEN);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "ALL", NORMAL_FONT));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}