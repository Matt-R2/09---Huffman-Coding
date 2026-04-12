import java.util.ArrayList;

public class PriorityQueue<E extends Comparable<E>> {
    private ArrayList<E> con;

    public PriorityQueue()
    {
        con = new ArrayList<>();
    }
    public boolean isEmpty() {
        return con.isEmpty();
    }

    public E front() {
        return con.get(0);
    }

    public E dequeue()
    {
        return con.remove(0);
    }

    public void enqueue(E val)
    {
        if(isEmpty() || val.compareTo(con.get(con.size() - 1)) >= 0)
        {
            con.add(val);
        }

        else {
            boolean keepIterating = true;
            for (int i = 0; i < con.size() && keepIterating; i++) {
                if (val.compareTo(con.get(i)) < 0) {
                    con.add(i, val);
                    keepIterating = false;
                }
            }
        }

    }
}
