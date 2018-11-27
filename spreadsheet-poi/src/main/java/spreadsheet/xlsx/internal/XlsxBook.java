/*
 * Copyright 2016 National Bank of Belgium
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
package spreadsheet.xlsx.internal;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import ioutil.IO;
import ioutil.Sax;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.XlsxDataType;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxPackage;
import spreadsheet.xlsx.XlsxReader;
import spreadsheet.xlsx.XlsxSheetBuilder;
import spreadsheet.xlsx.XlsxEntryParser;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class XlsxBook extends Book {

    @Nonnull
    public static XlsxBook create(@Nonnull XlsxPackage pkg, @Nonnull XlsxReader reader) throws IOException {
        XlsxEntryParser mainEntryParser = null;

        try {
            mainEntryParser = reader.getEntryParser().create();

            WorkbookData data = parseWorkbook(pkg::getWorkbook, mainEntryParser);

            return new XlsxBook(pkg, data.sheets,
                    dateSystemOf(reader.getDateSystem(), data.date1904),
                    sharedStringsOf(pkg, mainEntryParser),
                    dateFormatsOf(pkg, mainEntryParser, reader.getNumberingFormat()),
                    mainEntryParser,
                    reader.getSheetBuilder());
        } catch (IOException ex) {
            closeAll(ex, mainEntryParser);
            throw ex;
        }
    }

    private static Supplier<XlsxDateSystem> dateSystemOf(XlsxDateSystem.Factory dateSystem, boolean date1904) {
        return () -> dateSystem.of(date1904);
    }

    private static IO.Supplier<List<String>> sharedStringsOf(XlsxPackage pkg, XlsxEntryParser entryParser) {
        return () -> parseSharedStrings(pkg::getSharedStrings, entryParser);
    }

    private static IO.Supplier<boolean[]> dateFormatsOf(XlsxPackage pkg, XlsxEntryParser entryParser, XlsxNumberingFormat.Factory numberingFormat) {
        return () -> parseStyles(numberingFormat.of(), pkg::getStyles, entryParser);
    }

    private final XlsxPackage pkg;
    private final List<SheetMeta> sheets;
    private final Supplier<XlsxDateSystem> dateSystem;
    private final IO.Supplier<List<String>> sharedStrings;
    private final IO.Supplier<boolean[]> dateFormats;
    private final XlsxEntryParser mainEntryParser;
    private final XlsxSheetBuilder.Factory mainSheetBuilderFactory;
    private XlsxSheetBuilder mainSheetBuilder = null;

    @Override
    public void close() throws IOException {
        closeAll(null, pkg, mainEntryParser, mainSheetBuilder);
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Sheet getSheet(int index) throws IOException {
        if (mainSheetBuilder == null) {
            mainSheetBuilder = mainSheetBuilderFactory.create(dateSystem.get(), sharedStrings.getWithIO(), dateFormats.getWithIO());
        }
        return getSheet(index, mainSheetBuilder, mainEntryParser);
    }

    @Override
    public String getSheetName(@Nonnegative int index) {
        return sheets.get(index).getName();
    }

    @Override
    public void parallelForEach(ObjIntConsumer<? super Sheet> action) throws IOException {
        XlsxDateSystem x = dateSystem.get();
        List<String> y = sharedStrings.getWithIO();
        boolean[] z = dateFormats.getWithIO();

        IntStream.range(0, getSheetCount())
                .parallel()
                .forEach(index -> {
                    try {
                        Sheet sheet = getSheet(index, DefaultSheetBuilder.of(x, y, z), new SaxEntryParser(Sax.createReader()));
                        action.accept(sheet, index);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
    }

    private Sheet getSheet(int index, XlsxSheetBuilder sheetBuilder, XlsxEntryParser entryParser) throws IOException {
        SheetMeta meta = sheets.get(index);
        return parseSheet(meta.name, sheetBuilder, () -> pkg.getSheet(meta.relationId), entryParser);
    }

    static void closeAll(IOException initial, Closeable... closeables) throws IOException {
        for (Closeable o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (IOException ex) {
                    if (initial == null) {
                        initial = ex;
                    } else {
                        initial.addSuppressed(ex);
                    }
                }
            }
        }
        if (initial != null) {
            throw initial;
        }
    }

    @lombok.Value
    static class WorkbookData {

        List<SheetMeta> sheets;
        boolean date1904;
    }

    @lombok.Value
    static class SheetMeta {

        @Nonnull
        String relationId;
        @Nonnull
        String name;
    }

    static WorkbookData parseWorkbook(IO.Supplier<? extends InputStream> byteSource, XlsxEntryParser parser) throws IOException {
        WorkbookVisitorImpl result = new WorkbookVisitorImpl();
        try (InputStream stream = byteSource.getWithIO()) {
            parser.visitWorkbook(stream, result);
        }
        return result.build();
    }

    private static final class WorkbookVisitorImpl implements XlsxEntryParser.WorkbookVisitor {

        private final List<SheetMeta> sheets = new ArrayList<>();
        private boolean date1904 = false;

        @Override
        public void onSheet(String relationId, String name) {
            sheets.add(new SheetMeta(relationId, name));
        }

        @Override
        public void onDate1904(boolean date1904) {
            this.date1904 = date1904;
        }

        WorkbookData build() {
            return new WorkbookData(sheets, date1904);
        }
    }

    static List<String> parseSharedStrings(IO.Supplier<? extends InputStream> byteSource, XlsxEntryParser parser) throws IOException {
        List<String> result = new ArrayList<>();
        try (InputStream stream = byteSource.getWithIO()) {
            parser.visitSharedStrings(stream, o -> result.add(Objects.requireNonNull(o)));
        }
        return result;
    }

    static boolean[] parseStyles(XlsxNumberingFormat dateFormat, IO.Supplier<? extends InputStream> byteSource, XlsxEntryParser parser) throws IOException {
        StylesVisitorImpl result = new StylesVisitorImpl(dateFormat);
        try (InputStream stream = byteSource.getWithIO()) {
            parser.visitStyles(stream, result);
        }
        return result.build();
    }

    private static final class StylesVisitorImpl implements XlsxEntryParser.StylesVisitor {

        private final XlsxNumberingFormat dateFormat;
        private final List<Integer> orderedListOfIds = new ArrayList<>();
        private final Map<Integer, String> numberFormats = new HashMap<>();

        StylesVisitorImpl(XlsxNumberingFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public void onNumberFormat(int formatId, String formatCode) {
            numberFormats.put(formatId, formatCode);
        }

        @Override
        public void onCellFormat(int formatId) {
            orderedListOfIds.add(formatId);
        }

        public boolean[] build() {
            // Style order matters! -> accessed by index in sheets
            boolean[] result = new boolean[orderedListOfIds.size()];
            for (int i = 0; i < result.length; i++) {
                int numFmtId = orderedListOfIds.get(i);
                result[i] = dateFormat.isExcelDateFormat(numFmtId, numberFormats.getOrDefault(numFmtId, null));
            }
            return result;
        }
    }

    static Sheet parseSheet(String name, XlsxSheetBuilder sheetBuilder, IO.Supplier<? extends InputStream> byteSource, XlsxEntryParser parser) throws IOException {
        SheetVisitorImpl result = new SheetVisitorImpl(name, sheetBuilder);
        try (InputStream stream = byteSource.getWithIO()) {
            parser.visitSheet(stream, result);
        }
        return result.build();
    }

    private static final class SheetVisitorImpl implements XlsxEntryParser.SheetVisitor {

        private final String sheetName;
        private final XlsxSheetBuilder sheetBuilder;
        private boolean inData;

        SheetVisitorImpl(String sheetName, XlsxSheetBuilder sheetBuilder) {
            this.sheetName = sheetName;
            this.sheetBuilder = sheetBuilder;
            this.inData = false;
        }

        @Override
        public void onSheetData(String sheetBounds) {
            if (inData) {
                throw new IllegalStateException();
            }
            sheetBuilder.reset(sheetName, sheetBounds);
            inData = true;
        }

        @Override
        public void onCell(String ref, CharSequence value, XlsxDataType dataType, int styleIndex) {
            if (!inData) {
                throw new IllegalStateException();
            }
            sheetBuilder.put(ref, value, dataType, styleIndex);
        }

        public Sheet build() {
            return sheetBuilder.build();
        }
    }
}
