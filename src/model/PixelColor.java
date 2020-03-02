package model;

import java.util.Objects;

public class PixelColor
{
    private final int red;

    private final int blue;
    private final int green;
    public PixelColor(int red, int green, int blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public PixelColor(int rgb)
    {

        this.red = (rgb & 0xff0000) >> 16;
        this.green = (rgb & 0xff00) >> 8;
        this.blue = rgb & 0xff;
    }

    public static PixelColor BLACK()
    {
        return new PixelColor(0, 0, 0);
    }

    public static PixelColor difference(PixelColor oldPixel, PixelColor newPixel)
    {
        return new PixelColor(oldPixel.red - newPixel.red,
                              oldPixel.green - newPixel.green,
                              oldPixel.blue - newPixel.blue);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixelColor that = (PixelColor) o;
        return red == that.red &&
               blue == that.blue &&
               green == that.green;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(red, blue, green);
    }

    public int toInt()
    {
        int rgb;
        rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }

    public int getRed()
    {
        return red;
    }

    public int getBlue()
    {
        return blue;
    }

    public int getGreen()
    {
        return green;
    }
}
