package ec.util.spreadsheet.markdown;

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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

@ServiceProvider(Book.Factory.class)
public class MarkdownBookFactory extends Book.Factory {

    private static final String MARKDOWN_TYPE = "text/markdown";

    @Override
    public @NonNull String getName() {
        return "Markdown table";
    }

    @Override
    public int getRank() {
        return Book.Factory.NATIVE_RANK;
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new EOFException();
        }
        try (Reader reader = new InputStreamReader(stream, UTF_8)) {
            return MarkdownUtils.parseBook(reader);
        }
    }

    @Override
    public @NonNull Book load(@NonNull File file) throws IOException {
        return super.load(checkFile(file));
    }

    @Override
    public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
        try (Writer writer = new OutputStreamWriter(stream, UTF_8)) {
            for (int s = 0; s < book.getSheetCount2(); s++) {
                if (s > 0) writer.write('\n');
                MarkdownUtils.writeSheet(book.getSheet(s), writer);
            }
        }
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return singletonMap(MARKDOWN_TYPE, asList(".md", ".markdown"));
    }

    @Override
    public boolean accept(File pathname) {
        return FileHelper.accept(pathname, this);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".md", ".markdown")
                && (!Files.exists(file) || Files.size(file) > 0);
    }

    @Override
    public boolean isSupportedDataType(@NonNull Class<?> type) {
        return String.class.isAssignableFrom(type);
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
}

