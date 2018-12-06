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

import _test.Top5;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryAssert;
import ioutil.IO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class ExcelClassicBookFactoryTest {

    @ClassRule
    public static TemporaryFolder TEMP = new TemporaryFolder();
    private static File ORIGINAL;
    private static File BAD_EXTENSION;
    private static File WITH_TRAILING_SECTION;
    private static File NOT_XLS;
    private static File EMPTY;
    private static File MISSING;

    @BeforeClass
    public static void initFiles() {
        ORIGINAL = Top5.ORIGINAL.file(TEMP);
        BAD_EXTENSION = Top5.BAD_EXTENSION.file(TEMP);
        WITH_TRAILING_SECTION = Top5.WITH_TRAILING_SECTION.file(TEMP);
        NOT_XLS = Top5.NOT_XLS.file(TEMP);
        EMPTY = Top5.EMPTY.file(TEMP);
        MISSING = Top5.MISSING.file(TEMP);
    }

    @Test
    public void testCompliance() throws IOException {
        BookFactoryAssert.assertThat(new ExcelClassicBookFactory()).isCompliant(ORIGINAL, NOT_XLS);
    }

    @Test
    public void testLoadFile() throws IOException {
        ExcelClassicBookFactory x = new ExcelClassicBookFactory();

        try (Book book = x.load(ORIGINAL)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = x.load(BAD_EXTENSION)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = x.load(WITH_TRAILING_SECTION)) {
            Top5.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> x.load(NOT_XLS));
        assertThatIOException().isThrownBy(() -> x.load(EMPTY));
        assertThatIOException().isThrownBy(() -> x.load(MISSING));
    }

    private static Book doLoad(ExcelClassicBookFactory x, IO.Supplier<InputStream> byteSource) throws IOException {
        try (InputStream stream = byteSource.getWithIO()) {
            return x.load(stream);
        }
    }

    @Test
    public void testLoadStream() throws IOException {
        ExcelClassicBookFactory x = new ExcelClassicBookFactory();

        try (Book book = doLoad(x, Top5.ORIGINAL::stream)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = doLoad(x, Top5.BAD_EXTENSION::stream)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = doLoad(x, Top5.WITH_TRAILING_SECTION::stream)) {
            Top5.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> doLoad(x, Top5.NOT_XLS::stream));
        assertThatIOException().isThrownBy(() -> doLoad(x, Top5.EMPTY::stream));
    }

    @Test
    public void testAcceptFile() {
        ExcelClassicBookFactory x = new ExcelClassicBookFactory();

        assertThat(x.accept(ORIGINAL)).isTrue();
        assertThat(x.accept(MISSING)).isTrue();
        assertThat(x.accept(WITH_TRAILING_SECTION)).isTrue();
        assertThat(x.accept(BAD_EXTENSION)).isFalse();
        assertThat(x.accept(NOT_XLS)).isFalse();
        assertThat(x.accept(EMPTY)).isFalse();
    }

    @Test
    public void testAcceptPath() throws IOException {
        ExcelClassicBookFactory x = new ExcelClassicBookFactory();

        assertThat(x.accept(ORIGINAL.toPath())).isTrue();
        assertThat(x.accept(MISSING.toPath())).isTrue();
        assertThat(x.accept(WITH_TRAILING_SECTION.toPath())).isTrue();
        assertThat(x.accept(BAD_EXTENSION.toPath())).isFalse();
        assertThat(x.accept(NOT_XLS.toPath())).isFalse();
        assertThat(x.accept(EMPTY.toPath())).isFalse();
    }
}
