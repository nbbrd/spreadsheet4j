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
package spreadsheet.xlsx;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.FileHelper;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public final class XlsxBookFactory extends Book.Factory {

    private static final String XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String XLSM_TYPE = "application/vnd.ms-excel.sheet.macroEnabled.12";

    @Override
    public String getName() {
        return "Xlsx";
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        Map<String, List<String>> result = new HashMap<>();
        result.put(XLSM_TYPE, singletonList(".xlsm"));
        result.put(XLSX_TYPE, singletonList(".xlsx"));
        return result;
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".xlsx", ".xlsm")
                && (!Files.exists(file) || FileHelper.hasMagicNumber(file, ZIP_HEADER));
    }

    @Override
    public Book load(File file) throws IOException {
        checkFile(file);
        return new XlsxReader().read(file.toPath());
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return new XlsxReader().read(stream);
    }

    @Override
    public boolean canStore() {
        return false;
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        throw new UnsupportedOperationException("Not supported");
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
