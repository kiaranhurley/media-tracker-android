package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.UserDao
import com.kiaranhurley.mediatracker.database.entities.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    /**
     * Get a user by their ID
     */
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }
    
    /**
     * Get a user by their username
     */
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    /**
     * Get a user by their email
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
    
    /**
     * Insert a new user and return the generated ID
     */
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }
    
    /**
     * Update an existing user
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    /**
     * Delete a user
     */
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
    
    /**
     * Get all users as a Flow for reactive updates
     */
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Check if a username is already taken
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }
    
    /**
     * Check if an email is already registered
     */
    suspend fun isEmailRegistered(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
    
    /**
     * Authenticate user with username and password
     */
    suspend fun authenticateUser(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username)
        return if (user != null && user.password == password) user else null
    }
    
    /**
     * Authenticate user with email and password
     */
    suspend fun authenticateUserByEmail(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.password == password) user else null
    }
} 