package ec.util.spreadsheet.markdown;

import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownUtilsTest {

    @Test
    public void testWriteSheetEscapesNewlinesInCells() throws IOException {
        ArraySheet sheet = ArraySheet.builder()
                .name("test")
                .value(0, 0, "Header")
                .value(0, 1, "Value")
                .value(1, 0, "line1\nline2")
                .value(1, 1, "line1\r\nline2")
                .build();

        StringWriter writer = new StringWriter();
        MarkdownUtils.writeSheet(sheet, writer);

        assertThat(writer.toString().split("\n", -1))
                .containsExactly(
                        "| Header | Value |",
                        "| --- | --- |",
                        "| line1<br>line2 | line1<br>line2 |",
                        ""
                );
    }

    @Test
    public void testWriteSheetEscapesCrNewlineVariants() throws IOException {
        ArraySheet sheet = ArraySheet.builder()
                .name("test")
                .value(0, 0, "A")
                .value(1, 0, "x\ry")   // bare CR
                .build();

        StringWriter writer = new StringWriter();
        MarkdownUtils.writeSheet(sheet, writer);

        assertThat(writer.toString().split("\n", -1))
                .containsExactly(
                        "| A |",
                        "| --- |",
                        "| x<br>y |",
                        ""
                );
    }

    @Test
    public void testWriteSheetWithEmptyCells() throws IOException {
        ArraySheet sheet = ArraySheet.builder()
                .name("test")
                .value(0, 0, "A")
                .value(0, 1, "B")
                .value(1, 0, "x")
                // (1,1) left null
                .build();

        StringWriter writer = new StringWriter();
        MarkdownUtils.writeSheet(sheet, writer);

        assertThat(writer.toString().split("\n", -1))
                .containsExactly(
                        "| A | B |",
                        "| --- | --- |",
                        "| x |  |",
                        ""
                );
    }

    @Test
    public void testWriteEmptySheetProducesNoOutput() throws IOException {
        ArraySheet sheet = ArraySheet.builder().name("empty").build();

        StringWriter writer = new StringWriter();
        MarkdownUtils.writeSheet(sheet, writer);

        assertThat(writer.toString()).isEmpty();
    }

    @Test
    public void testParseSingleTable() throws IOException {
        ArrayBook book = parse(
                "| H1 | H2 |\n"
                        + "|----|----|\n"
                        + "| a  | b  |\n"
                        + "| c  | d  |\n");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getName()).isEqualTo("Table1");
        assertThat(book.getSheet(0).getRowCount()).isEqualTo(3);
        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(2);
        assertThat(book.getSheet(0).getCellValue(0, 0)).isEqualTo("H1");
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("b");
        assertThat(book.getSheet(0).getCellValue(2, 0)).isEqualTo("c");
    }

    @Test
    public void testParseMultipleTablesWithInterleavedText() throws IOException {
        ArrayBook book = parse(
                "# Title\n"
                        + "\n"
                        + "| A | B |\n"
                        + "|---|---|\n"
                        + "| 1 | 2 |\n"
                        + "\n"
                        + "Some paragraph.\n"
                        + "\n"
                        + "| X | Y | Z |\n"
                        + "|---|---|---|\n"
                        + "| 9 | 8 | 7 |\n");

        assertThat(book.getSheetCount2()).isEqualTo(2);
        assertThat(book.getSheet(0).getName()).isEqualTo("Table1");
        assertThat(book.getSheet(1).getName()).isEqualTo("Table2");
        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(2);
        assertThat(book.getSheet(1).getColumnCount()).isEqualTo(3);
        assertThat(book.getSheet(1).getCellValue(1, 2)).isEqualTo("7");
    }

    @Test
    public void testParseAcceptsAlignmentMarkers() throws IOException {
        ArrayBook book = parse(
                "| L | C | R |\n"
                        + "|:---|:---:|---:|\n"
                        + "| 1 | 2 | 3 |\n");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(3);
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("2");
    }

    @Test
    public void testParseSingleColumnTable() throws IOException {
        ArrayBook book = parse(
                "| only |\n"
                        + "|------|\n"
                        + "| foo  |\n"
                        + "| bar  |\n");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getRowCount()).isEqualTo(3);
        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(1);
        assertThat(book.getSheet(0).getCellValue(2, 0)).isEqualTo("bar");
    }

    @Test
    public void testParseRowsWithoutBoundingPipes() throws IOException {
        ArrayBook book = parse(
                "A | B\n"
                        + "--|--\n"
                        + "1 | 2\n");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(2);
        assertThat(book.getSheet(0).getCellValue(0, 0)).isEqualTo("A");
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("2");
    }

    @Test
    public void testParseHandlesEscapedPipes() throws IOException {
        ArrayBook book = parse(
                "| A | B |\n"
                        + "|---|---|\n"
                        + "| x\\|y | z |\n");

        assertThat(book.getSheet(0).getColumnCount()).isEqualTo(2);
        assertThat(book.getSheet(0).getCellValue(1, 0)).isEqualTo("x|y");
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("z");
    }

    @Test
    public void testParseIgnoresPipeLinesWithoutSeparator() throws IOException {
        // Looks table-ish but has no separator row — must not be treated as a table
        ArrayBook book = parse(
                "| not | a | table |\n"
                        + "| still | not | a | table |\n");

        assertThat(book.getSheetCount2()).isZero();
    }

    @Test
    public void testParseAcceptsCrLfLineEndings() throws IOException {
        ArrayBook book = parse(
                "| A | B |\r\n"
                        + "|---|---|\r\n"
                        + "| 1 | 2 |\r\n");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getCellValue(1, 0)).isEqualTo("1");
    }

    @Test
    public void testParseAcceptsMissingTrailingNewline() throws IOException {
        ArrayBook book = parse(
                "| A | B |\n"
                        + "|---|---|\n"
                        + "| 1 | 2 |");

        assertThat(book.getSheetCount2()).isEqualTo(1);
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("2");
    }

    @Test
    public void testParseEmptyDocument() throws IOException {
        ArrayBook book = parse("");

        assertThat(book.getSheetCount2()).isZero();
    }

    @Test
    public void testParseEmptyCellsAreNotStored() throws IOException {
        // Empty/whitespace-only cells should not be materialized as values
        ArrayBook book = parse(
                "| A | B |\n"
                        + "|---|---|\n"
                        + "|   | 2 |\n");

        assertThat(book.getSheet(0).getCellValue(1, 0)).isNull();
        assertThat(book.getSheet(0).getCellValue(1, 1)).isEqualTo("2");
    }

    @Test
    public void testWriteThenParseRoundTrip() throws IOException {
        ArraySheet original = ArraySheet.builder()
                .name("src")
                .row(0, 0, "H1", "H2", "H3")
                .row(1, 0, "a", "b", "c")
                .row(2, 0, "d", "e", "f")
                .build();

        StringWriter writer = new StringWriter();
        MarkdownUtils.writeSheet(original, writer);

        ArrayBook roundTripped = parse(writer.toString());

        assertThat(roundTripped.getSheetCount2()).isEqualTo(1);
        assertThat(roundTripped.getSheet(0).getRowCount()).isEqualTo(original.getRowCount());
        assertThat(roundTripped.getSheet(0).getColumnCount()).isEqualTo(original.getColumnCount());
        for (int r = 0; r < original.getRowCount(); r++) {
            for (int c = 0; c < original.getColumnCount(); c++) {
                assertThat(roundTripped.getSheet(0).getCellValue(r, c))
                        .as("cell [%d,%d]", r, c)
                        .isEqualTo(original.getCellValue(r, c));
            }
        }
    }

    private static ArrayBook parse(String text) throws IOException {
        return MarkdownUtils.parseBook(new StringReader(text));
    }
}
