package ec.util.spreadsheet.markdown;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static _test.MarkdownSamples.MD;
import static ec.util.spreadsheet.tck.Assertions.assertThat;
import static ec.util.spreadsheet.tck.Conditions.ableToLoadContent;
import static ec.util.spreadsheet.tck.Conditions.ableToStoreContent;

public class MarkdownBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        assertThat(new MarkdownBookFactory())
                .isCompliant(MD, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        Assertions.assertThat(new MarkdownBookFactory())
                .is(ableToLoadContent())
                .is(ableToStoreContent());

        Book.Factory x = new MarkdownBookFactory();
        BookFactoryAssert.assertReadWrite(x, x,
                MD.getValid().file(temp),
                Files.createTempFile(temp, "output", ".md").toFile()
        );
    }

    @Test
    public void testLoad(@TempDir Path temp) throws IOException {
        Book.Factory x = new MarkdownBookFactory();
        Book book = x.load(MD.getValid().file(temp));
        assertThat(book).hasSheetCount(2);
        assertThat(book.getSheet(0))
                .hasRowCount(3)
                .hasColumnCount(2)
                .hasCellValue(0, 0, "H1")
                .hasCellValue(0, 1, "H2")
                .hasCellValue(1, 0, "a")
                .hasCellValue(1, 1, "b")
                .hasCellValue(2, 0, "c")
                .hasCellValue(2, 1, "d");
    }
}

