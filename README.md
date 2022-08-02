1# MADIgNight
## Group Member:

Yong Zi Ren (S10219524J)

Lee Wee Kang (S10222162C)

Lim Long Teck (S10221824G)

Han Xihe (S10222998D)

Keefe Cheong Wenkai (S10222423C)

## Description of App:

A revamped dating app that incorporates blogs to express their passions for potential friends and lovers to connect! Revolving around igniting sparks, the app is determined to make your love shine brighter than fireworks in a dimly lit nightscape. If you are looking for a flexible yet intuitive app featuring love with no bounds but well within the law, then look no further, because IgNight has got your back and your love story. With very little paywalls, you can experience twice the fun when scouring for connections to spice up your life.

## Contributions:
Stage 1:
Yong Zi Ren - Main Menu, Side Menu

Lee Wee Kang - View Blog, Create Blog

Lim Long Teck - Create Profile, Edit Profile

Han Xihe - View Profile, Play Store listing

Keefe Cheong Wenkai - Login, Chat List Fragment, Chat

Stage 2:
Yong Zi Ren - Video/Voice call, Delete user

Lee Wee Kang - Map function (Google api and like locations), comments section (comment & like)

Lim Long Teck - ProposeDate feature (Google Calendar), Notification Activity, Blogs Notification (Comments and likes)

Han Xihe - Activity report, profile personalisation (colour theming)

Keefe Cheong Wenkai - Chatbot, Chat requests, Chat notification and settings, Chat request notification and settings

## Appendices (features, screenshots, user guide):

### Features

* Login/Registration page
    * Enter an existing +65 number to login 
    * Enter a new +65 number to login
    * Send OTP to receive an OTP for login 
    * Wrong number reset 
    
* Main menu
    * Side menu
        * Edit profile 
        * About Us (to be implemented in Stage 2)
        * Premium Service (to be implemented in Stage 2)
        * Map (to be implemented in Stage 2)
        * Terms and Conditions (to be implemented in Stage 2)
        * Create Blogs 
        * Log Out  
        * Delete User
    * Search for other users
    * 'Next' button to cycle to the next user
    * 'IgNight' button to send a chat request to the user
    * 'Home' button to return to home
* View Profile
    * Displays profile information
        * Username and Age
        * RecyclerView interests
        * About Me
        * What I'm Looking For 
    * 'Blog' button to open the blog for the user 
    * 'IgNight' button to send a chat request to the user
* Chat list
    * Shows RecyclerView of all IgNighted chats 
    * Select chats to open chat
* Chat 
    * Chat with an IgNighted user
    * Send texts
    * Send media
    * Date log for each session
    * Time log for each session 
    * Time sent
    * Last seen online date and time
    * Video Call
    * Chat notifications are sent when a new message is sent
* Blogs
    * Create posts
    * See posts by other users
    * Like posts 
* Create blog posts
    * Upload post media
    * Set post description
    * Set app location
    * Create post 
 * Create profile / Edit profile (for existing users)
    * Create profile for new users
    * Set username 
    * Set gender and age
    * Upload profile picture
    * Set 'About Me'
    * Add 'Interests'
    * Set 'Preferences' for relationship type and gender 
    * Add date location
    * Save profile changes 
 * Chatbot
    * Chat with a bot, accessed from chatlist page
 * Chat requests
    * Send chat requests to another user
    * Accept chat requests to start chat with the user
    * Notifications sent when new requests are sent or when requests are responded to
 * Notification settings
    * Users can enable or disable push notification for chat and chat request notifications
    * Users can customize notification settings for chat and chat request notifications
    * Can control settings including ringtone, vibration and priority
* Map function
    * Check out various dating locations spread across Singapore
    * Displays dating locations dynamically using user dating location preferences
    * Uses Google Maps API to display location
    * Upon clicking the map cursor, brings the user to Google Maps app showing possible directions
    * Like and save dating locations to be used next time
* Comments section
    * Express interest to user's blog
    * Blog owner can view the profile of the commenters and see if they are their type
    * Like comments to say thanks
    * View the time of comment     
* Propose Date
    * For more introverted user who do not have the courage to ask for a date
    * Use this feature to propose a date to the opposite party
    * The initiating party would have to fill up the Main Activity, Description and Date Time for the date
    * Accept and Decline buttons would be provided for the opposite party to choose to either accept or reject the proposal
* Notification Activity 
    * Capture the blog notifications
    * Notification that would be displayed are the user who liked and commented on their blog
    * The profile image of the user would be displayed on the notification activity
    * The associated blog's image would also be displayed
    * If the user click on the blog image, he/she would be brought to either the Blog Activity or Commentseciton Activity
 * Activity Report 
    * Collects the user's data: 
        * Number of chats 
        * Target user of each chat 
        * Number of texts sent between both users 
        * Amount of time the user uses IgNight for each day
    * Displays the user's data in an informative graphical manner:
        * Top 3 IgNights by the total number of texts sent between both users as a pie chart 
        * Top IgNight user, where the current user has the largest total number of texts sent between both parties
        * Amount of time the user uses IgNight for current day as a bar graph
        * Bar graph has a second value that shows the "goal" amount of time the user sets as comparison to the actual amount of time they spent
        * Displays the amount of time the user uses IgNight for current day in hours and minutes * Profile Personalisation
    * Colour theming of view profile, unique to each user 
    
### Play Store listing screenshots
![FeatureGraphic6](https://user-images.githubusercontent.com/103987209/182181996-9ec54524-2193-4877-bb7c-641370a1d502.png)
![Slide1](https://user-images.githubusercontent.com/103987209/182182002-701beb90-23ff-4893-ab60-f5760d42347c.PNG)
![Slide2](https://user-images.githubusercontent.com/103987209/182182009-70346355-2ea7-4c69-96e1-ba502ba3e77d.PNG)
![Slide3](https://user-images.githubusercontent.com/103987209/182182014-5889a46d-c9a0-4128-a5d5-e7d48ce17bc5.PNG)
![Slide4](https://user-images.githubusercontent.com/103987209/182182020-ca6b70ea-8078-4aed-ace8-aa8231d93135.PNG)
![Slide5](https://user-images.githubusercontent.com/103987209/182182026-7311948d-a0e7-4bf1-8567-56517b3fcb93.PNG)
![Slide6](https://user-images.githubusercontent.com/103987209/182182028-0e92f689-a178-4d83-9efd-c5d198ede45a.PNG)

### User guide

To begin for both existing and new users, enter your eight-digit phone number and tap on the OTP button. Receive your OTP via SMS and enter it into the OTP box. Tap on the Reset button for the wrong number. 
Once logged in, tap your profile picture on the top left to view the side menu, and the Search box to search for users based on their username. Tap on the Next button to cycle through users to IgNight with, and IgNight to IgNight (Start a chat) with the target user. Tap on Chat to view all existing IgNighted chats. Blog shows the blog posts by the user, while IgNight starts a chat with the user. 
In chat, select the text box and the send button to write and send texts. Use the image button to send media. 
