# Codeforces Bot for Discord

![Project status](https://img.shields.io/badge/status-in%20progress-yellow)

This project is a Discord bot designed to interact with the Codeforces API, providing users with information about
contests, user standings, and more directly through Discord commands.

## Features

- **User Information**: Retrieve and display information about a Codeforces user.
- **Contest Information**: Get details about upcoming and finished contests.
- **User Contest Standings**: Check the standing of a user in a specific contest.
- **Health Check**: A simple HTTP server for health checks.

## How to Use

1. **Invite the Bot**: Use the provided invitation link to add the bot to your Discord server.
2. **Commands**:
    - `/userinfo username:<username>`: Get information about a Codeforces user.
    - `/upcomingcontests`: List upcoming Codeforces contests.
    - `/finishedcontests`: List recently finished Codeforces contests.
    - `/standing username:<username> contest_id:<contestId>`: Get a user's standing in a specific contest.

## Screenshots

https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/9968dd27-21aa-4475-a67c-d22713c815e3

*Interacting with the bot in a Discord server.*

## Setup

1. **Clone the Repository**:

```
git clone https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot.git
```

2. **Build the Project** (requires Maven):

```
mvn clean install
```

3. **Run the Docker Container**:

```
docker build -t codeforces-bot .
docker run -d -p 8000:8000 codeforces-bot -t <your_bot_token>
```

> [!Note]
> Make sure to replace the guild ID in DiscordEventListener.java with your own.

## Contributing

Contributions are welcome! Please feel free to submit a pull request.

## TODO

- [x] Return upcoming contests
- [x] Return finished contests
- [x] Return user info
- [x] Return standing of a contest for a user
- [ ] Return submissions of a contest for a user
- [ ] Return random problem
    - [ ] Tag as optional
    - [ ] Rating as optional
- [ ] last Submissions of a user
- [ ] Return rating history of a user (given count, eg. last 5 contests)
- [ ] Given some usernames, return a contest that none of them participated in
    - [ ] Contest type as optional (Div3, so on)
    - [ ] Gym as optional