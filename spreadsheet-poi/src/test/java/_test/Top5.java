/*
 * Copyright 2017 National Bank of Belgium
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
package _test;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.tck.BookAssert;
import ec.util.spreadsheet.tck.Sample;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.tck.SheetAssert;
import java.io.IOException;
import static org.assertj.core.api.Assertions.atIndex;
import org.assertj.core.util.DateUtil;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Top5 {

    private static final byte[] CONTENT = Sample.bytesOf(Top5.class.getResource("/Top5Browsers.xls"));

    public final Sample VALID = Sample.of("valid.xls", CONTENT);
    public final Sample VALID_WITH_TAIL = Sample.of("validWithTail.xls", Sample.concat(CONTENT, (byte) '\0'));
    public final Sample INVALID_FORMAT = Sample.of("invalidFormat.xls", "...");
    public final Sample EMPTY = Sample.of("empty.xls", new byte[0]);
    public final Sample MISSING = Sample.of("missing.xls");
    public final Sample BAD_EXTENSION = Sample.of("badExtension.xml", CONTENT);

    public static void assertTop5Book(Book book) throws IOException {
        BookAssert
                .assertThat(book)
                .hasSheetCount(3)
                .satisfies(Top5::assertTop5Sheet1, atIndex(0));
    }

    public static void assertTop5Sheet1(Sheet sheet) {
        SheetAssert
                .assertThat(sheet)
                .hasName("Top 5 Browsers - Monthly")
                .hasRowCount(42)
                .hasColumnCount(7)
                .hasCellValue(0, 0, null)
                .hasCellValue(0, 1, "IE")
                .hasCellValue(1, 0, DateUtil.parse("2008-07-01"))
                .hasCellValue(41, 6, 0.93);
    }
}
