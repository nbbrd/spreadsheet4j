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
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class ExcelBookFactoryTest {

    private static ExcelBookFactory[] FACTORIES;
    private static File VALID;
    private static File BAD_EXTENSION;
    private static File VALID_WITH_TAIL;
    private static File INVALID_FORMAT;
    private static File EMPTY;
    private static File MISSING;

    @BeforeAll
    public static void initFiles(@TempDir Path temp) {
        FACTORIES = new ExcelBookFactory[]{new ExcelBookFactory(), new ExcelBookFactory()};
        FACTORIES[0].setFast(true);
        FACTORIES[1].setFast(false);
        VALID = Top5x.VALID.file(temp);
        BAD_EXTENSION = Top5x.BAD_EXTENSION.file(temp);
        VALID_WITH_TAIL = Top5x.VALID_WITH_TAIL.file(temp);
        INVALID_FORMAT = Top5x.INVALID_FORMAT.file(temp);
        EMPTY = Top5x.EMPTY.file(temp);
        MISSING = Top5x.MISSING.file(temp);
    }

    @Test
    public void testCompliance() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            BookFactoryAssert.assertThat(x).isCompliant(VALID, INVALID_FORMAT);
        }
    }

    @Test
    public void testLoadFile() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            try (Book book = x.load(VALID)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = x.load(BAD_EXTENSION)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = x.load(VALID_WITH_TAIL)) {
                Top5x.assertTop5Book(book);
            }
            assertThatIOException().isThrownBy(() -> x.load(INVALID_FORMAT));
            assertThatIOException().isThrownBy(() -> x.load(EMPTY));
            assertThatIOException().isThrownBy(() -> x.load(MISSING));
        }
    }

    @Test
    public void testLoadStream() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            try (Book book = Top5x.VALID.loadStream(x)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = Top5x.BAD_EXTENSION.loadStream(x)) {
                Top5x.assertTop5Book(book);
            }
            try (Book book = Top5x.VALID_WITH_TAIL.loadStream(x)) {
                Top5x.assertTop5Book(book);
            }
            assertThatIOException().isThrownBy(() -> Top5x.INVALID_FORMAT.loadStream(x));
            assertThatIOException().isThrownBy(() -> Top5x.EMPTY.loadStream(x));
        }
    }

    @Test
    public void testAcceptFile() {
        for (ExcelBookFactory x : FACTORIES) {
            assertThat(x.accept(VALID)).isTrue();
            assertThat(x.accept(MISSING)).isTrue();
            assertThat(x.accept(VALID_WITH_TAIL)).isTrue();
            assertThat(x.accept(BAD_EXTENSION)).isFalse();
            assertThat(x.accept(INVALID_FORMAT)).isFalse();
            assertThat(x.accept(EMPTY)).isFalse();
        }
    }

    @Test
    public void testAcceptPath() throws IOException {
        for (ExcelBookFactory x : FACTORIES) {
            assertThat(x.accept(VALID.toPath())).isTrue();
            assertThat(x.accept(MISSING.toPath())).isTrue();
            assertThat(x.accept(VALID_WITH_TAIL.toPath())).isTrue();
            assertThat(x.accept(BAD_EXTENSION.toPath())).isFalse();
            assertThat(x.accept(INVALID_FORMAT.toPath())).isFalse();
            assertThat(x.accept(EMPTY.toPath())).isFalse();
        }
    }
}
