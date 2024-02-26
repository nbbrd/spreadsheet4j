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
package ec.util.spreadsheet.html;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.FileHelper;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.XMLOutputFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public class HtmlBookFactory extends Book.Factory {

    private static final String HTML_TYPE = "text/html";

    private final XMLOutputFactory xof;

    public HtmlBookFactory() {
        this.xof = XMLOutputFactory.newInstance();
    }

    @Override
    public @NonNull String getName() {
        return "Html table";
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public @NonNull Book load(@NonNull File file) throws IOException {
        return newReader().read(file);
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        return newReader().read(stream);
    }

    @Override
    public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
        return singletonMap(HTML_TYPE, asList(".html", ".htm"));
    }

    @Override
    public boolean accept(File pathname) {
        return FileHelper.accept(pathname, this);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".html", ".htm")
                && (!Files.exists(file) || Files.size(file) > 0);
    }

    @Override
    public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
        newWriter().write(book, stream);
    }

    @Override
    public boolean isSupportedDataType(@NonNull Class<?> type) {
        return String.class.isAssignableFrom(type);
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private HtmlBookReader newReader() {
        return new HtmlBookReader();
    }

    private HtmlBookWriter newWriter() {
        return new HtmlBookWriter(xof);
    }
    //</editor-fold>
}
