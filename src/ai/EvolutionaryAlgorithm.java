package ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class EvolutionaryAlgorithm
{
    static final Logger LOGGER = LoggerFactory.getLogger(EvolutionaryAlgorithm.class);

    public Individual run(int populationSize, int stagnationFactor, double mutationChance)
    {
        LOGGER.info("Running steady-state evolutionary algorithm with {} individuals and {} stagnation factor",
                    populationSize,
                    stagnationFactor);

        List<Individual> population = generatePopulation(populationSize);


        Individual best = population.get(0);
        int iterationCounter = 1;
        int stagnationTime = 0;

        population.forEach(Individual::evaluate);

        while (stagnationTime < stagnationFactor)
        {

            best = best(population);
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
                double improvement = calculateImprovement(best, newBest);

                if (improvement != 0)
                {
                    best = newBest;
                    stagnationTime = 0;
                    LOGGER.info("Iteration {}, best fitness {} with {}% improvement",
                                iterationCounter,
                                (int) newBest.getFitness(),
                                improvement);
                }
                else
                {
                    stagnationTime++;
                }
            }
            else
            {
                best = newBest;
                LOGGER.info("Iteration 1, initial best fitness {}", (int) newBest.getFitness());
            }
            iterationCounter++;
        }

        LOGGER.info("Algorithm ran for {} iterations, last best fitness {}", iterationCounter, (int) best.getFitness());
        return best;
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
                         .orElse(population.get(0));
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
                         .orElse(population.get(0));
    }

    protected abstract Individual generateIndividual();

    protected abstract Individual select(List<Individual> population);

    protected abstract Individual crossover(Individual mother, Individual father);
}
