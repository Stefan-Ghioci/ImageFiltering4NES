package utils;

import processing.model.PixelColor;

import java.util.List;

public class ColorMathUtils
{
    public static PixelColor bestMatch(PixelColor color, List<PixelColor> palette)
    {
        PixelColor min_diff_color = palette.get(0);
        double min_diff = computeColorDiffSquared(color, palette.get(0));

        for (PixelColor paletteColor : palette)
        {
            double diff = computeColorDiffSquared(color, paletteColor);
            if (diff < min_diff)
            {
                min_diff = diff;
                min_diff_color = paletteColor;
            }
        }
        return min_diff_color;
    }

    private static double computeColorDiffSquared(PixelColor color1, PixelColor color2)
    {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        return ((r1 - r2) * 3) * ((r1 - r2) * 3) +
               ((g1 - g2) * 4) * ((g1 - g2) * 4) +
               ((b1 - b2) * 2) * ((b1 - b2) * 2);
    }
}
