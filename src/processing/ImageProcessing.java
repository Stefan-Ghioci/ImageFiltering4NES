package processing;

import processing.model.PixelColor;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.util.List;
import java.util.stream.IntStream;

import static utils.ImageUtils.STD_HEIGHT;
import static utils.ImageUtils.STD_WIDTH;

public class ImageProcessing
{

    public static List<PixelColor> computeBestPalette(PixelColor[][] originalImage, List<PixelColor> nesPalette)
    {
        return nesPalette;
    }

    public static void redraw(PixelColor[][] originalImage, List<PixelColor> palette, String filename, boolean dither)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];

        if (!dither)
            for (int x = 0; x < STD_WIDTH; x++)
                for (int y = 0; y < STD_HEIGHT; y++)
                    image[x][y] = ColorMathUtils.bestMatch(originalImage[x][y], palette);
        else
        {
            // array copy originalImage into image
            IntStream.range(0, STD_WIDTH)
                     .forEach(x -> System.arraycopy(originalImage[x], 0, image[x], 0, STD_HEIGHT));

            for (int y = 0; y < STD_HEIGHT; y++)
                for (int x = 0; x < STD_WIDTH; x++)
                {
                    PixelColor oldPixel = image[x][y];
                    PixelColor newPixel = ColorMathUtils.bestMatch(oldPixel, palette);

                    image[x][y] = newPixel;

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
        }
        ImageUtils.saveFile(image, filename + (dither ? "_dithered" : "_raw"));
    }

    private static PixelColor addQuantizationError(PixelColor pixelColor,
                                                   PixelColor quantizationError,
                                                   double factor)
    {
        return new PixelColor(pixelColor.getRed() + (int) Math.round(quantizationError.getRed() * factor),
                              pixelColor.getGreen() + (int) Math.round(quantizationError.getGreen() * factor),
                              pixelColor.getBlue() + (int) Math.round(quantizationError.getBlue() * factor));


    }
}
