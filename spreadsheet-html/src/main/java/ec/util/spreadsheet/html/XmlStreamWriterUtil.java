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
package ec.util.spreadsheet.html;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
final class XmlStreamWriterUtil {

    static XmlStreamWriterUtil of(XConsumer<? super XMLStreamWriter> consumer) {
        return new XmlStreamWriterUtil(consumer);
    }

    private final XConsumer<? super XMLStreamWriter> consumer;
    private Supplier<? extends XMLOutputFactory> factory;
    private Function<Exception, XMLStreamException> errorHandler;

    XmlStreamWriterUtil(XConsumer<? super XMLStreamWriter> consumer) {
        this.consumer = consumer;
        this.factory = XMLOutputFactory::newInstance;
        this.errorHandler = XmlStreamWriterUtil::asXMLStreamException;
    }

    XmlStreamWriterUtil factory(Supplier<? extends XMLOutputFactory> factory) {
        Objects.requireNonNull(factory);
        this.factory = factory;
        return this;
    }

    XmlStreamWriterUtil factory(XMLOutputFactory factory) {
        Objects.requireNonNull(factory);
        this.factory = () -> factory;
        return this;
    }

    XmlStreamWriterUtil errorHandler(Function<Exception, XMLStreamException> errorHandler) {
        Objects.requireNonNull(errorHandler);
        this.errorHandler = errorHandler;
        return this;
    }

    void writeTo(OutputStream stream, Charset charset) throws XMLStreamException {
        writeAndClose(factory.get().createXMLStreamWriter(stream, charset.name()));
    }

    void writeTo(Writer writer) throws XMLStreamException {
        writeAndClose(factory.get().createXMLStreamWriter(writer));
    }

    void writeTo(Path file, Charset cs, OpenOption... options) throws XMLStreamException, IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, cs, options)) {
            writeTo(writer);
        }
    }

    String writeToString() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        writeTo(writer);
        return writer.toString();
    }

    private void writeAndClose(XMLStreamWriter writer) throws XMLStreamException {
        try (AutoCloseable closeable = writer::close) {
            consumer.accept(writer);
        } catch (Exception ex) {
            throw errorHandler.apply(ex);
        }
    }

    private static XMLStreamException asXMLStreamException(Exception o) {
        return o instanceof XMLStreamException ? (XMLStreamException) o : new XMLStreamException(o);
    }

    @FunctionalInterface
    interface XConsumer<T> {

        void accept(T t) throws Exception;
    }
}
