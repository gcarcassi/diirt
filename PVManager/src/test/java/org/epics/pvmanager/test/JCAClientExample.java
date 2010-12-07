/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.test;

import java.util.Arrays;
import org.epics.pvmanager.data.VEnum;
import org.epics.pvmanager.data.VString;
import org.epics.pvmanager.data.VDouble;
import org.epics.pvmanager.data.VInt;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVValueChangeListener;
import org.epics.pvmanager.ThreadSwitch;
import org.epics.pvmanager.data.VByteArray;
import org.epics.pvmanager.data.VDoubleArray;
import org.epics.pvmanager.data.VFloatArray;
import org.epics.pvmanager.data.VIntArray;
import org.epics.pvmanager.data.VShortArray;
import org.epics.pvmanager.data.VStringArray;
import org.epics.pvmanager.jca.JCASupport;
import static org.epics.pvmanager.data.ExpressionLanguage.*;

/**
 *
 * @author carcassi
 */
public class JCAClientExample {

    private static final String doublePV = "SR:C01-BI:G02A<BPM:L1>Pos-X";
    private static final String enumPV = doublePV + ".SCAN";
    private static final String longPV = doublePV + ".RVAL";
    private static final String stringPV = doublePV + ".EGU";
    private static final String doubleArrayPV = "SR:C00-Glb:G00<BETA:00>RB-X";

    public static void main(String[] args) throws Exception {
        PVManager.setDefaultDataSource(JCASupport.jca());
        PVManager.setDefaultThread(ThreadSwitch.onTimerThread());

        testNativeTypeSupport();
        testVDoubleSupport();
        testVIntSupport();
        testVStringSupport();
        testVEnumSupport();
//        testVFloatArraySupport();
//        testVDoubleArraySupport();
//        testVByteArraySupport();
//        testVShortArraySupport();
//        testVIntArraySupport();
//        testVStringArraySupport();

    }

    private static void logException(Exception ex) {
        if (ex != null)
            ex.printStackTrace();
    }

    private static void testNativeTypeSupport() throws Exception {
        final PV<Object> pv = PVManager.read(nativeType(doublePV)).atHz(10);
        logException(pv.lastException());
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                logException(pv.lastException());
                VDouble value = (VDouble) pv.getValue();
                System.out.println(value.getValue() + " " + value.getTimeStamp().asDate() + " " + value.getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVFloatArraySupport() throws Exception {
        final PV<VFloatArray> pv = PVManager.read(vFloatArray(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVDoubleArraySupport() throws Exception {
        final PV<VDoubleArray> pv = PVManager.read(vDoubleArray(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVByteArraySupport() throws Exception {
        final PV<VByteArray> pv = PVManager.read(vByteArray(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVShortArraySupport() throws Exception {
        final PV<VShortArray> pv = PVManager.read(vShortArray(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVIntArraySupport() throws Exception {
        final PV<VIntArray> pv = PVManager.read(vIntArray(doublePV)).atHz(10);
        logException(pv.lastException());
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVStringArraySupport() throws Exception {
        final PV<VStringArray> pv = PVManager.read(vStringArray(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(Arrays.toString(pv.getValue().getArray()) + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVDoubleSupport() throws Exception {
        final PV<VDouble> pv = PVManager.read(vDouble(doublePV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                System.out.println(pv.getValue().getValue() + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVIntSupport() throws Exception {
            final PV<VInt> pv = PVManager.read(vInt(longPV)).atHz(10);
            pv.addPVValueChangeListener(new PVValueChangeListener() {

                @Override
                public void pvValueChanged() {
                    System.out.println(pv.getValue().getValue() + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
                }
            });

            Thread.sleep(10000);

            pv.close();
    }

    private static void testVStringSupport() throws Exception {
        final PV<VString> pv = PVManager.read(vString(stringPV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                logException(pv.lastException());
                if (pv.getValue() != null) {
                    System.out.println(pv.getValue().getValue() + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
                }
            }
        });

        Thread.sleep(10000);

        pv.close();
    }

    private static void testVEnumSupport() throws Exception {
        final PV<VEnum> pv = PVManager.read(vEnum(enumPV)).atHz(10);
        pv.addPVValueChangeListener(new PVValueChangeListener() {

            @Override
            public void pvValueChanged() {
                logException(pv.lastException());
                if (pv.getValue() != null) {
                    System.out.println(pv.getValue().getValue() + " " + pv.getValue().getTimeStamp().asDate() + " " + pv.getValue().getAlarmSeverity());
                }
            }
        });

        Thread.sleep(10000);

        pv.close();
    }
}
