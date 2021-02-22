# EVHE
This is a repository for a bachelors project concerning Electronic Voting using Homomorphic Encryption

The project report can be accessed [here](https://github.com/Ernstsen/EVHE/blob/master/Report.pdf)

## Description
The project ran in multiple iterations, which can be seen in the releases. In this section a short description will be made regarding each iteration.

The general idea of the project is to attempt to create an electronic voting system which utilizes some of the properties of Homomorphic Encryption. 
The homomorphic encryption scheme used in this project is ElGamal.

## Usage
The project was created using JetBrains IntelliJ IDEA, and has files relating to this. This includes multiple runconfigurations used in testing and/or using the project. 
For a guide in using EVHE for a poll please read this [wiki entry](https://github.com/Ernstsen/EVHE/wiki/Running-EVHE)

## Process
EVHE has been created through multiple iterations, during which the system functionality has changed a lot. As the project went through these iterations the system became more secure, by placing less trust in the different entities.

### Iteration 1
In the first iteration the system assumes that all voters are honest and the same goes for the keyServer. Honest means that it wont try to trick the system, and also that it will not send "broken" data or similar.

The voters will send votes which is either 1 or 0, which the publicServer then adds up. By having the publicServer adding them up, there is no way for the keyServer, which is the only one to see the plain values, to know who voted what. After the votes has been added the, still encrypted, result is sent to the keyServer who then decrypts and publishes it

### Iteration 2
For the second iteration the main task will be to remove trust in voters.
Iteration 1 assumed that all votes were either 0 or 1, meaning they could simply be summed. Removing trust in voters means moving away from this assumption. It still needs to hold, but will have to be verified for each vote

### Iteration 3
The the third, and last, iteration trust will be removed in the keyServer.
Keyservers are replaced by decryption authorities to signal the change. The public server is also replaced with a bulletinBoard, which no longer does anything but receive and serve data.
A trusted dealer is introduced to create a distributed partial key and post vital information to the bulletin board, such as when vote terminates and public keys.
The decryption authorities will fetch data and do partial decryptions then the poll terminates. Then clients can retrieve partial decryptions and then compine them to an actual result. 

## Contributing
Even though this was created as a bachelors project, which has been finished as this is made public, pull requests are welcome.
They must adhere to the guidelines set forth i GUIDELINES.md

For thoughts and descriptions about different parts of the system, such as logging or encryption, please visit the wiki
