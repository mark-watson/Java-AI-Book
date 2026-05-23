package com.markwatson.neuralnetworks;


import java.awt.*;

public class Plot1DPanel extends java.awt.Canvas {

    public Plot1DPanel(int num, float min, float max, float[] values) {
        super();
        this.num = num;
        this.min = min;
        this.max = max;
        this.values = values;
        colors = new Color[100];
        for (int i = 0; i < 100; i++) {
            float x = 1.0f - ((float) i) * 0.0096f;
            colors[i] = new Color(x, x, x);
        }
        this.setBackground(Color.white);
    }

    private final int num;
    private final float min, max;
    private float[] values = null;
    private final Color[] colors;

    public void paint(Graphics g) {
        if (values == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int delta_width = this.getWidth() / num;
        int delta_height = this.getHeight() / num;
        for (int i = 0; i < num; i++) {
            float temp = 100.0f * (values[i] - min) / (max - min);
            int ii = Math.clamp((int) temp, 0, 99);
            g2.setColor(colors[ii]);
            g2.fillRect(i * delta_width, 0, delta_width, delta_height);
            g2.setColor(Color.black);
            g2.drawRect(i * delta_width, 0, delta_width, delta_height);
        }
    }
}
