package com.rainbowrave;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public enum Theme
{
    DEFAULT,
    TRANS(new Color[]{
            new Color(91, 206, 250),
            new Color(245, 169, 184),
            new Color(255, 255, 255),
            new Color(245, 169, 184),
            new Color(91, 206, 250)
    }),
    ENBY(new Color[]{
            new Color(255, 255, 255),
            new Color(156, 89, 209),
            new Color(44, 44, 44),
            new Color(252, 244, 52),
            new Color(255, 255, 255),
    }),
    LESBIAN(new Color[]{
            new Color(212, 44, 0),
            new Color(253, 152, 85),
            new Color(255, 255, 255),
            new Color(209, 97, 162),
            new Color(162, 1, 97),
            //new Color(212, 44, 0)
    }),
    PAN(new Color[]{
            new Color(255, 33, 140),
            new Color(255, 216, 0),
            new Color(255, 33, 140),
            new Color(33, 177, 255),
            new Color(255, 33, 140),
    }),
    ACE(new Color[]{
            new Color(0, 0, 0),
            new Color(163, 163, 163),
            new Color(255, 255, 255),
            new Color(128, 0, 128),
            new Color(0, 0, 0)
    }),
    BI(new Color[]{
            new Color(214, 2, 112),
            new Color(0, 56, 168),
            new Color(155, 79, 150),
    }),
    GENDER_QUEER(new Color[]{
            new Color(181, 126, 220),
            new Color(255, 255, 255),
            new Color(74, 129, 35),
            new Color(181, 126, 220),
    }),
    GAY(new Color[]{
            new Color(255, 255, 255),
            new Color(123, 173, 226),
            new Color(80, 73, 204),
            new Color(61, 26, 120),
            new Color(7, 141, 112),
            new Color(38, 206, 170),
            new Color(152, 232, 193),
            new Color(255, 255, 255),
    });

    private final List<PerceptualGradient> gradients;

    Theme()
    {
        gradients = new ArrayList<>();
    }

    Theme(Color[] colors) {
        gradients = new ArrayList<>();
        for (int i=0; i<colors.length-1; i++) {
            gradients.add(new PerceptualGradient(colors[i], colors[i+1]));
        }
    }

    public Color getColor(float ratio)
    {
        // Subtract floor to match Color.getHSBColor() functionality
        ratio = Math.abs((float) (ratio - Math.floor(ratio)));

        if (gradients.size() == 0) {
            return Color.getHSBColor(ratio, 1.0f, 1.0f);
        }

        float increment = 1.0f/gradients.size(); // Since size isn't 0, increment is always (0-1]
        PerceptualGradient gradient = gradients.get((int) Math.floor(ratio/increment));
        float relativeRatio = (ratio%increment)/increment;
        return gradient.getColorMix(relativeRatio);
    }
}
