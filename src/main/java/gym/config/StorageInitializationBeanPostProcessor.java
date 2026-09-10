package gym.config;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class StorageInitializationBeanPostProcessor implements BeanPostProcessor {

    private final CsvDataLoader csvDataLoader = new CsvDataLoader();
    private final Environment environment;

    @Autowired
    public StorageInitializationBeanPostProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        try {
            switch (beanName) {

                case "traineeStorage" -> {
                    Map<UUID, Trainee> storage = castMap(bean);

                    String path = environment.getProperty("trainee.file.path");

                    List<Trainee> trainees =
                            csvDataLoader.loadTrainees(toInputStream(path));

                    for (Trainee trainee : trainees) {
                        storage.put(trainee.getUserId(), trainee);
                    }
                }

                case "trainerStorage" -> {
                    Map<UUID, Trainer> storage = castMap(bean);

                    String path = environment.getProperty("trainer.file.path");

                    List<Trainer> trainers =
                            csvDataLoader.loadTrainers(toInputStream(path));

                    for (Trainer trainer : trainers) {
                        storage.put(trainer.getUserId(), trainer);
                    }
                }

                case "trainingStorage" -> {
                    Map<UUID, Training> storage = castMap(bean);

                    String path = environment.getProperty("training.file.path");

                    List<Training> trainings =
                            csvDataLoader.loadTrainings(toInputStream(path));

                    for (Training training : trainings) {
                        storage.put(training.getTrainingId(), training);
                    }
                }

                default -> {
                    // Not a storage bean.
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to initialize storage bean: " + beanName,
                    e
            );
        }

        return bean;
    }

    private InputStream toInputStream(String filePath) throws IOException {
        return new ClassPathResource(filePath).getInputStream();
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> castMap(Object bean) {
        return (Map<K, V>) bean;
    }
}