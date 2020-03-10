package ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static model.Constants.CLUSTER_COUNT;

public abstract class KMeansClusteringAlgorithm<Entity>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(KMeansClusteringAlgorithm.class);
    private final static int CONVERGENCE_THRESHOLD = 100;

    public List<List<Entity>> run(List<Entity> entities, int iterationCount)
    {
        LOGGER.info("Running K-means clustering for {} blocks into {} clusters for {} iterations",
                    entities.size(),
                    CLUSTER_COUNT,
                    iterationCount);


        boolean anyClusterModified;
        List<List<Entity>> bestClusteredEntitiesList = null;
        double minCost = -1;

        for (int i = 0; i < iterationCount; i++)
        {
            Map<Entity, List<Entity>> clusterMap = generateClusterMap();
            int loopCounter = 0;

            do
            {
                anyClusterModified = false;

//            LOGGER.info("Clustering entities...");
                for (Entity entity : entities)
                {
                    Entity nearestCentroid = clusterMap.keySet().iterator().next();
                    double minDistance = calculateDistance(entity, nearestCentroid);

                    for (Entity centroid : clusterMap.keySet())
                    {
                        double distance = calculateDistance(entity, centroid);

                        if (distance < minDistance)
                        {
                            minDistance = distance;
                            nearestCentroid = centroid;
                        }
                    }

                    clusterMap.get(nearestCentroid).add(entity);
                }

                List<Entity> newCentroids = new ArrayList<>();
//                int reCentered = 0;

//            LOGGER.info("Averaging centroids for clustered entities...");

                for (Map.Entry<Entity, List<Entity>> entry : clusterMap.entrySet())
                {
                    Entity centroid = entry.getKey();
                    List<Entity> cluster = entry.getValue();

                    if (cluster.size() != 0)
                    {
                        Entity newCentroid = center(cluster);

                        if (calculateDistance(newCentroid, centroid) > 0)
                        {
                            anyClusterModified = true;
//                            reCentered++;
                        }
                        newCentroids.add(newCentroid);
                    }
                    else
                        newCentroids.add(centroid);
                }

                if (anyClusterModified)
                {
//                    LOGGER.info("{} centroids re-centered ", reCentered);
                    loopCounter++;
                    clusterMap = initializeClusterMap(newCentroids);
                }
            }
            while (anyClusterModified && loopCounter < CONVERGENCE_THRESHOLD);

            if (loopCounter >= CONVERGENCE_THRESHOLD)
            {
                LOGGER.info("Could not converge centroids on iteration {} :(", i);
                continue;
            }
            List<List<Entity>> clusteredEntitiesList = new ArrayList<>(clusterMap.values());

            double cost = computeCost(clusteredEntitiesList);
            if (minCost == -1 || minCost > cost)
            {
                minCost = cost;
                bestClusteredEntitiesList = clusteredEntitiesList;

                LOGGER.info("Centroids converged after {} re-centerings on iteration {}, new best fitness {}",
                            loopCounter,
                            i,
                            (int) cost);
            }
            else
                LOGGER.info("Centroids converged after {} re-centerings on iteration {}, fitness {}",
                            loopCounter,
                            i,
                            (int) cost);
        }

        assert bestClusteredEntitiesList != null;
        return bestClusteredEntitiesList.stream()
                                        .filter(cluster -> !cluster.isEmpty())
                                        .collect(Collectors.toList());
    }

    protected abstract double computeCost(List<List<Entity>> clusteredEntitiesList);

    private Map<Entity, List<Entity>> initializeClusterMap(List<Entity> centroids)
    {
        return centroids.stream()
                        .collect(Collectors.toMap(centroid -> centroid,
                                                  centroid -> new ArrayList<>(),
                                                  (a, b) -> b));
    }

    private Map<Entity, List<Entity>> generateClusterMap()
    {
        return IntStream.range(0, CLUSTER_COUNT)
                        .boxed()
                        .collect(Collectors.toMap(i -> generateCentroid(),
                                                  i -> new ArrayList<>(),
                                                  (a, b) -> b));
    }

    protected abstract Entity center(List<Entity> cluster);

    protected abstract double calculateDistance(Entity entity, Entity centroid);

    protected abstract Entity generateCentroid();
}
