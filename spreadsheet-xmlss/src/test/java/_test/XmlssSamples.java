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
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.atIndex;

/**
 * @author Philippe Charles
 */
public final class XmlssSamples {

    private XmlssSamples() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final String XML_CONTENT = Sample.contentOf(XmlssSamples.class, "/Top5Browsers.xml", StandardCharsets.UTF_8);

    public static final SampleSet XML_TOP5 = SampleSet
            .builder()
            .valid(Sample.of("valid.xml", XML_CONTENT))
            .validWithTail(Sample.of("validWithTail.xml", XML_CONTENT + "\0"))
            .invalidContent(Sample.of("invalidContent.xml", XML_CONTENT.replace("<?mso-application progid=\"Excel.Sheet\"?>", "")))
            .invalidFormat(Sample.of("invalidFormat.xml", "..."))
            .empty(Sample.of("empty.xml", ""))
            .missing(Sample.of("missing.xml"))
            .badExtension(Sample.of("badExtension.zip", XML_CONTENT))
            .build();

    public static void assertTop5Book(Book book) throws IOException {
        BookAssert
                .assertThat(book)
                .hasSheetCount(3)
                .satisfies(XmlssSamples::assertTop5Sheet1, atIndex(0));
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
