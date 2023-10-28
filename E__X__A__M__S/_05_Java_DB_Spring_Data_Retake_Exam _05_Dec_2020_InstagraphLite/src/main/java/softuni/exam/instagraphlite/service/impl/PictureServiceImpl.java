package softuni.exam.instagraphlite.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.instagraphlite.models.dto.PictureImportDTO;
import softuni.exam.instagraphlite.models.entity.Picture;
import softuni.exam.instagraphlite.repository.PictureRepository;
import softuni.exam.instagraphlite.service.PictureService;
import softuni.exam.instagraphlite.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PictureServiceImpl implements PictureService {

    private static final String PICTURES_FILE_PATH = "src/main/resources/files/pictures.json";
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtil validator;
    private final PictureRepository pictureRepository;

    @Autowired
    public PictureServiceImpl(ModelMapper modelMapper, Gson gson, ValidationUtil validator, PictureRepository pictureRepository) {
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validator = validator;
        this.pictureRepository = pictureRepository;
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readFromFileContent() throws IOException {
        return Files.readString(Path.of(PICTURES_FILE_PATH));
    }

    @Override
    public String importPictures() throws IOException {
        String json = this.readFromFileContent();

        PictureImportDTO[] importDTOs = this.gson.fromJson(json, PictureImportDTO[].class);

        return Arrays.stream(importDTOs)
                .map(this::importDTO)
                .collect(Collectors.joining("\n"));
    }

    private String importDTO(PictureImportDTO dto) {
        boolean isValid = this.validator.isValid(dto);

        if (!isValid) {
            return "Invalid Picture";
        }

        Optional<Picture> optPicture = this.pictureRepository.findByPath(dto.getPath());


        if (optPicture.isPresent()) {
            return "Invalid Picture";
        }


        Picture picture = this.modelMapper.map(dto, Picture.class);


        this.pictureRepository.save(picture);
        return String.format("Successfully imported Picture, with size %.2f", picture.getSize());
    }

    @Override
    public String exportPictures() {
        //Export all pictures with size bigger than 30000
        //Order the result by size ascending
        //Format the picture size to the 2nd digit after the floating point
        //Return the information in this format: {picSize} – {picPath}

        List<Picture> laptops = pictureRepository.findAllBySizeGreaterThanOrderBySizeAsc(30000);

        return laptops.stream()
                .map(Picture::toString)
                .collect(Collectors.joining("\n"));
    }
}
