//
//  KeychainManager.swift
//  FanPulse
//
//  Created by 김송 on 1/2/26.
//


import UIKit
import GoogleSignIn
import Security

// MARK: - Keychain Manager
class KeychainManager {
    static let shared = KeychainManager()
    
    private let service = Bundle.main.bundleIdentifier ?? "com.app.myapp"
    private let accessTokenKey = "googleAccessToken"
    private let refreshTokenKey = "googleRefreshToken"
    private let userIDKey = "googleUserID"
    
    private init() {}
    
    // MARK: - Save Token
    func saveToken(accessToken: String, refreshToken: String?, userID: String) {
        save(key: accessTokenKey, value: accessToken)
        if let refreshToken = refreshToken {
            save(key: refreshTokenKey, value: refreshToken)
        }
        save(key: userIDKey, value: userID)
    }
    
    // MARK: - Get Token
    func getAccessToken() -> String? {
        return get(key: accessTokenKey)
    }
    
    func getRefreshToken() -> String? {
        return get(key: refreshTokenKey)
    }
    
    func getUserID() -> String? {
        return get(key: userIDKey)
    }
    
    // MARK: - Delete Token
    func deleteAllTokens() {
        delete(key: accessTokenKey)
        delete(key: refreshTokenKey)
        delete(key: userIDKey)
    }
    
    // MARK: - Check if Logged In
    func isLoggedIn() -> Bool {
        return getAccessToken() != nil
    }
    
    // MARK: - Private Methods
    private func save(key: String, value: String) {
        let data = value.data(using: .utf8)!
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }
    
    private func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }
        
        return value
    }
    
    private func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        
        SecItemDelete(query as CFDictionary)
    }
}
