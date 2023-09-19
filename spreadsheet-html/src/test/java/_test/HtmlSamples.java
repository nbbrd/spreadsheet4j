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
package _test;

import ec.util.spreadsheet.tck.Sample;
import ec.util.spreadsheet.tck.SampleSet;

/**
 * @author Philippe Charles
 */
public final class HtmlSamples {

    private HtmlSamples() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final String HTML_CONTENT = "<table><tr><td>A1</td><td rowspan=2>B1</td></tr> <tr><td>A2</td></tr> <tr><td>A3</td><td>B3</td></tr>";

    public static final SampleSet HTML = SampleSet
            .builder()
            .valid(Sample.of("valid.htm", HTML_CONTENT))
            .validWithTail(null)
            .invalidContent(null)
            .invalidFormat(null)
            .empty(Sample.of("empty.htm", ""))
            .missing(Sample.of("missing.htm"))
            .badExtension(Sample.of("badExtension.zip", HTML_CONTENT))
            .build();
}
