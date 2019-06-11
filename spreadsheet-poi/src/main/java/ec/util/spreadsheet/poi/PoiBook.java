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

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import lombok.AccessLevel;
import org.apache.poi.EmptyFileException;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
final class PoiBook extends Book {

    @NonNull
    public static PoiBook create(@NonNull File file) throws IOException {
        try {
            return new PoiBook(new XSSFWorkbook(OPCPackage.open(file.getPath(), PackageAccess.READ)));
        } catch (OpenXML4JException | OpenXML4JRuntimeException | UnsupportedFileFormatException ex) {
            throw new IOException(file.getPath(), ex);
        } catch (EmptyFileException ex) {
            throw new EOFException();
        }
    }

    @NonNull
    public static PoiBook create(@NonNull InputStream stream) throws IOException {
        try {
            return new PoiBook(new XSSFWorkbook(OPCPackage.open(stream)));
        } catch (OpenXML4JException | OpenXML4JRuntimeException | UnsupportedFileFormatException ex) {
            throw new IOException(ex);
        } catch (EmptyFileException ex) {
            throw new EOFException();
        }
    }

    @NonNull
    public static PoiBook createClassic(@NonNull File file) throws IOException {
        try {
            return new PoiBook(new HSSFWorkbook(new POIFSFileSystem(file)));
        } catch (NotOLE2FileException | UnsupportedFileFormatException ex) {
            throw new IOException(file.getPath(), ex);
        } catch (EmptyFileException ex) {
            throw new EOFException(file.getPath());
        }
    }

    @NonNull
    public static PoiBook createClassic(@NonNull InputStream stream) throws IOException {
        try {
            return new PoiBook(new HSSFWorkbook(new POIFSFileSystem(stream)));
        } catch (NotOLE2FileException | UnsupportedFileFormatException ex) {
            throw new IOException(ex);
        } catch (EmptyFileException ex) {
            throw new EOFException();
        }
    }

    private final Workbook workbook;

    @Override
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    @Override
    public Sheet getSheet(int index) {
        try {
            return new PoiSheet(workbook.getSheetAt(index));
        } catch (IllegalArgumentException ex) {
            throw isSheetIndexOutOfBounds(index) ? new IndexOutOfBoundsException(ex.getMessage()) : ex;
        }
    }

    @Override
    public String getSheetName(@NonNegative int index) {
        try {
            return workbook.getSheetName(index);
        } catch (IllegalArgumentException ex) {
            throw isSheetIndexOutOfBounds(index) ? new IndexOutOfBoundsException(ex.getMessage()) : ex;
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }

    private boolean isSheetIndexOutOfBounds(int index) {
        return index < 0 || index >= getSheetCount();
    }
}
