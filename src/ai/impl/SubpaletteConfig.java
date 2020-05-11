package ai.impl;

import ai.Individual;
import model.PixelColor;
import utils.ColorMathUtils;

import java.util.List;
import java.util.stream.Collectors;

import static model.Constants.*;

public class SubpaletteConfig implements Individual
{
    private final PixelColor[][] image;
    private final List<List<PixelColor>> subpaletteList;
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
        for (int x = 0; x < STD_WIDTH; x += TILE_GROUP_SIZE)
            for (int y = 0; y < STD_HEIGHT; y += TILE_GROUP_SIZE)
            {
                double minDiffSum = ColorMathUtils.getMinDistanceSumPerTileGroup(x, y, subpaletteList, image);

                fitness += minDiffSum;
            }

        fitness = fitness / (STD_WIDTH * STD_HEIGHT);
    }


    @Override
    public void mutate()
    {
        // generate subpalette with one color from each of the other subpalettes

        List<PixelColor> subpalette = subpaletteList.stream()
                                                    .map(element -> element.get((int) (Math.random() * 4)))
                                                    .collect(Collectors.toList());

        if (!subpalette.contains(PixelColor.black()))
            subpalette.remove(subpalette.size() - 1);
        else
            subpalette.remove(PixelColor.black());
        subpalette.add(0, PixelColor.black());

        subpaletteList.remove((int) (Math.random() * 4));
        subpaletteList.add(subpalette);
    }

    @Override
    public List<List<PixelColor>> getSolution()
    {
        return subpaletteList;
    }
}
