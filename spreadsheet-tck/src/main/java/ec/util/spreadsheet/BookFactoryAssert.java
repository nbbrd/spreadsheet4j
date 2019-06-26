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
package ec.util.spreadsheet;

import static ec.util.spreadsheet.Assertions.msg;
import ec.util.spreadsheet.helpers.ArrayBook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

/**
 *
 * @author Philippe Charles
 */
public class BookFactoryAssert extends AbstractAssert<BookFactoryAssert, Book.Factory> {

    public BookFactoryAssert(Book.Factory actual) {
        super(actual, BookFactoryAssert.class);
    }

    public static BookFactoryAssert assertThat(Book.Factory actual) {
        return new BookFactoryAssert(actual);
    }

    public BookFactoryAssert isCompliant(File valid) throws IOException {
        return isCompliant(valid, null);
    }

    public BookFactoryAssert isCompliant(File valid, File invalid) throws IOException {
        isNotNull();
        SoftAssertions s = new SoftAssertions();
        assertCompliance(s, actual, valid, Optional.ofNullable(invalid));
        s.assertAll();
        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final File INVALID_PATH = new File("mapi16:\\{9054}\\x@y($ddab4c7c)\\0\\Inbox\\at=abc:hello.xml");

    private static void assertCompliance(SoftAssertions s, Book.Factory factory, File valid, Optional<File> invalid) throws IOException {
        s.assertThat(factory.getName()).isNotNull();
        s.assertThat(factory.accept(valid)).isTrue();
        s.assertThat(factory.accept(INVALID_PATH)).isFalse();
        if (invalid.isPresent()) {
            // FIXME: must add better invalid definition
//            s.assertThat(factory.accept(invalid.get())).isTrue();
        }

        s.assertThatThrownBy(() -> factory.isSupportedDataType(NULL_CLASS))
                .as(msg(factory, "isSupportedDataType(nullClass)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);

        if (factory.canLoad()) {
            assertLoadNull(s, factory);
            assertLoadValid(s, factory, valid);
            if (invalid.isPresent()) {
                assertLoadInvalid(s, factory, invalid.get());
            }
            assertLoadEmpty(s, factory);
            assertLoadMissing(s, factory);
            assertLoadDir(s, factory);
            s.assertThatThrownBy(() -> factory.load(INVALID_PATH))
                    .as(msg(factory, "load(invalidPath)", IOException.class))
                    .isInstanceOf(IOException.class);
        } else {
            assertLoadUnsupported(s, factory, valid);
        }

        if (factory.canStore()) {
            s.assertThatThrownBy(() -> factory.store(INVALID_PATH, ArrayBook.builder().build()))
                    .as(msg(factory, "store(invalidPath, book)", IOException.class))
                    .isInstanceOf(IOException.class);
        }

        if (factory.canLoad() && factory.canStore()) {
            assertLoadStore(s, factory, valid.toURI().toURL());
        }
    }

    private static void assertLoadNull(SoftAssertions s, Book.Factory factory) {
        s.assertThatThrownBy(() -> factory.load(NULL_FILE))
                .as(msg(factory, "load(nullFile)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_INPUT_STREAM))
                .as(msg(factory, "load(nullInputStream)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_PATH))
                .as(msg(factory, "load(nullPath)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_URL))
                .as(msg(factory, "load(nullURL)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
    }

    private static void assertLoadValid(SoftAssertions s, Book.Factory factory, File valid) throws IOException {
        ArrayBook b;
        try (Book x = factory.load(valid)) {
            BookAssert.assertCompliance(s, x);
            b = ArrayBook.copyOf(x);
        }
        try (InputStream stream = Files.newInputStream(valid.toPath())) {
            try (Book x = factory.load(stream)) {
                BookAssert.assertContentEquals(s, b, x, true);
            }
        }
        try (Book x = factory.load(valid.toPath())) {
            BookAssert.assertContentEquals(s, b, x, true);
        }
        try (Book x = factory.load(valid.toURI().toURL())) {
            BookAssert.assertContentEquals(s, b, x, true);
        }
    }

    private static void assertLoadInvalid(SoftAssertions s, Book.Factory f, File invalidFile) throws IOException {
        Class<? extends IOException> invalidException = IOException.class;

        s.assertThatThrownBy(() -> f.load(invalidFile))
                .as(msg(f, "load(invalidFile)", invalidException))
                .isNotInstanceOf(EOFException.class)
                .isInstanceOf(invalidException)
                .hasMessageContaining(invalidFile.getName());

        try (InputStream stream = Files.newInputStream(invalidFile.toPath())) {
            s.assertThatThrownBy(() -> f.load(stream))
                    .as(msg(f, "load(invalidStream)", invalidException))
                    .isNotInstanceOf(EOFException.class)
                    .isInstanceOf(invalidException);
        }

        s.assertThatThrownBy(() -> f.load(invalidFile.toPath()))
                .as(msg(f, "load(invalidPath)", invalidException))
                .isNotInstanceOf(EOFException.class)
                .isInstanceOf(invalidException)
                .hasMessageContaining(invalidFile.getName());

        s.assertThatThrownBy(() -> f.load(invalidFile.toURI().toURL()))
                .as(msg(f, "load(invalidURL)", invalidException))
                .isNotInstanceOf(EOFException.class)
                .isInstanceOf(invalidException);
    }

    private static void assertLoadEmpty(SoftAssertions s, Book.Factory f) throws IOException {
        File empty = File.createTempFile("empty", "file");

        s.assertThatThrownBy(() -> f.load(empty))
                .as(msg(f, "load(emptyFile)", EOFException.class))
                .isInstanceOf(EOFException.class)
                .hasMessage(empty.getPath());

        try (InputStream stream = Files.newInputStream(empty.toPath())) {
            s.assertThatThrownBy(() -> f.load(stream))
                    .as(msg(f, "load(emptyStream)", EOFException.class))
                    .isInstanceOf(EOFException.class);
        }

        s.assertThatThrownBy(() -> f.load(empty.toPath()))
                .as(msg(f, "load(emptyPath)", EOFException.class))
                .isInstanceOf(EOFException.class)
                .hasMessage(empty.getPath());

        s.assertThatThrownBy(() -> f.load(empty.toURI().toURL()))
                .as(msg(f, "load(emptyURL)", EOFException.class))
                .isInstanceOf(EOFException.class);

        empty.delete();
    }

    private static void assertLoadMissing(SoftAssertions s, Book.Factory f) throws IOException {
        File missing = File.createTempFile("missing", "file");
        missing.delete();

        s.assertThatThrownBy(() -> f.load(missing))
                .as(msg(f, "load(missingFile)", NoSuchFileException.class))
                .isInstanceOf(NoSuchFileException.class)
                .hasMessage(missing.getPath());

        s.assertThatThrownBy(() -> f.load(missing.toPath()))
                .as(msg(f, "load(missingPath)", NoSuchFileException.class))
                .isInstanceOf(NoSuchFileException.class)
                .hasMessage(missing.getPath());

        s.assertThatThrownBy(() -> f.load(missing.toURI().toURL()))
                .as(msg(f, "load(missingURL)", NoSuchFileException.class))
                .isInstanceOf(NoSuchFileException.class);
    }

    private static void assertLoadDir(SoftAssertions s, Book.Factory f) throws IOException {
        File folder = org.assertj.core.util.Files.newTemporaryFolder();

        s.assertThatThrownBy(() -> f.load(folder))
                .as(msg(f, "load(folderAsFile)", AccessDeniedException.class))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(folder.getPath());

        s.assertThatThrownBy(() -> f.load(folder.toPath()))
                .as(msg(f, "load(folderAsPath)", AccessDeniedException.class))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(folder.getPath());

//        s.assertThatThrownBy(() -> f.load(folder.toURI().toURL()))
//                .as(msg(f, "load(folderAsURL)", AccessDeniedException.class))
//                .isInstanceOf(AccessDeniedException.class);
        folder.delete();
    }

    private static void assertLoadUnsupported(SoftAssertions s, Book.Factory f, File valid) throws IOException {
        s.assertThatThrownBy(() -> f.load(valid))
                .isInstanceOf(UnsupportedOperationException.class);
        try (InputStream stream = Files.newInputStream(valid.toPath())) {
            s.assertThatThrownBy(() -> f.load(stream))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
        s.assertThatThrownBy(() -> f.load(valid.toPath()))
                .isInstanceOf(UnsupportedOperationException.class);
        s.assertThatThrownBy(() -> f.load(valid.toURI().toURL()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static void assertLoadStore(SoftAssertions s, Book.Factory factory, URL sample) throws IOException {
        try (Book original = factory.load(sample)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            factory.store(outputStream, original);
            byte[] data = outputStream.toByteArray();

            try (Book result = factory.load(new ByteArrayInputStream(data))) {
                BookAssert.assertContentEquals(s, original, result, false);
            }
        }
    }

    private static final Class<?> NULL_CLASS = null;
    private static final File NULL_FILE = null;
    private static final InputStream NULL_INPUT_STREAM = null;
    private static final Path NULL_PATH = null;
    private static final URL NULL_URL = null;
    //</editor-fold>
}
