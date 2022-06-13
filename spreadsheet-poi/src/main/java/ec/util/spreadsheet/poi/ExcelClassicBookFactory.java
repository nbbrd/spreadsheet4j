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

import ec.util.spreadsheet.helpers.FileHelper;
import ec.util.spreadsheet.Book;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import nbbrd.service.ServiceProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public class ExcelClassicBookFactory extends Book.Factory {

    private static final String XLS_TYPE = "application/vnd.ms-excel";

    @Override
    public String getName() {
        return "Excel Classic";
    }

    @Override
    public int getRank() {
        return WRAPPED_RANK;
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return singletonMap(XLS_TYPE, singletonList(".xls"));
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this::accept);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".xls")
                && (Files.exists(file) ? FileHelper.hasMagicNumber(file, XLS_HEADER) : true);
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

    @NonNull
    private static File checkFile(@NonNull File file) throws FileSystemException {
        if (!file.exists()) {
            throw new NoSuchFileException(file.getPath());
        }
        if (!file.canRead() || file.isDirectory()) {
            throw new AccessDeniedException(file.getPath());
        }
        return file;
    }

    // https://en.wikipedia.org/wiki/List_of_file_signatures
    private static final byte[] XLS_HEADER = {(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1};
}
