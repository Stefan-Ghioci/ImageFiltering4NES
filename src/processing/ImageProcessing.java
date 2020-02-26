package processing;

import processing.model.PixelColor;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.util.List;

import static utils.ImageUtils.STD_HEIGHT;
import static utils.ImageUtils.STD_WIDTH;

public class ImageProcessing
{

    public static List<PixelColor> computeBestPalette(List<PixelColor> nesPalette)
    {
        return nesPalette;
    }

    public static void redraw(PixelColor[][] originalImage, List<PixelColor> palette, String filename, boolean dither)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];

        if(!dither)
        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
                image[x][y] = ColorMathUtils.bestMatch(originalImage[x][y], palette);
        else
            for (int x = 0; x < STD_WIDTH; x++)
                for (int y = 0; y < STD_HEIGHT; y++)
                {
                    image[x][y] = ColorMathUtils.bestMatch(originalImage[x][y], palette);

                    //TODO: add dithering
                }


        ImageUtils.saveFile(image, filename);
    }

}
