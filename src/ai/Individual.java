package ai;

public interface Individual
{
    double getFitness();

    void evaluate();

    void mutate();

    Object getSolution();
}
