package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.FileHelper;
import lombok.NonNull;
import nbbrd.service.ServiceProvider;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public class ExcelBinaryBookFactory extends Book.Factory {

    private static final String XLSB_TYPE = "application/vnd.ms-excel.sheet.binary.macroEnabled.12";

    @Override
    public @NonNull String getName() {
        return "Excel Binary";
    }

    @Override
    public int getRank() {
        return WRAPPED_RANK;
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return singletonMap(XLSB_TYPE, singletonList(".xlsb"));
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".xlsb")
                && (!Files.exists(file) || FileHelper.hasMagicNumber(file, ZIP_HEADER));
    }

    @Override
    public @NonNull Book load(@NonNull File file) throws IOException {
        checkFile(file);
        return PoiBookBinaryReader.createBinary(file);
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        return PoiBookBinaryReader.createBinary(stream);
    }

    @Override
    public boolean canStore() {
        return false;
    }

    @Override
    public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
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

