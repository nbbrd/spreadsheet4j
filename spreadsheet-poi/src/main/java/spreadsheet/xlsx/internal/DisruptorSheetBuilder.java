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

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import spreadsheet.xlsx.XlsxDataType;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 */
public final class DisruptorSheetBuilder implements XlsxSheetBuilder {

    public static DisruptorSheetBuilder of(XlsxDateSystem dateSystem, IntFunction<String> sharedStrings, IntPredicate dateFormats) {
        return new DisruptorSheetBuilder(DefaultSheetBuilder.of(dateSystem, sharedStrings, dateFormats));
    }

    private final DefaultSheetBuilder delegate;
    private final Disruptor<CustomEvent> disruptor;
    private final RingBuffer<CustomEvent> ringBuffer;

    private DisruptorSheetBuilder(DefaultSheetBuilder delegate) {
        this.delegate = delegate;
        this.disruptor = new Disruptor<>(CustomEvent::new, 1024, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        disruptor.handleEventsWith(this::handleEvent);
        this.ringBuffer = disruptor.start();

    }

    private void handleEvent(CustomEvent event, long sequence, boolean endOfBatch) {
        delegate.put(event.ref, event.value, XlsxValueFactory.getDataTypeByOrdinal(event.dataType), event.styleIndex);
    }

    @Override
    public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
        waitForCompletion();
        delegate.reset(sheetName, sheetBounds);
        return this;
    }

    @Override
    public XlsxSheetBuilder put(String ref, CharSequence value, XlsxDataType dataType, int styleIndex) {
        long sequence = ringBuffer.next();
        try {
            CustomEvent event = ringBuffer.get(sequence);
            event.ref = ref;
            event.value = value;
            event.dataType = dataType.ordinal();
            event.styleIndex = styleIndex;
        } finally {
            ringBuffer.publish(sequence);
        }
        return this;
    }

    @Override
    public Sheet build() {
        waitForCompletion();
        return delegate.build();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        disruptor.halt();
    }

    private void waitForCompletion() {
        while (ringBuffer.getCursor() - ringBuffer.getMinimumGatingSequence() != 0) {
        }
    }

    private static final class CustomEvent {

        private String ref;
        private CharSequence value;
        private int dataType;
        private int styleIndex;
    }
}
