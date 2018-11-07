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
import ioutil.IO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.annotation.Nonnull;
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
public final class XlsxReader {

    private final XlsxPackage.Factory packager = ZipPackage.FACTORY;
    private final XlsxEntryParser.Factory entryParser = SaxEntryParser.FACTORY;
    private final XlsxNumberingFormat.Factory numberingFormat = DefaultNumberingFormat.FACTORY;
    private final XlsxDateSystem.Factory dateSystem = DefaultDateSystem.FACTORY;
    private final XlsxSheetBuilder.Factory sheetBuilder = MULTI_CORE ? MultiSheetBuilder::of : DefaultSheetBuilder::of;

    @Nonnull
    public Book read(@Nonnull Path file) throws IOException {
        return createBookOrClose(packager.open(file));
    }

    @Nonnull
    public Book read(@Nonnull InputStream stream) throws IOException {
        return createBookOrClose(packager.open(stream));
    }

    private Book createBookOrClose(XlsxPackage pkg) throws IOException {
        try {
            return XlsxBook.create(pkg, this);
        } catch (Error | RuntimeException | IOException ex) {
            IO.ensureClosed(ex, pkg);
            throw ex;
        }
    }

    private static final boolean MULTI_CORE = Runtime.getRuntime().availableProcessors() > 1;
}
