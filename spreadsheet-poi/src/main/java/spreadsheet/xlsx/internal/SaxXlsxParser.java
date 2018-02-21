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

import ioutil.IO;
import ioutil.Sax;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import spreadsheet.xlsx.XlsxParser;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class SaxXlsxParser implements XlsxParser {

    @lombok.NonNull
    private final XMLReader reader;

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
        Sax.Parser.builder().factory(() -> reader).handler(handler).after(VOID).build().parseStream(stream);
    }

    private static final IO.Supplier VOID = IO.Supplier.of(null);

    /**
     * FIXME: missing support of inline string <is><t>hello</t></is>
     */
    @lombok.RequiredArgsConstructor
    private static final class SheetSaxEventHandler extends DefaultHandler {

        private final SheetVisitor visitor;
        private final SaxStringBuilder stringBuilder = new SaxStringBuilder();
        private String sheetBounds = null;
        private String ref = null;
        private String rawDataType = null;
        private Integer rawStyleIndex = null;

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            switch (name) {
                case CELL_TAG:
                    ref = attributes.getValue(REFERENCE_ATTRIBUTE);
                    rawDataType = attributes.getValue(CELL_DATA_TYPE_ATTRIBUTE);
                    String tmp = attributes.getValue(STYLE_INDEX_ATTRIBUTE);
                    try {
                        rawStyleIndex = tmp != null ? Integer.valueOf(tmp) : null;
                    } catch (NumberFormatException ex) {
                        throw new SAXException(ex);
                    }
                    break;
                case CELL_VALUE_TAG:
                    stringBuilder.enable().clear();
                    break;
                case SHEET_DIMENSIONS_TAG:
                    sheetBounds = attributes.getValue(SHEET_BOUNDS_ATTRIBUTE);
                    break;
                case SHEET_DATA_TAG:
                    visitor.onSheetData(sheetBounds);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (stringBuilder.isEnabled() && (name.equals(CELL_VALUE_TAG) /*|| name.equals(INLINE_STRING_TAG)*/)) {
                visitor.onCell(ref, stringBuilder.disable().build(), rawDataType, rawStyleIndex);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.appendIfNeeded(ch, start, length);
        }

        private static final String CELL_TAG = "c";
        private static final String REFERENCE_ATTRIBUTE = "r";
        private static final String STYLE_INDEX_ATTRIBUTE = "s";
        private static final String CELL_DATA_TYPE_ATTRIBUTE = "t";
        private static final String CELL_VALUE_TAG = "v";
        private static final String SHEET_DIMENSIONS_TAG = "dimension";
        private static final String SHEET_BOUNDS_ATTRIBUTE = "ref";
        private static final String SHEET_DATA_TAG = "sheetData";
        //private static final String INLINE_STRING_TAG = "is";
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
                    visitor.onSharedString(stringBuilder.build().toString());
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
        private StringBuilder content = new StringBuilder();

        public SaxStringBuilder clear() {
            content = new StringBuilder();
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

        public CharSequence build() {
            // we defer CharSequence@toString()
            return content;
        }

        public SaxStringBuilder appendIfNeeded(char[] ch, int start, int length) {
            if (isEnabled()) {
                content.append(ch, start, length);
            }
            return this;
        }
    }
}
