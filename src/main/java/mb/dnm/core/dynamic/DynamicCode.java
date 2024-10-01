package mb.dnm.core.dynamic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
class DynamicCode {
    private String namespace;
    private String codeId;
    private String source;
    private List<String> imports;

    DynamicCode() {
        imports = new ArrayList<>();
    }

    void addImport(Class importClass) {
        imports.add("import " + importClass.getName() + ";");
    }

}
