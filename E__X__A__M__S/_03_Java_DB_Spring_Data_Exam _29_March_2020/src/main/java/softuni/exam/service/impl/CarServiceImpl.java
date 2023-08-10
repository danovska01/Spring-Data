package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.CarImportDTO;
import softuni.exam.models.entity.Car;
import softuni.exam.repository.CarRepository;
import softuni.exam.service.CarService;
import softuni.exam.util.ValidationUtilImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {

    private static final String CARS_FILE_PATH = "src/main/resources/files/json/cars.json";

    private final CarRepository carRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtilImpl validator;

    public CarServiceImpl(CarRepository carRepository, ModelMapper modelMapper, Gson gson, ValidationUtilImpl validator) {
        this.carRepository = carRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validator = validator;
    }

    @Override
    public boolean areImported() {
        return carRepository.count() > 0;
    }

    @Override
    public String readCarsFileContent() throws IOException {
        return Files.readString(Path.of(CARS_FILE_PATH));
    }

    @Override
    public String importCars() throws IOException {
        String json = this.readCarsFileContent();

        CarImportDTO[] importDTOs = this.gson.fromJson(json, CarImportDTO[].class);

        return Arrays.stream(importDTOs)
                .map(this::importDTO)
                .collect(Collectors.joining("\n"));
    }

    private String importDTO(CarImportDTO dto) {
        boolean isValid = this.validator.isValid(dto);

        if (!isValid) {
            return "Invalid Car";
        }
        //The combination of make, model and kilometers makes a car unique.
        Optional<Car> optCar = this.carRepository.findByMakeAndModelAndKilometers(dto.getMake(), dto.getModel(), dto.getKilometers());


        if (optCar.isPresent()) {
            return "Invalid Car";
        }

        Car car = this.modelMapper.map(dto, Car.class);
        this.carRepository.save(car);
        return String.format("Successfully imported Car %s - %s", car.getMake(), car.getModel());
    }

    @Override
    public String getCarsOrderByPicturesCountThenByMake() {
        return null;
    }
}
