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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import spreadsheet.xlsx.XlsxReader;
import spreadsheet.xlsx.internal.DefaultSheetBuilder;
import spreadsheet.xlsx.internal.DisruptorSheetBuilder;
import spreadsheet.xlsx.internal.MultiSheetBuilder;

/**
 *
 * @author Philippe Charles
 */
@State(Scope.Benchmark)
public class SheetBuilderBenchmark {

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(SheetBuilderBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(3))
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(3))
                .forks(1)
                .warmupForks(1)
                .build();
        new Runner(options).run();
    }

    private byte[] top5;
    private XlsxReader single;
    private XlsxReader multi;
    private XlsxReader disruptor;

    @Setup
    public void setup() throws IOException {
        top5 = Sample.TOP5;
        single = new XlsxReader().withSheetBuilder(DefaultSheetBuilder::of);
        multi = new XlsxReader().withSheetBuilder(MultiSheetBuilder::of);
        disruptor = new XlsxReader().withSheetBuilder(DisruptorSheetBuilder::of);
    }

    @Benchmark
    public ArrayBook single() throws IOException {
        try (Book book = single.read(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    @Benchmark
    public ArrayBook multi() throws IOException {
        try (Book book = multi.read(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }

    @Benchmark
    public ArrayBook disruptor() throws IOException {
        try (Book book = disruptor.read(new ByteArrayInputStream(top5))) {
            return ArrayBook.copyOf(book);
        }
    }
}
