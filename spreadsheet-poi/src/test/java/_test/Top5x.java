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
import ec.util.spreadsheet.BookAssert;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.SheetAssert;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.compress.utils.IOUtils;
import static org.assertj.core.api.Assertions.atIndex;
import org.assertj.core.util.DateUtil;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public enum Top5x {

    ORIGINAL(Holder.CONTENT, "original.xlsx"),
    WITH_TRAILING_SECTION(concat(Holder.CONTENT, (byte) '\0'), "withTrailingSection.xlsx"),
    NOT_XLSX("... not xml ...".getBytes(), "notXlsx.xlsx"),
    EMPTY(new byte[0], "empty.xlsx"),
    MISSING(null, "missing.xlsx"),
    BAD_EXTENSION(Holder.CONTENT, "badExtension.xml");

    private final byte[] content;
    private final String fileName;

    private Top5x(byte[] content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public File file(TemporaryFolder tmp) {
        return newFile(tmp, "file_" + fileName, content);
    }

    public Path path(TemporaryFolder tmp) {
        return newFile(tmp, "path_" + fileName, content).toPath();
    }

    public InputStream stream() {
        return newInputStream(content);
    }

    private static File newFile(TemporaryFolder temp, String fileName, byte[] content) {
        try {
            File result = temp.newFile(fileName);
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

    public static InputStream newInputStream(byte[] content) {
        return new ByteArrayInputStream(content);
    }

    public static void assertTop5Book(Book book) throws IOException {
        BookAssert
                .assertThat(book)
                .hasSheetCount(3)
                .satisfies(Top5x::assertTop5Sheet1, atIndex(0));
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

    private static final class Holder {

        static final byte[] CONTENT = load();

        private static byte[] load() {
            try (InputStream stream = Top5x.class.getResourceAsStream("/Top5Browsers.xlsx")) {
                return IOUtils.toByteArray(stream);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private static byte[] concat(byte[] l, byte... r) {
        byte[] result = Arrays.copyOf(l, l.length + r.length);
        System.arraycopy(r, 0, result, l.length, r.length);
        return result;
    }
}
