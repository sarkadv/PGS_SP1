public class Block {

    private int resources;

    public Block(int resources) {
        this.resources = Math.max(resources, 0);
    }

    public int getResources() {
        return this.resources;
    }

    public void mineResource() {
        if (this.resources > 0) {
            this.resources--;
        }
    }
}
