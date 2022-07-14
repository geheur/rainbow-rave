package com.rainbowrave;

import java.awt.Color;

public class PerceptualGradient
{
    private static final double GAMMA = .43;

    private final float[] normalizedStartColor;
    private final float[] normalizedEndColor;

    private final float startColorBrightness;
    private final float endColorBrightness;


    PerceptualGradient(Color startColor, Color endColor) {
        this.normalizedStartColor = normalize(startColor);
        this.normalizedEndColor = normalize(endColor);
        this.startColorBrightness = (float) Math.pow(sum(this.normalizedStartColor), GAMMA);
        this.endColorBrightness = (float) Math.pow(sum(this.normalizedEndColor), GAMMA);
    }

    public Color getColorMix(float frac) {
        float intensity = (float) Math.pow(lerp(this.startColorBrightness, this.endColorBrightness, frac), (1/GAMMA));

        float total = 0;
        float[] components = new float[3];
        for (int i=0; i<components.length; i++) {
            components[i] = lerp(this.normalizedStartColor[i], this.normalizedEndColor[i], frac);
            total += components[i];
        }
        if (total != 0) {
            for (int i=0; i<components.length; i++) {
                components[i] = components[i] * intensity / total;
            }
        }

        for (int i=0; i<components.length; i++) {
            float x = components[i];
            if (x <= 0.0031308) {
                components[i] = (float) (12.92*x);
            } else {
                components[i] = (float) ((1.055 * Math.pow(x , (1/2.4))) - 0.055);
            }
        }

        return new Color(components[0], components[1], components[2]);
    }

    public float sum(float[] array) {
        float sum = 0;
        for (float v : array)
        {
            sum += v;
        }
        return sum;
    }

    private float[] normalize(Color color) {
        float[] components = {color.getRed(), color.getGreen(), color.getBlue()};

        for (int i=0; i < components.length; i++) {
            double y;
            float x = components[i];

            x /= 255.0;
            if (x <= 0.04045) {
                y = x / 12.92;
            }
            else {
                y = Math.pow(((x + 0.055) / 1.055), 2.4);
            }
            components[i] = (float) y;
        }

        return components;
    }

    private float lerp(float color1, float color2, float frac)
    {
        return color1 * (1 - frac) + color2 * frac;
    }
}
