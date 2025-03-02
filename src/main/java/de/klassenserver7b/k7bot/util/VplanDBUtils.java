/**
 *
 */
package de.klassenserver7b.k7bot.util;

import de.klassenserver7b.k7bot.sql.LiteSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class VplanDBUtils {

    private static final Logger log;
    private static final List<String> defaultRooms;

    static {
        log = LoggerFactory.getLogger(VplanDBUtils.class);
        defaultRooms = List.of("005", "007", "013", "014", "019", "020", "203", "205", "214", "215", "220", "221");
    }

    /**
     * @param lesson the lesson to check
     * @param room   the room to check
     * @return if the room is free
     */
    public static boolean isRoomFree(long lesson, String room) {

        room = removeLeadingZero(room);

        try (ResultSet set = LiteSQL.onQuery("SELECT room FROM vplandata WHERE lesson=? ", lesson)) {

            boolean found = false;

            assert set != null;
            while (set.next()) {
                if (set.getString(1).equalsIgnoreCase(room)) {
                    found = true;
                    break;
                }
            }

            return !found;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public static List<String> checkDefaultRooms(long lesson) {
        List<String> ret = new ArrayList<>();

        for (String s : defaultRooms) {
            if (isRoomFree(lesson, s)) {
                ret.add(s);
            }
        }

        return ret;

    }

    public static HashMap<String, Long> getTeacherRooms(String teacher) {
        HashMap<String, Long> ret = new HashMap<>();

        try (ResultSet set = LiteSQL.onQuery("SELECT room, lesson FROM vplandata WHERE teacher=? ", teacher)) {
            assert set != null;
            while (set.next()) {
                ret.put(set.getString(1), set.getLong(2));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return ret;
    }

    public static String getTeacherRoomByLesson(String teacher, long lesson) {

        try (ResultSet set = LiteSQL.onQuery("SELECT room FROM vplandata WHERE teacher=? AND lesson=?", teacher, lesson)) {
            assert set != null;
            if (set.next()) {
                return set.getString(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String removeLeadingZero(String s) {
        if (s.startsWith("0")) {
            return s.substring(1);
        }
        return s;
    }

    /**
     * @author K7
     */
    public enum LessonMapper {

        Lesson1("07:30", "08:15", 45),
        Lesson2("08:25", "09:10", 45),
        Lesson3("09:30", "10:15", 45),
        Lesson4("10:25", "11:10", 45),
        Lesson5("11:20", "12:05", 45),
        Lesson6("12:15", "13:00", 45),
        Lesson7("13:30", "14:15", 45),
        Lesson8("14:20", "15:05", 45),
        Lesson9("15:10", "15:55", 45),
        Lesson10("16:00", "16:45", 45),
        Lesson_Short1("07:30", "08:00", 30),
        Lesson_Short2("08:10", "08:40", 30),
        Lesson_Short3("09:00", "09:30", 30),
        Lesson_Short4("09:40", "10:10", 30),
        Lesson_Short5("10:20", "10:50", 30),
        Lesson_Short6("11:00", "11:30", 30),
        Lesson_Short7("12:00", "12:30", 30),
        Lesson_Short8("12:35", "13:05", 30),
        Lesson_Short9("13:10", "13:40", 30),
        Lesson_Short10("13:45", "14:15", 30);

        public final String start;
        public final String end;
        public final int duration;

        LessonMapper(String start, String end, int dur) {
            this.start = start;
            this.end = end;
            this.duration = dur;
        }

    }
}
