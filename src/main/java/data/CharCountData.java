package data;

public class CharCountData {

    public final String modal;
    public final int count;

    public CharCountData(String model, int count) {
        this.modal = model;
        this.count = count;
    }

    @Override
    public String toString() {
        return modal + ", 字数: " + count;
    }
}
