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
import java.io.IOException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 *
 * @author Philippe Charles
 */
@State(Scope.Benchmark)
public class ExcelBookFactoryBenchmark {

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(ExcelBookFactoryBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .warmupForks(1)
                .build();
        new Runner(options).run();
    }

    private byte[] top5;
    private ExcelBookFactory fast;
    private ExcelBookFactory normal;

    @Setup
    public void setup() throws IOException {
        top5 = Sample.TOP5;

        fast = new ExcelBookFactory();
        fast.setFast(true);

        normal = new ExcelBookFactory();
        normal.setFast(false);
    }

    @Benchmark
    public ArrayBook fullFast() throws IOException {
        try (Book book = fast.load(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    @Benchmark
    public ArrayBook fullNormal() throws IOException {
        try (Book book = normal.load(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    @Benchmark
    public String[] partialFast() throws IOException {
        try (Book book = fast.load(new ByteArrayInputStream(top5))) {
            return getSheetNames(book);
        }
    }

    @Benchmark
    public String[] partialNormal() throws IOException {
        try (Book book = normal.load(new ByteArrayInputStream(top5))) {
            return getSheetNames(book);
        }
    }

    private String[] getSheetNames(Book book) throws IOException {
        String[] result = new String[book.getSheetCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = book.getSheetName(i);
        }
        return result;
    }
}
