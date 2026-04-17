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
import java.util.ArrayList;

/**
 * A priority queue implementation that maintians order based on priority and FIFO secondarily.
 * This implementation uses an arraylist as the back end.
 * @param <E> - the type of elements held in this queue, which must be comparable
 */
public class PriorityQueue<E extends Comparable<E>>
{
    //Underlying storage container for queue elements
    private ArrayList<E> con;

    /**
     * Initializes an empty PriorityQueue
     */
    public PriorityQueue()
    {
        con = new ArrayList<>();
    }

    /**
     * Checks if the queue contains no elements
     * @return - true if empty, false otherwise
     */
    public boolean isEmpty()
    {
        return con.isEmpty();
    }

    /**
     * Returns the number of elements currently in teh queue
     * @return - size of intenral storage container
     */
    public int size() {
        return con.size();
    }

    /**
     * Retrieves the element at the front of the queue, or next to be dequeued
     * @return - first element in the list
     */
    public E front() {
        return con.get(0);
    }

    /**
     * Retrieves and removes the element at the front of the queue
     * @return - the removed element
     */
    public E dequeue()
    {
        return con.remove(0);
    }

    /**
     * Adds an element to the queue. The element is inserted in the priority position for order
     * @param val - value to be added to the queue
     */
    public void enqueue(E val)
    {
        //Add element to end if empty or larger than current maximum
        if(isEmpty() || val.compareTo(con.get(con.size() - 1)) >= 0)
        {
            con.add(val);
        }

        else
        {
            //Linear search to find correct insertion point to maintain order
            boolean keepIterating = true;
            for (int i = 0; i < con.size() && keepIterating; i++)
            {
                if (val.compareTo(con.get(i)) < 0)
                {
                    con.add(i, val); //shifts elements and inserts
                    keepIterating = false;
                }
            }
        }
    }
}
