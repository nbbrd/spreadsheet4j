/*
 * Copyright 2022 National Bank of Belgium
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

module nbbrd.spreadsheet.xl {

    requires static org.checkerframework.checker.qual;
    requires static nbbrd.service;
    requires static lombok;

    requires nbbrd.spreadsheet.api;
    requires nbbrd.spreadsheet.util;
    requires com.lmax.disruptor;
    requires java.logging;
    
    provides ec.util.spreadsheet.Book.Factory with
            spreadsheet.xlsx.XlsxBookFactory;

    exports spreadsheet.xlsx to nbbrd.spreadsheet.poi;
    exports spreadsheet.xlsx.internal to nbbrd.spreadsheet.poi;
}
