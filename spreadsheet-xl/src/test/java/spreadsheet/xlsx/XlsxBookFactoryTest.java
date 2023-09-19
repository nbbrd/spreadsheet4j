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
package spreadsheet.xlsx;

import _test.XlSamples;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryLoader;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;

import static _test.XlSamples.XLSX_TOP5;
import static ec.util.spreadsheet.tck.Conditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class XlsxBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        BookFactoryAssert.assertThat(new XlsxBookFactory())
                .isCompliant(XLSX_TOP5, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        assertThat(new XlsxBookFactory())
                .is(ableToLoadContent())
                .isNot(ableToStoreContent());
    }

    @Test
    public void testIsSupportedDataType() {
        assertThat(new XlsxBookFactory())
                .is(supportingDataType(Date.class))
                .is(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        assertThat(new XlsxBookFactory())
                .is(acceptingFile(XLSX_TOP5.getValid().file(temp)))
                .is(acceptingFile(XLSX_TOP5.getMissing().file(temp)))
                .is(acceptingFile(XLSX_TOP5.getValidWithTail().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getBadExtension().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getInvalidFormat().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getEmpty().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        assertThat(new XlsxBookFactory())
                .is(acceptingPath(XLSX_TOP5.getValid().path(temp)))
                .is(acceptingPath(XLSX_TOP5.getMissing().path(temp)))
                .is(acceptingPath(XLSX_TOP5.getValidWithTail().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getBadExtension().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getInvalidFormat().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getEmpty().path(temp)));
    }

    @Test
    public void testLoadFile(@TempDir Path temp) throws IOException {
        XlsxBookFactory x = new XlsxBookFactory();

        try (Book book = x.load(XLSX_TOP5.getValid().path(temp))) {
            XlSamples.assertTop5Book(book);
        }
        try (Book book = x.load(XLSX_TOP5.getBadExtension().path(temp))) {
            XlSamples.assertTop5Book(book);
        }
        try (Book book = x.load(XLSX_TOP5.getValidWithTail().path(temp))) {
            XlSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> x.load(XLSX_TOP5.getInvalidFormat().path(temp)));
        assertThatIOException().isThrownBy(() -> x.load(XLSX_TOP5.getEmpty().path(temp)));
        assertThatIOException().isThrownBy(() -> x.load(XLSX_TOP5.getMissing().path(temp)));
    }

    @Test
    public void testLoadStream() throws IOException {
        XlsxBookFactory x = new XlsxBookFactory();

        try (ArrayBook book = XLSX_TOP5.getValid().loadStream(x)) {
            XlSamples.assertTop5Book(book);
        }
        try (ArrayBook book = XLSX_TOP5.getBadExtension().loadStream(x)) {
            XlSamples.assertTop5Book(book);
        }
        try (ArrayBook book = XLSX_TOP5.getValidWithTail().loadStream(x)) {
            XlSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> XLSX_TOP5.getInvalidFormat().loadStream(x));
        assertThatIOException().isThrownBy(() -> XLSX_TOP5.getEmpty().loadStream(x));
    }

    @Test
    public void testRank() {
        assertThat(BookFactoryLoader.get())
                .hasSize(3)
                .isSortedAccordingTo(Comparator.comparingInt(Book.Factory::getRank).reversed())
                .extracting(factory -> factory.getClass().getSimpleName())
                .containsExactly("XlsxBookFactory", "ExcelBookFactory", "ExcelClassicBookFactory");
    }
}
