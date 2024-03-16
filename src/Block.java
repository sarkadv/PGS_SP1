/**
 * Class represents a single block that can contain more resources.
 */
public class Block {

    /** number of resources the block contains */
    private final int resources;

    /**
     * Creates a Block instance.
     * @param resources     number of resources the block contains
     */
    public Block(int resources) {
        this.resources = Math.max(resources, 0);    // number of resources has to be positive
    }

    public int getResources() {
        return this.resources;
    }
}
