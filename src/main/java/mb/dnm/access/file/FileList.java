package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter @Getter
public class FileList {
    private String baseDirectory;
    private List<String> fileList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\n")
                .append("\t\"baseDirectory\": \"").append(baseDirectory).append("\",\n");
        sb.append("\t\"fileList\": [\n");
        for (String file : fileList) {
            sb.append("\t\t\"").append(baseDirectory).append(file).append("\",\n");
        }
        if (fileList.size() > 0) {
            sb.setLength(sb.length() - ",\n".length());
        }
        sb.append("\n\t]\n");
        sb.append("}");

        return sb.toString();
    }

}
