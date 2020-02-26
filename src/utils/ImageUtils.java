package utils;

import processing.PixelColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils
{

    public final static int STD_HEIGHT = 240;
    public final static int STD_WIDTH = 256;
    public final static int BLOCK_SIZE = 16;

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
        BufferedImage bufferedImage = new BufferedImage(STD_WIDTH, STD_HEIGHT, BufferedImage.TYPE_INT_RGB);

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
}
