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

import ioutil.IO;
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import spreadsheet.xlsx.XlsxPackage;
import spreadsheet.xlsx.internal.util.SaxUtil;

/**
 *
 * @author Philippe Charles
 */
public final class DefaultXlsxPackageFactory implements XlsxPackage.Factory {

    public static final DefaultXlsxPackageFactory INSTANCE = new DefaultXlsxPackageFactory();

    private DefaultXlsxPackageFactory() {
    }

    @Override
    public XlsxPackage open(InputStream stream) throws IOException {
        return createPackageOrClose(Zip.Loader.copyOf(stream, o -> isValidEntryName(o.getName())));
    }

    @Override
    public XlsxPackage open(Path file) throws IOException {
        Optional<File> target = IO.getFile(file);
        return target.isPresent() ? open(target.get()) : open(Files.newInputStream(file));
    }

    private XlsxPackage open(File file) throws IOException {
        return createPackageOrClose(Zip.Loader.of(file));
    }

    private XlsxPackage createPackageOrClose(Zip.Loader resource) throws IOException {
        try {
            return CustomPackage.create(resource);
        } catch (IOException ex) {
            throw IO.ensureClosed(ex, resource);
        }
    }

    static final class CustomPackage implements XlsxPackage {

        static CustomPackage create(Zip.Loader resource) throws IOException {
            return new CustomPackage(resource, parseRelationships(() -> resource.load(RELATIONSHIPS_ENTRY_NAME)));
        }

        private final Zip.Loader resource;
        private final Map<String, String> relationships;

        private CustomPackage(Zip.Loader resource, Map<String, String> relationships) {
            this.resource = resource;
            this.relationships = relationships;
        }

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
            String target = relationships.get(relationId);
            if (target == null) {
                throw new IOException("Cannot find target for '" + relationId + "'");
            }
            return resource.load("xl/" + target);
        }

        @Override
        public void close() throws IOException {
            resource.close();
        }
    }

    private static boolean isValidEntryName(String name) {
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

    private static final String RELATIONSHIPS_ENTRY_NAME = "xl/_rels/workbook.xml.rels";
    private static final String WORKBOOK_ENTRY_NAME = "xl/workbook.xml";
    private static final String SHARED_STRINGS_ENTRY_NAME = "xl/sharedStrings.xml";
    private static final String STYLES_ENTRY_NAME = "xl/styles.xml";

    private static Map<String, String> parseRelationships(IO.Supplier<? extends InputStream> byteSource) throws IOException {
        Map<String, String> result = new HashMap<>();
        try (InputStream stream = byteSource.getWithIO()) {
            new RelationshipsSaxEventHandler(result::put).runWith(XMLReaderFactory.createXMLReader(), stream);
        } catch (SAXException ex) {
            throw new IOException("While parsing relationships", ex);
        }
        return result;
    }

    private static final class RelationshipsSaxEventHandler extends DefaultHandler implements SaxUtil.ContentRunner {

        private static final String RELATIONSHIP_TAG = "Relationship";
        private static final String ID_ATTRIBUTE = "Id";
        private static final String TARGET_ATTRIBUTE = "Target";

        private final BiConsumer<String, String> visitor;

        private RelationshipsSaxEventHandler(BiConsumer<String, String> visitor) {
            this.visitor = visitor;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (localName) {
                case RELATIONSHIP_TAG:
                    visitor.accept(attributes.getValue(ID_ATTRIBUTE), attributes.getValue(TARGET_ATTRIBUTE));
                    break;
            }
        }
    }
}
