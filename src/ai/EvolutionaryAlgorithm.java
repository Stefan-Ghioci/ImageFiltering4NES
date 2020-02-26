package ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class EvolutionaryAlgorithm
{
    final static Logger LOGGER = LoggerFactory.getLogger(EvolutionaryAlgorithm.class);

    public Object run(int populationSize, int stagnationFactor, double mutationChance)
    {
        LOGGER.info("Running evolutionary algorithm with {} individuals with {} stagnation factor",
                    populationSize,
                    stagnationFactor);

        List<Individual> population = generatePopulation(populationSize);


        Individual lastBest = null;
        int iterationCounter = 1;
        int stagnationTime = 0;

        while (stagnationTime < stagnationFactor)
        {
            population.forEach(Individual::evaluate);

            lastBest = best(population);
            Individual mother = select(population);
            Individual father = select(population);

            Individual offspring = crossover(mother, father);

            if (Math.random() < mutationChance)
                offspring.mutate();

            offspring.evaluate();

            Individual worst = worst(population);

            if (offspring.getFitness() < worst.getFitness())
            {
                population.remove(worst);
                population.add(offspring);
            }


            Individual newBest = best(population);

            if (iterationCounter != 1)
            {
                double improvement = calculateImprovement(lastBest, newBest);

                if (improvement != 0)
                {
                    lastBest = newBest;
                    stagnationTime = 0;
                    LOGGER.info("Iteration {}, best fitness {} with {}% improvement",
                                iterationCounter,
                                lastBest.getFitness(),
                                improvement);
                }
                else
                {
                    stagnationTime++;
//                    LOGGER.info("Iteration {}, stagnation", iterationCounter);
                }
            }
            else
            {
                lastBest = newBest;
                LOGGER.info("Iteration 1, initial best fitness {}", lastBest.getFitness());
            }
            iterationCounter++;
        }

        return lastBest != null ? lastBest.getSolution() : null;
    }

    private double calculateImprovement(Individual lastBest, Individual newBest)
    {
        return (int) (Math.round((lastBest.getFitness() - newBest.getFitness()) / lastBest.getFitness() * 10000))
               / 100.0;
    }


    private Individual worst(List<Individual> population)
    {
        return population.stream()
                         .max(Comparator.comparingDouble(Individual::getFitness))
                         .orElse(null);
    }

    private List<Individual> generatePopulation(int populationSize)
    {
        return IntStream.range(0, populationSize)
                        .mapToObj(i -> generateIndividual())
                        .collect(Collectors.toList());
    }


    private Individual best(List<Individual> population)
    {
        return population.stream()
                         .min(Comparator.comparingDouble(Individual::getFitness))
                         .orElse(null);
    }

    protected abstract Individual generateIndividual();

    protected abstract Individual select(List<Individual> population);

    protected abstract Individual crossover(Individual mother, Individual father);
}
