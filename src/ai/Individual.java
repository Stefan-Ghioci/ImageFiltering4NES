package ai;

public abstract class Individual
{

    private double fitness;

    protected Individual(double fitness)
    {
        this.fitness = fitness;
    }

    double getFitness()
    {
        return fitness;
    }

    abstract protected void evaluate();

    abstract protected void mutate();
}
