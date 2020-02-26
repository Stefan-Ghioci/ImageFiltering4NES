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

    public Individual run(int populationSize, int stagnationFactor, double mutationChance)
    {
        LOGGER.info("Running evolutionary algorithm with {} individuals with {} stagnation factor",
                    populationSize,
                    stagnationFactor);

        List<Individual> population = generatePopulation(populationSize);


        Individual lastBest = null;
        int iterationCounter = 1;

        while (stagnationFactor > 0)
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

            double improvement = calculateImprovement(lastBest, newBest);

            if (improvement != 0)
            {
                lastBest = newBest;
                LOGGER.info("Iteration {}, best fitness {} with {}% improvement",
                            iterationCounter,
                            lastBest.getFitness(),
                            improvement);
            }
            else
            {
                stagnationFactor--;
                LOGGER.info("Iteration {}, stagnation", iterationCounter);
            }
            iterationCounter++;
        }

        return lastBest;
    }

    private double calculateImprovement(Individual lastBest, Individual newBest)
    {
        return Math.round((newBest.getFitness() - lastBest.getFitness()) / lastBest.getFitness() * 10000) / 100.0;
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
