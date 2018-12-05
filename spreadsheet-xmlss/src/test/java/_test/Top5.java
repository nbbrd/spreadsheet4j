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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.GregorianCalendar;
import static org.assertj.core.api.Assertions.atIndex;
import org.assertj.core.util.URLs;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public enum Top5 {

    ORIGINAL(URLs.contentOf(Top5.class.getResource("/Top5Browsers.xml"), StandardCharsets.UTF_8)),
    WITH_TRAILING_SECTION(URLs.contentOf(Top5.class.getResource("/Top5Browsers.xml"), StandardCharsets.UTF_8) + "\0"),
    WITHOUT_HEADER(URLs.contentOf(Top5.class.getResource("/Top5Browsers.xml"), StandardCharsets.UTF_8).replace("<?mso-application progid=\"Excel.Sheet\"?>", ""));

    private final String content;

    private Top5(String content) {
        this.content = content;
    }

    public File newFile(TemporaryFolder tmp) {
        return newFile(tmp, "top5_file_" + name() + ".xml", content);
    }

    public Path newPath(TemporaryFolder tmp) {
        return newFile(tmp, "top5_path_" + name() + ".xml", content).toPath();
    }

    public InputStream newStream() {
        return newInputStream(content);
    }

    private static File newFile(TemporaryFolder temp, String fileName, String content) {
        try {
            File result = temp.newFile(fileName);
            Files.write(result.toPath(), Collections.singleton(content));
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static InputStream newInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }

    public static void assertTop5Book(Book book) throws IOException {
        BookAssert
                .assertThat(book)
                .hasSheetCount(3)
                .satisfies(Top5::assertTop5Sheet1, atIndex(0));
    }

    public static void assertTop5Sheet1(Sheet sheet) {
        SheetAssert
                .assertThat(sheet)
                .hasName("Top 5 Browsers - Monthly")
                .hasRowCount(42)
                .hasColumnCount(7)
                .hasCellValue(0, 0, null)
                .hasCellValue(0, 1, "IE")
                .hasCellValue(1, 0, new GregorianCalendar(2008, 6, 1).getTime())
                .hasCellValue(41, 6, 0.93);
    }
}
