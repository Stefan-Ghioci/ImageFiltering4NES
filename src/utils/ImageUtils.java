package utils;

import model.PixelColor;
import model.TileConfig;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static model.Constants.*;

public class ImageUtils
{
    static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    private ImageUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    public static List<PixelColor> loadNESPalette()
    {
        List<PixelColor> palette = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("resources/nes_palette.csv")))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                String[] rgb = line.split(",");

                int red = Integer.parseInt(rgb[0]);
                int green = Integer.parseInt(rgb[1]);
                int blue = Integer.parseInt(rgb[2]);

                palette.add(new PixelColor(red, green, blue));
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load NES palette file. Cause: {}", e.getMessage());
        }
        return palette;
    }

    public static PixelColor[][] loadFile(String filename)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];
        try
        {
            BufferedImage bufferedImage = ImageIO.read(new File(filename));

            for (int x = 0; x < STD_WIDTH; x++)
                for (int y = 0; y < STD_HEIGHT; y++)
                    image[x][y] = new PixelColor(bufferedImage.getRGB(x, y));
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load image. Cause: {}", e.getMessage());

        }
        return image;
    }

    public static PixelColor[][] convertTileConfigsToPixelArray(List<TileConfig> tileConfigList)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];

        for (TileConfig tileConfig : tileConfigList)
        {
            int x = tileConfig.getRow() * TILE_SIZE;
            int y = tileConfig.getColumn() * TILE_SIZE;
            Integer[][] mapping = tileConfig.getMapping();
            List<PixelColor> subpalette = tileConfig.getSubpalette();

            for (int i = 0; i < TILE_SIZE; i++)
                for (int j = 0; j < TILE_SIZE; j++)
                {
                    image[x + i][y + j] = subpalette.get(mapping[i][j]);
                }

        }
        return image;
    }

    public static File saveFile(PixelColor[][] image, String filename)
    {
        BufferedImage bufferedImage = new BufferedImage(STD_WIDTH, STD_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
            {
                bufferedImage.setRGB(x, y, image[x][y].toInt());
            }

        File outputFile = new File(filename);
        try
        {
            ImageIO.write(bufferedImage, "bmp", outputFile);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to save new image. Cause: {}", e.getMessage());
        }
        return outputFile;
    }

    public static void writeSubpaletteMappingToFile(List<List<PixelColor>> subpaletteList,
                                                    List<Integer> subpaletteMapping,
                                                    File file)
    {
        String fileName = "generated/" + FilenameUtils.removeExtension(file.getName()) + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName)))
        {
            for (List<PixelColor> pixelColors : subpaletteList)
                for (PixelColor pixelColor : pixelColors)

                {
                    writer.write(pixelColor.getRed() +
                                 " " +
                                 pixelColor.getGreen() +
                                 " " +
                                 pixelColor.getBlue());
                    writer.newLine();
                }

            for (int i = 0; i < subpaletteMapping.size(); i++)
            {
                Integer subpaletteIndex = subpaletteMapping.get(i);
                writer.write(subpaletteIndex + " ");
                if ((i + 1) % (STD_WIDTH / TILE_GROUP_SIZE) == 0)
                    writer.newLine();
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to write subpalette mapping to file. Cause: {}", e.getMessage());
        }
    }

    public static List<TileConfig> loadTileConfigs(File textFile, PixelColor[][] image)
    {
        List<List<PixelColor>> subpaletteList = new ArrayList<>();
        Integer[][] subpaletteMappingList = new Integer
                [STD_WIDTH / TILE_GROUP_SIZE]
                [STD_HEIGHT / TILE_GROUP_SIZE];

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile)))
        {
            for (int i = 0; i < 4; i++)
            {
                List<PixelColor> subpalette = new ArrayList<>();
                for (int j = 0; j < 4; j++)
                {
                    String[] split = reader.readLine().split(" ");
                    int red = Integer.parseInt(split[0]);
                    int green = Integer.parseInt(split[1]);
                    int blue = Integer.parseInt(split[2]);
                    subpalette.add(new PixelColor(red, green, blue));
                }
                subpaletteList.add(subpalette);
            }

            for (int y = 0; y < STD_HEIGHT / TILE_GROUP_SIZE; y++)
            {
                String[] split = reader.readLine().split(" ");
                for (int x = 0; x < STD_WIDTH / TILE_GROUP_SIZE; x++)
                    subpaletteMappingList[x][y] = Integer.valueOf(split[x]);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load tile configs from file. Cause: {}", e.getMessage());
        }

        return createTileConfigs(image, subpaletteList, subpaletteMappingList);
    }

    private static List<TileConfig> createTileConfigs(PixelColor[][] image,
                                                      List<List<PixelColor>> subpaletteList,
                                                      Integer[][] subpaletteMappingList)
    {
        List<TileConfig> tileConfigList = new ArrayList<>();
        for (int x = 0; x < STD_WIDTH; x += TILE_SIZE)
            for (int y = 0; y < STD_HEIGHT; y += TILE_SIZE)
            {
                Integer[][] mapping = new Integer[TILE_SIZE][TILE_SIZE];
                int row = x / TILE_SIZE;
                int column = y / TILE_SIZE;

                List<PixelColor> subpalette = subpaletteList.get(
                        subpaletteMappingList
                                [row / (TILE_GROUP_SIZE / TILE_SIZE)]
                                [column / (TILE_GROUP_SIZE / TILE_SIZE)]
                                                                );

                for (int i = 0; i < TILE_SIZE; i++)
                    for (int j = 0; j < TILE_SIZE; j++)
                        mapping[i][j] =
                                subpalette.indexOf(image[x + i][y + j]);

                tileConfigList.add(new TileConfig(row, column, mapping, subpalette));
            }
        return tileConfigList;
    }
}
