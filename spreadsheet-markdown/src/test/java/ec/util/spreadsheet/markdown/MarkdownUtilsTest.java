package ec.util.spreadsheet.markdown;

import ec.util.spreadsheet.helpers.ArraySheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
}
