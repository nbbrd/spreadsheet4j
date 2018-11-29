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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.swing.table.DefaultTableModel;
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
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".ods");
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

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
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
    //</editor-fold>
}
