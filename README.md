# travel-lens
===

# TravelLens

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)


## Overview
### Description
Uses google maps API to get your current location or an entered location and show peoples posts around there. Peoples posts will have them reviewing places they go to.

This idea is geared towards unexpirienced travelers who want to find new places to go to while staying safe when traveling.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Travel, Social, Entertainment
- **Mobile:** This app is primarily used for mobile devices although a web version could be made over time.
- **Story:** This app is versatile because people can post reviews of all the places they go to while traveling. It doesnt need to be resturants or tourist attractions, it can also be hidden gems in a new city! This can hopefully help others out who are visiting the area to know where are some local spots and many other things. By making this locations based, we can use the idea of locals helping out traveling tourists to experience their area.
- **Market:** Everyone who likes to travel or even just explore new places in their area.
- **Habit:** You could be an everyday user just trying to find new things and places. You could also just use the app sporadically when looking for a day trip or a farther place to travel to.
- **Scope:** This app can be extended forward as a place to also find friends while traveling and create a social media feel. We can go further by matching a person to a place to visit by creating an explore page.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Login page
* Sign up page if no account
* Timeline page for your current location with search bar
* Timeline page for a different location with search bar
* Details page for each post so you can see the rating and description
* Post page where you can post a place you went to and review it.


**Optional Nice-to-have Stories**

* Personal info page with your saved posts
* Personal info page with your posts
* Settings page
* Personal info of other users page! AKA profile page for other people.
* More:
* Replying to other users feature/ chat feature.
* Explore page where the user gets many different, random locations if they dont know where to travel to next.
* Find a travel buddy explore page. Uses your location to match you to another traveler looking to explore in the place you are in.

### 2. Screen Archetypes

* Login
* Register - User signs up or logs into their account
* Timeline Location
* Explore Location
* Post Screen


### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Logout
* Compose a post
* Timeline

**Flow Navigation** (Screen to Screen)

* Forced Log-in -> Account creation if no login is not available
* Compose a post -> back to location timeline after posted
* Timeline post -> Detailed view

## Wireframes
<img src="./Screen Shot 2022-06-14 at 11.46.20 AM.png" width=1000>

## Schema
### Models
#### Post

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the user post (default field) |
| author        | Pointer to User| image author |
| image         | File     | image that user posts |
| review        | String   | image caption/review by author |
| createdAt     | DateTime | date when post is created (default field) |
| updatedAt     | DateTime | date when post is last updated (default field) |
| rating        | Number   | The rating that the author gives the post |
| location        | GeoPoint (?)   | The location the post is meant for |

#### User

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the user post (default field) |
| profilePicture         | File     | image that users profile picture |
| username       | String   | authors username |
| name       | String   | authors name |
| password       | String   | authors password |
| createdAt     | DateTime | date when post is created (default field) |
| updatedAt     | DateTime | date when post is last updated (default field) |
### Networking
#### List of network requests by screen
- Sign up page
  - (Post Request) Create new account with name, username, password, and profile picture.
- Login in screen
  - (Read/GET) Check account credentials
- Home Feed Screen at Current Location
  - (Read/GET) Query all posts where post is at that GeoPoint
     ```swift
     ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_LOCATION, ParseUser.getCurrentUser().getLocation());
    // limit query to latest 20 items
    query.setLimit(20);
    // order posts by creation date (newest first)
    query.addDescendingOrder("createdAt");
     ```
- Explore page for typed in location
  - (Read/GET) Query all posts where post is at that new GeoPoint
     ```swift
     ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_LOCATION, edittext.getText()); // sometype of way to enter a location into API
    // limit query to latest 20 items
    query.setLimit(20);
    // order posts by creation date (newest first)
    query.addDescendingOrder("createdAt");
     ```
- Detailed post page for clicked on post (won't need new api call, just pass Post object in intent)
- Create Post Screen
  - (Create/POST) Create a new post object
   
