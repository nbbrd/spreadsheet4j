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
import javax.swing.table.DefaultTableModel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Book.Factory.class)
public class OpenDocumentBookFactory extends Book.Factory {

    @Override
    public String getName() {
        return "Open Document";
    }

    @Override
    public boolean accept(File file) {
        try {
            return accept(file.toPath());
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".ods")
                && (Files.exists(file) ? FileHelper.hasMagicNumber(file, ZIP_HEADER) : true);
    }

    @Override
    public Book load(File file) throws IOException {
        checkFile(file);
        return new OdBook(SpreadSheet.create(new ODPackage(file)));
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return new OdBook(SpreadSheet.create(new ODPackage(stream)));
    }

    @Override
    public boolean canStore() {
        return true;
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        toOdSpreadSheet(book).getPackage().save(stream);
    }

    @Override
    public void store(File file, Book book) throws IOException {
        toOdSpreadSheet(book).saveAs(file);
    }

    private static SpreadSheet toOdSpreadSheet(Book book) throws IOException {
        SpreadSheet result = SpreadSheet.createEmpty(new DefaultTableModel());
        book.forEach((sheet, index) -> {
            org.jopendocument.dom.spreadsheet.Sheet odSheet = result.addSheet(sheet.getName());
            odSheet.setRowCount(sheet.getRowCount());
            odSheet.setColumnCount(sheet.getColumnCount());
            sheet.forEachValue((i, j, v) -> odSheet.setValueAt(v, j, i));
        });
        result.getSheet(0).detach();
        return result;
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
