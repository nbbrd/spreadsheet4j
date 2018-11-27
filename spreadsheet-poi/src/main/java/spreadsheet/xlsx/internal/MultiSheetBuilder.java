/*
 * Copyright 2018 National Bank of Belgium
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

import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.XlsxDataType;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 */
public final class MultiSheetBuilder implements XlsxSheetBuilder {

    public static MultiSheetBuilder of(XlsxDateSystem dateSystem, List<String> sharedStrings, boolean[] dateFormats) {
        return new MultiSheetBuilder(DefaultSheetBuilder.of(dateSystem, sharedStrings, dateFormats));
    }

    private static final int FIRST_BATCH_SIZE = 10;
    private static final int NEXT_BATCH_SIZE = 1000;
    private static final int QUEUE_MAX_SIZE = 10;

    private final DefaultSheetBuilder delegate;
    private final ExecutorService executor;
    private final CustomQueue queue;
    private Batch nextBatch;

    private MultiSheetBuilder(DefaultSheetBuilder delegate) {
        this.delegate = delegate;
        this.executor = Executors.newSingleThreadExecutor();
        this.queue = new CustomQueue(QUEUE_MAX_SIZE);
        this.nextBatch = new Batch(FIRST_BATCH_SIZE);
    }

    @Override
    public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
        queue.waitForCompletion();
        delegate.reset(sheetName, sheetBounds);
        return this;
    }

    @Override
    public XlsxSheetBuilder put(String ref, CharSequence value, XlsxDataType dataType, int styleIndex) {
        if (nextBatch.isFull()) {
            if (queue.isFull()) {
                queue.waitForCompletion();
            }
            queue.add(executor.submit(nextBatch.asTask(delegate)));
            nextBatch = new Batch(NEXT_BATCH_SIZE);
        }
        nextBatch.put(ref, value, dataType, styleIndex);
        return this;
    }

    @Override
    public Sheet build() {
        queue.waitForCompletion();
        if (nextBatch.getSize() > 0) {
            nextBatch.process(delegate);
            nextBatch = new Batch(FIRST_BATCH_SIZE);
        }
        return delegate.build();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new IOException("While closing executor", ex);
        }
    }

    private static final class CustomQueue {

        private final int maxQueueSize;
        private final List<Future<?>> queue;

        CustomQueue(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            this.queue = new ArrayList<>(maxQueueSize);
        }

        boolean isFull() {
            return queue.size() >= maxQueueSize;
        }

        void waitForCompletion() {
            for (Future<?> o : queue) {
                try {
                    o.get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
            queue.clear();
        }

        private void add(Future<?> submit) {
            queue.add(submit);
        }
    }

    private static final class Batch {

        private final Object[][] values;
        private final int[] dataTypes;
        private final int[] styleIndexes;
        private int size;

        Batch(int maxSize) {
            this.values = new Object[maxSize][2];
            this.dataTypes = new int[maxSize];
            this.styleIndexes = new int[maxSize];
            this.size = 0;
        }

        void put(@Nonnull String ref, @Nonnull CharSequence value, @Nonnull XlsxDataType dataType, int styleIndex) {
            Object[] row = values[size];
            row[0] = ref;
            row[1] = value;
            dataTypes[size] = dataType.ordinal();
            styleIndexes[size] = styleIndex;
            size++;
        }

        int getSize() {
            return size;
        }

        boolean isFull() {
            return values.length == size;
        }

        void process(DefaultSheetBuilder delegate) {
            for (int i = 0; i < size; i++) {
                delegate.put((String) values[i][0], (CharSequence) values[i][1], XlsxValueFactory.getDataTypeByOrdinal(dataTypes[i]), styleIndexes[i]);
            }
        }

        Runnable asTask(DefaultSheetBuilder delegate) {
            return () -> process(delegate);
        }
    }
}
