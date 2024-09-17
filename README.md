# Codeforces Bot for Discord

![Project status](https://img.shields.io/badge/status-in%20progress-yellow)

This project is a Discord bot designed to interact with the Codeforces API, providing users with information about
contests, user standings, and more directly through Discord commands.

## Features

- **User Information**: Retrieve and display information about a Codeforces user.
- **Contest Information**: Get details about upcoming and finished contests.
- **User Contest Standings**: Check the standing of a user in a specific contest.
- **Contest Tournament**: Suggest a random contest of a specified type for users. The contest will be one that none of the given usernames have participated in before. When the contest is over, the bot will announce the winners.
- **Health Check**: A simple HTTP server for health checks.

## How to Use
1. **Invite the Bot**:
    - Click [here](https://discord.com/api/oauth2/authorize?client_id=1257793557838692473&permissions=0&scope=bot%20applications.commands) to invite the bot to your server.
    - The bot is currently hosted on a free Koyeb instance, so it may not be available all the time.
2. **Commands**:
    - `/userinfo username:<username>`: Get information about a Codeforces user.
    - `/upcoming-contests`: List upcoming Codeforces contests.
    - `/finished-contests`: List recently finished Codeforces contests.
    - `/standing username:<username> contest_id:<contestId>`: Get a user's standing in a specific contest.
    - `/random-problem ratingSart:<ratingStart> ratingEnd:<ratingEnd> [tags:<tag1,tag2,...>]`: Get a random problem from Codeforces, given rating and tags.
    - `/rating-history username:<username>`: Get the rating history graph of a user.
    <!-- /random-contest usernames: _AhmedMohamed_, Shayan contest_type: div3 start_time: 2024-09-16 07:37:00 +03:00 -->
    - `/random-contest usernames:<username1>,<username2>,... contest_type:<contestType> [start_time:<startTime>]`: Suggest a random contest for a virtual tournament. Announce the winners when the contest is over.

## Screenshots

1. **Random Problem**:


https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/550f070c-ec15-4c89-a7e4-efcd4bf00933

2. **User Information**:


https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/5aa37744-2aa0-4c7a-81f2-203d5996db62

3. **Rating History**:


https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/a38da1d5-7618-4cb7-a139-d3d969058606

4. **Upcoming and Finished Contests**:


https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/e5a56165-4d59-409e-b666-0fe53080bff2

5. **User Contest Standings**:


https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/98cdc743-214a-430b-bcef-21448ecc36d7

6. **Contest Tournament**:

After the virtual contest ends, the bot will send the participants' ranks along with details like:

- Number of solved problems
- Number of wrong answers during the contest

The bot can handle multiple time zones. The user can also confirm or cancel the command using buttons.

![image](https://github.com/user-attachments/assets/bdf178bb-ad8d-400a-af31-1c287dc6e5d0)

![image](https://github.com/user-attachments/assets/dc3589c5-2c88-4ee8-85a1-08b8b25201c0)

![image](https://github.com/user-attachments/assets/599868d4-052f-4af4-b797-2e683e2c50e0)

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

## Contributing

Contributions are welcome! Please feel free to submit a pull request.

## TODO

- [x] Return upcoming contests
- [x] Return finished contests
- [x] Return user info
- [x] Return standing of a contest for a user
- [x] Return random problem
    - [x] Rating range (required)
    - [x] Tags (optional)
- [x] Return rating history graph of a user
- [ ] Return submissions of a contest for a user
- [ ] last Submissions of a user
- [ ] Given some usernames, return a contest that none of them participated in
    - [ ] Contest type as optional (Div3, so on)
    - [ ] Gym as optional
