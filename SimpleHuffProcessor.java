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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor
{

    private IHuffViewer myViewer;
    private Map<Integer, String> codes;
    private Map<Integer, Integer> frequencies;
    private TreeNode myRoot;
    private int myOriginalSize;
    private int myCompressedSize;
    private int headerFormat;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException
    {
        this.headerFormat = headerFormat;

        //Build Frequency Table
        BitInputStream bitIn = new BitInputStream(in);
        Map<Integer, Integer> freq = new HashMap<>();
        int totalBitsRead = 0;
        int val = 0;
        while((val = bitIn.readBits(BITS_PER_WORD)) != -1)
        {
            if(!freq.containsKey(val))
            {
                freq.put(val, 1);
            }
            else
            {
                freq.put(val, freq.get(val) + 1);
            }
            totalBitsRead += BITS_PER_WORD;
        }
        myOriginalSize = totalBitsRead;
        //Add Pseudo-EOF
        freq.put(PSEUDO_EOF, 1);
        frequencies = freq;

        //Build Tree
        HuffmanTree tree = new HuffmanTree(freq);
        myRoot = tree.getRoot();

        //Get Codes
        codes = new HashMap<>();
        generateCodes(myRoot, "", codes);

        //Calculate compressed size
        myCompressedSize = calculateCompressedSize(freq, headerFormat, codes);

        bitIn.close();
        return myOriginalSize - myCompressedSize;
    }

    /**
     * Recursively traverses the Huffman tree to generate binary econdings for each leaf node.
     * For every left, a  0 is added, for every right, a 1 is added
     *
     * @param node - the current node being visited in the tree
     * @param path - the combined string of bits representing the path from the root
     * @param map - table where char values are mapped to their huffman codes
     */
    private void generateCodes(TreeNode node, String path, Map<Integer, String> map)
    {
        if(node != null)
        {
            //Check if current node is a leaf
            if(node.getLeft() == null && node.getRight() == null)
            {
                map.put(node.getValue(), path);
            }
            else
            {
                //Traverse Left, Traverse Right appending 0 or 1 based on direction
                generateCodes(node.getLeft(), path + "0", map);
                generateCodes(node.getRight(), path + "1", map);
            }
        }
    }

    /**
     * Calculates the total number of bits the compressed file will occupy.
     * Includes magic number, header type, header data, encoded data, and Pseudo EOF
     *
     * @param freq - map of character values to their frequencies
     * @param header - constant representing which header format is being used
     * @param codes - map of character values to their generated huffman bit strings
     * @return - total bit count of resulting file
     */
    private int calculateCompressedSize
            (Map<Integer, Integer> freq, int header, Map<Integer, String> codes)
    {
        //Magic Number + Header ID (32bits each)
        int totalBits = BITS_PER_INT + BITS_PER_INT;

        if(header == STORE_COUNTS)
        {
            //Add size for standard frequency table of 256ints
            totalBits += ALPH_SIZE * BITS_PER_INT;
        }
        else if(header == STORE_TREE)
        {
            //Add size for tree header- 32 bit + the recursive tree bit count
            totalBits += BITS_PER_INT + countTreeHeaderBits(myRoot);
        }

        //Calculate the size of the actual encoded content
        for(int charVal : freq.keySet())
        {
            String code = codes.get(charVal);
            int frequency = freq.get(charVal);
            totalBits += (frequency * code.length());
        }

        return totalBits;
    }

    /**
     * Calculates the total number of bits required to store the huffman tree structure
     * in the file header using the STORE_TREE format
     *
     * @param node - the root of the huffman tree being measuered
     * @return - total bit count required to represent the tree's leaf vals
     */
    private int countTreeHeaderBits(TreeNode node)
    {
        int[] count = {0};
        countBitsHelper(node, count);
        return count[0];
    }

    /**
     * Recursive helper method that traverses the tree to count bits. Internal nodes contribute 1
     * bit, while leaf nodes contribute 1 bit plus the bits required for the char value (9)
     *
     * @param node - current node being accessed
     * @param count - an array of size 1 acting as a mutable integer for tracking total bits
     */
    private void countBitsHelper(TreeNode node, int[] count)
    {
        if(node != null)
        {
            //Every node visited adds 1 bit to represent the node itself
            count[0] += 1;

            if(node.getLeft() == null && node.getRight() == null)
            {
                //Leaf nodes add the size of the character value (8 bits)
                count[0] += (BITS_PER_WORD + 1);
            }
            else
            {
                //Process the left and right subtrees recursively
                countBitsHelper(node.getLeft(), count);
                countBitsHelper(node.getRight(), count);
            }
        }
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException
    {
        //Check if compression is actually beneficial
        if(!force && (myOriginalSize - myCompressedSize) < 0)
        {
            return 0;
        }

        BitOutputStream bitOut = new BitOutputStream(out);

        int bitsWritten = writeHeader(bitOut);

        //Iterate over the file and translate bytes into huffman codes
        BitInputStream bitIn = new BitInputStream(in);
        int val = 0;
        while((val = bitIn.readBits(BITS_PER_WORD)) != -1)
        {
            String code = codes.get(val);
            //String of bits turns into an integer for output stream
            writeStringOneAtTime(code, bitOut);
            bitsWritten += code.length();
        }
        bitIn.close();

        //Write the Pseudo EOF
        String eofCode = codes.get(PSEUDO_EOF);
        writeStringOneAtTime(eofCode, bitOut);
        bitsWritten += eofCode.length();

        bitOut.close();
        return bitsWritten;
    }

    /**
     *  Helper method to read in bits one at a time
     *
     * @param code - string rep of the binary huffman code
     * @param bitOut - where bits will be written to
     */
    private void writeStringOneAtTime(String code, BitOutputStream bitOut)
    {
        for(int i = 0; i < code.length(); i++)
        {
            int bit = (code.charAt(i) == '0') ? 0 : 1;
            bitOut.writeBits(1, bit);
        }
    }

    /**
     *  Writes the compression data to the start of the bitstream including the
     *  magic number, header format type, and specific data needed to rebuild the tree later
     *
     * @param bitOut - the bitOutputStream to which the header is written
     * @return - total number of bits written to the stream
     */
    private int writeHeader(BitOutputStream bitOut)
    {
        int bits = 0;
        bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        bitOut.writeBits(BITS_PER_INT, headerFormat);
        bits += (2 * BITS_PER_INT);

        if (headerFormat == STORE_COUNTS)
        {
            for (int i = 0; i < ALPH_SIZE; i++)
            {
                if(frequencies.containsKey(i))
                {
                    bitOut.writeBits(BITS_PER_INT, frequencies.get(i));
                }
               else
                {
                    bitOut.writeBits(BITS_PER_INT, 0);
                }
                bits += BITS_PER_INT;
            }
        }
        else
        {
            bitOut.writeBits(BITS_PER_INT, countTreeHeaderBits(myRoot));
            bits += BITS_PER_INT + writeTreeHeader(myRoot, bitOut);
        }
        return bits;
    }

    /**
     * Recursively writes the huffman tree structure to the output stream.
     * Internal nodes are marked with a 0 bit and leaf nodes are marked with 1 bit + 9
     *
     * @param node - current node of the tree being written
     * @param out - where the tree bits will be written
     * @return - total number of bits written during this call and its children
     */
    private int writeTreeHeader(TreeNode node, BitOutputStream out)
    {
        int bitsWritten = 0;
        if(node != null)
        {
            if(node.getLeft() == null && node.getRight() == null)
            {
                //Write 1 bit for a single leaf
                out.writeBits(1,1);
                //Write 9 bits for the character value
                out.writeBits(BITS_PER_WORD + 1, node.getValue());

                bitsWritten = 1 + (BITS_PER_WORD + 1);
            }
            else
            {
                //It's an internal node
                out.writeBits(1, 0);

                //Recurse left and right
                int left = writeTreeHeader(node.getLeft(), out);
                int right = writeTreeHeader(node.getRight(), out);

                bitsWritten = 1 + left + right;
            }
        }
        return bitsWritten;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException
    {
        int bitsWritten = 0;
        TreeNode root;
        BitInputStream input = new BitInputStream(in);
        BitOutputStream output = new BitOutputStream(out);
        int magicNum = input.readBits(BITS_PER_INT);

        //Verify Magic Number is in file
        if (magicNum != MAGIC_NUMBER)
        {
            throw new IOException("Magic Number does not match");
        }

        //Identify and Process the Header
        int whatStore = input.readBits(BITS_PER_INT);
        if (whatStore == STORE_COUNTS)
        {
            root = buildTreeSCF(input).getRoot();
        }
        else if (whatStore == STORE_TREE)
        {
            root = buildTreeSTF(input);
        }
        else
        {
            throw new IOException("Header was not stored correctly");
        }
        //Decode the body data
        bitsWritten += decode(input, output, root);
        //Close streams after decoding is complete
        input.close();
        output.close();
        return bitsWritten;
    }


    /**
     * Rebuilds the Huffman tree using teh store_counts format (frequency table)
     *
     * @param input - the bitstream currently positioned at the start of the counts table
     * @return - A huffman tree object reconstructed from the counts
     * @throws IOException
     */
    private HuffmanTree buildTreeSCF(BitInputStream input) throws IOException
    {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < ALPH_SIZE; i++)
        {
            int freq = input.readBits(BITS_PER_INT);
            if (freq > 0)
            {
                freqMap.put(i, freq);
            }
        }
        // PSEUDO EOF VALUE
        freqMap.put(PSEUDO_EOF, 1);
        return new HuffmanTree(freqMap);
    }

    /**
     * Reads the tree size and initiates the recursive tree reconstruction
     *
     * @param input - the bitstream positioned at the STORE_TREE header
     * @return - the root of the reconstructed huffman tree
     * @throws IOException
     */
    private TreeNode buildTreeSTF(BitInputStream input) throws IOException
    {
        input.readBits(BITS_PER_INT);
        return buildTreeSTFHelper(input);
    }

    /**
     * Recursive Helper to rebuild the tree from pre order bit representation
     *
     * @param input - bitstream object
     * @return - the root of the reconstructed tree
     * @throws IOException
     */
    private TreeNode buildTreeSTFHelper(BitInputStream input) throws IOException
    {
        int bit = input.readBits(1);
        if (bit == -1)
        {
            throw new IOException("No PSEUDO - EOF Value");
        }
        else if (bit == 0) //Internal node
        {
            TreeNode left = buildTreeSTFHelper(input);
            TreeNode right = buildTreeSTFHelper(input);
            return new TreeNode(left, -1, right);
        }
        else //Leaf Node (bit = 1)
        {
            int num = input.readBits(BITS_PER_WORD + 1);
            return new TreeNode(num, 1);
        }
    }

    /**
     * Uses teh Huffman tree to decode the compressed bits back into original bytes
     *
     * @param input - compressed bitstream
     * @param output - the stream where decoded bytes are written
     * @param root - the root of the huffman tree
     * @return - the total number of bits written to the output file
     * @throws IOException
     */
    private int decode(BitInputStream input, BitOutputStream output, TreeNode root)
            throws IOException
    {
        // get ready to walk tree, start at root
        int bitsToOutput = 0;
        TreeNode curr = root;
        boolean done = false;
        while (!done) {
            int bit = input.readBits(1);
            if (bit == -1)
            {
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            }
            else
            {
                // move left or right in tree based on value of bit
                if (bit == 0)
                {
                    curr = curr.getLeft();
                }
                else
                {
                    curr = curr.getRight();
                }
                if (curr.isLeaf())
                {
                    if (curr.getValue() == PSEUDO_EOF)
                    {
                        done = true;
                    }
                    else
                    {
                        // write out value in leaf to output
                        output.writeBits(BITS_PER_WORD, curr.getValue());
                        bitsToOutput += BITS_PER_WORD;
                        curr = root;
                    }
                }
            }
        }
        return bitsToOutput;
    }


    public void setViewer(IHuffViewer viewer)
    {
        myViewer = viewer;
    }

    private void showString(String s)
    {
        if (myViewer != null)
        {
            myViewer.update(s);
        }
    }
}
