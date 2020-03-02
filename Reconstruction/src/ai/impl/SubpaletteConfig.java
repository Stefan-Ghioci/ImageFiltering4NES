package ai.impl;

import ai.Individual;
import processing.PixelColor;
import utils.ColorMathUtils;

import java.util.List;
import java.util.stream.Collectors;

import static utils.ImageUtils.*;

public class SubpaletteConfig implements Individual
{
    private PixelColor[][] image;
    private List<List<PixelColor>> subpaletteList;
    double fitness;

    public SubpaletteConfig(List<List<PixelColor>> subpaletteList, PixelColor[][] image)
    {
        this.subpaletteList = subpaletteList;
        this.image = image;
        fitness = 0;
    }


    @Override
    public double getFitness()
    {
        return fitness;
    }

    @Override
    public void evaluate()
    {
        for (int x = 0; x < STD_WIDTH; x += BLOCK_SIZE)
            for (int y = 0; y < STD_HEIGHT; y += BLOCK_SIZE)
            {
                double minDiffSum = ColorMathUtils.getMinDiffSumPerBlock(x, y, subpaletteList, image);

                fitness += (minDiffSum / (BLOCK_SIZE * BLOCK_SIZE));
            }
        int blocks = (STD_HEIGHT * STD_WIDTH) / (BLOCK_SIZE * BLOCK_SIZE);
        fitness = fitness / blocks;
    }


    @Override
    public void mutate()
    {
        // generate subpalette with one color from each of the other subpalettes

        List<PixelColor> subpalette = subpaletteList.stream()
                                                    .map(element -> element.get((int) (Math.random() * 4)))
                                                    .collect(Collectors.toList());

        if (!subpalette.contains(PixelColor.BLACK()))
            subpalette.remove(subpalette.size() - 1);
        else
            subpalette.remove(PixelColor.BLACK());
        subpalette.add(0, PixelColor.BLACK());

        subpaletteList.remove((int) (Math.random() * 4));
        subpaletteList.add(subpalette);
    }

    @Override
    public List<List<PixelColor>> getSolution()
    {
        return subpaletteList;
    }
}
