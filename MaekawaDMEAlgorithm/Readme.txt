
Implementation of Maekawa's Distributed Mutual Exclusion Algorithm.

input.txt file contains the info of quorum.

config.txt file contains the configuration of the nodes.

First line of configuration file contains the information of the csnode.
localhost:1111   nodeid:port number
Rest of the lines contains the configuration of other systems in the network
0:net02.utdallas.edu:2222 nodeid:host name: port number

command to compile source files and place it in bin folder.
javac -d bin src/ma/*.java

command to run the compiled files by providing the main class
java -cp ./bin ma.MaekawaAlgorithm

Else run the provided jar directly
java -jar ma.jar 0
The argument specifies the nodeid in the config file

