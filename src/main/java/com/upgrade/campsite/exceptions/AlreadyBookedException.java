package com.upgrade.campsite.exceptions;

public class AlreadyBookedException extends RuntimeException {

    public AlreadyBookedException(String s) {
        super(s);
    }
}
