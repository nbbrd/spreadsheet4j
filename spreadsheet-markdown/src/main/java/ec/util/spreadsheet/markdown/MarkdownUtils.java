package ec.util.spreadsheet.markdown;

import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static java.util.Collections.singletonList;

final class MarkdownUtils {

    private static final Parser PARSER = Parser
            .builder(
                    new MutableDataSet()
                            .set(Parser.EXTENSIONS, singletonList(TablesExtension.create()))
            ).build();

    public static ArrayBook parseBook(Reader reader) throws IOException {
        Node doc = PARSER.parseReader(reader);
        ArrayBook.Builder book = ArrayBook.builder();
        int tableIndex = 0;
        for (Node node = doc.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof TableBlock) {
                book.sheet(parseSheet((TableBlock) node, "Table" + (++tableIndex)));
            }
        }
        return book.build();
    }

    private static ArraySheet parseSheet(TableBlock tableBlock, String name) {
        ArraySheet.Builder sheet = ArraySheet.builder().name(name);
        int row = 0;
        for (Node section = tableBlock.getFirstChild(); section != null; section = section.getNext()) {
            if (section instanceof TableHead || section instanceof TableBody) {
                for (Node child = section.getFirstChild(); child != null; child = child.getNext()) {
                    if (child instanceof TableRow) {
                        parseRow((TableRow) child, sheet, row++);
                    }
                }
            }
        }
        return sheet.build();
    }

    private static void parseRow(TableRow tableRow, ArraySheet.Builder sheet, int row) {
        int col = 0;
        for (Node cell = tableRow.getFirstChild(); cell != null; cell = cell.getNext()) {
            if (cell instanceof TableCell) {
                String text = ((TableCell) cell).getText().toString().trim();
                if (!text.isEmpty()) {
                    sheet.value(row, col, text);
                }
                col++;
            }
        }
    }

    static void writeSheet(Sheet sheet, Writer writer) throws IOException {
        int rowCount = sheet.getRowCount();
        int colCount = sheet.getColumnCount();
        if (rowCount == 0 || colCount == 0) return;

        for (int i = 0; i < rowCount; i++) {
            writer.write('|');
            for (int j = 0; j < colCount; j++) {
                Object value = sheet.getCellValue(i, j);
                writer.write(' ');
                writer.write(value == null ? "" : escapeCellContent(value.toString()));
                writer.write(" |");
            }
            writer.write('\n');
            if (i == 0) {
                writer.write('|');
                for (int j = 0; j < colCount; j++) {
                    writer.write(" --- |");
                }
                writer.write('\n');
            }
        }
    }

    private static String escapeCellContent(String value) {
        return value.replace("\r\n", "<br>")
                .replace("\r", "<br>")
                .replace("\n", "<br>");
    }
}
