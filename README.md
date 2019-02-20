# EVHE
This is a repository for a bachelors project concerning Electronic Voting using Homomorphic Encryption

## Description
The project ran in multiple iterations, which can be seen in the releases. In this section a short description will be made regarding each iteration.

The general idea of the project is to attempt to create an electronic voting system which utilizes some of the properties of Homomorphic Encryption. The system works by voters sending their encrypted votes to a publicServer, some times referred to as a bulletin board, which then adds them up and sends the result to be decrypted and published by a trusted key-server.

As the project goes through iterations the system will be more and more secure, by placing less trust in the different entities.

The homomorphic encryption scheme used in this project is ElGamal.

### Iteration 1
In the first iteration the system assumes that all voters are honest and the same goes for the keyServer. Honest means that it wont try to trick the system, and also that it will not send "broken" data or similar.

The voters will send votes which is either 1 or 0, which the publicServer then adds up. By having the publicServer adding them up, there is no way for the keyServer, which is the only one to see the plain values, to know who voted what. After the votes has been added the, still encrypted, result is sent to the keyServer who then decrypts and publishes it

## Contributing
Even though this was created as a bachelors project, which has been finished as this is made public, pull requests are welcome.
They must adhere to the guidelines set forth i GUIDELINES.md
