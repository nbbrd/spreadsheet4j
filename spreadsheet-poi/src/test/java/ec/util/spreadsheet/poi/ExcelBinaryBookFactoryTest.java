package ec.util.spreadsheet.poi;

import _test.PoiSamples;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

import static _test.PoiSamples.XLSB_TOP5;
import static ec.util.spreadsheet.tck.Conditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class ExcelBinaryBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        BookFactoryAssert.assertThat(new ExcelBinaryBookFactory())
                .isCompliant(XLSB_TOP5, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        assertThat(new ExcelBinaryBookFactory())
                .is(ableToLoadContent())
                .isNot(ableToStoreContent());
    }

    @Test
    public void testIsSupportedDataType() {
        assertThat(new ExcelBinaryBookFactory())
                .is(supportingDataType(Date.class))
                .is(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        assertThat(new ExcelBinaryBookFactory())
                .is(acceptingFile(XLSB_TOP5.getValid().file(temp)))
                .is(acceptingFile(XLSB_TOP5.getMissing().file(temp)))
                .is(acceptingFile(XLSB_TOP5.getValidWithTail().file(temp)))
                .isNot(acceptingFile(XLSB_TOP5.getBadExtension().file(temp)))
                .isNot(acceptingFile(XLSB_TOP5.getInvalidFormat().file(temp)))
                .isNot(acceptingFile(XLSB_TOP5.getEmpty().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        assertThat(new ExcelBinaryBookFactory())
                .is(acceptingPath(XLSB_TOP5.getValid().path(temp)))
                .is(acceptingPath(XLSB_TOP5.getMissing().path(temp)))
                .is(acceptingPath(XLSB_TOP5.getValidWithTail().path(temp)))
                .isNot(acceptingPath(XLSB_TOP5.getBadExtension().path(temp)))
                .isNot(acceptingPath(XLSB_TOP5.getInvalidFormat().path(temp)))
                .isNot(acceptingPath(XLSB_TOP5.getEmpty().path(temp)));
    }

    @Test
    public void testLoadFile(@TempDir Path temp) throws IOException {
        ExcelBinaryBookFactory x = new ExcelBinaryBookFactory();

        try (Book book = x.load(XLSB_TOP5.getValid().path(temp))) {
            PoiSamples.assertTop5Book(book);
        }
        try (Book book = x.load(XLSB_TOP5.getBadExtension().path(temp))) {
            PoiSamples.assertTop5Book(book);
        }
        try (Book book = x.load(XLSB_TOP5.getValidWithTail().path(temp))) {
            PoiSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> x.load(XLSB_TOP5.getInvalidFormat().path(temp)));
        assertThatIOException().isThrownBy(() -> x.load(XLSB_TOP5.getEmpty().path(temp)));
        assertThatIOException().isThrownBy(() -> x.load(XLSB_TOP5.getMissing().path(temp)));
    }

    @Test
    public void testLoadStream() throws IOException {
        ExcelBinaryBookFactory x = new ExcelBinaryBookFactory();

        try (ArrayBook book = XLSB_TOP5.getValid().loadStream(x)) {
            PoiSamples.assertTop5Book(book);
        }
        try (ArrayBook book = XLSB_TOP5.getBadExtension().loadStream(x)) {
            PoiSamples.assertTop5Book(book);
        }
        try (ArrayBook book = XLSB_TOP5.getValidWithTail().loadStream(x)) {
            PoiSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> XLSB_TOP5.getInvalidFormat().loadStream(x));
        assertThatIOException().isThrownBy(() -> XLSB_TOP5.getEmpty().loadStream(x));
    }
}
