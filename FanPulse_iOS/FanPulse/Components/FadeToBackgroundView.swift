//
//  FadeToBackgroundView.swift
//  FanPulse
//
//  Created by 김송 on 12/17/25.
//

import SwiftUI

struct FadeToBackgroundView: View {
    let backgroundColor: Color
    let fadeColor: Color
    let startPoint: UnitPoint
    let endPoint: UnitPoint
    
    init(backgroundColor: Color, fadeColor: Color, startPoint: UnitPoint = .leading, endPoint: UnitPoint = .trailing) {
        self.backgroundColor = backgroundColor
        self.fadeColor = fadeColor
        self.startPoint = startPoint
        self.endPoint = endPoint
    }
    
    var body: some View {
        ZStack {
            backgroundColor
            
            LinearGradient(
                colors: [
                    fadeColor,
                    fadeColor.opacity(0.7),
                    fadeColor.opacity(0.3),
                    fadeColor.opacity(0),
                    fadeColor.opacity(0.3),
                    fadeColor.opacity(0.7),
                    fadeColor
                ],
                startPoint: startPoint,
                endPoint: endPoint
            )
        }
    }
}
