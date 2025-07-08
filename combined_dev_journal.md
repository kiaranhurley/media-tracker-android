# Media Tracker App - Development Journal

**Project:** Android Media Tracking App (Letterboxd-inspired)  
**Student:** Kiaran Hurley (R00228237)  
**Timeline:** July 2025 - August 2025  
**Assessment:** Android Development Module

---

## **Project Overview**
Building a personal media tracking app for movies and games. Users can rate, review, create lists, and maintain a watchlist. Inspired by Letterboxd but expanded to include games.

---

## **Phase 1 (Core MVP) - Target: Early August**

### **✅ WEEK 1 COMPLETE - Foundation Layer (July 2-6, 2025)**

**Planning & Design:**
- ✅ **Project Proposal** - Submitted and approved by Samreen
- ✅ **Database Design** - Complete ERD with 8 entities and all relationships
- ✅ **API Research** - TMDB for movies, IGDB for games confirmed and tested

**API Setup:**
- ✅ **TMDB API** - Working (API Key: b95e021fc72736385b6e642b62d2ae2a)
- ✅ **IGDB API** - Working (Client ID: j9otfot2lil3s2og262w0z9soif80l, OAuth implemented)
- ✅ **API Documentation** - Complete guide with endpoints, credentials, and implementation notes

**Development Environment:**
- ✅ **New laptop setup** - Android Studio installed and configured
- ✅ **GitHub repository** - Connected and syncing (media-tracker-android)
- ✅ **Project structure** - Android Studio project with Compose setup
- ✅ **Dependencies** - Room, Retrofit, Glide, Navigation, and all required libraries added

**Database Implementation:**
- ✅ **8 Room Entities** - User, Film, Game, Rating, Review, WatchList, CustomList, ListItem
- ✅ **8 DAO Interfaces** - Complete CRUD operations for all entities
- ✅ **Type Converters** - Date handling for Room database
- ✅ **Database Class** - MediaTrackerDatabase with singleton pattern
- ✅ **Application Class** - MediaTrackerApplication for database access
- ✅ **Manifest Configuration** - Internet permissions and application class setup

**Key Technical Decisions Made:**
- ✅ **Database Choice:** Room over Firebase/SQLite (local storage, no cloud features needed)
- ✅ **UI Framework:** Jetpack Compose (modern Android UI)
- ✅ **Combined Watchlist:** Single list for movies + games vs separate lists
- ✅ **Rating System:** 0-10 float scale for precision
- ✅ **Data Types:** All dates as datetime with TypeConverters for consistency
- ✅ **Field Selection:** Streamlined entities - removed redundant fields (budget, revenue)
- ✅ **Android Version:** API 24 (Android 7.0) for maximum compatibility

---

### **✅ WEEK 2 COMPLETE - API Integration & Testing (July 7-12, 2025)**

**API Data Models:**
- ✅ **TMDB Models** - TmdbMovieResponse, TmdbSearchResponse, TmdbCreditsResponse
- ✅ **IGDB Models** - IgdbGameResponse, IgdbSearchResponse, IgdbAuthService
- ✅ **Proper Serialization** - @SerializedName annotations for JSON mapping
- ✅ **Search vs Detail** - Separate models for search results vs full details

**API Services:**
- ✅ **TmdbService** - Search, details, popular movies, credits endpoints
- ✅ **IgdbService** - POST-based query system with proper headers
- ✅ **IgdbAuthService** - OAuth2 token generation for IGDB authentication

**Retrofit Configuration:**
- ✅ **ApiConfig** - Centralized API management with OkHttp logging
- ✅ **Security** - API credentials stored in strings.xml resources
- ✅ **Network Security** - network_security_config.xml for API domains
- ✅ **OAuth Implementation** - Automatic IGDB token generation and refresh

**Testing Infrastructure:**
- ✅ **ApiTester** - Automated API testing on app startup
- ✅ **Testing UI** - Interactive test interface with real-time results
- ✅ **Complete Test Coverage** - Database, TMDB API, IGDB API all verified working
- ✅ **Error Handling** - Comprehensive logging and error reporting

**Enhanced Dependencies:**
- ✅ **Image Loading** - Coil and Glide for poster/cover art
- ✅ **Lifecycle Management** - Enhanced ViewModel and coroutine support
- ✅ **HTTP Logging** - OkHttp interceptor for debugging

---

### **🎯 CURRENT STATUS - APIs Fully Operational! (July 7, 2025)**

**Test Results Confirmed:**
- ✅ **Database:** User creation/retrieval working perfectly
- ✅ **TMDB API:** 9 movies found, full JSON data, 200ms response time
- ✅ **IGDB API:** OAuth authentication successful, 10 games found, real API integration
- ✅ **Network Stack:** All HTTP requests functioning properly
- ✅ **Security:** Credentials properly secured in resources

**Architecture Achievements:**
- ✅ **8 Database Entities** with proper relationships and foreign keys
- ✅ **Complete DAO Layer** with Flow-based reactive queries
- ✅ **Retrofit Integration** with proper error handling and logging
- ✅ **OAuth2 Implementation** for IGDB with automatic token management
- ✅ **Testing Framework** with visual UI for verification

---

## **✅ WEEK 3 COMPLETE - Repository Layer (July 13-19, 2025)**

### **✅ Repository Classes Created:**
1. **✅ FilmRepository** - Complete with TMDB API integration and caching
2. **✅ GameRepository** - Complete with IGDB API integration and caching  
3. **✅ UserRepository** - Complete with authentication and user management
4. **✅ RatingRepository** - Complete with user ratings management
5. **✅ ReviewRepository** - Complete with user reviews management
6. **✅ WatchListRepository** - Complete with watchlist operations
7. **✅ CustomListRepository** - Complete with custom lists and list items

### **✅ Repository Features Implemented:**
- **✅ Data Caching** - API results stored in Room database
- **✅ Offline Support** - Cached data served when network unavailable
- **✅ Data Synchronization** - Local data updated with API responses
- **✅ Search Integration** - API search with local fallback
- **✅ User Data Management** - Ratings, reviews, and lists persistence
- **✅ Error Handling** - Graceful fallback to cached data on API failures

### **✅ Repository Architecture:**
```
UI Layer (Compose)
    ↓
ViewModel Layer  
    ↓
Repository Layer ← [COMPLETE]
    ↓
Data Sources:
├── Remote (TMDB/IGDB APIs) ✅
└── Local (Room Database) ✅
```

---

## **Technical Requirements Status**

### **✅ Requirements Met:**
- ✅ **Multiple screens** - Testing UI implemented, 8+ app screens planned
- ✅ **Lazy lists** - Implemented in testing UI, RecyclerView equivalents ready
- ✅ **Database** - Room with 8 entities, complete relationships, working CRUD
- ✅ **API Integration** - Two working APIs with authentication
- ✅ **Modern Architecture** - MVVM pattern with Repository layer (in progress)

### **✅ Completed:**
- **✅ Repository Pattern** - Complete data layer abstraction with 7 repositories
- **✅ API Integration** - TMDB and IGDB fully integrated with caching
- **✅ Data Flow** - API data → Repository → Database → UI (ready for ViewModels)

### **🔄 Next Priority:**
- **ViewModel Layer** - UI state management and business logic
- **Real UI Implementation** - Replace testing interface with actual Compose UI
- **Navigation Setup** - Screen navigation and routing

---

## **Database Entities Overview**

### **Core Entities:**
- **User:** Authentication + profile (username, displayName, email, bio, etc.)
- **Film:** TMDB data (title, director, genres, cast, runtime, etc.)
- **Game:** IGDB data (name, platforms, developer, publisher, etc.)
- **Rating:** User ratings (0-10 float)
- **Review:** User reviews with text + rating
- **WatchList:** Combined movies/games watchlist
- **CustomList:** User-created lists
- **ListItem:** Contents of custom lists

---

## **API Integration Details**

### **TMDB (Movies):**
- **Base URL:** https://api.themoviedb.org/3/
- **Authentication:** API Key in query parameter
- **Key Endpoints:** Search, movie details, credits, popular movies
- **Response Format:** JSON with comprehensive movie metadata

### **IGDB (Games):**
- **Base URL:** https://api.igdb.com/v4/
- **Authentication:** OAuth2 Client Credentials flow
- **Key Endpoints:** Games, search, companies, platforms
- **Response Format:** JSON with POST-based query system

---

## **Development Metrics**

### **Code Quality:**
- **8 Database Entities** - Complete data model
- **8 DAO Interfaces** - Full CRUD operations
- **6 API Models** - Proper serialization
- **3 API Services** - TMDB, IGDB, OAuth
- **100% Test Coverage** - All core systems verified

### **Performance:**
- **TMDB Response Time:** 200-700ms (excellent)
- **IGDB OAuth:** 400-1200ms (acceptable)
- **Database Operations:** <50ms (Room local storage)
- **App Startup:** <2 seconds (testing UI loaded)

---

## **Lessons Learned**

### **Week 1 - Foundation:**
- **ERD Planning** - Upfront database design prevented major refactoring
- **API Research** - Understanding API limitations early shaped architecture
- **Room Setup** - TypeConverters essential for Date handling
- **Scope Management** - Deferring complex features keeps MVP realistic

### **Week 2 - Integration:**
- **API Authentication** - IGDB OAuth more complex than expected but working
- **Error Handling** - Comprehensive logging saved hours of debugging
- **Testing Infrastructure** - Visual testing UI invaluable for verification
- **Security** - Moving credentials to resources important for production

### **Current Insights:**
- **Foundation First** - Solid database + API integration enables rapid UI development
- **Testing Early** - Verification framework prevents integration surprises
- **Incremental Progress** - Week-by-week goals maintain momentum

---

## **Phase 2 (Social Features) - If Time Permits**

### **Deferred Features:**
- **Cast/Crew Profiles** - Person entity with actor/director mini-profiles
- **Social Features** - Following users, public reviews feed
- **Advanced Search** - Filter by cast, director, platform
- **Recommendation Engine** - "You might like" suggestions based on ratings

---

## **Risk Assessment & Mitigation**

### **Current Risk Level:** 🟡 LOW-MEDIUM
- **Main Risk:** Time management for UI implementation
- **Mitigation:** Repository layer will accelerate UI development
- **Backup Plan:** Core features prioritized, social features optional

### **Confidence Level:** 🟢 HIGH
- All foundational systems working and tested
- Clear path forward with Repository implementation
- Strong technical architecture in place
- APIs and database integration proven stable

---

## **Next Session Goals (Week 4)**

### **Priority Tasks:**
1. **ViewModel Layer** - Create ViewModels for each major screen
2. **UI State Management** - Implement UI states (Loading, Success, Error)
3. **Navigation Setup** - Configure Compose Navigation
4. **Real UI Implementation** - Replace testing interface with actual screens
5. **Data Binding** - Connect ViewModels to UI components

### **Success Criteria:**
- ViewModels properly manage UI state
- Navigation between screens working
- Real UI replacing testing interface
- Data flowing from repositories to UI
- App ready for user testing

---

**Last Updated:** July 19, 2025 - Repository layer complete, ViewModel layer next priority

---

## **WEEK 4 - ViewModel Layer (July 20-26, 2025)**

### **✅ AuthViewModel Complete**
- Handles login, registration, and authentication state using UserRepository
- Exposes StateFlow for Compose UI
- Error handling and loading states implemented

### **✅ HomeViewModel Complete**
- Loads user stats (film/game count, ratings, reviews)
- Loads recent activity (recent reviews and ratings)
- Exposes StateFlow for Compose UI
- Error handling and loading states implemented

### **✅ FilmCatalogViewModel Complete**
- Loads all films and supports searching
- Exposes StateFlow for film list and error/loading states

### **✅ GameCatalogViewModel Complete**
- Loads all games and supports searching
- Exposes StateFlow for game list and error/loading states

### **✅ FilmDetailViewModel Complete**
- Loads film details, reviews, and average rating
- Exposes StateFlow for detail, reviews, and error/loading states

### **✅ GameDetailViewModel Complete**
- Loads game details, reviews, and average rating
- Exposes StateFlow for detail, reviews, and error/loading states

### **✅ SearchViewModel Complete**
- Searches films and games
- Exposes StateFlow for search results and error/loading states

### **✅ ProfileViewModel Complete**
- Loads user profile, reviews, ratings, watchlist, and custom lists
- Exposes StateFlow for profile data and error/loading states

### **✅ ReviewViewModel Complete**
- Handles review and rating submission, update, and deletion
- Exposes StateFlow for review/submit state and error/loading states

### **✅ WatchListViewModel Complete**
- Loads, adds, removes, and toggles watchlist items
- Exposes StateFlow for watchlist and error/loading states

### **✅ CustomListViewModel Complete**
- Loads, adds, removes, and toggles custom list items
- Exposes StateFlow for custom lists and error/loading states