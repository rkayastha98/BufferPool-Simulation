package Project1;

import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner; // Import the Scanner class to read text files

public class Main {
    public static void main(String[] args) {
        // Creation and initialization of buffer pool and frames
        BufferPool bP = new BufferPool();
        bP.initialize(Integer.parseInt(args[0]));

        System.out.println("The program is ready for the next command");

        Scanner myObj = new Scanner(System.in); // Create a Scanner object

        // Loop to allow the input of commands infintely
        while (true) {
            // splits the command line entered into the command itself and the parameters
            String[] command = myObj.nextLine().split(" ", 3);

            // calls the functions corresponding to the commands
            switch (command[0].toUpperCase()) {
            case "GET":
                // Calls the get command with the desired record number
                getCommand(bP, Integer.parseInt(command[1]));
                break;

            case "SET":
                // Calls the set command with the desired record number and the new content to
                // replace with
                setCommand(bP, Integer.parseInt(command[1]), command[2]);
                break;

            case "PIN":
                // Calls the pin command with the desired block number to pin
                pinCommand(bP, Integer.parseInt(command[1]));
                break;

            case "UNPIN":
                // Calls the unpin command with the desired block number to unpin
                unpinCommand(bP, Integer.parseInt(command[1]));
                break;

            default: // Handles any irregularities
                break;
            }

        }
    }

    // Function to handle calculations of indices and perform buffer placements
    public static int[] handleMemoryBuffer(BufferPool bPool, int index) {
        Boolean memoryLeft = false;

        int blockNum = Math.floorDiv(index, 100) + 1; // calculates which block a given record is in
        int recordNum = Math.floorMod(index, 100); // calculates which row the record is in in a block

        // handles edge cases like record no. 100 and 200
        if (recordNum == 0) {
            blockNum = blockNum - 1;
            recordNum = 100;
        }

        // checks if block is not in buffer pool
        if (bPool.checkIfBlockInBuffer(blockNum) == -1) {
            // checks if there is space in the buffer pool and swaps out blocks if allowed
            memoryLeft = bPool.blockNotInBuffer(blockNum);

            // returns null to end the attempt at command if no space left in buffer pool
            if (!memoryLeft) {
                System.out.println("The corresponding block " + blockNum
                        + " cannot be accessed because the memory buffers are full");
                return null;
            }
        } else {
            System.out.println("File " + blockNum + " already in memory");
        }

        // gets the frame number the block is currently in
        int slotNum = bPool.checkIfBlockInBuffer(blockNum);
        System.out.println("Block is in Frame " + slotNum + "; ");

        // returns the frame number, block number, and caculated record number for the
        // command
        int[] arr = new int[3];
        arr[0] = slotNum;
        arr[1] = blockNum;
        arr[2] = recordNum;
        return arr;
    }

    // Function to retrieve a record at a given index
    public static void getCommand(BufferPool bPool, int index) {

        int[] arr = handleMemoryBuffer(bPool, index);
        if (arr == null) {
            return;
        }

        // gets the record from the calculated address and prints it out
        System.out.println(bPool.getBuffers()[arr[0] - 1].getContentFromBlock(arr[2] - 1));

    }

    // Function to update a record at a given index with the given content
    public static void setCommand(BufferPool bPool, int index, String rec) {
        int[] arr = handleMemoryBuffer(bPool, index);
        if (arr == null) {
            return;
        }

        // trims out the quotation marks of the content and updates the calculated
        // address with it
        rec = rec.substring(1, rec.length() - 1);
        bPool.getBuffers()[arr[0] - 1].updateRecord(arr[2] - 1, rec);

    }

    // Function to pin a block with the given block number
    public static void pinCommand(BufferPool bPool, int blockID) {
        Boolean result = true; // whether block is in buffer pool

        // check if block in buffer
        if (bPool.checkIfBlockInBuffer(blockID) == -1) {
            // result returns true if block is succesfully placed in buffer; false if buffer
            // is buffer is full
            result = bPool.blockNotInBuffer(blockID);
        }

        if (result) {
            // Gets frame number of a given block
            int slotNum = bPool.checkIfBlockInBuffer(blockID);
            System.out.println("Pinned block in Frame " + slotNum + "; ");

            // checks the block is already pinned; pins it if not pinned already
            if (bPool.getBuffers()[slotNum - 1].getPinned()) {
                System.out.println("This block is already pinned;");
            } else {
                bPool.getBuffers()[slotNum - 1].setPinned(true);
                System.out.println("This block was not already pinned");
            }

        } else {
            // ends command if not possible to place block in buffer
            System.out.println(
                    "The corresponding block " + blockID + " cannot be pinned because the memory buffers are full");
        }
    }

    // Function to unpin a given block
    public static void unpinCommand(BufferPool bPool, int blockID) {

        // checks if block to be unpinned is in buffer
        if (bPool.checkIfBlockInBuffer(blockID) == -1) {
            // ends command if the desired block is not in memory
            System.out.println("The corresponding block 3 cannot be unpinned because it is not in memory.");
        } else {
            // gets the frame number of the desired block
            int slotNum = bPool.checkIfBlockInBuffer(blockID);
            System.out.println("Block unpinned in Frame" + slotNum + ";");

            // Checks if block is already unpinned; unpins it if not unpinned
            if (!bPool.getBuffers()[slotNum - 1].getPinned()) {
                System.out.println("This block is already unpinned; ");
            } else {
                bPool.getBuffers()[slotNum - 1].setPinned(false);
                System.out.println("This block was not already pinned");
            }

        }
    }
}

class Frame {
    private String[] content; // records of a block
    private boolean dirty; // keeps track of whether a block is modified
    private boolean pinned; // allows the block to be left in memory until unpinned
    private int blockId; // block number of the block currently in frame

    // Getters and setters for the attributes
    public boolean getDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getPinned() {
        return this.pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    // fills the frame with all records in an array
    public void setContent(String con) {
        // loop through the line to get records
        String[] arr = con.split("\\.");
        int i = 1;
        for (String rec : arr) {
            this.content[i - 1] = rec;
            i++;
        }

    }

    // initializes the frame
    public void initialize() {
        this.content = new String[101];
        this.dirty = false;
        this.pinned = false;
        this.blockId = -1;
    }

    // gets the content of a single record from the frame
    public String getContentFromBlock(int RecordId) {
        return this.content[RecordId];
    }

    // updates the content of a single record in the frame
    public String updateRecord(int RecordId, String newContent) {
        this.content[RecordId] = newContent;
        this.setDirty(true); // changes the dirty bit to true after block modification
        System.out.println("Write was successful");
        return this.content[RecordId];
    }

    // Method to clear out all the contents of the frame and ready it for next block
    public void clearOutFrame() {
        for (var i = 1; i <= 100; i++) {
            this.content[i] = "";

        }
        this.setDirty(false);
        this.setPinned(false);
        this.setBlockId(-1);
    }

    // Method to get all the records in the frame
    public String getAllContent() {
        String content = "";
        for (int i = 1; i <= 100; i++) {
            content = content + this.getContentFromBlock(i) + ".";
        }
        return content;

    }

}

class BufferPool {
    Frame[] buffers; // collection of frames
    int lastEvicted; // keeps track of which frame was a block last evicted from

    // Getters and setters of the attributes
    public Frame[] getBuffers() {
        return this.buffers;
    }

    public void setBuffers(Frame[] buffers) {
        this.buffers = buffers;
    }

    // initializes the buffer pool
    public void initialize(int arg) {
        Frame[] buf = new Frame[arg];

        for (int i = 0; i < arg; i++) {
            buf[i] = new Frame();
            buf[i].initialize();
        }

        this.setBuffers(buf);
        this.lastEvicted = -1;

    }

    // checks if a block is in any frame of the buffer pool
    public int checkIfBlockInBuffer(int blockId) {
        for (int i = 0; i < this.getBuffers().length; i++) {
            if (this.getBuffers()[i].getBlockId() == blockId) {
                return (i + 1);
            }
        }

        return -1;
    }

    // get all records from the frame with the desired block
    public String getContentFromBuffer(int blockId) {
        int bufferNum = checkIfBlockInBuffer(blockId);
        String content = "";
        if (bufferNum != -1) {
            content = content + this.getBuffers()[bufferNum].getAllContent();
        }

        return "";
    }

    // Attempts to swap out a block to make space for a new block; returns false if
    // buffer is full
    public Boolean blockNotInBuffer(int blockId) {
        String data = ""; // stores the content of the block we want to work with
        try {
            File myObj = new File("F" + blockId + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        int frameNum = findEmptyFrame(); // frame number of an empty frame, or -1 if noone are empty
        if (frameNum == -1) {
            Boolean swap = switchOutFrames(blockId); // true if a block can be taken out

            // ends command if swap is unsuccessful
            if (!swap) {
                return false;
            }

            // attempts finding empty frame again after switching out a block
            frameNum = findEmptyFrame();
        }

        // stores the new block in the empty frame
        this.getBuffers()[frameNum - 1].setContent(data);
        this.getBuffers()[frameNum - 1].setBlockId(blockId);
        System.out.println("Brought File " + blockId + " from disk");
        return true;

    }

    // Method to traverse through the buffer and find an empty frame
    private int findEmptyFrame() {
        for (int i = 0; i < this.getBuffers().length; i++) {
            if (this.getBuffers()[i].getBlockId() == -1) {
                return (i + 1);
            }
        }

        return -1;
    }

    // Attempts to swap out a frame; returns false if unsuccessful
    public Boolean switchOutFrames(int blockNum) {
        Boolean swapSuccessful = false;
        int index = this.lastEvicted;
        int i = 0;
        while (i < this.getBuffers().length) {
            i++;

            // create a circular loop for eviction
            if (index >= (this.getBuffers().length - 1)) {
                index = 0;
            } else {
                index++;
            }

            Frame fr = this.getBuffers()[index];

            // check for pinned flag and dirty flag
            if (!fr.getPinned()) {
                if (fr.getDirty()) {
                    String data = fr.getAllContent();
                    // write the updated content on disk if block was modified
                    try {
                        FileWriter myWriter = new FileWriter("F" + fr.getBlockId() + ".txt");
                        myWriter.write(data);
                        myWriter.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }
                // empties out the frame of the block to be evicted
                this.getBuffers()[index].clearOutFrame();
                this.lastEvicted = index;
                swapSuccessful = true;

                System.out.println("Evicted file from Frame " + (this.lastEvicted + 1));
                return swapSuccessful;
            }
        }

        return swapSuccessful;

    }
}
