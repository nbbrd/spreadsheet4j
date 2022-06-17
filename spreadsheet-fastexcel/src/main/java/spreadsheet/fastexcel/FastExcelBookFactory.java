/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package spreadsheet.fastexcel;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.FileHelper;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public final class FastExcelBookFactory extends Book.Factory {

    private static final String XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Override
    public String getName() {
        return "Xlsx";
    }

    @Override
    public int getRank() {
        return WRAPPED_RANK + 1;
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return Collections.singletonMap(XLSX_TYPE, singletonList(".xlsx"));
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".xlsx")
                && (!Files.exists(file) || FileHelper.hasMagicNumber(file, ZIP_HEADER));
    }

    @Override
    public boolean canLoad() {
        return false;
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean canStore() {
        return true;
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        Workbook workbook = new Workbook(stream, "spreadsheet4j", null);
        writeBookData(workbook, book);
        workbook.finish();
    }

    @NonNull
    private static File checkFile(@NonNull File file) throws IOException {
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

    // https://en.wikipedia.org/wiki/List_of_file_signatures
    private static final byte[] ZIP_HEADER = {(byte) 0x50, (byte) 0x4B};

    private static void writeBookData(Workbook workbook, Book book) throws IOException {
        for (int s = 0; s < book.getSheetCount2(); s++) {
            Sheet sheet = book.getSheet(s);
            Worksheet worksheet = workbook.newWorksheet(sheet.getName());
            writeSheetData(worksheet, sheet);
            worksheet.finish();
        }
    }

    private static void writeSheetData(Worksheet worksheet, Sheet sheet) {
        for (int i = 0; i < sheet.getRowCount(); i++) {
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                Object cellValue = sheet.getCellValue(i, j);
                if (cellValue instanceof Number) {
                    worksheet.value(i, j, (Number) cellValue);
                } else if (cellValue instanceof Date) {
                    worksheet.value(i, j, (Date) cellValue);
                    worksheet.style(i, j).format("yyyy-MM-dd H:mm:ss").set();
                } else if (cellValue instanceof String) {
                    worksheet.value(i, j, (String) cellValue);
                }
            }
        }
    }
}
