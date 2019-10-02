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

import _test.EmptyInputStream;
import ec.util.spreadsheet.tck.SheetAssert;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxSheetBuilder;
import internal.spreadsheet.ioutil.IO;
import internal.spreadsheet.ioutil.Sax;
import spreadsheet.xlsx.XlsxEntryParser;

/**
 *
 * @author Philippe Charles
 */
public class SaxEntryParserTest {

    private final IO.Function<String, InputStream> files = o -> IO.getResourceAsStream(SaxEntryParserTest.class, o).orElseThrow(IOException::new);
    private final IO.Supplier<InputStream> empty = EmptyInputStream::new;
    private final IO.Supplier<InputStream> throwing = IO.Supplier.throwing(CustomIOException::new);

    @Test
    public void testWorkbookSax2EventHandler() throws IOException {
        XlsxEntryParser parser = new SaxEntryParser(Sax.createReader());

        XlsxBook.WorkbookData data = XlsxBook.parseWorkbook(() -> files.applyWithIO("/workbook.xml"), parser);
        assertThat(data.getSheets())
                .extracting("name", "relationId")
                .containsExactly(
                        tuple("Top 5 Browsers - Monthly", "rId1"),
                        tuple("Top 5 Browsers - Quarterly", "rId2"));
        assertFalse(data.isDate1904());

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parser))
                .isInstanceOf(EOFException.class)
                .hasNoCause();

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testSheetSax2EventHandler() throws IOException {
        XlsxEntryParser parser = new SaxEntryParser(Sax.createReader());

        XlsxSheetBuilder b = MultiSheetBuilder.of(
                DefaultDateSystem.X1904,
                Arrays.asList("1", "2", "3", "4", "5", "6", "7"),
                new boolean[]{false, true}
        );

        SheetAssert.assertThat(XlsxBook.parseSheet("regular", b, () -> files.applyWithIO("/RegularXlsxSheet.xml"), parser))
                .hasName("regular")
                .hasColumnCount(7)
                .hasRowCount(42);

        SheetAssert.assertThat(XlsxBook.parseSheet("formulas", b, () -> files.applyWithIO("/FormulasXlsxSheet.xml"), parser))
                .hasName("formulas")
                .hasColumnCount(7)
                .hasRowCount(42);

        SheetAssert.assertThat(XlsxBook.parseSheet("inlineStrings", b, () -> files.applyWithIO("/InlineStrings.xml"), parser))
                .hasName("inlineStrings")
                .hasColumnCount(1)
                .hasRowCount(1)
                .hasCellValue(0, 0, "hello world");

        assertThatThrownBy(() -> XlsxBook.parseSheet("empty", b, empty, parser))
                .isInstanceOf(EOFException.class)
                .hasNoCause();

        assertThatThrownBy(() -> XlsxBook.parseSheet("missing", b, throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testSharedStringsSax2EventHandler() throws IOException {
        XlsxEntryParser parser = new SaxEntryParser(Sax.createReader());

        assertThat(XlsxBook.parseSharedStrings(() -> files.applyWithIO("/Sst.xml"), parser))
                .contains("Cell A1", atIndex(0))
                .contains("Cell B2", atIndex(4));

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parser))
                .isInstanceOf(EOFException.class)
                .hasNoCause();

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testStylesSax2EventHandler() throws IOException {
        XlsxEntryParser parser = new SaxEntryParser(Sax.createReader());

        XlsxNumberingFormat df = DefaultNumberingFormat.INSTANCE;

        assertThat(XlsxBook.parseStyles(df, () -> files.applyWithIO("/styles.xml"), parser))
                .containsExactly(false, true);

        assertThatThrownBy(() -> XlsxBook.parseStyles(df, empty, parser))
                .isInstanceOf(EOFException.class)
                .hasNoCause();

        assertThatThrownBy(() -> XlsxBook.parseStyles(df, throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    private static final class CustomIOException extends IOException {

    }
}
