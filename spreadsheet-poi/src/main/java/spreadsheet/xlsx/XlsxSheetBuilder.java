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
package spreadsheet.xlsx;

import ec.util.spreadsheet.Sheet;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface XlsxSheetBuilder extends Closeable {

    @NonNull
    XlsxSheetBuilder reset(@NonNull String sheetName, @Nullable String sheetBounds);

    @NonNull
    XlsxSheetBuilder put(@NonNull String ref, @NonNull CharSequence value, @NonNull XlsxDataType dataType, int styleIndex);

    @NonNull
    Sheet build();

    interface Factory {

        @NonNull
        XlsxSheetBuilder create(
                @NonNull XlsxDateSystem dateSystem,
                @NonNull List<String> sharedStrings,
                @NonNull boolean[] dateFormats
        ) throws IOException;
    }
}
