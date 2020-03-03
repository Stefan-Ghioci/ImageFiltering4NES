import ai.impl.SubpaletteAlgorithm;
import ai.impl.SubpaletteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.ImageProcessing;
import model.PixelColor;
import utils.ImageUtils;

import java.util.List;

public class Reconstruct
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Reconstruct.class);

    public static void main(String[] args)
    {
        String filename = args[0];
        int populationSize = Integer.parseInt(args[1]);
        int stagnationFactor = Integer.parseInt(args[2]);
        double mutationChance = Double.parseDouble(args[3]);

        PixelColor[][] originalImage = ImageUtils.loadFile(filename);

        List<PixelColor> nesPalette = ImageUtils.loadNESPalette();
        List<PixelColor> bestPalette = ImageProcessing.computeBestPalette(originalImage, nesPalette);

        PixelColor[][] nesRawImage = ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", false);
        PixelColor[][] nesDitheredImage = ImageProcessing.redraw(originalImage, nesPalette, filename + "_nes", true);


        PixelColor[][] bestRawImage = ImageProcessing.redraw(originalImage, bestPalette, filename + "_best", false);
        PixelColor[][] bestDitheredImage = ImageProcessing.redraw(originalImage, bestPalette, filename + "_best", true);

        {
            LOGGER.info("Reconstruction using best palette on nes raw image");

            SubpaletteAlgorithm algorithm = new SubpaletteAlgorithm(nesRawImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = filename + "_ai_nes_raw_" + (int) bestConfig.getFitness();
            ImageProcessing.reconstruct(nesRawImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on nes dithered image");

            SubpaletteAlgorithm algorithm = new SubpaletteAlgorithm(nesDitheredImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = filename + "_ai_nes_dithered_" + (int) bestConfig.getFitness();
            ImageProcessing.reconstruct(nesDitheredImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on best raw image");

            SubpaletteAlgorithm algorithm = new SubpaletteAlgorithm(bestRawImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = filename + "_ai_best_raw_" + (int) bestConfig.getFitness();
            ImageProcessing.reconstruct(bestRawImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on best dithered image");

            SubpaletteAlgorithm algorithm = new SubpaletteAlgorithm(bestDitheredImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = filename + "_ai_best_dithered_" + (int) bestConfig.getFitness();
            ImageProcessing.reconstruct(bestDitheredImage, subpalettes, resultFilename);
        }
    }
}
