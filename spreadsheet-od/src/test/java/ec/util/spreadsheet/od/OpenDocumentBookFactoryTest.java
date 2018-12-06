/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.spreadsheet.od;

import _test.Top5;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryAssert;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
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
public class OpenDocumentBookFactoryTest {

    @ClassRule
    public static TemporaryFolder TEMP = new TemporaryFolder();
    private static File ORIGINAL;
    private static File BAD_EXTENSION;
    private static File WITH_TRAILING_SECTION;
    private static File NOT_ODS;
    private static File EMPTY;
    private static File MISSING;

    @BeforeClass
    public static void initFiles() {
        ORIGINAL = Top5.ORIGINAL.file(TEMP);
        BAD_EXTENSION = Top5.BAD_EXTENSION.file(TEMP);
        WITH_TRAILING_SECTION = Top5.WITH_TRAILING_SECTION.file(TEMP);
        NOT_ODS = Top5.NOT_ODS.file(TEMP);
        EMPTY = Top5.EMPTY.file(TEMP);
        MISSING = Top5.MISSING.file(TEMP);
    }

    @Test
    public void testCompliance() throws IOException {
        // FIXME: find a way to detect invalid files
//        File invalid = temp.newFile("invalid.ods");
//        Files.write(invalid.toPath(), Arrays.asList("..."));
        BookFactoryAssert.assertThat(new OpenDocumentBookFactory()).isCompliant(ORIGINAL);
    }

    @Test
    public void testLoadFile() throws IOException {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        try (Book book = x.load(ORIGINAL)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = x.load(BAD_EXTENSION)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = x.load(WITH_TRAILING_SECTION)) {
            Top5.assertTop5Book(book);
        }
//        assertThatIOException().isThrownBy(() -> x.load(NOT_ODS));
        assertThatIOException().isThrownBy(() -> x.load(EMPTY));
        assertThatIOException().isThrownBy(() -> x.load(MISSING));
    }

    private static Book doLoad(OpenDocumentBookFactory x, Supplier<InputStream> byteSource) throws IOException {
        try (InputStream stream = byteSource.get()) {
            return x.load(stream);
        }
    }

    @Test
    public void testLoadStream() throws IOException {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        try (Book book = doLoad(x, Top5.ORIGINAL::stream)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = doLoad(x, Top5.BAD_EXTENSION::stream)) {
            Top5.assertTop5Book(book);
        }
        try (Book book = doLoad(x, Top5.WITH_TRAILING_SECTION::stream)) {
            Top5.assertTop5Book(book);
        }
//        assertThatIOException().isThrownBy(() -> doLoad(x, Top5.NOT_ODS::stream));
        assertThatIOException().isThrownBy(() -> doLoad(x, Top5.EMPTY::stream));
    }

    @Test
    public void testAcceptFile() {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        assertThat(x.accept(ORIGINAL)).isTrue();
        assertThat(x.accept(MISSING)).isTrue();
        assertThat(x.accept(WITH_TRAILING_SECTION)).isTrue();
        assertThat(x.accept(BAD_EXTENSION)).isFalse();
        assertThat(x.accept(NOT_ODS)).isFalse();
        assertThat(x.accept(EMPTY)).isFalse();
    }

    @Test
    public void testAcceptPath() throws IOException {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        assertThat(x.accept(ORIGINAL.toPath())).isTrue();
        assertThat(x.accept(MISSING.toPath())).isTrue();
        assertThat(x.accept(WITH_TRAILING_SECTION.toPath())).isTrue();
        assertThat(x.accept(BAD_EXTENSION.toPath())).isFalse();
        assertThat(x.accept(NOT_ODS.toPath())).isFalse();
        assertThat(x.accept(EMPTY.toPath())).isFalse();
    }
}
