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
package ec.util.spreadsheet.poi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.io.IOException;
import java.util.Date;

/**
 * @author Philippe Charles
 */
final class PoiBookWriter {

    public static void copy(ec.util.spreadsheet.Book source, Workbook target) throws IOException {
        new PoiBookWriter(target).copy(source);
    }

    private final Workbook workbook;
    private final CellStyle dateStyle;

    private PoiBookWriter(Workbook workbook) {
        this.workbook = workbook;
        this.dateStyle = createDateStyle(workbook);
    }

    private static CellStyle createDateStyle(Workbook workbook) {
//        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle result = workbook.createCellStyle();
//        result.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy"));
        result.setDataFormat((short) 14);
        return result;
    }

    private void copy(ec.util.spreadsheet.Book source) throws IOException {
        source.forEach((sheet, index) -> copy(sheet, workbook.createSheet(sheet.getName())));
    }

    private void copy(ec.util.spreadsheet.Sheet source, Sheet target) {
        for (int i = 0; i < source.getRowCount(); i++) {
            Row row = target.createRow(i);
            for (int j = 0; j < source.getColumnCount(); j++) {
                Object cellValue = source.getCellValue(i, j);
                if (cellValue != null) {
                    copyCellValue(cellValue, row.createCell(j));
                }
            }
        }
        if (target instanceof SXSSFSheet) {
            ((SXSSFSheet) target).trackAllColumnsForAutoSizing();
        }
        for (int j = 0; j < source.getColumnCount(); j++) {
            target.autoSizeColumn(j);
        }
    }

    private void copyCellValue(Object cellValue, Cell target) {
        if (cellValue instanceof Date) {
            target.setCellValue((Date) cellValue);
            target.setCellStyle(dateStyle);
        } else if (cellValue instanceof Number) {
            target.setCellValue(((Number) cellValue).doubleValue());
        } else if (cellValue instanceof String) {
            target.setCellValue((String) cellValue);
        }
    }
}
