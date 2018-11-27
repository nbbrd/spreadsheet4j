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

import spreadsheet.xlsx.XlsxDataType;
import ec.util.spreadsheet.SheetAssert;
import ioutil.IO;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxSheetBuilder;
import test.EmptyInputStream;
import spreadsheet.xlsx.XlsxEntryParser;

/**
 *
 * @author Philippe Charles
 */
public class XlsxBookTest {

    private final IO.Supplier<? extends InputStream> empty = EmptyInputStream::new;
    private final IO.Supplier<? extends InputStream> boom = IO.Supplier.throwing(CustomIOException::new);
    private final XlsxEntryParser emptyParser = new NoOpParser();

    @Test
    @SuppressWarnings("null")
    public void testParseSharedStrings() throws IOException {
        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThat(XlsxBook.parseSharedStrings(empty, emptyParser)).isEmpty();

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> o.onSharedString(null))))
                .isInstanceOf(NullPointerException.class);

        assertThat(XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> {
            o.onSharedString("hello");
            o.onSharedString("world");
        }))).containsExactly("hello", "world");
    }

    @Test
    public void testParseSheet() throws IOException {
        XlsxSheetBuilder builder = DefaultSheetBuilder.of(
                DefaultDateSystem.X1900,
                Arrays.asList("hello", "world"),
                new boolean[]{false, true}
        );

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onCell("A1", "hello", XlsxDataType.STRING, XlsxValueFactory.NULL_STYLE_INDEX);
        }))).as("Must follow call order").isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onSheetData(null);
            o.onSheetData(null);
        }))).as("Must follow call order").isInstanceOf(IllegalStateException.class);

        SheetAssert.assertThat(XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onSheetData(null);
            o.onCell("A1", "hello", XlsxDataType.STRING, XlsxValueFactory.NULL_STYLE_INDEX);
        }))).hasName("").hasRowCount(1).hasColumnCount(1).hasCellValue(0, 0, "hello");
    }

    @Test
    public void testParseStyles() throws IOException {
        XlsxNumberingFormat nf = DefaultNumberingFormat.INSTANCE;

        assertThatThrownBy(() -> XlsxBook.parseStyles(nf, boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThat(XlsxBook.parseStyles(nf, empty, emptyParser)).isEmpty();

        assertThatThrownBy(() -> XlsxBook.parseStyles(nf, empty, parserOnStyles(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThat(XlsxBook.parseStyles(nf, empty, parserOnStyles(o -> {
            o.onNumberFormat(1000, "");
            o.onCellFormat(14);
        }))).containsExactly(true);
    }

    @Test
    @SuppressWarnings("null")
    public void testParseWorkbook() throws IOException {
        assertThatThrownBy(() -> XlsxBook.parseWorkbook(boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onSheet(null, "");
        }))).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onSheet("", null);
        }))).isInstanceOf(NullPointerException.class);

        assertThat(XlsxBook.parseWorkbook(empty, emptyParser))
                .isEqualTo(new XlsxBook.WorkbookData(new ArrayList<>(), false));

        assertThat(XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onDate1904(true);
            o.onSheet("rId1", "hello");
        }))).isEqualTo(new XlsxBook.WorkbookData(new ArrayList<>(Arrays.asList(new XlsxBook.SheetMeta("rId1", "hello"))), true));
    }

    private static XlsxEntryParser parserOnSharedStrings(IO.Consumer<? super XlsxEntryParser.SharedStringsVisitor> consumer) {
        return new NoOpParser() {
            @Override
            public void visitSharedStrings(InputStream s, XlsxEntryParser.SharedStringsVisitor v) throws IOException {
                consumer.acceptWithIO(v);
            }
        };
    }

    private static XlsxEntryParser parserOnSheet(IO.Consumer<? super XlsxEntryParser.SheetVisitor> consumer) {
        return new NoOpParser() {
            @Override
            public void visitSheet(InputStream s, XlsxEntryParser.SheetVisitor v) throws IOException {
                consumer.acceptWithIO(v);
            }
        };
    }

    private static XlsxEntryParser parserOnStyles(IO.Consumer<? super XlsxEntryParser.StylesVisitor> consumer) {
        return new NoOpParser() {
            @Override
            public void visitStyles(InputStream s, XlsxEntryParser.StylesVisitor v) throws IOException {
                consumer.acceptWithIO(v);
            }
        };
    }

    private static XlsxEntryParser parserOnWorkbook(IO.Consumer<? super XlsxEntryParser.WorkbookVisitor> consumer) {
        return new NoOpParser() {
            @Override
            public void visitWorkbook(InputStream s, XlsxEntryParser.WorkbookVisitor v) throws IOException {
                consumer.acceptWithIO(v);
            }
        };
    }

    private static class NoOpParser implements XlsxEntryParser {

        @Override
        public void visitWorkbook(InputStream s, WorkbookVisitor v) throws IOException {
        }

        @Override
        public void visitSharedStrings(InputStream s, SharedStringsVisitor v) throws IOException {
        }

        @Override
        public void visitStyles(InputStream s, StylesVisitor v) throws IOException {
        }

        @Override
        public void visitSheet(InputStream s, SheetVisitor v) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static final class CustomIOException extends IOException {

    }
}
