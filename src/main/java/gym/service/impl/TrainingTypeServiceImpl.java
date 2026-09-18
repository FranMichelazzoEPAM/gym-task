package gym.service.impl;

import gym.domain.TrainingType;
import gym.repository.TrainingTypeRepository;
import gym.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        LOG.debug("Getting all training types");
        return trainingTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingType getTrainingTypeByName(String name) {
        LOG.debug("Getting training type: {}", name);
        return trainingTypeRepository.findByTrainingTypeName(name)
                .orElseThrow(() -> new NoSuchElementException("Training type '" + name + "' not found."));
    }
}