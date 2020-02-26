import ai.EvolutionaryAlgorithm;
import ai.impl.SubpaletteAlgorithm;
import processing.ImageProcessing;
import processing.PixelColor;
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

        PixelColor[][] nesRawImage = ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", false);
        PixelColor[][] nesDitheredImage = ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", true);


        PixelColor[][] bestRawImage = ImageProcessing.redraw(originalImage, bestPalette, filename + "_best", false);
        PixelColor[][] bestDitheredImage = ImageProcessing.redraw(originalImage, bestPalette, filename + "_best", true);


        SubpaletteAlgorithm algorithm = new SubpaletteAlgorithm(bestDitheredImage, bestPalette);
        List<List<PixelColor>> subpalettes = algorithm.run(20, 50, 0.1);

    }
}
