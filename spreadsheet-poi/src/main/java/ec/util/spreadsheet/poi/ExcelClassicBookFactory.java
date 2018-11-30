/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Book;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Book.Factory.class)
public class ExcelClassicBookFactory extends Book.Factory {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase(Locale.ROOT).endsWith(".xls");
    }

    @Override
    public String getName() {
        return "Excel Classic";
    }

    @Override
    public Book load(File file) throws IOException {
        checkFile(file);
        return PoiBook.createClassic(file);
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return PoiBook.createClassic(stream);
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        HSSFWorkbook target = new HSSFWorkbook();
        PoiBookWriter.copy(book, target);
        target.write(stream);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @Nonnull
    private static File checkFile(@Nonnull File file) throws FileSystemException {
        if (!file.exists()) {
            throw new NoSuchFileException(file.getPath());
        }
        if (!file.canRead() || file.isDirectory()) {
            throw new AccessDeniedException(file.getPath());
        }
        return file;
    }
    //</editor-fold>
}
