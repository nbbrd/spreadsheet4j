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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openide.util.lookup.ServiceProvider;
import spreadsheet.xlsx.XlsxReader;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Book.Factory.class)
public class ExcelBookFactory extends Book.Factory {

    private final AtomicBoolean fast;

    public ExcelBookFactory() {
        this.fast = new AtomicBoolean(true);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setFast(boolean fast) {
        this.fast.set(fast);
    }

    public boolean isFast() {
        return fast.get();
    }
    //</editor-fold>

    @Override
    public String getName() {
        return "Excel";
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
        return hasValidExtension(file) && (Files.exists(file) ? hasValidHeader(file) : true);
    }

    @Override
    public Book load(File file) throws IOException {
        checkFile(file);
        return fast.get()
                ? new XlsxReader().read(file.toPath())
                : PoiBook.create(file);
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return fast.get()
                ? new XlsxReader().read(stream)
                : PoiBook.create(stream);
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        // Currenty, inline string is not supported in FastPoiBook -> use of shared strings table
        SXSSFWorkbook target = new SXSSFWorkbook(null, 100, false, true);
        try {
            PoiBookWriter.copy(book, target);
            target.write(stream);
        } finally {
            // dispose of temporary files backing this workbook on disk
            target.dispose();
        }
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

    private static boolean hasValidExtension(Path file) {
        String tmp = file.getName(file.getNameCount() - 1).toString().toLowerCase(Locale.ROOT);
        return tmp.endsWith(".xlsx") || tmp.endsWith(".xlsm");
    }

    // https://en.wikipedia.org/wiki/List_of_file_signatures
    private static boolean hasValidHeader(Path file) {
        try {
            try (InputStream stream = Files.newInputStream(file)) {
                int first = stream.read();
                if (first == -1 || first != 0x50) {
                    return false;
                }
                int second = stream.read();
                if (second == -1 || second != 0x4B) {
                    return false;
                }
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
    }
}
