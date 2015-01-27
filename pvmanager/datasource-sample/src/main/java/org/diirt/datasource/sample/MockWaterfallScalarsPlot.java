/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sample;

import java.util.List;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;
import org.diirt.vtype.ValueUtil;
import org.diirt.vtype.VImage;
import org.diirt.datasource.extra.ColorScheme;
import org.diirt.datasource.extra.WaterfallPlot;
import org.diirt.datasource.extra.WaterfallPlotParameters;
import static org.diirt.datasource.vtype.ExpressionLanguage.*;
import static org.diirt.datasource.extra.ExpressionLanguage.*;
import static org.diirt.datasource.extra.WaterfallPlotParameters.*;
import static org.diirt.util.concurrent.Executors.swingEDT;
import static org.diirt.util.time.TimeDuration.*;

/**
 *
 * @author carcassi
 */
public class MockWaterfallScalarsPlot extends javax.swing.JFrame {

    /** Creates new form MockWaterfallPlot */
    public MockWaterfallScalarsPlot() {
        initComponents();
        WaterfallPlotParameters defaults = WaterfallPlotParameters.defaults();
        adaptiveRangeField.setSelected(defaults.isAdaptiveRange());
        scrollDownField.setSelected(defaults.isScrollDown());
        pixelDurationField.setValue(defaults.getPixelDuration().getNanoSec() / 1000000);
        plotView.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (plot != null) {
                    plot.with(height(plotView.getHeight()));
                }
            }
            
        });
    }

    private PVReader<VImage> pv;
    private WaterfallPlot plot;

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lastError = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        adaptiveRangeField = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        pixelDurationField = new javax.swing.JSpinner();
        scrollDownField = new javax.swing.JCheckBox();
        plotView = new org.diirt.datasource.sample.ImagePanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pvNamesField = new javax.swing.JEditorPane();
        goButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("PV Names:");

        lastError.setEditable(false);

        adaptiveRangeField.setText("Adaptive range");
        adaptiveRangeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adaptiveRangeFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("ms per pixel:");

        pixelDurationField.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1)));
        pixelDurationField.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pixelDurationFieldStateChanged(evt);
            }
        });

        scrollDownField.setSelected(true);
        scrollDownField.setText("Latest on top");
        scrollDownField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollDownFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout plotViewLayout = new javax.swing.GroupLayout(plotView);
        plotView.setLayout(plotViewLayout);
        plotViewLayout.setHorizontalGroup(
            plotViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 474, Short.MAX_VALUE)
        );
        plotViewLayout.setVerticalGroup(
            plotViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 279, Short.MAX_VALUE)
        );

        pvNamesField.setText("sim://ramp(0,10,1,0.1)\nsim://ramp(0,10,1,0.2)\nsim://ramp(0,10,1,0.5)\nsim://ramp(0,10,1,1)\n");
        jScrollPane1.setViewportView(pvNamesField);

        goButton.setText("Go");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pixelDurationField, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(plotView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(goButton, 0, 0, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                    .addComponent(lastError, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(adaptiveRangeField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollDownField)
                .addContainerGap(268, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(goButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(pixelDurationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adaptiveRangeField)
                    .addComponent(scrollDownField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lastError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pixelDurationFieldStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pixelDurationFieldStateChanged
        if (plot != null) {
            plot.with(pixelDuration(ofMillis(((Number) pixelDurationField.getValue()).intValue())));
        }
    }//GEN-LAST:event_pixelDurationFieldStateChanged

    private void scrollDownFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollDownFieldActionPerformed
        if (plot != null) {
            plot.with(scrollDown(scrollDownField.isSelected()));
        }
    }//GEN-LAST:event_scrollDownFieldActionPerformed

    private void adaptiveRangeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adaptiveRangeFieldActionPerformed
        if (plot != null) {
            plot.with(adaptiveRange(adaptiveRangeField.isSelected()));
        }
    }//GEN-LAST:event_adaptiveRangeFieldActionPerformed

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        if (pv != null)
            pv.close();
        
        String[] tokens = pvNamesField.getText().split("\n");
        List<String> names = new ArrayList<String>();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                names.add(token.trim());
            }
        }

        plot = waterfallPlotOf(vNumbers(names)).with(
                colorScheme(ColorScheme.singleRangeGradient(Color.BLACK, Color.WHITE, getBackground())),
                backgroundColor(getBackground().getRGB()),
                adaptiveRange(adaptiveRangeField.isSelected()),
                scrollDown(scrollDownField.isSelected()),
                height(plotView.getHeight()),
                pixelDuration(ofMillis(((Number) pixelDurationField.getValue()).intValue())));
        pv = PVManager.read(plot)
                .readListener(new PVReaderListener<VImage>() {
                    @Override
                    public void pvChanged(PVReaderEvent<VImage> event) {
                        setLastError(pv.lastException());
                        if (pv.getValue() != null) {
                            BufferedImage image = ValueUtil.toImage(pv.getValue());
                            plotView.setImage(image);
                        }
                    }
                })
                .notifyOn(swingEDT())
                .maxRate(ofHertz(50));
    }//GEN-LAST:event_goButtonActionPerformed


    private void setLastError(Exception ex) {
        if (ex != null) {
            lastError.setText(ex.getMessage());
            Logger.getLogger(MockWaterfallScalarsPlot.class.getName()).log(Level.WARNING, "Error", ex);
        } else
            lastError.setText("");
    }


    final BufferedImage finalBuffer = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        SetupUtil.defaultCASetupForSwing();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MockWaterfallScalarsPlot().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox adaptiveRangeField;
    private javax.swing.JButton goButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lastError;
    private javax.swing.JSpinner pixelDurationField;
    private org.diirt.datasource.sample.ImagePanel plotView;
    private javax.swing.JEditorPane pvNamesField;
    private javax.swing.JCheckBox scrollDownField;
    // End of variables declaration//GEN-END:variables

}
