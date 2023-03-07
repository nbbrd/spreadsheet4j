/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package ec.util.spreadsheet.od;

import com.github.miachm.sods.NotAnOdsException;
import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.FileHelper;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public class OpenDocumentBookFactory extends Book.Factory {

    private static final String ODS_TYPE = "application/vnd.oasis.opendocument.spreadsheet";

    @Override
    public @NonNull String getName() {
        return "Open Document";
    }

    @Override
    public int getRank() {
        return WRAPPED_RANK;
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return singletonMap(ODS_TYPE, singletonList(".ods"));
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this::accept);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".ods")
                && (Files.exists(file) ? FileHelper.hasMagicNumber(file, ZIP_HEADER) : true);
    }

    @Override
    public @NonNull Book load(@NonNull File file) throws IOException {
        checkFile(file);
        try {
            return new OdBook(new SpreadSheet(file));
        } catch (NotAnOdsException ex) {
            throw new IOException(file.getPath(), ex);
        }
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        try {
            return new OdBook(new SpreadSheet(stream));
        } catch (NotAnOdsException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean canStore() {
        return true;
    }

    @Override
    public void store(@NonNull File file, @NonNull Book book) throws IOException {
        toSpreadSheet(book).save(file);
    }

    @Override
    public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
        toSpreadSheet(book).save(stream);
    }

    private static SpreadSheet toSpreadSheet(Book book) throws IOException {
        SpreadSheet result = new SpreadSheet();
        for (int s = 0; s < book.getSheetCount2(); s++) {
            result.appendSheet(toSheet(book.getSheet(s)));
        }
        return result;
    }

    private static Sheet toSheet(ec.util.spreadsheet.Sheet sheet) {
        Sheet result = new Sheet(sheet.getName());
        result.deleteRow(0);
        result.deleteColumn(0);

        result.appendRows(sheet.getRowCount());
        result.appendColumns(sheet.getColumnCount());

        Range data = result.getDataRange();
        sheet.forEachValue((i, j, value) -> data.getCell(i, j).setValue(toCellValue(value)));

        return result;
    }

    private static Object toCellValue(Object obj) {
        if (obj instanceof Date) {
            return ((Date) obj).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return obj;
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
}
