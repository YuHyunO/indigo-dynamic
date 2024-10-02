package mb.dnm.core.dynamic.adaptersupport;

import java.util.LinkedList;

public class ClassPathBean {
    private LinkedList name = new LinkedList();
    private LinkedList pdName = new LinkedList();
    private LinkedList kind = new LinkedList();
    private LinkedList entry = new LinkedList();
    private LinkedList Specification_Title = new LinkedList();
    private LinkedList Specification_Version = new LinkedList();
    private LinkedList Implementation_Version = new LinkedList();

    public ClassPathBean() {
    }

    public void setName(String name) {
        this.name.add(name);
    }

    public void setPdName(String pdName) {
        this.pdName.add(pdName);
    }

    public void setKind(String kind) {
        this.kind.add(kind);
    }

    public void setEntry(String entry) {
        this.entry.add(entry);
    }

    public void setSpecification_Title(String Specification_Title) {
        this.Specification_Title.add(Specification_Title);
    }

    public void setSpecification_Version(String Specification_Version) {
        this.Specification_Version.add(Specification_Version);
    }

    public void setImplementation_Version(String Implementation_Version) {
        this.Implementation_Version.add(Implementation_Version);
    }

    public String[] getName() {
        return (String[])((String[])this.name.toArray(new String[0]));
    }

    public String[] getPdName() {
        return (String[])((String[])this.pdName.toArray(new String[0]));
    }

    public String[] getKind() {
        return (String[])((String[])this.kind.toArray(new String[0]));
    }

    public String[] getEntry() {
        return (String[])((String[])this.entry.toArray(new String[0]));
    }

    public String[] getSpecification_Title() {
        return (String[])((String[])this.Specification_Title.toArray(new String[0]));
    }

    public String[] getSpecification_Version() {
        return (String[])((String[])this.Specification_Version.toArray(new String[0]));
    }

    public String[] getImplementation_Version() {
        return (String[])((String[])this.Implementation_Version.toArray(new String[0]));
    }
}
