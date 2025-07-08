# **Android App Proposal \- Media Tracker**

**Student:** Kiaran Hurley  
 **Student Number:** R00228237

## **App Idea**

I propose to develop a media tracking application that allows users to catalog and organise their entertainment consumption across both movies and games. The app will enable users to rate content, write reviews, and maintain organised lists of media they have consumed or plan to consume.

## **Main Features**

* Browse movies and games catalogs  
* Rate content (0-10 scale) and write reviews  
* Combined "Watch List" for movies and games users want to watch/play  
* Create custom lists to organise content by genre, completion status, or personal preference  
* Personal profile showing user stats and recent activity  
* Search for movies, games, and user content

## **Technical Requirements**

**Multiple Screens:**

* Home, Films catalog, Games catalog, Search, Profile  
* Individual movie/game detail pages  
* Review writing screen, Watch List screen  
* Authentication screens (sign in/create account)

**Lazy Lists:**

* Films catalog with RecyclerView and lazy loading  
* Games catalog with RecyclerView and lazy loading  
* Search results

**Database:**

I'll use Room database over Firebase or SQLite because Room provides a clean abstraction layer over SQLite with compile-time verification, and I don't need Firebase cloud features for this personal tracking app. 

Main tables:

* Users (basic profile info)  
* Films and Games (from APIs)  
* Ratings and Reviews  
* Watch List items  
* Custom Lists

## **APIs**

* TMDB API for movie data (titles, release dates, posters, descriptions)  
* IGDB API for game information (titles, platforms, cover art, descriptions)

I think this covers all the requirements while being something I'm interested in building. Let me know if this direction works or if you have any suggestions.