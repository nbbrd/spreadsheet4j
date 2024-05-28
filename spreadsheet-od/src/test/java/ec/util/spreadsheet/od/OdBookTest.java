package ec.util.spreadsheet.od;

import com.github.miachm.sods.SpreadSheet;
import ec.util.spreadsheet.tck.BookAssert;
import ec.util.spreadsheet.tck.SheetAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class OdBookTest {

    @SuppressWarnings({"DataFlowIssue", "resource"})
    @Test
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> new OdBook(null));
    }

    @Test
    public void testContent() throws IOException {
        try (OdBook book = new OdBook(loadResource("/Top5Browsers.ods"))) {
            BookAssert.assertThat(book).hasSheetCount(3);
            SheetAssert.assertThat(book.getSheet(0))
                    .hasName("Top 5 Browsers - Monthly")
                    .hasRowCount(42)
                    .hasColumnCount(7)
                    .hasCellValue(0, 0, null)
                    .hasCellValue(0, 1, "IE")
                    .hasCellValue(1, 0, OdSheet.toDate(LocalDateTime.of(2008, 7, 1, 0, 0), ZoneId.systemDefault()))
                    .hasCellValue(1, 1, 68.57);
        }

        try (OdBook book = new OdBook(loadResource("/world_libre_office.ods"))) {
            BookAssert.assertThat(book).hasSheetCount(4);
            SheetAssert.assertThat(book.getSheet(0))
                    .hasName("Europe")
                    .hasRowCount(383) // FIXME: should be 382?
                    .hasColumnCount(4)
                    .hasCellValue(0, 0, "date")
                    .hasCellValue(0, 1, "France")
                    .hasCellValue(1, 0, OdSheet.toDate(LocalDate.of(1990, 1, 1), ZoneId.systemDefault()))
                    .hasCellValue(1, 1, 395.8926090299);
        }
    }

    private static SpreadSheet loadResource(String name) throws IOException {
        try (InputStream stream = OdBookTest.class.getResourceAsStream(name)) {
            return new SpreadSheet(requireNonNull(stream));
        }
    }
}
