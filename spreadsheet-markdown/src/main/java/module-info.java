module nbbrd.spreadsheet.markdown {

    requires static org.jspecify;
    requires static nbbrd.service;
    requires static lombok;

    requires nbbrd.spreadsheet.api;
    requires flexmark;
    requires flexmark.ext.tables;
    requires flexmark.util.ast;
    requires flexmark.util.collection;
    requires flexmark.util.data;
    requires flexmark.util.sequence;

    provides ec.util.spreadsheet.Book.Factory with
            ec.util.spreadsheet.markdown.MarkdownBookFactory;
}

