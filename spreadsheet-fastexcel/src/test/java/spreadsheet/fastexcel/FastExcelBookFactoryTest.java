package spreadsheet.fastexcel;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.tck.Assertions;
import ec.util.spreadsheet.tck.BookAssert;
import ec.util.spreadsheet.tck.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spreadsheet.xlsx.XlsxBookFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FastExcelBookFactoryTest {

    private static final byte[] CONTENT = Sample.bytesOf(FastExcelBookFactoryTest.class.getResource("/Top5Browsers.xlsx"));

    public final Sample VALID = Sample.of("valid.xlsx", CONTENT);
    public final Sample INVALID_FORMAT = Sample.of("invalidFormat.xlsx", "...");

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        Assertions.assertThat(new FastExcelBookFactory())
                .isCompliant(VALID.file(temp), INVALID_FORMAT.file(temp));
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        File inputFile = VALID.file(temp);
        File outputFile = Files.createTempFile("output", ".xlsx").toFile();
        System.out.println(outputFile);

        XlsxBookFactory reader = new XlsxBookFactory();
        FastExcelBookFactory writer = new FastExcelBookFactory();

        try (Book original = reader.load(inputFile)) {
            writer.store(outputFile, original);

            try (Book modified = reader.load(outputFile)) {
                BookAssert.assertThat(modified)
                        .hasSameContentAs(modified, true);
            }
        }
    }
}
