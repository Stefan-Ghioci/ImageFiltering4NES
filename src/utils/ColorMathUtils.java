package utils;

import model.BlockConfig;
import model.PixelColor;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        PixelColor minDiffColor = palette.get(0);
        double minDiff = computeColorDiffSquared(color, palette.get(0));

        for (PixelColor paletteColor : palette)
        {
            double diff = computeColorDiffSquared(color, paletteColor);
            if (diff < minDiff)
            {
                minDiff = diff;
                minDiffColor = paletteColor;
            }
        }
        return minDiffColor;
    }

    public static int computeColorDiffSquared(PixelColor color1, PixelColor color2)
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
        int pixelCount = STD_WIDTH * STD_HEIGHT;
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

    public static List<Integer> getMappingFrequency(Integer[][] mapping)
    {
        Map<Integer, Integer> frequencyMap = IntStream.range(0, 4)
                                                      .boxed()
                                                      .collect(Collectors.toMap(i -> i,
                                                                                i -> 0,
                                                                                (a, b) -> b));

        for (int y = 0; y < BLOCK_SIZE; y++)
            for (int x = 0; x < BLOCK_SIZE; x++)
                frequencyMap.put(mapping[x][y], frequencyMap.get(mapping[x][y]) + 1);

        return frequencyMap.entrySet()
                           .stream()
                           .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<Integer, Integer>>) Map.Entry::getValue)
                                             .reversed())
                           .map(Map.Entry::getKey)
                           .collect(Collectors.toList());
    }

    public static Integer[][] computeMappingByFrequency(List<BlockConfig> cluster)
    {
        Integer[][] meanMapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];


        for (int y = 0; y < BLOCK_SIZE; y++)
            for (int x = 0; x < BLOCK_SIZE; x++)
            {
                int[] frequency = new int[4];

                for (BlockConfig blockConfig : cluster)
                    frequency[blockConfig.getMapping()[x][y]]++;

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

    public static double computeMappingVariance(Integer[][] mapping)
    {
        int[] frequency = new int[4];


        for (int y = 0; y < BLOCK_SIZE; y++)
            for (int x = 0; x < BLOCK_SIZE; x++)
                frequency[mapping[x][y]]++;

        double mean = Arrays.stream(frequency, 0, 4).sum() / 4.0;

        return IntStream.range(0, 4).mapToDouble(i -> pow(mean - frequency[i], 2)).sum() / 4.0;
    }
}
