package utils;

import model.BlockMapping;
import model.PixelColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            BufferedImage bufferedImage = ImageIO.read(new File("images/" + filename + ".bmp"));

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

    public static void saveFile(PixelColor[][] image, String filename)
    {
        BufferedImage
                bufferedImage =
                new BufferedImage(STD_WIDTH, STD_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < STD_WIDTH; x++)
            for (int y = 0; y < STD_HEIGHT; y++)
            {
                bufferedImage.setRGB(x, y, image[x][y].toInt());
            }

        File outputFile = new File("generated/" + filename + ".bmp");
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

    public static List<BlockMapping> loadGeneratedBlockMappings(String filename)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader("generated/" + filename + ".txt")))
        {
            List<BlockMapping> blockMappings = new ArrayList<>();
            List<List<PixelColor>> subpaletteList = new ArrayList<>();

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

            //TODO
            BufferedImage bufferedImage = ImageIO.read(new File("images/" + filename + ".bmp"));

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
