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
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.util.Date;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class DefaultSheetBuilder implements XlsxSheetBuilder {

    public static DefaultSheetBuilder of(XlsxDateSystem dateSystem, IntFunction<String> sharedStrings, IntPredicate dateFormats) {
        return new DefaultSheetBuilder(new XlsxValueFactory(dateSystem, dateFormats), sharedStrings);
    }

    private final XlsxValueFactory valueFactory;
    private final IntFunction<String> sharedStrings;
    private final CellRefHelper refHelper;
    private ExtCallback callback;

    private DefaultSheetBuilder(XlsxValueFactory valueFactory, IntFunction<String> sharedStrings) {
        this.valueFactory = valueFactory;
        this.sharedStrings = sharedStrings;
        this.refHelper = new CellRefHelper();
        this.callback = NoOpCallback.INSTANCE;
    }

    @Override
    public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
        callback = new ArraySheetCallback(sharedStrings, refHelper, ArraySheet.builder(sheetBounds).name(sheetName));
        return this;
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

        @Nonnull
        ExtCallback moveTo(@Nonnull String ref);

        @Nonnull
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
        public void onString(String string) {
        }

        @Override
        public void onNull() {
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ArraySheetCallback implements ExtCallback {

        private final IntFunction<String> sharedStrings;
        private final CellRefHelper refHelper;
        private final ArraySheet.Builder arraySheet;
        private String ref;

        @Override
        public ExtCallback moveTo(String ref) {
            this.ref = ref;
            return this;
        }

        @Override
        public Sheet build() {
            return arraySheet.build();
        }

        @Override
        public void onNumber(double number) {
            if (refHelper.parse(ref)) {
                arraySheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), number);
            }
        }

        @Override
        public void onDate(long date) {
            if (refHelper.parse(ref)) {
                arraySheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), new Date(date));
            }
        }

        @Override
        public void onSharedString(int index) {
            if (refHelper.parse(ref)) {
                arraySheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), sharedStrings.apply(index));
            }
        }

        @Override
        public void onString(String string) {
            if (refHelper.parse(ref)) {
                arraySheet.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), string);
            }
        }

        @Override
        public void onNull() {
        }
    }
}
