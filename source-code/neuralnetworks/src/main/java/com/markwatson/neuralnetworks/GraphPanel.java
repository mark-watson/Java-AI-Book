package com.markwatson.neuralnetworks;


/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */

import java.awt.*;

public class GraphPanel extends java.awt.Canvas {

    public GraphPanel(float[] data1, float[] data2) {
        super();
        this.data1 = data1;
        this.data2 = data2;
    }

    float[] data1;
    float[] data2;

    public void paint(Graphics g) {
        if (data1 == null || data2 == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int width = this.getWidth();
        int height = this.getHeight();
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < data1.length; i++) { // assume length of data1 and data2 are the same
            if (min > data1[i]) min = data1[i];
            if (max < data1[i]) max = data1[i];
            if (min > data2[i]) min = data2[i];
            if (max < data2[i]) max = data2[i];
        }
        g2.setColor(Color.red);
        for (int i = 0; i < data1.length - 1; i++) {
            float y1 = height - 5 - 0.95f * height * ((data1[i] - min) / (max - min));
            float y2 = height - 5 - 0.95f * height * ((data1[i + 1] - min) / (max - min));
            g2.drawLine(i + 20, (int) y1, i + 21, (int) y2);
            y1 = height - 5 - 0.95f * height * ((data2[i] - min) / (max - min));
            y2 = height - 5 - 0.95f * height * ((data2[i + 1] - min) / (max - min));
            g2.drawLine(i + 20, (int) y1, i + 21, (int) y2);
        }
        float yzero = height - 5 - 0.95f * height * ((0.0f - min) / (max - min));
        g2.setColor(Color.black);
        g2.drawLine(20, (int) yzero, data2.length + 19, (int) yzero);
        g2.drawLine(width / 2, height / 2 - 118, width / 2, height / 2 + 118);
        g2.drawString("Sigmoid", width / 2 + 100, height / 4 - 10);
        g2.drawString("SigmoidP", width / 2 +60, 3 * height / 4 + 10);
        g2.drawString("-5", 4, (int) yzero);
        g2.drawString("5", width - 19, (int) yzero);
        g2.drawString("1.0", width / 2 - 7, 12);
        g2.drawString("0.0", width / 2 - 9, height - 5);
    }
}
