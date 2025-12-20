//
//  LoginView.swift
//  FanPulse
//
//  Created by 김송 on 12/16/25.
//

import SwiftUI

struct LoginView: View {
    @State private var email = ""
    @State private var password = ""
    @State private var isLoginSelected = true
    
    var body: some View {
        ZStack {
            // 배경 그라데이션
            FadeToBackgroundView(
                backgroundColor: Color(hex: "#7E22CE"),
                fadeColor: Color(hex: "#EC4899"),
                startPoint: UnitPoint(x: 0, y: 0.4),
                endPoint: UnitPoint(x: 1, y: 0.6)
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                Spacer()
                    .frame(height: 80)
                
                // 타이틀
                VStack(spacing: 8) {
                    Text("Welcome to FanPulse")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("글로벌 K-POP 팬들의 인터랙티브 플랫폼")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.9))
                }
                .padding(.bottom, 40)
                
                // 로그인 폼 카드
                VStack(spacing: 0) {
                    HStack(spacing: 0) {
                        Button(action: {
                            isLoginSelected = true
                        }) {
                            Text("로그인")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(isLoginSelected ? .white : .gray)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(
                                    isLoginSelected ? Color(hex: "#7E22CE") : Color.clear
                                )
                                .cornerRadius(25)
                        }
                        
                        Button(action: {
                            isLoginSelected = false
                        }) {
                            Text("회원가입")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(!isLoginSelected ? .white : .gray)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(
                                    !isLoginSelected ? Color(hex: "#7E22CE") : Color.clear
                                )
                                .cornerRadius(25)
                        }
                    }
                    .padding(4)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(25)
                    .padding(.horizontal, 24)
                    .padding(.top, 24)
                    
                    VStack(spacing: 16) {
                        Button(action: {
                            
                        }) {
                            HStack {
                                Image("googleLoginBtn")
                                    .resizable()
                                    .scaledToFill()
                            }
                        }
                        
                        HStack {
                            Rectangle()
                                .fill(Color.gray.opacity(0.3))
                                .frame(height: 1)
                            Text("또는")
                                .font(.system(size: 13))
                                .foregroundColor(.gray)
                                .padding(.horizontal, 12)
                            Rectangle()
                                .fill(Color.gray.opacity(0.3))
                                .frame(height: 1)
                        }
                        .padding(.vertical, 8)
                        
                        // 이메일 입력
                        VStack(alignment: .leading, spacing: 8) {
                            Text("이메일")
                                .font(.system(size: 13))
                                .foregroundColor(.gray)
                            
                            TextField("", text: $email)
                                .padding()
                                .background(Color.gray.opacity(0.1))
                                .cornerRadius(12)
                                .autocapitalization(.none)
                                .keyboardType(.emailAddress)
                        }
                        
                        // 비밀번호 입력
                        VStack(alignment: .leading, spacing: 8) {
                            Text("비밀번호")
                                .font(.system(size: 13))
                                .foregroundColor(.gray)
                            
                            SecureField("", text: $password)
                                .padding()
                                .background(Color.gray.opacity(0.1))
                                .cornerRadius(12)
                        }
                        
                        // 로그인 버튼
                        Button(action: {
                            
                        }) {
                            Text("로그인")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(
                                    LinearGradient(
                                        colors: [Color(hex: "#EC4899"), Color(hex: "#9333EA")],
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .cornerRadius(25)
                        }
                        .padding(.top, 8)
                        
                        // 비밀번호 찾기
                        Button(action: {
                            
                        }) {
                            Text("비밀번호를 잊으셨나요?")
                                .font(.system(size: 13))
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 24)
                }
                .background(Color.white)
                .cornerRadius(30)
                .padding(.horizontal, 24)
                
                Spacer(minLength: 100)
            }
        }
    }
}

#Preview {
    LoginView()
}
