package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * <code>FileList</code> 탐색을 시작한 최상위 디렉터리 경로(baseDirectory)와 탐색한 파일, 디렉터리 경로에 대한 결과를 담고있는 객체이다.<br>
 * 탐색을 시작한 최상위 디렉터리는 <code>setBaseDirectory(String)</code> 메소드와 <code>getBaseDirectory()</code> 메소드를 통해 값 지정과 값에 대한 접근이 가능하다.
 * <code>filiList</code> 는 탐색을 시작한 최상위 경로인 <code>baseDirectory</code> 를 제외한 파일 및 디렉터리의 경로를 담고있는 변수이다.
 * <code>baseDirectory</code>는 경로의 끝이 파일 구분자로 마무리되어야 하며, <code>filiList</code> 의 원소들 즉, <code>baseDirectory</code> 하부의 파일 또는 디렉터리의 경로들은 경로의 시작에 파일구분자가 있으면 안된다.
 * 또 각 원소가 파일인 경우 경로의 끝이 파일 구분자 없이 끝나야하며, 디렉터리인 경우에는 파일구분자로 마무리되어야 한다.<br>
 * 하지만 이 객체는 이 규칙을 강제하지 않는다.
 * 이 객체를 활용하여 개발하려는 서비스의 유형에 따라 적절한 규칙을 정한 사용이 필요하다.
 *
 * @author Yuhyun O
 * @version 2024.09.15
 *
 * */
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
