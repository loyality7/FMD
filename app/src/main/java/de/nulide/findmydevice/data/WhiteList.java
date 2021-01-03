package de.nulide.findmydevice.data;

import android.content.Context;

import java.util.LinkedList;

import de.nulide.findmydevice.data.io.IO;

public class WhiteList extends LinkedList<Contact> {

    public WhiteList() {

    }

    @Override
    public boolean add(Contact c) {
        super.add(c);
        IO.write(this, IO.whiteListFileName);
        return true;
    }
}