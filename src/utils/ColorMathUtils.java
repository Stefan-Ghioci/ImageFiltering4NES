package utils;

import model.PixelColor;
import model.TileConfig;

import java.util.List;

import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static model.Constants.*;

public class ColorMathUtils
{
    private ColorMathUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    public static PixelColor bestMatch(PixelColor color, List<PixelColor> palette)
    {
        PixelColor minDistanceColor = palette.get(0);
        double minDistance = distanceBetween(color, palette.get(0));

        for (PixelColor paletteColor : palette)
        {
            double distance = distanceBetween(color, paletteColor);
            if (distance < minDistance)
            {
                minDistance = distance;
                minDistanceColor = paletteColor;
            }
        }
        return minDistanceColor;
    }

    public static double distanceBetween(PixelColor color1, PixelColor color2)
    {
        double r1 = color1.getRed();
        double g1 = color1.getGreen();
        double b1 = color1.getBlue();

        double r2 = color2.getRed();
        double g2 = color2.getGreen();
        double b2 = color2.getBlue();

        return (((r1 - r2) * 3) * ((r1 - r2) * 3) +
                ((g1 - g2) * 4) * ((g1 - g2) * 4) +
                ((b1 - b2) * 2) * ((b1 - b2) * 2)) /
               (3 * 3 + 4 * 4 + 2 * 2);
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
            // out of image boundary, continue
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

    public static double getMinDistanceSumPerTileGroup(int x,
                                                       int y,
                                                       List<List<PixelColor>> subpaletteList,
                                                       PixelColor[][] image)
    {
        double minDistanceSum = -1;

        for (List<PixelColor> subpalette : subpaletteList)
        {
            double distanceSum = 0;

            for (int i = 0; i < TILE_GROUP_SIZE; i++)
                for (int j = 0; j < TILE_GROUP_SIZE; j++)
                {
                    PixelColor color = image[x + i][y + j];
                    PixelColor bestMatch = bestMatch(color, subpalette);
                    distanceSum += distanceBetween(color, bestMatch);
                }
            if (minDistanceSum == -1 || minDistanceSum > distanceSum)
                minDistanceSum = distanceSum;
        }
        return minDistanceSum;
    }

    public static List<PixelColor> getMinDistanceSubpalettePerTileGroup(int x,
                                                                        int y,
                                                                        List<List<PixelColor>> subpaletteList,
                                                                        PixelColor[][] image)
    {
        double minDistanceSum = -1;
        List<PixelColor> bestSubpalette = subpaletteList.get(0);

        for (List<PixelColor> subpalette : subpaletteList)
        {
            double distanceSum = 0;

            for (int i = 0; i < TILE_GROUP_SIZE; i++)
                for (int j = 0; j < TILE_GROUP_SIZE; j++)
                {
                    PixelColor color = image[x + i][y + j];
                    PixelColor bestMatch = bestMatch(color, subpalette);
                    distanceSum += distanceBetween(color, bestMatch);
                }
            if (minDistanceSum == -1 || minDistanceSum > distanceSum)
            {
                minDistanceSum = distanceSum;
                bestSubpalette = subpalette;
            }
        }
        return bestSubpalette;
    }

    public static double computeAverageDistance(PixelColor[][] image1, PixelColor[][] image2)
    {
        int pixelCount = STD_WIDTH * STD_HEIGHT;
        double distanceSum = 0;

        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
                distanceSum += distanceBetween(image1[x][y], image2[x][y]);

        return distanceSum / pixelCount;
    }

    private static double getSquaredError(PixelColor color1, PixelColor color2)
    {
        return pow(color1.getLuminance() - color2.getLuminance(), 2);
    }


    public static Integer[][] getBestFitMapping(List<TileConfig> cluster, PixelColor[][] image)
    {
        Integer[][] bestFitMapping = null;
        double minMSE = -1;

        for (TileConfig config1 : cluster)
        {
            double mse = 0;

            for (TileConfig config2 : cluster)
            {
                Integer row = config2.getRow();
                Integer column = config2.getColumn();
                List<PixelColor> subpalette = config2.getSubpalette();

                Integer[][] mapping = config1.getMapping();

                TileConfig temp = new TileConfig(row, column, mapping, subpalette);

                mse += getSquaredErrorSumPerTile(temp, image);
            }
            mse /= cluster.size();

            if (minMSE == -1 || minMSE > mse)
            {
                bestFitMapping = config1.getMapping();
                minMSE = mse;
            }
        }

        return bestFitMapping;
    }

    private static double getSquaredErrorSumPerTile(TileConfig tileConfig, PixelColor[][] image)
    {
        double squaredErrorSum = 0;
        Integer[][] mapping = tileConfig.getMapping();
        List<PixelColor> subpalette = tileConfig.getSubpalette();

        int x = tileConfig.getRow() * TILE_SIZE;
        int y = tileConfig.getColumn() * TILE_SIZE;

        for (int i = 0; i < TILE_SIZE; i++)
            for (int j = 0; j < TILE_SIZE; j++)
            {
                squaredErrorSum += getSquaredError(image[x + i][y + j], subpalette.get(mapping[i][j]));
            }

        return squaredErrorSum;
    }


    public static Integer[][] computeAverageMappingByFrequency(List<TileConfig> cluster)
    {
        Integer[][] meanMapping = new Integer[TILE_SIZE][TILE_SIZE];


        for (int y = 0; y < TILE_SIZE; y++)
            for (int x = 0; x < TILE_SIZE; x++)
            {
                int[] frequency = new int[4];

                for (TileConfig tileConfig : cluster)
                    frequency[tileConfig.getMapping()[x][y]]++;

                int mostFrequent = -1;
                int maxFrequency = 0;

                for (int i = 0; i < 4; i++)
                    if (maxFrequency < frequency[i])
                    {
                        mostFrequent = i;
                        maxFrequency = frequency[i];
                    }

                meanMapping[x][y] = mostFrequent;
            }

        return meanMapping;
    }

    public static double computeMeanSquaredError(PixelColor[][] image1, PixelColor[][] image2)
    {
        int pixelCount = STD_WIDTH * STD_HEIGHT;
        double squaredErrorSum = 0;

        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
                squaredErrorSum += getSquaredError(image1[x][y], image2[x][y]);

        return squaredErrorSum / pixelCount;
    }

    public static double getPeakSignal2NoiseRatio(double mse)
    {
        return 10 * log10(255 * 255 / mse);
    }
}
