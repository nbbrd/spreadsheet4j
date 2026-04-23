package ec.util.spreadsheet.markdown;

import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

final class MarkdownUtils {

    private static final Pattern SEPARATOR_CELL = Pattern.compile("\\s*:?-+:?\\s*");

    public static ArrayBook parseBook(Reader reader) throws IOException {
        ArrayBook.Builder result = ArrayBook.builder();
        CellsSplitter splitter = new CellsSplitter();
        List<String> lines = readLines(reader);
        int tableIndex = 0;
        int i = 0;
        while (i < lines.size()) {
            if (isTableRow(lines.get(i))) {
                int start = i;
                while (i < lines.size() && isTableRow(lines.get(i))) {
                    i++;
                }
                // a valid GFM table requires at least 2 rows, with the second being a separator
                if (i - start >= 2 && isSeparatorRow(splitter, lines.get(start + 1))) {
                    result.sheet(parseTable(splitter, lines, start, i, "Table" + (++tableIndex)));
                }
            } else {
                i++;
            }
        }
        return result.build();
    }

    private static List<String> readLines(Reader reader) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            result.add(line);
        }
        return result;
    }

    private static boolean isTableRow(String line) {
        return line.contains("|");
    }

    private static boolean isSeparatorRow(CellsSplitter splitter, String line) {
        Iterator<String> cells = splitter.reset(line);
        if (!cells.hasNext()) return false;
        do {
            if (!SEPARATOR_CELL.matcher(cells.next()).matches()) return false;
        } while (cells.hasNext());
        return true;
    }

    private static ArraySheet parseTable(CellsSplitter splitter, List<String> lines, int start, int end, String name) {
        ArraySheet.Builder result = ArraySheet.builder().name(name);
        int row = 0;
        for (int i = start; i < end; i++) {
            if (i == start + 1) continue; // skip separator row
            Iterator<String> cells = splitter.reset(lines.get(i));
            for (int j = 0; cells.hasNext(); j++) {
                String text = cells.next().trim();
                if (!text.isEmpty()) {
                    result.value(row, j, text);
                }
            }
            row++;
        }
        return result.build();
    }

    private static final class CellsSplitter implements Iterator<String> {

        private final StringBuilder builder = new StringBuilder();

        private String source = "";
        private int pos = 0;
        private String next = null;
        private boolean done = false;

        CellsSplitter reset(String line) {
            String s = line.trim();
            if (s.startsWith("|")) s = s.substring(1);
            // strip trailing pipe unless it is escaped
            if (s.endsWith("|") && !(s.length() >= 2 && s.charAt(s.length() - 2) == '\\')) {
                s = s.substring(0, s.length() - 1);
            }
            this.source = s;
            this.pos = 0;
            this.done = false;
            this.next = null;
            return this;
        }

        @Override
        public boolean hasNext() {
            if (next != null) return true;
            if (done) return false;
            next = readNext();
            return next != null;
        }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            String result = next;
            next = null;
            return result;
        }

        private String readNext() {
            if (done) return null;
            builder.setLength(0);
            while (pos < source.length()) {
                char c = source.charAt(pos);
                if (c == '\\' && pos + 1 < source.length() && source.charAt(pos + 1) == '|') {
                    builder.append('|');
                    pos += 2;
                } else if (c == '|') {
                    pos++;
                    return builder.toString();
                } else {
                    builder.append(c);
                    pos++;
                }
            }
            done = true;
            return builder.toString();
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
