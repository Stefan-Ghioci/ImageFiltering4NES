package ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class KMeansClustering<Entity>
{
    final static Logger LOGGER = LoggerFactory.getLogger(KMeansClustering.class);

    public List<List<Entity>> run(List<Entity> entities, int clusterCount, int iterationCount)
    {
        LOGGER.info("Running K-means clustering for {} blocks into {} clusters for {} iterations",
                    entities.size(),
                    clusterCount,
                    iterationCount);

        Map<Entity, List<Entity>> clusterMap = generateClusterMap(clusterCount);

        boolean anyClusterModified;

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
            int reCentered = 0;

//            LOGGER.info("Averaging centroids for clustered entities...");

            for (Map.Entry<Entity, List<Entity>> entry : clusterMap.entrySet())
            {

                Entity centroid = entry.getKey();
                List<Entity> cluster = entry.getValue();

                if (cluster.size() != 0)
                {
                    Entity newCentroid = calculateAverage(cluster);

                    if (calculateDistance(newCentroid, centroid) > 0)
                    {
                        anyClusterModified = true;
                        reCentered++;
                    }
                    newCentroids.add(newCentroid);
                }
                else
                    newCentroids.add(centroid);
            }

            if (anyClusterModified)
            {
                LOGGER.info("{} centroids re-centered ", reCentered);
                clusterMap = initializeClusterMap(newCentroids);
            }
            else
                LOGGER.info("Centroids converged, iteration finished!");

            //TODO multiple iterations, keep best (compare vs original image)
        }
        while (anyClusterModified);

        return new ArrayList<>(clusterMap.values());
    }


    private Map<Entity, List<Entity>> initializeClusterMap(List<Entity> centroids)
    {
        return centroids.stream()
                        .collect(Collectors.toMap(centroid -> centroid, centroid -> new ArrayList<>(), (a, b) -> b));
    }

    protected abstract Entity calculateAverage(List<Entity> cluster);

    protected abstract double calculateDistance(Entity entity, Entity centroid);

    private Map<Entity, List<Entity>> generateClusterMap(int clusterCount)
    {
        return IntStream.range(0, clusterCount)
                        .boxed()
                        .collect(Collectors.toMap(i -> generateCentroid(), i -> new ArrayList<>(), (a, b) -> b));
    }

    protected abstract Entity generateCentroid();
}
