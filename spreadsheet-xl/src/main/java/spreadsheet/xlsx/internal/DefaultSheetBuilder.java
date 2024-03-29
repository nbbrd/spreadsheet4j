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
package spreadsheet.xlsx.internal;

import spreadsheet.xlsx.XlsxDataType;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.util.Date;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class DefaultSheetBuilder implements XlsxSheetBuilder {

    public static DefaultSheetBuilder of(XlsxDateSystem dateSystem, List<String> sharedStrings, boolean[] dateFormats) {
        return new DefaultSheetBuilder(new XlsxValueFactory(dateSystem, o -> dateFormats[o]), sharedStrings);
    }

    private final XlsxValueFactory valueFactory;
    private final List<String> sharedStrings;
    private final CellRefHelper refHelper;
    private ExtCallback callback;

    private DefaultSheetBuilder(XlsxValueFactory valueFactory, List<String> sharedStrings) {
        this.valueFactory = valueFactory;
        this.sharedStrings = sharedStrings;
        this.refHelper = new CellRefHelper();
        this.callback = NoOpCallback.INSTANCE;
    }

    @Override
    public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
        callback = newCallback(sheetName, sheetBounds);
        return this;
    }

    private ExtCallback newCallback(String sheetName, String sheetBounds) {
        if (refHelper.parseEnd(sheetBounds)) {
            int rowCount = refHelper.getRowIndex() + 1;
            int columnCount = refHelper.getColumnIndex() + 1;
            if (!CompactSheet.isOverflow(rowCount, columnCount)) {
                return new CompactCallback(refHelper, CompactSheet.builder(rowCount, columnCount, sheetName, sharedStrings));
            }
        }
        return new ArraySheetCallback(sharedStrings, refHelper, ArraySheet.builder().name(sheetName));
    }

    @Override
    public XlsxSheetBuilder put(String ref, CharSequence value, XlsxDataType dataType, int styleIndex) {
        valueFactory.parse(callback.moveTo(ref), value, dataType, styleIndex);
        return this;
    }

    @Override
    public Sheet build() {
        return callback.build();
    }

    @Override
    public void close() {
        callback = NoOpCallback.INSTANCE;
    }

    private interface ExtCallback extends XlsxValueFactory.Callback {

        @NonNull
        ExtCallback moveTo(@NonNull String ref);

        @NonNull
        Sheet build();
    }

    private enum NoOpCallback implements ExtCallback {

        INSTANCE;

        @Override
        public ExtCallback moveTo(String ref) {
            return this;
        }

        @Override
        public Sheet build() {
            return ArraySheet.copyOf("", new Object[0][0]);
        }

        @Override
        public void onNumber(double number) {
        }

        @Override
        public void onDate(long date) {
        }

        @Override
        public void onSharedString(int index) {
        }

        @Override
        public void onString(CharSequence string) {
        }

        @Override
        public void onNull() {
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ArraySheetCallback implements ExtCallback {

        private final List<String> sharedStrings;
        private final CellRefHelper refHelper;
        private final ArraySheet.Builder sheet;
        private String ref;

        @Override
        public ExtCallback moveTo(String ref) {
            this.ref = ref;
            return this;
        }

        @Override
        public Sheet build() {
            return sheet.build();
        }

        @Override
        public void onNumber(double number) {
            if (refHelper.parse(ref)) {
                sheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), number);
            }
        }

        @Override
        public void onDate(long date) {
            if (refHelper.parse(ref)) {
                sheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), new Date(date));
            }
        }

        @Override
        public void onSharedString(int index) {
            if (refHelper.parse(ref)) {
                sheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), sharedStrings.get(index));
            }
        }

        @Override
        public void onString(CharSequence string) {
            if (refHelper.parse(ref)) {
                sheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), string.toString());
            }
        }

        @Override
        public void onNull() {
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class CompactCallback implements ExtCallback {

        private final CellRefHelper refHelper;
        private final CompactSheet.Builder sheet;
        private String ref;

        @Override
        public ExtCallback moveTo(String ref) {
            this.ref = ref;
            return this;
        }

        @Override
        public Sheet build() {
            return sheet.build();
        }

        @Override
        public void onNumber(double number) {
            if (refHelper.parse(ref)) {
                sheet.putNumber(refHelper.getRowIndex(), refHelper.getColumnIndex(), number);
            }
        }

        @Override
        public void onDate(long date) {
            if (refHelper.parse(ref)) {
                sheet.putDate(refHelper.getRowIndex(), refHelper.getColumnIndex(), date);
            }
        }

        @Override
        public void onSharedString(int index) {
            if (refHelper.parse(ref)) {
                sheet.putSharedString(refHelper.getRowIndex(), refHelper.getColumnIndex(), index);
            }
        }

        @Override
        public void onString(CharSequence string) {
            if (refHelper.parse(ref)) {
                sheet.putString(refHelper.getRowIndex(), refHelper.getColumnIndex(), string.toString());
            }
        }

        @Override
        public void onNull() {
        }
    }
}
