/*
 * Copyright 2018 National Bank of Belgium
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
package _benchmark;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.poi.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 *
 * @author Philippe Charles
 */
@State(Scope.Benchmark)
public class ExcelBookFactoryBenchmark {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    private byte[] top5;
    private ExcelBookFactory fast;
    private ExcelBookFactory normal;

    @Setup
    public void setup() throws IOException {
        try (InputStream stream = ExcelBookFactoryBenchmark.class.getResourceAsStream("/Top5Browsers.xlsx")) {
            top5 = toByteArray(stream);
        }

        fast = new ExcelBookFactory();
        fast.setFast(true);

        normal = new ExcelBookFactory();
        normal.setFast(false);
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    public ArrayBook testFast() throws IOException {
        try (Book book = fast.load(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    public ArrayBook testNormal() throws IOException {
        try (Book book = normal.load(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    private static byte[] toByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
