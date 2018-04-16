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

import ioutil.Sax;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.internal.SaxXlsxParser;

/**
 * Parser for Office Open XML files.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface XlsxParser extends Closeable {

    void visitWorkbook(InputStream stream, WorkbookVisitor visitor) throws IOException;

    void visitSharedStrings(InputStream stream, SharedStringsVisitor visitor) throws IOException;

    void visitStyles(InputStream stream, StylesVisitor visitor) throws IOException;

    void visitSheet(InputStream stream, SheetVisitor visitor) throws IOException;

    interface WorkbookVisitor {

        void onSheet(@Nonnull String relationId, @Nonnull String name);

        void onDate1904(boolean date1904);
    }

    interface SharedStringsVisitor {

        void onSharedString(@Nonnull String str);
    }

    interface StylesVisitor {

        void onNumberFormat(int formatId, String formatCode) throws IllegalStateException;

        void onCellFormat(int formatId) throws IllegalStateException;
    }

    interface SheetVisitor {

        void onSheetData(@Nullable String sheetBounds) throws IllegalStateException;

        void onCell(
                @Nullable String ref,
                @Nonnull CharSequence value,
                @Nullable String dataType,
                @Nullable Integer styleIndex) throws IllegalStateException;
    }

    interface Factory {

        @Nonnull
        XlsxParser create() throws IOException;

        @Nonnull
        static Factory getDefault() {
            return () -> new SaxXlsxParser(Sax.createReader());
        }
    }
}
