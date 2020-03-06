package processing;

import model.BlockConfig;
import model.PixelColor;
import utils.ColorMathUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static model.Constants.*;
import static utils.ColorMathUtils.bestMatch;
import static utils.ColorMathUtils.getBestSubpalettePerBlock;
import static utils.ImageUtils.saveFile;
import static utils.ImageUtils.writeSubpaletteMappingToFile;

public class ImageProcessing
{

    public static List<PixelColor> computeBestPalette(PixelColor[][] originalImage, List<PixelColor> palette)
    {
        Map<PixelColor, Integer> histogram = palette.stream()
                                                    .collect(Collectors.toMap(color -> color, color -> 0, (a, b) -> b));


        for (PixelColor[] pixelColors : originalImage)
            for (PixelColor pixelColor : pixelColors)
            {
                PixelColor bestMatch = bestMatch(pixelColor, palette);
                histogram.put(bestMatch, histogram.get(bestMatch) + 1);
            }
        List<PixelColor> bestPalette = histogram.entrySet()
                                                .stream()
                                                .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<PixelColor, Integer>>) Map.Entry::getValue)
                                                                  .reversed())
                                                .limit(13)
                                                .map(Map.Entry::getKey)
                                                .collect(Collectors.toList());

        if (!bestPalette.contains(PixelColor.BLACK()))
        {
            bestPalette.remove(bestPalette.size() - 1);
            bestPalette.add(PixelColor.BLACK());
        }
        return bestPalette;
    }

    public static PixelColor[][] redraw(PixelColor[][] originalImage,
                                        List<PixelColor> palette,
                                        String filename,
                                        boolean dither)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];

        if (!dither)
            for (int x = 0; x < STD_WIDTH; x++)
                for (int y = 0; y < STD_HEIGHT; y++)
                    image[x][y] = bestMatch(originalImage[x][y], palette);
        else
        {
            // array copy originalImage into image
            IntStream.range(0, STD_WIDTH)
                     .forEach(x -> System.arraycopy(originalImage[x], 0, image[x], 0, STD_HEIGHT));

            for (int y = 0; y < STD_HEIGHT; y++)
                for (int x = 0; x < STD_WIDTH; x++)
                {
                    PixelColor oldPixel = image[x][y];
                    PixelColor newPixel = bestMatch(oldPixel, palette);

                    image[x][y] = newPixel;

                    ColorMathUtils.dither(image, x, y, oldPixel, newPixel);
                }
        }
        saveFile(image, filename);

        return image;
    }

    public static void reconstruct(PixelColor[][] originalImage, List<List<PixelColor>> subpaletteList, String filename)
    {

        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];
        List<Integer> subpaletteMapping = new ArrayList<>();

        subpaletteList.forEach(subpalette -> subpalette.sort(Comparator.comparingInt(PixelColor::getLuminance)));

        for (int y = 0; y < STD_HEIGHT; y += BLOCK_GROUP_SIZE)
            for (int x = 0; x < STD_WIDTH; x += BLOCK_GROUP_SIZE)
            {
                List<PixelColor> subpalette = getBestSubpalettePerBlock(x, y, subpaletteList, originalImage);

                subpaletteMapping.add(subpaletteList.indexOf(subpalette));

                for (int i = 0; i < BLOCK_GROUP_SIZE; i++)
                    for (int j = 0; j < BLOCK_GROUP_SIZE; j++)
                    {
                        PixelColor bestMatch = bestMatch(originalImage[x + i][y + j], subpalette);
                        image[x + i][y + j] = bestMatch;
                    }
            }

        writeSubpaletteMappingToFile(subpaletteList, subpaletteMapping, saveFile(image, filename));
    }

    public static List<BlockConfig> compress(List<List<BlockConfig>> clusteredBlockConfigList,
                                             PixelColor[][] image)
    {
        List<BlockConfig> compressedBlockConfigList = new ArrayList<>();

        for (List<BlockConfig> cluster : clusteredBlockConfigList)
        {
            BlockConfig bestFit = ColorMathUtils.bestFitMapping(cluster, image);

            for (BlockConfig blockConfig : cluster)
            {
                List<PixelColor> subpalette = blockConfig.getSubpalette();
                Integer row = blockConfig.getRow();
                Integer column = blockConfig.getColumn();

                Integer[][] mapping = bestFit.getMapping();

                compressedBlockConfigList.add(new BlockConfig(row, column, mapping, subpalette));
            }
        }

        return compressedBlockConfigList;
    }

}
