//
//  Post.swift
//  FanPulse
//
//  Created by 김송 on 2/1/26.
//

import UIKit

// MARK: - Post Model

struct Post {
    let id: Int
    let userName: String
    let artistName: String
    let time: String
    let hasVIPBadge: Bool
    let content: String
    let hashtags: [String]
    let imageURL: String?
    let likes: Int
    let comments: Int
    let shares: Int
}
