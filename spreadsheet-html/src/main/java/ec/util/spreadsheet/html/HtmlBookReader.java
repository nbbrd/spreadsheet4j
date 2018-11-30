/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.spreadsheet.html;

import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Philippe Charles
 */
final class HtmlBookReader {

    private Charset charset;
    private String baseUri;

    HtmlBookReader() {
        this.charset = null;
        this.baseUri = "";
    }

    public void setCharset(@Nullable Charset charset) {
        this.charset = charset;
    }

    public void setBaseUri(@Nonnull String baseUri) {
        this.baseUri = Objects.requireNonNull(baseUri);
    }

    @Nonnull
    public ArrayBook read(@Nonnull String html) {
        return readHtml(Jsoup.parse(html, baseUri));
    }

    @Nonnull
    public ArrayBook read(@Nonnull File file) throws IOException {
        checkFile(file);
        return readHtml(Jsoup.parse(file, getCharsetNameOrNull(), baseUri));
    }

    @Nonnull
    public ArrayBook read(@Nonnull InputStream stream) throws IOException {
        Objects.requireNonNull(stream);
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return readHtml(Jsoup.parse(stream, getCharsetNameOrNull(), baseUri));
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @Nonnull
    private static File checkFile(@Nonnull File file) throws IOException {
        if (!file.exists()) {
            throw new NoSuchFileException(file.getPath());
        }
        if (!file.canRead() || file.isDirectory()) {
            throw new AccessDeniedException(file.getPath());
        }
        if (file.length() == 0) {
            throw new EOFException(file.getPath());
        }
        return file;
    }

    private String getCharsetNameOrNull() {
        return charset != null ? charset.name() : null;
    }

    private static ArrayBook readHtml(Document doc) {
        ArrayBook.Builder bookBuilder = ArrayBook.builder();
        ArraySheet.Builder sheetBuilder = ArraySheet.builder();
        RowSpans rowSpans = new RowSpans();

        int sheetIndex = 0;
        for (Element table : doc.getElementsByTag("table")) {
            bookBuilder.sheet(readTable(table, sheetIndex, sheetBuilder, rowSpans));
            sheetIndex++;
        }

        return bookBuilder.build();
    }

    private static ArraySheet readTable(Element table, int tableIndex, ArraySheet.Builder builder, RowSpans rowSpans) {
        builder.clear();
        rowSpans.clear();
        builder.name(parseTableName(table, tableIndex));
        int i = 0;
        for (Element row : table.getElementsByTag("tr")) {
            if (!row.parent().tagName().equals("tfoot")) {
                int j = 0;
                for (Element cell : row.children().select("td, th")) {
                    while (rowSpans.hasSpan(j)) {
                        j++;
                    }
                    String cellValue = cell.text();
                    if (!cellValue.isEmpty()) {
                        builder.value(i, j, cellValue);
                    }
                    rowSpans.increase(j, parseSpan(cell.attr("rowspan")));
                    j += parseSpan(cell.attr("colspan"));
                }
                rowSpans.decrease();
                i++;
            }
        }
        return builder.build();
    }

    private static String parseTableName(Element table, int tableIndex) {
        if (table.childNodeSize() > 0) {
            Element first = table.child(0);
            if (first.tagName().equals("caption")) {
                String result = first.text();
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return "Sheet " + tableIndex;
    }

    private static int parseSpan(String value) {
        try {
            int result = Integer.parseInt(value);
            return result > 0 ? result : 0;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private static final class RowSpans {

        private int[] data = new int[0];

        private void checkSize(int columnIndex) {
            if (data.length < columnIndex + 1) {
                int[] old = data;
                data = new int[columnIndex + 1];
                System.arraycopy(old, 0, data, 0, old.length);
            }
        }

        public void increase(int columnIndex, int count) {
            if (count > 0) {
                checkSize(columnIndex);
                data[columnIndex] += count;
            }
        }

        public boolean hasSpan(int columnIndex) {
            return columnIndex < data.length && data[columnIndex] > 0;
        }

        public void decrease() {
            for (int j = 0; j < data.length; j++) {
                if (data[j] > 0) {
                    data[j] = data[j] - 1;
                }
            }
        }

        public void clear() {
            Arrays.fill(data, 0);
        }
    }
    //</editor-fold>
}
