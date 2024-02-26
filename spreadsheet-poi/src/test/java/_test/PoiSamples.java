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
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.tck.BookAssert;
import ec.util.spreadsheet.tck.Sample;
import ec.util.spreadsheet.tck.SampleSet;
import ec.util.spreadsheet.tck.SheetAssert;
import org.assertj.core.util.DateUtil;

import java.io.IOException;

import static org.assertj.core.api.Assertions.atIndex;

/**
 * @author Philippe Charles
 */
public final class PoiSamples {

    private PoiSamples() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final byte[] XLS_BYTES = Sample.bytesOf(PoiSamples.class, "/Top5Browsers.xls");

    public static final SampleSet XLS_TOP5 = SampleSet
            .builder()
            .valid(Sample.of("valid.xls", XLS_BYTES))
            .validWithTail(Sample.of("validWithTail.xls", Sample.concat(XLS_BYTES, (byte) '\0')))
            .invalidContent(null)
            .invalidFormat(Sample.of("invalidFormat.xls", "..."))
            .empty(Sample.of("empty.xls", new byte[0]))
            .missing(Sample.of("missing.xls"))
            .badExtension(Sample.of("badExtension.xml", XLS_BYTES))
            .build();

    private static final byte[] XLSX_BYTES = Sample.bytesOf(PoiSamples.class, "/Top5Browsers.xlsx");

    public static final SampleSet XLSX_TOP5 = SampleSet
            .builder()
            .valid(Sample.of("valid.xlsx", XLSX_BYTES))
            .validWithTail(Sample.of("validWithTail.xlsx", Sample.concat(XLSX_BYTES, (byte) '\0')))
            .invalidContent(null)
            .invalidFormat(Sample.of("invalidFormat.xlsx", "..."))
            .empty(Sample.of("empty.xlsx", new byte[0]))
            .missing(Sample.of("missing.xlsx"))
            .badExtension(Sample.of("badExtension.xml", XLSX_BYTES))
            .build();

    public static void assertTop5Book(Book book) throws IOException {
        BookAssert
                .assertThat(book)
                .hasSheetCount(3)
                .satisfies(PoiSamples::assertTop5Sheet1, atIndex(0));
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
