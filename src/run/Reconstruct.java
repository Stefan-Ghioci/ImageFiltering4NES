package run;

import ai.impl.SubpaletteConfig;
import ai.impl.SubpaletteEA;
import model.PixelColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.ImageProcessing;
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

        PixelColor[][] originalImage = ImageUtils.loadFile("images/" + filename + ".bmp");

        List<PixelColor> nesPalette = ImageUtils.loadNESPalette();
        List<PixelColor> bestPalette = ImageProcessing.computeBestPalette(originalImage, nesPalette);

        PixelColor[][] nesRawImage = ImageProcessing.redraw(originalImage,
                                                            nesPalette,
                                                            "generated/" + filename + "_nes_raw.bmp",
                                                            false);
        LOGGER.info("Redrawn image with nes palette and no dithering");

        PixelColor[][] nesDitheredImage = ImageProcessing.redraw(originalImage,
                                                                 nesPalette,
                                                                 "generated/" + filename + "_nes_dithered.bmp",
                                                                 true);
        LOGGER.info("Redrawn image with nes palette and dithering");

        PixelColor[][] bestRawImage = ImageProcessing.redraw(originalImage,
                                                             bestPalette,
                                                             "generated/" + filename + "_best_raw.bmp",
                                                             false);
        LOGGER.info("Redrawn image with best palette and no dithering");

        PixelColor[][] bestDitheredImage = ImageProcessing.redraw(originalImage,
                                                                  bestPalette,
                                                                  "generated/" + filename + "_best_dithered.bmp",
                                                                  true);
        LOGGER.info("Redrawn image with best palette and dithering");

        {
            LOGGER.info("Reconstruction using best palette on original image");

            SubpaletteEA algorithm = new SubpaletteEA(originalImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = "generated/" + filename + "_ai_original_" + (int) bestConfig.getFitness() + ".bmp";
            ImageProcessing.reconstruct(originalImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on nes raw image");

            SubpaletteEA algorithm = new SubpaletteEA(nesRawImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = "generated/" + filename + "_ai_nes_raw_" + (int) bestConfig.getFitness() + ".bmp";
            ImageProcessing.reconstruct(nesRawImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on nes dithered image");

            SubpaletteEA algorithm = new SubpaletteEA(nesDitheredImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String
                    resultFilename =
                    "generated/" + filename + "_ai_nes_dithered_" + (int) bestConfig.getFitness() + ".bmp";
            ImageProcessing.reconstruct(nesDitheredImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on best raw image");

            SubpaletteEA algorithm = new SubpaletteEA(bestRawImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String resultFilename = "generated/" + filename + "_ai_best_raw_" + (int) bestConfig.getFitness() + ".bmp";
            ImageProcessing.reconstruct(bestRawImage, subpalettes, resultFilename);
        }
        {
            LOGGER.info("Reconstruction using best palette on best dithered image");

            SubpaletteEA algorithm = new SubpaletteEA(bestDitheredImage, bestPalette);
            SubpaletteConfig bestConfig = algorithm.run(populationSize, stagnationFactor, mutationChance);
            List<List<PixelColor>> subpalettes = bestConfig.getSolution();

            String
                    resultFilename =
                    "generated/" + filename + "_ai_best_dithered_" + (int) bestConfig.getFitness() + ".bmp";
            ImageProcessing.reconstruct(bestDitheredImage, subpalettes, resultFilename);
        }
    }
}
