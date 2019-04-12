package com.github.gervaisb.demo;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class Identity implements Serializable  {
    public final String value;

    protected Identity() {
        this(UUID.randomUUID().toString());
    }

    protected Identity(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return String.format("%1$s(%2$s)", getClass().getSimpleName(), value);
    }
}
