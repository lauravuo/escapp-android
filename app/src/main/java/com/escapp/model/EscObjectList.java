package com.escapp.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by laura on 28.10.2014.
 */
public class EscObjectList<E> extends ArrayList<E> {

    private Class type;

    public EscObjectList(int capacity, Class type) {
        super(capacity);
        this.type = type;
    }

    public EscObjectList(Class type) {
        super();
        this.type = type;
    }

    public EscObjectList(Collection<? extends E> collection, Class type) {
        super(collection);
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
