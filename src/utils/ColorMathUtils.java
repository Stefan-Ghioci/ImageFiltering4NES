package utils;

import model.BlockConfig;
import model.PixelColor;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static model.Constants.*;

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

            for (int i = 0; i < BLOCK_GROUP_SIZE; i++)
                for (int j = 0; j < BLOCK_GROUP_SIZE; j++)
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

            for (int i = 0; i < BLOCK_GROUP_SIZE; i++)
                for (int j = 0; j < BLOCK_GROUP_SIZE; j++)
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

    public static double calculateAvgDiff(PixelColor[][] image1, PixelColor[][] image2)
    {
        double pixelCount = STD_WIDTH * STD_HEIGHT;
        double diffSum = 0;

        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
                diffSum += computeColorDiffSquared(image1[x][y], image2[x][y]);

        return diffSum / pixelCount;
    }

    public static Integer[][] bestFitMapping(List<BlockConfig> cluster, PixelColor[][] image)
    {


        Integer[][] bestFitMapping = null;
        double minAvgDiffSum = -1;

        for (BlockConfig config1 : cluster)
        {
            double avgDiffSum = 0;

            for (BlockConfig config2 : cluster)
            {
                Integer row = config2.getRow();
                Integer column = config2.getColumn();
                List<PixelColor> subpalette = config2.getSubpalette();

                Integer[][] mapping = config1.getMapping();

                BlockConfig temp = new BlockConfig(row, column, mapping, subpalette);

                avgDiffSum += calculateBlockDiffSum(temp, image);
            }
            avgDiffSum /= cluster.size();

            if (minAvgDiffSum == -1 || minAvgDiffSum > avgDiffSum)
            {
                bestFitMapping = config1.getMapping();
                minAvgDiffSum = avgDiffSum;
            }
        }

        return bestFitMapping;
    }

    private static double calculateBlockDiffSum(BlockConfig blockConfig, PixelColor[][] image)
    {
        double diffSum = 0;
        Integer[][] mapping = blockConfig.getMapping();
        List<PixelColor> subpalette = blockConfig.getSubpalette();

        int x = blockConfig.getRow() * BLOCK_SIZE;
        int y = blockConfig.getColumn() * BLOCK_SIZE;

        for (int i = 0; i < BLOCK_SIZE; i++)
            for (int j = 0; j < BLOCK_SIZE; j++)
            {
                diffSum += computeColorDiffSquared(image[x + i][y + j], subpalette.get(mapping[i][j]));
            }

        return diffSum;
    }

    public static double norm(Integer[][] matrix)
    {

        return sqrt(Arrays.stream(matrix)
                          .mapToDouble(row -> Arrays.stream(row)
                                                    .mapToDouble(integer -> pow(integer, 2))
                                                    .sum())
                          .sum());
    }

    public static double meanSquaredError(Integer[][] matrix1, Integer[][] matrix2)
    {
        double result = 0.0;
        int X = matrix1.length;
        int Y = matrix1[0].length;

        for (int x = 0; x < X; x++)
            for (int y = 0; y < Y; y++)
            {
                result += pow(matrix1[x][y] - matrix2[x][y], 2);
            }

        return result / (X * Y);
    }

    public static double structuralSimilarity(Integer[][] mapping1, Integer[][] mapping2)
    {
        double variance1 = calculateMappingVariance(mapping1);
        double variance2 = calculateMappingVariance(mapping2);
        double covariance = calculateMappingCovariance(mapping1, mapping2);
        return covariance / (variance1 * variance2);
    }

    private static double calculateMappingCovariance(Integer[][] mapping1, Integer[][] mapping2)
    {
        double mean1 = mean(mapping1);
        double mean2 = mean(mapping2);

        double diffProdSum = 0;

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
                diffProdSum += (mapping1[x][y] - mean1) * (mapping2[x][y] - mean2);

        return abs(diffProdSum / (BLOCK_SIZE * BLOCK_SIZE));
    }

    private static double mean(Integer[][] mapping)
    {
        int sum = 0;

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
                sum += mapping[x][y];

        return (double) sum / (BLOCK_SIZE * BLOCK_SIZE);
    }

    private static double calculateMappingVariance(Integer[][] mapping)
    {
        double mean = mean(mapping);

        double squaredDiffSum = 0;

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
                squaredDiffSum += pow(mapping[x][y] - mean, 2);

        return squaredDiffSum / (BLOCK_SIZE * BLOCK_SIZE);
    }

    public static Set<List<Integer>> generatePermutations(int[] numbers)
    {
        Set<List<Integer>> permutations = new HashSet<>();

        int maxSize = factorial(numbers.length);

        while (permutations.size() < maxSize)
        {
            List<Integer> list = Arrays.stream(numbers)
                                       .boxed()
                                       .collect(Collectors.toList());
            Collections.shuffle(list);

            permutations.add(list);
        }
        return permutations;
    }

    private static int factorial(int number)
    {
        if (number == 1 || number == 0)
            return 1;
        else
            return factorial(number - 1) * number;
    }
}
