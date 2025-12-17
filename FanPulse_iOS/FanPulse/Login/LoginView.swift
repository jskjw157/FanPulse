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
            
            VStack(spacing: 0) {
                Spacer()
                    .frame(height: 80)
                
                VStack(spacing: 8) {
                    Text("Welcome to FanPulse")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("글로벌 K-POP 팬들의 인터랙티브 플랫폼")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.9))
                }
                .padding(.bottom, 40)
                
                VStack {
                    // TODO: 로그인  . . .
                }
                .frame(maxWidth: .infinity)
                .frame(height: 450)
                .background(Color.white)
                .cornerRadius(30)
                .padding(.horizontal, 24)
                
                Spacer()
            }
        }
    }
}

#Preview {
    LoginView()
}
