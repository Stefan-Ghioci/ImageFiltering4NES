package processing;

import utils.ColorMathUtils;
import utils.ImageUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.ImageUtils.STD_HEIGHT;
import static utils.ImageUtils.STD_WIDTH;

public class ImageProcessing
{

    public static List<PixelColor> computeBestPalette(PixelColor[][] originalImage, List<PixelColor> palette)
    {
        Map<PixelColor, Integer> histogram = palette.stream()
                                                    .collect(Collectors.toMap(color -> color, color -> 0, (a, b) -> b));


        for (PixelColor[] pixelColors : originalImage)
            for (PixelColor pixelColor : pixelColors)
            {
                PixelColor bestMatch = ColorMathUtils.bestMatch(pixelColor, palette);
                histogram.put(bestMatch, histogram.get(bestMatch) + 1);
            }
        List<PixelColor> bestPalette = histogram.entrySet()
                                                .stream()
                                                .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<PixelColor, Integer>>) Map.Entry::getValue)
                                                                  .reversed())
                                                .limit(13)
                                                .map(Map.Entry::getKey)
                                                .collect(Collectors.toList());

        if (!bestPalette.contains(PixelColor.BLACK))
        {
            bestPalette.remove(bestPalette.size() - 1);
            bestPalette.add(PixelColor.BLACK);
        }
        return bestPalette;
    }

    public static PixelColor[][] redraw(PixelColor[][] originalImage, List<PixelColor> palette, String filename, boolean dither)
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

                    ColorMathUtils.dither(image, x, y, oldPixel, newPixel);
                }
        }
        ImageUtils.saveFile(image, filename + (dither ? "_dithered" : "_raw"));

        return image;
    }

}
