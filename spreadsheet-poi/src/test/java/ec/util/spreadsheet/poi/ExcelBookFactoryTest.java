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
package ec.util.spreadsheet.poi;

import _test.Top5x;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryAssert;
import ioutil.IO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.*;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class ExcelBookFactoryTest {

    @ClassRule
    public static TemporaryFolder TEMP = new TemporaryFolder();
    private static ExcelBookFactory[] FACTORIES;
    private static File ORIGINAL;
    private static File BAD_EXTENSION;
    private static File WITH_TRAILING_SECTION;
    private static File NOT_XLSX;
    private static File EMPTY;
    private static File MISSING;

    @BeforeClass
    public static void initFiles() {
        FACTORIES = new ExcelBookFactory[]{new ExcelBookFactory(), new ExcelBookFactory()};
        FACTORIES[0].setFast(true);
        FACTORIES[1].setFast(false);
        ORIGINAL = Top5x.ORIGINAL.file(TEMP);
        BAD_EXTENSION = Top5x.BAD_EXTENSION.file(TEMP);
        WITH_TRAILING_SECTION = Top5x.WITH_TRAILING_SECTION.file(TEMP);
        NOT_XLSX = Top5x.NOT_XLSX.file(TEMP);
        EMPTY = Top5x.EMPTY.file(TEMP);
        MISSING = Top5x.MISSING.file(TEMP);
    }

    @Test
    public void testCompliance() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            BookFactoryAssert.assertThat(x).isCompliant(ORIGINAL, NOT_XLSX);
        }
    }

    @Test
    public void testLoadFile() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            try (Book book = x.load(ORIGINAL)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = x.load(BAD_EXTENSION)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = x.load(WITH_TRAILING_SECTION)) {
                Top5x.assertTop5Book(book);
            }
            assertThatIOException().isThrownBy(() -> x.load(NOT_XLSX));
            assertThatIOException().isThrownBy(() -> x.load(EMPTY));
            assertThatIOException().isThrownBy(() -> x.load(MISSING));
        }
    }

    private static Book doLoad(ExcelBookFactory x, IO.Supplier<InputStream> byteSource) throws IOException {
        try (InputStream stream = byteSource.getWithIO()) {
            return x.load(stream);
        }
    }

    @Test
    public void testLoadStream() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            try (Book book = doLoad(x, Top5x.ORIGINAL::stream)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = doLoad(x, Top5x.BAD_EXTENSION::stream)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = doLoad(x, Top5x.WITH_TRAILING_SECTION::stream)) {
                Top5x.assertTop5Book(book);
            }
            assertThatIOException().isThrownBy(() -> doLoad(x, Top5x.NOT_XLSX::stream));
            assertThatIOException().isThrownBy(() -> doLoad(x, Top5x.EMPTY::stream));
        }
    }

    @Test
    public void testAcceptFile() {
        for (ExcelBookFactory x : FACTORIES) {
            assertThat(x.accept(ORIGINAL)).isTrue();
            assertThat(x.accept(MISSING)).isTrue();
            assertThat(x.accept(WITH_TRAILING_SECTION)).isTrue();
            assertThat(x.accept(BAD_EXTENSION)).isFalse();
            assertThat(x.accept(NOT_XLSX)).isFalse();
            assertThat(x.accept(EMPTY)).isFalse();
        }
    }

    @Test
    public void testAcceptPath() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            assertThat(x.accept(ORIGINAL.toPath())).isTrue();
            assertThat(x.accept(MISSING.toPath())).isTrue();
            assertThat(x.accept(WITH_TRAILING_SECTION.toPath())).isTrue();
            assertThat(x.accept(BAD_EXTENSION.toPath())).isFalse();
            assertThat(x.accept(NOT_XLSX.toPath())).isFalse();
            assertThat(x.accept(EMPTY.toPath())).isFalse();
        }
    }
}
