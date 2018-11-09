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
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class DefaultSheetBuilder implements XlsxSheetBuilder {

    public static DefaultSheetBuilder of(XlsxDateSystem dateSystem, IntFunction<String> sharedStrings, IntPredicate dateFormats) {
        return new DefaultSheetBuilder(new XlsxValueFactory(dateSystem, sharedStrings, dateFormats));
    }

    private final XlsxValueFactory valueFactory;
    private final CellRefHelper refHelper;
    private ArraySheet.Builder arraySheetBuilder;

    private DefaultSheetBuilder(XlsxValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.refHelper = new CellRefHelper();
        this.arraySheetBuilder = ArraySheet.builder();
    }

    @Override
    public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
        arraySheetBuilder = ArraySheet.builder(sheetBounds).name(sheetName);
        return this;
    }

    @Override
    public XlsxSheetBuilder put(String ref, CharSequence value, XlsxDataType dataType, int styleIndex) {
        Object cellValue = valueFactory.getValue(dataType, value.toString(), styleIndex);
        if (cellValue != null && refHelper.parse(ref)) {
            arraySheetBuilder.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), cellValue);
        }
        return this;
    }

    @Override
    public Sheet build() {
        return arraySheetBuilder.build();
    }

    @Override
    public void close() {
        arraySheetBuilder.clear();
    }
}
