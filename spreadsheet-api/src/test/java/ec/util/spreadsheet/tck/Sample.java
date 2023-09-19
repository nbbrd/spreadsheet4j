/*
 * Copyright 2018 National Bank of Belgium
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
package ec.util.spreadsheet.tck;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import lombok.AccessLevel;
import lombok.NonNull;
import org.assertj.core.util.URLs;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Sample {

    @NonNull
    public static Sample of(@NonNull String fileName, @NonNull String content) {
        return new Sample(fileName, () -> content.getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    public static Sample of(@NonNull String fileName, @NonNull byte[] content) {
        return new Sample(fileName, () -> content);
    }

    @NonNull
    public static Sample of(@NonNull String fileName) {
        return new Sample(fileName, () -> null);
    }

    private final String fileName;
    private final Supplier<byte[]> data;

    @NonNull
    public String getFileName() {
        return fileName;
    }

    @NonNull
    public File file(@NonNull Path temp) {
        return newFile(temp);
    }

    @NonNull
    public Path path(@NonNull Path temp) {
        return newFile(temp).toPath();
    }

    @NonNull
    public InputStream stream() {
        return new ByteArrayInputStream(data.get());
    }

    @NonNull
    public ArrayBook loadStream(Book.@NonNull Factory x) throws IOException {
        try (InputStream stream = stream()) {
            try (Book book = x.load(stream)) {
                return ArrayBook.copyOf(book);
            }
        }
    }

    private File newFile(Path temp) {
        try {
            File result = Files.createFile(temp.resolve(fileName)).toFile();
            byte[] content = data.get();
            if (content != null) {
                Files.write(result.toPath(), content);
            } else {
                result.delete();
            }
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static byte @NonNull [] toByteArray(@NonNull InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public static byte @NonNull [] concat(@NonNull byte[] l, @NonNull byte... r) {
        byte[] result = Arrays.copyOf(l, l.length + r.length);
        System.arraycopy(r, 0, result, l.length, r.length);
        return result;
    }


    public static byte @NonNull [] bytesOf(@NonNull Class<?> anchor, @NonNull String name) {
        try (InputStream stream = anchor.getResourceAsStream(name)) {
            return toByteArray(Objects.requireNonNull(stream));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static @NonNull String contentOf(@NonNull Class<?> anchor, @NonNull String name, @NonNull Charset charset) {
        return URLs.contentOf(Objects.requireNonNull(anchor.getResource("/Top5Browsers.xml")), charset);
    }
}
