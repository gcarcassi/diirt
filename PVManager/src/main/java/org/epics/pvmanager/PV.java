/*
 * Copyright 2008-2011 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

/**
 * A PV that can be both read and written. In general, the read payload will be
 * different from the write payload.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public class PV<R, W> implements PVReader<R>, PVWriter<W> {
    
    // This class is a wrapper around a reader and a write. It has no logic by
    // itself, and just forwards the messages to the appropriate object.
    
    private final PVReader<R> reader;
    private final PVWriter<W> writer;

    PV(PVReader<R> reader, PVWriter<W> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void addPVValueWriteListener(PVValueWriteListener listener) {
        writer.addPVValueWriteListener(listener);
    }

    @Override
    public void removePVValueChangeListener(PVValueWriteListener listener) {
        writer.removePVValueChangeListener(listener);
    }

    @Override
    public void write(W newValue) {
        writer.write(newValue);
    }

    @Override
    public Exception lastWriteException() {
        return writer.lastWriteException();
    }

    @Override
    public void addPVValueChangeListener(PVValueChangeListener listener) {
        reader.addPVValueChangeListener(listener);
    }

    @Override
    public void addPVValueChangeListener(Class<?> clazz, PVValueChangeListener listener) {
        reader.addPVValueChangeListener(clazz, listener);
    }

    @Override
    public void removePVValueChangeListener(PVValueChangeListener listener) {
        reader.removePVValueChangeListener(listener);
    }

    @Override
    public String getName() {
        return reader.getName();
    }

    @Override
    public R getValue() {
        return reader.getValue();
    }

    @Override
    public void close() {
        reader.close();
        writer.close();
    }

    @Override
    public boolean isClosed() {
        return reader.isClosed();
    }

    @Override
    public Exception lastException() {
        return reader.lastException();
    }
    
}
