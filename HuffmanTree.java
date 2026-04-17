/*  Student information for assignment:
 *
 *  On OUR honor, Matthew and Yusuf,
 *  this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1: Matthew Roach
 *  UTEID: mr67358
 *  email address: matthewr@utexas.edu
 *
 *  Student 2: Yusuf Marwat
 *  UTEID: ykm245
 *  email address: ykm245@eid.utexas.edu
 *
 *  Grader name: Brayden
 *  Section number: 52965
 */
import java.util.Map;

/**
 * Represents a Huffman tree used for compression and decompression. The tree is constructed
 * using an algorithm that repeatedly combines the two nodes with the lowest frequencies until
 * only the root remains
 */
public class HuffmanTree
{
    private TreeNode root;

    /**
     * Constructs a Huffman tree based ona  provided map of char frequencies
     *
     * @param frequencyMap - map where keys are character values
     */
    public HuffmanTree(Map<Integer, Integer> frequencyMap)
    {
        this.root = buildTree(frequencyMap);
    }

    /**
     * Algorithm for building the huffman tree. Uses a priority queue to ensure nodes with
     * smallest priority are combined into subtrees first.
     *
     * @param map - frequency data for the alphabet
     * @return - root node of the tree
     */
    private TreeNode buildTree(Map<Integer, Integer> map)
    {
        PriorityQueue<TreeNode> queue = new PriorityQueue();

        //Loops in Ascending Order
        for(int i = 0; i <= IHuffConstants.PSEUDO_EOF; i++)
        {
            if(map.containsKey(i))
            {
                TreeNode leaf = new TreeNode(i, map.get(i));
                queue.enqueue(leaf);
            }
        }

        //Then, we merge nodes until only the root remains
        while(queue.size() > 1)
        {
            //Dequeue the two nodes of the lowest frequencies
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();

            int sum = left.getFrequency()+ right.getFrequency();
            TreeNode parentNode = new TreeNode(-1, sum);

            //Connect Children
            parentNode.setRight(right);
            parentNode.setLeft(left);;

            queue.enqueue(parentNode);
        }
        return queue.dequeue();
    }

    /**
     * Returns the root node of the Huffman Tree
     * @return - TreeNode representing the root
     */
    public TreeNode getRoot()
    {
        return this.root;
    }
}
