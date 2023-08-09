package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.PassengerImportDTO;
import softuni.exam.models.entity.Passenger;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.PassengerRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.PassengerService;
import softuni.exam.util.ValidationUtilImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {

    private static final String PASSENGERS_FILE_PATH = "src/main/resources/files/json/passengers.json";
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtilImpl validator;
    private final PassengerRepository passengerRepository;
    private final TownRepository townRepository;

    public PassengerServiceImpl(PassengerRepository passengerRepository, TownRepository townRepository, ModelMapper modelMapper, Gson gson, ValidationUtilImpl validator) {
        this.passengerRepository = passengerRepository;
        this.townRepository = townRepository;
        this.gson = gson;
        this.validator = validator;
        this.modelMapper = modelMapper;
    }


    @Override
    public boolean areImported() {
        return passengerRepository.count() > 0;
    }

    @Override
    public String readPassengersFileContent() throws IOException {
        return Files.readString(Path.of(PASSENGERS_FILE_PATH));
    }

    @Override
    public String importPassengers() throws IOException {
        String json = this.readPassengersFileContent();

        PassengerImportDTO[] importDTOs = this.gson.fromJson(json, PassengerImportDTO[].class);

        return Arrays.stream(importDTOs)
                .map(this::importDTO)
                .collect(Collectors.joining("\n"));
    }

    private String importDTO(PassengerImportDTO dto) {

        boolean isValid = this.validator.isValid(dto);

        if (!isValid) {
            return "Invalid Passenger";
        }

        Optional<Passenger> optPassenger = this.passengerRepository.findByEmail(dto.getEmail());


        if (optPassenger.isPresent()) {
            return "Invalid Passenger";
        }
        Optional<Town> town = this.townRepository.findByName(dto.getTown());

        Passenger passenger = this.modelMapper.map(dto, Passenger.class);

        passenger.setTown(town.get());

        this.passengerRepository.save(passenger);
        return String.format("Successfully imported Passenger %s - %s", passenger.getLastName(), passenger.getEmail());


    }

    @Override
    public String getPassengersOrderByTicketsCountDescendingThenByEmail() {
        return null;
    }
}
