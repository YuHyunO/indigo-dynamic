package mb.dnm.access.file;

public class FileNamePatternFilter {

    private final String fileNamePattern;
    private boolean requireCompleteMatch;
    private String startsWith;
    private String endsWith;


    public FileNamePatternFilter(String fileNamePattern) {
        this.fileNamePattern = (fileNamePattern == null || fileNamePattern.isEmpty()) ? "*" : fileNamePattern;

        int wildcardIdx = this.fileNamePattern.indexOf('*');
        if (wildcardIdx != -1) {
            startsWith = this.fileNamePattern.substring(0, wildcardIdx);
            endsWith = this.fileNamePattern.substring(wildcardIdx + 1);
        } else {
            requireCompleteMatch = true;
        }
    }

    public boolean accept(String fileName) {
        if (requireCompleteMatch) {
            return fileNamePattern.equals(fileName);
        } else {
            return fileName.startsWith(startsWith) && fileName.endsWith(endsWith);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileNamePatternFilter{");
        sb.append("fileNamePattern='").append(fileNamePattern).append('\'');
        sb.append(", requireCompleteMatch=").append(requireCompleteMatch);
        sb.append(", startsWith='").append(startsWith).append('\'');
        sb.append(", endsWith='").append(endsWith).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
