package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import lombok.AccessLevel;
import lombok.NonNull;
import org.apache.poi.EmptyFileException;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFComment;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
final class PoiBookBinaryReader {

    @NonNull
    public static ArrayBook createBinary(@NonNull File file) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ)) {
            return readBook(new FixedXSSFBReader(pkg));
        } catch (OpenXML4JException | OpenXML4JRuntimeException | UnsupportedFileFormatException ex) {
            throw new IOException(file.getPath(), ex);
        } catch (EmptyFileException ex) {
            throw new EOFException();
        }
    }

    @NonNull
    public static ArrayBook createBinary(@NonNull InputStream stream) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(stream)) {
            return readBook(new FixedXSSFBReader(pkg));
        } catch (OpenXML4JException | OpenXML4JRuntimeException | UnsupportedFileFormatException ex) {
            throw new IOException(ex);
        } catch (EmptyFileException ex) {
            throw new EOFException();
        }
    }

    @NonNull
    private static ArrayBook readBook(@NonNull XSSFBReader reader) throws IOException {
        ArrayBook.Builder result = ArrayBook.builder().clear();
        try {
            XSSFBReader.SheetIterator iter = reader.getSheetIterator();
            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    result.sheet(readSheet(stream, iter.getSheetName(), reader));
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return result.build();
    }

    private static ArraySheet readSheet(InputStream stream, String name, XSSFBReader reader) throws IOException {
        ArraySheet.Builder result = ArraySheet.builder().name(name);
        try {
            XSSFBStylesTable styles = reader.getXSSFBStylesTable();
            SharedStrings strings = reader.getSharedStringsTable();
            new XSSFBSheetHandler(
                    stream,
                    styles,
                    null,  // XSSFBCommentsTable - optional
                    strings,
                    new CustomHandler(result),
                    false
            ).parse();
        } catch (Exception ex) {
            throw new IOException("Error parsing sheet: " + name, ex);
        }
        return result.build();
    }

    private static final class FixedXSSFBReader extends XSSFBReader {

        public FixedXSSFBReader(OPCPackage pkg) throws IOException, OpenXML4JException {
            super(pkg);
        }

        @Override
        public SharedStrings getSharedStringsTable() throws IOException, InvalidFormatException {
            SharedStrings result = super.getSharedStringsTable();
            if (result != null) return result;
            try {
                return new XSSFBSharedStringsTable(pkg);
            } catch (Exception ex) {
                throw new InvalidFormatException("Failed to parse SharedStringsTable", ex);
            }
        }
    }

    private static final class CustomHandler implements XSSFBSheetHandler.XSSFBSheetContentsHandler {

        private final ArraySheet.Builder builder;

        public CustomHandler(@NonNull ArraySheet.Builder builder) {
            this.builder = builder;
        }

        @Override
        public void startRow(int rowNum) {
            // Not needed for ArraySheet
        }

        @Override
        public void endRow(int rowNum) {
            // Not needed for ArraySheet
        }

        @Override
        public void stringCell(String cellReference, String value, XSSFComment comment) {
            if (cellReference != null && value != null && !value.isEmpty()) {
                CellAddress addr = new CellAddress(cellReference);
                builder.value(addr.getRow(), addr.getColumn(), value);
            }
        }

        @Override
        public void doubleCell(String cellReference, double value, XSSFComment comment, ExcelNumberFormat numberFormat) {
            if (cellReference != null) {
                CellAddress addr = new CellAddress(cellReference);
                Object cellValue = isDateFormat(numberFormat) ? DateUtil.getJavaDate(value) : value;
                builder.value(addr.getRow(), addr.getColumn(), cellValue);
            }
        }

        @Override
        public void booleanCell(String cellReference, boolean value, XSSFComment comment) {
            if (cellReference != null) {
                CellAddress addr = new CellAddress(cellReference);
                builder.value(addr.getRow(), addr.getColumn(), value);
            }
        }

        @Override
        public void errorCell(String cellReference, FormulaError error, XSSFComment comment) {
            // Skip error cells
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Not needed for data extraction
        }

        @Override
        public void endSheet() {
            // Nothing to do
        }

        private static boolean isDateFormat(ExcelNumberFormat numberFormat) {
            return numberFormat != null
                    && DateUtil.isADateFormat(numberFormat.getIdx(), numberFormat.getFormat());
        }
    }
}





