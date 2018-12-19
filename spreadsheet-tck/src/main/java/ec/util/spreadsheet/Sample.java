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
package ec.util.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Sample {

    @Nonnull
    public static Sample of(@Nonnull String fileName, @Nonnull String content) {
        return new Sample(fileName, () -> content.getBytes(StandardCharsets.UTF_8));
    }

    @Nonnull
    public static Sample of(@Nonnull String fileName, @Nonnull byte[] content) {
        return new Sample(fileName, () -> content);
    }

    @Nonnull
    public static Sample of(@Nonnull String fileName) {
        return new Sample(fileName, () -> null);
    }

    private final String fileName;
    private final Supplier<byte[]> data;

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    @Nonnull
    public File file(@Nonnull TemporaryFolder temp) {
        return newFile(temp);
    }

    @Nonnull
    public Path path(@Nonnull TemporaryFolder temp) {
        return newFile(temp).toPath();
    }

    @Nonnull
    public InputStream stream() {
        return new ByteArrayInputStream(data.get());
    }

    @Nonnull
    public Book loadStream(@Nonnull Book.Factory x) throws IOException {
        try (InputStream stream = stream()) {
            return x.load(stream);
        }
    }

    private File newFile(TemporaryFolder temp) {
        try {
            File result = temp.newFile(fileName);
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

    @Nonnull
    public static byte[] toByteArray(@Nonnull InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    @Nonnull
    public static byte[] concat(@Nonnull byte[] l, @Nonnull byte... r) {
        byte[] result = Arrays.copyOf(l, l.length + r.length);
        System.arraycopy(r, 0, result, l.length, r.length);
        return result;
    }

    @Nonnull
    public static byte[] bytesOf(@Nonnull URL url) {
        try (InputStream stream = url.openStream()) {
            return Sample.toByteArray(stream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
