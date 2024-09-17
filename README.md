# Codeforces Bot for Discord

![Project status](https://img.shields.io/badge/status-in%20progress-yellow)

This project is a Discord bot designed to interact with the Codeforces API, providing users with information about
contests, user standings, and more directly through Discord commands.

## Features

- **User Information**: Retrieve and display information about a Codeforces user.
- **Contest Information**: Get details about upcoming and finished contests.
- **User Contest Standings**: Check the standing of a user in a specific contest.
- **Compare Problem Ratings**: Get rating statistics of solved problems for a user or compare between two users.
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
    - `/problem-ratings username:<username>`: Get the rating statistics of solved problems for a user.
    - `/compare-problem-ratings username1:<username1> username2:<username2>`: Compare the rating statistics of solved problems between two users.

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


6. **Problem Rating Statistics**:

    1. User problem rating statistics:
    ![image](https://github.com/user-attachments/assets/681b2ad2-6c69-488e-bc15-fbeb8711098e)
    ![image](https://github.com/user-attachments/assets/f6f3828f-ba1d-43dc-bd18-cb47cf5f0e5c)


    2. Comparison between two users:
    ![image](https://github.com/user-attachments/assets/c4fffbd2-725e-4f80-8d10-43e1c7900fbd)
    ![image](https://github.com/user-attachments/assets/5e373b13-0907-4c92-b779-bbd1f1978715)

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
