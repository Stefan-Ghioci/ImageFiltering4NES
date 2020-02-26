import processing.ImageProcessing;
import processing.model.PixelColor;
import utils.ImageUtils;

import java.util.List;

public class Start
{
    public static void main(String[] args)
    {
        String filename = args[0];

        PixelColor[][] originalImage = ImageUtils.loadFile(filename);

        List<PixelColor> nesPalette = ImageUtils.loadNESPalette();
        List<PixelColor> bestPalette = ImageProcessing.computeBestPalette(originalImage, nesPalette);

        ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", false);
        ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", true);

    }
}
