/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package ec.util.spreadsheet.html;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
final class HtmlBookWriter {

    private static final String STYLE = getStyleContent();

    private final XMLOutputFactory xof;
    @Deprecated
    private DateFormat dateFormat;
    @Deprecated
    private NumberFormat numberFormat;
    private Charset charset;

    HtmlBookWriter(@NonNull XMLOutputFactory xof) {
        this.xof = Objects.requireNonNull(xof);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        this.numberFormat = NumberFormat.getNumberInstance(Locale.ROOT);
        numberFormat.setMaximumFractionDigits(9);
        numberFormat.setMaximumIntegerDigits(12);
        this.charset = StandardCharsets.UTF_8;
    }

    @Deprecated
    public void setDateFormat(@NonNull DateFormat dateFormat) {
        this.dateFormat = Objects.requireNonNull(dateFormat);
    }

    @Deprecated
    public void setNumberFormat(@NonNull NumberFormat numberFormat) {
        this.numberFormat = Objects.requireNonNull(numberFormat);
    }

    public void setCharset(@NonNull Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    public void write(@NonNull Book book, @NonNull OutputStream stream) throws IOException {
        getFormatter().formatStream(book, stream);
    }

    public void write(@NonNull Book book, @NonNull Writer writer) throws IOException {
        getFormatter().formatWriter(book, writer);
    }

    @NonNull
    public String writeToString(@NonNull Book book) throws IOException {
        return getFormatter().formatToString(book);
    }

    private Xml.Formatter<Book> getFormatter() {
        return Stax.StreamFormatter
                .<Book>builder()
                .factory(() -> xof)
                .encoding(charset)
                .handler(this::writeHtml)
                .build();
    }

    private void writeHtml(Book book, XMLStreamWriter w) throws XMLStreamException, IOException {
        BasicHtmlWriter f = new BasicHtmlWriter(w);
        f.beginHtml();
        f.beginHead();
        f.writeStyle(STYLE);
        f.endHead();
        f.beginBody();
        int sheetCount = book.getSheetCount2();
        for (int s = 0; s < sheetCount; s++) {
            writeTable(f, book.getSheet(s));
        }
        f.endBody();
        f.endHtml();
    }

    private void writeTable(BasicHtmlWriter f, Sheet sheet) throws XMLStreamException {
        f.beginTable(sheet.getName(), "sheet");
        int rowCount = sheet.getRowCount();
        int columnCount = sheet.getColumnCount();
        // content
        for (int i = 0; i < rowCount; i++) {
            f.beginRow();
            for (int j = 0; j < columnCount; j++) {
                Object cellValue = sheet.getCellValue(i, j);
                if (cellValue != null) {
                    if (cellValue instanceof Date) {
                        f.writeCell(dateFormat.format((Date) cellValue), false, "type-date");
                    } else if (cellValue instanceof Number) {
                        f.writeCell(numberFormat.format(((Number) cellValue).doubleValue()), false, "type-number");
                    } else if (cellValue instanceof String) {
                        f.writeCell((String) cellValue, false, "");
                    }
                } else {
                    f.writeCell("", false, "");
                }
            }
            f.endRow();
        }
        f.endTable();
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static String getStyleContent() {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(HtmlBookWriter.class.getResourceAsStream("/BasicStyle.css"), StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class BasicHtmlWriter {

        private final XMLStreamWriter writer;

        public BasicHtmlWriter(XMLStreamWriter writer) {
            this.writer = writer;
        }

        public void beginHtml() throws XMLStreamException {
//        writer.writeStartDocument("utf-8", "1.0");
            writer.writeStartDocument();
            writer.writeStartElement("html");
        }

        public void endHtml() throws XMLStreamException {
            writer.writeEndElement(); // html
            writer.writeEndDocument();
            writer.flush();
        }

        public void beginHead() throws XMLStreamException {
            writer.writeStartElement("head");
            writer.writeEmptyElement("meta");
            writer.writeAttribute("charset", "utf-8");
        }

        public void endHead() throws XMLStreamException {
            writer.writeEndElement(); // head
        }

        public void writeStyle(String content) throws XMLStreamException {
            writer.writeStartElement("style");
            writer.writeAttribute("media", "screen");
            writer.writeAttribute("type", "text/css");
            writer.writeCharacters(content);
            writer.writeEndElement();
        }

        public void beginBody() throws XMLStreamException {
            writer.writeStartElement("body");
        }

        public void endBody() throws XMLStreamException {
            writer.writeEndElement(); // body
        }

        public void writeCaption(String text) throws XMLStreamException {
            writer.writeStartElement("caption");
            writer.writeCharacters(text);
            writer.writeEndElement(); // caption
        }

        public void beginTable(String name, String style) throws XMLStreamException {
            writer.writeStartElement("table");
            if (!name.isEmpty()) {
                writer.writeAttribute("id", name);
            }
            if (!style.isEmpty()) {
                writer.writeAttribute("class", style);
            }
            writeCaption(name);
        }

        public void endTable() throws XMLStreamException {
            writer.writeEndElement(); // table
            writer.flush();
        }

        public void beginRow() throws XMLStreamException {
            writer.writeStartElement("tr");
        }

        public void endRow() throws XMLStreamException {
            writer.writeEndElement(); // row
        }

        public void writeCell(CharSequence characters, boolean header, String style) throws XMLStreamException {
            if (characters.length() > 0) {
                writer.writeStartElement(header ? "th" : "td");
                if (!style.isEmpty()) {
                    writer.writeAttribute("class", style);
                }
                writer.writeCharacters(characters.toString());
                writer.writeEndElement(); // cell
            } else {
                writer.writeEmptyElement(header ? "th" : "td");
                if (!style.isEmpty()) {
                    writer.writeAttribute("class", style);
                }
            }
        }
    }
    //</editor-fold>
}
