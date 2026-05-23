package com.markwatson.neuralnetworks;

import java.awt.*;

public class Plot2DPanel extends java.awt.Canvas {

    public Plot2DPanel(int num1, int num2, float min, float max, float[][] values) {
        super();
        this.num1 = num1;
        this.num2 = num2;
        this.min = min;
        this.max = max;
        this.values = values;
        colors = new Color[100];
        for (int i = 0; i < 100; i++) {
            float x = 1.0f - ((float) i) * 0.0096f;
            colors[i] = new Color(x, x, x);
        }
    }

    private final int num1;
    private final int num2;
    private final float min, max;
    private float[][] values = null;
    private final Color[] colors;

    public void paint(Graphics g) {
        if (values == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int delta_width = this.getWidth() / num1;
        int delta_height = this.getHeight() / num2;
        for (int i = 0; i < num1; i++) {
            for (int j = 0; j < num2; j++) {
                float temp = 100.0f * (values[i][j] - min) / (max - min);
                int ii = Math.clamp((int) temp, 0, 99);
                g2.setColor(colors[ii]);
                g2.fillRect(i * delta_width, j * delta_height, delta_width, delta_height);
                g2.setColor(Color.black);
                g2.drawRect((i * delta_width), (j * delta_height), delta_width, delta_height);
            }
        }
    }
}
