package com.protopie.test;

import com.protopie.test.vm.VendingMachine;

public enum ProtopieTest {
    INSTANCE;

    private void init() {
        new VendingMachine().start();
    }

    public static void main(String[] args) {
        try {
            ProtopieTest.INSTANCE.init();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
