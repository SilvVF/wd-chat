# wd-chat

## Generated Dependency Graph
![](dependency-graph/project.dot.png)

## Features
- Uses WiFi Direct to create a P2p connection between devices.
- Runs a ktor server on the group owner of the wifi P2p group and acts as the backend for the chat.
- Uses Websockets to transmit messages and images betweens multiple devices.
- All images are stored locally and will be deleted after if not saved.

## Tech Stack 
![Dagger Hilt](https://dagger.dev/hilt/)
