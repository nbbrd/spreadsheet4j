/*
 * Copyright 2017 National Bank of Belgium
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

import ioutil.IO;
import ioutil.Sax;
import ioutil.Zip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import spreadsheet.xlsx.XlsxPackage;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class ZipPackage implements XlsxPackage {

    @lombok.NonNull
    private final IO.ResourceLoader<String> resource;
    private Map<String, String> relationships = null;

    @Override
    public InputStream getWorkbook() throws IOException {
        return resource.load(WORKBOOK_ENTRY_NAME);
    }

    @Override
    public InputStream getSharedStrings() throws IOException {
        return resource.load(SHARED_STRINGS_ENTRY_NAME);
    }

    @Override
    public InputStream getStyles() throws IOException {
        return resource.load(STYLES_ENTRY_NAME);
    }

    @Override
    public InputStream getSheet(String relationId) throws IOException {
        return resource.load("xl/" + getRelationShipPath(relationId));
    }

    @Override
    public void close() throws IOException {
        resource.close();
    }

    private String getRelationShipPath(String relationId) throws IOException {
        if (relationships == null) {
            relationships = parseRelationships(() -> resource.load(RELATIONSHIPS_ENTRY_NAME));
        }
        String result = relationships.get(relationId);
        if (result == null) {
            throw new IOException("Cannot find target for '" + relationId + "'");
        }
        return result;
    }

    private static final String RELATIONSHIPS_ENTRY_NAME = "xl/_rels/workbook.xml.rels";
    private static final String WORKBOOK_ENTRY_NAME = "xl/workbook.xml";
    private static final String SHARED_STRINGS_ENTRY_NAME = "xl/sharedStrings.xml";
    private static final String STYLES_ENTRY_NAME = "xl/styles.xml";

    private static Map<String, String> parseRelationships(IO.Supplier<? extends InputStream> byteSource) throws IOException {
        Map<String, String> result = new HashMap<>();
        return Sax.Parser.<Map<String, String>>builder()
                .factory(() -> SaxEntryParser.disableNamespaces(Sax.createReader()))
                .contentHandler(new RelationshipsSaxEventHandler(result::put))
                .after(IO.Supplier.of(result))
                .build()
                .parseStream(byteSource);
    }

    @lombok.AllArgsConstructor
    private static final class RelationshipsSaxEventHandler extends DefaultHandler {

        private static final String RELATIONSHIP_TAG = "Relationship";
        private static final String ID_ATTRIBUTE = "Id";
        private static final String TARGET_ATTRIBUTE = "Target";

        private final BiConsumer<String, String> visitor;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case RELATIONSHIP_TAG:
                    visitor.accept(attributes.getValue(ID_ATTRIBUTE), attributes.getValue(TARGET_ATTRIBUTE));
                    break;
            }
        }
    }

    static boolean isUsefulEntryName(String name) {
        switch (name) {
            case RELATIONSHIPS_ENTRY_NAME:
            case WORKBOOK_ENTRY_NAME:
            case SHARED_STRINGS_ENTRY_NAME:
            case STYLES_ENTRY_NAME:
                return true;
            default:
                return name.startsWith("xl/worksheets/") && !name.endsWith(".rels");
        }
    }

    public static final XlsxPackage.Factory FACTORY = ZipPackageFactory.INSTANCE;

    private enum ZipPackageFactory implements XlsxPackage.Factory {

        INSTANCE;

        @Override
        public XlsxPackage open(InputStream stream) throws IOException {
            return open(() -> Zip.loaderCopyOf(stream, ZipPackageFactory::isUsefulEntry));
        }

        @Override
        public XlsxPackage open(Path path) throws IOException {
            Optional<File> file = IO.getFile(path);
            return file.isPresent()
                    ? open(file.get())
                    : open(Files.newInputStream(path));
        }

        @Override
        public XlsxPackage open(File file) throws IOException {
            try {
                return open(() -> Zip.loaderOf(file));
            } catch (ZipException ex) {
                if (!ex.getMessage().contains(file.getPath())) {
                    ZipException ex2 = new ZipException(ex.getMessage() + ": " + file.getPath());
                    ex2.addSuppressed(ex);
                    throw ex2;
                }
                throw ex;
            }
        }

        private XlsxPackage open(IO.Supplier<IO.ResourceLoader<String>> source) throws IOException {
            return new ZipPackage(source.getWithIO());
        }

        private static boolean isUsefulEntry(ZipEntry entry) {
            return ZipPackage.isUsefulEntryName(entry.getName());
        }
    }
}
