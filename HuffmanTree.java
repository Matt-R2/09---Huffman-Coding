import java.util.Map;

public class HuffmanTree
{
    private TreeNode root;

    public HuffmanTree(Map<Integer, Integer> frequencyMap)
    {
        this.root = buildTree(frequencyMap);
    }

    private TreeNode buildTree(Map<Integer, Integer> map)
    {
        PriorityQueue<TreeNode> queue = new PriorityQueue();

        for(Map.Entry<Integer, Integer> entry : map.entrySet())
        {
            TreeNode leaf = new TreeNode(entry.getKey(), entry.getValue());
            queue.enqueue(leaf);
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

    public TreeNode getRoot()
    {
        return this.root;
    }
}
