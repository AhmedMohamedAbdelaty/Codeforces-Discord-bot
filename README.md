An unofficial Discord bot that provides info about contests, user standings, and more.

## Features

- **User Info**: Get details about a Codeforces user.
- **Contest Info**: Check out upcoming and finished contests.
- **User Standings**: See how a user did in a specific contest.
- **Compare Problem Ratings**: Compare the problem-solving stats of one or two users.
- **Contest Tournament**: Suggest a random contest for users. The bot will announce the winners when the contest is over.

## Redis Caching

We use Redis to cache API responses. This makes things faster and reduces unnecessary API calls.

## How to Use

1. **Invite the Bot**:
    - Click [here](https://discord.com/api/oauth2/authorize?client_id=1257793557838692473&permissions=0&scope=bot%20applications.commands) to invite the bot to your server.
    - Note: The bot is hosted on a free Koyeb instance, so it might not always be available.

2. **Commands**:
    - `/userinfo username:<username>`: Get info about a Codeforces user.
    - `/upcoming-contests`: List upcoming Codeforces contests.
    - `/finished-contests`: List recently finished Codeforces contests.
    - `/standing username:<username> contest_id:<contestId>`: See a user's standing in a specific contest.
    - `/random-problem ratingStart:<ratingStart> ratingEnd:<ratingEnd> [tags:<tag1,tag2,...>]`: Get a random problem from Codeforces based on rating and tags.
    - `/rating-history username:<username>`: Get the rating history graph of a user.
    - `/problem-ratings username:<username>`: Get the rating stats of solved problems for a user.
    - `/compare-problem-ratings username1:<username1> username2:<username2>`: Compare the rating stats of solved problems between two users.
    - `/random-contest usernames:<username1>,<username2>,... contest_type:<contestType> [start_time:<startTime>]`: Suggest a random contest for a virtual tournament. The bot will announce the winners when the contest is over.

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

   User Contest Standings](https://github.com/AhmedMohamedAbdelaty/Codeforces-Discord-bot/assets/73834838/98cdc743-214a-430b-bcef-21448ecc36d7

6. **Problem Rating Statistics**:
    1. User problem rating statistics:
       ![User Problem Rating Statistics](https://github.com/user-attachments/assets/681b2ad2-6c69-488e-bc15-fbeb8711098e)
       ![User Problem Rating Statistics](https://github.com/user-attachments/assets/f6f3828f-ba1d-43dc-bd18-cb47cf5f0e5c)

    2. Comparison between two users:
       ![Comparison Between Two Users](https://github.com/user-attachments/assets/c4fffbd2-725e-4f80-8d10-43e1c7900fbd)
       ![Comparison Between Two Users](https://github.com/user-attachments/assets/5e373b13-0907-4c92-b779-bbd1f1978715)

7. **Contest Tournament**:
   After the virtual contest ends, the bot will send the participants' ranks along with details like:
   - Number of solved problems
   - Number of wrong answers during the contest

   The bot can handle multiple time zones. The user can also confirm or cancel the command using buttons.

   ![Contest Tournament](https://github.com/user-attachments/assets/bdf178bb-ad8d-400a-af31-1c287dc6e5d0)
   ![Contest Tournament](https://github.com/user-attachments/assets/dc3589c5-2c88-4ee8-85a1-08b8b25201c0)
   ![Contest Tournament](https://github.com/user-attachments/assets/599868d4-052f-4af4-b797-2e683e2c50e0)

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
   The docker image will run the bot and a Redis server. You need to provide the bot token to run the bot.
   ```
   docker build -t codeforces-bot .
   docker run -p 8000:8000 -p 6379:6379 codeforces-bot -t <your_bot_token>
   ```

## Contributing

We'd love your help! Feel free to submit a pull request.

