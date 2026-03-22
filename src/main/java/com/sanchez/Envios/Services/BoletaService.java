package com.sanchez.Envios.Services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Repositories.EnviosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BoletaService {

    @Autowired
    private EnviosRepository enviosRepository;

    // Colores corporativos
    private static final DeviceRgb COLOR_PRIMARY    = new DeviceRgb(26, 86, 219);   // azul
    private static final DeviceRgb COLOR_PRIMARY_LIGHT = new DeviceRgb(219, 234, 254); // azul claro
    private static final DeviceRgb COLOR_HEADER_BG  = new DeviceRgb(30, 58, 138);   // azul oscuro
    private static final DeviceRgb COLOR_GRAY_BG    = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb COLOR_GRAY_BORDER = new DeviceRgb(226, 232, 240);
    private static final DeviceRgb COLOR_TEXT_MUTED  = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb COLOR_TEXT_DARK   = new DeviceRgb(15, 23, 42);
    private static final DeviceRgb COLOR_SUCCESS     = new DeviceRgb(21, 128, 61);
    private static final DeviceRgb COLOR_SUCCESS_BG  = new DeviceRgb(220, 252, 231);

    public ResponseDto<Map<String, String>> generarBoletaPdf(UUID envioId) {
        try {
            Optional<Envios> opt = enviosRepository.findById(envioId);
            if (opt.isEmpty()) {
                return new ResponseDto<>(404, "Envío no encontrado", null);
            }
            Envios envio = opt.get();

            byte[] pdfBytes = buildPdf(envio);
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            Map<String, String> data = new HashMap<>();
            data.put("base64", base64);
            data.put("filename", envio.getNumeroDocumento() + ".pdf");
            data.put("mimeType", "application/pdf");

            return new ResponseDto<>(200, "Documento generado correctamente", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDto<>(500, "Error al generar el documento: " + e.getMessage(), null);
        }
    }

    private byte[] buildPdf(Envios envio) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(36, 40, 36, 40);

        PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont mono   = PdfFontFactory.createFont(StandardFonts.COURIER);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        boolean esBoleta = !"FACTURA".equalsIgnoreCase(envio.getTipoDocumento());
        String tipoLabel = esBoleta ? "BOLETA DE VENTA" : "FACTURA";
        String serie     = esBoleta ? "B" : "F";

        // ──────────────────────────────────────────────
        // ENCABEZADO con fondo oscuro
        // ──────────────────────────────────────────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0);

        // Celda izquierda: nombre empresa
        Cell leftHeader = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_HEADER_BG)
                .setPadding(18);
        leftHeader.add(new Paragraph("ENVIOS TODOPAIS")
                .setFont(bold).setFontSize(18).setFontColor(ColorConstants.WHITE));
        leftHeader.add(new Paragraph("Sistema de Envíos y Courier")
                .setFont(regular).setFontSize(9).setFontColor(new DeviceRgb(148, 163, 184)));
        leftHeader.add(new Paragraph("noreplyenviostodopais@gmail.com")
                .setFont(regular).setFontSize(8).setFontColor(new DeviceRgb(148, 163, 184))
                .setMarginTop(4));
        header.addCell(leftHeader);

        // Celda derecha: tipo + número documento
        Cell rightHeader = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_PRIMARY)
                .setPadding(18)
                .setTextAlignment(TextAlignment.RIGHT);
        rightHeader.add(new Paragraph(tipoLabel)
                .setFont(bold).setFontSize(14).setFontColor(ColorConstants.WHITE));
        rightHeader.add(new Paragraph(envio.getNumeroDocumento() != null ? envio.getNumeroDocumento() : "-")
                .setFont(mono).setFontSize(12).setFontColor(COLOR_PRIMARY_LIGHT)
                .setMarginTop(4));
        rightHeader.add(new Paragraph("Fecha: " + (envio.getFechaCreacion() != null
                ? envio.getFechaCreacion().format(dtf) : "-"))
                .setFont(regular).setFontSize(8).setFontColor(new DeviceRgb(186, 230, 253))
                .setMarginTop(6));
        header.addCell(rightHeader);
        doc.add(header);

        // ──────────────────────────────────────────────
        // ESTADO / BADGE
        // ──────────────────────────────────────────────
        Table estadoBand = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        Cell trackCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_GRAY_BG)
                .setPadding(10).setPaddingLeft(14);
        trackCell.add(new Paragraph("Código de Tracking")
                .setFont(regular).setFontSize(8).setFontColor(COLOR_TEXT_MUTED));
        trackCell.add(new Paragraph(envio.getCodigoTracking())
                .setFont(bold).setFontSize(13).setFontColor(COLOR_PRIMARY));
        estadoBand.addCell(trackCell);

        Cell estadoCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_SUCCESS_BG)
                .setPadding(10).setTextAlignment(TextAlignment.RIGHT).setPaddingRight(14);
        estadoCell.add(new Paragraph("Estado")
                .setFont(regular).setFontSize(8).setFontColor(COLOR_SUCCESS));
        estadoCell.add(new Paragraph(envio.getEstado() != null ? envio.getEstado() : "Registrado")
                .setFont(bold).setFontSize(11).setFontColor(COLOR_SUCCESS));
        estadoBand.addCell(estadoCell);
        doc.add(estadoBand);

        // ──────────────────────────────────────────────
        // EMISOR / RECEPTOR
        // ──────────────────────────────────────────────
        Table partes = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        partes.addCell(buildParteCard("EMISOR / REMITENTE", bold, regular,
                envio.getEmisorNombre(),
                envio.getEmisorDni(),
                envio.getEmisorRazonSocial(),
                envio.getEmisorTelefono(),
                envio.getEmisorCorreo(),
                true));

        partes.addCell(buildParteCard("RECEPTOR / DESTINATARIO", bold, regular,
                envio.getReceptorNombre(),
                envio.getReceptorDni(),
                envio.getReceptorRazonSocial(),
                null,
                null,
                false));

        doc.add(partes);

        // ──────────────────────────────────────────────
        // DETALLE DEL ENVÍO
        // ──────────────────────────────────────────────
        doc.add(sectionTitle("DETALLE DEL SERVICIO", bold));

        Table detalle = new Table(UnitValue.createPercentArray(new float[]{30, 50, 20}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Cabecera tabla
        String[] cols = {"Concepto", "Descripción", "Importe"};
        for (String col : cols) {
            detalle.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_PRIMARY)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .add(new Paragraph(col)
                            .setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE)));
        }

        // Fila: tipo de entrega
        String destino = envio.getDestino() != null ? envio.getDestino().getNombre() : "-";
        if ("DOMICILIO".equalsIgnoreCase(envio.getTipoEntrega())) {
            destino = "Domicilio: " + (envio.getDireccionEntrega() != null ? envio.getDireccionEntrega() : "-");
        }
        addDetalleRow(detalle, regular, "Tipo de entrega",
                (envio.getTipoEntrega() != null ? envio.getTipoEntrega() : "-") + " — " + destino, null, false);

        // Fila: descripción del paquete
        addDetalleRow(detalle, regular, "Contenido / Paquete",
                envio.getDescripcionPaquete() != null && !envio.getDescripcionPaquete().isBlank()
                        ? envio.getDescripcionPaquete() : "Sin descripción especificada",
                null, true);

        // Fila: fecha estimada
        String fechaEst = envio.getFechaEstimada() != null
                ? envio.getFechaEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Por confirmar";
        addDetalleRow(detalle, regular, "Entrega estimada", fechaEst, null, false);

        // Fila: precio (span visual)
        BigDecimal precio = envio.getPrecioEnvio() != null ? envio.getPrecioEnvio() : BigDecimal.ZERO;
        addDetalleRow(detalle, regular, "Servicio de envío", "Courier nacional", precio, true);

        doc.add(detalle);

        // ──────────────────────────────────────────────
        // TOTAL
        // ──────────────────────────────────────────────
        doc.add(buildTotalBox(bold, regular, precio, esBoleta));

        // ──────────────────────────────────────────────
        // PIE DE PÁGINA
        // ──────────────────────────────────────────────
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(COLOR_GRAY_BORDER).setMarginTop(20).setMarginBottom(10));

        Table footer = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell fl = new Cell().setBorder(Border.NO_BORDER);
        fl.add(new Paragraph("Envios Todopais — Documento generado electrónicamente")
                .setFont(regular).setFontSize(7).setFontColor(COLOR_TEXT_MUTED));
        fl.add(new Paragraph("Este documento no requiere firma ni sello para su validez.")
                .setFont(regular).setFontSize(7).setFontColor(COLOR_TEXT_MUTED));
        footer.addCell(fl);

        Cell fr = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        fr.add(new Paragraph("Generado: " + java.time.LocalDateTime.now().format(dtf))
                .setFont(regular).setFontSize(7).setFontColor(COLOR_TEXT_MUTED));
        fr.add(new Paragraph("© 2025 Envios Todopais")
                .setFont(bold).setFontSize(7).setFontColor(COLOR_PRIMARY));
        footer.addCell(fr);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    // ──────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────

    private Cell buildParteCard(String titulo, PdfFont bold, PdfFont regular,
                                String nombre, String docId, String razonSocial,
                                String telefono, String correo, boolean leftSide) {
        Cell card = new Cell()
                .setBorder(new SolidBorder(COLOR_GRAY_BORDER, 0.5f))
                .setBackgroundColor(COLOR_GRAY_BG)
                .setPadding(12)
                .setPaddingRight(leftSide ? 8 : 12)
                .setPaddingLeft(leftSide ? 12 : 8);

        card.add(new Paragraph(titulo)
                .setFont(bold).setFontSize(8).setFontColor(COLOR_PRIMARY)
                .setMarginBottom(6));

        card.add(new Paragraph(nombre != null ? nombre : "—")
                .setFont(bold).setFontSize(11).setFontColor(COLOR_TEXT_DARK)
                .setMarginBottom(2));

        if (razonSocial != null && !razonSocial.isBlank()) {
            card.add(new Paragraph(razonSocial)
                    .setFont(regular).setFontSize(9).setFontColor(COLOR_TEXT_MUTED)
                    .setMarginBottom(2));
        }

        String tipoDoc = (docId != null && docId.length() == 11) ? "RUC" : "DNI";
        card.add(infoRow(tipoDoc + ":", docId != null ? docId : "—", regular));

        if (telefono != null && !telefono.isBlank() && !"Desconocido".equalsIgnoreCase(telefono)) {
            card.add(infoRow("Tel.:", telefono, regular));
        }
        if (correo != null && !correo.isBlank()) {
            card.add(infoRow("Email:", correo, regular));
        }
        return card;
    }

    private Paragraph infoRow(String label, String value, PdfFont regular) {
        return new Paragraph()
                .add(new Text(label + " ").setFontColor(COLOR_TEXT_MUTED).setFont(regular).setFontSize(8))
                .add(new Text(value).setFontColor(COLOR_TEXT_DARK).setFont(regular).setFontSize(8))
                .setMarginBottom(2);
    }

    private Paragraph sectionTitle(String text, PdfFont bold) {
        return new Paragraph(text)
                .setFont(bold).setFontSize(9)
                .setFontColor(COLOR_PRIMARY)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 1.5f))
                .setPaddingBottom(4)
                .setMarginBottom(8);
    }

    private void addDetalleRow(Table table, PdfFont regular, String concepto,
                                String descripcion, BigDecimal importe, boolean shaded) {
        DeviceRgb bg = shaded ? COLOR_GRAY_BG : ColorConstants.WHITE;

        table.addCell(new Cell()
                .setBackgroundColor(bg)
                .setBorderBottom(new SolidBorder(COLOR_GRAY_BORDER, 0.3f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPadding(8)
                .add(new Paragraph(concepto).setFont(regular).setFontSize(9).setFontColor(COLOR_TEXT_DARK)));

        table.addCell(new Cell()
                .setBackgroundColor(bg)
                .setBorderBottom(new SolidBorder(COLOR_GRAY_BORDER, 0.3f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPadding(8)
                .add(new Paragraph(descripcion).setFont(regular).setFontSize(9).setFontColor(COLOR_TEXT_DARK)));

        String importeStr = importe != null ? "S/ " + String.format("%.2f", importe) : "—";
        table.addCell(new Cell()
                .setBackgroundColor(bg)
                .setBorderBottom(new SolidBorder(COLOR_GRAY_BORDER, 0.3f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPadding(8).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(importeStr).setFont(regular).setFontSize(9).setFontColor(COLOR_TEXT_DARK)));
    }

    private Table buildTotalBox(PdfFont bold, PdfFont regular, BigDecimal precio, boolean esBoleta) {
        BigDecimal igv = esBoleta
                ? BigDecimal.ZERO
                : precio.multiply(new BigDecimal("0.18")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal subtotal = precio.subtract(igv).setScale(2, java.math.RoundingMode.HALF_UP);

        Table t = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Celda izquierda vacía
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setMinHeight(60));

        // Celda derecha: totales
        Cell totales = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_GRAY_BG)
                .setPadding(12);

        if (!esBoleta) {
            totales.add(totalLine("Sub-total:", "S/ " + String.format("%.2f", subtotal), regular, false));
            totales.add(totalLine("IGV (18%):", "S/ " + String.format("%.2f", igv), regular, false));
            totales.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                    .setStrokeColor(COLOR_GRAY_BORDER).setMarginBottom(4));
        }

        // Fila total destacada
        Cell totalFinal = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(COLOR_PRIMARY)
                .setPadding(10);
        totalFinal.add(new Paragraph()
                .add(new Text("TOTAL A PAGAR").setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .add(new Text("      S/ " + String.format("%.2f", precio))
                        .setFont(bold).setFontSize(14).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.RIGHT));
        totales.add(totalFinal);
        t.addCell(totales);

        return t;
    }

    private Paragraph totalLine(String label, String value, PdfFont regular, boolean big) {
        return new Paragraph()
                .add(new Text(label).setFontColor(COLOR_TEXT_MUTED).setFont(regular).setFontSize(8))
                .add(new Text("  " + value).setFontColor(COLOR_TEXT_DARK).setFont(regular).setFontSize(big ? 11 : 9))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(3);
    }
}
