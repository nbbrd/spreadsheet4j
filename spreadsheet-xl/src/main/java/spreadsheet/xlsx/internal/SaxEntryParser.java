/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package spreadsheet.xlsx.internal;

import shaded.spreadsheet.nbbrd.io.function.IOSupplier;
import shaded.spreadsheet.nbbrd.io.xml.Sax;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import spreadsheet.xlsx.XlsxEntryParser;
import spreadsheet.xlsx.XlsxDataType;

/**
 *
 * @author Philippe Charles
 */
public final class SaxEntryParser implements XlsxEntryParser {

    @lombok.NonNull
    private final XMLReader reader;

    public SaxEntryParser(XMLReader reader) {
        this.reader = disableNamespaces(reader);
    }

    @Override
    public void visitWorkbook(InputStream stream, WorkbookVisitor visitor) throws IOException {
        visit(new WorkbookSaxEventHandler(visitor), stream);
    }

    @Override
    public void visitSharedStrings(InputStream stream, SharedStringsVisitor visitor) throws IOException {
        visit(new SharedStringsSaxEventHandler(visitor), stream);
    }

    @Override
    public void visitStyles(InputStream stream, StylesVisitor visitor) throws IOException {
        visit(new StylesSaxEventHandler(visitor), stream);
    }

    @Override
    public void visitSheet(InputStream stream, SheetVisitor visitor) throws IOException {
        visit(new SheetSaxEventHandler(visitor), stream);
    }

    @Override
    public void close() throws IOException {
    }

    private void visit(ContentHandler handler, InputStream stream) throws IOException {
        Sax.Parser.builder().factory(() -> reader).contentHandler(handler).after(VOID).build().parseStream(stream);
    }

    private static final IOSupplier VOID = IOSupplier.of(null);

    @lombok.RequiredArgsConstructor
    private static final class SheetSaxEventHandler extends DefaultHandler {

        private final SheetVisitor visitor;
        private final SaxStringBuilder stringBuilder = new SaxStringBuilder();
        private int level = 0;
        private String sheetBounds = null;
        private String ref = null;
        private String rawDataType = null;
        private String rawStyleIndex = null;

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            switch (level) {
                case 1:
                    switch (name) {
                        case SHEET_DIMENSIONS_TAG:
                            sheetBounds = attributes.getValue(SHEET_BOUNDS_ATTRIBUTE);
                            break;
                        case SHEET_DATA_TAG:
                            visitor.onSheetData(sheetBounds);
                            break;
                    }
                    break;
                case 3:
                    if (isEqualTo(name, CELL_TAG)) {
                        parseCellAttributes(attributes);
                    }
                    break;
                case 4:
                    if (isChar(name)) {
                        if (name.charAt(0) == CELL_VALUE_TAG) {
                            stringBuilder.enable().clear();
                        }
                    } else {
                        if (name.equals(INLINE_STRING_TAG)) {
                            stringBuilder.enable().clear();
                        }
                    }
                    break;
            }
            level++;
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            level--;
            switch (level) {
                case 4:
                    if (isChar(name)) {
                        if (name.charAt(0) == CELL_VALUE_TAG) {
                            pushCellValue();
                        }
                    } else {
                        if (name.equals(INLINE_STRING_TAG)) {
                            pushCellValue();
                        }
                    }
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.appendIfNeeded(ch, start, length);
        }

        private void pushCellValue() {
            if (ref != null) {
                XlsxDataType dataType = parseDataType(rawDataType);
                int styleIndex = XlsxValueFactory.isStyleRequired(dataType)
                        ? XlsxValueFactory.parseStyleIndex(rawStyleIndex)
                        : XlsxValueFactory.NULL_STYLE_INDEX;
                visitor.onCell(ref, stringBuilder.disable().build(), dataType, styleIndex);
            }
        }

        private void parseCellAttributes(Attributes attributes) {
            ref = null;
            rawDataType = null;
            rawStyleIndex = null;
            for (int i = 0; i < attributes.getLength(); i++) {
                String attribute = attributes.getLocalName(i);
                if (isChar(attribute)) {
                    switch (attribute.charAt(0)) {
                        case REFERENCE_ATTRIBUTE:
                            ref = attributes.getValue(i);
                            break;
                        case CELL_DATA_TYPE_ATTRIBUTE:
                            rawDataType = attributes.getValue(i);
                            break;
                        case STYLE_INDEX_ATTRIBUTE:
                            rawStyleIndex = attributes.getValue(i);
                            break;
                    }
                }
            }
        }

        private static final char CELL_TAG = 'c';
        private static final char REFERENCE_ATTRIBUTE = 'r';
        private static final char STYLE_INDEX_ATTRIBUTE = 's';
        private static final char CELL_DATA_TYPE_ATTRIBUTE = 't';
        private static final char CELL_VALUE_TAG = 'v';
        private static final String SHEET_DIMENSIONS_TAG = "dimension";
        private static final String SHEET_BOUNDS_ATTRIBUTE = "ref";
        private static final String SHEET_DATA_TAG = "sheetData";
        private static final String INLINE_STRING_TAG = "is";

        @NonNull
        private static XlsxDataType parseDataType(@Nullable String rawDataType) {
            if (rawDataType == null) {
                return XlsxDataType.UNDEFINED;
            }
            if (isChar(rawDataType)) {
                switch (rawDataType.charAt(0)) {
                    case NUMBER_TYPE:
                        return XlsxDataType.NUMBER;
                    case SHARED_STRING_TYPE:
                        return XlsxDataType.SHARED_STRING;
                    case DATE_TYPE:
                        return XlsxDataType.DATE;
                    case BOOLEAN_TYPE:
                        return XlsxDataType.BOOLEAN;
                    case ERROR_TYPE:
                        return XlsxDataType.ERROR;
                    default:
                        return XlsxDataType.UNKNOWN;
                }
            }
            switch (rawDataType) {
                case STRING_TYPE:
                    return XlsxDataType.STRING;
                case INLINE_STRING_TYPE:
                    return XlsxDataType.INLINE_STRING;
                default:
                    return XlsxDataType.UNKNOWN;
            }
        }

        // http://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.cellvalues.aspx
        private static final char BOOLEAN_TYPE = 'b';
        private static final char NUMBER_TYPE = 'n';
        private static final char ERROR_TYPE = 'e';
        private static final char SHARED_STRING_TYPE = 's';
        private static final String STRING_TYPE = "str";
        private static final String INLINE_STRING_TYPE = "inlineStr";
        private static final char DATE_TYPE = 'd';
    }

    /**
     * http://msdn.microsoft.com/en-us/library/office/documentformat.openxml.spreadsheet.aspx
     */
    @lombok.RequiredArgsConstructor
    private static final class WorkbookSaxEventHandler extends DefaultHandler {

        private final WorkbookVisitor visitor;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case SHEET_TAG:
                    visitor.onSheet(attributes.getValue(SHEET_TAB_ID_ATTRIBUTE), attributes.getValue(SHEET_NAME_ATTRIBUTE));
                    break;
                case WORKBOOK_PROPERTIES_TAG:
                    visitor.onDate1904(Boolean.parseBoolean(attributes.getValue(DATE1904_ATTRIBUTE)));
                    break;
            }
        }

        private static final String SHEET_TAG = "sheet";
        private static final String WORKBOOK_PROPERTIES_TAG = "workbookPr";
        private static final String DATE1904_ATTRIBUTE = "date1904";
        private static final String SHEET_TAB_ID_ATTRIBUTE = "r:id";
        private static final String SHEET_NAME_ATTRIBUTE = "name";
    }

    /**
     * http://msdn.microsoft.com/en-us/library/office/gg278314.aspx
     */
    @lombok.RequiredArgsConstructor
    private static final class SharedStringsSaxEventHandler extends DefaultHandler {

        private final SharedStringsVisitor visitor;
        private final SaxStringBuilder stringBuilder = new SaxStringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case SHARED_STRING_ITEM_TAG:
                    stringBuilder.clear();
                    break;
                case TEXT_TAG:
                    stringBuilder.enable();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case TEXT_TAG:
                    stringBuilder.disable();
                    break;
                case SHARED_STRING_ITEM_TAG:
                    visitor.onSharedString(stringBuilder.build());
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.appendIfNeeded(ch, start, length);
        }

        private static final String SHARED_STRING_ITEM_TAG = "si";
        private static final String TEXT_TAG = "t";
    }

    @lombok.RequiredArgsConstructor
    private static final class StylesSaxEventHandler extends DefaultHandler {

        private final StylesVisitor visitor;
        private boolean insideGroupTag = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case CELL_FORMATS_TAG:
                    insideGroupTag = true;
                    break;
                case CELL_FORMAT_TAG:
                    if (insideGroupTag) {
                        try {
                            visitor.onCellFormat(Integer.parseInt(attributes.getValue(NUMBER_FORMAT_ID_ATTRIBUTE)));
                        } catch (NumberFormatException ex) {
                            throw new SAXException(ex);
                        }
                    }
                    break;
                case NUMBER_FORMATS_TAG:
                    insideGroupTag = true;
                    break;
                case NUMBER_FORMAT_TAG:
                    if (insideGroupTag) {
                        try {
                            visitor.onNumberFormat(
                                    Integer.parseInt(attributes.getValue(NUMBER_FORMAT_ID_ATTRIBUTE)),
                                    attributes.getValue(NUMBER_FORMAT_CODE_ATTRIBUTE));
                        } catch (NumberFormatException ex) {
                            throw new SAXException(ex);
                        }
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(CELL_FORMATS_TAG) || qName.equals(NUMBER_FORMATS_TAG)) {
                insideGroupTag = false;
            }
        }

        private static final String CELL_FORMAT_TAG = "xf";
        private static final String CELL_FORMATS_TAG = "cellXfs";
        private static final String NUMBER_FORMAT_TAG = "numFmt";
        private static final String NUMBER_FORMATS_TAG = "numFmts";
        private static final String NUMBER_FORMAT_ID_ATTRIBUTE = "numFmtId";
        private static final String NUMBER_FORMAT_CODE_ATTRIBUTE = "formatCode";
    }

    private static final class SaxStringBuilder {

        private boolean enabled = false;
        private char[] buffer = new char[64];
        private int bufferLength = 0;

        public SaxStringBuilder clear() {
            bufferLength = 0;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public SaxStringBuilder enable() {
            this.enabled = true;
            return this;
        }

        public SaxStringBuilder disable() {
            this.enabled = false;
            return this;
        }

        public String build() {
            return new String(buffer, 0, bufferLength);
        }

        public SaxStringBuilder appendIfNeeded(char[] ch, int start, int length) {
            if (isEnabled()) {
                int expectedLength = bufferLength + length;
                if (expectedLength > buffer.length) {
                    buffer = Arrays.copyOf(buffer, expectedLength);
                }
                System.arraycopy(ch, start, buffer, bufferLength, length);
                bufferLength = expectedLength;
            }
            return this;
        }
    }

    static boolean isChar(String input) {
        return input.length() == 1;
    }

    static boolean isEqualTo(String input, char c) {
        return input.length() == 1 && input.charAt(0) == c;
    }

    static XMLReader disableNamespaces(XMLReader reader) {
        try {
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            Logger.getLogger(SaxEntryParser.class.getName()).log(Level.FINE, null, ex);
        }
        return reader;
    }

    public static final XlsxEntryParser.Factory FACTORY = () -> new SaxEntryParser(Sax.createReader());
}
