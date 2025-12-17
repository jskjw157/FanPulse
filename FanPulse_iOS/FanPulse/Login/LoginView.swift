//
//  LoginView.swift
//  FanPulse
//
//  Created by 김송 on 12/16/25.
//

import SwiftUI

struct LoginView: View {
    var body: some View {
        ZStack {
            FadeToBackgroundView(
                backgroundColor: Color(hex: "#EC4899"),
                fadeColor: Color(hex: "#7E22CE"),
                startPoint: UnitPoint(x: 0, y: 0.4),
                endPoint: UnitPoint(x: 1, y: 0.6)
            )
            .ignoresSafeArea()
        }
    }
}

#Preview {
    LoginView()
}
