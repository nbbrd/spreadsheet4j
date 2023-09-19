package spreadsheet.fastexcel;

import ec.util.spreadsheet.tck.Assertions;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spreadsheet.xlsx.XlsxBookFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

import static _test.FastExcelSamples.XLSX_TOP5;
import static ec.util.spreadsheet.tck.Conditions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FastExcelBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        Assertions.assertThat(new FastExcelBookFactory())
                .isCompliant(XLSX_TOP5, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        assertThat(new FastExcelBookFactory())
                .isNot(ableToLoadContent())
                .is(ableToStoreContent());

        BookFactoryAssert.assertReadWrite(
                new XlsxBookFactory(),
                new FastExcelBookFactory(),
                XLSX_TOP5.getValid().file(temp),
                Files.createTempFile(temp, "output", ".xlsx").toFile()
        );
    }

    @Test
    public void testIsSupportedDataType() {
        assertThat(new FastExcelBookFactory())
                .is(supportingDataType(Date.class))
                .is(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        assertThat(new FastExcelBookFactory())
                .is(acceptingFile(XLSX_TOP5.getValid().file(temp)))
                .is(acceptingFile(XLSX_TOP5.getMissing().file(temp)))
                .is(acceptingFile(XLSX_TOP5.getValidWithTail().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getBadExtension().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getInvalidFormat().file(temp)))
                .isNot(acceptingFile(XLSX_TOP5.getEmpty().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        assertThat(new FastExcelBookFactory())
                .is(acceptingPath(XLSX_TOP5.getValid().path(temp)))
                .is(acceptingPath(XLSX_TOP5.getMissing().path(temp)))
                .is(acceptingPath(XLSX_TOP5.getValidWithTail().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getBadExtension().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getInvalidFormat().path(temp)))
                .isNot(acceptingPath(XLSX_TOP5.getEmpty().path(temp)));
    }
}
