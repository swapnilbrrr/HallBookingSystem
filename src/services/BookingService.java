package services;

import models.Hall;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private static final String HALLS_FILE = "data/halls.txt";

    public List<Hall> getAllHalls() {
        List<String> hallData = FileHandler.readLines(HALLS_FILE);
        List<Hall> halls = new ArrayList<>();

        for (String line : hallData) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] data = line.split(",");
            if (data.length == 5) {
                halls.add(new Hall(
                    data[0].trim(),
                    data[1].trim(),
                    data[2].trim(),
                    Integer.parseInt(data[3].trim()),
                    Double.parseDouble(data[4].trim())
                ));
            }
        }
        return halls;
    }
}