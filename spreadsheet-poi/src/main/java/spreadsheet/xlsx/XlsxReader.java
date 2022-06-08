/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
import shaded.spreadsheet.nbbrd.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import spreadsheet.xlsx.internal.DefaultNumberingFormat;
import spreadsheet.xlsx.internal.SaxEntryParser;
import spreadsheet.xlsx.internal.XlsxBook;
import spreadsheet.xlsx.internal.DefaultDateSystem;
import spreadsheet.xlsx.internal.DefaultSheetBuilder;
import spreadsheet.xlsx.internal.MultiSheetBuilder;
import spreadsheet.xlsx.internal.ZipPackage;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Getter
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class XlsxReader {

    private final XlsxPackage.Factory packager;
    private final XlsxEntryParser.Factory entryParser;
    private final XlsxNumberingFormat.Factory numberingFormat;
    private final XlsxDateSystem.Factory dateSystem;
    @lombok.With
    private final XlsxSheetBuilder.Factory sheetBuilder;

    public XlsxReader() {
        this(
                ZipPackage.FACTORY,
                SaxEntryParser.FACTORY,
                DefaultNumberingFormat.FACTORY,
                DefaultDateSystem.FACTORY,
                MULTI_CORE ? MultiSheetBuilder::of : DefaultSheetBuilder::of
        );
    }

    @NonNull
    public Book read(@NonNull Path file) throws IOException {
        return createBookOrClose(packager.open(file));
    }

    @NonNull
    public Book read(@NonNull InputStream stream) throws IOException {
        return createBookOrClose(packager.open(stream));
    }

    private Book createBookOrClose(XlsxPackage pkg) throws IOException {
        try {
            return XlsxBook.create(pkg, this);
        } catch (Error | RuntimeException | IOException ex) {
            Resource.ensureClosed(ex, pkg);
            throw ex;
        }
    }

    private static final boolean MULTI_CORE = Runtime.getRuntime().availableProcessors() > 1;
}
