package utils;

import model.BlockMapping;
import model.PixelColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static model.Constants.*;

public class ImageUtils
{

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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return image;
    }

    public static PixelColor[][] convertBlockMappingsToPixelArray(List<BlockMapping> blockMappingList)
    {
        PixelColor[][] image = new PixelColor[STD_WIDTH][STD_HEIGHT];

        for (BlockMapping blockMapping : blockMappingList)
        {
            int x = blockMapping.getRow() * BLOCK_SIZE;
            int y = blockMapping.getColumn() * BLOCK_SIZE;
            Integer[][] mapping = blockMapping.getMapping();
            List<PixelColor> subpalette = blockMapping.getSubpalette();

            for (int i = 0; i < BLOCK_SIZE; i++)
                for (int j = 0; j < BLOCK_SIZE; j++)
                {
                    image[x + i][y + j] = subpalette.get(mapping[i][j]);
                }

        }
        return image;
    }

    public static void saveFile(PixelColor[][] image, String filename)
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
            e.printStackTrace();
        }
    }

    public static void writeSubpaletteMappingToFile(List<List<PixelColor>> subpaletteList,
                                                    List<Integer> subpaletteMapping,
                                                    String filename)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("generated/" + filename + ".txt")))
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
                if ((i + 1) % (STD_WIDTH / BLOCK_GROUP_SIZE) == 0)
                    writer.newLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static List<BlockMapping> loadGeneratedBlockMappings(File textFile, PixelColor[][] image)
    {
        List<BlockMapping> blockMappingList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile)))
        {
            List<List<PixelColor>> subpaletteList = new ArrayList<>();
            Integer[][] subpaletteMappingList = new Integer
                    [STD_WIDTH / BLOCK_GROUP_SIZE]
                    [STD_HEIGHT / BLOCK_GROUP_SIZE];

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

            for (int y = 0; y < STD_HEIGHT / BLOCK_GROUP_SIZE; y++)
            {
                String[] split = reader.readLine().split(" ");
                for (int x = 0; x < STD_WIDTH / BLOCK_GROUP_SIZE; x++)
                    subpaletteMappingList[x][y] = Integer.valueOf(split[x]);
            }


            for (int x = 0; x < STD_WIDTH; x += BLOCK_SIZE)
                for (int y = 0; y < STD_HEIGHT; y += BLOCK_SIZE)
                {
                    Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];
                    int row = x / BLOCK_SIZE;
                    int column = y / BLOCK_SIZE;

                    List<PixelColor> subpalette = subpaletteList
                            .get(subpaletteMappingList
                                         [row / (BLOCK_GROUP_SIZE / BLOCK_SIZE)]
                                         [column / (BLOCK_GROUP_SIZE / BLOCK_SIZE)]
                                );

                    for (int i = 0; i < BLOCK_SIZE; i++)
                        for (int j = 0; j < BLOCK_SIZE; j++)
                            mapping[i][j] =
                                    subpalette.indexOf(image[x + i][y + j]);

                    blockMappingList.add(new BlockMapping(row, column, mapping, subpalette));
                }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return blockMappingList;
    }
}
