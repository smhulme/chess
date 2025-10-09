# ♕ Shawn's Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

##Server Design
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGQCFMmAOZQIAV2wBiACwBmABwAmAJwgYt5GYAWYDoIVoYASihmSKpgUHJIEGiYiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9FYGUAA6aADeAET1lGjAALYobbltMG0ANMO46gDu0BwDQ6PDKD3ASAhzwwC+mMI5MBms7FyU+e2dUN196wttE6rTULODw2NtSytrT21bbJzcsPs7UT5KARKJgSgACnCkWilHCAEcQtEAJTbbKiPaZWTyJQqdT5MwoMAAVQaELOFxQqOximUalUmJgHB0uQAYkhODBSZQaTAdABPGAU3piHQg4AAay5DRgkyQYD8QoalJgwAQ4o4gpQAA8wRoabj6XsAeiVPluVAaWiRCpjZkdscYAp1ShgJrypL0ABRbUqbAEBLW3apfbJdBgfL2AAMjma7T66mAhIGwy9UEseSVXRFqpdbsF8gl6C+mHQHEwBrp6jtWRtKHyaCsCAQQYx+0reNUuRA4vBFvJDRp1O0hur+2ZuQUHA40p52lbtvbI6rXZ7rvBCisCohwC3fiHFeXncZE6nM83CqtgMX+x+R0z0LBcLUTawd7+NYdmdOypFQ3y8wvLuCrlBARZoP+wybEGlA1mGGD5K4UZRnGHS-pcMAAc8wzAX4oHgZB8xbGW5iWDYtjQOwhLeKycBetIcAKDAAAyECRIkMCcfAyDhoyX75MUZRVLUBjqPEaCocKlwvLc9yzFsXG1sGmTvo6P7ZtJ4z6HcMwlopqn-Papr1jACBsRyEKsexCJImAqKKdehhLjiK4EkS-ZSVSmCKR2RrtkY+TspyFpaPIfKCp53lcdIRg1r5+Kzpa2gwNAMCNs2UWcfFDIhkZdbmoOyWpelLaKZlfLilKCQwAAkmgVBqkgM5rhw4ZIGqGiKYpaAQOCMBQEg-jMBAABmSlAjAABqlBICNhBxNVI3vJ1XWcY5fHGfkCqWJMXLdHu0BIAAXigHA+n6AYcat403iphx-Oa+0KodJ1nb6KD+uJ5X6fdsG5UpjrEk9fgvad50fZd31cV+jLwRGMDRgAjCcbQJqoSb9ABaYZvkVjA6D8nlSR5U9X1A1DSlY2Ofk00DSNgqyTMMBRDAIDQCC4Dletzm0p2+RrnIKAXn4A4aV5PlHn5WIBU605OnuVo+bF-3ZQ2KC7cLivRZLY4mvlaXq-Ll7zg5xmMgZ+RWRy4SqK+5UGRt1BqWhYvXLh+HFgp0PZH9mRw4hyEo55bt7h7EHEZwpgWNYdgWCg6DeL4ARxwnVnWFgilw47OQCdIXrMV65RetUNSiao4nNO7YHoFzPuGQcvyOlX4H2799fU6ZbHpzuofV2g9ne3WjKqzAhJgMLPcgX3A9ZTrOXS4FHLngryUCjAkAt0rcVz-kmtFbAJXldln6bUb+772lTYtuVYqulVaC1fVjUzs3NfXTdTl5VQE3bRAu1A7hAm4NPqBnftzW8bdHqAIGq9YBkN34O3+vxPa0Djpg3eiAq6q0Yahh4ghBGUZkYwHjPSDGKY2jY2gLjfGMDTp6S4sTU2Q8eaji7EyFA3ANx7knnhaeR857DxltIDhRJDB73kEfZWLDXL8JcseJBm1a7MLuo3TMZArArhtnbH6qjs7HCUcpbiKR4ZISjKWSOpEY62BBDOWw2AORSmYmCGAABxEUK1OJZz1jnQoLjC4lzMCKSuvdN6D0MRbVUIS35cUQV-CayBohuITDwsOM8P7Dx3qPIkE9X791kbzKWxhF6cnEcAcK68+6SO3nI+ku8V5hWKlffJrCT761KSlA+TSb6VRSg-OqDVkAvyiVgrq3M4lmnXn4HaKCDq0LehdL6YCzb-QiQA2ZaD5kQ0WddRB3jAY0I2XA7Z2C66wzwfDJGKM0ZkKxumKhQoDmvXoZxRhYS2xYkyQksASS1AQjScffy+QXFElzAgUe7iqkq0yU46IAAeH5NJ0jNJXK07+EyYVgHhSKRFBj3kN3vJbMEPzVBaIQBnGJbc9HfjaIEhMKYCgADomWVC9mtU5uDjEByjCjWlah6VMoZSy8x5ZLHkQ4AAdncFGFAUZvBekcHAOiAA2eAvZDA-PKl4r+jpBIVACUE4GYc4y8oAHIilZQDeuETcljBaKa81rddHeImgLcEPyUl9zGHANVPybJqDsrixcHyakJTHjk4Z-yBGApgEFZexswprw3tErKUjg0FISu0xpGUJYhpys6iZmbOkZW6XfXpj8BlNUiVPUJJzlEfy2lMv+MznpzKOaA664CVEEubSDVtGD4E7MpQop2mY1ktsOf245oz2V+3OZGQhVzSHJluTjB5qCnkWpImVUwTC0WfxkJk11KB3X2r6JG3Ngjd5yzQIbBFJtoqpoPbmtWu071hQhHKBU4K+g1WkOe9NebxkmRva+7FyUP3ykVLy39aSxn4oeqq9cx6RSkuYO0aD0hnnwd9panlIpf1YZwbOzlMBTF4Z-ZhiOIro7kX5Bwsyu0fCDQCHR5sTaABSEAOSuJFJq85VKBJFGJMJGovLgnVvQHGbACBgB0agHACAZkoBjAwxaoj2HMw2pgJxjkvqUCIn9Y6+82cJoACsuNoHdVpnTlmUP6dsqiQN+6R5hu4bk-9LTo2xrPryRNlSt5QufT57QotzgimRB0y+2btYXuHXuup8bgChcpBFrNpUuK30lGWsdvaNlQzZXWjuv9-6PPQQs9tta901lWaVzZmD8sadRfs9dZWtkVenSOs5JHLnENRkuzGqY7mZjxi1wmilXkFaq9IvmWTvkighBhjzKKvNLx430Xzgpf2Qum7UtbKBeRpeRfI-NJk31lMO4pTL989tpV6jAEa1hI5LMK6fYrMAjBuj9dENtIzd1tggao-IH2OBfbAD9hruztWZmB6D8Hz3DH+wIdy3r1zl2DdXXoGcIIDPRCwxN27ZNmPDSpqfOAfgUAgClLNGA0nZOUAU0p5mD8zvMwZIWZNFVS3VQZ6lNUGoCyel+28oN9bJnTOdPzj04E4cduWQD7tEvXTukFzL1akPcNOjzMr6Xk72u7pw4j6MjhF2JjRxQob+Q+dK4FwRTdFi-si5HjYUKyG+gLfw9IMYbMlNjE8kt+RC8uTYBd3trWKbqkAdyEdvyJ2o8O-3RE9RmiXxkqMx+PZeQDEG7naR5Cwqo5kVjrEQUTGhoWFk9xV0sBgDYGk-NAdnj+MZ9zvnQuxdajMiz1ayBaecMdxANwPACha-IBAAtNAfynMZKCwPqvzoEA0knzmgDl6ZAiL6mqMFZxVAqfcWMXCHjjAR9YXHmLK+4tAin-LhDSfOyod7+3Oup-JvZ5I6Y4VQA
