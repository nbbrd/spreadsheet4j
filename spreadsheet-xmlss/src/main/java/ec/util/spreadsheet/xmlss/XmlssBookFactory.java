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
package ec.util.spreadsheet.xmlss;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.FileHelper;
import ioutil.Stax;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public class XmlssBookFactory extends Book.Factory {

    private final XMLOutputFactory xof;

    public XmlssBookFactory() {
        this.xof = XMLOutputFactory.newInstance();
    }

    @Override
    public String getName() {
        return "XML Spreadsheet (XMLSS)";
    }

    @Override
    public boolean accept(File file) {
        return FileHelper.accept(file, this::accept);
    }

    @Override
    public boolean accept(Path file) throws IOException {
        return FileHelper.hasExtension(file, ".xml")
                && (Files.exists(file) ? hasValidHeader(file) : true);
    }

    @Override
    public Book load(File file) throws IOException {
        return XmlssBookReader.parseFile(file);
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        return XmlssBookReader.parseStream(stream);
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        newWriter().write(stream, book);
    }

    private XmlssBookWriter newWriter() {
        return new XmlssBookWriter(xof, StandardCharsets.UTF_8);
    }

    private static boolean hasValidHeader(Path file) {
        try {
            return Stax.StreamParser.valueOf(XmlssBookFactory::hasValidHeader).parsePath(file);
        } catch (IOException ex) {
            return false;
        }
    }

    private static boolean hasValidHeader(XMLStreamReader xml) throws XMLStreamException {
        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    return XML_HEADER_TARGET.equals(xml.getPITarget())
                            && XML_HEADER_DATA.equals(xml.getPIData());
                case XMLStreamConstants.START_ELEMENT:
                    return false;
            }
        }
        return false;
    }

    static final String XML_HEADER_TARGET = "mso-application";
    static final String XML_HEADER_DATA = "progid=\"Excel.Sheet\"";
}
