public class Block {

    private final int resources;

    public Block(int resources) {
        this.resources = Math.max(resources, 0);
    }

    public int getResources() {
        return this.resources;
    }
}
