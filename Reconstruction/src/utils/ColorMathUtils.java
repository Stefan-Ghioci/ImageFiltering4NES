package utils;

import processing.PixelColor;

import java.util.List;

import static utils.ImageUtils.BLOCK_SIZE;

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

    public static double computeColorDiffSquared(PixelColor color1, PixelColor color2)
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

    public static void dither(PixelColor[][] image, int x, int y, PixelColor oldPixel, PixelColor newPixel)
    {
        PixelColor quantizationError = PixelColor.difference(oldPixel, newPixel);

        try
        {
            image[x + 1][y] = addQuantizationError(image[x + 1][y], quantizationError, 7 / 16.0);
            image[x - 1][y + 1] = addQuantizationError(image[x - 1][y + 1], quantizationError, 3 / 16.0);
            image[x][y + 1] = addQuantizationError(image[x][y + 1], quantizationError, 4 / 16.0);
            image[x + 1][y + 1] = addQuantizationError(image[x + 1][y + 1], quantizationError, 1 / 16.0);
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {
        }
    }

    private static PixelColor addQuantizationError(PixelColor pixelColor,
                                                   PixelColor quantizationError,
                                                   double factor)
    {
        return new PixelColor(pixelColor.getRed() + (int) Math.round(quantizationError.getRed() * factor),
                              pixelColor.getGreen() + (int) Math.round(quantizationError.getGreen() * factor),
                              pixelColor.getBlue() + (int) Math.round(quantizationError.getBlue() * factor));


    }

    public static double getMinDiffSumPerBlock(int x,
                                               int y,
                                               List<List<PixelColor>> subpaletteList,
                                               PixelColor[][] image)
    {
        double minDiffSum = -1;

        for (List<PixelColor> subpalette : subpaletteList)
        {
            double diffSum = 0;

            for (int i = 0; i < BLOCK_SIZE; i++)
                for (int j = 0; j < BLOCK_SIZE; j++)
                {
                    PixelColor color = image[x + i][y + j];
                    PixelColor bestMatch = bestMatch(color, subpalette);
                    diffSum += computeColorDiffSquared(color, bestMatch);
                }
            if (minDiffSum == -1 || minDiffSum > diffSum)
                minDiffSum = diffSum;
        }
        return minDiffSum;
    }

    public static List<PixelColor> getBestSubpalettePerBlock(int x,
                                                             int y,
                                                             List<List<PixelColor>> subpaletteList,
                                                             PixelColor[][] image)
    {
        double minDiffSum = -1;
        List<PixelColor> minSubpalette = subpaletteList.get(0);

        for (List<PixelColor> subpalette : subpaletteList)
        {
            double diffSum = 0;

            for (int i = 0; i < BLOCK_SIZE; i++)
                for (int j = 0; j < BLOCK_SIZE; j++)
                {
                    PixelColor color = image[x + i][y + j];
                    PixelColor bestMatch = bestMatch(color, subpalette);
                    diffSum += computeColorDiffSquared(color, bestMatch);
                }
            if (minDiffSum == -1 || minDiffSum > diffSum)
            {
                minDiffSum = diffSum;
                minSubpalette = subpalette;
            }
        }
        return minSubpalette;
    }
}
