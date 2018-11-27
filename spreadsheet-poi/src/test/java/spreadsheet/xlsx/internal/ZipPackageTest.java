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

import ec.util.spreadsheet.SheetAssert;
import ioutil.Sax;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.atIndex;
import org.junit.Test;
import spreadsheet.xlsx.XlsxPackage;
import spreadsheet.xlsx.XlsxSheetBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import spreadsheet.xlsx.XlsxEntryParser;

/**
 *
 * @author Philippe Charles
 */
public class ZipPackageTest {

    private static final URL TOP5 = ZipPackageTest.class.getResource("/Top5Browsers.xlsx");

    @Test
    public void testOpenInputStream() throws IOException {
        try (InputStream stream = TOP5.openStream()) {
            try (XlsxPackage pkg = ZipPackage.FACTORY.open(stream)) {
                assertPackageContent(pkg);
            }
        }
    }

    @Test
    public void testOpenPath() throws IOException, URISyntaxException {
        Path path = Paths.get(TOP5.toURI());
        try (XlsxPackage pkg = ZipPackage.FACTORY.open(path)) {
            assertPackageContent(pkg);
        }
    }

    private void assertPackageContent(XlsxPackage pkg) throws IOException {
        XlsxEntryParser parser = new SaxEntryParser(Sax.createReader());

        assertThat(XlsxBook.parseWorkbook(pkg::getWorkbook, parser))
                .satisfies(o -> {
                    assertThat(o.getSheets()).contains(new XlsxBook.SheetMeta("rId1", "Top 5 Browsers - Monthly"));
                    assertThat(o.isDate1904()).isFalse();
                });

        List<String> sharedStrings = XlsxBook.parseSharedStrings(pkg::getSharedStrings, parser);
        assertThat(sharedStrings).contains("IE", atIndex(0)).contains("helloworld", atIndex(8));

        boolean[] styles = XlsxBook.parseStyles(DefaultNumberingFormat.INSTANCE, pkg::getStyles, parser);
        assertThat(styles).containsExactly(false, true);

        XlsxSheetBuilder b = MultiSheetBuilder.of(DefaultDateSystem.X1900, sharedStrings, styles);
        SheetAssert.assertThat(XlsxBook.parseSheet("hello", b, () -> pkg.getSheet("rId1"), parser))
                .hasRowCount(42)
                .hasColumnCount(7);
    }
}
