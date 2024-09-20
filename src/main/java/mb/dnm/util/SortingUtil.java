package mb.dnm.util;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Strings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortingUtil {
    private SortingUtil() {}
    private static final Comparator STRING_DESC = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            return (o2.toString()).compareToIgnoreCase(o1.toString());
        }
    };
    private static final Comparator STRING_ASC = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            return (o1.toString()).compareToIgnoreCase(o2.toString());
        }
    };

    public static void sortList(List<?> list, Sorting sorting) {
        List<?> sortedList = new ArrayList<>();
        switch (sorting) {
            case ASC: Collections.sort(list, STRING_ASC); break;
            case DESC: Collections.sort(list, STRING_DESC); break;
        }
    }


    public enum Sorting {
        DESC,
        ASC;
    }
}
