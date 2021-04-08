Name: Rimsha Kayastha

Database Systems II- Project 1

This Java program is a simulation of how the database system works internally. After specifying the number of frames desired, you can run commands infinitely until you press ctrl+c. The commands taken in are Get, Set, Pin, and Unpin.

Section I: Compilation and execution

For compilation and execution of this code, make sure you have the appropriate java environment installed.

To compile this code, run the following command:
javac Main.java

To execute this code, run the following command with the number of frames you want in your buffer pool:
java Main [Number of frames in buffer]

Section II: Test results

When running this code against the given test cases and my own test cases, all tests passed.

Section III:
 The few design changes that I brought into this code were creatin methods that made the code less redundant and more flexible. 

 Addtional attribute for BufferPool was 'lastEvicted'.

 Additional methods for the classes were:
 Main
   -handleMemoryBuffer()
   -getCommand()
   -setCommand()
   -pinCommand()
   -unpinCommand()
 Frame
   -setContent()
   -clearOutFrame()
   -getAllContent()
 BufferPool
   -switchOutFrames()