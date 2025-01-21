package de.klassenserver7b.k7bot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.klassenserver7b.k7bot.K7Bot;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TeacherDB {
    private JsonObject teachersList = null;

    public TeacherDB() {
    }

    /**
     * This method is used to load the Teachers List from the resources folder.
     * The Teachers List is a JSON file containing the teachers of the school.
     */
    public void loadTeachersList() {
        File file = new File("resources/teachers.json");
        if (!file.exists()) return;

        try {
            String jsonstring = Files.readString(file.toPath());

            JsonElement json = JsonParser.parseString(jsonstring);
            teachersList = json.getAsJsonObject();
        } catch (IOException e) {
            K7Bot.getInstance().getMainLogger()
                    .error("Could not load teachers file", e);
        }
    }

    /**
     * This method is used to get the teacher's JSON object from the teachers list.
     *
     * @param id The teacher's ID
     * @return The teacher's JSON object
     */
    @Nullable
    public JsonObject getTeacherJson(@Nullable String id) {
        if (id == null || id.isBlank()) return null;

        JsonElement element = teachersList.get(id);
        if (element == null || !element.isJsonObject()) return null;

        return element.getAsJsonObject();
    }

    /**
     * This method is used to get the teacher from the teachers list.
     *
     * @param id The teacher's ID
     * @return The teacher
     */
    @Nullable
    public Teacher getTeacher(@Nullable String id) {
        JsonObject teacherJson = getTeacherJson(id);
        if (teacherJson == null) return null;

        return new Teacher(teacherJson);
    }

    /**
     * This record is used to represent a teacher.
     */
    public record Teacher(JsonObject data) {

        /**
         * This method is used to get the teacher's full name.
         *
         * @return The teacher's full name
         */
        public String getFullName() {
            JsonElement element = data.get("full_name");
            if (element == null || !element.isJsonPrimitive()) return null;

            return element.getAsString();
        }

        /**
         * This method is used to check if the teacher is a doctor.
         *
         * @return True if the teacher is a doctor, otherwise false
         */
        public boolean isDoctor() {
            JsonElement element = data.get("is_doctor");
            return element != null && element.isJsonPrimitive() && element.getAsBoolean();
        }

        /**
         * This method is used to get the teacher's gender
         *
         * @return The teacher's gender
         */
        public String getGender() {
            JsonElement element = data.get("gender");
            if (element == null || !element.isJsonPrimitive()) return null;

            return element.getAsString();
        }

        /**
         * This method is used to get the teacher's decorated name.
         *
         * @return The teacher's decorated name
         */
        public String getDecoratedName() {
            StringBuilder name = new StringBuilder();

            String gender = getGender();
            if ("female".equalsIgnoreCase(gender)) name.append("Frau ");
            else if ("male".equalsIgnoreCase(gender)) name.append("Herr ");

            if (isDoctor()) name.append("Dr. ");

            name.append(getFullName());
            return name.toString();
        }
    }
}
