package ai.impl;

import ai.EvolutionaryAlgorithm;
import ai.Individual;
import model.PixelColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SubpaletteEA extends EvolutionaryAlgorithm
{
    private final List<PixelColor> palette;
    private final PixelColor[][] image;

    public SubpaletteEA(PixelColor[][] image, List<PixelColor> palette)
    {
        this.image = image;
        this.palette = palette;
    }

    @Override
    public SubpaletteConfig run(int populationSize, int stagnationFactor, double mutationChance)
    {
        return (SubpaletteConfig) super.run(populationSize, stagnationFactor, mutationChance);
    }

    @Override
    protected Individual generateIndividual()
    {
        List<List<PixelColor>> subpaletteList = new ArrayList<>();

        for (int i = 0; i < 4; i++)
        {
            List<PixelColor> subpalette = new ArrayList<>();
            subpalette.add(0, PixelColor.black());

            while (subpalette.size() < 4)
            {
                PixelColor color = palette.get((int) (Math.random() * palette.size()));
                if (!subpalette.contains(color))
                    subpalette.add(color);
            }
            subpaletteList.add(subpalette);
        }

        return new SubpaletteConfig(subpaletteList, image);
    }

    @Override
    protected Individual select(List<Individual> population)
    {
        double topPercent = 3 / 4.0;
        int topPopulationSize = (int) (population.size() * topPercent);

        return population.stream()
                         .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
                         .limit(topPopulationSize)
                         .collect(Collectors.toList())
                         .get((int) (Math.random() * topPopulationSize));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Individual crossover(Individual mother, Individual father)
    {
        List<List<PixelColor>> subpaletteList = new ArrayList<>();
        List<List<PixelColor>> motherSubpaletteList = (List<List<PixelColor>>) mother.getSolution();
        List<List<PixelColor>> fatherSubpaletteList = (List<List<PixelColor>>) father.getSolution();

        subpaletteList.add(motherSubpaletteList.get(0));
        subpaletteList.add(fatherSubpaletteList.get(1));
        subpaletteList.add(motherSubpaletteList.get(2));
        subpaletteList.add(fatherSubpaletteList.get(3));

        return new SubpaletteConfig(subpaletteList, image);
    }
}
